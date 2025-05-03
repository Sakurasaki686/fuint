<template>
  <goodsForm :is-edit="false" :is-add="true"/>
</template>

<script>
import goodsForm from './goodsForm'

export default {
  name: 'goodsAdd',
  components: { goodsForm }
}
</script>
