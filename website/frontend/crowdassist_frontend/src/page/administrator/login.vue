<template>
    <div id="login" class="_flex _center_vertical _center_horizontal">
        <div class="login_box">
            <p class="title _2em _underline">Login | ADMIN</p>
            <div class="login_username _flex">
                <input type="text" class="item" placeholder="Username" v-model="username">
            </div>
            <div class="login_password _flex">
                <input type="password" class="item" placeholder="Password" v-model="password">
            </div>
            <div class="confirm">
                <div class="button" @click="login()">
                    <p>Login</p>
                </div>
            </div>
        </div>
        <Loading v-show="progress"></Loading>
    </div>
</template>
<script>
import vuex from '../../vuex'
const Loading = resolve => {
 require.ensure(['../../components/Loading'], () => {
 resolve(require('../../components/Loading.vue'))
 })
}
export default {
  name:"Login",
  data(){
      return {
          username:"",
          password:""
      }
  },
  components:{
      "Loading":Loading
  },
  mounted(){
    document.querySelector('body').style.backgroundColor = "#d3d3d3"
  },
  methods:{
      login:function(){
          vuex.dispatch("login", {username:this.username, password:this.password})
      }
  },
  computed:{
      auth:function(){          
          return vuex.getters.getAuth
      },
      progress:function(){
          return vuex.getters.getProgress
      }
  },
  watch:{
      auth:function(value){
          this.$click({name:'dashboard'})
      }
  }
}
</script>
<style>
    #login .confirm{
        padding: 0px 20px 20px 20px;
    }
    #login .confirm .button{
        margin-top: 0px;
        padding-top: 0.8px;
        padding-bottom: 0.8px;
        margin-left: 30%;
        margin-right: 30%
    }
    .login_username{
        margin-top:60px
    }
    .login_username, .login_password{
        margin-bottom: 30px;
    }
    .login_box p{
        position: relative;
    }
    #login{
        height: 100vh;
    }
    .login_box{
        width: 500px;
        background-color: aqua;
        font-family: 'Kanit', sans-serif;
        border-radius: 20px;
        background-color: #ffffff;
        box-shadow: 0 10px 16px 0 rgba(0,0,0,0.2),0 6px 20px 0 rgba(0,0,0,0.19);
        padding-left: 10px;
        padding-right: 10px;
        padding-top: 30px;
        padding-bottom: 40px
    }
    .login_box .item {
        flex-grow: 1;
        margin-left: 10px;
        margin-right:10px;
        border-radius: 10px;
        outline: none;
        border: 1px solid rgba(0, 0, 0, 0.5);
        height: 30px;
        padding: 5px;
        font-size: 1em;
        font-family: 'Kanit', sans-serif;
    }
</style>
