<template>
    <el-dialog class="common-dialog" title="挂单列表" :visible="showDialog" @close="close" width="1000px" destroy-on-close>
        <el-alert title="提示：请选择一个空白位置挂单" type="warning" :closable="false"></el-alert>
        <div class="order-list">
          <div class="order-item" v-for="orderInfo in orderList">
            <div class="info" v-if="!orderInfo.isEmpty">
              <div class="sn">{{ orderInfo.hangNo }} <span @click="remove(orderInfo.hangNo)" class="remove el-icon-delete"></span></div>
              <div class="order" v-if="!orderInfo.isEmpty" @click="getHangUp(orderInfo.hangNo, orderInfo.memberInfo)">
                <div class="item">会员：{{ orderInfo.memberInfo ? orderInfo.memberInfo.name : '游客' }}</div>
                <div class="item">件数：{{ orderInfo.num }}</div>
                <div class="item">金额：￥{{ orderInfo.amount }}</div>
                <div class="item time">{{ orderInfo.dateTime }}</div>
              </div>
            </div>
            <div v-if="orderInfo.isEmpty" class="none" @click="doHangUp(orderInfo.hangNo)">
              <div class="sn">{{ orderInfo.hangNo }} <span @click="remove(orderInfo.hangNo)" class="remove el-icon-delete"></span></div>
              <div class="tips">空白位置</div>
            </div>
          </div>
        </div>
        <div class="clearfix"></div>
        <div slot="footer" class="dialog-footer">
            <el-button type="primary" class="main-button" @click="close()">关闭</el-button>
        </div>
    </el-dialog>
</template>
<script>
import { doHangUp, getHangUpList, removeHangUp } from "@/api/cashier";
import { Message } from "element-ui";
export default {
    props: {
      showDialog: {
        type:[Boolean],
        default:()=>false
      },
      memberInfo: {
         type:[Object],
         default:()=>{}
      },
      cartList: {
        type:[Array],
        default:()=>[]
      },
    },
    watch: {
      showDialog(value) {
        if (value) {
            console.log('memberInfo = ', this.memberInfo)
            this.getHangUpList();
        }
      }
    },
    data(){
        return {
          loading: false,
          orderList: []
        }
    },
    methods: {
        // 删除挂单
        remove(hangNo) {
          const app = this;
          const param = { hangNo: hangNo };
          removeHangUp(param).then( response => {
            if (response) {
                Message({
                  message: "删除挂单成功",
                  type: "success"
                });
                app.getHangUpList();
            }
          }).catch(() => {
             // empty
          });
        },
        // 执行挂单
        doHangUp(hangNo) {
           const app = this;
           if (!app.cartList || app.cartList.length < 1) {
               Message({
                 message: "请先添加结算商品",
                 type: "error"
               });
               return false;
           }
           let cartIds = [];
           app.cartList.forEach(function(cart) {
              cartIds.push(cart.cartId);
           })
           const param = { hangNo: hangNo, userId: app.memberInfo ? app.memberInfo : '', cartIds: cartIds.join(',') };
           doHangUp(param).then( response => {
               if (response) {
                   Message({
                     message: "挂单成功",
                     type: "success"
                   });
                   app.getHangUpList();
                   app.$emit('doHangUp');
               }
           }).catch(() => {
              // empty
           });
        },
        // 获取挂单列表
        getHangUpList() {
          const app = this;
          getHangUpList().then( response => {
            if (response.data) {
                app.orderList = response.data;
            }
          }).catch(() => {
             // empty
          });
        },
        // 取单
        getHangUp(hangNo, memberInfo) {
           this.$emit('getHangNo', { 'hangNo': hangNo, 'memberInfo': memberInfo });
        },
        close() {
           this.$emit('closeDialog','hangUpDialog');
        }
    }
}
</script>
<style lang="scss" scoped>
  .order-list {
     margin-top: 6px;
     .order-item {
         width: 20%;
         float: left;
         height: 200px;
         cursor: pointer;
         padding: 5px;
         .info {
           border: solid 1px #cccccc;
           padding: 5px;
           height: 100%;
           border-radius: 4px;
           background: #e6f7f7;
           border: #00acac solid 1px;
           .sn {
             font-weight: bold;
             font-size: 20px;
             color: #ff5b57;
             .remove {
               float: right;
               font-size: 12px;
               color: #666666;
               font-weight: bold;
             }
           }
           .order {
              margin: 10px;
              .item {
                 margin-bottom: 3px;
              }
              .time {
                 position: relative;
                 bottom : -50px;
                 color: #909399;
              }
           }
         }
         .none {
            height: 100%;
            color: #666666;
            border: solid 1px #cccccc;
            border-radius: 4px;
            padding: 10px;
            .tips {
               line-height: 150px;
               text-align: center;
            }
           .sn {
             font-weight: bold;
             font-size: 20px;
             color: #ff5b57;
             .remove {
               float: right;
               font-size: 12px;
               color: #666666;
               font-weight: bold;
             }
           }
         }
     }
  }
</style>
