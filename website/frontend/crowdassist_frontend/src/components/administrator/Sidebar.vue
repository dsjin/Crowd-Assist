<template>
  <div class="app-sidebar">
      <div class="sidebar-wrapper left">
          <aside class="menu">
            <p class="menu-label">
                General
            </p>
            <ul class="menu-list">
                <li><a :class="{'is-active':isActive.dashboard}" @click="$click({name:'dashboard'})">Dashboard</a></li>
            </ul>
            <p class="menu-label">
                User
            </p>
            <ul class="menu-list">
                <li><a :class="{'is-active':isActive.verify}" @click="$click({name:'verify'})">Verify</a></li>
            </ul>
            </aside>
      </div>
      <div class="credit _flex _center_vertical _center_horizontal">
          <p><i class="fa fa-code"></i> with <i class="fa fa-heart"></i> By DSJIN</p>
      </div>
  </div>
</template>
<script>
export default {
  name:"Sidebar",
  data(){
      return{
        isActive:{
            dashboard:false,
            verify:false
        }
      }
  },
  mounted(){
      if(this.path.includes("verify")){
              this.setDefault()
              this.isActive.verify = true
          }else if(this.path.includes("dashboard")){
              this.setDefault()
              this.isActive.dashboard = true
          }
  },
  methods:{
      getPath() {
          return this.$route.path
      },
      setDefault(){
          this.isActive = {
            dashboard:false,
            verify:false
        }
      }
  },
  computed:{
      path(){
          return this.$route.path
      }
  },
  watch:{
      path: function(value){          
          if(value.includes("verify")){
              this.setDefault()
              this.isActive.verify = true
          }else if(value.includes("dashboard")){
              this.setDefault()
              this.isActive.dashboard = true
          }
      }
  }
}
</script>
<style>
    .app-sidebar{
        position: fixed;
        top: 80px;
        left: 0;
        bottom: 0;
        padding: 20px 0 50px;
        width: 200px;
        min-width: 45px;
        max-height: 100vh;
        height: calc(100% - 150px);
        z-index: 1024 - 1;
        background: #FFF;
        box-shadow: 0 2px 3px rgba(17, 17, 17, 0.1), 0 0 0 1px rgba(17, 17, 17, 0.1);
    }
    .left{
        text-align: left;
    }
    .sidebar-wrapper{
        overflow-x: hidden;
        height: calc(100% - 80px);
    }
    .sidebar-wrapper ul{
        list-style: none;
        margin: 0;
        padding: 0
    }
    .menu-label{
        color: #7a7a7a;
        font-size: .75em;
        letter-spacing: .1em;
        text-transform: uppercase;
    }
    .menu-list a{
        border-radius: 2px;
        color: #4a4a4a;
        display: block;
        padding: .5em .75em;
        cursor: pointer;
        transition: all .2s ease-in;
    }
    .menu-list a:hover{
        background-color: #f5f5f5;
        color: #363636;
    }
    .menu-list a.is-active{
        background-color: #3273dc;
        color: #fff;
    }
    .sidebar-wrapper .menu-label{
        padding-left: 10px
    }
    .credit:before{
        border-bottom: 2px solid #000000;
        content: "";
        position: absolute;
        width: 40%;
        height: 1px;
        top:0;
        margin-top: 10px;
        margin-left: auto;
        margin-right: auto;
        left: 0;
        right: 0;
    }
    .credit{
        width: 100%;
        height: 80px;
        position: absolute;
        bottom: 0;
    }
</style>
