<template>
  <el-container>

    <el-aside class="authentication-aside align-items-center ">
      <div class="text-center">
        <h2> Dactiv 后台管理系统 </h2>
        <p> 请登陆您的账户 </p>
      </div>
      <el-form ref="login-form" :model="form" :rules="rules"  label-position="top">
        <el-form-item label="登陆账户:" prop="username">
          <el-input v-model="form.username"></el-input>
        </el-form-item>
        <el-form-item label="登陆密码:" prop="password">
          <el-input v-model="form.password" show-password></el-input>
        </el-form-item>
        <el-form-item>
            <el-checkbox v-model="form.rememberMe" label="一周内记住我" name="type"></el-checkbox>
        </el-form-item>
        <el-form-item class="text-center">
          <el-button-group class="">
          <el-button icon="el-icon-unlock" type="success" @click="submitForm('login-form')">登陆</el-button>
          <el-button icon="el-icon-delete" @click="resetForm('login-form')">重置</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-aside>

    <el-container class="authentication-container">

      <el-header>
        <div class="logo">
          <img alt="Dactiv logo" src="../assets/logo.png">
          <span> Dactiv </span>
        </div>
      </el-header>

      <el-main>
        <img alt="Dactiv logo" src="../assets/login.svg">
      </el-main>

    </el-container>

  </el-container>
</template>

<script>

export default {
  data() {
    return {
      form: {
        username: '',
        password: '',
        rememberMe: true
      },
      rules: {
        username: { required: true, message: '请输入登陆账户', trigger: 'blur' },
        password: { required: true, message: '请输入登陆密码', trigger: 'blur' }
      }
    }
  },
  methods: {
    submitForm:function(form) {
      let _this = this;
      _this.$refs[form].validate((valid) => {
        if (valid) {

          _this.axios.request({
            url:"/authentication/login",
            method: "post",
            params: {
              username: this.form.username,
              password: this.form.password,
              rememberMe: this.form.rememberMe,
              type: "Username"
            },
            headers:{
              "X-AUTHENTICATION-TYPE":"Console"
            }
          }).then(function (response) {

            let requestPath = localStorage.getItem("requestPath");

            if (requestPath !== null) {
              _this.$router.push(requestPath);
            } else {
              _this.$router.push("/");
            }

            localStorage.setItem(process.env.VUE_APP_PRINCIPAL_NAME, JSON.stringify(response.data.data));
          });

        } else {
          return false;
        }
      });
    },
    resetForm:function(form) {
      this.$refs[form].resetFields();
    }
  }
}
</script>