<template>
   <div id="app" class="app-container">
     <el-drawer
       :withHeader="false"
       :visible.sync="isClose"
       direction="ttb"
       size="100%"
       :show-close="false">
       <div class="main">
         <div class="left-side">
           <div class="logo">
             <div class="title">收银台</div>
             <div class="store" v-if="storeInfo">({{ storeInfo.name }})</div>
             <div class="account">您好，{{ accountInfo.realName }}！</div>
           </div>
           <div class="cate">
             <ul class="nav">
               <li class="nav-item" v-for="menu in menuList">
                 <a :class="'nav-link' + (activeMenu == menu.key ? ' active' : '')" href="javascript:;" @click="switchMenu(menu.key)">
                    <img class="cate-logo" :src="menu.logo">
                    <span>{{ menu.name }}</span>
                 </a>
               </li>
             </ul>
             <div class="client">
                <a target="_blank" href="https://www.fuint.cn/download?v=202307024">下载客户端</a>
             </div>
           </div>
         </div>
         <div class="main-side">
            <div class="title">
              <el-form class="search-form" ref="searchForm" :inline="true" :model="searchForm">
                <el-form-item class="form-item" label="" prop="keyword">
                  <el-input v-model="searchForm.keyword" prefix-icon="el-icon-full-screen" class="input-item" placeholder="请输入商品关键字：商品名称、条码、商品ID..." clearable maxlength="100" />
                </el-form-item>
                <el-button type="primary" style="height: 50px;" @click="doQueryGoods()" icon="el-icon-search">查询商品</el-button>
              </el-form>
              <el-tabs class="tab-box" type="card" v-model="navTab" @tab-click="switchTab">
                <el-tab-pane label="全部" name="0"></el-tab-pane>
                <el-tab-pane v-for="tab in tabList" :label="tab.name" :key="tab.id+''" :name="tab.id+''"></el-tab-pane>
              </el-tabs>
            </div>
            <div class="goods-list">
              <div class="goods-item" v-for="goodsInfo in activeGoodsList">
                  <div class="item" @click="clickGoods(goodsInfo)">
                    <img class="image" :src="imagePath + goodsInfo.logo">
                    <div class="goods-name">{{ goodsInfo.name }}</div>
                    <div class="goods-price">￥{{ goodsInfo.price }}</div>
                  </div>
              </div>
              <el-empty v-if="activeGoodsList.length == 0" description="暂无商品..."></el-empty>
            </div>
         </div>
         <div class="right-side">
           <div class="title">
             <div class="logo">
               <img class="avatar" v-if="!memberInfo || !memberInfo.avatar" src="@/assets/images/avatar.png" />
               <img class="avatar" v-else :src="memberInfo.avatar"/>
             </div>
              <div class="member-info">
                  <span class="name" v-if="memberInfo">{{ memberInfo.mobile ? memberInfo.mobile : memberInfo.name }}</span>
                  <span class="none" v-if="!memberInfo">当前为游客</span>
                  <el-button size="mini" class="switch" type="danger" icon="el-icon-refresh" @click="switchMember()">关联会员</el-button>
              </div>
              <div class="full-screen">
                  <screenfull id="screenfull" class="icon" />
              </div>
           </div>
           <div class="carts">
             <el-tabs type="border-card" v-model="activeTab">
               <el-tab-pane class="tab" label="cart" name="cart">
                 <div slot="label" class="cart-tab-label"><i class="el-icon-goods"></i>结算商品</div>
                 <div class="cart-list" v-if="cartList.length > 0">
                     <div class="cart-item" v-for="cartInfo in cartList">
                       <img class="image" :src="cartInfo.logo"/>
                       <div class="info">
                           <div class="name">{{ cartInfo.name }}</div>
                           <div class="spec" v-if="cartInfo.specList && cartInfo.specList.length > 0">
                              <span class="item" v-for="spec in cartInfo.specList" :title="spec.value">{{ spec.value }}</span>
                           </div>
                           <div class="num"><el-input-number class="input" @change="changeBuyNum(cartInfo)" v-model="cartInfo.buyNum" :min="1" :max="1000"/></div>
                       </div>
                       <div class="option">
                           <div class="remove el-icon-delete" @click="removeFromCart(cartInfo.cartId)"></div>
                           <div class="total">￥{{ (cartInfo.price * cartInfo.buyNum).toFixed(2) }}</div>
                       </div>
                     </div>
                 </div>
                 <div class="empty" v-if="cartList.length < 1">
                   <el-empty description="暂无结算商品" :image-size="40"></el-empty>
                 </div>
               </el-tab-pane>
               <el-tab-pane class="tab" label="order" name="order">
                 <div slot="label" class="cart-tab-label" @click="getOrderList()"><i class="el-icon-notebook-2"></i>订单列表</div>
                 <div class="order-list" v-if="orderList.length > 0">
                   <div class="order-item" v-for="orderItem in orderList">
                       <div class="left">
                         <div class="info">单号：<span class="order-sn">{{ orderItem.orderSn }}</span></div>
                         <div class="info">金额：<span class="order-price">￥{{ orderItem.amount }}</span></div>
                         <div class="info" v-if="orderItem.isVisitor == 'N'">会员：<span class="member-info">{{ orderItem.userInfo.name }} {{ orderItem.userInfo.mobile }}</span></div>
                         <div class="info" v-if="orderItem.isVisitor == 'Y'">会员：<span class="member-info">游客</span></div>
                         <div class="info">时间：<span class="order-time">{{ orderItem.createTime }}</span></div>
                       </div>
                       <div class="right">
                          <div class="info">
                            <el-button v-if="orderItem.payStatus== 'B'" type="primary" style="padding:5px;" size="mini">已支付</el-button>
                            <el-button v-if="orderItem.payStatus== 'A'" @click="doPayOrder(orderItem)" type="danger" style="padding:5px;" size="mini">未支付</el-button>
                          </div>
                       </div>
                   </div>
                 </div>
                 <div class="empty" v-if="orderList.length < 1">
                   <el-empty description="暂无订单" :image-size="40"></el-empty>
                 </div>
               </el-tab-pane>
             </el-tabs>
           </div>
           <div class="footer">
             <div class="number">
               <div class="total-num">总件数：<b class="num">{{ cartTotalNum }}</b></div>
               <div class="total-price">总金额：<b class="num">￥{{ cartTotalPrice.toFixed(2) }}</b></div>
             </div>
             <div class="options">
               <div class="cash" @click="hangUp()">挂单 / 取单</div>
               <div class="submit" v-if="cartTotalNum > 0" @click="doSettlement()">提交结算</div>
               <div class="submit" v-if="cartTotalNum == 0" @click="doCashier()">无商品收款</div>
             </div>
           </div>
         </div>
       </div>
     </el-drawer>

     <!-- 规格详情 start-->
     <el-dialog title="选择商品规格" :visible.sync="openGoodsDialog" class="common-dialog" append-to-body>
       <div class="goods-info">
         <div class="name">{{ goodsInfo.name }}</div>
         <div class="price">￥{{ goodsInfo.price }}</div>
         <div class="num"><el-input-number class="input" v-model="goodsNum" :min="1" :max="1000"/></div>
         <div class="spec-list" v-if="goodsInfo.isSingleSpec == 'N'">
             <div class="spec-item" v-for="specInfo in goodsInfo.specList">
                <div class="spec-name">{{ specInfo.name }}</div>
                <div class="values">
                  <span v-for="value in specInfo.child" :class="goodsSpecIds.includes(value.id) ? 'value active' : 'value'" @click="selectGoodsSpec(value.id)">{{ value.name }}</span>
                </div>
             </div>
         </div>
       </div>
       <div slot="footer" class="dialog-footer">
         <el-button type="primary" @click="addToCart()">加入结算</el-button>
         <el-button @click="closeGoodsDialog()">取消</el-button>
       </div>
     </el-dialog>
     <!-- 规格详情 end-->

     <!--关联会员对话框 start-->
     <switchMemberDialog :show-dialog="openSwitchMemberDialog" @doSwitchMember="doSwitchMember"/>
     <!--关联会员对话框 end-->

     <!--结算对话框 start-->
     <settlementDialog :show-dialog="openSettlementDialog" :memberInfo="memberInfo" :totalPrice="cartTotalPrice" :orderInfo="orderInfo" :couponList="couponList" @submit="submitSettlement" @switchMember="switchMember" @closeDialog="closeDialog"/>
     <!--结算对话框 end-->

     <!--扫码付款对话框 start-->
     <scanPayCodeDialog ref="scanPayCodeDialog" :show-dialog="openScanPayCodeDialog" :memberInfo="memberInfo" :orderId="orderId" :payType="payType" :payAmount="payAmount" @closeDialog="closeDialog" @showPayResult="showPayResult"/>
     <!--扫码付款对话框 end-->

     <!--支付结果对话框 start-->
     <payResultDialog :show-dialog="openPayResultDialog" :payResult="payResult" @showOrderPrint="showOrderPrint" @closeDialog="closeDialog"/>
     <!--支付结果对话框 end-->

     <!--打印订单对话框 start-->
     <orderPrintDialog :show-dialog="openOrderPrintDialog" :storeInfo="storeInfo" :orderInfo="orderInfo" @closeDialog="closeDialog"/>
     <!--打印订单对话框 end-->

     <!--挂单对话框 start-->
     <hangUpDialog :show-dialog="openHangUpDialog" :memberInfo="memberInfo" :cartList="cartList" @getHangNo="getHangNo" @doHangUp="doHangUp" @closeDialog="closeDialog"/>
     <!--挂单对话框 end-->

     <!-- 无商品收款组件 start-->
     <noGoodsCashier :show-dialog="openNoGoodsCashierDialog" @submit="submitCashier" @closeDialog="closeDialog"></noGoodsCashier>
     <!-- 无商品收款组件 end-->
   </div>
