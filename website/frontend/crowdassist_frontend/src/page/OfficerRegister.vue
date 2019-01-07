<template>
    <div id="officerRegister">
        <p class="title _2em _underline">ลงทะเบียนสำหรับเจ้าหน้าที่</p>
        <ProfileImage></ProfileImage>
        <GeneralForm ref="general"></GeneralForm>
        <SectorForm ref="sector"></SectorForm>
        <VerifyForm ref="verify"></VerifyForm>
        <AcceptTerm ref="accept"></AcceptTerm>
        <div class="confirm _flex _center_horizontal">
          <div class="button" @click="send()">
            <p>สมัคร</p>
          </div>
        </div>
        <Loading v-show="inProgress"></Loading>
    </div>
</template>
<script>
const ProfileImage = resolve => {
 require.ensure(['../components/ProfileImage'], () => {
 resolve(require('../components/ProfileImage.vue'))
 })
}
const GeneralForm = resolve => {
 require.ensure(['../components/form/GeneralForm'], () => {
 resolve(require('../components/form/GeneralForm.vue'))
 })
}
const SectorForm = resolve => {
 require.ensure(['../components/form/SectorForm'], () => {
 resolve(require('../components/form/SectorForm.vue'))
 })
}
const VerifyForm = resolve => {
 require.ensure(['../components/form/VerifyForm'], () => {
 resolve(require('../components/form/VerifyForm.vue'))
 })
}
const AcceptTerm = resolve => {
 require.ensure(['../components/AcceptTerm'], () => {
 resolve(require('../components/AcceptTerm.vue'))
 })
}
const Loading = resolve => {
 require.ensure(['../components/Loading'], () => {
 resolve(require('../components/Loading.vue'))
 })
}
import vuex from '../vuex'
import bus from '../bus'
var errors = []
export default {
  name:"OfficerRegister",
  components:{
    "ProfileImage":ProfileImage,
    "GeneralForm":GeneralForm,
    "SectorForm":SectorForm,
    "VerifyForm": VerifyForm,
    "AcceptTerm":AcceptTerm,
    "Loading":Loading
  },
  methods:{
    send:function(){
      Promise.all([
        this.getValidate("general"),
        this.getValidate("sector"),
        this.getValidate("verify"),
        this.getValidate("accept")
      ]).then((result)=>{
        if(result.includes(false) || !vuex.getters.getImage || !vuex.getters.getUsernameAvaliable){
          if(!vuex.getters.getImage){
            errors.push("กรุณาเลือกรูปภาพประจำตัว")
          }
          if(!vuex.getters.getUsernameAvaliable){
            errors.push("มีคนใช้ชื่อผู้ใช้งานนี้แล้ว")
          }
          alert("กรุณาใส่รายละเอียดให้ถูกต้อง\n"+errors.join("\n"))
          errors = []
        }else{
          let payload = Object.assign(vuex.getters.getImageForm, vuex.getters.getBasicData, vuex.getters.getSectorData, vuex.getters.getVerifyData)
          vuex.dispatch("createAccount", payload);
        }
      })
    },
    getValidate:function(name){
      return new Promise((resolve, reject)=>{
        this.$refs[name].$validator.validateAll().then((result)=>{
          if(result) return resolve(true)
          else {
            this.$refs[name].errors.items.forEach((value)=>{
              errors.push(value.msg)
            })            
            return resolve(false)
          }
        })
      })
    }
  },
  mounted() {
    this.$on('veeValidate', () => {
     bus.$emit('veeValidate');
    });
    document.querySelector('body').style.backgroundColor = "#d3d3d3"
  },
  computed:{
    inProgress:function(){
      return vuex.getters.getInRegister
    },
    complete:function(){
      return vuex.getters.getComplete
    }
  },
  watch:{
    complete:function(value){
      if(value){
        this.$click({name:'Complete'})
      }
    }
  },
  beforeRouteLeave(to, from, next){
    if(vuex.getters.getComplete){
      vuex.commit('setOfficerRegisterDefault')
    }
    next()
  }
}
</script>
<style>
  @media (min-width: 1160px){
    #officerRegister {
        width: 1130px;
    }
  }
  .confirm{
    padding: 0px 20px 20px 20px;
  }
  .confirm .button{
    margin-top: 0px
  }
  .title{
    position: relative;
  }
  #officerRegister{
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
    min-height: 100vh;
    box-shadow: 0 10px 16px 0 rgba(0,0,0,0.2),0 6px 20px 0 rgba(0,0,0,0.19)
  }
</style>


