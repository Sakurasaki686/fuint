<template>
  <div class="app-container">
    <el-form :model="queryParams" class="main-search" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="会员ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入会员ID"
          clearable
          style="width: 140px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input
          v-model="queryParams.mobile"
          placeholder="请输入会员手机号"
          clearable
          style="width: 150px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="卡券ID" prop="couponId">
        <el-input
          v-model="queryParams.couponId"
          placeholder="请输入卡券ID"
          clearable
          style="width: 180px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          style="width: 100px"
          clearable
        >
          <el-option key="A" label="正常" value="A"/>
          <el-option key="D" label="撤销" value="D"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table ref="tables" v-loading="loading" :data="list" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column label="流水号" align="center" prop="code" width="200"/>
      <el-table-column label="会员ID" align="center" prop="userInfo.id" />
      <el-table-column label="手机号" align="center" width="120" prop="userInfo.mobile">
        <template slot-scope="scope">
          <span v-if="scope.row.userInfo.mobile">{{ scope.row.userInfo.mobile }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="会员名称" align="center" prop="userInfo.name">
        <template slot-scope="scope">
          <span v-if="scope.row.userInfo.name">
              <span>{{ scope.row.userInfo.name }}</span>
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="卡券类型" align="center" prop="couponInfo.type">
        <template slot-scope="scope">
          <span v-if="scope.row.couponInfo">{{ getName(typeList, scope.row.couponInfo.type) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="卡券名称" align="center" prop="couponInfo.name">
        <template slot-scope="scope">
          <span v-if="scope.row.couponInfo">{{ scope.row.couponInfo.name }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="所属店铺" align="center" prop="storeInfo.name">
        <template slot-scope="scope">
          <span v-if="scope.row.storeInfo">{{ scope.row.storeInfo.name }}</span>
          <span v-else>暂无</span>
        </template>
      </el-table-column>
      <el-table-column label="核销金额" align="center" prop="amount">
         <template slot-scope="scope">
             <span v-if="scope.row.amount" style="color:red">{{ scope.row.amount }}元</span>
             <span v-else>-</span>
         </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark">
        <template slot-scope="scope">
          <span>{{ scope.row.remark }}</span>
        </template>
      </el-table-column>
      <el-table-column label="核销人" align="center" prop="status">
        <template slot-scope="scope">
          <span v-if="scope.row.operator">{{scope.row.operator }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="核销时间" align="center" width="150" prop="createTime">
        <template slot-scope="scope">
          <span v-if="scope.row.createTime">{{ parseTime(scope.row.createTime) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <span v-if="scope.row.status == 'A'">正常</span>
          <span v-else>撤销</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="68">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            v-if="scope.row.status == 'A'"
            icon="el-icon-delete"
            v-hasPermi="['coupon:confirm:index']"
            @click="handleDelete(scope.row)"
          >撤销</el-button>
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
import { getConfirmLogList, rollbackUserCoupon } from "@/api/coupon/confirmLog";
export default {
  name: "ConfirmLogIndex",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      // 卡券类型
      typeList: [],
      // 默认排序
      defaultSort: {prop: 'createTime', order: 'descending'},
      // 表单参数
      // 查询参数
      queryParams: {
        page: 1,
        pageSize: 10,
        mobile: '',
        userId: '',
        couponId: '',
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
      getConfirmLogList(this.queryParams).then( response => {
          this.list = response.data.paginationResponse.content;
          this.total = response.data.paginationResponse.totalElements;
          this.typeList = response.data.typeList
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
    },
    // 撤销按钮操作
    handleDelete(row) {
      this.$modal.confirm('是否确认撤销ID等于' + row.id + '的数据项？').then(function() {
        return rollbackUserCoupon(row.id, row.userCouponId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("撤销成功");
      }).catch(() => {});
    }
  }
};
</script>

