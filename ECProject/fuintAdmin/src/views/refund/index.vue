<template>
  <div class="app-container">
    <el-form :model="queryParams" class="main-search" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="订单号" prop="name">
        <el-input
          v-model="queryParams.orderSn"
          placeholder="请输入订单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="name">
        <el-input
          v-model="queryParams.mobile"
          placeholder="请输入手机号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="会员ID" prop="name">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入会员ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
        >
          <el-option v-for="statusItem in statusList" :key="statusItem.key+''" :label="statusItem.name" :value="statusItem.key+''"/>
        </el-select>
      </el-form-item>
      <el-form-item label="提交时间">
        <el-date-picker
          v-model="queryParams.startTime"
          value-format="yyyy-MM-dd HH:mm:ss"
          type="datetime"
          style="width:200px"
          placeholder="开始时间"
        ></el-date-picker>
        <span class="sp"> ~ </span>
        <el-date-picker
          v-model="queryParams.endTime"
          value-format="yyyy-MM-dd HH:mm:ss"
          type="datetime"
          style="width:200px"
          placeholder="结束时间"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table ref="tables" v-loading="loading" :data="list" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column label="售后ID" prop="id" width="60"/>
      <el-table-column label="订单ID" align="center" prop="orderId" />
      <el-table-column label="会员ID" align="center" prop="userId" />
      <el-table-column label="店铺ID" align="center" prop="storeId" />
      <el-table-column label="售后类型" align="center" prop="type">
         <template slot-scope="scope">
             <span v-if="scope.row.type">{{ getName(refundTypeList, scope.row.type) }}</span>
         </template>
      </el-table-column>
      <el-table-column label="退款金额" align="center" prop="amount">
        <template slot-scope="scope">
          <span v-if="true">{{ scope.row.amount.toFixed(2) }}</span>
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark">
        <template slot-scope="scope">
          <span v-if="scope.row.remark">{{ scope.row.remark }}</span>
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="80" prop="status">
        <template slot-scope="scope">
          <span v-if="scope.row.status == 'B'" class="status-active">{{ getName(statusList, scope.row.status) }}</span>
          <span v-else-if="scope.row.status == 'A'" class="status-normal">{{ getName(statusList, scope.row.status) }}</span>
          <span v-else class="status-disabled">{{ getName(statusList, scope.row.status) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" align="center" width="140" prop="createTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" width="140" prop="updateTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            v-hasPermi="['order:detail']"
            @click="handleView(scope.row)"
          >详情</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            v-hasPermi="['refund:edit']"
            @click="handleUpdate(scope.row)"
          >审核</el-button>
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

    <!-- 审核对话框 start-->
    <el-dialog :title="title" :visible.sync="open" class="common-dialog" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="订单号" prop="orderSn">
              <el-input v-model="orderInfo.orderSn" placeholder="订单号" disabled maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="订单总金额" prop="amount">
              <el-input v-model="'￥' + orderInfo.amount" placeholder="订单总金额" disabled maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="orderInfo.userInfo">
          <el-col :span="24">
            <el-form-item label="会员名称" prop="userInfo">
              <el-input v-model="orderInfo.userInfo.name" placeholder="会员名称" disabled maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="申请时间" prop="createTime">
              <el-input v-model="orderInfo.createTime" placeholder="申请时间" disabled maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="申请备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" type="textarea" maxlength="255" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="状态">
              <el-select v-model="form.status" placeholder="状态">
                <el-option v-for="statusItem in statusList" :key="statusItem.key+''" :label="statusItem.name" :value="statusItem.key+''"/>
              </el-select>
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
import { getRefundList, getRefundInfo, saveRefund } from "@/api/refund";
import {parseTime} from "../../utils/fuint";
export default {
  name: "RefundIndex",
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
      // 状态列表
      statusList: [],
      // 售后类型列表
      refundTypeList: [],
      // 是否显示弹出层
      open: false,
      // 默认排序
      defaultSort: {prop: 'createTime', order: 'descending'},
      // 表单参数
      form: { id: '', refundId: '', remark: '', status: "A", createTime: ''},
      // 订单详情
      orderInfo: {},
      // 查询参数
      queryParams: {
        page: 1,
        pageSize: 10,
        orderSn: '',
        status: '',
        startTime: '',
        endTime: ''
      },
      // 表单校验
      rules: {
        status: [
          { required: true, message: "请选择状态", trigger: "blur" },
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // 查询售后订单列表
    getList() {
      this.loading = true;
      getRefundList(this.queryParams).then( response => {
          this.list = response.data.paginationResponse.content;
          this.total = response.data.paginationResponse.totalElements;
          this.statusList = response.data.statusList;
          this.refundTypeList = response.data.refundTypeList;
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
      this.ids = selection.map(item => item.id);
      this.multiple = !selection.length;
    },
    // 排序触发事件
    handleSortChange(column, prop, order) {
      this.queryParams.orderByColumn = column.prop;
      this.queryParams.isAsc = column.order;
      this.getList();
    },
    // 表单重置
    reset() {
      this.form = {
        id: "",
        refundId: "",
        status: "A",
        remark: "",
        createTime: ""
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
            saveRefund(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
        }
      });
    },
    // 审核按钮操作
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getRefundInfo(id).then(response => {
        this.form = response.data.refundInfo;
        this.form.refundId = this.form.id;
        this.form.createTime = parseTime(this.form.createTime);
        this.orderInfo = response.data.orderInfo;
        this.open = true;
        this.title = "审核售后订单";
      });
    },
    // 查看详情按钮操作
    handleView(row) {
      this.$router.push( { path: '/order/detail?orderId=' + row.orderId } )
    },
  }
};
</script>

