<template>
  <div class="app-container">
    <div class="main-panel">
      <el-form ref="form" class="content" :model="form" :rules="rules" label-width="200px">
        <el-row>
          <el-col :span="12">
            <el-form-item class="recharge-item" prop="pointNeedConsume" label="返1积分所需消费金额">
              <el-input v-model="form.pointNeedConsume" placeholder="返1积分所需消费金额" maxlength="10" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item class="recharge-item" prop="canUsedAsMoney" label="积分是否可当作现金使用">
              <el-radio-group v-model="form.canUsedAsMoney">
                <el-radio key="true" label="true" value="true">是</el-radio>
                <el-radio key="false" label="false" value="false">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item class="recharge-item" prop="exchangeNeedPoint" label="多少积分可抵扣1元现金">
              <el-input v-model="form.exchangeNeedPoint" placeholder="多少积分可抵扣1元现金" maxlength="10" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item class="recharge-item" prop="rechargePointSpeed" label="充值返积分倍数">
              <el-input v-model="form.rechargePointSpeed" placeholder="充值返积分倍数" maxlength="10" />
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
import { getSettingInfo, saveSetting } from "@/api/point";

export default {
  name: "PointSetting",
  data() {
    return {
      // 遮罩层
      loading: false,
      // 表单参数
      form: { pointNeedConsume: '', canUsedAsMoney: 'false', exchangeNeedPoint: '', rechargePointSpeed: '', status: 'A' },
      // 表单校验
      rules: {
        pointNeedConsume: [
          { required: true, message: "请输入", trigger: "blur" },
          { pattern: /^[0-9]*$$/ , message: `必须输入正整数`, trigger: 'blur' }
        ],
        canUsedAsMoney: [
          { required: true, message: "请输入", trigger: "blur" }
        ],
        exchangeNeedPoint: [
          { required: true, message: "请输入", trigger: "blur" },
          { pattern: /^[0-9]*$/ , message: `正整数或含一位小数数字`, trigger: 'blur' }
        ],
        rechargePointSpeed: [
          { required: true, message: "请输入", trigger: "blur" },
          { pattern: /^[+]?((\d*(\.\d{1,1})$)|([1-9]\d*$))/ , message: `正整数或含一位小数数字`, trigger: 'blur' }
        ],
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
          this.form.pointNeedConsume = response.data.pointNeedConsume;
          this.form.canUsedAsMoney = response.data.canUsedAsMoney;
          this.form.exchangeNeedPoint = response.data.exchangeNeedPoint;
          this.form.rechargePointSpeed = response.data.rechargePointSpeed;
          this.form.status = response.data.status;
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
}
</style>
