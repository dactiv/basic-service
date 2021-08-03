import { createRouter, createWebHistory} from 'vue-router';

import axios from 'axios'

import Login from '@/views/Login'
import Index from '@/views/Index'

import NProgress from 'nprogress'
import 'nprogress/nprogress.css';

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
    },
    children: [{
      path: "console/user",
      component: () => import("@/views/console/user/Index.vue"),
      name: "console_user",
      meta: {
        title: "后台用户管理"
      }
    },{
      path: "console/user/edit",
      component: () => import("@/views/console/user/Edit.vue"),
      name: "console_user_edit",
      meta: {
        title: "编辑后台用户管理"
      }
    }]
  }
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
});

/**
 * 添加导航卫士
 */
router.beforeEach((to, from, next) => {
  NProgress.start();

  let principal = JSON.parse(localStorage.getItem(process.env.VUE_APP_PRINCIPAL_NAME));

  // 如果路径为"登陆", 跳用服务器登出，并把所有的本地数据清除
  if (to.path === "/" + process.env.VUE_APP_LOGIN_PATH) {

    if (principal !== null) {
      axios.post("/authentication/logout").then(function () {
        localStorage.removeItem(process.env.VUE_APP_PRINCIPAL_NAME);
        localStorage.removeItem(process.env.VUE_APP_MENU_NAME);
      });
    }

  }

  if (principal === null) {
    next("/" + process.env.VUE_APP_LOGIN_PATH);
  } else {
    next();

  }

});

router.afterEach(() => {
  NProgress.done()
})

export default router
