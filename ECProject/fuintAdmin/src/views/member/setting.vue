<template>
  <div class="app-container">
    <div class="main-panel">
      <el-form ref="form" :model="form" :rules="rules" label-width="200px" style="margin-top: 20px;">
        <el-row>
          <el-col :span="24">
            <el-form-item label="领券是否需要手机号" prop="getCouponNeedPhone">
              <el-select v-model="form.getCouponNeedPhone" placeholder="领券是否需要手机号">
                <el-option key="true" label="是" value="true"/>
                <el-option key="false" label="否" value="false"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="提交订单是否需要手机号" prop="submitOrderNeedPhone">
              <el-select v-model="form.submitOrderNeedPhone" placeholder="提交订单是否需要手机号">
                <el-option key="true" label="是" value="true"/>
                <el-option key="false" label="否" value="false"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="登录是否需要手机号" prop="loginNeedPhone">
              <el-select v-model="form.loginNeedPhone" placeholder="提交订单是否需要手机号">
                <el-option key="true" label="是" value="true"/>
                <el-option key="false" label="否" value="false"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="footer">
        <el-button type="primary" @click="submitForm">确定</el-button>
        <el-button @click="cancel">取消</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { getMemberSetting, saveSetting } from "@/api/member";
export default {
  name: "MemberSetting",
  data() {
    return {
      // 遮罩层
      loading: false,
      // 表单参数
      form: { getCouponNeedPhone: "false", submitOrderNeedPhone: "false", loginNeedPhone: "false" },
      // 表单校验
      rules: {
        getCouponNeedPhone: [
          { required: true, message: "请选择", trigger: "blur" },
        ],
        submitOrderNeedPhone: [
          { required: true, message: "请选择", trigger: "blur" },
        ],
        loginNeedPhone: [
          { required: true, message: "请选择", trigger: "blur" },
        ],
      }
    };
  },
  created() {
    this.getSettingInfo();
  },
  methods: {
    // 查询配置
    getSettingInfo() {
      this.loading = true;
      getMemberSetting().then(response => {
          this.form = response.data;
          this.loading = false;
        }
      );
    },
    // 取消按钮
    cancel() {
      this.$store.dispatch('tagsView/delView', this.$route)
      this.$router.push('/')
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
