<template>
  <div class="sku-list">
    <template v-if="!disabled">
      <div class="sku-list-head">
        <el-button type="primary" size="mini" @click="addSkuRow">添加规格</el-button
        >
      </div>
      <div
        class="sku-list-item"
        v-for="(item, index) in skuData.attrList"
        :key="index"
      >
        <div class="sku-list-item-main">
          <div class="sku-list-item__layout">
            <span class="span">规格名</span>
            <el-input
              size="small"
              v-model="item.name"
              class="input"
            ></el-input>
          </div>
          <div class="sku-list-item__layout">
            <span class="span">规格值</span>
            <div class="sku-list-item-tags">
              <el-tag
                class="sku-list-item-tag"
                closable
                v-for="(subItem, i) in item.child"
                v-if="subItem.name"
                @close="removeSkuAttr(index, i)"
                :key="i"
              >{{ subItem.name }}</el-tag
              >
              <el-button
                size="small"
                icon="el-icon-plus"
                @click="addSkuAttr(index)"
              >添加</el-button
              >
            </div>
          </div>
        </div>
        <el-button
          type="text"
          size="small"
          class="sku-list-item-removeBtn"
          @click="removeSkuRow(index)"
        >删除规格</el-button
        >
      </div>
    </template>

    <div class="batch-setting">
      <span class="label">批量设置</span>
      <el-input size="small" v-model="batch.skuNo" placeholder="sku编码" class="input input-sn"></el-input>
      <span class="create-sn" @click="createGoodsSn()">随机生成</span>
      <el-input size="small" v-model="batch.price" placeholder="商品价格" class="input"></el-input>
      <el-input size="small" v-model="batch.linePrice" placeholder="划线价格" class="input"></el-input>
      <el-input size="small" v-model="batch.stock" placeholder="商品库存" class="input"></el-input>
      <el-input size="small" v-model="batch.weight" placeholder="商品重量" class="input"></el-input>
      <el-button size="small" type="danger" class="button" @click="batchSetSku()">确认设置</el-button>
    </div>

    <el-table border :data="skuData.skuList">
      <el-table-column label="序号" align="center" width="60">
        <template slot-scope="scope">
          <span>{{ scope.$index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="图片" align="center" width="120">
        <template slot-scope="scope">
          <el-upload
            :disabled="disabled"
            :action="uploadAction"
            list-type="picture-card"
            :auto-upload="true"
            :headers="uploadHeader"
            :file-list="[]"
            :limit="1"
            :on-success="function(file) {
               return onUploadImgSuccess(file, scope.$index) }"
            :show-file-list="false"
          >
            <img
              v-if="scope.row.logo"
              :src="uploadDomain + scope.row.logo"
              class="sku-logo"
            />
            <i v-if="!scope.row.logo" class="el-icon-plus"></i>
          </el-upload>
        </template>
      </el-table-column>
      <el-table-column label="规格" align="center">
        <template slot-scope="scope">
          <div class="spec-tag" v-for="spec in scope.row.specList"><span class="i">{{ spec.value }}</span></div>
        </template>
      </el-table-column>
      <el-table-column label="sku编码" align="center" width="160">
        <template slot-scope="scope">
          <el-input
            :readonly="disabled"
            v-model="scope.row.skuNo"
          ></el-input>
        </template>
      </el-table-column>
      <el-table-column label="销售价格(元)" align="center">
        <template slot-scope="scope">
          <el-input
            :readonly="disabled"
            v-model="scope.row.price"
          ></el-input>
        </template>
      </el-table-column>
      <el-table-column label="划线价格(元)" align="center">
        <template slot-scope="scope">
          <el-input
            :readonly="disabled"
            v-model="scope.row.linePrice"
          ></el-input>
        </template>
      </el-table-column>
      <el-table-column label="商品库存" align="center">
        <template slot-scope="scope">
          <el-input :readonly="disabled" v-model="scope.row.stock"></el-input>
        </template>
      </el-table-column>
      <el-table-column label="重量(千克)" align="center">
        <template slot-scope="scope">
          <el-input :readonly="disabled" v-model="scope.row.weight"></el-input>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth';
import { saveSpecName, saveSpecValue, deleteSpec, deleteSpecValue } from "@/api/goods";
export default {
  name: 'Sku',
  model: {
    prop: "skuData",
    event: "change",
  },
  props: {
    goodsId: {
      type: String,
      default: "",
    },
    uploadDomain: {
      type: String,
      default: "",
    },
    skuData: {
      type: Object,
      default: () => ({}),
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      // 上传地址
      uploadAction: process.env.VUE_APP_SERVER_URL + 'backendApi/file/upload',
      uploadHeader: { 'Access-Token' : getToken() },
      // 批量设置
      batch: { price: '', skuNo: '', linePrice: '', weight: '', stock: '' }
    };
  },
  watch: {
    "skuData.attrList": {
      handler() {
        if (!this.disabled) {
            this.$set(this.skuData, "skuList", this.getTable());
        }
      },
      deep: true,
      immediate: true,
    },
  },
  methods: {
    // 添加规格行
    addSkuRow(i) {
      this.$prompt("请输入规格名称", "添加规格", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        inputPattern: /\S+/,
        inputErrorMessage: "规格名称不能为空",
        closeOnClickModal: false,
      }).then(({ value }) => {
          saveSpecName({ goodsId: this.goodsId, name: value }).then(response => {
            this.skuData.attrList.push({
                id: response.data.id,
                name: value,
                child: [],
            });
            this.$emit("skuChange", this.skuData);
          })
      });
    },
    // 删除规格行
    removeSkuRow(i) {
      const app = this
      app.$modal.confirm('确认删除该规格吗？').then(function() {
          deleteSpec({ goodsId: app.goodsId, specName: app.skuData.attrList[i].name }).then(response => {
              app.skuData.attrList.splice(i, 1);
              app.$emit("skuChange", app.skuData);
          })
      });
    },
    // 删除规格属性值
    removeSkuAttr(a, b) {
      const app = this
      app.$modal.confirm('确认删除该规格值吗？').then(function() {
          deleteSpecValue({ id: app.skuData.attrList[a].child[b].id }).then(response => {
              app.skuData.attrList[a].child.splice(b, 1);
              app.$emit("skuChange", app.skuData);
          })
      });
    },
    // 添加规格属性值
    addSkuAttr(i) {
      this.$prompt("请输入规格值", "添加规格值", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        inputPattern: /\S+/,
        inputErrorMessage: "规格值不能为空",
        closeOnClickModal: false,
      }).then(({ value }) => {
          saveSpecValue({ goodsId: this.goodsId, specName: this.skuData.attrList[i].name, value: value }).then(response => {
              this.skuData.attrList[i].child.push({
                 id: response.data.id,
                 name: value,
                 value: value,
              });
              this.$emit("skuChange", this.skuData);
          })
      });
    },
    // 批量设置sku
    batchSetSku() {
      const app = this;
      app.skuData.skuList.forEach(function(skuInfo, index) {
         if (app.batch.skuNo) {
             skuInfo.skuNo = app.batch.skuNo + index;
         }
         if (app.batch.price) {
             if (app.batch.price > 0) {
                 skuInfo.price = app.batch.price;
             } else {
                 app.$modal.alert("商品价格须大于0！");
                 return false;
             }
         }
         if (app.batch.linePrice) {
             if (app.batch.linePrice >= 0) {
                 skuInfo.linePrice = app.batch.linePrice;
             } else {
                 app.$modal.alert("商品划线价格须大于等于0！");
                 return false;
             }
         }
         if (app.batch.weight) {
             if (app.batch.weight >= 0) {
                 skuInfo.weight = app.batch.weight;
             } else {
                 app.$modal.alert("商品重量须大于等于0！");
                 return false;
             }
         }
         if (app.batch.stock) {
             if (app.batch.stock >= 0) {
                 skuInfo.stock = app.batch.stock;
             } else {
                 app.$modal.alert("商品库存须大于等于0！");
                 return false;
             }
         }
      })
    },
    // 生成随机条码
    createGoodsSn() {
      let sn = (Math.random() + 1) * 10000000000000;
      this.batch.skuNo = sn.toFixed(0);
    },
    onUploadImgSuccess(file, index) {
      if (!file) {
          return;
      }
      this.uploadDomain = file.data.domain;
      this.skuData.skuList[index].logo = file.data.fileName
      this.$emit("skuChange", this.skuData);
    },
    getTable() {
      const table = [];
      const attrValueAry = [];
      const arr = [];
      const tmpSkuData = (this.skuData.attrList || []).filter(
        (d) => d.name != "" && d.child.length > 0
      );
      if (!tmpSkuData || tmpSkuData.length == 0) {
         return [];
      }

      tmpSkuData.forEach((item) => {
         attrValueAry.push(item.child);
      });

      function func(skuArr = [], i) {
        for (let j = 0; j < attrValueAry[i].length; j++) {
          if (i < attrValueAry.length - 1) {
            skuArr[i] = attrValueAry[i][j];
            func(skuArr, i + 1);
          } else {
            arr.push([...skuArr, attrValueAry[i][j]]);
          }
        }
      }
      func([], 0);

      console.log('arr = ', arr);

      arr.forEach((item) => {
        let specIds = "",
            specList = [],
            findItem,
            tableItem;
        item.forEach((d) => {
            specIds += specIds ? `-${d.id}` : `${d.id}`;
            specList.push( { id: d.id, name: d.name, value: d.name } )
        });

        console.log('specList = ', specList);

        findItem =
          this.skuData.initSkuList.find((item) => {
            return specIds.includes(item.specIds);
          }) || {};

        tableItem = Object.assign(
          {
            price: '',
            linePrice: '',
            skuNo: '',
            stock: '',
            logo: '',
            weight: '0',
            specList: specList
          },
          findItem,
          {
            specIds,
          }
        );
        tableItem.specList = specList;
        console.log('tableItem = ', tableItem)
        table.push(tableItem);
      });

      return table;
    },
  },
};
</script>

