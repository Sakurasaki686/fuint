<template>
  <div class="app-container">
    <el-form :model="queryParams" class="main-search" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="卡券ID" prop="couponId">
        <el-input
          v-model="queryParams.couponId"
          placeholder="请输入卡券ID"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="会员等级" prop="gradeId">
        <el-select
          v-model="queryParams.gradeId"
          clearable
          placeholder="会员等级"
        >
          <el-option v-for="grade in userGradeList" :key="grade.id" :label="grade.name" :value="grade.id"/>
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
          v-hasPermi="['openGift:add']"
        >新增</el-button>
      </el-form-item>
    </el-form>

    <el-table ref="tables" v-loading="loading" :data="list" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column label="ID" prop="id" width="55"/>
      <el-table-column label="会员等级" align="center" prop="gradeInfo.name" />
      <el-table-column label="赠送卡券" align="center" prop="couponInfo.name">
        <template slot-scope="scope">
          <span v-if="scope.row.couponInfo">
              <span>{{ scope.row.couponInfo.name }}</span>
          </span>
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column label="卡券数量" align="center" prop="couponNum" />
      <el-table-column label="赠送积分" align="center" prop="point">
         <template slot-scope="scope">
             <span v-if="scope.row.point > 0">{{ scope.row.point }}</span>
             <span v-else>无</span>
         </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime">
        <template slot-scope="scope">
          <span>{{ scope.row.createTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime">
        <template slot-scope="scope">
          <span>{{ scope.row.updateTime }}</span>
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
            v-hasPermi="['openGift:add']"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            v-hasPermi="['openGift:add']"
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
            <el-form-item label="会员等级" prop="gradeId">
              <el-select
                v-model="form.gradeId"
                style="width: 240px"
                placeholder="会员等级"
              >
                <el-option v-for="grade in userGradeList" :key="grade.id" :label="grade.name" :value="grade.id"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="卡券ID" prop="couponId">
              <el-input v-model="form.couponId" placeholder="请输入要赠送的卡券ID" maxlength="10" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="卡券数量" prop="couponNum">
              <el-input v-model="form.couponNum" placeholder="请输入要赠送的卡券数量" maxlength="10" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="积分数量" prop="point">
              <el-input v-model="form.point" placeholder="请输入赠送积分数量"></el-input>
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
import { getOpenGiftList, updateOpenGiftStatus, getOpenGiftInfo, saveOpenGift, deleteOpenGift } from "@/api/openGift";
export default {
  name: "OpenGiftIndex",
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
      // 会员等级列表
      userGradeList: [],
      // 是否显示弹出层
      open: false,
      // 默认排序
      defaultSort: {prop: 'createTime', order: 'descending'},
      // 表单参数
      form: { id: '', gradeId: '', couponId: '', couponNum: '', point: '', status: "A" },
      // 查询参数
      queryParams: {
        page: 1,
        pageSize: 10,
        couponId: '',
        gradeId: '',
        status: ''
      },
      // 表单校验
      rules: {
        gradeId: [
          { required: true, message: "会员等级不能为空", trigger: "blur" },
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
      getOpenGiftList(this.queryParams).then( response => {
          this.list = response.data.paginationResponse.content;
          this.total = response.data.paginationResponse.totalElements;
          this.userGradeList = response.data.userGradeList
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
      this.$modal.confirm('确认要' + text + 'ID等于' + row.id + '的数据项吗？').then(function() {
        return updateOpenGiftStatus(row.id, row.status);
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
      this.title = "新增开卡赠礼";
    },
    // 表单重置
    reset() {
      this.form = {
        id: "",
        gradeId: "",
        couponId: "",
        couponNum: "",
        point: ""
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
              saveOpenGift(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
          } else {
              saveOpenGift(this.form).then(response => {
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
      getOpenGiftInfo(id).then(response => {
        this.form.id = response.data.openGiftInfo.id;
        if (response.data.openGiftInfo.gradeInfo) {
            this.form.gradeId = response.data.openGiftInfo.gradeInfo.id
        }
        if (response.data.openGiftInfo.couponInfo) {
            this.form.couponId = response.data.openGiftInfo.couponInfo.id;
            this.form.couponNum = response.data.openGiftInfo.couponNum;
        }
        this.form.point = response.data.openGiftInfo.point;
        this.open = true;
        this.title = "编辑开卡赠礼";
      });
    },
    // 删除按钮操作
    handleDelete(row) {
      this.$modal.confirm('是否确认删除ID等于' + row.id + '的数据项？').then(function() {
        return deleteOpenGift(row.id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>

