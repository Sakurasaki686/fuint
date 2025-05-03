<template>
  <div class="app-container">
    <el-form :model="queryParams" class="main-search" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="卡券ID" prop="couponId">
        <el-input
          v-model="queryParams.id"
          placeholder="请输入卡券ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="分组ID" prop="groupId">
        <el-input
          v-model="queryParams.groupId"
          placeholder="请输入卡券分组ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入卡券名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select
          v-model="queryParams.type"
          clearable
          placeholder="卡券类型"
        >
          <el-option v-for="typeInfo in typeList" :key="typeInfo.key" :label="typeInfo.name" :value="typeInfo.key"/>
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
        >
          <el-option v-for="statusInfo in statusList" :key="statusInfo.key" :label="statusInfo.name" :value="statusInfo.key"/>
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
          v-hasPermi="['coupon:coupon:add']"
        >新增</el-button>
      </el-form-item>
    </el-form>

    <el-table ref="tables" v-loading="loading" :data="list" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange">
      <el-table-column label="卡券ID" prop="id" width="66"/>
      <el-table-column label="图标" align="center" width="150">
        <template slot-scope="scope">
          <img class="list-img" :src="imagePath + scope.row.image">
        </template>
      </el-table-column>
      <el-table-column label="卡券名称" align="center" prop="name">
        <template slot-scope="scope">
          <span v-if="scope.row.name">{{ scope.row.name }}</span>
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column label="分组" align="center" prop="groupName">
        <template slot-scope="scope">
          <span v-if="scope.row.groupId">
              <span>{{ getName(groupList, scope.row.groupId) }}</span>
          </span>
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column label="类型" align="center" prop="type">
        <template slot-scope="scope">
          <span v-if="scope.row.type">
            <span>{{ getName(typeList, scope.row.type) }}</span>
          </span>
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column label="有效期" align="center" width="180" prop="updateTime">
        <template slot-scope="scope">
          <p v-if="scope.row.expireType == 'fix'">{{ parseTime(scope.row.beginTime) }}</p>
          <p v-if="scope.row.expireType == 'fix'">{{ parseTime(scope.row.endTime) }}</p>
          <p v-if="scope.row.expireType == 'flex'">领取后{{ scope.row.expireTime }}天</p>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" width="160" prop="createTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <span>{{ getName(statusList, scope.row.status) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-plus"
            v-hasPermi="['coupon:coupon:edit']"
            @click="handleSend(scope.row)"
          >发券</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            v-hasPermi="['coupon:coupon:edit']"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            v-hasPermi="['coupon:coupon:edit']"
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

    <!-- 添加或修改卡券对话框 start-->
    <el-dialog :title="title" :visible.sync="open" class="common-dialog" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="140px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="卡券类型" prop="type">
              <el-select
                v-if="form.id"
                v-model="form.type"
                style="width: 260px"
                placeholder="卡券类型"
                disabled
              >
                <el-option v-for="typeInfo in typeList" :key="typeInfo.key" :label="typeInfo.name" :value="typeInfo.key"/>
              </el-select>
              <el-select
                v-if="!form.id"
                v-model="form.type"
                style="width: 260px"
                placeholder="卡券类型"
              >
                <el-option v-for="typeInfo in typeList" :key="typeInfo.key" :label="typeInfo.name" :value="typeInfo.key"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="卡券分组" prop="groupId">
              <el-select
                v-model="form.groupId"
                style="width: 260px"
                placeholder="请选择卡券分组"
                clearable
                filterable
              >
                <el-option v-for="group in groupOptions" :key="group.id" :label="group.name" :value="group.id"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="卡券名称" prop="name">
              <el-input v-model="form.name" style="width: 300px" placeholder="请输入卡券名称" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="过期类型" prop="expireType">
              <el-radio-group v-model="form.expireType">
                <el-radio key="fix" label="fix" value="fix">固定时间</el-radio>
                <el-radio key="flex" label="flex" value="flex">领取后生效</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="form.expireType == 'fix'">
          <el-col :span="24">
            <el-form-item label="有效时间" prop="beginTime">
              <el-date-picker
                v-model="form.beginTime"
                value-format="yyyy-MM-dd HH:mm:ss"
                type="datetime"
                placeholder="开始时间"
              ></el-date-picker>
              <span class="sp"> 至 </span>
              <el-date-picker
                v-model="form.endTime"
                value-format="yyyy-MM-dd HH:mm:ss"
                type="datetime"
                placeholder="结束时间"
              ></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="form.expireType == 'flex'">
          <el-col :span="24">
            <el-form-item label="有效天数" prop="expireTime">
              <el-input v-model="form.expireTime" style="width: 150px" placeholder="请输入有效天数" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 优惠券特有属性 start -->
        <div v-if="form.type == 'C'">
          <el-row>
            <el-col :span="24">
              <el-form-item label="卡券面额" prop="amount">
                <el-input v-model="form.amount" style="width: 240px" placeholder="请输入卡券面额，单位：元" maxlength="10" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="发放方式" prop="sendWay">
                <el-radio-group v-model="form.sendWay">
                  <el-radio key="backend" label="backend" value="backend">后台发放</el-radio>
                  <el-radio key="front" label="front" value="front">前台领取</el-radio>
                  <el-radio key="offline" label="offline" value="offline">线下发放</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="发行总数量" prop="total">
                <el-input v-model="form.total" style="width: 300px" placeholder="请输入发行总数量，单位：套"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="满多少可用" prop="outRule">
                <el-input v-model="form.outRule" style="width: 300px" placeholder="单位：元，输入0表示无门槛"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="允许转赠好友" prop="isGive">
                <el-radio-group v-model="form.isGive">
                  <el-radio key="0" label="0" value="0">不允许</el-radio>
                  <el-radio key="1" label="1" value="1">允许</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="适用商品" prop="applyGoods">
                <el-radio-group v-model="form.applyGoods">
                  <el-radio key="allGoods" label="allGoods" value="allGoods">全场通用</el-radio>
                  <el-radio key="parkGoods" label="parkGoods" value="parkGoods">指定商品</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24" v-if="form.applyGoods == 'parkGoods'">
              <el-form-item label="适用商品ID" prop="goodsIds">
                <el-input v-model="goodsIds" rows="2" type="textarea" placeholder="英文逗号分隔，如：19932,938423,324234"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="每人最多拥有数量" prop="limitNum">
                <el-input v-model="form.limitNum" style="width: 300px" placeholder="请输入每人最多拥有数量，单位：张"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="领取所需积分" prop="point">
                <el-input v-model="form.point" style="width: 300px" placeholder="输入0或为空表示不需要积分领取"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="领取码" prop="receiveCode">
                <el-input v-model="form.receiveCode" style="width: 300px" placeholder="为空表示不需要领取码"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="会员等级" prop="gradeIds">
                <el-select
                  v-model="form.gradeIds"
                  style="width: 260px"
                  placeholder="适用会员等级"
                  clearable
                  filterable
                  multiple
                >
                  <el-option v-for="grade in gradeOptions" :key="grade.id+''" :label="grade.name" :value="grade.id+''"/>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="使用专项" prop="useFor">
                <el-radio-group v-model="form.useFor">
                  <el-select
                    v-model="form.useFor"
                    style="width: 260px"
                    placeholder="请选择使用专项，不选则为通用"
                    clearable
                  >
                    <el-option v-for="useForItem in couponUseForList" :key="useForItem.key+''" :label="useForItem.name" :value="useForItem.key+''"/>
                  </el-select>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        <!-- 优惠券特有属性 end -->
        <!-- 储值卡特有属性 start -->
        <div v-if="form.type == 'P'">
          <el-row>
            <el-col :span="24">
              <el-form-item label="预存规则" prop="storeItem">
                <div class="add-item"><el-button type="danger" size="mini" icon="el-icon-plus" @click="addStoreItem()">添加</el-button></div>
                <div class="store-item" v-for="(item, index) in storeItem">
                    <el-input v-model="item.storeAmount" style="width: 180px" placeholder="请输入实际支付金额" maxlength="100" />
                    <span class="sp"> 到账 </span>
                    <el-input v-model="item.giveAmount" style="width: 180px" placeholder="请输入实际到账金额" maxlength="100" />
                    <span class="remove-item el-icon-remove" @click="removeStoreItem(index)"></span>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        <!-- 储值卡特有属性 end -->
        <!-- 计次卡特有属性 start -->
        <div v-if="form.type == 'T'">
          <el-row>
            <el-col :span="24">
              <el-form-item label="计次次数" prop="outRule">
                <el-input v-model="form.outRule" style="width: 260px" placeholder="请输入计次次数" maxlength="10" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="发放方式" prop="sendWay">
                <el-radio-group v-model="form.sendWay">
                  <el-radio key="backend" label="backend" value="backend">后台发放</el-radio>
                  <el-radio key="front" label="front" value="front">前台领取</el-radio>
                  <el-radio key="offline" label="offline" value="offline">线下发放</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="领取所需积分" prop="point">
                <el-input v-model="form.point" style="width: 300px" placeholder="输入0或为空表示不需要积分领取"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="领取码" prop="receiveCode">
                <el-input v-model="form.receiveCode" style="width: 300px" placeholder="为空表示不需要领取码"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        <!-- 计次卡特有属性 end -->
        <el-row>
          <el-col :span="24">
            <el-form-item label="适用店铺" prop="storeIds">
              <el-select
                v-model="form.storeIds"
                style="width: 260px"
                placeholder="适用店铺"
                multiple
              >
                <el-option v-for="storeInfo in storeList" :key="storeInfo.id+''" :label="storeInfo.name" :value="storeInfo.id+''"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="图标" prop="image">
              <el-upload
                 :action="uploadAction"
                 list-type="picture-card"
                 :file-list="uploadFiles"
                 :auto-upload="true"
                 :on-success="handleUploadSuccess"
                 :headers="uploadHeader"
                 :show-file-list="false">
                <img
                  v-if="this.form.image"
                  :src="imagePath + this.form.image"
                  class="list-img"
                />
                 <i class="el-icon-plus"></i>
              </el-upload>
            </el-form-item>
          </el-col>
          <p class="form-tips">（提示：点击图片修改，建议尺寸：300 x 240）</p>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="描述信息">
              <el-input v-model="form.description" rows="5" type="textarea" placeholder="请输入内容"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="后台备注">
              <el-input v-model="form.remarks" rows="2" type="textarea" placeholder="请输入内容"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确定</el-button>
        <el-button @click="cancel">取消</el-button>
      </div>
    </el-dialog>
    <!-- 添加或修改卡券对话框 end-->

    <!--发券对话框 start-->
    <el-dialog title="发放卡券" :visible.sync="openSendCouponDialog" class="common-dialog" width="800px" append-to-body>
      <el-form ref="sendForm" :model="sendForm" :rules="sendFormRules" label-width="120px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="卡券名称：" prop="couponName">
              <el-input style="width: 420px;" v-model="sendForm.couponName" disabled maxlength="100"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="发放对象">
              <el-radio-group v-model="sendForm.object" @change="objectChange()">
                <el-radio key="part" label="part" value="part">部分会员</el-radio>
                <el-radio key="all" label="all" value="all">全部会员</el-radio>
                <el-button type="danger" size="mini" v-if="sendForm.object == 'all'">总会员数：{{ totalMember }}</el-button>
                <el-button type="primary" size="mini" v-if="sendForm.object == 'part'" @click="selectUser()">选择会员（已选{{ sendForm.memberIds.length }}人）</el-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="发放数量：" prop="num">
              <el-input style="width: 420px;" v-model="sendForm.num"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注信息：" prop="remark">
              <el-input v-model="sendForm.remark" type="textarea" rows="2" placeholder="请输入备注信息"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" v-loading="sendLoading" @click="doSendCoupon">确定发放</el-button>
        <el-button @click="cancelSend">取消</el-button>
      </div>
    </el-dialog>
    <!--发券对话框 end-->

    <!--会员选择对话框start-->
    <UserSelect :showDialog="openUserSelect" @doSelectUser="doSelectUser" @closeDialog="closeDialog"></UserSelect>
    <!--会员选择对话框end-->
  </div>
</template>

<script>
import { getToken } from '@/utils/auth';
import { getCouponList, updateCouponStatus, getCouponInfo, saveCoupon, deleteCoupon, sendCoupon } from "@/api/coupon/coupon";
import { getAllGroupList } from "@/api/coupon/group";
import { getTotalMember } from "@/api/statistic";
import { parseTime } from "@/utils/fuint";

// 初始表单
const initForm = { id: '', type: 'C', groupId: '', gradeIds: '', name: '', expireType: 'fix', expireTime: '', beginTime: '', endTime: '', applyGoods: 'allGoods', isGive: '0', amount: '', goodsIds: '',
                   outRule: '', inRule: '', inRuleForPreStore: '', sendNum: '1', total: '', limitNum: '', sendWay: 'backend', point: '', receiveCode: '',
                   useFor: '', storeIds: '', image: '/static/defaultImage/coupon.png', description: '', remarks: '', outRuleForTimer: '', status: "A" };

export default {
  name: "CouponIndex",
  data() {
    const storeItemValid = (rule, value, callback) => {
      // 储值卡验证
      if (this.form.type == 'P') {
        if (this.storeItem.length > 0) {
            let validCount = 0;
            this.storeItem.forEach(function(item) {
              if (item.storeAmount == '' || isNaN(parseFloat(item.storeAmount)) || item.giveAmount == '' || isNaN(parseFloat(item.giveAmount))) {
                  validCount++;
              }
            })
            if (validCount > 0) {
                callback(new Error("请输入预存规则"));
            } else {
                callback();
            }
        } else {
             callback(new Error("请添加预存规则"));
        }
      }
    };
    return {
      // 遮罩层
      loading: true,
      sendLoading: false,
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
      // 卡券类别列表
      typeList: [],
      // 分组列表
      groupList: [],
      // 分组选项
      groupOptions: [],
      // 等级选项
      gradeOptions: [],
      // 店铺列表
      storeList: [],
      // 状态列表
      statusList: [],
      // 卡券使用专项列表
      couponUseForList: [],
      // 是否显示弹出层
      open: false,
      // 显示发券对话框
      openSendCouponDialog: false,
      // 默认排序
      defaultSort: {prop: 'createTime', order: 'descending'},
      // 表单参数
      form: initForm,
      goodsIds: '',
      // 预存规则
      storeItem: [ { storeAmount: '', giveAmount: '' }],
      // 上传地址
      uploadAction: process.env.VUE_APP_SERVER_URL + 'backendApi/file/upload',
      // 隐藏上传
      hideUpload: false,
      uploadHeader: { 'Access-Token' : getToken() },
      // 选择会员对话框
      openUserSelect: false,
      // 上传文件列表
      uploadFiles: [],
      // 查询参数
      queryParams: {
        page: 1,
        pageSize: 10,
        id: '',
        groupId: '',
        name: '',
        type: '',
        status: ''
      },
      // 表单校验
      rules: {
        type: [
          { required: true, message: "请选择卡券类型", trigger: "blur" },
        ],
        name: [
          { required: true, message: "卡券名称不能为空", trigger: "blur" },
        ],
        groupId: [
          { required: true, message: "请选择卡券分组", trigger: "blur" },
        ],
        expireType: [
          { required: true, message: "过期类型不能为空", trigger: "blur" },
        ],
        expireTime: [
          { required: true, message: "有效天数不能为空", trigger: "blur" },
          { pattern: /^[0-9]{1,10}$/, message: `请输入1-10位数字`, trigger: 'blur' }
        ],
        beginTime: [
          { required: true, message: "请选择开始时间", trigger: "blur" },
        ],
        endTime: [
          { required: true, message: "请选择结束时间", trigger: "blur" },
        ],
        amount: [
          { required: true, message: "请输入面额", trigger: "blur" },
        ],
        total: [
          { required: true, message: "请输入发行总数", trigger: "blur" },
          { pattern: /^[0-9]{1,10}$/, message: `请输入1-10位数字`, trigger: 'blur' }
        ],
        sendWay: [
          { required: true, message: "请选择发放方式", trigger: "blur" },
        ],
        outRule: [
          { required: true, message: "请输入该项", trigger: "blur" },
        ],
        storeItem: [
          { required: true, validator: storeItemValid, trigger: 'change' },
        ]
      },
      totalMember: 0,
      sendForm: { couponId: '', couponName: '', num: '', memberIds: [], object: 'part', remark: '' },
      sendFormRules: {
        num: [
            { required: true, message: "请输入发放数量", trigger: "blur" },
            { pattern: /^[0-9]{1,10}$/, message: `请输入1-10位数字`, trigger: 'blur' }
        ]
      }
    };
  },
  created() {
    this.getList();
    this.getGroupList();
    this.getTotalMember();
  },
  methods: {
    // 查询列表
    getList() {
      this.loading = true;
      getCouponList(this.queryParams).then( response => {
          this.list = response.data.paginationResponse.content;
          this.total = response.data.paginationResponse.totalElements;
          this.groupList = response.data.groupList;
          this.storeList = response.data.storeList;
          this.imagePath = response.data.imagePath;
          this.typeList = response.data.typeList;
          this.statusList = response.data.statusList;
          this.gradeOptions = response.data.gradeList;
          this.couponUseForList = response.data.couponUseForList;
          this.loading = false;
        }
      );
    },
    // 获取会员总数
    getTotalMember() {
      getTotalMember().then(response => {
          this.totalMember = response.data.totalMember;
        }
      );
    },
    // 获取分组列表
    getGroupList() {
      this.loading = true;
      getAllGroupList().then( response => {
          this.groupOptions = response.data.groupList;
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
      this.$modal.confirm('确认要' + text + 'ID等于' + row.id + '的数据项吗？').then(function() {
        return updateCouponStatus(row.id, row.status);
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
      this.reset();
      this.open = true;
      this.title = "新增卡券";
    },
    // 表单重置
    reset() {
      this.resetForm("form");
      this.form = initForm;
      this.goodsIds = '';
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 提交按钮
    submitForm: function() {
      const app = this;
      app.$refs["form"].validate(valid => {
        if (valid) {
          app.form.storeIds = app.form.storeIds ? app.form.storeIds.join(",") : '';
          app.form.gradeIds = app.form.gradeIds ? app.form.gradeIds.join(",") : '';
          app.form.goodsIds = app.goodsIds;
          // 预存规则
          if (app.form.type == 'P') {
              app.form.inRule = '';
              app.storeItem.forEach(function(rule){
                 if (app.form.inRule) {
                     app.form.inRule = app.form.inRule + ',' + rule.storeAmount + '_' + rule.giveAmount;
                 } else {
                     app.form.inRule = rule.storeAmount + '_' + rule.giveAmount;
                 }
              })
          }
          if (parseInt(app.form.id) > 0) {
              saveCoupon(app.form).then(response => {
                app.$modal.msgSuccess("修改成功");
                app.open = false;
                app.getList();
              });
          } else {
              saveCoupon(app.form).then(response => {
                app.$modal.msgSuccess("新增成功");
                app.open = false;
                app.getList();
              });
          }
        } else {
            app.$modal.msgError("请先完善表单数据项");
        }
      });
    },
    // 修改按钮操作
    handleUpdate(row) {
      const app = this;
      app.reset();
      const id = row.id || this.ids;
      getCouponInfo(id).then(response => {
        app.form = response.data.couponInfo;
        app.form.isGive = app.form.isGive == true ? '1' : '0';
        app.form.beginTime = parseTime(app.form.beginTime);
        app.form.endTime = parseTime(app.form.endTime);
        app.goodsIds = response.data.goodsIds;
        if (app.form.storeIds && app.form.storeIds.length > 0) {
            app.form.storeIds = app.form.storeIds.split(",");
        }
        if (app.form.gradeIds && app.form.gradeIds.length > 0) {
            app.form.gradeIds = app.form.gradeIds.split(",");
        }
        if (app.form.type == 'P' && app.form.inRule) {
           const storeRules = app.form.inRule.split(",");
           app.storeItem = [];
           if (storeRules.length > 0) {
               storeRules.forEach(function(rule){
                   const ruleItem = rule.split("_");
                   if (ruleItem.length == 2) {
                       app.storeItem.push( { storeAmount: ruleItem[0], giveAmount: ruleItem[1] } );
                   }
               })
           }
        }
        app.open = true;
        app.title = "编辑卡券";
      });
    },
    // 删除按钮操作
    handleDelete(row) {
      this.$modal.confirm('是否确认删除ID等于' + row.id + '的数据项？').then(function() {
         return deleteCoupon(row.id);
      }).then(() => {
         this.getList();
         this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    // 发券
    handleSend(row) {
       this.openSendCouponDialog = true;
       this.sendForm.couponName = row.name;
       this.sendForm.couponId = row.id;
       this.sendForm.num = row.sendNum ? parseInt(row.sendNum): '';
       this.sendForm.object = 'part';
       this.sendForm.memberIds = [];
       this.sendForm.remark = '';
    },
    // 取消发券
    cancelSend() {
      if (this.sendLoading) {
          this.$modal.msgError("请耐心等待...");
          return false;
      }
      this.openSendCouponDialog = false;
    },
    // 发放对象
    objectChange() {
      if (this.sendForm.object == 'all') {
          this.sendForm.memberIds = [];
      }
    },
    // 选择会员
    selectUser() {
      this.openUserSelect = true;
    },
    // 确定选择会员
    doSelectUser(memberIds) {
      this.sendForm.memberIds = memberIds;
      this.openUserSelect = false;
    },
    // 关闭选择会员对话框
    closeDialog() {
      this.openUserSelect = false;
    },
    // 确定发券
    doSendCoupon() {
      const app = this;
      if (app.sendLoading) {
          return false;
      }
      app.$refs["sendForm"].validate(valid => {
        if (valid) {
          app.$confirm('您确定要发放卡券吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
              app.sendLoading = true;
              const userIds = app.sendForm.memberIds ? app.sendForm.memberIds.join(",") : '';
              const param = { couponId: app.sendForm.couponId, userIds : userIds, object: app.sendForm.object, num : app.sendForm.num, remark: app.sendForm.remark };
              sendCoupon(param).then(response => {
                if (response.data) {
                    app.openSendCouponDialog = false
                    app.$modal.msgSuccess("卡券发放成功");
                    app.sendLoading = false;
                }
              }).catch(() => {
                  app.sendLoading = false;
              });;
          }).catch(() => {
              app.sendLoading = false;
          });
        }
      })
    },
    // 添加预存规则
    addStoreItem() {
      this.storeItem.push( { storeAmount: '', giveAmount: '' } );
    },
    // 删除预存规则
    removeStoreItem(index) {
      const newStoreItem = [];
      this.storeItem.forEach(function(item, i){
        if (index !== i) {
            newStoreItem.push(item);
        }
      });
      this.storeItem = newStoreItem;
    },
    // 上传成功
    handleUploadSuccess(file) {
      this.form.image = file.data.fileName
    },
    handleRemove(file, fileList) {
      setTimeout(() => {
        this.hideUpload = fileList.length > 0
      }, 520)
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
.sp {
  font-size: 12px;
  margin-left: 5px;
  margin-right: 5px;
}
.store-item {
  margin-top: 5px;
  margin-bottom: 5px;
}
.remove-item {
  color: red;
  cursor: pointer;
  margin-left: 10px;
}
</style>
