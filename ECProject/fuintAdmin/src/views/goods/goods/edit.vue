<template>
  <goodsForm :is-edit="true" :is-add="false"/>
</template>

<script>
import goodsForm from './goodsForm'

export default {
  name: 'goodsEdit',
  components: { goodsForm }
}
</script>
