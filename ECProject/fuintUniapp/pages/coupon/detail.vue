<template>
  <view v-if="!isLoading" class="container b-f p-b">
    <view class="base">
        <view class="coupon-image">
            <image class="image" :src="detail.image"></image>
        </view>
        <view class="coupon-title">
          <view class="name">{{ detail.name ? detail.name : '' }}</view>
          <view v-if="detail.code && detail.status == 'A' && detail.isGive" class="gift" @click="give()"><text>转赠好友</text></view>
          <view v-if="detail.amount > 0" class="price">
              <view class="label">
                  面额：<span class="amount">￥{{ detail.amount }}</span>
              </view>
          </view>
          <view v-if="detail.point > 0" class="point">
              <view class="label">
                  兑换积分：<span class="amount">{{ detail.point }}</span>
              </view>
          </view>
          <view class="time">有效期：{{ detail.effectiveDate }}</view>
        </view>
    </view>
    <view class="coupon-qr" v-if="detail.code">
      <view>
         <image class="image" :src="detail.qrCode"></image>
      </view>
      <view class="qr-code">
          <p class="code">核销码：{{ detail.code }}</p>
          <p class="tips">请出示以上券码给核销人员</p>
      </view>
    </view>
    <view class="coupon-content m-top20">
        <view class="title">使用须知</view>
        <view class="content"><jyf-parser :html="detail.description"></jyf-parser></view>
    </view>
    <!-- 快捷导航 -->
    <shortcut/>
    <view class="give-popup" v-if="detail.qrCode">
       <uni-popup ref="givePopup" type="dialog">
          <uni-popup-dialog mode="input" focus="false" title="转赠给好友" type="info" placeholder="输入好友手机号码" :before-close="true" @close="cancelGive" @confirm="doGive"></uni-popup-dialog>
       </uni-popup>
    </view>
    <!-- 底部选项卡 -->
    <view v-if="!detail.code && !detail.isReceive" class="footer-fixed">
      <view class="footer-container">
        <!-- 操作按钮 -->
        <view class="foo-item-btn">
          <view class="btn-wrapper">
            <view class="btn-item btn-item-main" @click="receive(detail.id)">
              <text v-if="!detail.point || detail.point < 1">立即领取</text>
              <text v-if="detail.point && detail.point > 0">立即兑换</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    <view v-if="detail.isReceive" class="footer-fixed">
      <view class="footer-container">
        <!-- 操作按钮 -->
        <view class="foo-item-btn">
          <view class="btn-wrapper">
            <view class="btn-item btn-item-main state">
              <text v-if="!detail.point || detail.point < 1">您已领取</text>
              <text v-if="detail.point && detail.point > 0">您已兑换</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!--领取码 start-->
    <view class="receive-pop">
       <uni-popup ref="receiveCodePopup" type="dialog">
          <uni-popup-dialog mode="input" v-model="receiveCode" focus="false" title="领取码" type="info" placeholder="请输入领取码" :before-close="true" @close="cancelReceive" @confirm="doReceive"></uni-popup-dialog>
       </uni-popup>
    </view>
    <!--领取码 end-->
  </view>
</template>

