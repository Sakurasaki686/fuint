<template>
  <div class="app-container">
      <div class="overview">
          <div class="title">运营概况</div>
          <div class="content" v-loading="loading">
              <el-row class="line">
                <el-col class="item" :span="6">
                   <div class="do">
                       <svg t="1641957967018" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="7597" width="64" height="64"><path d="M917.504 376.832c-20.48 0-36.864 16.384-36.864 36.864v450.56c0 4.096 0 16.384-12.288 28.672-8.192 8.192-16.384 12.288-28.672 12.288H180.224c-20.48 0-40.96-16.384-40.96-40.96V155.648c0-20.48 16.384-40.96 40.96-40.96h344.064c20.48 0 36.864-16.384 36.864-36.864s-16.384-36.864-36.864-36.864H180.224C118.784 40.96 65.536 90.112 65.536 155.648v708.608c0 61.44 49.152 114.688 114.688 114.688h663.552c28.672 0 57.344-12.288 81.92-32.768 28.672-28.672 32.768-65.536 32.768-81.92V409.6c-4.096-16.384-20.48-32.768-40.96-32.768z" fill="#8a8a8a" p-id="7598"></path><path d="M782.336 528.384c0-20.48-20.48-40.96-40.96-40.96H266.24c-24.576 0-40.96 20.48-40.96 40.96v8.192c0 20.48 16.384 40.96 40.96 40.96h475.136c20.48 0 40.96-20.48 40.96-40.96v-8.192zM741.376 712.704H266.24c-24.576 0-40.96 20.48-40.96 40.96v8.192c0 20.48 16.384 40.96 40.96 40.96h475.136c20.48 0 40.96-20.48 40.96-40.96v-8.192c0-20.48-16.384-40.96-40.96-40.96zM584.04864 304.08704l298.43456-262.30784 62.17728 70.73792-298.43456 262.30784zM532.48 413.696l90.112-20.48-61.44-69.632z" fill="#8a8a8a" p-id="7599"></path></svg>
                       <p class="text">今日新增订单数（笔）</p>
                       <p class="number">{{ homeData.todayOrder }}</p>
                   </div>
                </el-col>
                <el-col class="item" :span="6">
                  <div class="do">
                     <svg t="1641958321700" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="10174" width="64" height="64"><path d="M204.8 438.528c-16.896 0-30.72-13.824-30.72-30.72 0-121.088 98.56-219.648 219.648-219.648 16.896 0 30.72 13.824 30.72 30.72s-13.824 30.72-30.72 30.72c-87.296 0-158.208 70.912-158.208 158.208 0 16.896-13.824 30.72-30.72 30.72zM393.728 856.064c-121.088 0-219.648-98.56-219.648-219.648 0-16.896 13.824-30.72 30.72-30.72s30.72 13.824 30.72 30.72c0 87.296 70.912 158.208 158.208 158.208 16.896 0 30.72 13.824 30.72 30.72 0 16.896-13.824 30.72-30.72 30.72zM622.336 856.064c-16.896 0-30.72-13.824-30.72-30.72 0-16.896 13.824-30.72 30.72-30.72 87.296 0 158.208-70.912 158.208-158.208 0-16.896 13.824-30.72 30.72-30.72 16.896 0 30.72 13.824 30.72 30.72 0 121.088-98.56 219.648-219.648 219.648zM811.264 438.528c-16.896 0-30.72-13.824-30.72-30.72 0-87.296-70.912-158.208-158.208-158.208-16.896 0-30.72-13.824-30.72-30.72s13.824-30.72 30.72-30.72c121.088 0 219.648 98.56 219.648 219.648 0 16.896-13.824 30.72-30.72 30.72z" fill="#8a8a8a" p-id="10175"></path><path d="M684.544 564.224H355.328c-16.896 0-30.72-13.824-30.72-30.72s13.824-30.72 30.72-30.72h329.216c16.896 0 30.72 13.824 30.72 30.72s-13.824 30.72-30.72 30.72z" fill="#8a8a8a" p-id="10176"></path></svg>
                     <p class="text">今日交易金额（元）</p>
                     <p class="number">{{ homeData.todayPay ? homeData.todayPay.toFixed(2) : 0.00.toFixed(2) }}</p>
                  </div>
                </el-col>
                <el-col class="item" :span="6">
                  <div class="do">
                     <svg t="1641957839736" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="6014" width="64" height="64"><path d="M624.6 211.8c58.3 0 105.7 46.3 105.7 103.2s-47.4 103.2-105.7 103.2S518.9 371.9 518.9 315s47.4-103.2 105.7-103.2m0-50c-86 0-155.7 68.6-155.7 153.2s69.7 153.2 155.7 153.2S780.3 399.6 780.3 315s-69.7-153.2-155.7-153.2zM427 314.9c0-24.3 5.9-47.3 16.5-67.7-22.8-19.5-52.9-31.4-85.8-31.4-71.2 0-128.9 55.2-128.9 123.4s57.7 123.4 128.9 123.4c43.2 0 81.4-20.3 104.8-51.6-22.3-26.2-35.5-59.6-35.5-96.1zM297.5 727.2v16.9c0-103.5 72.5-191.8 173.7-226.2-29.2-13.3-62.1-20.9-96.9-20.9h-28.5c-118.6 0-215.6 87.2-215.6 194.7v-13.6c0 56.3-4.5 102.3 57.6 102.3H298c-0.9-16.2-0.5-34.2-0.5-53.2z" p-id="6015" fill="#8a8a8a"></path><path d="M639.4 561c27.9 0 55.1 5.2 80.6 15.4 24.5 9.8 46.6 23.9 65.5 41.7 38.3 36.2 59.4 83.9 59.4 134.5h0.2c0.1 5.1 0.3 10 0.4 14.9 0.4 13 0.8 25.2 0.3 34.9-0.3 4.5-0.7 7.7-1.1 9.8-1.9 0.3-4.8 0.5-8.7 0.5H418.4c-8.1 0-12.1-1.1-13.7-1.6-0.7-1.6-2.3-5.9-3.4-15.6-1.3-11.8-1.3-26.8-1.2-42.8h0.1c0-50.6 21.1-98.3 59.4-134.5 18.9-17.9 40.9-31.9 65.5-41.7 25.5-10.2 52.6-15.4 80.6-15.4h33.7m0-50.1h-33.8c-140.5 0-255.5 108.3-255.5 241.6v-16.9c0 69.8-5.3 127 68.2 127H836c73.6 0 58.9-57.1 58.9-127v16.9c0-133.4-115-241.6-255.5-241.6z" p-id="6016" fill="#8a8a8a"></path><path d="M785.1 706.4c0 10.2-8.4 18.5-18.7 18.5h-37.9v36.4c0 10.2-8.4 18.5-18.7 18.5h-9.4c-10.3 0-18.7-8.3-18.7-18.5v-36.4h-39.8c-10.3 0-18.7-8.3-18.7-18.5v-9.2c0-10.2 8.4-18.5 18.7-18.5h39.8v-40.2c0-10.2 8.4-18.5 18.7-18.5h9.4c10.3 0 18.7 8.3 18.7 18.5v40.2h37.9c10.3 0 18.7 8.3 18.7 18.5v9.2z" p-id="6017" fill="#8a8a8a"></path></svg>
                     <p class="text">今日新增会员数</p>
                     <p class="number">{{ homeData.todayUser }}</p>
                  </div>
                </el-col>
                <el-col class="item" :span="6">
                  <div class="do">
                     <svg t="1641958094548" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="9210" width="64" height="64"><path d="M888.34834 0.000284H127.658773A126.776854 126.776854 0 0 0 0.853475 126.777138v295.82214a126.776854 126.776854 0 0 0 126.805298 126.805298v169.045287a211.313719 211.313719 0 0 0 169.045287 207.075498v46.506653a42.268433 42.268433 0 0 0 84.508421 0V887.466705c0-23.324438-18.91555-42.268433-42.268433-42.268433a126.776854 126.776854 0 0 1-126.776854-126.776854v-169.045286h302.16525a241.3226 241.3226 0 0 0-42.268433 146.63107 42.268433 42.268433 0 1 0 84.536865 0c-1.706666-146.63107 120.433744-146.63107 120.433745-146.63107a42.268433 42.268433 0 0 0 42.268433-42.268433 54.527985 54.527985 0 0 0 0-8.021331 44.799988 44.799988 0 0 0 0-8.021331v-142.421294a48.611542 48.611542 0 0 1 42.268432-52.849763 48.611542 48.611542 0 0 1 42.268433 52.849763v369.777675a126.776854 126.776854 0 0 1-126.805298 126.776854c-23.324438 0-42.239988 18.91555-42.239988 42.268433v84.536865a42.268433 42.268433 0 0 0 84.508421 0v-46.506654a211.313719 211.313719 0 0 0 169.045286-207.075498v-169.045286a126.776854 126.776854 0 0 0 126.805298-126.776854v-295.82214A126.776854 126.776854 0 0 0 888.319895 0.000284z m42.268433 422.627439c0 23.324438-18.91555 42.239988-42.268433 42.239988v-116.195523a132.693296 132.693296 0 0 0-126.776854-137.386629 122.993744 122.993744 0 0 0-105.642637 62.151094 150.015958 150.015958 0 1 0-21.134216 76.060423v115.370635H127.630329c-23.324438 0-42.239988-18.91555-42.239988-42.239988v-295.82214c0-23.352882 18.91555-42.268433 42.268432-42.268433H888.319895c23.324438 0 42.268433 18.91555 42.268433 42.239988v295.82214z m-359.224789-147.91107a63.402649 63.402649 0 1 1-126.776854 0 63.402649 63.402649 0 0 1 126.776854 0z" p-id="9211" fill="#8a8a8a"></path></svg>
                     <p class="text">今日活跃会员数</p>
                     <p class="number">{{ homeData.todayActiveUser }}</p>
                  </div>
                </el-col>
              </el-row>
              <el-row class="line">
                <el-col class="item second-line" :span="6">
                  <div class="do">
                    <svg t="1641957661068" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5770" width="64" height="64"><path d="M261.7 522.6h0.2c19.3 0 35-15.7 35-35s-15.7-35-35-35c-31.9 0-58.8-37.7-58.8-82.4s26.9-82.4 58.8-82.4c19.3 0 35-15.7 35-35s-15.7-35-35-35c-35.4 0-69.5 17.3-93.7 47.5-22.6 28.4-35.1 65.6-35.1 104.9s12.5 76.5 35.1 104.9c0.6 0.8 1.3 1.6 2 2.4-22.9 13.2-43.4 31-61.1 53.2-38.3 48-59.5 111.4-59.5 178.4 0 19.3 15.7 35 35 35s35-15.7 35-35c0-51.2 15.7-99.1 44.1-134.7 26.6-33.3 61.3-51.7 97.7-51.7 0.2 0 0.3-0.1 0.3-0.1zM914.8 530.7c-17.7-22.2-38.2-40-61.1-53.2 0.7-0.8 1.3-1.6 2-2.4 22.6-28.4 35.1-65.6 35.1-104.9s-12.5-76.5-35.1-104.9c-24.1-30.2-58.3-47.5-93.7-47.5-19.3 0-35 15.7-35 35s15.7 35 35 35c31.9 0 58.8 37.7 58.8 82.4s-26.9 82.4-58.8 82.4c-19.3 0-35 15.7-35 35s15.7 35 35 35H762.3c36.4 0 71.1 18.4 97.7 51.7 28.5 35.6 44.1 83.5 44.1 134.7 0 19.3 15.7 35 35 35s35-15.7 35-35c0.2-67-21-130.3-59.3-178.3z" p-id="5771" fill="#8a8a8a"></path><path d="M747.4 541c-30.9-30.8-66.9-54.7-106.1-70.8 40.3-34.7 65.8-86 65.8-143.2 0-104.2-84.8-188.9-188.9-188.9-104.2 0-188.9 84.8-188.9 188.9 0 57 25.4 108.2 65.4 142.9C355.2 486 318.9 510 287.8 541c-61.2 61.1-95.1 142.4-95.5 228.9V850.8c0 19.3 15.7 35 35 35h580.5c19.3 0 35-15.7 35-35v-80.2-0.8c-0.3-86.4-34.2-167.6-95.4-228.8zM518.2 208.1c65.6 0 118.9 53.4 118.9 118.9s-53.4 118.9-118.9 118.9S399.3 392.6 399.3 327s53.4-118.9 118.9-118.9z m254.7 607.8H262.3v-44.6-0.6c0.2-140.5 114.8-254.8 255.3-254.8s255 114.3 255.3 254.8v45.2z" p-id="5772" fill="#8a8a8a"></path></svg>
                    <p class="text">总会员数</p>
                    <p class="number">{{ homeData.totalUser }}</p>
                  </div>
                </el-col>
                <el-col class="item second-line" :span="6">
                  <div class="do">
                    <svg t="1641969647517" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3586" width="64" height="64"><path d="M128 544h128v288H128zM320 480h128v352H320zM512 320h128v512H512zM704 416h128v416H704z" p-id="3587" fill="#8a8a8a"></path><path d="M576 128L352 352h448L576 128z" p-id="3588" fill="#8a8a8a"></path></svg>
                    <p class="text">总交易金额（元）</p>
                    <p class="number">{{ homeData.totalPay ? homeData.totalPay : '0.00'}}</p>
                  </div>
                </el-col>
                <el-col class="item second-line" :span="6">
                  <div class="do">
                    <svg t="1641957411945" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4780" width="64" height="64"><path d="M725.504 768C725.290667 768 725.333333 170.709333 725.333333 170.709333L640 170.666667V128h85.376C748.928 128 768 146.986667 768 170.709333v597.248A42.666667 42.666667 0 0 1 725.504 810.666667H170.496C147.029333 810.666667 128 791.68 128 767.957333V170.709333C128 147.114667 147.157333 128 170.624 128H256v42.666667H170.624L170.666667 767.957333 725.504 768z m124.949333-469.333333H810.666667v-42.666667h42.666666c23.573333 0 42.666667 18.986667 42.666667 42.709333v597.248A42.666667 42.666667 0 0 1 853.504 938.666667H298.496A42.453333 42.453333 0 0 1 256 896v-42.666667h42.666667v42.666667l554.666666-0.042667 0.021334-228.416c-0.042667-168.597333-0.021333-368.853333-0.021334-368.853333L850.453333 298.666667zM298.666667 341.333333h298.666666v42.666667H298.666667v-42.666667z m0 128h213.333333v42.666667H298.666667v-42.666667z m0-341.376C298.666667 104.405333 317.802667 85.333333 341.461333 85.333333h213.077334C578.176 85.333333 597.333333 104.490667 597.333333 127.957333v42.752A42.688 42.688 0 0 1 554.538667 213.333333h-213.077334A42.752 42.752 0 0 1 298.666667 170.709333V127.957333z m42.666666 42.752L554.538667 170.666667 554.666667 127.957333 341.461333 128 341.333333 170.709333z" fill="#8a8a8a" p-id="4781"></path></svg>
                    <p class="text">总订单数</p>
                    <p class="number">{{ homeData.totalOrder }}</p>
                  </div>
                </el-col>
                <el-col class="item second-line" :span="6">
                  <div class="do">
                    <svg t="1641958462543" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="13815" width="64" height="64"><path d="M983.696 422.704A480 480 0 1 0 40.72 602.72a469.456 469.456 0 0 0 69.44 172.688 36.4 36.4 0 1 0 60.672-40.24 408.16 408.16 0 0 1 3.664-452.224 400.832 400.832 0 0 1 260.24-171.2 407.136 407.136 0 1 1-112.576 760.288 36.8 36.8 0 0 0-34.352 65.12 477.024 477.024 0 0 0 223.68 54.88 467.984 467.984 0 0 0 89.92-8.784A481.424 481.424 0 0 0 983.712 422.72z" p-id="13816" fill="#8a8a8a"></path><path d="M471.712 579.84h-130.768a36.944 36.944 0 0 0 0 73.888h130.768v116.8a36.944 36.944 0 1 0 73.888 0v-116.8h130.768a36.944 36.944 0 1 0 0-73.888H545.6v-71.664a22.784 22.784 0 0 0-0.736-7.392h131.504a36.944 36.944 0 1 0 0-73.888H341.68a36.944 36.944 0 1 0 0 73.888h131.504a25.152 25.152 0 0 0-0.736 7.392v71.664z" p-id="13817" fill="#8a8a8a"></path><path d="M405.968 383.312a37.104 37.104 0 0 0 52.464-52.464l-79.792-79.792a37.104 37.104 0 1 0-52.464 52.464z" p-id="13818" fill="#8a8a8a"></path><path d="M602.48 383.312l79.792-79.792a37.088 37.088 0 1 0-52.448-52.464l-79.792 79.792a36.576 36.576 0 0 0 0 52.464 37.936 37.936 0 0 0 52.448 0z" p-id="13819" fill="#8a8a8a"></path></svg>
                    <p class="text">总支付人数</p>
                    <p class="number">{{ homeData.totalPayUser }}</p>
                  </div>
                </el-col>
              </el-row>
          </div>
      </div>
      <div class="overview">
          <div class="title">常用功能</div>
          <div class="content">
            <el-row class="line home-tools">
                <el-col class="item no-border" :span="8"><div class="do" @click="toCashier('/cashier/index')"><img class="t-icon" src="@/assets/images/home/pay.png"><div class="text">收银下单</div></div></el-col>
                <el-col class="item no-border" :span="8"><div class="do" @click="toTarget('/coupon/confirm/index')"><img class="t-icon" src="@/assets/images/home/coupon.png"><div class="text">核销卡券</div></div></el-col>
                <el-col class="item no-border" :span="8"><div class="do" @click="toTarget('/member/index')"><img class="t-icon" src="@/assets/images/home/member.png"><div class="text">会员管理</div></div></el-col>
            </el-row>
            <el-row class="line home-tools">
                <el-col class="item no-border" :span="8"><div class="do" @click="toTarget('/order/index')"><img class="t-icon" src="@/assets/images/home/order.png"><div class="text">订单管理</div></div></el-col>
                <el-col class="item no-border" :span="8"><div class="do" @click="toTarget('/coupon/coupon/index')"><img class="t-icon" src="@/assets/images/home/card.png"><div class="text">卡券管理</div></div></el-col>
                <el-col class="item no-border" :span="8"><div class="do" @click="toTarget('/goods/goods/index')"><img class="t-icon" src="@/assets/images/home/goods.png"><div class="text">商品管理</div></div></el-col>
            </el-row>
          </div>
      </div>
      <div class="overview">
        <div class="title">报表统计</div>
        <div class="content">
          <el-row>
            <el-col class="item" :span="12">
               <commonChart v-if="chartData1.length > 0" :title="chart1.title" :color="chart1.color" :chart-type="chart1.chartType" :head-list="chart1.header" :data-list="chartData1" width="100%" id="chart1" height="400px"/>
            </el-col>
            <el-col class="item" :span="12">
               <commonChart v-if="chartData2.length > 0" :title="chart2.title" :color="chart2.color" :chart-type="chart2.chartType" :head-list="chart2.header" :data-list="chartData2" width="100%" id="chart2" height="400px"/>
            </el-col>
          </el-row>
        </div>
      </div>
  </div>
