package com.fuyu.basic.support.security.plugin;

import com.fuyu.basic.commons.exception.ServiceException;
import com.fuyu.basic.commons.tree.Tree;
import com.fuyu.basic.commons.tree.TreeUtils;
import com.fuyu.basic.support.security.entity.RoleAuthority;
import com.fuyu.basic.support.security.enumerate.ResourceSource;
import com.fuyu.basic.support.security.enumerate.ResourceType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.ast.MethodReference;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.fuyu.basic.support.security.entity.SecurityUserDetails.*;

/**
 * 插件信息终端
 *
 * @author maurice
 */
@Endpoint(id = "plugin")
public class PluginEndpoint {

    private final static Logger LOGGER = LoggerFactory.getLogger(PluginEndpoint.class);

    /**
     * 默认的插件节点名称
     */
    public final static String DEFAULT_PLUGIN_KEY_NAME = "plugin";

    /**
     * 信息奉献者集合
     */
    private final List<InfoContributor> infoContributors = new ArrayList<>();

    /**
     * 需要扫描的包路径
     */
    private List<String> basePackages = new ArrayList<>(16);

    /**
     * spring 资源解析器
     */
    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    /**
     * spring 元数据读取工厂
     */
    private final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);

    /**
     * spring security 方法表达式具柄
     */
    private final MethodSecurityExpressionHandler mse = new DefaultMethodSecurityExpressionHandler();

    /**
     * 缓存值
     */
    private final Map<String, Object> cache = new LinkedHashMap<>();

    /**
     * 找不到父类的插件信息
     */
    private Map<String,PluginInfo> parent = new LinkedHashMap<>();

    /**
     * 并发锁
     */
    private final Lock lock = new ReentrantLock();

    public PluginEndpoint(List<InfoContributor> infoContributors) {
        this.infoContributors.addAll(infoContributors);
    }

    @ReadOperation
    public Map<String, Object> plugin() {
        // 如果缓存没有，就去扫描遍历
        lock.lock();

        try {

            if (cache.isEmpty()) {

                Info.Builder builder = new Info.Builder();

                for (InfoContributor contributor : this.infoContributors) {
                    contributor.contribute(builder);
                }

                Info build = builder.build();

                Map<String, Object> info = new LinkedHashMap<>();

                Map<String, Object> details = build.getDetails();

                if (MapUtils.isNotEmpty(details)) {
                    info.putAll(details);
                }

                List<Tree<String, PluginInfo>> enumMap = resolvePlugin();

                info.put(DEFAULT_PLUGIN_KEY_NAME, enumMap);

                cache.putAll(info);
            }

            return cache;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 解析资源信息
     */
    private List<Tree<String, PluginInfo>> resolvePlugin() {
        LOGGER.info("开始解析 info.plugin 信息");

        List<Tree<String, PluginInfo>> result = new ArrayList<>();

        // 扫描所有带 Controller 注解的类
        Set<Class<?>> classes = resolvePlaceholders();
        // 如果找不到，什么都不做
        if (CollectionUtils.isEmpty(classes)) {
            return result;
        }

        List<PluginInfo> pluginInfoList = new ArrayList<>();

        // 循环解析类中的方法
        for (Class<?> target : classes) {

            Plugin plugin = AnnotationUtils.findAnnotation(target, Plugin.class);
            PluginInfo parent = null;

            if (plugin != null) {

                List<ResourceSource> sources = Arrays.asList(plugin.source());

                if (sources.contains(ResourceSource.Console) || sources.contains(ResourceSource.All)) {
                    parent = new PluginInfo(plugin);

                    // 如果类头存在 RequestMapping 注解，需要将该注解的 value 合并起来
                    RequestMapping mapping = AnnotationUtils.findAnnotation(target, RequestMapping.class);

                    if (mapping != null) {

                        List<String> uri = new ArrayList<>();

                        for (String value : mapping.value()) {
                            // 添加 /** 通配符，作用是为了可能某些需要带参数过来
                            String url = StringUtils.appendIfMissing(value, "/**");
                            // 删除.（逗号）后缀的所有内容，作用是可能有些配置是
                            // 有.html 和 .json的类似配置，但其实一个就够了
                            uri.add(RegExUtils.removePattern(url, "\\{.*\\}"));
                        }

                        parent.setValue(StringUtils.join(uri, ","));

                    }

                    pluginInfoList.add(parent);

                }
            }

            // 如果该 plugin 配置没有 id 值，就直接用类名做 id 值
            if (parent != null && StringUtils.isEmpty(parent.getId())) {
                parent.setId(target.getName());
            }

            Method[] methods = target.isInterface() ? target.getMethods() : target.getDeclaredMethods();
            List<Method> methodList = Arrays.asList(methods);
            // 遍历方法级别的 plugin 注解。并添加到 parent 中
            pluginInfoList.addAll(buildPluginInfo(methodList, parent));
        }

        parent.values().forEach(p->{
            p.setParent(PluginInfo.DEFAULT_ROOT_PARENT_NAME);

            if (StringUtils.isEmpty(p.getSource())) {
                p.setSource(ResourceSource.Console.toString());
            }

            if (StringUtils.isEmpty(p.getType())) {
                p.setType(ResourceType.Menu.toString());
            }

        });

        pluginInfoList.addAll(parent.values());

        LOGGER.info("找到" + cache.size() + "条记录信息");

        result = TreeUtils.buildTree(pluginInfoList);

        return result;
    }

    /**
     * 遍历方法级别的 {@link Plugin} 信息，并将值合并的 {@link PluginInfo#getChildren()} 中
     *
     * @param methodList 方法集合
     * @param parent     根节点信息
     *
     */
    private List<PluginInfo> buildPluginInfo(List<Method> methodList, PluginInfo parent) {
        List<PluginInfo> result = new ArrayList<>();

        for (Method method : methodList) {
            // 如果找不到 PluginInfo 注解，什么都不做
            Plugin plugin = AnnotationUtils.findAnnotation(method, Plugin.class);

            if (plugin == null) {
                continue;
            }

            List<ResourceSource> sources = Arrays.asList(plugin.source());

            if (!sources.contains(ResourceSource.Console) && !sources.contains(ResourceSource.All)) {
                continue;
            }
            // 获取请求 url 值
            List<String> values = getRequestValues(method, parent);

            if (values.isEmpty()) {
                continue;
            }

            PluginInfo target = new PluginInfo(plugin);

            // 如果方法级别的 plugin 信息没有 id，就用方法名称做 id
            if (StringUtils.isEmpty(target.getId())) {
                target.setId(method.getName());
            }

            if (StringUtils.isEmpty(target.getParent()) && parent != null) {
                target.setParent(parent.getId());
            }

            target.setValue(StringUtils.join(values, ","));

            List<String> authorize = getSecurityAuthorize(method);

            if (!authorize.isEmpty()) {
                target.setAuthority(StringUtils.join(authorize, ","));
            }

            result.add(target);
        }

        return result;
    }

    private List<String> getSecurityAuthorize(Method method) {

        // 获取 RequestMapping 的 value 信息
        List<String> values = new ArrayList<>();

        PreAuthorize preAuthorize = AnnotationUtils.findAnnotation(method, PreAuthorize.class);

        if (preAuthorize != null) {
            String v = preAuthorize.value();

            List<String> methodValues = getAuthorityMethodValue(v);

            values.addAll(methodValues);
        }

        PostAuthorize postAuthorize = AnnotationUtils.findAnnotation(method, PostAuthorize.class);

        if (postAuthorize != null) {
            String v = postAuthorize.value();

            List<String> methodValues = getAuthorityMethodValue(v);

            values.addAll(methodValues);
        }

        return values;
    }

    private List<String> getAuthorityMethodValue(String value) {

        Expression expression = mse.getExpressionParser().parseExpression(value);
        SpelExpression spelExpression = (SpelExpression) expression;
        SpelNode spelNode = spelExpression.getAST();

        return getAuthorityMethodValue(spelNode);
    }

    private List<String> getAuthorityMethodValue(SpelNode spelNode) {

        List<String> result = new ArrayList<>();

        if (MethodReference.class.isAssignableFrom(spelNode.getClass())) {

            MethodReference mr = (MethodReference) spelNode;

            if (DEFAULT_SUPPORT_SECURITY_METHOD_NAME.contains(mr.getName())) {


                if (mr.getName().equals(DEFAULT_IS_AUTHENTICATED_METHOD_NAME)) {
                    result.add(DEFAULT_IS_AUTHENTICATED_METHOD_NAME);
                } else {
                    for (int i = 0; i < mr.getChildCount(); i++) {
                        String value = mr.getChild(i).toString().replaceAll("'", "");

                        if (DEFAULT_ROLE_PREFIX_METHOD_NAME.contains(mr.getName())) {
                            value = RoleAuthority.DEFAULT_ROLE_PREFIX + value;
                        }

                        result.add(value);
                    }
                }

                return result;
            }
        }

        for (int i = 0; i < spelNode.getChildCount(); i++) {
            SpelNode child = spelNode.getChild(i);
            result.addAll(getAuthorityMethodValue(child));
        }

        return result;
    }

    /**
     * 获取多个请求 uri 信息，并用 ，（逗号）分割
     *
     * @param targetValue 目标 url 值
     * @param parent 父类 plugin
     * @return 多个请求 uri 信息，并用 ，（逗号）分割
     */
    private String getRequestValueString(String targetValue, PluginInfo parent) {

        List<String> uri = new ArrayList<>();

        for (String parentValue : StringUtils.split(parent.getValue())) {
            for (String value : StringUtils.split(targetValue)) {
                parentValue = StringUtils.remove(parentValue, "**");
                String url = StringUtils.appendIfMissing(parentValue, value + "/**");
                uri.add(RegExUtils.removeAll(url, "\\{.*\\}"));
            }
        }

        return StringUtils.join(uri, ",");
    }

    /**
     * 获取方法名请求值
     *
     * @param method 方法
     * @param parent 父类节点
     * @return 请求值集合
     */
    private List<String> getRequestValues(Method method, PluginInfo parent) {

        // 获取 RequestMapping 的 value 信息
        List<String> values = new ArrayList<>();

        // 如果找不到 RequestMapping 注解，什么都不做
        RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);

        if (requestMapping != null) {
            values = Arrays.asList(requestMapping.value());
        }


        // 如果为空值，表示可能是 GetMapping 注解
        if (values.isEmpty()) {

            // 如果找不到 GetMapping 注解，什么都不做
            GetMapping annotation = AnnotationUtils.findAnnotation(method, GetMapping.class);

            if (annotation != null) {
                values = Arrays.asList(annotation.value());
            }

        }

        // 如果为空值，表示可能是 PostMapping 注解
        if (values.isEmpty()) {

            // 如果找不到 PostMapping 注解，什么都不做
            PostMapping annotation = AnnotationUtils.findAnnotation(method, PostMapping.class);

            if (annotation != null) {
                values = Arrays.asList(annotation.value());
            }

        }

        // 如果为空值，表示可能是 PutMapping 注解
        if (values.isEmpty()) {

            // 如果找不到 PutMapping 注解，什么都不做
            PutMapping annotation = AnnotationUtils.findAnnotation(method, PutMapping.class);

            if (annotation != null) {
                values = Arrays.asList(annotation.value());
            }

        }

        // 如果为空值，表示可能是 DeleteMapping 注解
        if (values.isEmpty()) {

            // 如果找不到 PutMapping 注解，什么都不做
            DeleteMapping annotation = AnnotationUtils.findAnnotation(method, DeleteMapping.class);

            if (annotation != null) {
                values = Arrays.asList(annotation.value());
            }

        }

        // 如果为空值，表示注解没命名，直接用方法名
        if (values.isEmpty()) {
            values = Collections.singletonList(method.getName());
        }

        return values.stream()
                .map(v -> parent == null ? v : getRequestValueString(v, parent))
                .collect(Collectors.toList());
    }

    /**
     * 扫描包含 Controller 注解的所有类
     *
     * @return 包含 Controller 注解的所有类
     */
    private Set<Class<?>> resolvePlaceholders() {
        Set<Class<?>> classes = new HashSet<>();

        for (String basePackage : basePackages) {
            String classPath = ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage)) + "/**/*.class";

            TypeFilter filter = new AnnotationTypeFilter(Plugin.class);

            try {
                Resource[] resources = this.resourcePatternResolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + classPath);

                for (Resource resource : resources) {
                    if (resource.isReadable()) {
                        MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                        if (filter.match(metadataReader, metadataReaderFactory)) {
                            classes.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
                        }
                    }
                }
            } catch (Exception e) {
                throw new ServiceException(e);
            }
        }

        return classes;
    }

    public Map<String, PluginInfo> getParent() {
        return parent;
    }

    public void setParent(Map<String, PluginInfo> parent) {
        this.parent = parent;
    }

    @PostConstruct
    public void init() {
        for (Map.Entry<String, PluginInfo> entry : parent.entrySet()) {
            entry.getValue().setId(entry.getKey());
        }
    }

    /**
     * 设置要扫描的包路径
     *
     * @param basePackages 包路径
     */
    public void setBasePackages(List<String> basePackages) {
        this.basePackages = basePackages;
    }

    /**
     * 获取要扫描的包路径
     *
     * @return 要扫描的包路径集合
     */
    public List<String> getBasePackages() {
        return basePackages;
    }
}
