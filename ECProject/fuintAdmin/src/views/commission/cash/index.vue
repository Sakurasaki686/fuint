<template>
  <div class="app-container">

    <el-form size="small" :inline="true" label-width="68px">
      <el-form-item label="所属店铺" prop="storeId">
        <el-select v-model="storeId" filterable clearable placeholder="请选择店铺" style="width: 100%;">
          <el-option
            v-for="item in storeOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="姓名">
        <el-input
          v-model="realName"
          placeholder="请输入姓名"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input
          v-model="mobile"
          placeholder="请输入手机号"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="开始日期">
        <el-date-picker
          v-model="startDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="选择开始日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="结束日期">
        <el-date-picker
          v-model="endDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="选择结束日期">
        </el-date-picker>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" :summary-method="getSummaries" show-summary border size="medium" style="width: 100%">
      <el-table-column prop="staffName" label="员工姓名" width="77"></el-table-column>
      <el-table-column prop="storeName" min-width="136" :show-overflow-tooltip="true" label="辖属门店"></el-table-column>
      <el-table-column prop="orderDate" label="开单日期">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" style="color: #3594e3;font-size: 14px;" @click="handleDetail(scope.row.staffId, scope.row.orderDate)">
            {{scope.row.orderDate}}
          </el-button>
        </template>
      </el-table-column>

      <el-table-column label="提成金额">
        <el-table-column prop="xmCount" label="项目">
          <template slot-scope="scope">
            ￥ {{scope.row.xmCount}} 元
          </template>
        </el-table-column>

        <el-table-column prop="spCount" label="商品">
          <template slot-scope="scope">
            ￥ {{scope.row.spCount}} 元
          </template>
        </el-table-column>

        <el-table-column prop="ckCount" label="次卡">
          <template slot-scope="scope">
            ￥ {{scope.row.ckCount}} 元
          </template>
        </el-table-column>

        <el-table-column prop="czCount" label="储值卡">
          <template slot-scope="scope">
            ￥ {{scope.row.czCount}} 元
          </template>
        </el-table-column>

        <el-table-column prop="commission" label="总计">
          <template slot-scope="scope">
            {{scope.row.commission}} 元
          </template>
        </el-table-column>
      </el-table-column>
    </el-table>
    <el-pagination
      style="margin-top: 5px;text-align: right"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      :page-sizes="[10, 20, 40, 60, 80, 100, 1000, 10000, 100000]"
      :page-size="pageSize"
      :current-page="currentPage"
      @current-change="handleCurrentChange"
      @size-change="handleSizeChange"
    ></el-pagination>

    <!--统计详情对话框 start-->
    <detail :show-dialog="detailDialog" ref="detail"/>
    <!--统计详情对话框 end-->

  </div>
</template>
<script>
  import {getStaffStatistics} from "@/api/commission/log";
  import {searchStore} from "@/api/store";
  import Detail from "./detail";

  export default {
    name: 'busStatList',
    components: {
      Detail
    },
    data() {
      return {
        currentPage: 1, //初始页
        pageSize: 10, //每页的数据
        total: 0,
        tableData: [],
        storeOptions: [],
        realName: '',
        mobile: '',
        storeId: '',
        startDate: null,
        endDate: null,
        openStaffScheme: false,
        detailDialog: false
      }
    },
    methods: {
      handleCurrentChange(page) {
        let params = {
          page: page,
          limit: this.pageSize,
          realName: this.realName,
          mobile: this.mobile,
          storeId: this.storeId,
          startDate: this.startDate,
          endDate: this.endDate
        }
        getStaffStatistics(params).then(r => {
            this.tableData = r.data.rows;
            this.total = r.data.total;
          }
        );
      },
      getSummaries(param) {
        const {columns, data} = param;
        const sums = [];
        columns.forEach((column, index) => {
          if (index === 0) {
            sums[index] = '总计';
            return;
          }
          if (index < 3) {
            sums[index] = '--';
            return;
          }
          const values = data.map(item => Number(item[column.property]));
          if (!values.every(value => isNaN(value))) {
            sums[index] = values.reduce((prev, curr) => {
              const value = Number(curr);
              if (!isNaN(value)) {
                return prev + curr;
              } else {
                return prev;
              }
            }, 0);
            sums[index] += ' 元';
          } else {
            sums[index] = 'N/A';
          }
        });
        return sums;
      },
      handleSizeChange(val) {
        this.pageSize = val;
        this.handleCurrentChange(1);
      },
      handleDetail(staffId,orderDate) {
        this.detailDialog = true;
        let params = {
          realName: this.realName,
          mobile: this.mobile,
          storeId: this.storeId,
          staffId: staffId,
          startDate: orderDate,
          endDate: orderDate
        }
        this.$nextTick(() => {
          this.$refs.detail.init(params);
        })
      },
      handleQuery() {
        this.handleCurrentChange(1);
      },
      // 店铺列表
      getStoreList() {
        searchStore().then(response => {
            this.storeOptions = response.data.storeList;
          }
        )
      }
    },
    created: function () {
      this.handleCurrentChange(this.currentPage);
      this.getStoreList();
    }
  }
</script>
<style scoped>
  .queryInput >>> .el-input__inner {
    border-radius: 0px;
    width: 200px;
  }
</style>
