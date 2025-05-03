<template>
  <div class="nav-tool">
    <img class="icon" title="收银下单" @click="goto" src="@/assets/images/cashier.svg">
    <div class="name">收银下单</div>
  </div>
</template>

<script>
export default {
  name: 'FuintCashier',
  data() {
    return {
      url: '/cashier/index'
    }
  },
  methods: {
    goto() {
       const routeLink = this.$router.resolve({ path: this.url });
       window.open(routeLink.href, '_blank');
    }
  }
}
</script>
