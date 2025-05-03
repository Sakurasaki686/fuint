<template>
  <div class="app-container">
    <el-form :model="queryParams" class="main-search" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="备注信息" prop="description">
        <el-input
          v-model="queryParams.description"
          placeholder="请输入备注信息"
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
          <el-option key="A" label="待确认" value="A"/>
          <el-option key="B" label="已完成" value="B"/>
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
          @click="doSettle"
          v-hasPermi="['settlement:doSubmit']"
        >发起结算</el-button>
      </el-form-item>
    </el-form>

    <el-table ref="tables" v-loading="loading" :data="list" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column label="ID" prop="id" width="80"/>
      <el-table-column label="结算单号" prop="settlementNo" width="180"/>
      <el-table-column label="订单总金额" align="center" prop="totalOrderAmount">
          <template slot-scope="scope">
            <span>{{ scope.row.totalOrderAmount.toFixed(2) }}</span>
          </template>
      </el-table-column>
      <el-table-column label="结算金额" align="center" prop="amount">
         <template slot-scope="scope">
             <span>{{ scope.row.amount.toFixed(2) }}</span>
         </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark">
        <template slot-scope="scope">
          <span v-if="scope.row.description">{{ scope.row.description }}</span>
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column label="结算时间" align="center" prop="createTime">
        <template slot-scope="scope">
          <span v-if="scope.row.createTime">{{ parseTime(scope.row.createTime) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="80" prop="status">
        <template slot-scope="scope">
          <span>{{ getName(settleStatusList, scope.row.status) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作人" align="center" prop="createTime">
        <template slot-scope="scope">
          <span v-if="scope.row.operator">{{ scope.row.operator }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="130" fixed='right'>
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            v-hasPermi="['settlement:index']"
            @click="onDetail(scope.row)"
          >详情</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            :disabled="scope.row.status == 'B'"
            v-hasPermi="['settlement:doConfirm']"
            @click="doConfirm(scope.row)"
          >确认</el-button>
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

    <!-- 结算对话框 -->
    <el-dialog title="发起结算" :visible.sync="settleDialog" class="common-dialog" width="80%" append-to-body>
      <el-form ref="form" :model="form" label-width="120px" size="small" :inline="true">
        <el-row>
            <el-form-item label="结算商户" prop="merchantId">
              <el-select class="input" v-model="form.merchantId" style="width: 240px" placeholder="请选择商户">
                <el-option
                  v-for="item in merchantList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                  :disabled="item.status !== 'A'"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="结算店铺" prop="storeId">
              <el-select class="input" v-model="form.storeId" style="width: 240px" clearable placeholder="请选择店铺">
                <el-option
                  v-for="item in storeList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                  :disabled="item.status !== 'A'"
                ></el-option>
              </el-select>
            </el-form-item>
        </el-row>
        <el-row>
            <el-form-item label="下单时间">
              <el-date-picker
                v-model="form.startTime"
                value-format="yyyy-MM-dd HH:mm:ss"
                type="datetime"
                placeholder="开始时间"
              ></el-date-picker>
              <span class="sp"> ~ </span>
              <el-date-picker
                v-model="form.endTime"
                value-format="yyyy-MM-dd HH:mm:ss"
                type="datetime"
                placeholder="结束时间"
              ></el-date-picker>
            </el-form-item>
            <el-form-item>
               <el-button type="danger" icon="el-icon-search" size="mini" @click="queryOrder">查询订单</el-button>
            </el-form-item>
        </el-row>
      </el-form>

      <el-table ref="tables" v-loading="loading" :data="orderList">
        <el-table-column label="订单ID" prop="id" width="80"/>
        <el-table-column label="订单号" prop="orderSn" width="190"/>
        <el-table-column label="订单类型" align="center" prop="typeName">
          <template slot-scope="scope">
            <span>{{ scope.row.typeName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="所属店铺" align="center" width="140" prop="storeInfo.name">
          <template slot-scope="scope">
            <span v-if="scope.row.storeInfo">{{ scope.row.storeInfo.name }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="订单金额" align="center" prop="amount">
          <template slot-scope="scope">
            <span v-if="true">{{ scope.row.amount.toFixed(2) }}</span>
            <span v-else>0.00</span>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" align="center" width="130" prop="createTime">
          <template slot-scope="scope">
            <span>{{ scope.row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" align="center" width="80" prop="status">
          <template slot-scope="scope">
            <span class="status-normal">{{ getName(statusList, scope.row.status) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="totalOrder>0"
        :total="totalOrder"
        :page.sync="form.page"
        :limit.sync="form.pageSize"
        @pagination="queryOrder"
      />
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="doSubmit">确定结算</el-button>
        <el-button @click="cancelSettle">取消</el-button>
      </div>
    </el-dialog>

    <!--结算详情对话框-->
    <el-dialog title="结算详情" :visible.sync="detailDialog" class="common-dialog" width="80%" append-to-body>
      <el-table ref="tables" v-loading="loading" :data="settlementInfo ? settlementInfo.orderList.content : []">
        <el-table-column label="订单ID" prop="orderId" width="80">
          <template slot-scope="scope">
            <span>{{ scope.row.orderInfo.id }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单号" prop="orderSn" width="190">
          <template slot-scope="scope">
            <span>{{ scope.row.orderInfo.orderSn }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单类型" align="center" prop="typeName">
          <template slot-scope="scope">
            <span>{{ scope.row.orderInfo.typeName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="所属店铺" align="center" width="140" prop="storeInfo.name">
          <template slot-scope="scope">
            <span v-if="scope.row.orderInfo.storeInfo">{{ scope.row.orderInfo.storeInfo.name }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="订单金额" align="center" prop="amount">
          <template slot-scope="scope">
            <span v-if="true">{{ scope.row.orderInfo.amount.toFixed(2) }}</span>
            <span v-else>0.00</span>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" align="center" width="130" prop="createTime">
          <template slot-scope="scope">
            <span>{{ scope.row.orderInfo.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" align="center" width="80" prop="status">
          <template slot-scope="scope">
            <span class="status-normal">{{ getName(statusList, scope.row.orderInfo.status) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="settlementInfoTotalOrder>0"
        :total="settlementInfoTotalOrder"
        :page.sync="settlementInfoQuery.page"
        :limit.sync="settlementInfoQuery.pageSize"
        @pagination="getSettlementInfo"
      />
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="closeDetail">关闭</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { getSettlementList, getSettlementInfo, doSubmit, doConfirm } from "@/api/settlement";
import { getOrderList } from "@/api/order";
export default {
  name: "Settlement",
  data() {
    return {
      // 发起结算
      settleDialog: false,
      // 结算详情
      detailDialog: false,
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
      // 商户列表
      merchantList: [],
      // 店铺列表
      storeList: [],
      // 订单列表
      orderList: [],
      // 总订单数
      totalOrder: 0,
      // 订单状态列表
      statusList: [],
      // 结算状态列表
      settleStatusList: [],
      // 结算详情
      settlementInfo: null,
      // 默认排序
      defaultSort: {prop: 'createTime', order: 'descending'},
      // 查询参数
      queryParams: {
        page: 1,
        pageSize: 10,
        status: ''
      },
      // 结算详情查询参数
      settlementInfoQuery: {
        settlementId: 0,
        page: 1,
        pageSize: 10
      },
      // 结算单订单数量
      settlementInfoTotalOrder: 0,
      form: { page: 1, pageSize: 10, merchantId: this.$store.getters.merchantId, storeId: '', startTime: '', endTime: '' }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // 查询列表
    getList() {
      this.loading = true;
      getSettlementList(this.queryParams).then( response => {
          this.list = response.data.paginationResponse.content;
          this.total = response.data.paginationResponse.totalElements;
          this.merchantList = response.data.merchantList;
          this.storeList = response.data.storeList;
          this.settleStatusList = response.data.statusList;
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
      this.$refs.tables.sort(this.defaultSort.prop, this.defaultSort.order);
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
    // 发起结算
    doSettle() {
      this.settleDialog = true;
      this.form.merchantId = this.$store.getters.merchantId;
      this.form.storeId = '';
      this.form.startTime = '';
      this.form.endTime = '';
    },
    // 取消结算
    cancelSettle() {
      this.settleDialog = false;
    },
    // 提交结算
    doSubmit() {
      const app = this;
      app.loading = true;
      app.$refs["form"].validate(valid => {
        if (valid) {
            doSubmit(this.form).then(response => {
              app.settleDialog = false;
              app.loading = false;
              if (response.data) {
                  app.$modal.msgSuccess("提交结算成功！");
                  app.getList();
                  app.orderList = [];
                  app.totalOrder = 0;
              }
            }).catch(function() {
                app.loading = false;
            });
        }
      });
    },
    // 结算确认
    doConfirm(row) {
      const app = this;
      app.$modal.confirm('确定已完成结算吗？').then(function() {
        let param = { settlementId: row.id };
        app.loading = true;
        doConfirm(param).then(response => {
            app.loading = false;
            if (response.code == '200') {
                app.$modal.msgSuccess("完成结算");
                app.getList();
            } else {
                app.$modal.msgError("确认失败");
            }
        }).catch(function() {
            app.loading = false;
        });
      })
    },
    // 查看结算详情
    onDetail(row) {
       this.settlementInfoQuery.settlementId = row.id;
       this.getSettlementInfo();
       this.detailDialog = true;
    },
    // 查询结算详情
    getSettlementInfo() {
      const app = this;
      app.loading = true;
      getSettlementInfo(app.settlementInfoQuery).then(response => {
        app.settlementInfo = response.data.settlementInfo;
        app.settlementInfoTotalOrder = app.settlementInfo.orderList.totalElements;
        app.statusList = response.data.statusList;
        app.loading = false;
      }).catch(function() {
        app.loading = false;
      });
    },
    // 关闭详情对话框
    closeDetail() {
      this.settlementInfo = null;
      this.detailDialog = false;
    },
    // 查询订单
    queryOrder() {
      const app = this;
      app.loading = true;
      app.form.payStatus = 'B';
      app.form.settleStatus = 'A';
      getOrderList(this.form).then( response => {
          app.orderList = response.data.paginationResponse.content;
          app.totalOrder = response.data.paginationResponse.totalElements;
          app.statusList = response.data.statusList;
          app.loading = false;
        }).catch(function() {
           app.loading = false;
      });;
    }
  }
};
</script>

