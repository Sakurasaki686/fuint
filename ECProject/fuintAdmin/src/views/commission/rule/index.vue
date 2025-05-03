<template>
  <div class="app-container">
    <!--查询参数-->
    <el-form size="small" :inline="true" class="main-search" label-width="68px">
      <el-form-item label="方案名称">
        <el-input
          v-model="name"
          placeholder="请输入方案名称"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="方案对象" prop="target">
        <el-select v-model="target" placeholder="请选择方案对象" clearable style="width: 100%;">
          <el-option key="staff" label="员工提成" value="staff"/>
          <el-option key="member" label="会员分销" value="member"/>
        </el-select>
      </el-form-item>
      <el-form-item label="方案类型" prop="type">
        <el-select v-model="type" placeholder="请选择方案类型" clearable style="width: 100%;">
          <el-option key="goods" label="商品销售" value="goods"/>
          <el-option key="coupon" label="卡券销售" value="coupon"/>
          <el-option key="recharge" label="会员充值" value="recharge"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          v-hasPermi="['commission:rule:index']"
          @click="handleAdd('')"
        >新增方案
        </el-button>
      </el-form-item>
    </el-form>
    <!--方案列表 start-->
    <el-table :data="list" v-loading="loading" size="medium" style="width: 100%">
      <el-table-column prop="name" label="方案名称"></el-table-column>
      <el-table-column prop="target" label="方案对象" :formatter="dataFormatTarget"></el-table-column>
      <el-table-column prop="type" label="方案类型" :formatter="dataFormat"></el-table-column>
      <el-table-column label="创建时间">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            v-hasPermi="['commission:rule:index']"
            @click="handleAdd(scope.row.id)"
          >修改
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            v-hasPermi="['commission:rule:index']"
            @click="handleDelete(scope.row)"
          >删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="page"
      :limit.sync="pageSize"
      @pagination="getCommissionRuleList"
    />
    <!-- 添加或修改方案对话框 -->
    <el-dialog :title="title" :visible.sync="open" class="common-dialog" width="1100px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="126px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="方案名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入名称" maxlength="90" style="width: 55%"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="方案对象" prop="target">
              <el-radio-group v-model="form.target">
                <el-radio label="staff">员工提成</el-radio>
                <el-radio label="member">会员分销</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="方案类型" prop="type">
              <el-radio-group v-model="form.type" @change="changeType()">
                <el-radio label="goods">商品销售</el-radio>
                <el-radio label="coupon">卡券销售</el-radio>
                <el-radio label="recharge">会员充值</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="适用门店" prop="storeIdList">
              <el-select v-model="form.storeIdList" filterable multiple clearable placeholder="请选择适用门店" style="width: 55%">
                <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <div v-if="form.type == 'recharge'">
          <el-row>
            <el-col :span="24">
              <el-form-item label="提成方式" prop="percent">
                <el-select disabled v-model="percent" style="width: 25%">
                  <el-option key="percent" value="percent" label="按比率提成（%）"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="散客比例" prop="visitorVal">
                  <el-input v-model="visitorVal" placeholder="请输入散客比例值" maxlength="90" style="width: 25%"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="会员比例" prop="memberVal">
                <el-input v-model="memberVal" placeholder="请输入会员比例值" maxlength="90" style="width: 25%"/>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-form>
      <div class="item-config" v-if="form.type != 'recharge'">
        <div class="rule-item-op">
          <span class="label"><span class="icon">*</span>详细规则配置：</span>
          <el-button type="primary" size="mini" icon="el-icon-plus" plain @click="addRuleItem">添加项目</el-button>
        </div>
        <el-table :data="form.detailList" border size="medium" style="width: 100%">
          <el-table-column label="图片" align="center" width="136">
            <template slot-scope="scope">
              <img class="list-img" style="width: 45px;height: 35px;" :src="scope.row.logo">
            </template>
          </el-table-column>
          <el-table-column prop="goodsName" label="商品名称" min-width="136" :show-overflow-tooltip="true"></el-table-column>
          <el-table-column label="标准价(原价)">
            <template slot-scope="scope">
              ￥ {{scope.row.price}} 元
            </template>
          </el-table-column>
          <el-table-column prop="type" label="提成类型" :formatter="dataFormat"></el-table-column>
          <el-table-column prop="method" label="提成方式" width="136">
            <template slot-scope="scope">
              <select :rowId="scope.row.goodsId" tp="method" @change="selectChange(scope.row.goodsId,'method')" style="border: 1px solid #dbdee5;">
                <option :selected="scope.row.method=='percent'?true:false" value="percent">按比率提成（%）</option>
                <option :selected="scope.row.method=='fixed'?true:false" value="fixed">按固定提成（元）</option>
              </select>
            </template>
          </el-table-column>
          <el-table-column prop="visitorVal" label="散客" width="99">
            <template slot-scope="scope">
              <el-input v-model="scope.row.visitorVal" onkeyup="value=value.replace(/[^\d.]/g, '')"/>
            </template>
          </el-table-column>
          <el-table-column label="会员" width="99">
            <template slot-scope="scope">
              <el-input v-model="scope.row.memberVal" onkeyup="value=value.replace(/[^\d.]/g, '')"/>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="77">
            <template slot-scope="scope">
              <el-button size="mini" @click="removeRule(scope.row.goodsId)" slot="reference" type="text">删除<i class="el-icon-delete"></i>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确定</el-button>
        <el-button @click="open=false">取消</el-button>
      </div>
    </el-dialog>

    <!--商品服务选择列表-->
    <el-dialog title="商品选择" :visible.sync="openSelect" class="common-dialog" width="700px" append-to-body>
      <el-table
        :data="selectGoodList"
        border
        size="medium"
        ref="multipleTable"
        @selection-change="handleSelectionChange"
        :row-key="getRowKeys"
        style="width: 100%">
        <el-table-column
          type="selection"
          width="55"
          :reserve-selection="true"
        >
        </el-table-column>
        <el-table-column label="图片" align="center" width="156">
          <template slot-scope="scope">
            <img class="list-img" style="width: 45px;height: 35px;" :src="scope.row.logo">
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称"></el-table-column>
        <el-table-column label="标准价(原价)" width="100">
          <template slot-scope="scope">
            ￥{{scope.row.price}} 元
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="totalGoods>0"
        :total="totalGoods"
        :page.sync="params.page"
        :limit.sync="params.pageSize"
        @pagination="getGoodsList"
      />
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="selectOk">确定</el-button>
        <el-button @click="openSelect=false">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
  import { getCommissionRuleList, removeCommissionRule, getCommissionRuleInfo, saveCommissionRule } from "@/api/commission/rule";
  import { getGoodsList } from "@/api/goods";
  import { searchStore } from "@/api/store";
  export default {
    name: 'ruleIndex',
    data() {
      return {
        loading: false,
        multipleSelection: [],
        uploadAction: process.env.VUE_APP_SERVER_URL + '/backendApi/file/upload',
        page: 1, //初始页
        pageSize: 10, //每页的数据
        total: 0,
        list: [],
        selectGoodList: [],
        totalGoods: 0,
        storeOptions: [],
        name: '',
        type: '',
        target: '',
        title: '',
        open: false,
        openSelect: false,
        visitorVal: '',
        memberVal: '',
        percent: 'percent',
        params:  {
          page: 1,
          pageSize: 10,
          type: '',
          keyword: ''
        },
        form: {
          id: '',
          storeIdList: [],
          name: '',
          target: 'staff',
          type: 'goods',
          createTime: '',
          updateTime: '',
          status: '',
          detailList: []
        },
        rules: {
          storeIdList: [{required: true, message: '请选择方案适用门店', trigger: 'blur'}],
          name: [{required: true, message: '请输入方案名称', trigger: 'blur'}],
          target: [{required: true, message: '请选择方案对象', trigger: 'blur'}],
          type: [{required: true, message: '请选择方案类型', trigger: 'blur'}]
        }
      }
    },
    methods: {
      // 获取方案列表
      getCommissionRuleList() {
        let params = {
          page: this.page,
          pageSize: this.pageSize,
          name: this.name,
          target: this.target,
          type: this.type
        };
        getCommissionRuleList(params).then(response => {
            this.list = response.data.paginationResponse.content;
            this.total = response.data.paginationResponse.totalElements;
          }
        );
      },
      // 获取商品列表
      getGoodsList() {
        getGoodsList(this.params).then(response => {
            this.selectGoodList = response.data.paginationResponse.content;
            this.totalGoods = response.data.paginationResponse.totalElements;
            //数据回显
            this.selectGoodList.forEach(item => {
              this.form.detailList.forEach(item2 => {
                if (item2.goodsId == item.id) {
                    // 满足条件选中
                    this.$refs.multipleTable.toggleRowSelection(item);
                }
              })
            })
          }
        );
      },
      // 修改方案类型
      changeType() {
         this.params.type = this.form.type;
         this.form.detailList = [];
      },
      dataFormat: function (row, column) {
        // goods：商品提成；coupon=卡券提成；recharge=充值提成
        let val = row[column.property];
        if (val == 'goods') {return '商品提成'}
        if (val == 'coupon') {return '卡券提成'}
        if (val == 'recharge') {return '充值提成'}
        return val;
      },
      dataFormatTarget: function (row, column) {
        // staff：员工提成；member=会员分销
        let val = row[column.property];
        if (val == 'staff') {return '员工提成'}
        if (val == 'member') {return '会员分销'}
        return val;
      },
      // 获取店铺列表
      getStoreList() {
        searchStore().then(response => {
            this.storeOptions = response.data.storeList;
          }
        )
      },
      // 查询方案
      handleQuery() {
        this.getCommissionRuleList();
      },
      // 新增方案
      handleAdd(id) {
        this.title = "新增方案"
        this.form.id = '';
        this.form.storeIdList = [];
        this.form.detailList = [];
        this.form.name = '';
        this.form.type = 'goods';
        this.form.createTime = '';
        this.form.updateTime = '';
        this.form.status = '';
        this.open = true;
        if (id) {
          this.title = "修改方案"
          getCommissionRuleInfo(id).then(r => {
              this.form = r.data.commissionRule;
            }
          );
        }
      },
      // 提交按钮
      submitForm: function () {
        this.$refs["form"].validate(valid => {
          if (valid) {
            if (!this.form.id) {
               saveCommissionRule(this.form).then(r => {
                  this.$modal.msgSuccess("修改成功");
                  this.open = false;
                  this.handleQuery();
               });
            } else {
              saveCommissionRule(this.form).then(r => {
                  this.$modal.msgSuccess("新增成功");
                  this.open = false;
                  this.handleQuery();
              });
            }
          }
        });
      },
      // 删除方案
      handleDelete(row) {
        this.$confirm('确认删除该方案吗?', '提示信息', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let id = row.id;
          removeCommissionRule({ id: id, status: 'D' }).then(r => {
              if (r.code == 200) {
                this.$message({ // 成功
                  type: 'success',
                  message: '操作成功'
                })
                this.getCommissionRuleList();
              }
            }
          )
        }).catch(() => {
        });
      },
      // 添加提成方案详情
      addRuleItem() {
        if (!this.form.type) {
            this.$modal.msgWarning("请先选择提成方案类别");
            return;
        }
        this.openSelect = true;
        this.multipleSelection = [];//清空已选择的列表
        // 清空所有选中
        this.$nextTick(() => {
          this.$refs.multipleTable.clearSelection();
        })
        this.getGoodsList();
      },
      // 删除提成方案商品服务
      removeRule(goodId){
        this.form.detailList = this.form.detailList.filter(item => {
            return item.goodsId != goodId;
        })
      },
      getRowKeys(row) {
        return row.id;
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
      },
      // 确定选择的商品服务
      selectOk() {
        let selectList = [];
        // 1.转换已经选择的商品
        this.multipleSelection.forEach(item => {
            let detailObj = {
              goodsId: item.id,//商品ID
              priceType: "1",//价格类型 1：标准价(原价) 2：会员价
              method: "percent",//提成方式 1：比率提成(按100的比率) 2：固定提成
              visitorVal: 0.00,//散客（提成值）
              memberVal: 0.00,//会员（提成值）
              type: item.type,//提成类型
              goodsName: item.name,//商品名称
              price: item.price,//商品价格
              logo: item.logo//商品图片
            }
            // 2.比对现有已选择的商品
            this.form.detailList.forEach(item2 => {
              if (item2.goodsId == item.id) {
                  detailObj = item2;
              }
            })
            selectList.push(detailObj);
        })
        this.form.detailList = selectList;
        this.openSelect = false;
      }
    },
    created: function () {
      this.getCommissionRuleList();
      this.getStoreList();
    }
  }
</script>
<style scoped lang="scss">
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
    background-color: #ffffff;
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
  .rule-item-op {
    margin-bottom: 20px;
    .label {
      size: 26px;
      font-weight: bold;
      margin-left: 20px;
      .icon {
        color: red;
        margin-right: 5px;
      }
    }
  }
</style>
