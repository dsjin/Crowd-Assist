<template>
    <div id="accept" class="_flex _center_vertical _center_horizontal">
        <input v-validate="'required'" name="verify" data-vv-as="ยอมรับ" type="checkbox"><p class="term">คุณ ( "ผู้ใช้" ) ยอมรับ<a class="a_term" @click="open()"> ข้อกำหนดและเงื่อนไขในการให้บริการ </a>ของแอปพลิเคชัน Crowd Assist</p><br>
        <TermModal v-show="show"></TermModal>
    </div>
</template>
<script>
import vuex from '../vuex'
const TermModal = resolve => {
 require.ensure(['./TermModal'], () => {
 resolve(require('./TermModal.vue'))
 })
}
export default {
    name:"AcceptTerm",
    components:{
        "TermModal":TermModal
    },
    computed:{
        show(){
            return vuex.getters.getShowTermModal
        }
    },
    methods:{
        open(){
            vuex.commit('change', {name:"showTermModal", val:true})
        }
    },
    watch:{
      show: function(value){
          document.querySelector('body').style.overflow = this.show? 'hidden' : null
      }
    }
}
</script>
<style>
    #accept::before{
        border-bottom: 2px solid #000000;
        content: "";
        position: absolute;
        width: 20%;
        height: 1px;
        margin-top: 10px;
        margin-left: auto;
        margin-right: auto;
        left: 0;
        right: 0;
        top:0;
    }
    #accept{
        padding: 50px 30px 40px 30px;
        position: relative;
    }
    .term{
        margin:0;
        margin-left: 10px
    }
    .a_term{
        cursor: pointer;
        font-weight: bold;
    }
</style>
