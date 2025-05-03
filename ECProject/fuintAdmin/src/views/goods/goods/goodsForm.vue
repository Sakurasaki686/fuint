<template>
  <div class="app-container">
    <div class="main-panel">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="基础信息" name="base">
          <div class="content">
            <el-form ref="baseForm" :model="baseForm" :rules="baseRules" label-width="120px">
              <el-row>
                <el-col :span="24">
                  <el-form-item label="商品类型" prop="cateId">
                    <el-select class="input" v-model="baseForm.type" placeholder="请选择商品类型">
                      <el-option
                        v-for="item in typeOptions"
                        :key="item.key"
                        :label="item.name"
                        :value="item.key"
                      ></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="24">
                  <el-form-item label="商品名称" prop="name">
                    <el-input class="input" v-model="baseForm.name" placeholder="请输入商品名称" maxlength="200" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="24">
                  <el-form-item label="商品条码" prop="goodsNo">
                    <el-input class="input" v-model="baseForm.goodsNo" placeholder="请输入商品条码，或使用扫码枪扫描" maxlength="50"/>
                    <div class="create-sn" @click="createGoodsSn()">随机生成条码</div>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="24">
                  <el-form-item label="商品分类" prop="cateId">
                    <el-select class="input" v-model="baseForm.cateId" placeholder="请选择商品分类">
                      <el-option
                        v-for="item in cateOptions"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id"
                        :disabled="item.status !== 'A'"
                      ></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="24">
                  <el-form-item label="所属店铺" prop="storeId">
                    <el-select class="input" v-model="baseForm.storeId" clearable placeholder="请选择所属店铺">
                      <el-option :key="0" label="公共商品" v-if="storeId == 0" :value="0"/>
                      <el-option
                        v-for="item in storeOptions"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id"
                        :disabled="item.status !== 'A'"
                      ></el-option>
                    </el-select>
                    <div class="form-tips">提示：未选择则属于公共商品</div>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="24">
                  <el-form-item label="商品图片" prop="images">
                    <el-upload class="form__head-icon-upload"
                               :action="uploadAction"
                               list-type="picture-card"
                               :file-list="uploadFiles"
                               :limit="10"
                               :auto-upload="true"
                               :headers="uploadHeader"
                               :on-success="handleUploadSuccess"
                               :on-remove="handleRemove">
                      <i class="el-icon-plus"></i>
                    </el-upload>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="24">
                  <el-form-item label="显示排序" prop="sort">
                    <el-input-number v-model="baseForm.sort" :min="0" />
                    <div class="form-tips">提示：数值越小，排行越靠前</div>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="24">
                  <el-form-item label="商品状态">
                    <el-radio-group v-model="baseForm.status">
                      <el-radio key="A" label="A" value="A">上架</el-radio>
                      <el-radio key="N" label="N" value="N">下架</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>
        </el-tab-pane>
        <el-tab-pane label="扩展信息" name="extend">
          <div class="content">
            <el-form ref="extendForm" :model="extendForm" :rules="extendRules" label-width="120px">
              <el-row>
                <el-col :span="24">
                  <el-form-item label="积分抵扣" prop="canUsePoint">
                    <el-radio-group v-model="extendForm.canUsePoint">
                      <el-radio key="Y" label="Y" value="Y">可用</el-radio>
                      <el-radio key="N" label="N" value="N">不可用</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="24">
                  <el-form-item label="会员折扣" prop="isMemberDiscount">
                    <el-radio-group v-model="extendForm.isMemberDiscount">
                      <el-radio key="Y" label="Y" value="Y">有折扣</el-radio>
                      <el-radio key="N" label="N" value="N">无折扣</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="24">
                  <el-form-item label="规格类型" prop="isSingleSpec">
                    <el-radio-group v-model="extendForm.isSingleSpec">
                      <el-radio key="Y" label="Y" value="Y">单规格</el-radio>
                      <el-radio key="N" label="N" value="N">多规格</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row v-if="extendForm.isSingleSpec == 'N'">
                <el-col :span="24">
                  <el-form-item label="商品规格" prop="goodsSpec">
                     <Sku ref="Sku" :skuData="skuData" :goodsId="baseForm.goodsId" :uploadDomain="uploadDomain" @skuChange="skuChange"/>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row v-if="baseForm.type == 'service'">
                <el-col :span="24">
                  <el-form-item label="服务时长" prop="serviceTime">
                    <el-input v-model="extendForm.serviceTime" class="min-input" placeholder="请输入服务时长，单位：分钟" maxlength="50"/>
                    <div class="form-tips">提示：输入数字，单位：分钟</div>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row v-if="baseForm.type == 'coupon'">
                <el-col :span="24">
                  <el-form-item label="卡券ID" prop="couponIds">
                    <el-input v-model="extendForm.couponIds" class="input" rows="2" type="textarea" placeholder="请输入购买的卡券ID，英文逗号分隔，如：19932,938423,324234" maxlength="1000"/>
                    <div class="form-tips">提示：购买的卡券ID，英文逗号分隔</div>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row v-if="extendForm.isSingleSpec == 'Y'">
                <el-col :span="24">
                  <el-form-item label="库存数量" prop="stock">
                    <el-input v-model="extendForm.stock" class="min-input" placeholder="请输入库存数量" maxlength="50"/>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row v-if="extendForm.isSingleSpec == 'Y'">
                <el-col :span="24">
                  <el-form-item label="商品价格" prop="price">
                    <el-input v-model="extendForm.price" class="min-input" placeholder="请输入商品价格" maxlength="50"/>
                    <div class="form-tips">单位：元</div>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row v-if="extendForm.isSingleSpec == 'Y'">
                <el-col :span="24">
                  <el-form-item label="划线价格" prop="linePrice">
                    <el-input v-model="extendForm.linePrice" class="min-input" placeholder="请输入商品划线，空则不显示" maxlength="50" />
                    <div class="form-tips">单位：元</div>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="24">
                  <el-form-item label="初始销量" prop="initSale">
                    <el-input v-model="extendForm.initSale" class="min-input" placeholder="请输入初始销量" maxlength="10"/>
                    <div class="form-tips">提示：输入数字，虚拟销量</div>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row v-if="extendForm.isSingleSpec == 'Y' && baseForm.type == 'product'">
                <el-col :span="24">
                  <el-form-item label="商品重量" prop="weight">
                    <el-input v-model="extendForm.weight" class="min-input" placeholder="请输入商品重量" maxlength="10"/>
                    <div class="form-tips">提示：输入数字，单位kg</div>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="24">
                  <el-form-item label="商品卖点" prop="salePoint">
                    <el-input class="input" v-model="extendForm.salePoint" placeholder="请输入商品卖点，几个字总结" maxlength="50"/>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>
        </el-tab-pane>
        <el-tab-pane label="商品介绍" name="detail">
          <div class="content" style="width: 375px;margin-left: 80px;">
            <el-form ref="detailForm" :model="detailForm" :rules="detailRules" label-width="120px">
               <editor v-model="detailForm.description" :min-height="550"/>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
      <div slot="footer" class="footer">
         <el-button type="primary" @click="submitForm">确定</el-button>
         <el-button @click="cancel">取消</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'
