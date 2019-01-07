<template>
  <div id="SectorForm">
      <h1><i class="fas fa-file"></i>ข้อมูลหน่วยงาน</h1>
      <div class="section _flex">
          <select name="section" v-validate="'required'" data-vv-as="หน่วยงาน" class="item select" :class="{'color':selectGrayStyle}" v-model="section">
            <option value="" selected disabled>เลือกหน่วยงาน</option>
            <option value="police">ตำรวจ</option>
          </select>
      </div>
      <div class="sub_section _flex">
          <input type="text" class="item" v-model="subSection" placeholder="หน่วยงานสังกัด ( ถ้ามี ) เช่น ตำรวจภูธร ภาค 1">
      </div>
      <div class="id_officer _flex">
          <input v-validate="'required'" data-vv-as="หมายเลขประจำตัวเจ้าหน้าที่" name="employeeNumber" type="number" class="item" placeholder="หมายเลขประจำตัวเจ้าหน้าที่" v-model="employeeNumber">
      </div>
  </div>
</template>
<script>
import vuex from '../../vuex'
import bus from '../../bus'
export default {
    name:"SectorForm",
    data(){
        return {
        }
    },
    mounted() {
        bus.$on('veeValidate', () => {
            this.$validator.validateAll();
        });
    },
    computed:{
        selectGrayStyle:function(){
            if(vuex.getters.getSectorData.type !== ""){
                return false        
            }
            return true
        },
        section:{
            get: function(){
                return vuex.getters.getSectorData.section
            },
            set: function(value){
                vuex.commit('changeNested', {name1:"sector_data", name2:"section", val: value})
            }
        },
        subSection:{
            get: function(){
                return vuex.getters.getSectorData.subSection
            },
            set: function(value){
                vuex.commit('changeNested', {name1:"sector_data", name2:"subSection", val: value})
            }
        },
        employeeNumber:{
            get: function(){
                return vuex.getters.getSectorData.employeeNumber
            },
            set: function(value){
                vuex.commit('changeNested', {name1:"sector_data", name2:"employeeNumber", val: value})
            }
        }
    }
}
</script>
<style>
    #SectorForm h1{
        text-align: left;
    }
    #SectorForm{
        padding: 0px 20px 20px 20px;
    }
    #SectorForm h1 i{
        padding-right: 10px
    }
    .section, .sub_section{
        margin-bottom: 30px;
    }
    .item.select{
        height: 42px;
    }
    .item.color{
        color:#999;
    }
    .item option:nth-child(n+2){
        color:black;
    }
</style>
