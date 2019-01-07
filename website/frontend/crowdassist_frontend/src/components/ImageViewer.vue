<template>
    <div class="_imageview _modal _flex _center_vertical _center_horizontal">
        <div class="exit_button">
            <i class="fas fa-times fa-2x _white" @click="click()"></i>
        </div>
        <div class="_image">
            <img :src="imgPath">
        </div>
    </div>
</template>
<script>
import vuex from '../vuex'
export default {
  name:"ImageViewer",
  computed:{
      imgPath:function(){
          return vuex.getters.getImageViewer.path
      }
  },
  methods:{
      click:function(){
          vuex.commit('changeNestedAdmin',{name1:"imageViewer",name2:"showModal",val:false})
      }
  }
}
</script>
<style>
    ._imageview{
        z-index: 4000 !important; 
        margin-left: 0px;
        min-height: 100vh;
        background-color: rgb(0,0,0);
        background-color: rgba(0,0,0,0.8) !important;
    }
    .exit_button{
        position: fixed;
        right: 80px;
        top:50px;
    }
    .exit_button i{
        color: aliceblue;
        cursor: pointer;
    }
</style>