</template>

<script>
import { getHomeData, getStatisticData } from "@/api/home";
import commonChart from './components/charts/index';
export default {
  name: "HomePage",
  components: {
    commonChart
  },
  data() {
    return {
       loading: false,
       homeData: { todayUser: 0, totalUser: 0, todayOrder: 0, totalOrder: 0, todayPay: 0, totalPay: 0, todayActiveUser: 0, totalPayUser: 0 },
       chart1: { title: '近七日订单数量', color: '#ff5b57', chartType: 'bar', header: ['订单统计'] },
       chart2: { title: '近七日会员活跃数', color: '#EE9A49', chartType: 'line', header: ['会员统计'] },
       chartData1: [],
       chartData2: [],
    };
  },
  created() {
    this.getHomeData();
    this.getChartsData();
  },
  methods: {
    // 查询首页数据
    getHomeData() {
      this.loading = true;
      getHomeData().then(response => {
          this.homeData = response.data;
          this.loading = false
        }
      );
    },
    // 查询统计数据
    getChartsData() {
      const app = this;
      app.loading = true;
      // 近7日订单数量和活跃会员数量
      getStatisticData({ tag : 'order,user_active' }).then(response => {
          const data = response.data;
          const labelData1 = data.data[0] ? data.data[0] : [];
          const labelData2 = data.data[1] ? data.data[1] : [];
          const dataList1 = [];
          const dataList2 = [];

          data.labels.forEach(function(label, index) {
             const value1 = labelData1[index] ? labelData1[index] : 0;
             const value2 = labelData2[index] ? labelData2[index] : 0;
             dataList1.push( { name: label, value0: value1 } );
             dataList2.push( { name: label, value0: value2 } );
          })
          app.chartData1 = dataList1;
          app.chartData2 = dataList2;
          app.loading = false;
        }
      )
    },
    // 页面跳转
    toTarget(url) {
       this.$router.push( { path: url } );
    },
    // 去收银台
    toCashier(url) {
       const routeLink = this.$router.resolve({ path: url });
       window.open(routeLink.href, '_blank');
    }
  },
};
</script>

