<template>
  <el-dialog class="common-dialog" title="提成明细" :visible="showDialog" width="1200px" @close="cancel" destroy-on-close>
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
  </el-dialog>
</template>
<script>
  import {getStaffStatisticsDetail} from "@/api/commission/log";

  export default {
    props: {
      orderInfo: {
        type: [Object],
        default: () => {
        }
      }
    },
    data() {
      return {
        showDialog: false,
        tableData: [],
        currentPage: 1, // 初始页
        pageSize: 10, // 每页的数据
        total: 0,
        realName: '',
        mobile: '',
        storeId: '',
        staffId: '',
        startDate: null,
        endDate: null
      }
    },
    methods: {
      init(params) {

        this.realName = params.realName;
        this.mobile = params.mobile;
        this.storeId = params.storeId;
        this.staffId = params.staffId;
        this.startDate = params.startDate;
        this.endDate = params.endDate;

        this.showDialog = true;
        this.handleCurrentChange(1);
      },
      // 订单详情
      handleView(orderId) {
        this.$router.push({path: '/order/detail?orderId=' + orderId})
      },
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
      dataFormat: function (row, column) {
        //商品分类=提成类型：1：项目=劳动提成:2：卡项=销售提成:3：商品=销售提成
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
      cancel() {
        this.showDialog = false;
      }
    }
  }
</script>
<style scoped lang="scss">
  .print-area {
    font-size: 14px;
    border: solid 1px #ccc;
    padding: 10px;

    .store {
      font-weight: bold;
      margin-bottom: 20px;
    }

    .goods-list {
      margin-bottom: 20px;

      .goods-item {
        margin-bottom: 2px;
      }
    }

    .member-info {
      margin-bottom: 20px;
    }

    .total-info {
      .item {
        margin-bottom: 2px;
      }
    }
  }
</style>
<style media="print" lang="scss">
  @page {
    size: auto;
    margin: 0mm;
  }

  @media print {
    html {
      height: auto;
      margin: 0px;
    }
    body {
      border: solid 1px #ffffff;
    }
    #printArea {
      font-size: 12px;
      display: block;
      min-height: 400px;

      .store {
        margin-bottom: 30px;
        font-weight: bold;
      }

      .goods-list {
        margin-bottom: 30px;

        .goods-item {
          margin-bottom: 15px;

          .item {
            margin-right: 10px;
          }
        }
      }

      .member-info {
        margin-top: 20px;
        margin-bottom: 20px;

        .item {
          margin-bottom: 2px;
        }
      }

      .total-info {
        margin-top: 20px;
        margin-bottom: 20px;

        .item {
          margin-bottom: 2px;
        }
      }
    }
  }
</style>
