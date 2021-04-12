package com.fuyu.basic.support.security.plugin;

import com.fuyu.basic.commons.Casts;
import com.fuyu.basic.commons.tree.Tree;
import com.fuyu.basic.support.security.entity.ResourceAuthority;
import com.fuyu.basic.support.security.enumerate.ResourceSource;
import com.fuyu.basic.support.security.enumerate.ResourceType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 插件信息
 *
 * @author maurice.chen
 */
public class PluginInfo extends ResourceAuthority implements Tree<String, PluginInfo> {

    private static final long serialVersionUID = -6354440242310314107L;

    /**
     * 默认的子节点字段名
     */
    public static final String DEFAULT_CHILDREN_NAME = "children";

    /**
     * 默认的来源字段名
     */
    public static final String DEFAULT_SOURCE_NAME = "source";

    /**
     * 顶级父类表示
     */
    public static final String DEFAULT_ROOT_PARENT_NAME = "root";

    /**
     * id
     */
    private String id;

    /**
     * 图标名称
     */
    private String icon;

    /**
     * 顺序值
     */
    private Integer sort = 0;

    /**
     * 类型：参考 {@link ResourceType};
     */
    private String type;

    /**
     * 来源: 参考 {@link ResourceSource};
     */
    private String source;

    /**
     * 父类
     */
    private String parent;

    /**
     * 子节点
     */
    private List<Tree<String, PluginInfo>> children = new ArrayList<>();

    /**
     * 备注
     */
    private String remark;

    /**
     * 插件信息
     */
    public PluginInfo() {

    }

    /**
     * 插件信息
     *
     * @param plugin 插件信息
     */
    public PluginInfo(Plugin plugin) {
        this(plugin, new ArrayList<>());
    }

    /**
     * 插件信息
     *
     * @param plugin   插件信息
     * @param children 子节点
     */
    public PluginInfo(Plugin plugin, List<Tree<String, PluginInfo>> children) {
        this.setId(plugin.id());

        this.parent = plugin.parent();
        this.children = children;

        this.setName(plugin.name());
        this.icon = plugin.icon();
        this.sort = plugin.sort();
        this.setType(plugin.type().toString());
        this.setSource(StringUtils.join(plugin.source(), ","));
        this.remark = plugin.remark();
    }

    /**
     * 获取 id
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * 设置 id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取图标
     *
     * @return 图标值
     */
    public String getIcon() {
        return icon;
    }

    /**
     * 设置图标
     *
     * @param icon 图标值
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * 设置父类 id
     *
     * @param parent 父类 id
     */
    public void setParent(String parent) {
        this.parent = parent;
    }

    /**
     * 设置子节点
     *
     * @param children 子节点
     */
    public void setChildren(List<Tree<String, PluginInfo>> children) {
        this.children = children;
    }

    /**
     * 获取顺序值
     *
     * @return 顺序值
     */
    public Integer getSort() {
        return sort;
    }

    /**
     * 设置顺序值
     *
     * @param sort 顺序值
     */
    public void setSort(Integer sort) {
        this.sort = sort;
    }

    /**
     * 获取备注
     *
     * @return 备注
     */
    public String getRemark() {
        return remark;
    }


    /**
     * 获取资源类型
     *
     * @return MENU.菜单类型, SECURITY.资源类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置资源类型
     *
     * @param type MENU.菜单类型, SECURITY.资源类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取来源
     *
     * @return FRONT.前端, ADMIN.管理后台, USER_CENTER.用户中心
     */
    public String getSource() {
        return source;
    }

    /**
     * 设置来源
     *
     * @param source FRONT.前端, ADMIN.管理后台, USER_CENTER.用户中心
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 设置备注
     *
     * @param remark 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public List<Tree<String, PluginInfo>> getChildren() {
        return children;
    }

    @Override
    public String getParent() {
        return parent;
    }

    @Override
    public boolean isChildren(Tree<String, PluginInfo> parent) {
        PluginInfo pluginInfo = Casts.cast(parent);
        return StringUtils.equals(pluginInfo.getId(), this.parent);
    }
}
