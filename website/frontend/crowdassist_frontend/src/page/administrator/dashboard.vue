<template>
    <div id="dashboard" class="_flex _column">
        <h1>Dashboard</h1>
        <div class="_flex">
            <Card :data="data"></Card>
        </div>
    </div>
</template>
<script>
const Card = resolve => {
 require.ensure(['../../components/administrator/Card'], () => {
 resolve(require('../../components/administrator/Card.vue'))
 })
}
import vuex from '../../vuex'
export default {
  name:"DashBoard",
  created(){
      vuex.commit("reset")
      vuex.dispatch("fetchVerifyDocument")
  },
  computed:{
      data:()=>{
          return {
              name:"verify",
              title:"รอการ Verify",
              count:vuex.getters.getData.length
          }
      }
  },
  components:{
      "Card":Card
  }
}
</script>

<style>
    #dashboard{
        padding: 20px
    }
    #dashboard h1{
        text-align: left
    }
</style>
