<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="商户号" prop="no">
        <el-input
          v-model="queryParams.no"
          placeholder="请输入商户号"
          clearable
          style="width: 150px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="商户名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入商户名称"
          clearable
          style="width: 200px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
          style="width: 120px"
        >
          <el-option key="A" label="启用" value="A"/>
          <el-option key="N" label="禁用" value="N"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['merchant:index']"
        >新增商户</el-button>
      </el-form-item>
    </el-form>

    <el-table ref="tables" v-loading="loading" :data="list" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column label="ID" prop="id"  width="55"/>
      <el-table-column label="商户号" align="center" prop="no" />
      <el-table-column label="商户名称" align="center" prop="name" />
      <el-table-column label="联系人" align="center" prop="contact"/>
      <el-table-column label="手机号" align="center" prop="phone"/>
      <el-table-column label="地址" align="center" prop="address"/>
      <el-table-column label="创建时间" align="center" prop="createTime" sortable="custom" :sort-orders="['descending', 'ascending']" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
      <template slot-scope="scope">
        <el-switch
          v-model="scope.row.status"
          active-value="A"
          inactive-value="N"
          @change="handleStatusChange(scope.row)"
        ></el-switch>
      </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            v-hasPermi="['merchant:index']"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            v-hasPermi="['merchant:index']"
            @click="handleDelete(scope.row)"
          >删除</el-button>
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

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" class="common-dialog" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="180px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="商户号" prop="no">
              <el-input v-model="form.no" placeholder="请输入商户号，5-30个字符，未输入则系统自动生成" maxlength="30"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="商户类型" prop="type">
              <el-select class="input" v-model="form.type" clearable placeholder="请选择商户类型" style="width: 300px">
                <el-option
                  v-for="item in typeOptions"
                  :key="item.key"
                  :label="item.name"
                  :value="item.key"
                ></el-option>
              </el-select>
              <div class="form-tips">提示：选择适合的行业</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="商户名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入商户名称" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="9">
            <el-form-item label="商户logo" prop="logo">
              <el-upload
                :action="uploadAction"
                list-type="picture-card"
                :class="{hide:hideUpload}"
                :file-list="uploadFiles"
                :auto-upload="true"
                :show-file-list="false"
                :headers="uploadHeader"
                :on-success="handleUploadSuccess">
                <img
                  v-if="form.logo"
                  :src="imagePath + form.logo"
                  class="list-img"
                />
                <i v-if="!form.logo" class="el-icon-plus"></i>
              </el-upload>
            </el-form-item>
          </el-col>
          <p class="form-tips">（提示：点击图片修改）</p>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="联系人" prop="contact">
              <el-input v-model="form.contact" placeholder="请输入联系人姓名" maxlength="30"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="30"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="联系地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入联系地址" maxlength="200" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="微信小程序appId" prop="wxAppId">
              <el-input v-model="form.wxAppId" placeholder="请输入微信小程序appId，30-50个字符" maxlength="50"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="微信小程序appSecret" prop="wxAppSecret">
              <el-input v-model="form.wxAppSecret" placeholder="请输入微信小程序appSecret，30-50个字符" maxlength="50"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="微信公众号appId" prop="wxOfficialAppId">
              <el-input v-model="form.wxOfficialAppId" placeholder="请输入微信小程序appId，30-50个字符" maxlength="50"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="微信公众号appSecret" prop="wxOfficialAppSecret">
              <el-input v-model="form.wxOfficialAppSecret" placeholder="请输入微信小程序appSecret，30-50个字符" maxlength="50"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注信息">
              <el-input v-model="form.description" type="textarea" placeholder="请输入内容"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio key="A" label="A" value="A">启用</el-radio>
                <el-radio key="N" label="N" value="N">禁用</el-radio>
              </el-radio-group>
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
import { getToken } from '@/utils/auth';
import { getMerchantList, updateMerchantStatus, getMerchantInfo, saveMerchant } from "@/api/merchant";
export default {
  name: "MerchantList",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 标题
      title: "",
      // 选中数组
      ids: [],
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      // 是否显示弹出层
      open: false,
      // 默认排序
      defaultSort: {prop: 'createTime', order: 'descending'},
      // 商户类型
      typeOptions: [],
      // 表单参数
      form: { id: '', name: '', no: '', logo: '', contact: '', phone: '', wxAppId: '', wxAppSecret: '', wxOfficialAppId: '', wxOfficialAppSecret: '', description: '', status: "A" },
      // 上传地址
      uploadAction: process.env.VUE_APP_SERVER_URL + 'backendApi/file/upload',
      // 隐藏上传
      hideUpload: false,
      // 上传文件列表
      uploadFiles: [],
      uploadHeader: { 'Access-Token' : getToken() },
      // 图片根目录
      imagePath: "",
      // 查询参数
      queryParams: {
        page: 1,
        pageSize: 10,
        no: '',
        name: '',
        status: ''
      },
      // 表单校验
      rules: {
        type: [
          { required: true, message: "商户号不能为空", trigger: "blur" },
        ],
        no: [
          { required: true, message: "商户号不能为空", trigger: "blur" },
          { min: 5, max: 30, message: '商户号长度必须介于5至30之间', trigger: 'blur' }
        ],
        name: [
          { required: true, message: "商户名称不能为空", trigger: "blur" },
          { min: 2, max: 30, message: '商户名称长度必须介于 2 和 50 之间', trigger: 'blur' }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // 查询日志
    getList() {
      this.loading = true;
      getMerchantList(this.addDateRange(this.queryParams, this.dateRange)).then( response => {
          this.list = response.data.dataList.content;
          this.total = response.data.dataList.totalElements;
          this.imagePath = response.data.imagePath;
          this.typeOptions = response.data.typeList;
          this.loading = false;
      });
    },
    // 搜索按钮操作
    handleQuery() {
      this.queryParams.page = 1;
      this.getList();
    },
    // 重置按钮操作
    resetQuery() {
      this.resetForm("queryForm");
      this.$refs.tables.sort(this.defaultSort.prop, this.defaultSort.order)
      this.handleQuery();
    },
    // 状态修改
    handleStatusChange(row) {
      let text = row.status == "1" ? "启用" : "禁用";
      this.$modal.confirm('确认要' + text + '"' + row.name + '"商户吗？').then(function() {
         return updateMerchantStatus(row.id, row.status);
      }).then(() => {
         this.$modal.msgSuccess(text + "成功");
      }).catch(function() {
         row.status = row.status === "A" ? "A" : "D";
      });
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.multiple = !selection.length;
    },
    // 排序触发事件
    handleSortChange(column, prop, order) {
      this.queryParams.orderByColumn = column.prop;
      this.queryParams.isAsc = column.order;
      this.getList();
    },
    // 新增按钮操作
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新增商户";
    },
    // 表单重置
    reset() {
      this.form = {
        id: "",
        no: "",
        name: "",
        logo: "",
        license: "",
        wxAppId: "",
        wxAppSecret: "",
        wxOfficialAppId: "",
        wxOfficialAppSecret: "",
        description: "",
        status: "A"
      };
      this.resetForm("form");
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 提交按钮
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id) {
              saveMerchant(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
          } else {
              saveMerchant(this.form).then(response => {
                this.$modal.msgSuccess("新增成功");
                this.open = false;
                this.getList();
              });
          }
        }
      });
    },
    // 修改按钮操作
    handleUpdate(row) {
      const app = this;
      app.reset();
      const id = row.id || this.ids;
      getMerchantInfo(id).then(response => {
        app.form = response.data.merchantInfo;
        app.open = true;
        app.title = "编辑商户";
      });
    },
    // 删除按钮操作
    handleDelete(row) {
      const name = row.name || this.id;
      this.$modal.confirm('是否确认删除"' + name + '"的数据项？').then(function() {
        return updateMerchantStatus(row.id, 'D');
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleUploadSuccess(file) {
      this.form.logo = file.data.fileName;
    }
  }
};
</script>
<style scoped>
.common-dialog >>> .el-upload--picture-card {
  width: 60px;
  height: 50px;
  line-height: 60px;
}
</style>

