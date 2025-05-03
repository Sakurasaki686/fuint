<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!--用户数据-->
      <el-col :span="24" :xs="24">
        <el-form :model="queryParams" class="main-search" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="用户名" prop="accountName">
            <el-input
              v-model="queryParams.accountName"
              placeholder="请输入用户名"
              clearable
              style="width: 240px"
              @keyup.enter.native="handleQuery"
            />
          </el-form-item>
          <el-form-item label="真实姓名" prop="accountName">
            <el-input
              v-model="queryParams.realName"
              placeholder="请输入真实姓名"
              clearable
              style="width: 240px"
              @keyup.enter.native="handleQuery"
            />
          </el-form-item>
          <el-form-item label="状态" prop="accountStatus">
            <el-select
              v-model="queryParams.accountStatus"
              placeholder="用户状态"
              clearable
              style="width: 240px"
            >
              <el-option key="1" label="启用" value="1"/>
              <el-option key="0" label="禁用" value="0"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="el-icon-plus"
              size="mini"
              @click="handleAdd"
              v-hasPermi="['system:account:add']"
            >新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="success"
              plain
              icon="el-icon-edit"
              size="mini"
              :disabled="single"
              v-hasPermi="['system:account:edit']"
              @click="handleUpdate"
            >修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="danger"
              plain
              icon="el-icon-delete"
              size="mini"
              :disabled="multiple"
              v-hasPermi="['system:account:delete']"
              @click="handleDelete"
            >删除</el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="ID" align="center" key="id" prop="id" v-if="columns[0].visible" />
          <el-table-column label="用户名" align="center" key="accountName" prop="accountName" v-if="columns[1].visible" :show-overflow-tooltip="true" />
          <el-table-column label="真实姓名" align="center" key="realName" prop="realName">
            <template slot-scope="scope">
              <span>{{ scope.row.realName ? scope.row.realName : '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="所属商户" align="center" prop="merchantName" width="160">
            <template slot-scope="scope">
              <span>{{ scope.row.merchantName ? scope.row.merchantName : '平台方' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="所属店铺" align="center" prop="storeName" width="160">
            <template slot-scope="scope">
              <span>{{ scope.row.storeName ? scope.row.storeName : '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" key="accountStatus" v-if="columns[2].visible">
            <template slot-scope="scope">
              <el-switch
                v-model="scope.row.accountStatus"
                :active-value="1"
                :inactive-value="0"
                @change="handleStatusChange(scope.row)"
              ></el-switch>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center" prop="createDate" v-if="columns[3].visible" width="160">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createDate) }}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            align="center"
            width="160"
            class-name="small-padding fixed-width"
          >
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-edit"
                v-hasPermi="['system:account:edit']"
                @click="handleUpdate(scope.row)"
              >修改</el-button>
              <el-button
                size="mini"
                type="text"
                icon="el-icon-delete"
                v-hasPermi="['system:account:delete']"
                @click="handleDelete(scope.row)"
              >删除</el-button>
              <el-dropdown size="mini" @command="(command) => handleCommand(command, scope.row)" v-hasPermi="['system:account:resetPwd', 'system:account:edit']">
                <span class="el-dropdown-link">
                  <i class="el-icon-d-arrow-right el-icon--right"></i>更多
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="handleResetPwd" icon="el-icon-key" v-hasPermi="['system:account:edit']">重置密码</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="total>0"
          :total="total"
          :page.sync="queryParams.page"
          :limit.sync="queryParams.pageSize"
          @pagination="getList"
        />
      </el-col>
    </el-row>

    <!-- 添加或修改管理员配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" class="common-dialog" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="用户名" prop="accountName">
              <el-input v-model="form.accountName" placeholder="请输入用户名" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item v-if="form.id == undefined" label="登录密码" prop="password">
              <el-input v-model="form.password" placeholder="请输入登录密码" maxlength="30" show-password/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item v-if="form.id == undefined" label="确认密码" prop="password1">
              <el-input v-model="form.password1" placeholder="请输入确认密码" type="password" maxlength="30" show-password/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="状态">
              <el-radio-group v-model="form.accountStatus">
                <el-radio :key="1" :label="1" :value="1">启用</el-radio>
                <el-radio :key="0" :label="0" :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="分配角色">
              <el-select v-model="form.roleIds" multiple @change="$forceUpdate()" placeholder="请选择角色">
                <el-option
                  v-for="item in roleOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                  :disabled="item.status !== 'A'"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="所属商户">
              <el-select v-model="form.merchantId" placeholder="请选择所属商户">
                <el-option
                  v-for="item in merchantOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                  :disabled="item.status !== 'A'"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="所属店铺">
              <el-select v-model="form.storeId" placeholder="请选择所属店铺" @change="getStaffList()">
                <el-option :key="0" label="全部店铺" :value="0"></el-option>
                <el-option
                  v-for="item in storeOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                  :disabled="item.status !== 'A'"
                ></el-option>
              </el-select>
              <p class="form-tips">（提示：不选择则可管理所有店铺的数据）</p>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="店铺员工">
              <el-select v-model="form.staffId" placeholder="请选择店铺员工">
                <el-option :key="0" label="请选择员工" :value="0"
                ></el-option>
                <el-option
                  v-for="item in staffOptions"
                  :key="item.id"
                  :label="item.realName"
                  :value="item.id"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确定</el-button>
        <el-button @click="cancel">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getStoreStaffList } from "@/api/staff";
import { getAccountList, getAccount, delAccount, addAccount, updateAccount, resetAccountPwd, changeAccountStatus } from "@/api/system/account";
import { getToken } from "@/utils/auth";

export default {
  name: "Account",
  data() {
    return {
      // 遮罩层
      loading: false,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 用户表格数据
      userList: null,
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 角色选项
      roleOptions: [],
      storeOptions: [],
      staffOptions: [],
      merchantOptions: [],
      // 表单参数
      form: { id: "", accountName: "", realName: "", accountStatus: 1, roleIds: [], merchantId: this.$store.getters.merchantId, storeId: this.$store.getters.storeId, staffId: 0 },
      defaultProps: {
        children: "children",
        label: "label"
      },
      // 用户导入参数
      upload: {
        // 是否显示弹出层（用户导入）
        open: false,
        // 弹出层标题（用户导入）
        title: "",
        // 是否禁用上传
        isUploading: false,
        // 是否更新已经存在的用户数据
        updateSupport: 0,
        // 设置上传的请求头部
        headers: { 'Access-Token': getToken() },
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + "/system/account/importData"
      },
      // 查询参数
      queryParams: {
        page: 1,
        pageSize: 10,
        accountName: "",
        realName: "",
        accountStatus: ""
      },
      // 列信息
      columns: [
        { key: 0, label: `用户编号`, visible: true },
        { key: 1, label: `用户名称`, visible: true },
        { key: 2, label: `用户昵称`, visible: true },
        { key: 3, label: `状态`, visible: true },
        { key: 4, label: `创建时间`, visible: true }
      ],
      // 表单校验
      rules: {
        accountName: [
          { required: true, message: "用户名不能为空", trigger: "blur" },
          { min: 2, max: 30, message: '用户名长度必须介于 2 和 20 之间', trigger: 'blur' }
        ],
        password: [
          { required: true, message: "用户密码不能为空", trigger: "blur" },
          { min: 5, max: 30, message: '用户密码长度必须介于 5 和 20 之间', trigger: 'blur' }
        ],
        password1: [
          { required: true, message: "确认密码不能为空", trigger: "blur" },
          { min: 5, max: 30, message: '确认密码长度必须介于 5 和 20 之间', trigger: 'blur' }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // 查询账户列表
    getList() {
      this.loading = true;
      getAccountList(this.queryParams).then(response => {
          this.userList = response.data.content;
          this.total = response.data.totalElements;
          this.loading = false;
        }
      );
    },
    // 用户状态修改
    handleStatusChange(row) {
      let text = row.accountStatus == "1" ? "启用" : "禁用";
      this.$modal.confirm('确认要' + text + '"' + row.accountName + '"用户吗？').then(function() {
        return changeAccountStatus(row.id, row.accountStatus);
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(function() {
        row.accountStatus = row.accountStatus === "0" ? "1" : "0";
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: undefined,
        accountName: "",
        password: "",
        accountStatus: 1,
        realName: "",
        roleIds: [],
        storeId: this.$store.getters.storeId,
        merchantId: this.$store.getters.merchantId
      };
      this.resetForm("form");
    },
    // 搜索按钮操作
    handleQuery() {
      this.queryParams.page = 1;
      this.getList();
    },
    // 重置按钮操作
    resetQuery() {
      this.queryParams.accountName = '';
      this.queryParams.accountStatus = '';
      this.queryParams.realName = '';
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    // 更多操作触发
    handleCommand(command, row) {
      switch (command) {
        case "handleResetPwd":
          this.handleResetPwd(row);
          break;
        default:
          break;
      }
    },
    // 新增按钮操作
    handleAdd() {
      this.reset();
      getAccount(-1).then(response => {
        this.roleOptions = response.data.roles;
        this.storeOptions = response.data.stores;
        this.merchantOptions = response.data.merchants;
        this.open = true;
        this.title = "新增管理员";
      });
    },
    // 修改按钮操作
    handleUpdate(row) {
      const app = this;
      app.reset();
      const id = row.id || this.ids;
      getAccount(id).then(response => {
          app.form = response.data.account
          app.form.roleIds = response.data.roleIds;
          app.roleOptions = response.data.roles;
          app.storeOptions = response.data.stores;
          app.merchantOptions = response.data.merchants;
          app.getStaffList();
          app.open = true;
          app.title = "修改管理员";
          app.form.password = "";
      });
    },
    // 重置密码按钮操作
    handleResetPwd(row) {
      this.$prompt('请输入"' + row.accountName + '"的新密码', "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        closeOnClickModal: false,
        inputPattern: /^.{5,20}$/,
        inputErrorMessage: "用户密码长度必须介于 5 和 20 之间"
      }).then(({ value }) => {
          resetAccountPwd(row.id, value).then(response => {
            this.$modal.msgSuccess("修改成功，新密码是：" + value);
          });
        }).catch(() => {});
    },
    // 店铺员工列表
    getStaffList() {
      const storeId = this.form.storeId ? this.form.storeId : 0;
      getStoreStaffList(storeId).then(response => {
         this.staffOptions = response.data.staffList;
      });
    },
    // 提交按钮
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id) {
            updateAccount(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addAccount(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    // 删除按钮操作
    handleDelete(row) {
      const userIds = row.id || this.ids;
      this.$modal.confirm('您确认删除用户ID为"' + userIds + '"的账户？').then(function() {
        return delAccount(userIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>
