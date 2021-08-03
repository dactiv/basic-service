<template>

  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item>系统管理</el-breadcrumb-item>
    <el-breadcrumb-item :to="{ path: '/index/console/user' }">后台用户管理</el-breadcrumb-item>
    <el-breadcrumb-item>编辑后台用户</el-breadcrumb-item>
  </el-breadcrumb>

  <el-card>
    <template #header>
      <span>编辑后台用户</span>
    </template>

    <el-form ref="search-form" :model="form" label-position="top" v-loading="formLoading">

      <el-row>
        <el-col :span="12" class="padding-left-10 padding-right-10">
          <el-form-item label="登陆账户:">
            <el-input v-model="form.username"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12" class="padding-left-10 padding-right-10">
          <el-form-item label="真是姓名:">
            <el-input v-model="form.realName"></el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row v-if="query.id === undefined">
        <el-col :span="12" class="padding-left-10 padding-right-10">
          <el-form-item label="登陆密码:">
            <el-input v-model="form.password"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12" class="padding-left-10 padding-right-10">
          <el-form-item label="确认密码:">
            <el-input v-model="form.confirmPassword"></el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="12" class="padding-left-10 padding-right-10">
          <el-form-item label="电子邮箱:">
            <el-input v-model="form.email"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12" class="padding-left-10 padding-right-10">
          <el-form-item label="状态:" prop="status">
            <el-select style="width: 100%" v-model="form.status" placeholder="请选择">
              <el-option v-for="(value, name) in statusOptions" :key="value" :label="name" :value="value">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="24" class="padding-left-10 padding-right-10">
          <el-form-item label="所在组:">
            <el-table ref="group-table">
              <el-table-column type="selection"/>
              <el-table-column type="index" label="序号" />
              <el-table-column prop="name" label="组名称" />
              <el-table-column prop="remark" label="备注"/>
            </el-table>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="24" class="padding-left-10 padding-right-10">
          <el-form-item label="独立权限:">
            <el-table ref="resource-table">
              <el-table-column type="selection"/>
              <el-table-column type="index" label="序号" />
              <el-table-column prop="name" label="名称" />
              <el-table-column prop="applicationName" label="应用系统"/>
              <el-table-column prop="type" label="类型"/>
              <el-table-column prop="remark" label="备注"/>
            </el-table>
          </el-form-item>
        </el-col>
      </el-row>

    </el-form>

    <div class="margin-top-20">

      <el-button-group>
        <el-button icon="el-icon-check" type="success">保存</el-button>
        <el-button icon="el-icon-delete" type="warning">重置</el-button>
      </el-button-group>
    </div>

  </el-card>

</template>

<script>
export default {
  name:"ConsoleUserEdit",
  data() {
    return {
      formLoading:true,
      statusOptions:[],
      query:null,
      form: {
        username: "",
        realName: "",
        password: "",
        confirmPassword:"",
        email: "",
        status:""
      }
    }
  },
  created() {
    this.loadConfig({service:"config", enumerateName:"UserStatus"}, r=> this.statusOptions = r);
    this.query = this.$route.query;

    if (this.query.id !== undefined) {

      this
          .$http
          .get("/authentication/console/user/get?id=" + this.query.id)
          .then(r => {
            this.form = r;
            this.formLoading = false
          });

    } else {
      this.formLoading = false
    }
  }
}
</script>