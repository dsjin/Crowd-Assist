<template>
  <div class="verify_card box" @click="click()">
      <p>{{resolveTitle(title)}}</p>
  </div>
</template>
<script>
import vuex from '../../vuex'
export default {
  name:"Hello",
  props:[
      'data',
      'title'
  ],
  methods:{
      click:function(){
          vuex.commit('changeNestedAdmin',{name1:"imageViewer",name2:"showModal",val:true})
          vuex.commit('changePath',this.data)
      },
      resolveTitle:(title)=>{
          let solve = {
              'idCard' : 'บัตรประชาชน',
              'photoWithCard':'รูปถ่ายพร้อมบัตรประชาชน',
              'employeeCard':'บัตรประจำตัวข้าราชการ/หน่วยงาน',
              'photoWithEmployeeCard':'รูปถ่ายพร้อมบัตรข้าราชการ/หน่วยงาน'
          }          
          return solve[title]
      }
  }
}
</script>
<style>
    .verify_card{
        flex-basis: 0;
        flex-grow: 1;
        min-width: 20%;
        max-width: 20%;
        margin-left: 10px;
        margin-right: 10px;
        margin-top:30px;
        cursor: pointer;
    }
</style>

