<template>
  <div id="VerifyForm">
      <h1><i class="fas fa-file"></i>หลักฐานอื่นๆ</h1>
      <div class="file-form _flex">
          <p>บัตรประชาชน</p>
          <label class="file-select-verify _flex _center_vertical">
                <div class="button">
                    <p>เลือกรูปภาพในเครื่อง</p>
                </div>
                <input v-validate="'required|image'" data-vv-as="รูปบัตรประชาชน" name="idCardImage" type="file" @change="handleFileChange($event, 'idCardFile')" accept="image/*"/>
                <p v-show="showText.idCardFile"><i class="fas fa-check"></i> {{msg.idCardFile}}</p>
          </label>
      </div>
      <div class="file-form _flex">
          <p>บัตรข้าราชการ / บัตรพนักงาน</p>
          <label class="file-select-verify _flex _center_vertical">
                <div class="button">
                    <p>เลือกรูปภาพในเครื่อง</p>
                </div>
                <input v-validate="'required|image'" name="employeeCard" data-vv-as="รูปบัตรข้าราชการ / บัตรพนักงาน" type="file" @change="handleFileChange($event, 'employeeCard')" accept="image/*"/>
                <p v-show="showText.employeeCard"><i class="fas fa-check"></i> {{msg.employeeCard}}</p>
          </label>
      </div>
      <div class="file-form _flex">
          <p>รูปตัวเองพร้อมบัตรข้าราชการ หรือ บัตรพนักงาน</p>
          <label class="file-select-verify _flex _center_vertical">
                <div class="button">
                    <p>เลือกรูปภาพในเครื่อง</p>
                </div>
                <input v-validate="'required|image'" name="photoWithEmployeeCard" data-vv-as="รูปตัวเองพร้อมบัตรข้าราชการ หรือ บัตรพนักงาน"  type="file" @change="handleFileChange($event, 'photoWithEmployeeCard')" accept="image/*"/>
                <p v-show="showText.photoWithEmployeeCard"><i class="fas fa-check"></i> {{msg.photoWithEmployeeCard}}</p>
          </label>
      </div>
  </div>
</template>
<script>
import vuex from '../../vuex'
import bus from '../../bus'
export default {
  name:"VerifyForm",
  data(){
      return {
          msg:{
              idCardFile:"",
              employeeCard:"",
              photoWithEmployeeCard:""
          },
          showText:{
              idCardFile:false,
              employeeCard:false,
              photoWithEmployeeCard:false
          }
      }
  },
  mounted() {
    bus.$on('veeValidate', () => {
        this.$validator.validateAll();
    });
  },
  methods:{
      handleFileChange(e, name) {
          this.showText[name] = true;
          this.msg[name] = "เลือกไฟล์เรียบร้อย"
          vuex.commit("changeNested", {name1: "verify_data", name2:name, val: e.target.files[0]})
      }
  }
}
</script>
<style>
    #VerifyForm h1{
        text-align: left;
    }
    #VerifyForm{
        padding: 0px 20px 20px 20px;
    }
    #VerifyForm h1 i{
        padding-right: 10px
    }
    .file-form{
        margin-left: 10px;
        margin-right:10px;
    }
    .file-select-verify .button:hover{
        background-color: #546E7A
        
    }
    .file-select-verify .button{
        padding-top: 0.5px;
        padding-bottom: 0.5px;
        padding-left: 20px;
        padding-right: 20px;
        margin-top: 0px;
        margin-left: 20px;
        margin-right: 20px;
        background-color:#78909C
    }
    .file-select-verify .button p{
        font-family: 'Kanit', sans-serif;
        color: aliceblue;
        font-size: 0.7em
    }
    .file-select-verify > input[type="file"] {
        display: none;
    }
    .file-select-verify i{
        color: darkgreen
    }
</style>


