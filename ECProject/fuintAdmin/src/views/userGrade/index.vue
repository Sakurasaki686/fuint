<template>
  <div class="app-container">
    <el-form :model="queryParams" class="main-search" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="等级名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入等级名称"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="升级方式" prop="catchType">
        <el-select
          v-model="queryParams.catchType"
          clearable
          placeholder="升级方式"
        >
          <el-option v-for="catchType in catchTypeList" :key="catchType.key" :label="catchType.name" :value="catchType.value"/>
        </el-select>
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
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['userGrade:add']"
        >新增</el-button>
      </el-form-item>
    </el-form>

    <el-table ref="tables" v-loading="loading" :data="list" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column label="等级" prop="grade" width="55"/>
      <el-table-column label="等级名称" align="center" prop="name" />
      <el-table-column label="升级方式" align="center" prop="catchType" />
      <el-table-column label="升级条件值" align="center" prop="catchValue">
        <template slot-scope="scope">
          <span v-if="scope.row.catchValue > 0">
              <span>{{ scope.row.catchValue }}</span>
          </span>
          <span v-else>无条件</span>
        </template>
      </el-table-column>
      <el-table-column label="有效天数" align="center" prop="validDay">
         <template slot-scope="scope">
             <span v-if="scope.row.validDay > 0">{{ scope.row.validDay + '天' }}</span>
             <span v-else>永久</span>
         </template>
      </el-table-column>
      <el-table-column label="支付折扣" align="center" prop="discount">
        <template slot-scope="scope">
          <span v-if="scope.row.discount > 0">{{ scope.row.discount + '折' }}</span>
          <span v-else>无折扣</span>
        </template>
      </el-table-column>
      <el-table-column label="积分加速" align="center" prop="speedPoint">
        <template slot-scope="scope">
          <span v-if="scope.row.speedPoint > 0">{{ scope.row.speedPoint + '倍数' }}</span>
          <span v-else>不加倍</span>
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
            v-hasPermi="['userGrade:add']"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            v-hasPermi="['userGrade:add']"
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
    <el-dialog :title="title" :visible.sync="open" class="common-dialog" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="等级" prop="grade">
              <el-input v-model="form.grade" placeholder="请输入等级，正整数" maxlength="10" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="等级名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入等级名称" maxlength="200" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="升级条件描述" prop="catchCondition">
              <el-input v-model="form.catchCondition" placeholder="请输入升级条件描述" maxlength="200" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="升级权益描述" prop="userPrivilege">
              <el-input v-model="form.userPrivilege" type="textarea" placeholder="请输入升级权益描述"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="升级方式" prop="catchType">
              <el-select
                v-model="form.catchType"
                clearable
                placeholder="升级方式"
              >
                <el-option v-for="catchType in catchTypeList" :key="catchType.key" :label="catchType.name" :value="catchType.value"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="升级条件值" prop="catchValue">
              <el-input v-model="form.catchValue" placeholder="请输入升级条件值，单位可能是元、次"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="有效天数" prop="validDay">
              <el-input v-model="form.validDay" placeholder="请输入有效天数，如30，0表示永久有效"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="支付折扣" prop="discount">
              <el-input v-model="form.discount" placeholder="请输入支付折扣，数字，如：8.8"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="积分加速" prop="speedPoint">
              <el-input v-model="form.speedPoint" placeholder="请输入积分加速，积分加速倍数，数字"></el-input>
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
import { getUserGradeList, updateUserGradeStatus, getUserGradeInfo, saveUserGrade, deleteUserGrade } from "@/api/userGrade";
export default {
  name: "UserGradeIndex",
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
      // 升级条件
      catchTypeList: [],
      // 是否显示弹出层
      open: false,
      // 默认排序
      defaultSort: {prop: 'createTime', order: 'descending'},
      // 表单参数
      form: { id: '', grade: 0, name: '', catchCondition: '', userPrivilege: '', catchType: '', catchValue: '', validDay: '', discount: '', speedPoint: '', status: "A" },
      // 查询参数
      queryParams: {
        page: 1,
        pageSize: 10,
        name: '',
        catchType: '',
        status: ''
      },
      // 表单校验
      rules: {
        grade: [
          { required: true, message: "等级不能为空", trigger: "blur" },
          { pattern: /^[0-9]{1,10}$/ , message: `必须正整数`, trigger: 'blur' }
        ],
        name: [
          { required: true, message: "等级名称不能为空", trigger: "blur" },
          { min: 2, max: 100, message: '等级名称长度必须介于2 和 100 之间', trigger: 'blur' }
        ],
        catchType: [
          { required: true, message: "请选择升级方式", trigger: "blur" },
        ],
        catchValue: [
          { required: true, message: "请输入升级条件值", trigger: "blur" },
        ],
        validDay: [
          { required: true, message: "请输入有效期天数，0表述永久有效", trigger: "blur" },
        ],
        discount: [
          { required: true, message: "请输入支付折扣", trigger: "blur" },
        ],
        speedPoint: [
          { required: true, message: "请输入加分加速倍数", trigger: "blur" },
        ],
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
      getUserGradeList(this.queryParams).then( response => {
          this.list = response.data.paginationResponse.content;
          this.total = response.data.paginationResponse.totalElements;
          this.catchTypeList = response.data.catchTypeList
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
      this.dateRange = [];
      this.resetForm("queryForm");
      this.$refs.tables.sort(this.defaultSort.prop, this.defaultSort.order)
      this.handleQuery();
    },
    // 状态修改
    handleStatusChange(row) {
      let text = row.status == "A" ? "启用" : "禁用";
      this.$modal.confirm('确认要' + text + '"' + row.name + '"吗？').then(function() {
        return updateUserGradeStatus(row.id, row.status);
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(function() {
        row.status = row.status === "N" ? "A" : "N";
      });
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
      this.title = "新增会员等级";
    },
    // 表单重置
    reset() {
      this.form = {
        id: "",
        grade: "",
        name: "",
        status: "A",
        catchType: "",
        catchValue: "",
        validDay: "",
        discount: "",
        speedPoint: "",
        catchCondition: "",
        userPrivilege: ""
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
              saveUserGrade(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
          } else {
              saveUserGrade(this.form).then(response => {
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
      this.reset();
      const id = row.id || this.ids;
      getUserGradeInfo(id).then(response => {
        this.form = response.data.userGradeInfo;
        this.open = true;
        this.title = "编辑会员等级";
      });
    },
    // 删除按钮操作
    handleDelete(row) {
      const name = row.name
      this.$modal.confirm('是否确认删除"' + name + '"的数据项？').then(function() {
        return deleteUserGrade(row.id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>

