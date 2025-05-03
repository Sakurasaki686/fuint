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
      <el-table-column label="ID" align="center" prop="id" width="60"/>
      <el-table-column label="会员ID" align="center" prop="userId" />
      <el-table-column label="手机号" align="center" width="120" prop="mobile">
        <template slot-scope="scope">
          <span v-if="scope.row.mobile">{{ scope.row.mobile }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="卡券名称" align="center" prop="couponNames">
        <template slot-scope="scope">
          <span v-if="scope.row.couponNames">{{ scope.row.couponNames }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="转赠手机号" align="center" prop="userMobile">
         <template slot-scope="scope">
             <span v-if="scope.row.userMobile">{{ scope.row.userMobile }}</span>
             <span v-else>-</span>
         </template>
      </el-table-column>
      <el-table-column label="转赠数量" align="center" prop="num">
        <template slot-scope="scope">
          <span>{{ scope.row.num }}</span>
        </template>
      </el-table-column>
      <el-table-column label="总金额(元)" align="center" prop="money">
        <template slot-scope="scope">
          <span v-if="scope.row.money">{{scope.row.money }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="转赠时间" align="center" width="180" prop="createTime">
        <template slot-scope="scope">
          <span v-if="scope.row.createTime">{{ scope.row.createTime }}</span>
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
            icon="el-icon-view"
            v-hasPermi="['coupon:give:index']"
            @click="handleDetail(scope.row)"
          >查看</el-button>
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

    <el-dialog title="转赠明细" :visible.sync="open" class="common-dialog" width="800px" append-to-body>
      <el-table ref="tables" v-loading="loading" :data="itemList">
        <el-table-column label="ID" align="center" prop="id" width="60"/>
        <el-table-column label="手机号" align="center" width="120" prop="mobile"/>
        <el-table-column label="赠予手机号" align="center" width="120" prop="userMobile"/>
        <el-table-column label="卡券名称" align="center" prop="couponName" />
        <el-table-column label="总金额" align="center" prop="money" />
        <el-table-column label="赠送时间" align="center" prop="createTime">
          <template slot-scope="scope">
            {{ parseTime(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

  </div>
</template>

<script>
import { getGiveLogList, getGiveItem } from "@/api/coupon/give";
export default {
  name: "GiveIndex",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 弹框开关
      open: false,
      // 明细列表
      itemList: [],
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
      getGiveLogList(this.queryParams).then( response => {
          this.list = response.data.paginationResponse.content;
          this.total = response.data.paginationResponse.totalElements;
          this.typeList = response.data.typeList;
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
    handleDetail(row) {
      this.loading = true;
      getGiveItem(row.id).then( response => {
          this.itemList = response.data.itemList;
          this.open = true
          this.loading = false;
        }
      );
    }
  }
};
</script>

