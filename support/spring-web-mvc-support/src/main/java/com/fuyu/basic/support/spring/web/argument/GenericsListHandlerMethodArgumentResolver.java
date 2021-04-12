package com.fuyu.basic.support.spring.web.argument;

import com.fuyu.basic.support.spring.web.argument.annotation.GenericsList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

/**
 * 泛型集合参数解析器，用于在表单提交动态集合参数(param[0].name, param[1].name,param[2].name)时使用
 *
 * @author maurice
 */
public class GenericsListHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private Validator validator;

    public GenericsListHandlerMethodArgumentResolver() {

    }

    public GenericsListHandlerMethodArgumentResolver(Validator validator) {
        this.validator = validator;
    }

    /**
     * 重写父类方法，该 argument resolver 仅仅支持带有 {@link GenericsList} 注解, 并且参数类型是 List 的
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(GenericsList.class) && List.class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * 重写父类方法，解析 request 的参数信息
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        GenericsList genericsList = parameter.getParameterAnnotation(GenericsList.class);

        Objects.requireNonNull(genericsList, parameter + "找不到 GenericsList 注解");

        String name = StringUtils.isEmpty(genericsList.name()) ? parameter.getParameterName() : genericsList.name();
        // 获取参数为 name 做前缀的参数集合
        List<Map<String, Object>> param = getParameter(name, webRequest);

        // 获取该参数的泛型 class
        Class<?> type = ResolvableType.forMethodParameter(parameter).asCollection().resolveGeneric();
        // 如果参数为空
        if (param.isEmpty()) {
            // 并且 GenericsList 注解的 required 等于 true， 表示该值必须要传，但又没转，直接抛出异常
            if (genericsList.required()) {
                throw new MissingServletRequestParameterException(name, parameter.getGenericParameterType().toString());
            }
            // 如果 GenericsList 注解的 required 等于 false，那就返回 null 回去
            return null;
        }
        // 如果有参数，并且该参数的泛型是 Map 那就直接返回 param 即可
        if (Map.class.isAssignableFrom(type)) {
            return param;
        } else { // 否则走 spring mvc 的流程
            List<Object> result = new ArrayList<>();

            for (Map<String, Object> map : param) {
                Object o = BeanUtils.instantiateClass(type);

                WebDataBinder binder = binderFactory.createBinder(webRequest, o, name);

                MutablePropertyValues mutablePropertyValues = new MutablePropertyValues(map);
                binder.bind(mutablePropertyValues);

                Valid valid = parameter.getParameterAnnotation(Valid.class);

                if (valid != null && validator instanceof org.springframework.validation.Validator) {
                    binder.setValidator(validator);
                    binder.validate();
                    if (binder.getBindingResult().hasErrors()) {
                        throw new BindException(binder.getBindingResult());
                    }
                }

                result.add(o);
            }

            return result;
        }

    }

    /**
     * 获取参数信息
     *
     * @param name       参数名称
     * @param webRequest request 对象
     * @return 参数信息集合
     */
    private List<Map<String, Object>> getParameter(String name, NativeWebRequest webRequest) {

        List<Map<String, Object>> result = new ArrayList<>();

        // 排序树 map
        Map<Integer, Map<String, Object>> temp = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer target, Integer source) {
                return target.compareTo(source);
            }
        });

        putParameterValue(temp, name, webRequest.getParameterMap());

        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        MultipartHttpServletRequest multipartRequest = WebUtils.getNativeRequest(servletRequest, MultipartHttpServletRequest.class);

        if (multipartRequest != null) {
            putParameterValue(temp, name, multipartRequest.getFileMap());
        }

        // 上面做完后，在将所有 key 加入到 result 中。保持值的顺序
        for (Integer key : temp.keySet()) {
            result.add(temp.get(key));
        }

        return result;
    }

    /**
     * 设置参数到 result 参数里
     *
     * @param result    result map
     * @param name      参数名称
     * @param parameter 参数集合
     */
    private void putParameterValue(Map<Integer, Map<String, Object>> result, String name, Map<String, ?> parameter) {

        // 获取所有参数信息
        for (Map.Entry<String, ?> entry : parameter.entrySet()) {
            String key = entry.getKey();
            if (StringUtils.startsWith(key, name)) {
                // 获取当前参数的索引值
                int index = Integer.parseInt(StringUtils.substringBetween(key, "[", "]"));
                Map<String, Object> data = new LinkedHashMap<>();
                // 如果存在该索引值，先 get 出来
                if (result.containsKey(index)) {
                    data = result.get(index);
                } else {
                    result.put(index, data);
                }

                Object value = entry.getValue();

                if (value instanceof String[]) {
                    String[] tempValue = (String[]) value;
                    value = tempValue.length > 1 ? tempValue : tempValue[0];
                }

                data.put(StringUtils.substringAfterLast(key, "."), value);
            }
        }

    }

    /**
     * 设置验证器
     *
     * @param validator 验证器
     */
    public void setValidator(Validator validator) {
        this.validator = validator;
    }
}

