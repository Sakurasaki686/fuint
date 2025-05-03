<template>
  <div class="app-container">
    <el-form :model="queryParams" class="main-search" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="ID" prop="id">
        <el-input
          v-model="queryParams.id"
          placeholder="请输入ID"
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
          style="width: 150px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="会员ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入会员ID"
          clearable
          style="width: 150px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="会员号" prop="userNo">
        <el-input
          v-model="queryParams.userNo"
          placeholder="请输入会员号"
          clearable
          style="width: 150px;"
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
      <el-form-item label="核销码" prop="code">
        <el-input
          v-model="queryParams.code"
          placeholder="请输入核销二维码"
          clearable
          style="width: 150px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          style="width: 150px"
          clearable
        >
          <el-option v-for="statusInfo in statusList" :key="statusInfo.key" :label="statusInfo.name" :value="statusInfo.key"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery(false)">搜索</el-button>
        <el-button type="danger" icon="el-icon-download" size="mini" @click="handleQuery(true)">导出</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table ref="tables" v-loading="loading" :data="list" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column label="ID" prop="id" width="60"/>
      <el-table-column label="核销码" prop="code" width="150"/>
      <el-table-column label="会员ID" align="center" prop="userInfo.id">
        <template slot-scope="scope">
          <span v-if="scope.row.userInfo">
              <span>{{ scope.row.userInfo.id }}</span>
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="会员号" align="center" width="130" prop="userInfo.userNo">
        <template slot-scope="scope">
          <span v-if="scope.row.userInfo && scope.row.userInfo.userNo">{{ scope.row.userInfo.userNo }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="手机号" align="center" width="120" prop="userInfo.mobile">
        <template slot-scope="scope">
          <span v-if="scope.row.userInfo">{{ scope.row.userInfo.mobile ? scope.row.userInfo.mobile : '-' }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="会员名称" align="center" prop="userInfo.name">
        <template slot-scope="scope">
          <span v-if="scope.row.userInfo">
              <span>{{ scope.row.userInfo.name }}</span>
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="卡券类型" align="center" prop="type">
        <template slot-scope="scope">
          <span v-if="scope.row.type">{{ getName(typeList, scope.row.type) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="卡券名称" align="center" prop="name"/>
      <el-table-column label="所属店铺" align="center" prop="storeInfo">
        <template slot-scope="scope">
          <span v-if="scope.row.storeInfo">{{ scope.row.storeInfo.name }}</span>
          <span v-else>暂无</span>
        </template>
      </el-table-column>
      <el-table-column label="面额(元)" align="center" prop="amount">
         <template slot-scope="scope">
           <span style="color:red">{{ scope.row.amount }}</span>
         </template>
      </el-table-column>
      <el-table-column label="余额(元)" align="center" prop="balance">
        <template slot-scope="scope">
          <span v-if="scope.row.balance" style="color:forestgreen">{{ scope.row.balance }}</span>
          <span v-else style="color:forestgreen">{{ scope.row.amount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <span v-if="scope.row.status">{{ getName(statusList, scope.row.status) }}</span>
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column label="领取时间" align="center" width="150" prop="createTime">
        <template slot-scope="scope">
          <span v-if="scope.row.createTime">{{ parseTime(scope.row.createTime) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed='right' width="120">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.status == 'A'"
            size="mini"
            type="text"
            icon="el-icon-edit"
            v-hasPermi="['coupon:confirm:index']"
            @click="handleConfirm(scope.row)"
          >核销</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            v-hasPermi="['coupon:userCoupon:delete']"
            @click="handleDelete(scope.row)"
          >作废</el-button>
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
import { getToken } from "@/utils/auth";
import { getUserCouponList, deleteUserCoupon } from "@/api/coupon/userCoupon";
export default {
  name: "UserCouponIndex",
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
      // 状态列表
      statusList: [],
      // 默认排序
      defaultSort: {prop: 'createTime', order: 'descending'},
      // 查询参数
      queryParams: {
        page: 1,
        pageSize: 10,
        mobile: '',
        userId: '',
        userNo: '',
        status: '',
        id: '',
        couponId: ''
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
      getUserCouponList(this.queryParams).then(response => {
          this.list = response.data.paginationResponse.content;
          this.total = response.data.paginationResponse.totalElements;
          this.typeList = response.data.typeList;
          this.statusList = response.data.statusList;
          this.loading = false;
        }
      );
    },
    // 导出excel
    exportExcel() {
      this.download('/backendApi/userCoupon/exportList', {
        ...this.queryParams
      })
    },
    // 搜索按钮操作
    handleQuery(isExport) {
      this.queryParams.page = 1;
      if (isExport) {
          this.exportExcel();
      } else {
          this.getList();
      }
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
    // 删除按钮操作
    handleDelete(row) {
      this.$modal.confirm('是否确认作废ID等于' + row.id + '的数据项？').then(function() {
        return deleteUserCoupon(row.id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("作废成功");
      }).catch(() => {});
    },
    // 核销按钮操作
    handleConfirm(row) {
      this.$router.push( { path:  '/coupon/confirm/index?code=' + row.code } );
    },
  }
};
</script>

