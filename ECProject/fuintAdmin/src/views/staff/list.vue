<template>
  <div class="app-container">
    <el-form :model="queryParams" class="main-search" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="员工类别" prop="category">
        <el-select v-model="queryParams.category" clearable placeholder="请选择员工类别">
          <el-option
            v-for="item in categoryOptions"
            :key="item.key"
            :label="item.name"
            :value="item.key"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="姓名" prop="name">
        <el-input
          v-model="queryParams.realName"
          placeholder="请输入姓名"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input
          v-model="queryParams.mobile"
          placeholder="请输入手机号"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="auditedStatus">
        <el-select
          v-model="queryParams.auditedStatus"
          placeholder="状态"
          clearable
          style="width: 240px"
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
          v-hasPermi="['staff:list']"
        >新增员工</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tables" v-loading="loading" :data="list" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="类别" align="center" prop="category">
        <template slot-scope="scope">
          <span v-if="scope.row.category">
              <span>{{ getName(categoryOptions, scope.row.category) }}</span>
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="姓名" align="center" prop="realName" />
      <el-table-column label="手机号" align="center" prop="mobile" />
      <el-table-column label="关联会员ID" align="center" prop="userId" />
      <el-table-column label="所属店铺" align="center" prop="storeName">
        <template slot-scope="scope">
          <span v-if="scope.row.storeId">
              <span>{{ getName(storeOptions, scope.row.storeId) }}</span>
          </span>
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="storeName">
      <template slot-scope="scope">
        <el-switch
          v-model="scope.row.auditedStatus"
          active-value="A"
          inactive-value="N"
          @change="handleStatusChange(scope.row)"
        ></el-switch>
      </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            v-hasPermi="['staff:list']"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            v-hasPermi="['staff:list']"
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
            <el-form-item label="员工类别" prop="category">
              <el-select v-model="form.category" placeholder="请选择员工类别">
                <el-option key="0" label="请选择员工类别" value="0"></el-option>
                <el-option
                  v-for="item in categoryOptions"
                  :key="item.key"
                  :label="item.name"
                  :value="item.key"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入姓名" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="手机号" prop="mobile">
              <el-input v-model="form.mobile" placeholder="请输入联系人电话" maxlength="30"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="所属店铺" prop="storeId">
              <el-select v-model="form.storeId" placeholder="请选择所属店铺">
                <el-option :key="0" label="全部店铺" :value="0"></el-option>
                <el-option
                  v-for="item in storeOptions"
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
            <el-form-item label="备注信息">
              <el-input v-model="form.description" type="textarea" placeholder="请输入内容"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="状态">
              <el-radio-group v-model="form.auditedStatus">
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
import { getStaffList, getStaffInfo, updateStaffStatus, deleteStaff, saveStaff } from "@/api/staff";
import { searchStore } from "@/api/store";

export default {
  name: "StaffList",
  data() {
    return {
      // 标题
      title: "",
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      categoryOptions: [],
      storeOptions: [],
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      // 是否显示弹出层
      open: false,
      // 日期范围
      dateRange: [],
      // 默认排序
      defaultSort: {prop: 'createTime', order: 'descending'},
      // 表单参数
      form: { id: '', category: '0', realName: '', auditedStatus: 'A', mobile: '', storeId: '' },
      // 查询参数
      queryParams: {
        page: 1,
        pageSize: 10,
        realName: '',
        mobile: '',
        auditedStatus: ''
      },
      // 表单校验
      rules: {
        realName: [
          { required: true, message: "姓名不能为空", trigger: "blur" },
          { min: 2, max: 30, message: '姓名长度必须介于 2 和 20 之间', trigger: 'blur' }
        ],
        mobile: [
          { required: true, message: "手机号不能为空", trigger: "blur" },
          { min: 11, max: 20, message: '手机号长度必须11', trigger: 'blur' }
        ],
        storeId: [
          { required: true, message: "请选择所属店铺", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
    this.getStoreList();
  },
  methods: {
    // 查询列表
    getList() {
      this.loading = true;
      getStaffList(this.addDateRange(this.queryParams, this.dateRange)).then( response => {
          this.list = response.data.paginationResponse.content;
          this.total = response.data.paginationResponse.totalElements;
          this.categoryOptions = response.data.categoryList;
          this.loading = false;
        }
      );
    },
    // 店铺列表
    getStoreList() {
      searchStore().then( response => {
          this.storeOptions = response.data.storeList;
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
      let text = row.auditedStatus == "A" ? "启用" : "禁用";
      this.$modal.confirm('确认要' + text + '"' + row.realName + '"吗？').then(function() {
          return updateStaffStatus(row.id, row.auditedStatus);
      }).then(() => {
          this.$modal.msgSuccess(text + "成功");
      }).catch(function() {
          row.auditedStatus = row.auditedStatus === "A" ? "A" : "N";
      });
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.operId)
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
      this.title = "新增员工";
    },
    // 表单重置
    reset() {
      this.form = {
        id: "",
        name: "",
        category: '0',
        auditedStatus: "A",
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
          if (!this.form.id) {
            saveStaff(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          } else {
            saveStaff(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
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
      getStaffInfo(id).then(response => {
        this.form = response.data.staffInfo;
        this.form.category = response.data.staffInfo.category + '';
        this.open = true;
        this.title = "编辑员工信息";
      });
    },
    // 删除按钮操作
    handleDelete(row) {
      const name = row.realName || this.id;
      this.$modal.confirm('是否确认删除"' + name + '"的数据项？').then(function() {
        return deleteStaff(row.id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>

