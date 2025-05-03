<template>
  <div>
    <svg-icon icon-class="github" @click="goto" />
  </div>
</template>

<script>
export default {
  name: 'FuintGit',
  data() {
    return {
      url: 'https://gitee.com/fuint/fuint'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>