</template>

<script>
import { init, getGoodsInfo, searchGoods, getCartList, saveCart, removeFromCart, submitSettlement, getOrderList, doPay, getMemberInfoById } from "@/api/cashier";
import { getOrderInfo } from "@/api/order";
import { getUserId, setUserId, removeUserId } from '@/utils/auth';
import switchMemberDialog from "./components/switchMemberDialog";
import settlementDialog from "./components/settlementDialog";
import scanPayCodeDialog from "./components/scanPayCodeDialog";
import payResultDialog from "./components/payResultDialog";
import orderPrintDialog from "./components/orderPrintDialog";
import hangUpDialog from "./components/hangUpDialog";
import Screenfull from '@/components/Screenfull';
import noGoodsCashier from "./components/noGoodsCashier";
export default {
  name: "Cashier",
  components: {
    switchMemberDialog,
    settlementDialog,
    scanPayCodeDialog,
    payResultDialog,
    orderPrintDialog,
    hangUpDialog,
    Screenfull,
    noGoodsCashier
  },
  data() {
    return {
      // 右侧tab
      activeTab: 'cart',
      // 导航tab
      navTab: '0',
      isClose: true,
      openGoodsDialog: false,
      openSwitchMemberDialog: false,
      openSettlementDialog: false,
      openScanPayCodeDialog: false,
      openPayResultDialog: false,
      openOrderPrintDialog: false,
      openHangUpDialog: false,
      openNoGoodsCashierDialog: false,
      searchForm: { keyword: '' },
      payResult: { isSuccess: false, payAmount: 0, orderId: 0 },
      goodsForm: {},
      // 左侧菜单
      menuList: [{ name: '商品列表', key: 'goods', logo: require('../../assets/images/goods.png') },
                 { name: '订单列表', key: 'order', logo: require('../../assets/images/order.png') },
                 { name: '卡券核销', key: 'coupon', logo: require('../../assets/images/life.png') },
                 { name: '会员列表', key: 'member', logo: require('../../assets/images/hot.png') }],
      // 激活菜单
      activeMenu: 'goods',
      // 导航栏tab
      tabList: [],
      // 当前操作会员
      memberInfo: null,
      // 当前登录用户
      accountInfo: {},
      // 当前门店信息
      storeInfo: {},
      // 当前操作商品
      goodsInfo: { num : 1, specList : [], skuList: [] },
      // 当前选择属性
      goodsSpecIds: [],
      // 商品数量
      goodsNum: 1,
      // 当前分类
      cateId: 0,
      // 图片目录
      imagePath: '',
      // 商品分类列表
      cateList: [],
      // 商品列表
      goodsList: [],
      // 当前分类商品列表
      activeGoodsList: [],
      // 购物车列表
      cartList: [],
      // 订单列表
      orderList: [],
      // 总金额
      cartTotalPrice: 0,
      // 购物车备注
      cartRemark: '',
      // 总件数
      cartTotalNum: 0,
      // 支付金额
      payAmount: 0,
      // 当前订单号
      orderId: 0,
      // 支付方式
      payType: '',
      // 当前订单
      orderInfo: {},
      // 可用卡券列表
      couponList: [],
      // 挂单序号
      hangNo: '',
      isSearch: true
    };
  },
  mounted() {
    const app = this;
    // 监听扫码枪按键
    let code = '';
    let lastTime, nextTime; // 上次时间、最新时间
    let lastCode, nextCode; // 上次按键、最新按键
    document.onkeypress = (e) => {
      // 获取按键
      if (window.event) { // IE
          nextCode = e.keyCode;
      } else if (e.which) { // Netscape/Firefox/Opera
          nextCode = e.which;
      }
      // 如果触发了回车事件(扫码结束时间)
      if (nextCode === 13) {
          if (code.length < 3) {
              return;
          }
          if (app.openScanPayCodeDialog == true) {
              app.$refs.scanPayCodeDialog.submit(code);
              return false;
          }
          if (app.openSwitchMemberDialog == true) {
              return false;
          }
          if (app.openSettlementDialog == true) {
              app.$alert("请点击确定收款！");
              return false;
          }
          app.addToCart(code);
          code = '';
          lastCode = '';
          lastTime = '';
          return true;
      }
      nextTime = new Date().getTime(); // 记录最新时间
      if (!lastTime && !lastCode) { // 如果上次时间和上次按键为空
          code += e.key; // 执行叠加操作
      }
      // 如果有上次时间及上次按键
      if (lastCode && lastTime && nextTime - lastTime > 30) {
          code = e.key;
      } else if (lastCode && lastTime) {
          code += e.key;
      }
      lastCode = nextCode;
      lastTime = nextTime;
    }
  },
  created() {
    // 隐藏各路菜单
    this.$store.state.app.sidebar.hide = true
    this.initCashier();
    this.getCartList();
  },
  methods: {
    // 初始化数据
    initCashier() {
      const app = this;
      const userId = getUserId() > 0 ? getUserId() : 0;
      init(userId).then( response => {
          app.cateList = response.data.cateList;
          app.tabList = response.data.cateList;
          app.goodsList = response.data.goodsList;
          app.activeGoodsList = this.goodsList;
          app.imagePath = response.data.imagePath;
          app.storeInfo = response.data.storeInfo;
          app.accountInfo = response.data.accountInfo;
          app.memberInfo = response.data.memberInfo;
          app.loading = false;
          app.hangNo = '';
        }
      ).catch(() => {
        // empty
      });
    },
    // 菜单切换
    switchMenu(menuKey) {
      this.activeMenu = menuKey;
      let url = '';
      if (menuKey == 'coupon') {
          url = '/coupon/confirm/index';
      }
      if (menuKey == 'order') {
          url = '/order/index';
      }
      if (menuKey == 'member') {
          url = '/member/index';
      }
      if (url) {
          const routeLink = this.$router.resolve({ path: url });
          window.open(routeLink.href, '_blank');
      }
    },
    // tab切换
    switchTab(el) {
      this.navTab = el.name;
      this.filterCate(this.navTab);
    },
    // 过滤分类商品
    filterCate(cateId) {
       this.cateId = cateId;
       const dataList = [];
       this.goodsList.forEach(function(goods) {
          if (cateId == goods.cateId && cateId > 0) {
              dataList.push(goods);
          } else if (cateId == 0) {
              dataList.push(goods);
          }
       })
       this.activeGoodsList = dataList;
    },
    // 购物车列表
    getCartList() {
      const app = this;
      if (app.loading) {
          return false;
      }
      app.loading = true;
      app.cartList = [];
      getCartList({ userId: getUserId(), hangNo: app.hangNo }).then( response => {
           const cartList = response.data.list;
           if (cartList && cartList.length > 0) {
               cartList.forEach(function(item) {
                  const specList = [];
                  if (item.specList && item.specList.length > 0) {
                      item.specList.forEach(function (spec) {
                        specList.push({name: spec.specName, value: spec.specValue});
                      })
                  }
                  const cartInfo = { cartId: item.id, skuId: item.skuId, goodsId: item.goodsInfo.id, name: item.goodsInfo.name, logo: item.goodsInfo.logo, price: item.goodsInfo.price, buyNum: item.num, specList: specList };
                  app.cartList.push(cartInfo);
               })
           }
           app.cartTotalPrice = response.data.totalPrice;
           app.cartTotalNum = response.data.totalNum;
           app.couponList = response.data.couponList;
           app.loading = false;
      }).catch(() => {
          // empty
      });
    },
    // 查询商品
    doQueryGoods() {
      const app = this;
      if (!app.searchForm.keyword) {
          app.activeGoodsList = app.goodsList;
          return false;
      }
      app.loading = true;
      searchGoods({ keyword: app.searchForm.keyword }).then( response => {
         app.loading = false;
         if (response.data && response.data.length > 0) {
             app.activeGoodsList = response.data;
         } else {
             app.$modal.alert("抱歉，未查询到商品信息！");
             return false;
         }
      }).catch(() => {
         // empty
      });
    },
    // 点击商品规格弹框
    clickGoods(goodsInfo) {
      const app = this;
      if (app.loading) {
          return false;
      }
      app.loading = true;
      getGoodsInfo(goodsInfo.id).then( response => {
          app.goodsInfo = response.data.goodsInfo;
          app.goodsInfo.specList = response.data.specList;
          app.goodsInfo.skuList = response.data.skuList;
          app.goodsNum = 1;
          app.loading = false;
          if (app.goodsInfo.isSingleSpec == 'N') {
              app.openGoodsDialog = true;
          } else {
              app.addToCart(false);
          }
      }).catch(() => {
          // empty
      });
    },
    // 关闭规格弹框
    closeGoodsDialog() {
       this.openGoodsDialog = false;
    },
    // 选择商品属性
    selectGoodsSpec(specId) {
       const app = this;
       let specIds = [];
       app.goodsInfo.specList.forEach(function() {
          specIds.push(0);
       })
       app.goodsInfo.specList.forEach(function(specItem, index) {
          const children = [];
          specItem.child.forEach(function(child) {
              children.push(child.id)
          })
          if (children.includes(specId)) {
              specIds[index] = specId
          } else {
              specIds[index] = app.goodsSpecIds[index] == undefined ? 0 : app.goodsSpecIds[index];
          }
       })
       app.goodsSpecIds = specIds;
    },
    // 加入购物车
    addToCart(skuNo) {
      const app = this;
      app.isSearch = false;
      // 扫码枪扫描商品条码，直接加入购物车
      if (skuNo) {
          searchGoods({ keyword: skuNo }).then( response => {
            app.loading = false;
            if (response.data && response.data.length == 1) {
                app.clickGoods(response.data[0]);
                app.isSearch = true;
            } else {
                app.$modal.alert("抱歉，未查询到商品信息！");
                return false;
            }
          }).catch(() => {
             //empty
          });
      }
      if (app.loading || app.isSearch || !app.goodsInfo.id || app.goodsNum <= 0) {
          return false;
      }
      const specIds = app.goodsSpecIds.join('-');
      let skuId = 0;
      app.goodsInfo.skuList.forEach(function(skuInfo) {
         if (skuInfo.specIds == specIds) {
             skuId = skuInfo.id;
         }
      })
      if (app.goodsInfo.isSingleSpec == 'N' && skuId <= 0) {
          app.$modal.alert("请先确认商品规格！");
          return false;
      }
      // 添加到购物车
      const cartInfo = { goodsId: app.goodsInfo.id,
                         name: app.goodsInfo.name,
                         logo: app.goodsInfo.logo,
                         price: app.goodsInfo.price,
                         skuId: skuId,
                         userId: getUserId(),
                         hangNo: app.hangNo,
                         buyNum: app.goodsNum };
      app.loading = true;
      saveCart(cartInfo).then(response => {
          if (response.data.cartId) {
              app.loading = false;
              app.activeTab = 'cart';
              app.getCartList();
              app.openGoodsDialog = false;
              app.goodsSpecIds = [];
              app.goodsNum = 0;
          }
      }).catch(() => {
         // empty
      });
    },
    // 删除购物车
    removeFromCart(cartId) {
      const app = this;
      removeFromCart({ cartId: [cartId], userId: getUserId() }).then(response => {
        if (response.data) {
            app.getCartList();
        }
      }).catch(() => {
         // empty
      });
    },
    // 购物车数量变化
    changeBuyNum(cartInfo) {
      const app = this;
      const param = { goodsId: cartInfo.goodsId,
                      skuId: cartInfo.skuId,
                      cartId: cartInfo.cartId,
                      action: '=',
                      userId: app.memberInfo ? app.memberInfo.id : null,
                      hangNo: app.hangNo,
                      buyNum: cartInfo.buyNum };
      saveCart(param).then(response => {
        if (response.data.cartId) {
            app.getCartList();
            app.openGoodsDialog = false;
            app.goodsSpecIds = [];
        }
      }).catch(() => {
         // empty
      });
    },
    // 弹出关联会员
    switchMember() {
       this.openSwitchMemberDialog = true;
    },
    // 确认关联会员
    doSwitchMember(memberInfo) {
       this.openSwitchMemberDialog = false;
       if (memberInfo != 0) {
           this.memberInfo = memberInfo;
           if (memberInfo) {
               setUserId(memberInfo.id);
           } else {
               removeUserId();
           }
           this.getCartList();
       }
    },
    // 无商品结算
    doCashier() {
      this.orderInfo = {};
      this.openNoGoodsCashierDialog = true;
    },
    submitCashier(param) {
      this.orderInfo = {};
      this.openSettlementDialog = true;
      this.cartTotalPrice = parseFloat(param.amount);
      this.cartRemark = param.remark;
      this.openNoGoodsCashierDialog = false;
    },
    // 发起结算
    doSettlement() {
       if (this.cartList.length < 1) {
           this.$modal.alert("请先添加结算商品！");
           return false;
       }
       this.getCartList();
       this.orderInfo = {};
       this.openSettlementDialog = true;
    },
    // 确认结算
    submitSettlement(param) {
      const app = this;
      // 已生成的订单支付
      if (app.orderInfo.id) {
          // 微信、支付宝支付
          if (param.payType == 'MICROPAY' || param.payType == 'ALISCAN') {
              app.payAmount = app.orderInfo.payAmount;
              app.orderId = app.orderInfo.id;
              app.openScanPayCodeDialog = true;
              app.openSettlementDialog = false;
              app.payType = param.payType;
          }
          // 现金、余额支付
          if (param.payType == 'CASH' || param.payType == 'BALANCE') {
              doPay({ orderId: app.orderId, payType: param.payType, cashierPayAmount: param.totalPrice, cashierDiscountAmount: param.discountPrice, userId: getUserId() }).then( response => {
                  app.openSettlementDialog = false;
                  if (response.data.orderInfo.payStatus == 'B') {
                      app.showPayResult({ isSuccess: true, payAmount: response.data.orderInfo.payAmount, orderId: response.data.orderInfo.id });
                  } else {
                      app.$modal.alert(response.data.message ? response.data.message : "抱歉，订单操作异常！");
                  }
              }).catch(() => {
                // empty
              });
          }
          return false;
      }
      // 购物车提交结算
      let cartIds = [];
      app.cartList.forEach(function(cart){
          cartIds.push(cart.cartId);
      })
      const data = { cashierPayAmount: param.totalPrice,
                     cashierDiscountAmount: param.discountPrice,
                     cartIds: cartIds.join(','),
                     orderMode: 'oneself',
                     payType: param.payType,
                     remark: param.remark,
                     type: app.cartList.length > 0 ? 'goods' : 'payment',
                     couponId: param.userCouponId ? param.userCouponId : 0,
                     userId: getUserId() };
      submitSettlement(data).then( response => {
          app.initCashier();
          app.getCartList();
          // 微信支付，弹出扫码框
          if (response.data.orderInfo.payType == 'MICROPAY' || param.payType == 'ALISCAN') {
             app.payAmount = response.data.orderInfo.payAmount;
             app.orderId = response.data.orderInfo.id;
             app.openScanPayCodeDialog = true;
             app.openSettlementDialog = false;
             app.payType = param.payType;
             return false;
          }
          // 现金、余额支付
          if (response.data.orderInfo.payType == 'CASH' || response.data.orderInfo.payType == 'BALANCE') {
              app.openSettlementDialog = false;
              if (response.data.orderInfo.payStatus == 'B') {
                  app.showPayResult({ isSuccess: true, payAmount: response.data.orderInfo.payAmount, orderId: response.data.orderInfo.id });
              } else {
                  app.$modal.alert(response.data.message);
              }
              return false;
          }
      }).catch(() => {
         // empty
      });
    },
    // 点击挂单/取单
    hangUp() {
       this.openHangUpDialog = true;
    },
    // 取单
    getHangNo(data) {
       this.openHangUpDialog = false;
       this.hangNo = data.hangNo;
       this.getCartList();
       // 关联会员信息
       if (data.hangNo && data.hangNo.length > 0 ) {
           this.doSwitchMember(data.memberInfo);
       }
    },
    // 执行挂单
    doHangUp() {
      this.hangNo = '';
      this.getCartList();
      removeUserId();
      this.memberInfo = null;
    },
    // 关闭对话框
    closeDialog(dialog) {
       if (dialog == 'settlementDialog') {
           this.openSettlementDialog = false;
       } else if (dialog == 'switchMemberDialog') {
           this.openSwitchMemberDialog = false;
       } else if (dialog == 'scanPayCodeDialog') {
           this.openScanPayCodeDialog = false;
       } else if (dialog == 'payResultDialog') {
           this.openPayResultDialog = false;
           this.getOrderList();
       } else if (dialog == 'printOrder') {
           this.openOrderPrintDialog = false;
           this.getOrderList();
       } else if (dialog == 'hangUpDialog') {
           this.openHangUpDialog = false;
       } else if (dialog == 'openNoGoodsCashierDialog') {
           this.openNoGoodsCashierDialog = false;
       }
    },
    // 展示支付结果
    showPayResult(payResult) {
       this.payResult = payResult;
       this.openPayResultDialog = true;
    },
    // 订单列表
    getOrderList() {
      const app = this;
      if (app.loading) {
          return false;
      }
      app.loading = true;
      getOrderList({ page: 1, pageSize: 20 }).then(response => {
          app.loading = false;
          if (response.data) {
              app.orderList = response.data.content;
          }
      })
    },
    // 订单支付
    doPayOrder(orderInfo) {
       const app = this;
       app.payAmount = orderInfo.amount;
       app.orderId = orderInfo.id;
       app.orderInfo = orderInfo;
       let userId = 0
       if (orderInfo.isVisitor !== "Y") {
           userId = app.orderInfo.userInfo.id;
       }
       getMemberInfoById(userId).then(response => {
          if (response.data.memberInfo) {
              app.memberInfo = response.data.memberInfo;
          } else {
              app.memberInfo = null;
          }
       }).catch(() => {
          // empty
       });
       app.openSettlementDialog = true;
    },
    // 打印小票
    showOrderPrint(orderId) {
      const app = this;
      getOrderInfo(orderId).then(response => {
        if (response.data.orderInfo) {
            app.orderInfo = response.data.orderInfo;
            app.openOrderPrintDialog = true;
        }
      }).catch(() => {
         // empty
      });
    }
  }
};
</script>

