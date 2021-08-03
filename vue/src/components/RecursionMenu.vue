<template>
  <template v-for="(d,idx) of data" :key="d.id">
    <el-menu-item :index="getPath(d.value)" v-if="!hasChildren(d)">
        <i :class="d.icon"></i>
        <span>{{d.name}}</span>
    </el-menu-item>
    <el-submenu v-else :index="parent === undefined ? idx + '' : parent + '-' + idx">
      <template #title>
        <i :class="d.icon"></i>
        <span>{{ d.name }}</span>
      </template>
      <!-- 调用自身递归数据 -->
      <recursion-menu :data="d.children" :parent="idx" />
    </el-submenu>
  </template>
</template>

<script>

/**
 * 递归菜单，
 */
export default {
  name: "RecursionMenu",
  props: ["data","parent"],
  methods: {
    /**
     * 判断是否存在子节点
     *
     * @param d 当前后台菜单数据
     *
     * @returns {boolean} true 是，否则 false
     */
    hasChildren:function(d) {
      return d.children !== undefined && d.children.length > 0;
    },
    /**
     * 替换后台菜单 value 值
     *
     * @param v 值
     *
     * @returns {*} 替换后的值
     */
    replaceValue:function(v) {
      return v.replace("/**","");
    },
    /**
     * 获取路由路径
     *
     * @param v 值
     *
     * @returns {string} 路由路径值
     */
    getPath:function(v) {
      return "/" + process.env.VUE_APP_INDEX_PATH + "/" + this.replaceValue(v);
    }
  }
}
</script>