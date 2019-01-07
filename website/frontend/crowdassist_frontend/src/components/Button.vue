<template>
  <div class="button" @click="click">
      <p>ลงทะเบียนสำหรับเจ้าหน้าที่</p>
  </div>
</template>
<script>
export default {
  name:"Button",
  methods:{
      click: function(){
          this.$click({name:'OfficerRegister'})
      }
  }
}
</script>
<style>
</style>

