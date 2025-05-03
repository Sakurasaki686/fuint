<template>
  <div class="app-container">

    <!--查询参数-->
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
      <el-table-column prop="orderId" label="来源订单">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            style="color: #3594e3;font-size: 14px;"
            v-hasPermi="['order:index']"
            @click="handleView(scope.row.orderId)"
          >订单
          </el-button>
        </template>
      </el-table-column>
      <el-table-column prop="staffName" label="员工姓名" width="77"></el-table-column>
      <el-table-column prop="storeName" min-width="136" :show-overflow-tooltip="true" label="开单门店"></el-table-column>
      <el-table-column prop="orderDate" label="开单日期" width="116"></el-table-column>
      <el-table-column prop="goodName" label="商品名称" min-width="136" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="goodPrice" label="商品价格">
        <template slot-scope="scope">
          {{scope.row.goodPrice}} 元
        </template>
      </el-table-column>
      <el-table-column prop="goodNum" label="商品数量" width="77"></el-table-column>
      <el-table-column prop="priceType" label="价格类型">原价</el-table-column>
      <el-table-column prop="tcWay" label="提成方式">
        <template slot-scope="scope">
          <span v-if="scope.row.tcWay=='1'">比率提成</span>
          <span v-if="scope.row.tcWay=='2'">固定提成</span>
        </template>
      </el-table-column>
      <el-table-column prop="visitorVal" label="散客（首充）" width="106">
        <template slot-scope="scope">
          <span v-if="scope.row.tcWay=='1'">{{scope.row.visitorVal}} %</span>
          <span v-if="scope.row.tcWay=='2'">{{scope.row.visitorVal}} 元</span>
        </template>
      </el-table-column>
      <el-table-column prop="memberVal" label="会员（续充）" width="106">
        <template slot-scope="scope">
          <span v-if="scope.row.tcWay=='1'">{{scope.row.memberVal}} %</span>
          <span v-if="scope.row.tcWay=='2'">{{scope.row.memberVal}} 元</span>
        </template>
      </el-table-column>

      <el-table-column label="商品分类" width="77">
        <template slot-scope="scope">
          <span v-if="scope.row.comType=='1'">项目</span>
          <span v-if="scope.row.comType=='2'">商品</span>
          <span v-if="scope.row.comType=='4'">次卡</span>
          <span v-if="scope.row.comType=='5'">储值卡</span>
        </template>
      </el-table-column>

      <el-table-column prop="comType" label="提成类型" :formatter="dataFormat"></el-table-column>
      <el-table-column prop="commission" label="提成金额">
        <template slot-scope="scope">
          {{scope.row.commission}} 元
        </template>
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


  </div>
</template>
<script>
  import { getStaffStatisticsDetail } from "@/api/commission/log";
  import { searchStore } from "@/api/store";

  export default {
    name: 'busStaffList',
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
          staffId: this.staffId,
          startDate: this.startDate,
          endDate: this.endDate
        }
        getStaffStatisticsDetail(params).then(r => {
            this.tableData = r.data.rows;
            this.total = r.data.total;
          }
        );
      },
      handleSizeChange(val) {
        this.pageSize = val;
        this.handleCurrentChange(1);
      },
      dataFormat: function (row, column) {
        let val = row[column.property];
        if (val == '1') {
          return '劳动提成'
        }
        if (val == '3') {
          return '销售提成'
        }
        if (val == '4') {
          return '销售提成'
        }
        if (val == '5') {
          return '销售提成'
        }
        return val;
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
      },
      // 订单详情
      handleView(orderId) {
        this.$router.push({path: '/order/detail?orderId=' + orderId})
      },
      getSummaries(param) {
        const {columns, data} = param;
        const sums = [];
        columns.forEach((column, index) => {
          if (index === 0) {
            sums[index] = '总计';
            return;
          }
          if (index < 13) {
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
      handleSelectionChange(val) {
        this.multipleSelection = val;
      }
    },
    created: function () {
      this.handleCurrentChange(this.currentPage);
      this.getStoreList();
    }
  }
</script>
<style scoped>

  select {
    /*去除select边框*/
    border: 0;
    background: transparent;
    /*去除下拉框的三角下标*/
    appearance: none;
    -moz-appearance: none; /* Firefox */
    -webkit-appearance: none; /* Safari 和 Chrome */
    height: 32px;
    width: 100%;
    background-color: white;
    padding-left: 5px;
    border-radius: 5px;
  }

  input {
    width: 96%;
    height: 32px;
    font-size: 14px;
    color: #686868;
    border: 1px solid #d3d6dd;
    padding-left: 5px;
    border-radius: 5px;
  }

  .queryInput >>> .el-input__inner {
    border-radius: 0px;
    width: 200px;
  }
</style>
