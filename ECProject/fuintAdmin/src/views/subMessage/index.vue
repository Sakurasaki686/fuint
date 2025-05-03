<template>
  <div class="app-container">
    <el-form :model="queryParams" class="main-search" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="关键字" prop="content">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入关键字"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
        >
          <el-option key="A" label="启用" value="A"/>
          <el-option key="N" label="禁用" value="N"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table ref="tables" v-loading="loading" :data="list" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column label="消息类型" prop="title">
        <template slot-scope="scope">
          <span class="msg-type">
            <svg-icon icon-class="wechat" class="wechat-icon" />{{ scope.row.title }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="消息键值" align="center" prop="key">
        <template slot-scope="scope">
          <span v-if="scope.row.key">
              <span>{{ scope.row.key }}</span>
          </span>
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100" prop="status">
        <template slot-scope="scope">
          <span v-if="scope.row.status == 'A'" class="status-active">启用</span>
          <span v-else>禁用</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            v-hasPermi="['subMessage:edit']"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" class="common-dialog" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="消息类型" prop="name">
               <el-input v-model="form.name" placeholder="请输入消息类型" disabled maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="模板ID" prop="templateId">
              <el-input v-model="form.templateId" placeholder="请输入模板ID，小程序订阅消息模板templateId" maxlength="120" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="模板编号" prop="tid">
              <el-input v-model="form.tid" placeholder="请输入模板编号，小程序订阅消息模板编号tid" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-for="(para, key) in form.params" :key="key">
          <el-col :span="24">
            <el-form-item :label="para.name + '参数'" :prop="para.key">
              <el-input v-model="para.value" :placeholder="paramTips" maxlength="259" />
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
import { getSubMessageList, getSubMessageInfo, saveSubMessage, deleteSubMessage } from "@/api/subMessage";
export default {
  name: "SubMessageIndex",
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
      // 表格数据
      list: [],
      // 是否显示弹出层
      open: false,
      // 默认排序
      defaultSort: {prop: 'createTime', order: 'descending'},
      // 表单参数
      form: { key: '', templateId: '', tid: '', name: '', params: [] },
      paramTips: "格式如：{{thing1.DATA}}，详见小程序模板消息详情",
      // 查询参数
      queryParams: {
        page: 1,
        pageSize: 10,
        title: '',
        status: ''
      },
      // 表单校验
      rules: {
        name: [
          { required: true, message: "模板名称不能为空", trigger: "blur" },
        ],
        templateId: [
          { required: true, message: "模板ID不能为空", trigger: "blur" },
        ],
        tid: [
          { required: true, message: "模板编号不能为空", trigger: "blur" },
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // 查询列表
    getList() {
      this.loading = true;
      getSubMessageList(this.queryParams).then( response => {
          this.list = response.data.dataList;
          this.loading = false;
        }
      );
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
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.multiple = !selection.length
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
      this.title = "新增订阅消息模板";
    },
    // 表单重置
    reset() {
      this.form = {
        key: "",
        name: "",
        tid: "",
        templateId: "",
        params: []
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
            saveSubMessage(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
        }
      });
    },
    // 修改按钮操作
    handleUpdate(row) {
      this.reset();
      const key = row.key;
      getSubMessageInfo(key).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "编辑订阅消息";
      });
    },
    // 删除按钮操作
    handleDelete(row) {
      this.$modal.confirm('是否确认删除ID等于' + row.id + '的数据项？').then(function() {
        return deleteSubMessage(row.id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>
<style rel="stylesheet/scss" lang="scss">
.msg-type {
  padding: 20px;
  display: block;
  border:1px solid #cccccc;
  width: 140px;
  border-radius: 2px;
  color:#080808;
  .wechat-icon {
    color: #52c41a;
  }
}
</style>
