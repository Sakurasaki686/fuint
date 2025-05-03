<template>
  <!-- 积分变更对话框 -->
  <el-dialog title="无商品收款" :visible.sync="showDialog" class="common-dialog" width="700px" @close="close" destroy-on-close>
    <el-form ref="form" :model="form" :rules="rules" label-width="120px">
      <el-row>
        <el-col :span="24">
          <el-form-item label="收款金额：" prop="amount">
            <el-input v-model="form.amount" placeholder="请输入收款金额" style="width: 200px" maxlength="150" />
            <div class="form-tips">( 单位：元 )</div>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <el-form-item class="recharge-item" prop="remark" label="备注信息：">
            <el-input v-model="form.remark" type="textarea" rows="3" placeholder="请输入备注信息" maxlength="255" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" class="main-button" @click="submitForm">确定收款</el-button>
      <el-button @click="close">取消</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: "noGoodsCashier",
  props: {
    showDialog:{
      type:[Boolean],
      default:()=>false
    }
  },
  data() {
    return {
      // 遮罩层
      loading: false,
      // 表单参数
      form: {  amount: '', remark: '' },
      // 表单校验
      rules: {
        amount: [
          { required: true, message: "金额不能为空", trigger: "blur" },
          { pattern: /^[0-9]{1,10}$/, message: `请输入1-10位数字`, trigger: 'blur' }
        ]
      }
    };
  },
  watch: {
    showDialog(value) {
      if (value) {
          this.form.amount = '';
          this.form.remark = '';
      }
    }
  },
  methods: {
    // 取消按钮
    close() {
      this.$emit('closeDialog', 'openNoGoodsCashierDialog');
    },
    // 重置表单
    reset() {
      this.form.amount = '';
      this.form.remark = '';
    },
    // 提交按钮
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
            this.$emit('submit', this.form);
        }
      });
    }
  }
};
</script>
