<template>
  <div class="app-container">
    <div class="main-panel">
      <el-form ref="form" :model="form" :rules="rules" label-width="200px" style="margin-top: 20px;">
        <el-row>
          <el-col :span="24">
            <el-form-item label="充值对象">
              <el-radio-group v-model="form.object" @change="objectChange()">
                <el-radio key="all" label="all" value="all">全部会员</el-radio>
                <el-radio key="part" label="part" value="part">部分会员</el-radio>
                <el-button type="danger" size="mini" v-if="form.object == 'all'">总会员数：{{ totalMember }}</el-button>
                <el-button type="primary" size="mini" v-if="form.object == 'part'" @click="selectUser()">选择会员（已选{{ form.memberIds.length }}人）</el-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="充值数额" prop="amount">
              <el-input v-model="form.amount" style="width: 200px" placeholder="请输入充值数额" maxlength="200" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.description" style="width: 400px" type="textarea" placeholder="请输入内容"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="footer" style="padding-left: 120px;">
          <el-button type="primary" @click="submitForm">确定</el-button>
          <el-button @click="cancel">取消</el-button>
      </div>
    </div>
    <!--会员选择对话框start-->
    <UserSelect :showDialog="openUserSelect" @doSelectUser="doSelectUser" @closeDialog="closeDialog"></UserSelect>
    <!--会员选择对话框end-->
  </div>
</template>

<script>
import { distribute } from "@/api/balance";
import { getTotalMember } from "@/api/statistic";
export default {
  name: "Distribute",
  data() {
    return {
      // 打开会员选择对话框
      openUserSelect: false,
      // 遮罩层
      loading: false,
      // 总会员数
      totalMember: 5430,
      // 表单参数
      form: { object: "all", memberIds: [], amount: "", description: "" },
      // 表单校验
      rules: {
        amount: [
          { required: true, message: "请选择", trigger: "blur" },
        ]
      }
    };
  },
  created() {
     this.getTotalMember();
  },
  methods: {
    // 取消按钮
    cancel() {
      this.$store.dispatch('tagsView/delView', this.$route);
      this.$router.push('/');
    },
    // 提交按钮
    submitForm: function() {
      const app = this;
      app.$refs["form"].validate(valid => {
        if (valid) {
            app.$confirm('您确定要充值吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                const userIds = app.form.memberIds ? app.form.memberIds.join(",") : '';
                const param = { userIds : userIds, object: app.form.object, amount : app.form.amount, remark: app.form.description }
                distribute(param).then(response => {
                  if (response.code == '200') {
                    app.$modal.msgSuccess("充值成功！");
                    app.form = { object: "all", memberIds: [], amount: "", description: "" };
                  } else {
                    app.$modal.msgError("充值失败!");
                  }
                });
            }).catch(() => {});
        }
      });
    },
    // 获取会员总数
    getTotalMember() {
      getTotalMember().then(response => {
           this.totalMember = response.data.totalMember;
        }
      );
    },
    // 选择会员
    selectUser() {
      this.openUserSelect = true;
    },
    // 关闭对话框
    closeDialog(dialog) {
       if (dialog == 'selectUser') {
           this.openUserSelect = false;
       }
       return true;
    },
    // 确定选择会员
    doSelectUser(memberIds) {
       this.form.memberIds = memberIds;
       this.openUserSelect = false;
    },
    // 发放对象
    objectChange() {
       if (this.form.object == 'all') {
           this.form.memberIds = [];
       }
    }
  }
};
</script>
