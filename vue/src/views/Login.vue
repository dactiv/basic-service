<template>
  <el-container>

    <el-aside class="authentication-aside align-items-center ">
      <div class="text-center">
        <h2> Dactiv 后台管理系统 </h2>
        <p> 请登陆您的账户 </p>
      </div>
      <el-form ref="login-form" :model="form" :rules="rules" label-position="top">
        <el-form-item label="登陆账户:" prop="username">
          <el-input v-model="form.username"></el-input>
        </el-form-item>
        <el-form-item label="登陆密码:" prop="password">
          <el-input v-model="form.password" show-password></el-input>
        </el-form-item>
        <el-form-item label="验证码:" class="picture-captcha" prop="captcha" v-if="captcha.data.type === 'picture'">
          <el-input v-model="form.captcha">
            <template #append>
              <el-image style="width: 100px; height: 36px; vertical-align: middle;" :src="this.captcha.pictureCaptchaUrl" @click="generatePictureCaptcha()"/>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
            <el-checkbox v-model="form.rememberMe" label="一周内记住我"></el-checkbox>
        </el-form-item>
        <el-form-item class="text-center">
          <el-button-group>
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
      captcha: {
        generateMapping:{
          picture:this.generatePictureCaptcha
        },
        submitFormMapping: {
          picture:this.getPictureCaptchaField
        },
        data:{},
        pictureCaptchaUrl:"",
        submit:false
      },
      form: {
        username: "",
        password: "",
        rememberMe: true,
        type: "Username",
        captcha:""
      },
      rules: {
        username: { required: true, message: '请输入登陆账户', trigger: 'blur' },
        password: { required: true, message: '请输入登陆密码', trigger: 'blur' },
        captcha: { required: true, message: '请输入验证码', trigger: 'blur' }
      }
    }
  },
  created() {
    let _this = this;
    _this.$http.get("/authentication/prepare").then(this.validCaptcha);
  },
  methods: {
    validCaptcha:function(error) {
      if (error.data.executeCode === "1001") {

        this.captcha.submit = true;
        this.captcha.data = error.data.data;

        this.captcha.generateMapping[this.captcha.data.type]();
      }
    },
    getPictureCaptchaField:function() {
      this.form["_pictureCaptcha"] = this.form.captcha;
      this.form[this.captcha.data.paramName] = this.captcha.data.token.name;
    },
    generatePictureCaptcha:function() {
      this.captcha.pictureCaptchaUrl = "/captcha/generateCaptcha?" + this.captcha.data.paramName + "=" + this.captcha.data.token.name + "&height=36&time=" + new Date().getTime();
    },
    submitForm:function(form) {
      let _this = this;

      _this.$refs[form].validate((valid) => {

        if (valid) {

          if (this.captcha.submit) {
            this.captcha.submitFormMapping[this.captcha.data.type]();
          }

          _this
              .$http
              .post("/authentication/login", _this.formUrlencoded(_this.form),{headers:{"X-AUTHENTICATION-TYPE":"Console"}})
              .then(response => {

                localStorage.setItem(process.env.VUE_APP_PRINCIPAL_NAME, JSON.stringify(response));

                let requestPath = localStorage.getItem("requestPath");

                if (requestPath !== null) {
                  _this.$router.push(requestPath);
                  localStorage.removeItem("requestPath");
                } else {
                  _this.$router.push("/");
                }

              })
              .catch(this.validCaptcha);

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