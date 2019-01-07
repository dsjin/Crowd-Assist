<template>
    <div id="termModal" class="_modal _flex _center_vertical _center_horizontal">
        <div class="_content">
            <p class="title">ข้อกำหนดและเงื่อนไขในการให้บริการ</p>
            <div class="termText">
                <p>{{terms}}</p>
            </div>
            <div class="button" @click="close()">
                <p>ปิด</p>
            </div>
        </div>
    </div>
</template>
<script>
import termsAndConditions from '../assets/terms_and_conditions.js'
import vuex from '../vuex'
export default {
  name:"TermModal",
  data(){
      return {
          terms:termsAndConditions
      }
  },
  methods:{
      close(){
          vuex.commit("change", {name: "showTermModal", val:false})
      }
  }
}
</script>
<style>
    #termModal ._content{
        min-height: 60vh;
        width: 50%;
        position: relative;
    }
    .termText{
        height: 50vh;
        width: cal(100%)-10px;
        background-color: #cecaca;
        overflow-y: scroll;
        padding: 10px
    }
    .termText p{
        text-align: left;
        margin:0;
        white-space: pre-wrap; 
        word-wrap: break-word;
    }
    #termModal .button{
        padding-top: 0.8px;
        padding-bottom: 0.8px;
        margin-top: 10px;
        margin-left: 30%;
        margin-right: 30%;
    }
</style>


