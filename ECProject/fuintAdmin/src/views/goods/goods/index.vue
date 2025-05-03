<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" class="main-search" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="商品名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入商品名称"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所属店铺" prop="store">
        <el-select
          v-model="queryParams.storeId"
          placeholder="所属店铺"
          clearable
          style="width: 180px"
        >
          <el-option :key="0" label="公共商品" v-if="!storeId" :value="0"/>
          <el-option v-for="storeInfo in storeOptions" :key="storeInfo.id" :label="storeInfo.name" :value="storeInfo.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="商品条码" prop="goodsNo">
        <el-input
          v-model="queryParams.goodsNo"
          placeholder="请输入商品条码"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="商品类型" prop="cateId">
        <el-select class="input" v-model="queryParams.type" clearable placeholder="请选择商品类型">
          <el-option
            v-for="item in typeOptions"
            :key="item.key"
            :label="item.name"
            :value="item.key"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="规格类型" prop="isSingleSpec">
        <el-select
          v-model="queryParams.isSingleSpec"
          placeholder="规格类型"
          clearable
          style="width: 120px"
        >
          <el-option key="Y" label="单规格" value="Y"/>
          <el-option key="N" label="多规格" value="N"/>
        </el-select>
      </el-form-item>
      <el-form-item label="上架状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="上架状态"
          clearable
          style="width: 120px"
        >
          <el-option key="A" label="上架" value="A"/>
          <el-option key="N" label="下架" value="N"/>
        </el-select>
      </el-form-item>
      <el-form-item label="库存状态" prop="stock">
        <el-select
          v-model="queryParams.stock"
          placeholder="库存状态"
          clearable
          style="width: 120px"
        >
          <el-option key="Y" label="有库存" value="Y"/>
          <el-option key="N" label="无库存" value="N"/>
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
          v-hasPermi="['goods:goods:add']"
        >新增</el-button>
      </el-form-item>
    </el-form>

    <el-table ref="tables" v-loading="loading" :data="list" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column label="ID" prop="id" width="50"/>
      <el-table-column label="所属店铺" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.storeInfo">{{ scope.row.storeInfo.name }}</span>
          <span v-else>公共所有</span>
        </template>
      </el-table-column>
      <el-table-column label="商品名称" align="center" min-width="300" prop="name" />
      <el-table-column label="主图" align="center" width="100">
        <template slot-scope="scope">
           <img class="list-img" :src="scope.row.logo">
        </template>
      </el-table-column>
      <el-table-column label="商品条码" align="center" prop="goodsNo" width="140"/>
      <el-table-column label="剩余库存" align="center" prop="stock" width="100"/>
      <el-table-column label="所属分类" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.cateInfo.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="价格" align="center" width="80">
        <template slot-scope="scope">
          <span v-if="scope.row.price">{{ scope.row.price.toFixed(2) }}</span>
          <span v-else>0.00</span>
        </template>
      </el-table-column>
      <el-table-column label="上架状态" align="center" prop="status">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="A"
            inactive-value="N"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" width="150" prop="createTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" width="150" prop="updateTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="120" fixed='right'>
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            v-hasPermi="['goods:goods:edit']"
            @click="handleUpdate(scope.row)"
            v-if="storeId == scope.row.storeId || storeId == 0"
          >编辑</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            v-hasPermi="['goods:goods:edit']"
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
  </div>
</template>

<script>
import { getGoodsList, updateGoodsStatus } from "@/api/goods";
export default {
  name: "GoodsIndex",
  data() {
    return {
      storeId: this.$store.getters.storeId,
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
      // 店铺列表
      storeOptions: [],
      // 商品类型
      typeOptions: [],
      // 是否显示弹出层
      open: false,
      // 默认排序
      defaultSort: {prop: 'sort', order: 'descending'},
      // 表单参数
      form: { id: '', name: '', logo: '', sort: 0, status: "A" },
      // 上传地址
      uploadAction: process.env.VUE_APP_SERVER_URL + 'backendApi/file/upload',
      // 隐藏上传
      hideUpload: false,
      // 上传文件列表
      uploadFiles: [],
      // 查询参数
      queryParams: {
        page: 1,
        pageSize: 10,
        storeId: '',
        name: '',
        isSingleSpec: '',
        goodsNo: '',
        stock: '',
        status: ''
      },
      // 表单校验
      rules: {
        name: [
          { required: true, message: "名称不能为空", trigger: "blur" },
          { min: 2, max: 200, message: '名称长度必须介于2 和 200 之间', trigger: 'blur' }
        ],
        logo: [{ required: true, message: "请上传图片", trigger: "blur" }]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // 查询商品列表
    getList() {
      this.loading = true;
      getGoodsList(this.queryParams).then( response => {
          this.list = response.data.paginationResponse.content;
          this.total = response.data.paginationResponse.totalElements;
          this.typeOptions = response.data.typeList;
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
      let text = row.status == "A" ? "上架" : "下架";
      this.$modal.confirm('确认要' + text + '"' + row.name + '"吗？').then(function() {
        return updateGoodsStatus(row.id, row.status);
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
    // 新增按钮操作
    handleAdd() {
      this.$router.push( { path: '/goods/goods/add' } )
    },
    // 修改按钮操作
    handleUpdate(row) {
      this.$router.push( { path: '/goods/goods/edit?goodsId=' + row.id } )
    },
    // 删除按钮操作
    handleDelete(row) {
      const name = row.name
      this.$modal.confirm('是否确认删除商品"' + name + '"？').then(function() {
        return updateGoodsStatus(row.id, 'D');
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleUploadSuccess (file) {
      this.form.logo = file.data.fileName
    },
    handleRemove (file, fileList) {
      setTimeout(() => {
        this.hideUpload = fileList.length > 0
      }, 520)
    }
  }
};
</script>

