<template>
  <nav class="app-navbar">
      <div class="_flex _center_vertical" style="height:100%;justify-content: space-between">
          <h1 style="margin-left:60px">ADMIN CONSOLE</h1>
          <p style="margin-right:60px;cursor:pointer" @click="logout">LOGOUT</p>
      </div>
  </nav>
</template>
<script>
import vuex from '../../vuex'
export default {
  name:"Navbar",
  methods:{
      logout(){
          vuex.dispatch("logout")
      }
  },
  computed:{
      isAuth(){
          return vuex.getters.getAuth
      }
  },
  watch:{
      isAuth(value){
          if(!value){
              this.$click({name:'login'})
          }
      }
  }
}
</script>
<style>
    .app-navbar {
        font-family: 'Kanit', sans-serif;
        position: fixed;
        min-width: 100%;
        z-index: 1024;
        background-color: #fff;
        box-shadow: 0 2px 3px rgba(17, 17, 17, 0.1), 0 0 0 1px rgba(17, 17, 17, 0.1);
        height: 80px;
    }
    .app-navbar h1{
        font-size: 1.5em
    }
</style>
