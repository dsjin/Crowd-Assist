<template>
  <div id="main" class="_flex _column _center_vertical _center_horizontal">
      <img src="../assets/image/logo.png">
      <Button></Button>
  </div>
</template>
<script>
const Button = resolve => {
 require.ensure(['../components/Button'], () => {
 resolve(require('../components/Button.vue'))
 })
}
export default {
  name:"Main",
  components:{
      'Button':Button
  },
  mounted(){
    document.querySelector('body').style.backgroundColor = "#FFFFFF"
  }
}
</script>
<style>
    #main img{
        width: 30%
    }
    #main{
        height: 100vh;
    }
</style>
