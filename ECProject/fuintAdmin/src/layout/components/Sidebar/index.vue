<template>
    <div :class="{'has-logo':showLogo}" :style="{ backgroundColor: settings.sideTheme === 'theme-dark' ? variables.menuBackground : variables.menuLightBackground }">
        <logo v-if="showLogo" :collapse="isCollapse" />
        <div class="store-info" v-if="storeName">
            <div class="name">{{ storeName }}</div>
            <div class="role" v-if="role">（{{ role }}）</div>
        </div>
        <el-scrollbar :class="settings.sideTheme" wrap-class="scrollbar-wrapper">
            <el-menu
                :default-active="activeMenu"
                :collapse="isCollapse"
                :background-color="settings.sideTheme === 'theme-dark' ? variables.menuBackground : variables.menuLightBackground"
                :text-color="settings.sideTheme === 'theme-dark' ? variables.menuColor : variables.menuLightColor"
                :unique-opened="true"
                :active-text-color="settings.theme"
                :collapse-transition="false"
                mode="vertical"
                style="margin-bottom: 80px;"
            >
                <sidebar-item
                    v-for="(route, index) in sidebarRouters"
                    :key="route.path  + index"
                    :item="route"
                    :base-path="route.path"
                />
            </el-menu>
        </el-scrollbar>
    </div>
</template>

<script>
import { mapGetters, mapState } from "vuex";
import Logo from "./Logo";
import SidebarItem from "./SidebarItem";
import variables from "@/assets/styles/variables.scss";

export default {
    components: { SidebarItem, Logo },
    computed: {
        ...mapState(["settings"]),
        ...mapGetters(["sidebarRouters", "sidebar"]),
        activeMenu() {
            const route = this.$route;
            const { meta, path } = route;
            // if set path, the sidebar will highlight the path you set
            if (meta.activeMenu) {
                return meta.activeMenu;
            }
            return path;
        },
        showLogo() {
            return this.$store.state.settings.sidebarLogo;
        },
        variables() {
            return variables;
        },
        isCollapse() {
            return !this.sidebar.opened;
        },
        storeName() {
            if (this.$store.getters.merchantName && this.$store.getters.merchantId) {
                return this.$store.getters.merchantName;
            }
            if (this.$store.getters.storeName && this.$store.getters.storeId) {
                return this.$store.getters.storeName;
            }
            return '';
        },
        role() {
          if (this.$store.getters.storeName && this.$store.getters.storeId) {
              return '店铺管理员';
          }
          if (this.$store.getters.merchantName && this.$store.getters.merchantId) {
              return '商户管理员';
          }
        }
    }
};
</script>

<style lang="scss" scoped>
  .store-info {
    height: 42px;
    padding-top: 2px;
    text-align: center;
    background: #EE9A49;
    overflow: hidden;
    border-radius: 3px;
    margin: 0px 6px 10px 6px;
    cursor: pointer;
    .name {
      font-weight: bold;
      font-size: 14px;
      color: #ffffff;
      text-align: center;
      overflow: hidden;
    }
    .role {
      font-size: 10px;
      margin-top: 3px;
      color: #ffffff;
    }
  }
</style>
