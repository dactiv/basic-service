<template>
  <el-container>

    <el-aside class="main-aside">
      <div class="logo">
        <el-row>
          <el-col :span="22">
            <img alt="Dactiv logo" src="../assets/logo.png">
            <span> Dactiv </span>
          </el-col>
          <el-col :span="2">
            <i class="el-icon-aim"></i>
          </el-col>
        </el-row>
      </div>
      <div class="main-menu">
        <el-menu :router="true" :default-active="this.$route.path">
          <recursion-menu :data="this.menus" />
        </el-menu>
      </div>
    </el-aside>

    <el-container class="main-container">

      <el-header class="header-navbar">
        <el-row>
          <el-col :span="16">
            <el-menu mode="horizontal" class="left">
              <el-menu-item index="1"><i class="el-icon-date"></i></el-menu-item>
              <el-menu-item index="2"><i class="el-icon-chat-square"></i></el-menu-item>
              <el-menu-item index="3"><i class="el-icon-message"></i></el-menu-item>
              <el-menu-item index="4"><i class="el-icon-star-off"></i></el-menu-item>
              <el-menu-item index="5"><i class="el-icon-notebook-1"></i></el-menu-item>
            </el-menu>
          </el-col>
          <el-col :span="8">
            <el-menu mode="horizontal" class="float-right right">
              <el-menu-item index="1"><i class="el-icon-moon"></i></el-menu-item>
              <el-menu-item index="2"><i class="el-icon-search"></i></el-menu-item>
              <el-submenu index="3">
                <template #title>
                  <span class="username">{{ principal.username }}</span>
                  <el-avatar size="medium" ></el-avatar>
                </template>
                <el-menu-item index="3-1"><i class="el-icon-user"></i> 用户信息</el-menu-item>
                <el-menu-item index="3-2"><i class="el-icon-setting"></i> 系统设置</el-menu-item>
                <el-menu-item index="3-3"><i class="el-icon-question"></i> 常见问题</el-menu-item>
                <el-menu-item index="3-3"><el-button type="text" icon="el-icon-key" @click="logout()"> 注销账户</el-button></el-menu-item>
              </el-submenu>
            </el-menu>
          </el-col>
        </el-row>
      </el-header>

      <el-main>
        <router-view />
      </el-main>

      <el-footer class="main-footer">COPYRIGHT © 2021 Dactiv, All rights ReservedHand-crafted & Made with </el-footer>

    </el-container>
  </el-container>
</template>

<script>

import RecursionMenu from '../components/RecursionMenu.vue'

export default {
  name: 'Index',
  components:{RecursionMenu},
  created() {
    this.getMenus();
  },
  methods: {
    logout:function() {
      this.$router.push('/' + process.env.VUE_APP_LOGIN_PATH);
    },
    getMenus:function() {

      let _this = this;

      _this.$http
          .get("/authentication/resource/getConsolePrincipalResources",{
            params: {
              type:"Menu",
              mergeTree:true
            }
          })
          .then(function (response) {

            localStorage.setItem(process.env.VUE_APP_MENU_NAME, JSON.stringify(response));
            _this.menus = response;
          });
    }
  },
  data() {
    return {
      principal: JSON.parse(localStorage.getItem(process.env.VUE_APP_PRINCIPAL_NAME)),
      menus: {}
    }
  }
}
</script>