import { saveGoods, getGoodsInfo } from "@/api/goods";
import Sku from '../components/Sku';
export default {
  name: "GoodsForm",
  components: { Sku },
  data() {
    return {
      storeId: this.$store.getters.storeId,
      // 遮罩层
      loading: false,
      activeTab: 'base',
      storeOptions: [],
      cateOptions: [],
      typeOptions: [],
      skuData: { attrList: [], skuList: [], initSkuList: [] },
      // 基础信息表单
      baseForm: { type: 'goods', goodsId: '', name: '', storeId: this.$store.getters.storeId, cateId: '', goodsNo: '', images: [], status: "A", sort: 0 },
      // 扩展信息表单
      extendForm: { goodsId: '', canUsePoint: 'Y', isMemberDiscount: 'Y', isSingleSpec: 'Y', serviceTime: 0, couponIds: '', stock: '', price: '', linePrice: '', salePoint: '', initSale: '', weight: '', skuData: null },
      // 详情信息表单
      detailForm: { goodsId: '', description : '' },
      // 上传地址
      uploadAction: process.env.VUE_APP_SERVER_URL + 'backendApi/file/upload',
      uploadHeader: { 'Access-Token' : getToken() },
      // 上传域名
      uploadDomain: '',
      // 上传文件列表
      uploadFiles: [],
      // 基础信息表单校验
      baseRules: {
        name: [
          { required: true, message: "商品名称不能为空", trigger: "blur" },
          { min: 2, max: 30, message: '商品名称长度必须介于2和200 之间', trigger: 'blur' }
        ],
        goodsNo: [
          { required: true, message: "商品条码不能为空", trigger: "blur" },
          { min: 2, max: 100, message: '商品条码长度必须介于2和100之间', trigger: 'blur' }
        ],
        cateId: [
          { required: true, message: "请选择商品分类", trigger: "blur" }
        ],
        images: [
          { required: true, message: "请上传商品图片", trigger: "blur" }
        ],
      },
      // 扩展信息表单校验
      extendRules: {
        canUsePoint: [
          { required: true, message: "请选择", trigger: "blur" }
        ],
        isMemberDiscount: [
          { required: true, message: "请选择", trigger: "blur" }
        ],
        isSingleSpec: [
          { required: true, message: "请选择", trigger: "blur" }
        ],
        price: [
          { required: true, message: "请输入商品价格", trigger: "blur" }, { pattern: /(^[1-9]\d*(\.\d{1,2})?$)|(^0(\.\d{1,2})?$)/, message: `价格必须大于0`, trigger: 'blur' }
        ],
        stock: [
          { required: true, message: "请输入库存数量", trigger: "blur" }, { pattern: /^[0-9]{1,10}$/, message: `库存数量必须是正整数`, trigger: 'blur' }
        ],
        initSale: [
          { pattern: /^[0-9]{1,10}$/, message: `初始销量必须是正整数`, trigger: 'blur' }
        ],
        weight: [
          { pattern: /(^[1-9]\d*(\.\d{1,2})?$)|(^0(\.\d{1,2})?$)/, message: `重量必须大于等于0`, trigger: 'blur' }
        ],
      },
      detailRules: {
        description: [
          { required: true, message: "请输入商品详情", trigger: "blur" }
        ],
      }
    };
  },
  created() {
    const goodsId = this.$route.query.goodsId ? this.$route.query.goodsId : '0'
    this.getGoodsInfo(goodsId);
  },
  methods: {
    handleTabClick() {
       // empty
    },
    getGoodsInfo(goodsId) {
      const app = this;
      getGoodsInfo(goodsId).then(response => {
          const goodsInfo = response.data.goods;
          const imagePath = response.data.imagePath;
          this.uploadDomain = imagePath;
          if (goodsInfo) {
              // 商品基础信息
              this.baseForm.goodsId = goodsInfo.id+'';
              this.baseForm.type = goodsInfo.type;
              this.baseForm.name = goodsInfo.name;
              this.baseForm.goodsNo = goodsInfo.goodsNo;
              this.baseForm.cateId = goodsInfo.cateId;
              this.baseForm.storeId = goodsInfo.storeId;
              this.baseForm.sort = goodsInfo.sort;
              this.baseForm.status = goodsInfo.status;
              // 商品图片
              this.baseForm.images = response.data.images;
              const images = this.baseForm.images;
              app.uploadFiles = [];
              if (images && images.length > 0) {
                  images.forEach(function(url){
                     app.uploadFiles.push({ url: imagePath + url })
                  })
              }

              // 扩展信息
              this.extendForm.goodsId = goodsInfo.id;
              this.extendForm.canUsePoint = goodsInfo.canUsePoint == null ? 'Y' : goodsInfo.canUsePoint;
              this.extendForm.isMemberDiscount = goodsInfo.isMemberDiscount == null ? 'Y' : goodsInfo.isMemberDiscount;
              this.extendForm.isSingleSpec = goodsInfo.isSingleSpec == null ? 'Y' : goodsInfo.isSingleSpec;
              this.extendForm.stock = goodsInfo.stock;
              this.extendForm.price = goodsInfo.price;
              this.extendForm.linePrice = goodsInfo.linePrice;
              this.extendForm.initSale = goodsInfo.initSale;
              this.extendForm.salePoint = goodsInfo.salePoint;
              this.extendForm.weight = goodsInfo.weight;
              this.extendForm.serviceTime = goodsInfo.serviceTime;
              this.extendForm.couponIds = goodsInfo.couponIds;

              // 多规格
              this.skuData.attrList = response.data.specData;
              this.skuData.skuList = response.data.skuData;
              this.skuData.initSkuList = response.data.skuData;

              // 商品详情
              this.detailForm.goodsId = goodsInfo.id;
              this.detailForm.description = goodsInfo.description;
          }
          this.cateOptions = response.data.cateList;
          this.storeOptions = response.data.storeList;
          this.typeOptions = response.data.typeList;
      });
    },
    cancel() {
      this.$store.dispatch('tagsView/delView', this.$route)
      this.$router.push( { path: '/goods/goods/index' } );
    },
    skuChange(skuData) {
       // empty
    },
    // 提交按钮
    submitForm: function() {
      const app = this;
      if (app.activeTab == 'base') {
          app.$refs["baseForm"].validate(valid => {
            if (valid) {
                saveGoods(app.baseForm).then(response => {
                   app.$modal.msgSuccess("保存成功！");
                   app.getGoodsInfo(response.data.goodsInfo.id);
                });
            }
          });
      } else if (app.activeTab == 'extend') {
          if (!app.extendForm.goodsId) {
              app.$modal.msgError("请先提交商品基础信息");
              app.activeTab = 'base';
              return false;
          }
          // 多规格商品验证
          if (app.skuData.skuList && app.skuData.skuList.length > 0) {
              let isValid0 = true;
              let isValid1 = true;
              let isValid2 = true;
              app.skuData.skuList.forEach(function(item) {
                 if (!item.skuNo || item.skuNo.length < 1 ) {
                     isValid0 = false;
                 }
                 if (item.stock < 0) {
                     isValid1 = false;
                 }
                 if (item.price < 0) {
                     isValid2 = false;
                 }
              })
              if (!isValid1) {
                  app.$modal.alert("商品sku编码长度需大于1，请仔细核对！");
                  return false;
              }
              if (!isValid1) {
                  app.$modal.alert("商品库存须大于等于0，请仔细核对！");
                  return false;
              }
              if (!isValid2) {
                  app.$modal.alert("商品价格须大于等于0，请仔细核对！");
                  return false;
              }
          }

          app.extendForm.skuData = app.skuData.skuList;
          app.extendForm.specData = app.skuData.attrList;
          app.$refs["extendForm"].validate(valid => {
            if (valid) {
                saveGoods(app.extendForm).then(response => {
                    app.$modal.msgSuccess("保存成功！");
                    app.getGoodsInfo(response.data.goodsInfo.id);
                });
            }
          });
      } else {
          if (!app.detailForm.goodsId) {
              app.$modal.msgError("请先提交商品基础信息");
              app.activeTab = 'base';
              return false;
          }
          app.$refs["detailForm"].validate(valid => {
              if (valid) {
                  saveGoods(app.detailForm).then(response => {
                      app.$modal.msgSuccess("保存成功！");
                      app.getGoodsInfo(response.data.goodsInfo.id);
                  });
              }
          });
      }
    },
    // 文件上传成功
    handleUploadSuccess(file) {
      this.baseForm.images.push(file.data.fileName)
    },
    // 文件删除处理
    handleRemove(file) {
      const newImages = [];
      if (this.baseForm.images && this.baseForm.images.length > 0) {
          this.baseForm.images.forEach(function(item){
              if (file.url.indexOf(item) == -1) {
                  newImages.push(item);
              }
          })
      }
      this.baseForm.images = newImages;
    },
    // 生成随机条码
    createGoodsSn() {
      let sn = (Math.random() + 1) * 100000000000000;
      this.baseForm.goodsNo = sn.toFixed(0);
    }
  }
};
</script>
<style rel="stylesheet/scss" lang="scss">
   .main-panel {
      padding-top: 5px;
      .content {
          margin-top: 30px;
          margin-left: 20px;
      }
     .footer {
        margin-top: 20px;
     }
     .create-sn {
        font-size: 12px;
        color: blue;
        cursor: pointer;
        width: 100px;
     }
   }
</style>
