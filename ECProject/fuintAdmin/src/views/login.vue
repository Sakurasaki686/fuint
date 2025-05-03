<template>
  <div class="login">
      <div class="login-main">
        <div class="caption">
            <h4 class="caption-title">{{ systemName }}</h4>
            <p class="caption-remark">欢迎使用 {{ systemName }}，您的卡券、储值卡、计次卡等会员营销小管家！</p>
        </div>
      </div>
      <div class="login-form">
         <el-form ref="loginForm" :model="loginForm" :rules="loginRules">
          <div class="title">
            <img class="logo" src="@/assets/logo/logo.png"/>
            <span class="name">{{ systemName }}</span>
          </div>
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              type="text"
              auto-complete="off"
              clearable
              placeholder="账号"
            >
              <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              auto-complete="off"
              placeholder="密码"
              clearable
              @keyup.enter.native="handleLogin"
            >
              <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
            </el-input>
          </el-form-item>
          <el-form-item prop="captchaCode" v-if="captchaOnOff">
            <el-input
              v-model="loginForm.captchaCode"
              auto-complete="off"
              placeholder="请输入验证码"
              style="width: 63%"
              clearable
              @keyup.enter.native="handleLogin"
            >
              <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
            </el-input>
            <div class="login-code">
              <img :src="codeUrl" @click="getCode" class="login-code-img"/>
            </div>
          </el-form-item>
          <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
          <el-form-item style="width:100%;">
            <el-button
              :loading="loading"
              size="medium"
              type="primary"
              style="width:100%;line-height: 24px;font-size: 16px;"
              @click.native.prevent="handleLogin"
            >
              <span v-if="!loading">立即登录</span>
              <span v-else>登 录 中...</span>
            </el-button>
          </el-form-item>
         </el-form>
      </div>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login";
import Cookies from "js-cookie";
import { encrypt, decrypt } from '@/utils/jsencrypt'

export default {
  name: "Login",
  data() {
    return {
      codeUrl: "",
      systemName : process.env.VUE_APP_TITLE,
      loginForm: {
        username: "fuint",
        password: "123456",
        rememberMe: false,
        captchaCode: "",
        uuid: ""
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" }
        ],
        captchaCode: [{ required: true, trigger: "change", message: "请输入验证码" }, { min: 4, max: 6, message: '请输入验证码', trigger: 'blur' }]
      },
      loading: false,
      // 验证码开关
      captchaOnOff: true,
      // 注册开关
      register: false,
      redirect: undefined
    };
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect;
      },
      immediate: true
    }
  },
  created() {
    this.getCode();
    this.getCookie();
  },
  methods: {
    getCode() {
      const app = this
      getCodeImg().then(response => {
        app.codeUrl = response.data.captcha
        app.loginForm.uuid = response.data.uuid
      })
    },
    getCookie() {
      const username = Cookies.get("username");
      const password = Cookies.get("password");
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe),
        uuid: this.loginForm.uuid
      };
    },
    handleLogin() {
      const app = this;
      if (app.loading) {
          return false;
      }
      this.$refs.loginForm.validate(valid => {
        if (valid) {
            app.loading = true;
            if (app.loginForm.rememberMe) {
                Cookies.set("username", this.loginForm.username, { expires: 30 });
                Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 });
                Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 });
            } else {
                Cookies.remove("username");
                Cookies.remove("password");
                Cookies.remove('rememberMe');
            }
            app.$store.dispatch("Login", this.loginForm).then(() => {
            app.$router.push({ path: this.redirect || "/" }).catch(()=>{});
          }).catch(() => {
              app.loading = false;
              if (app.captchaOnOff) {
                  app.getCode();
              }
          });
        }
      });
    }
  }
};
</script>

<style rel="stylesheet/scss" lang="scss">
.login {
  height: 100%;
  width: 100%;
  position: relative;
  .login-main {
      background-image: url("../assets/images/login-bg.png");
      position: fixed;
      left: 0;
      right: 500px;
      top: 0;
      bottom: 0;
      -webkit-transform: translateZ(0);
      overflow: hidden;
      background-size: cover;
      background-repeat: no-repeat;
      background-position: center;
      .caption {
          .caption-title {
              font-size: 30px;
              font-weight: bold;
              line-height: 0px;
          }
          .caption-remark {
              line-height: 0px;
          }
          background: rgba(0,0,0,.7);
          color: #ffffff;
          position: absolute;
          bottom: 0;
          left: 0;
          right: 0;
          padding: 30px 50px;
          font-size: 14px;
          z-index: 20;
          font-weight: 300;
          min-width: 680px;
      }
  }
  .login-form {
      width: 500px;
      height: 100%;
      float: right;
      padding: 80px 60px;
      background: #FFF;
      .title {
        text-align: left;
        font-size: 24px;
        color: #707070;
        padding: 20px 20px 40px 0px;
        .logo {
          height: 24px;
          width: 24px;
          vertical-align: middle;
        }
        .name {
          margin-left: 5px;
        }
      }
      .login-tip {
        font-size: 13px;
        text-align: center;
        color: #bfbfbf;
      }
      .login-code {
        width: 33%;
        height: 38px;
        float: right;
        img {
          cursor: pointer;
          vertical-align: middle;
        }
      }
      .login-code-img {
        height: 45px;
      }
      .el-input {
        height: 45px;
        input {
          height: 45px;
        }
      }
      .input-icon {
        height: 45px;
        width: 14px;
        margin-left: 2px;
      }
      .copy-right {
         color: #888888;
         text-align: center;
         font-size: 12px;
         .link {
             color: #ff5b57;
             font-weight: bold;
         }
      }
  }
}
</style>
