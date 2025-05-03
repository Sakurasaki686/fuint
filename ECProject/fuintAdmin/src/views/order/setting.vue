<template>
  <div class="app-container">
    <div class="main-panel">
      <el-form ref="form" class="content" :model="form" :rules="rules" label-width="200px">
        <el-row>
          <el-col :span="12">
            <el-form-item class="recharge-item" prop="isClose" label="关闭系统交易功能">
              <el-radio-group v-model="form.isClose">
                <el-radio key="false" label="false" value="false">否</el-radio>
                <el-radio key="true" label="true" value="true">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item class="recharge-item" prop="deliveryFee" label="每笔订单配送费用">
              <el-input v-model="form.deliveryFee" placeholder="每笔订单配送费用，单位：元" maxlength="10" />
              <span class="tips">单位：元</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="footer">
        <el-button type="primary" @click="submitForm">保 存</el-button>
        <el-button @click="cancel">取消</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { getSettingInfo, saveSetting } from "@/api/order";

export default {
  name: "OrderSetting",
  data() {
    return {
      // 遮罩层
      loading: false,
      // 表单参数
      form: { deliveryFee: '', isClose: 'false' },
      // 表单校验
      rules: {
        deliveryFee: [
          { required: true, message: "请输入", trigger: "blur" },
          { min: 0, max: 4, message: `请输入1000以内数字`, trigger: 'blur' }
        ],
        isClose: [
          { required: true, message: "请输入", trigger: "blur" },
        ]
      }
    };
  },
  created() {
    this.getSettingInfo();
  },
  activated() {
    this.getSettingInfo();
  },
  methods: {
    // 查询账户列表
    getSettingInfo() {
      this.loading = true;
      getSettingInfo(this.queryParams).then(response => {
          this.form.deliveryFee = response.data.deliveryFee;
          this.form.isClose = response.data.isClose;
          this.loading = false;
        }
      );
    },
    // 取消按钮
    cancel() {
      this.$store.dispatch('tagsView/delView', this.$route)
      this.$router.push( { path: '/point/list' } );
    },
    // 提交按钮
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
            saveSetting(this.form).then(response => {
              this.$modal.msgSuccess("保存成功");
              this.getSettingInfo();
            });
        }
      });
    }
  }
};
</script>
<style rel="stylesheet/scss" lang="scss">
.main-panel {
  .content {
    margin-left: 50px;
    margin-top: 30px;
  }
  .footer {
    margin-top: 10px;
    margin-left: 250px;
  }
  .tips {
    font-size: 12px;
    color: #888888;
  }
}
</style>
