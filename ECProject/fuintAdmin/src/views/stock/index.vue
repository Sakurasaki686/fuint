<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" class="main-search" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="说明备注" prop="description">
        <el-input
          v-model="queryParams.description"
          placeholder="请输入说明备注"
          clearable
          style="width: 180px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="操作类型" prop="type">
        <el-select
          v-model="queryParams.type"
          placeholder="操作类型"
          clearable
          style="width: 100px"
        >
          <el-option key="increase" label="入库" value="increase"/>
          <el-option key="reduce" label="出库" value="reduce"/>
        </el-select>
      </el-form-item>
      <el-form-item label="所属店铺" prop="store">
        <el-select
          v-model="queryParams.storeId"
          placeholder="所属店铺"
          clearable
          style="width: 180px"
        >
          <el-option :key="0" label="公共所有" v-if="!this.$store.getters.storeId" :value="0"/>
          <el-option v-for="storeInfo in storeOptions" :key="storeInfo.id" :label="storeInfo.name" :value="storeInfo.id"/>
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
          @click="handleAdd"
          v-hasPermi="['goods:goods:index']"
        >新增入库</el-button>
        <el-button
          type="common"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleReduce"
          v-hasPermi="['goods:goods:index']"
        >新增出库</el-button>
      </el-form-item>
    </el-form>

    <el-table ref="tables" v-loading="loading" :data="list" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column label="ID" prop="id" width="80"/>
      <el-table-column label="类型" align="center" width="80" prop="type">
        <template slot-scope="scope">
          <span v-if="scope.row.type == 'reduce'">出库</span>
          <span v-if="scope.row.type == 'increase'">入库</span>
        </template>
      </el-table-column>
      <el-table-column label="所属店铺" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.storeId && scope.row.storeId > 0">
              <span>{{ getName(storeOptions, scope.row.storeId) }}</span>
          </span>
          <span v-else>公共所有</span>
        </template>
      </el-table-column>
      <el-table-column label="说明备注" align="center" prop="description">
        <template slot-scope="scope">
           <span>{{ scope.row.description ? scope.row.description : '--' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            v-hasPermi="['goods:goods:index']"
            v-if="storeId == scope.row.storeId || storeId == 0"
            @click="handleDetail(scope.row)"
          >详情</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            v-hasPermi="['goods:goods:index']"
            v-if="storeId == scope.row.storeId || storeId == 0"
            @click="handleDelete(scope.row)"
          >删除</el-button>
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
    <!--编辑商品库存-->
    <el-dialog :title="title" :visible.sync="open" class="common-dialog" width="80%" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="所属店铺" prop="storeId">
              <el-select class="input" v-model="form.storeId" clearable placeholder="请选择所属店铺">
                <el-option :key="0" label="公共所有" v-if="storeId == 0" :value="0"/>
                <el-option
                  v-for="item in storeOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                  :disabled="item.status !== 'A'"
                ></el-option>
              </el-select>
              <div class="form-tips">提示：未选择则属于公共所有</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注信息">
              <el-input v-model="form.description" type="textarea" rows="3" placeholder="请输入内容"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div class="mb5">
         <el-button v-if="!isView" type="primary" size="mini" @click="selectGoods()">添加商品</el-button>
      </div>
      <el-table ref="goodsTables" v-loading="loading" border :data="goodsList">
        <el-table-column label="商品条码" prop="goodsNo"/>
        <el-table-column label="主图" align="center" width="100">
          <template slot-scope="scope">
            <img class="list-img" :src="imagePath + scope.row.logo">
          </template>
        </el-table-column>
        <el-table-column label="商品名称" align="center" prop="name">
          <template slot-scope="scope">
            <span>{{ scope.row.name ? scope.row.name : '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="商品规格" prop="spec">
          <template slot-scope="scope">
           <span v-if="scope.row.specList">
              <span class="spec-item" v-for="spec in scope.row.specList">{{ spec.value }}</span>
           </span>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column label="数量" align="center" prop="type">
          <template slot-scope="scope">
            <el-input-number :disabled="isView" style="width: 150px" v-model="scope.row.num" :min="0"/>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-delete"
              v-hasPermi="['goods:goods:index']"
              :disabled="isView"
              v-if="storeId == scope.row.storeId || storeId == 0"
              @click="deleteGoods(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" v-if="!isView" @click="submitForm">提交</el-button>
        <el-button @click="cancel">取消</el-button>
      </div>
    </el-dialog>
    <!-- 选择商品组件 start-->
    <selectGoodsDialog :show-dialog="openSelectGoodsDialog" :storeId="form.storeId" :dataList="goodsList" @submit="doSelectGoods" @closeDialog="closeSelectGoods"></selectGoodsDialog>
    <!-- 选择商品组件 end-->
  </div>
</template>

<script>
import { getStockList, saveStock, deleteStock, getStockInfo } from "@/api/stock";
import selectGoodsDialog from './selectGoodsDialog';
export default {
  name: "StockIndex",
  components: {
    selectGoodsDialog
  },
  data() {
    return {
      isView: false,
      openSelectGoodsDialog: false,
      storeId: this.$store.getters.storeId,
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
      // 图片根目录
      imagePath: "",
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      goodsList: [],
      // 店铺列表
      storeOptions: [],
      // 是否显示弹出层
      open: false,
      // 默认排序
      defaultSort: {prop: 'sort', order: 'descending'},
      // 表单参数
      form: { storeId: this.$store.getters.storeId, type: 'increase', id: '', description: '', status: "A", goodsList: [] },
      // 查询参数
      queryParams: {
        page: 1,
        pageSize: 10,
        description: '',
        type: ''
      },
      // 表单校验
      rules: {}
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // 查询分类列表
    getList() {
      this.loading = true;
      getStockList(this.queryParams).then( response => {
          this.list = response.data.paginationResponse.content;
          this.total = response.data.paginationResponse.totalElements;
          this.imagePath = response.data.imagePath;
          this.storeOptions = response.data.storeList;
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
      this.resetForm("queryForm");
      this.$refs.tables.sort(this.defaultSort.prop, this.defaultSort.order)
      this.handleQuery();
    },
    // 状态修改
    handleStatusChange(row) {
      let text = row.status == "A" ? "启用" : "禁用";
      this.$modal.confirm('确认要' + text + '"' + row.name + '"吗？').then(function() {
        return deleteStock(row.id, row.status);
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(function() {
        row.status = row.status === "N" ? "A" : "N";
      });
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
    // 新增入库按钮操作
    handleAdd() {
      this.reset();
      this.isView = false;
      this.open = true;
      this.form.type = 'increase';
      this.title = "新增入库";
    },
    // 新增出库按钮操作
    handleReduce() {
      this.reset();
      this.isView = false;
      this.open = true;
      this.form.type = 'reduce';
      this.title = "新增出库";
    },
    // 表单重置
    reset() {
      this.form = {
        id: "",
        storeId: 0,
        status: "A",
        description: "",
        goodsList: []
      };
      this.goodsList = [];
      this.resetForm("form");
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 选择商品
    selectGoods() {
      this.openSelectGoodsDialog = true;
    },
    closeSelectGoods() {
       this.openSelectGoodsDialog = false;
    },
    doSelectGoods(selectData) {
      const app = this;
      app.openSelectGoodsDialog = false;
      app.goodsList = selectData;
      app.goodsList.forEach(function(goods, key) {
         if (!goods.num) {
             app.$set(app.goodsList[key], 'num', 1);
         }
      })
    },
    // 删除商品操作
    deleteGoods(row) {
      const dataList = [];
      this.goodsList.forEach(function(item) {
         if (item.id != row.id || item.skuId != row.skuId) {
             dataList.push(item);
         }
      })
      this.goodsList = dataList;
    },
    // 提交按钮
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
            if (!this.goodsList || this.goodsList.length < 1) {
                this.$modal.alert("请先添加商品");
                return false;
            }
            this.form.goodsList = this.goodsList;
            saveStock(this.form).then(response => {
               this.$modal.msgSuccess("新增成功");
               this.open = false;
               this.getList();
               this.reset();
            });
        }
      });
    },
    // 详情按钮操作
    handleDetail(row) {
       this.reset();
       this.isView = true;
       const id = row.id;
        getStockInfo(id).then(response => {
            this.form = response.data.stockInfo;
            this.goodsList = response.data.goodsList;
            this.open = true;
            this.title = "记录详情";
        });
    },
    // 删除按钮操作
    handleDelete(row) {
      const name = row.id
      this.$modal.confirm('是否确认删除"' + name + '"的数据项？').then(function() {
         return deleteStock(row.id, 'D');
      }).then(() => {
         this.getList();
         this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>
<style lang="scss" scoped>
.common-dialog >>> .el-upload--picture-card {
  width: 60px;
  height: 50px;
  line-height: 60px;
}
</style>