<script>
  import jyfParser from '@/components/jyf-parser/jyf-parser'
  import Shortcut from '@/components/shortcut'
  import * as myCouponApi from '@/api/myCoupon'
  import * as giveApi from '@/api/give'
  import * as couponApi from '@/api/coupon'
  import * as MessageApi from '@/api/message'

  export default {
    components: {
      Shortcut
    },
    data() {
      return {
        // 当前会员卡券ID
        userCouponId: 0,
        // 卡券ID
        couponId: 0,
        // 加载中
        isLoading: true,
        // 当前卡券详情
        detail: null,
        qrCode: '',
        receiveCode: '' // 领取码
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      this.userCouponId = options.userCouponId ? options.userCouponId : 0;
      this.couponId = options.couponId ?options.couponId : 0;
      this.getCouponDetail();
    },

    methods: {
      // 获取卡券详情
      getCouponDetail() {
        const app = this
        myCouponApi.detail(app.couponId, app.userCouponId, "")
          .then(result => {
            app.detail = result.data
          })
          .finally(() => app.isLoading = false)
      },
      // 转赠
      give() {
        this.$refs.givePopup.open('top');
      },
      cancelGive() {
        this.$refs.givePopup.close();
      },
      doGive(friendMobile) {
        const app = this
        if (friendMobile.length < 11) {
            app.$error("请先输入好友手机号码！")
            return false
        } else {
            app.$refs.givePopup.close()
            const param = {'mobile': friendMobile, 'couponId': this.userCouponId, 'message': '转赠一张优惠券给你'}
            giveApi.doGive(param)
              .then(result => {
                  if (result.code == '200') {
                      uni.showModal({
                        title: '提示信息',
                        content: '恭喜，转增成功！',
                        showCancel: false,
                        success(o) {
                          if (o.confirm) {
                             uni.navigateBack()
                          }
                        }
                      })
                  } else {
                    app.$error(result.message)
                  }
            })
        }
      },
      // 取消领取码
      cancelReceive() {
          this.receiveCode = '';
          this.$refs.receiveCodePopup.close();
      },
      // 确认领取码
      doReceive(vCode) {
          if (!vCode || vCode.length < 1) {
              this.$error("请先输入领取码！");
              return false;
          }
          this.receiveCode = vCode;
          this.$refs.receiveCodePopup.close();
          this.receive(this.couponId);
      },
      // 领取卡券
      receive(couponId) {
        const app = this
        if (app.detail.needReceiveCode && !app.receiveCode) {
            app.$refs.receiveCodePopup.open('top');
            return false;
        }
        
        if (app.detail.point) {
            uni.showModal({
              title: "提示信息",
              content: "需要"+app.detail.point+"积分兑换，您确定兑换吗?",
              success({ confirm }) {
                if (confirm) {
                    couponApi.receive({ 'couponId': couponId, 'receiveCode': app.receiveCode })
                      .then(result => {
                        app.receiveCode = '';
                        // 显示提示
                        if (parseInt(result.code) === 200) {
                            app.detail.isReceive = true
                            app.$success("兑换成功！")
                            // #ifdef MP-WEIXIN
                            MessageApi.getSubTemplate({keys: "couponArrival,couponConfirm"}).then(result => {
                                const templateIds = result.data
                                wx.requestSubscribeMessage({tmplIds: templateIds, 
                                    success(res) {
                                        console.log("调用成功！")
                                    }, fail(res) {
                                        console.log("调用失败:", res)
                                    }, complete() {
                                        // empty
                                    }
                                })
                            })
                            // #endif
                        } else {
                            app.$error(result.message)
                        }
                      })
                }
              }
            });
        } else {
            couponApi.receive({ 'couponId': couponId, 'receiveCode': app.receiveCode })
              .then(result => {
                app.receiveCode = '';
                // 显示提示
                if (parseInt(result.code) === 200) {
                    app.detail.isReceive = true
                    app.$success("领取成功！")
                    // #ifdef MP-WEIXIN
                    MessageApi.getSubTemplate({keys: "couponArrival,couponConfirm"}).then(result => {
                        const templateIds = result.data
                        wx.requestSubscribeMessage({tmplIds: templateIds, 
                            success(res) {
                                console.log("调用成功！")
                            }, fail(res) {
                                console.log("调用失败:", res)
                            }, complete() {
                                // empty
                            }
                        })
                    })
                    // #endif
                } else {
                    app.$error(result.message)
                }
            })
        }
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    min-height: 100vh;
    padding: 20rpx;
    background: #fff;
    color:#666666;
  }
  .base {
        border: dashed 5rpx #cccccc;
        padding: 15rpx 0rpx 15rpx 15rpx;
        border-radius: 10rpx;
        margin: 20rpx;
        display: block;
        min-height: 245rpx;
        .coupon-image {
            float: left;
            margin-top: 10rpx;
            .image {
                width: 200rpx;
                height: 158rpx;
                border-radius: 8rpx;
            }
            width: 30%;
        }
        .coupon-title {
            float: left;
            margin-left: 15rpx;
            overflow: hidden;
            width: 65%;
            .name {
               font-weight: bold;
               font-size: 30rpx;
            }
            .price {
               margin-top: 28rpx;
               font-size: 25rpx;
             .amount {
                 color: #f03c3c;
                 font-weight: bold;
             }
            }
          .point {
             margin-top: 26rpx;
             font-size: 25rpx;
              .amount {
               color: #666666;
              }
          }
          .time {
               margin-top: 26rpx;
               font-size: 25rpx;
               color: #666666;
          }
        }
  }
  .coupon-qr {
      border: dashed 5rpx #cccccc;
      border-radius: 10rpx;
      margin: 20rpx;
      text-align: center;
      padding-top: 80rpx;
      padding-bottom: 30rpx;
      .image{
          width: 360rpx;
          height: 360rpx;
          margin: 0 auto;
      }
      .qr-code{
          .code{
              font-weight: bold;
              font-size: 30rpx;
              line-height: 50rpx;
          }
          .tips{
              font-size: 25rpx;
              color:#C0C4CC;
          }
      }
  }
  .coupon-content {
    padding: 15rpx;
    border: dashed 5rpx #cccccc;
    border-radius: 5rpx;
    margin: 20rpx;
    min-height: 400rpx;
    .title {
        margin-bottom: 15rpx;
    }
  }
  .gift {
    height: 46rpx;
    width: 120rpx;
    margin-top: 20rpx;
    line-height: 46rpx;
    text-align: center;
    border: 1px solid #f8df00;
    border-radius: 6rpx;
    color: #f86d48;
    background: #f8df98;
    font-size: 22rpx;
    float: right;
    &.state {
      border: none;
        color: #F5F5F5;
        background: #888888;
    }
  }
  
  /* 底部操作栏 */
  .footer-fixed {
    position: fixed;
    bottom: var(--window-bottom);
    left: 0;
    right: 0;
    display: flex;
    height: 180rpx;
    z-index: 11;
    box-shadow: 0 -4rpx 40rpx 0 rgba(144, 52, 52, 0.1);
    background: #fff;
  }
  
  .footer-container {
    width: 100%;
    display: flex;
    margin-bottom: 40rpx;
  }
  
  // 操作按钮
  .foo-item-btn {
    flex: 1;
    .btn-wrapper {
      height: 100%;
      display: flex;
      align-items: center;
    }
  
    .btn-item {
      flex: 1;
      font-size: 28rpx;
      height: 80rpx;
      line-height: 80rpx;
      margin-right: 16rpx;
      margin-left: 16rpx;
      text-align: center;
      color: #fff;
      border-radius: 40rpx;
    }
    // 立即领取按钮
    .btn-item-main {
      background: linear-gradient(to right, #f9211c, #ff6335);
      &.state {
        border: none;
          color: #cccccc;
          background: #F5F5F5;
      }
    }
  }
</style>
