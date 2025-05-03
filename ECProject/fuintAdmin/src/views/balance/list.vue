<template>
  <div class="app-container">
    <el-form :model="queryParams" class="main-search" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="会员ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入会员ID"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="会员号" prop="userNo">
        <el-input
          v-model="queryParams.userNo"
          placeholder="请输入会员号"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input
          v-model="queryParams.mobile"
          placeholder="请输入会员手机号"
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
          <el-option key="A" label="有效" value="A"/>
          <el-option key="N" label="无效" value="N"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table ref="tables" v-loading="loading" :data="list" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column label="ID" prop="id" width="80"/>
      <el-table-column label="会员ID" align="center" prop="userInfo.userId">
        <template slot-scope="scope">
          <span v-if="scope.row.userId && scope.row.userId">{{ scope.row.userId }}</span>
          <span v-else>全体会员</span>
        </template>
      </el-table-column>
      <el-table-column label="会员号" align="center" prop="userInfo.userNo">
        <template slot-scope="scope">
          <span v-if="scope.row.userInfo && scope.row.userInfo.userNo">{{ scope.row.userInfo.userNo }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="手机号" align="center" prop="userInfo.mobile">
        <template slot-scope="scope">
          <span v-if="scope.row.userInfo && scope.row.userInfo.mobile">{{ scope.row.userInfo.mobile }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="会员名称" align="center" prop="userInfo.name">
        <template slot-scope="scope">
          <span v-if="scope.row.userInfo && scope.row.userInfo.name">
              <span>{{ scope.row.userInfo.name }}</span>
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="变动数量" align="center" prop="amount">
         <template slot-scope="scope">
             <span v-if="scope.row.amount > 0" style="color:forestgreen">+{{ scope.row.amount }}</span>
             <span v-else style="color:red">{{ scope.row.amount }}</span>
         </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark">
        <template slot-scope="scope">
          <span v-if="scope.row.description">{{ scope.row.description }}</span>
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column label="时间" align="center" width="150" prop="createTime">
        <template slot-scope="scope">
          <span v-if="scope.row.createTime">{{ parseTime(scope.row.createTime) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作人" align="center" prop="operator">
        <template slot-scope="scope">
          <span v-if="scope.row.operator">{{ scope.row.operator }}</span>
          <span v-else>-</span>
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
  </div>
</template>

<script>
import { getBalanceList } from "@/api/balance";
export default {
  name: "BalanceList",
  data() {
    return {
      // 遮罩层
      loading: true,
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
      // 默认排序
      defaultSort: {prop: 'createTime', order: 'descending'},
      // 查询参数
      queryParams: {
        page: 1,
        pageSize: 10,
        mobile: '',
        userId: '',
        userNo: '',
        orderSn: '',
        status: ''
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
      getBalanceList(this.queryParams).then( response => {
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
    }
  }
};
</script>

