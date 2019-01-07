<template>
    <div id="complete" class="_flex _center_vertical _center_horizontal _column">
        <i class="fas fa-check-circle fa-10x _color_green"></i>
        <h2 style="margin-top:50px">สมัครเรียบร้อยแล้ว กรุณารอการอนุมัติจากผู้พัฒนาเพื่อใช้งานแอปพลิเคชั่น</h2>
        <div class="button" @click="backToHome">
            <p>กลับหน้าหลัก</p>
        </div>
    </div>
</template>
<script>
import vuex from '../vuex'
export default {
    name:"Complete",
    mounted(){
        document.querySelector('body').style.backgroundColor = "#d3d3d3"
    },
    methods:{
        backToHome(){
            vuex.commit("setComplete", false)
            this.$click({name:'Main'})
        }
    }
}
</script>
<style>
    @media (min-width: 1160px){
        #complete {
            width: 1130px;
        }
    }
    #complete{
        font-family: 'Kanit', sans-serif;
        border-radius: 20px;
        background-color: #ffffff;
        padding-right: 15px;
        padding-left: 15px;
        padding-top: 15px;
        padding-bottom: 15px;
        margin-top: 20px;
        margin-bottom: 20px;
        margin-right: auto;
        margin-left: auto;
        min-height: 90vh;
        box-shadow: 0 10px 16px 0 rgba(0,0,0,0.2),0 6px 20px 0 rgba(0,0,0,0.19)
    }
</style>
