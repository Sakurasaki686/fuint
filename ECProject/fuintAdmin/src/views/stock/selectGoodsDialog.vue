<template>
  <el-dialog class="common-dialog" title="选择商品" :visible="showDialog" @close="close" width="70%" destroy-on-close>
    <el-form :model="params" ref="queryForm" class="main-search" size="small" :inline="true" label-width="100px">
      <el-form-item label="商品关键字" prop="keyword">
        <el-input
          v-model="params.keyword"
          placeholder="请输入商品关键字"
          clearable
          style="width: 200px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查找商品</el-button>
      </el-form-item>
    </el-form>

    <el-table ref="goodsTables" v-loading="loading" border :data="goodsList">
      <el-table-column width="55" align="center" prop="checked" label="选择">
        <template slot-scope="scope">
          <el-checkbox v-model="scope.row.checked" @change="checkRow($event, scope.$index, scope.row)" ></el-checkbox>
        </template>
      </el-table-column>
      <el-table-column label="主图" align="center" width="100">
        <template slot-scope="scope">
          <img class="list-img" :src="imagePath + scope.row.logo">
        </template>
      </el-table-column>
      <el-table-column label="商品条码" width="180" prop="goodsNo"/>
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
      <el-table-column label="库存数量" align="center" width="120" prop="type">
        <template slot-scope="scope">
          <span>{{ scope.row.stock ? scope.row.stock : '0' }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="params.page"
      :limit.sync="params.pageSize"
      @pagination="getGoodsList"
    />

    <div class="clearfix"></div>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="doSave()">保存</el-button>
      <el-button @click="close()">取消</el-button>
    </div>
  </el-dialog>
</template>
<script>
import { selectGoodsList } from "@/api/goods";
export default {
  props: {
    showDialog: {
      type:[Boolean],
      default:()=>false
    },
    storeId: {
      type:[Number],
      default:()=>"0"
    },
    dataList: {
      type:[Array],
      default:()=> []
    }
  },
  watch: {
    showDialog(value) {
      if (value) {
          this.getGoodsList();
      }
    }
  },
  data(){
    return {
      params:  {
        page: 1,
        pageSize: 10,
        keyword: ''
      },
      selectData: [],
      loading: false,
      goodsList: [],
      // 图片根目录
      imagePath: "",
      // 总条数
      total: 0
    }
  },
  methods: {
    // 获取商品列表
    getGoodsList() {
      const app = this;
      app.params.storeId = app.storeId;
      selectGoodsList(this.params).then( response => {
        if (response.data) {
            app.goodsList = response.data.paginationResponse.content;
            app.goodsList.forEach(function(goods) {
                app.$set(goods, 'checked', false);
            })
            // 回显
            app.goodsList.forEach(function(item, key) {
                app.dataList.forEach(function(row) {
                    if (item.id == row.id && item.skuId == row.skuId) {
                        app.$set(app.goodsList[key], 'checked', true);
                    }
                })
            })
            app.total = response.data.paginationResponse.totalElements;
            app.imagePath = response.data.imagePath;
        }
      })
    },
    // 选择一行
    checkRow(checked, index, row) {
       const app = this;
       if (checked) {
           let isExist  = false;
           app.selectData.forEach(function(item) {
              if (item.id == row.id && item.skuId == row.skuId) {
                  isExist = true;
              }
           })
           if (!isExist) {
               app.selectData.push(row);
           }
       } else {
           // 删除
           const dataList = [];
           app.$set(app.goodsList[index], 'checked', false);
           app.goodsList.forEach(function(item) {
              if (item.checked) {
                  dataList.push(item);
              }
           })
           app.selectData = dataList;
       }
    },
    // 查询商品
    handleQuery() {
      this.getGoodsList();
    },
    close() {
      this.$emit('closeDialog');
    },
    doSave() {
      this.$emit('submit', this.selectData);
    }
  }
}
</script>
<style lang="scss" scoped>
.spec-item {
   display: block;
   float: left;
   margin-right: 5px;
}
.common-dialog >>> .el-upload--picture-card {
   width: 60px;
   height: 50px;
   line-height: 60px;
}
</style>