<style scoped lang="scss">
   .overview {
       min-height: 270px;
       background: #FFFFFF;
       margin-bottom: 0px;
       box-shadow: 0 1px 2px #d5d7d8;
       .title {
         margin: 10px 0px 0px 0px;
         padding: 10px 0px 0px 10px;
         height: 44px;
         border: 1px solid #e2e1e1;
         background: #f4f4f4;
         color: #333;
         font-weight: bold;
       }
       .content {
           padding: 15px;
           border: solid 1px #d5d7d8;
           .item {
               display: block;
               border-right: none;
               border-bottom: none;
               min-height: 100px;
               padding: 6px;
               text-align: center;
               cursor: pointer;
               .do {
                   border: solid 1px #cccccc;
                   padding: 20px;
                   border-radius: 2px;
               }
               .icon {
                 height: 40px;
                 width: 40px;
                 display: block;
                 padding: 5px;
                 float: left;
                 border: solid #8a8a8a 1px;
                 border-radius: 30px;
               }
             .text {
                 text-align: left;
                 margin: 0px;
                 text-indent: 10px;
                 font-size: 14px;
             }
             .number {
                 text-align: left;
                 margin: 0px;
                 font-weight: bold;
                 text-indent: 10px;
                 font-size: 18px;
                 color: #ff5b57;
             }
           }
           .home-tools {
             .do {
                 height: 90px;
                 margin-bottom: 10px;
                 cursor: pointer;
                 font-weight: bold;
                 color: #666666;
                 border-radius: 2px;
                 text-align: left;
                 padding: 20px 0px 10px 20px;
                 float: left;
                 width: 100%;
                 background: #ffffff;
                 border: solid 1px #cccccc;
                 .text {
                     margin-top: 14px;
                 }
             }
             .t-icon {
                 width: 50px;
                 height: 50px;
                 float: left;
             }
           }
       }
   }
</style>

