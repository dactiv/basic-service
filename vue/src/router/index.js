import { createRouter, createWebHistory} from 'vue-router';

import Login from '@/views/Login'
import Index from '@/views/Index'

import recursionMenu from "@/components/RecursionMenu";
import axios from "axios";

const routes = [
  {
    path: '/',
    redirect: "/" + process.env.VUE_APP_INDEX_PATH
  },
  {
    path: "/" + process.env.VUE_APP_LOGIN_PATH,
    name: process.env.VUE_APP_LOGIN_PATH,
    component: Login,
    meta: {
      title: "用户登陆"
    }
  },
  {
    path: "/" + process.env.VUE_APP_INDEX_PATH,
    name: process.env.VUE_APP_INDEX_PATH,
    component: Index,
    meta: {
      title: "首页"
    }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
});

/**
 * 通过后台获取的当前用户菜单查找出子菜单信息，并构造出子路由需要的数据
 *
 * @param menu 后台菜单数据
 *
 * @returns {*[]} 子路由数据数组
 */
function getChildren(menu) {

  let result = [];
  // 遍历数组
  menu.forEach(m => {
    // 如果当前数据存在孩子节点，递归继续遍历出最终的子节点，否则直接构造返回数组
    if (recursionMenu.methods.hasChildren(m)) {
      let data = getChildren(m.children);
      data.forEach(d => result.push(d));
    } else {
      let v = recursionMenu.methods.replaceValue(m.value);
      result.push({
        path: v,
        component: () => import("@/views/" + v + "/Index.vue"),
        name: m.code,
        meta: {
          title: m.name
        }
      });
    }
  });

  return result;
}

/**
 * 添加路由菜单
 *
 * @param menus 后台菜单数据
 */
function addMenuRoute(menus) {

  let c = getChildren(menus);

  // 在 index path 理添加子菜单，参考 .env.development 文件
  c.forEach(a => router.addRoute(process.env.VUE_APP_INDEX_PATH, a));

}

/**
 * 获取后台菜单数据
 *
 * @param to 导航卫士的 RouteLocationNormalized
 * @param next 导航卫士的 NavigationGuardNext
 */
function getMenus(to, next) {

  axios
      .get("/authentication/resource/getConsolePrincipalResources",{
        params: {
          type:"Menu",
          mergeTree:true
        }
      })
      .then(function (response) {

        addMenuRoute(response.data.data);

        localStorage.setItem(process.env.VUE_APP_MENU_NAME, JSON.stringify(response.data.data));

        next({ ...to, replace: true });

      });
}

/**
 * 添加导航卫士
 */
router.beforeEach((to, from, next) => {
  // 如果路径为"登陆", 跳用服务器登出，并把所有的本地数据清除
  if (to.path === "/" + process.env.VUE_APP_LOGIN_PATH) {
    axios.post("/authentication/logout").then(function () {
      localStorage.removeItem(process.env.VUE_APP_PRINCIPAL_NAME);
      localStorage.removeItem(process.env.VUE_APP_MENU_NAME);
      next();
    });
  } else {

    let principal = JSON.parse(localStorage.getItem(process.env.VUE_APP_PRINCIPAL_NAME));
    // 如果用户没登陆，记录当前的路径，跳转到登陆页面
    if (principal === null) {
      localStorage.setItem("requestPath", to.path);
      next("/" + process.env.VUE_APP_LOGIN_PATH);
    } else {
      // 如果已经登陆，获取菜单信息
      let menus = JSON.parse(localStorage.getItem(process.env.VUE_APP_MENU_NAME));
      // 如果没有菜单信息，获取一次后台数据，并跳转相应的页面
      if (menus === null) {

        getMenus(to, next);

      } else {
        // 如果有菜单，但路由找不到菜单，添加到路由里在跳转，否则直接该干嘛干嘛
        if (router.getRoutes().find(s => s.path === to.path) === undefined) {
          addMenuRoute(menus);
          next({ ...to, replace: true });
        } else {
          next();
        }

      }
    }

  }
});

export default router
