<!-- 会员选择对话框 -->
<template>
    <el-dialog class="common-dialog" title="选择会员" :visible="showDialog" @close="close" width="1000px" destroy-on-close>
        <div class="user-content">
          <div class="group-list">
            <div class="search">
              <el-input
              v-model="keyword"
              placeholder="请输入会员号、手机号..."
              style="width: 190px;margin-right: 2px;"
              clearable
              ></el-input>
              <el-button type="primary" @click="doSearch()">查询</el-button>
            </div>
            <el-tree
              ref="tree"
              :data="groupList"
              show-checkbox
              node-key="id"
              @check-change="checkGroup"
              :props="defaultProps">
            </el-tree>
            <div class="clearfix"></div>
          </div>
          <div class="member-list">
            <el-alert type="error" class="tips" :closable="false">共{{ memberList.length }}会员，已选择{{ memberIds.length }}位会员</el-alert>
            <el-table
              v-loading="loading"
              ref="multipleTable"
              :data="memberList"
              tooltip-effect="dark"
              style="width: 100%"
              @selection-change="checkUser">
              <el-table-column
                type="selection"
                align="center"
                width="55">
              </el-table-column>
              <el-table-column
                label="会员号"
                align="center"
                width="160">
                <template slot-scope="scope">{{ scope.row.userNo }}</template>
              </el-table-column>
              <el-table-column
                prop="name"
                align="center"
                label="名称">
              </el-table-column>
              <el-table-column
                prop="mobile"
                label="手机"
                align="center"
                width="120">
                <template slot-scope="scope">
                  <span>{{ scope.row.mobile ? scope.row.mobile : '-' }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
        <div class="clearfix"></div>
        <div slot="footer" class="dialog-footer">
            <el-button @click="close()">取消</el-button>
            <el-button type="primary" @click="doSelectUser()">确定</el-button>
        </div>
    </el-dialog>
</template>
<script>
import { searchMembers, getGroupList } from "@/api/member";
export default {
    name: "UserSelect",
    props: {
      showDialog: {
        type:[Boolean],
        default:()=>false
      },
      memberInfo: {
         type:[Object],
         default:()=>{}
      },
      cartList: {
        type:[Array],
        default:()=>[]
      },
    },
    watch: {
      showDialog(value) {
        if (value) {
            this.getGroupList();
            this.memberIds = [];
            this.memberList = [];
        }
      }
    },
    data(){
        return {
          loading: false,
          keyword: '',
          groupIds: [],
          memberIds: [],
          groupList: [],
          memberList: [],
          defaultProps: {
            children: 'children',
            label: 'name'
          }
        }
    },
    methods: {
      // 获取会员列表
      getMemberList() {
           const app = this;
           if (app.groupIds.length < 1 && !app.keyword) {
               app.memberList = [];
               return false;
           }
           const param = { groupIds: app.groupIds.join(','), keyword: app.keyword };
           app.loading = true;
           searchMembers(param).then( response => {
               if (response) {
                   app.memberList = response.data;
               }
               app.loading = false;
           })
        },
        // 获取分组列表
        getGroupList() {
          const app = this;
          getGroupList().then( response => {
            if (response.data) {
                app.groupList = response.data;
            }
          })
        },
        // 选择分组
        checkGroup() {
           const app = this;
           app.groupIds = app.$refs.tree.getCheckedKeys();
           app.getMemberList();
        },
        // 选择会员
        checkUser(userList) {
           let arr = [];
           if (userList) {
               userList.forEach(function(user) {
                  arr.push(user.id);
               })
           }
           this.memberIds = arr;
        },
        // 查找用户
        doSearch() {
          this.getMemberList();
        },
        // 关闭对话框
        close() {
           this.$emit('closeDialog','selectUser');
        },
        // 确定选择会员
        doSelectUser() {
           this.$emit('doSelectUser', this.memberIds);
        }
    }
}
</script>
<style lang="scss" scoped>
  .user-content {
    min-height: 300px;
    .group-list {
      width: 300px;
      float: left;
      padding: 10px;
      border: solid 1px #cccccc;
      height: 400px;
      overflow-y: scroll;
      .search {
        margin-bottom: 10px;
      }
    }
    .member-list {
      float: left;
      height: 400px;
      overflow-y: scroll;
      border: solid 1px #cccccc;
      width: 606px;
      padding: 10px;
      margin-left: 20px;
      .tips {
         position: absolute;
         width: 400px;
         top: 30px;
      }
    }
  }
</style>
