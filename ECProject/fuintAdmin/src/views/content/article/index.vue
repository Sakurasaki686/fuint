<template>
  <div class="app-container">
    <el-form :model="queryParams" class="main-search" ref="queryForm" size="small" :inline="true" label-width="68px">
      <el-form-item label="文章标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入文章标题"
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
          <el-option key="A" label="启用" value="A"/>
          <el-option key="N" label="禁用" value="N"/>
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
          v-hasPermi="['content:article:add']"
        >新增</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="articleList" @selection-change="handleSelectionChange">
      <el-table-column label="ID" prop="id" width="60"/>
      <el-table-column label="图片" align="center" width="200">
        <template slot-scope="scope">
          <img class="list-img" :src="scope.row.image">
        </template>
      </el-table-column>
      <el-table-column
        label="文章标题"
        align="left"
        prop="title"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="点击数" prop="click" align="center" width="60"/>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="A"
            inactive-value="N"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="创建者" align="center" prop="operator" width="100" />
      <el-table-column label="创建时间" align="center" width="160" prop="createTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['content:article:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['content:article:edit']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改文章对话框 -->
    <el-dialog :title="title" :visible.sync="open" class="common-dialog" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
          <el-row>
            <el-col :span="24">
              <el-form-item label="标题" prop="title">
                <el-input v-model="form.title" placeholder="请输入文章标题" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="简介">
                <el-input v-model="form.brief" type="textarea" placeholder="请输入简介"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="9">
              <el-form-item label="图片" prop="image">
                <el-upload
                  :action="uploadAction"
                  list-type="picture-card"
                  :class="{hide:hideUpload}"
                  :file-list="uploadFiles"
                  :auto-upload="true"
                  :show-file-list="false"
                  :headers="uploadHeader"
                  :on-success="handleUploadSuccess">
                  <img
                    v-if="this.form.image"
                    :src="imagePath + this.form.image"
                    class="list-img"
                  />
                  <i v-if="!this.form.image" class="el-icon-plus"></i>
                </el-upload>
              </el-form-item>
            </el-col>
            <p class="form-tips">（提示：点击图片修改，建议尺寸：640 x 350）</p>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="所属店铺" prop="storeId">
                <el-select
                  v-model="form.storeId"
                  style="width: 260px"
                  placeholder="所属店铺，空则为公共所有"
                >
                  <el-option :key="0" label="公共所有" v-if="!this.$store.getters.storeId" :value="0"/>
                  <el-option v-for="storeInfo in storeList" :key="storeInfo.id" :label="storeInfo.name" :value="storeInfo.id"/>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="14">
              <el-form-item label="排序" prop="sort">
                <el-input v-model="form.sort" placeholder="请输入排序数字，越小越靠前" maxlength="10"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="内容">
                <editor v-model="form.description" :min-height="300"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="状态">
                <el-radio-group v-model="form.status">
                  <el-radio key="A" label="A" value="A">启用</el-radio>
                  <el-radio key="N" label="N" value="N">禁用</el-radio>
                </el-radio-group>
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
import { getArticleList, getArticle, updateStatus, saveArticle } from "@/api/article";
import { getToken } from '@/utils/auth';
export default {
  name: "Article",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 总条数
      total: 0,
      // 图片根目录
      imagePath: "",
      // 上传地址
      uploadAction: process.env.VUE_APP_SERVER_URL + 'backendApi/file/upload',
      // 隐藏上传
      hideUpload: false,
      // 上传文件列表
      uploadFiles: [],
      uploadHeader: { 'Access-Token' : getToken() },
      // 文章列表
      articleList: [],
      // 店铺列表
      storeList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: '',
        status: ''
      },
      // 表单参数
      form: {
        id: '',
        title: '',
        description: '',
        storeId: 0,
        image: '',
        brief: "",
        sort: 0,
        status: "A"
      },
      // 表单校验
      rules: {
        title: [
          { required: true, message: "文章标题不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // 查询文章列表
    getList() {
      this.loading = true;
      getArticleList(this.queryParams).then(response => {
        this.articleList = response.data.dataList.content;
        this.total = response.data.dataList.totalElements;
        this.imagePath = response.data.imagePath
        this.storeList = response.data.storeList;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: '',
        title: '',
        description: '',
        storeId: 0,
        image: '',
        brief: "",
        sort: 0,
        status: "A"
      };
      this.resetForm("form");
    },
    // 搜索按钮操作
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    // 重置按钮操作
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.articleId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    // 状态修改
    handleStatusChange(row) {
      let text = row.status == "A" ? "启用" : "禁用";
      this.$modal.confirm('确认要' + text + '"' + row.title + '"吗？').then(function() {
        return updateStatus(row.id, row.status);
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(function() {
        row.status = row.status === "N" ? "A" : "N";
      });
    },
    // 图片上传成功
    handleUploadSuccess(file) {
      this.form.image = file.data.fileName
    },
    // 新增按钮操作
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新增文章";
    },
    // 修改按钮操作
    handleUpdate(row) {
      this.reset();
      const articleId = row.id || this.ids
      getArticle(articleId).then(response => {
        this.form = response.data.articleInfo;
        this.open = true;
        this.title = "修改文章";
      });
    },
    // 提交按钮
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.image.length < 1) {
              this.form.image = '/static/defaultImage/none.png';
          }
          if (this.form.articleId != undefined) {
              saveArticle(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
          } else {
              saveArticle(this.form).then(response => {
                  this.$modal.msgSuccess("新增成功");
                  this.open = false;
                  this.getList();
              });
          }
        }
      });
    },
    // 删除按钮操作
    handleDelete(row) {
      const articleId = row.id;
      this.$modal.confirm('是否确认删除文章ID为"' + articleId + '"的数据项？').then(function() {
        return updateStatus(articleId, 'D');
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>
<style scoped>
.common-dialog >>> .el-upload--picture-card {
  width: 60px;
  height: 50px;
  line-height: 60px;
}
</style>
