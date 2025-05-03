<template>
  <!-- 充值对话框 -->
  <el-dialog class="common-dialog" title="余额充值/扣减" :visible.sync="showDialog" width="700px" @close="cancel" destroy-on-close>
    <el-form ref="form" :model="form" :rules="rules" label-width="120px">
      <el-row>
        <el-col :span="24">
          <el-form-item label="会员信息：">
            <span>{{ memberInfo.name }}（ID:{{ memberInfo.id }}）</span>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <el-form-item label="可用余额：" prop="balance">
            <span>￥{{ memberInfo.balance.toFixed(2) }}</span>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <el-form-item label="变更类型：" prop="type">
            <el-radio-group v-model="form.type">
              <el-radio key="1" label="1" value="1">增加</el-radio>
              <el-radio key="2" label="2" value="2">扣减</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <el-form-item label="变更金额：" prop="amount">
            <el-input v-model="form.amount" placeholder="请输入变更金额" style="width: 200px" maxlength="100" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <el-form-item class="recharge-item" prop="remark" label="备注信息：">
            <el-input v-model="form.remark" type="textarea" rows="2" placeholder="请输入备注信息" maxlength="255" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="submitForm">确定</el-button>
      <el-button @click="cancel">取消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { doRecharge } from "@/api/balance";
import { getMemberInfo } from "@/api/member";
export default {
  name: "balanceRecharge",
  props: {
    showDialog:{
      type:[Boolean],
      default:()=>false
    },
    userId:{
      type:[String],
      default:()=> ''
    }
  },
  data() {
    return {
      // 遮罩层
      loading: false,
      memberInfo: { name: '', id: '', balance: 0 },
      // 表单参数
      form: { type: '1', amount: '', userId: '' },
      // 表单校验
      rules: {
        userId: [
          { required: true, message: "请选择会员", trigger: "blur" },
        ],
        amount: [
          { required: true, message: "金额不能为空", trigger: "blur" },
          { min: 1, max: 6, message: '请在1至99999元范围内输入', trigger: 'blur' },
          { pattern: /((^[1-9]\d*)|^0)(\.\d{0,2}){0,1}$/, message: `请输入正确的金额`, trigger: 'blur' }
        ]
      }
    };
  },
  watch: {
    showDialog(value) {
      if (value) {
          this.getMemberInfo()
      }
    }
  },
  methods: {
    // 查询会员信息
    getMemberInfo() {
      this.loading = true;
      getMemberInfo(this.userId).then(response => {
          this.form.userId = response.data.memberInfo.id;
          this.memberInfo = response.data.memberInfo;
          this.loading = false;
        }
      );
    },
    // 取消按钮
    cancel() {
      this.$emit('closeDialog','balance');
    },
    // 充值表单
    reset() {
      this.form.userId = '';
      this.form.type = '1';
      this.form.amount = '';
      this.form.remark = '';
      this.memberInfo = { name: '', id: '', balance: 0 };
    },
    // 提交按钮
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
            doRecharge(this.form).then(response => {
               this.$alert("余额操作成功！");
               this.$emit('closeDialog','balance');
               this.reset();
            });
        }
      });
    }
  }
};
</script>