<style lang="scss" scoped>
.sku-list {
  &-head {
    margin-bottom: 10px;
  }
  &-item {
    display: flex;
    align-items: center;
    border: 1px solid #eee;
    border-radius: 5px;
    margin-bottom: 5px;
    padding: 10px;
    background: #f5f5f5;
    &-main {
      flex: 1;
    }
    &-removeBtn {
      margin-left: 20px;
      color: #f56c6c;
    }
    &__layout {
      display: flex;
      align-items: center;
      margin-bottom: 5px;
      &:last-child {
        margin-bottom: 0;
      }
      .input {
        width: 240px;
      }
      .span {
        font-size: 13px;
        font-weight: bold;
        margin-right: 10px;
        color: #666;
      }
    }
    &-tags {
      flex: 1;
    }
    &-tag {
      margin-bottom: 10px;
      margin-right: 10px;
    }
  }
}
.spec-tag {
     margin: 10px 0px 10px 0px;
    .i {
       background: #cceeee;
       color: #EE9A49;
       padding: 5px;
       border-radius: 12px;
    }
}
.sku-logo {
    height: 78px;
    width: 78px;
    border-radius: 6px;
}
.batch-setting {
    margin: 20px 0px 3px 0px;
   .input {
      width: 100px;
      margin-left: 10px;
   }
   .input-sn {
      width: 140px;
   }
   .button {
     margin-left: 5px;
   }
}
.create-sn {
  margin-left: 2px;
}
</style>
