<template>

  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item>系统管理</el-breadcrumb-item>
    <el-breadcrumb-item>后台用户管理</el-breadcrumb-item>
  </el-breadcrumb>

  <el-card>
    <template #header>
        <span>用户管理</span>
    </template>

    <el-table :data="page.content" ref="multipleTable" v-loading="loading">
      <el-table-column type="selection"/>
      <el-table-column type="index" label="序号" width="60"/>
      <el-table-column prop="creationTime" width="180" :formatter="(callValue) => this.$moment(callValue).format('YYYY-MM-DD HH:mm:ss')" label="创建时间" />
      <el-table-column prop="username" label="登陆账号" width="170"/>
      <el-table-column prop="realName" label="真是姓名" width="170"/>
      <el-table-column prop="email" label="邮箱" width="170"/>
      <el-table-column prop="statusName" label="状态" width="170"/>
      <el-table-column prop="remark" label="备注" width="170"/>
      <el-table-column fixed="right" label="操作" width="140">
        <template #default>
          <el-button type="text" size="small">编辑</el-button>
          <el-button type="text" size="small">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="margin-top-20">

      <el-button-group>
        <el-button icon="el-icon-search" @click="searchDialogVisible = true">查询</el-button>
        <el-button icon="el-icon-edit" type="success" @click="edit()">添加</el-button>
        <el-button icon="el-icon-delete" type="danger" @click="deleteData()">删除</el-button>
      </el-button-group>

      <el-button-group class="float-right">
        <el-button @click="previous()" :disabled="!page.first"><i class="el-icon-arrow-left" /></el-button>
        <el-button disabled>{{ page.number }}</el-button>
        <el-button @click="next()" :disabled="!page.last"><i class="el-icon-arrow-right" /></el-button>
      </el-button-group>

    </div>

  </el-card>

  <el-dialog
      title="查询"
      v-model="searchDialogVisible"
      width="40%"
      center>
    <el-form ref="search-form" :model="form" label-position="top">

      <el-row>
        <el-col :span="12" class="padding-left-10 padding-right-10">
          <el-form-item label="登陆账户:">
            <el-input v-model="form['filter_[username_eq]']"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12" class="padding-left-10 padding-right-10">
          <el-form-item label="真是姓名:">
            <el-input v-model="form['filter_[real_name_like]']"></el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="12" class="padding-left-10 padding-right-10">
          <el-form-item label="电子邮箱:">
            <el-input v-model="form['filter_[email_like]']"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12" class="padding-left-10 padding-right-10">
          <el-form-item label="状态:" prop="status">
            <el-select style="width: 100%" v-model="form['filter_[status_eq]']" placeholder="请选择">
              <el-option key="" label="全部" value="" />
              <el-option v-for="(value, name) in statusOptions" :key="value" :label="name" :value="value">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

    </el-form>
    <template #footer>
    <span class="dialog-footer">
      <el-button @click="searchDialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="search('search-form')">确 定</el-button>
    </span>
    </template>

  </el-dialog>
</template>

<script>

export default {
  data() {
    return {
      form:{
        "filter_[username_eq]":"",
        "filter_[real_name_like]":"",
        "filter_[email_like]":"",
        "filter_[status_eq]":""
      },
      page: {
        content:[],
        first:false,
        last:false,
        number:1
      },
      loading: true,
      statusOptions:[],
      multipleSelection: [],
      searchDialogVisible: false
    }
  },
  created() {

    this.search();

    this
        .$http
        .get("/config/getServiceEnumerate",{
          params: {
            service:"config",
            enumerateName:"UserStatus"
          }
        }).then(r => {
          this.statusOptions = r;
        });
  },
  methods:{
    edit:function () {

    },
    deleteData:function() {

    },
    previous:function () {

    },
    next:function () {

    },
    search:function() {
      let _this = this;
      _this.loading = true;
      _this.searchDialogVisible = false;

      _this.$http.post("/authentication/console/user/page",_this.formUrlencoded(this.form)).then(r => {
        _this.page = r;
        _this.loading = false;
      });
    }
  }
}

</script>