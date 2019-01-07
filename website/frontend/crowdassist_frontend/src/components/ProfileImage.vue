<template>
    <div class="profileImage">
        <div class="img">
             <img :src="this.image">
        </div>
        <div class="selectImage _flex _center_vertical _center_horizontal" @click="open()">
            <i class="fas fa-plus fa-lg"></i>
        </div>
        <transition name="fade" mode="out-in">
            <ImageModal v-show="showModal"></ImageModal>
        </transition>
    </div>
</template>
<script>
import vuex from '@/vuex'
const ImageModal = resolve => {
 require.ensure(['../components/ImageModal'], () => {
 resolve(require('../components/ImageModal.vue'))
 })
}
export default {
    name:"ProfileImage",
    components:{
    "ImageModal":ImageModal
    },
    data(){
        return {
        }
    },
    methods:{
        open(){
            vuex.commit('change', {name:"showModal", val:true})    
        }
    },
    computed:{
        showModal: function(){
            return vuex.getters.getShowModal
        },
        image:function(){
            return vuex.getters.getImage
        }
    },
    watch:{
      showModal: function(value){
          document.querySelector('body').style.overflow = this.showModal? 'hidden' : null
      }
  }
}
</script>
<style>
    .profileImage{
        width: 200px;
        border-radius: 50%;
        background-color: #888181;
        height: 200px;
        margin-left: auto;
        margin-right: auto;
        margin-top: 50px;
        position: relative;
    }
    .profileImage .img{
        width: 200px;
        border-radius: 50%;
        background-color: #888181;
        height: 200px;
        margin-left: auto;
        margin-right: auto;
        margin-top: 50px;
        overflow: hidden;
    }
    .img img{
        width: 200px;
        height: 200px;
    }
    .selectImage{
        position: absolute;
        right: 15px;
        border-radius: 50%;
        bottom: -5px;
        width: 60px;
        height: 60px;
        background-color: #DB4537;
        cursor: pointer;
        transition: all .2s ease-in;
    }
    .selectImage:hover{
        background-color: #BA3322;
    }
    .selectImage i{
        margin: auto 0;
        color: aliceblue
    }
</style>