<style lang="scss" scoped>
   .main {
     height: 100%;
     width: 100%;
     display: flex;
     flex-direction: row;
     .left-side {
        width: 160px;
        height: 100%;
        background: #EE9A49;
        border-right: #888888 solid 2px;
        position: absolute;
        left: 0;
        top: 0;
        padding: 0px;
        color: #FFFFFF;
        overflow-x: hidden;
        overflow-y: auto;
        display: block;
        text-align: center;
       .client {
         margin-top: 20px;
         text-align: center;
         font-size: 12px;
         color: #ffffff;
         font-weight: bold;
       }
       .logo {
         height: 70px;
         padding: 20px 12px 20px 12px;
         font-weight: bold;
         .store {
           font-size: 12px;
         }
         .account {
           font-size: 12px;
           border: solid 2px #FFFFFF;
           background: #6c757d;
           cursor: pointer;
           margin-top: 6px;
           padding: 2px;
           border-radius: 2px;
         }
       }
       .cate {
         text-align: center;
         margin-left: 14px;
         margin-top: 40px;
         .nav {
            list-style: none;
            display: block;
            margin: 0px;
            padding: 5px;
            text-align: center;
            .nav-item {
                margin-top: 10px;
                font-size: 14px;
                width: 120px;
                text-align: center;
               .nav-link {
                 position: relative;
                 padding: .5356875rem .9375rem;
                 white-space: nowrap;
                 text-align: center;
                 font-weight: 600;
                 color: #666666;
                 display: flex;
                 align-items: center;
                 flex-direction: column;
                 justify-content: center;
                 border-radius: 5px;
                 transition: color .2s linear;
                 background: #f5f5f5;
                 border: 2px solid #FFFFFF;
                 .cate-logo {
                   display: block;
                   width: 50px;
                   height: 50px;
                 }
               }
               .active {
                 font-weight: bold;
                 border: 4px #ff5b57 solid;
               }
            }
         }
       }
     }
     .right-side {
        width: 310px;
        height: 100%;
        background: #FFFFFF;
        display: block;
        position: absolute;
        right: 0;
        top: 0;
        overflow-x: hidden;
        overflow-y: auto;
        padding: 0px;
        border-left: #cccccc solid 1px;
        .title {
          height: 70px;
          width: 100%;
          background: #6c757d;
          border-bottom: #CCCCCC 2px solid;
          color: #FFFFFF;
          padding-top: 20px;
          padding-left: 1px;
          display: block;
          position: fixed;
          top: 0;
          z-index: 999;
          clear: both;
          .logo {
            float: left;
            font-size: 24px;
            .avatar {
               width: 30px;
               height: 30px;
               border-radius: 40px;
               margin: 5px 0px 5px 5px;
            }
          }
          .member-info {
            float: left;
            margin-top: 5px;
            .name {
              margin-left: 2px;
              margin-right: 3px;
            }
            .none {
               margin-left: 2px;
               margin-right: 5px;
               font-size: 13px;
            }
            .switch {
              padding: 8px 8px 8px 4px;
            }
          }
          .full-screen {
             background: #EE9A49;
             border: solid 1px #ffffff;
             width: 30px;
             height: 30px;
             line-height: 30px;
             text-align: center;
             margin-left: 270px;
             overflow: hidden;
             margin-top: 5px;
             cursor: pointer;
          }
        }
       .carts {
          display: block;
          width: 100%;
          color: #666666;
          margin-bottom: 120px;
          margin-top: 70px;
          padding: 0px;
          .cart-tab-label {
             width: 105px;
             text-align: center;
          }
          .tab {
            width: 50%;
            .cart-list {
              margin-bottom: 60px;
              .cart-item {
                border-bottom: 1px dashed #cccccc;
                height: 110px;
                width: 310px;
                padding-top: 5px;
                padding-right: 10px;
                .image {
                   width: 50px;
                   height: 50px;
                   margin-left: 5px;
                   border-radius: 5px;
                   border: solid 1px #ccc;
                   float: left;
                   margin-top: 20px;
                }
                .info {
                   float: left;
                   padding-left: 5px;
                   margin-top: 8px;
                   .name {
                     font-weight: bold;
                     font-size: 12px;
                     width: 150px;
                     overflow: hidden;
                     display: -webkit-box;
                     -webkit-box-orient: vertical;
                     -webkit-line-clamp: 2;
                   }
                   .spec {
                     font-size: 12px;
                     width: 160px;
                     height: 20px;
                     .item {
                        margin-right: 2px;
                        border-radius: 10px;
                        padding: 0px 2px 0px 2px;
                        text-align: center;
                        max-width: 50px;
                        height: 20px;
                        line-height: 20px;
                        float: left;
                        display: block;
                        overflow: hidden;
                        color: #606266;
                        cursor: pointer;
                        background: #cceeee;
                        padding: 0px 3px 0px 3px;
                        white-space: nowrap;
                        text-overflow: ellipsis;
                     }
                   }
                   .input {
                     width: 120px;
                     margin-top: 2px;
                   }
                }
                .option {
                   float: right;
                   text-align: right;
                   margin-right: 10px;
                   .remove {
                      font-size: 12px;
                      cursor: pointer;
                   }
                   .total {
                     margin-top: 10px;
                     font-size: 16px;
                     color: #ff5b57;
                   }
                }
              }
            }
            .order-list {
               margin-bottom: 60px;
               .order-item {
                 width: 310px;
                 height: 100px;
                 padding: 10px;
                 border-bottom: 1px dashed #cccccc;
                 margin-bottom: 5px;
                 .left {
                    float: left;
                    font-size: 14px;
                    .info {
                      .order-price {
                        font-weight: normal;
                        color: #ff5b57;
                      }
                    }
                 }
                 .right {
                   float: right;
                   margin-top: 30px;
                   margin-right: 10px;
                 }
               }
            }
            .empty {
              width: 300px;
            }
          }
       }
       .footer {
          position: fixed;
          z-index: 999;
          background: #6c757d;
          bottom: 0;
          height: 120px;
          width: 100%;
          padding-top: 5px;
          padding-right: 15px;
          color: #FFFFFF;
          display: block;
          width: 310px;
          border-top: solid 1px #cccccc;
          .number {
            float: right;
            margin: 5px;
            .total-price {
               margin-top: 3px;
               .num {
                 color: #ff5b57;
                 font-size: 20px;
               }
            }
          }
          .options {
             text-align: center;
             cursor: pointer;
             float: right;
             color: #FFFFFF;
            .cash {
              float: left;
              height: 50px;
              border: solid 1px #EE9A49;
              line-height: 50px;
              width: 135px;
              border-radius: 5px;
              background: #EE9A49;
              font-weight: bold;
            }
            .submit {
               float: left;
               height: 50px;
               border: solid 1px #ff5b57;
               margin-left: 10px;
               line-height: 50px;
               width: 135px;
               border-radius: 5px;
               background: #ff5b57;
               font-weight: bold;
            }
          }
       }
     }
     .main-side {
       height: 100%;
       width: 100%;
       min-width: 760px;
       margin-left: 160px;
       margin-right: 320px;
       overflow: auto;
       display: block;
       background: #FFFFFF;
       margin-bottom: 10px;
       .title {
         position: fixed;
         height: 106px;
         width: calc(100% - 465px);
         min-width: 700px;
         background: #ffffff;
         padding: 5px;
         border-top: #CCCCCC 2px solid;
         color: #FFFFFF;
         top: 0;
         .search-form {
            height: 50px;
            .input-item {
              min-width: 456px;
            }
         }
         .tab-box {
            margin-top: 3px;
            width: 100%;
         }
       }
       .goods-list {
         height: 100%;
         width: 100%;
         margin-top: 108px;
         margin-left: 2px;
         margin-bottom: 10px;
         .goods-item {
           width: 33%;
           min-height: 320px;
           min-width: 240px;
           padding: 3px;
           float: left;
           background: #ffffff;
           text-align: left;
           cursor: pointer;
           .item {
              background: #FFFFFF;
              padding: 10px;
              border-radius: 5px;
              margin: 0px;
              border: solid 1px #cccccc;
             .goods-name {
               margin-top: 10px;
               font-size: 18px;
               color: #666666;
               height: 30px;
               overflow: hidden;
               white-space: nowrap;
               text-overflow: ellipsis;
             }
             .goods-price {
               color: #ff5b57;
               font-size: 18px;
               font-weight: bold;
             }
             .image {
               width: 100%;
               height: 220px;
               border-radius: 3px;
             }
           }
         }
       }
     }
   }
   .goods-info {
     border: solid 1px #ccc;
     padding: 30px;
     border-radius: 5px;
     .name {
        height: 40px;
        font-weight: bold;
        font-size: 20px;
     }
     .price {
       height: 40px;
       color: #ff5b57;
       font-size: 16px;
     }
     .spec-list {
        border: solid 1px #ccc;
        padding: 20px;
        margin-top: 10px;
        border-radius: 6px;
        .spec-item {
           margin-bottom: 20px;
          .spec-name {
             font-weight: bold;
             font-size: 16px;
          }
          .values {
             display: block;
             padding-top: 10px;
             margin-left: 0px;
             padding-left: 0px;
             font-size: 12px;
             .value {
               border: solid 1px #cceeee;
               margin-right: 10px;
               padding: 8px 15px 5px 15px;
               cursor: pointer;
               border-radius: 4px;
               background: rgba(0, 172, 172, 0.1);
               color: #666666;
             }
             .active {
               border: solid 1px #ff5891;
               background: #ff5b57;
               color: #FFFFFF;
             }
          }
        }
     }
   }
</style>
<style scoped>
  .input-item >>> .el-input__inner {
    border: #EE9A49 solid 2px;
    line-height: 50px;
    height: 50px;
  }
  .form-item >>> .el-form-item__label {
    line-height: 50px;
    height: 50px;
  }
  .el-tabs--border-card {
    box-shadow: none;
    border: none;
  }
  ::v-deep .el-tabs--border-card > .el-tabs__content {
    padding: 0px;
  }
</style>
