<template>
  <div title="帮助文档" class="nav-icon">
    <svg-icon icon-class="question" @click="goto" />
  </div>
</template>

<script>
export default {
  name: 'FuintDoc',
  data() {
    return {
      url: 'https://www.fuint.cn/docs/index/'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>
