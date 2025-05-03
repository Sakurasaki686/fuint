<template>
  <!-- 积分变更对话框 -->
  <el-dialog title="积分变更" :visible.sync="showDialog" class="common-dialog" width="700px" @close="cancel" append-to-body destroy-on-close>
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
          <el-form-item label="可用积分：" prop="balance">
            <span>￥{{ memberInfo.point }}</span>
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
          <el-form-item label="变更数量：" prop="amount">
            <el-input v-model="form.amount" placeholder="请输入变更数量" style="width: 200px" maxlength="100" />
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
      <el-button type="primary" class="main-button" @click="submitForm">确 定</el-button>
      <el-button @click="cancel">取 消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { doRecharge } from "@/api/point";
import { getMemberInfo } from "@/api/member";
export default {
  name: "pointRecharge",
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
      memberInfo: { name: '', id: '', point: 0 },
      // 表单参数
      form: { type: '1', amount: '', userId: '' },
      // 表单校验
      rules: {
        userId: [
          { required: true, message: "请选择会员", trigger: "blur" },
        ],
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
      ).catch(() => {
        // empty
      });
    },
    // 取消按钮
    cancel() {
      this.$emit('closeDialog','point');
    },
    // 充值表单
    reset() {
      this.form.userId = '';
      this.form.type = '1';
      this.form.amount = '';
      this.form.remark = '';
      this.memberInfo = { name: '', id: '', point: 0 };
    },
    // 提交按钮
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
            doRecharge(this.form).then(response => {
               this.$alert("积分操作成功！");
               this.$emit('closeDialog','point');
               this.reset();
            }).catch(() => {
               // empty
            });
        }
      });
    }
  }
};
</script>
