<template>
    <div id="GeneralForm">
      <h1><i class="fas fa-file"></i>ข้อมูลทั่วไป</h1>
      <div class="username _flex _center_vertical _wrap">
          <input type="text" v-validate="'required|alpha_dash'" name="username" data-vv-as="ชื่อผู้ใช้งาน" class="item" placeholder="ชื่อผู้ใช้งาน" v-model="username">
          <div class="username_description">
              <p v-show="!isChecking && checkUsernameMsg.length === 0">กรอกชื่อผู้ใช้งานเป็นตัวอักษร A-Z 0-9 - _ เท่านั้น </p>
              <p v-show="isChecking">กำลังเช็ค Username</p>
              <p v-show="checkUsernameMsg.length != 0">{{checkUsernameMsg}}</p>
          </div>
      </div>
      <div class="name _flex _wrap">
          <input type="text" class="item" v-validate="'required'" name="firstName" data-vv-as="ชื่อจริง" placeholder="ชื่อจริง" style="margin-top: 30px;" v-model="firstName">
          <input type="text" class="item" v-validate="'required'" name="lastName" data-vv-as="นามสกุล" placeholder="นามสกุล" style="margin-top: 30px;" v-model="lastName">
      </div>
      <div class="email _flex">
          <input type="email" v-validate="'required|email'" name="email" class="item" data-vv-as="Email" placeholder="email" v-model="email">
      </div>
      <div class="idcard _flex">
          <input type="text" maxlength="13" v-validate="'required|min:13'" name="idCard" data-vv-as="หมายเลขบัตรประชาชน" v-on:keypress="this.$isNumber" class="item" placeholder="หมายเลขบัตรประชาชน" v-model="idcard">
      </div>
      <div class="password _flex">
          <input :type="passwordType(visible)" class="item" v-validate="'required|min:6'" name="password" data-vv-as="รหัสผ่าน" placeholder="รหัสผ่าน (อย่างน้อย 6 ตัว)" v-model="password">
          <div class="toggle-view" @click="visible = !visible">
              <i class="fas fa-eye fa-lg" v-show="visible"></i>
              <i class="fas fa-eye-slash fa-lg" v-show="!visible"></i>
          </div>
      </div>
      <div class="password _flex">
          <input :type="passwordType(pinVisible)" maxlength="4" class="item" v-validate="'required|min:4'" name="pin" data-vv-as="รหัสยืนยันการช่วยเหลือในแอปพลิเคชั่น" placeholder="รหัสยืนยันการช่วยเหลือในแอปพลิเคชั่น (ตัวเลข 4 ตัว)" v-on:keypress="this.$isNumber" v-model="pin">
          <div class="toggle-view" @click="pinVisible = !pinVisible">
              <i class="fas fa-eye fa-lg" v-show="pinVisible"></i>
              <i class="fas fa-eye-slash fa-lg" v-show="!pinVisible"></i>
          </div>
      </div>
      <div class="birthday_gender _flex _wrap">
          <input type="text" v-validate="'required'" data-vv-as="วันเกิด" name="dateOfBirth" class="item" placeholder="วันเกิด" onfocus="(this.type='date')" style="margin-top: 30px;" v-model="birthday" :max="today">
          <div class="gender_item _flex _center_vertical" style="margin-top: 30px;">
            <p style="margin-right:10px">เพศ</p>
            <input type="radio" v-validate="'required'" data-vv-as="เพศ" name="gender" value="male" checked v-model="gender"> 
            <p style="margin-right:10px">ชาย</p>   
            <input type="radio" name="gender" value="female" v-model="gender">
            <p style="margin-right:10px;">หญิง</p>
          </div>
      </div>
  </div>
</template>
<script>
import vuex from '../../vuex'
import bus from '../../bus'
export default {
  name:"GeneralForm",
  data(){
      return{
          visible:false,
          pinVisible:false,
          isChecking:false,
          checkUsernameMsg:""
      }
  },
  mounted() {
    bus.$on('veeValidate', () => {
        this.$validator.validateAll();
    });
  },
  computed:{
      today:function(){
          var today = new Date();
          var dd = today.getDate();
          var mm = today.getMonth()+1; //January is 0!
          var yyyy = today.getFullYear();
            if(dd<10){
                    dd='0'+dd
                } 
                if(mm<10){
                    mm='0'+mm
                } 

           return yyyy+'-'+mm+'-'+dd;
      },
      pin:{
          get: function(){
              return vuex.getters.getBasicData.pin
          },
          set: function(value){
              vuex.commit('changeNested', {name1:"basic_data", name2:"pin", val: value})
          }
      },
      username:{
          get: function(){
              return vuex.getters.getBasicData.username
          },
          set: function(value){
              vuex.commit('changeNested', {name1:"basic_data", name2:"username", val: value})
          }
      },
      firstName:{
          get: function(){
              return vuex.getters.getBasicData.firstName
          },
          set: function(value){
              vuex.commit('changeNested', {name1:"basic_data", name2:"firstName", val: value})
          }
      },
      lastName:{
          get: function(){
              return vuex.getters.getBasicData.lastName
          },
          set: function(value){
              vuex.commit('changeNested', {name1:"basic_data", name2:"lastName", val: value})
          }
      },
      email:{
          get: function(){
              return vuex.getters.getBasicData.email
          },
          set: function(value){
              vuex.commit('changeNested', {name1:"basic_data", name2:"email", val: value})
          }
      },
      idcard:{
          get: function(){
              return vuex.getters.getBasicData.idCard
          },
          set: function(value){
              vuex.commit('changeNested', {name1:"basic_data", name2:"idCard", val: value})
          }
      },
      password:{
          get: function(){
              return vuex.getters.getBasicData.password
          },
          set: function(value){
              vuex.commit('changeNested', {name1:"basic_data", name2:"password", val: value})
          }
      },
      birthday:{
          get: function(){
              return vuex.getters.getBasicData.birthdate
          },
          set: function(value){
              vuex.commit('changeNested', {name1:"basic_data", name2:"birthdate", val: value})
          }
      },
      gender:{
          get: function(){
              return vuex.getters.getBasicData.gender
          },
          set: function(value){
              vuex.commit('changeNested', {name1:"basic_data", name2:"gender", val: value})
          }
      }
  },
  methods:{
      passwordType:function(value){
          if(value){
              return "text"
          }else{
              return "password"
          }
      },
      checkUsername:function(){
          this.checkUsernameMsg = ""
          this.isChecking = true
          if(vuex.getters.getBasicData.username.length !== 0){
              this.$firebase.database().ref('user_id/'+vuex.getters.getBasicData.username).once('value').then((datasnapshot)=>{
                if(vuex.getters.getBasicData.username.length !== 0){
                    if(datasnapshot.val()){
                        this.checkUsernameMsg = "มีคนใช้ชื่อผู้ใช้งานนี้แล้ว"
                        vuex.commit('change', {name:"usernameAvaliable", val: false})
                    }else{
                        this.checkUsernameMsg = "สามารถใช้งานชื่อผู้ใช้งานนี้ได้"
                        vuex.commit('change', {name:"usernameAvaliable", val: true})
                    }
                }
                this.isChecking = false
            })
          }else{
              this.isChecking = false
          }
      }
  },
  watch:{
      username:function(){
          this.checkUsername()
      }
  }
}
</script>
<style>
    .password{
        position: relative;
    }
    .toggle-view{
        position: absolute;
        right: 20px;
        top:10px
    }
    #GeneralForm h1{
        text-align: left;
    }
    #GeneralForm{
        padding: 20px;
    }
    #GeneralForm h1 i{
        padding-right: 10px
    }
    .username, .name, .email , .idcard, .password, .pin{
        margin-bottom: 30px
    }
    .name, .birthday_gender {
        margin-top: -30px;
    }
    .item {
        flex-basis: 0;
        flex-grow: 1;
        margin-left: 10px;
        margin-right:10px;
        border-radius: 10px;
        outline: none;
        border: 1px solid rgba(0, 0, 0, 0.5);
        height: 30px;
        padding: 5px;
        font-size: 1em;
        font-family: 'Kanit', sans-serif;
    }
    .gender_item{
        flex-basis: 0;
        flex-grow: 1;
        margin-left: 10px;
        margin-right:10px;
        height: 30px;
        padding: 5px;
        font-size: 1em;
    }
    .username_description{
        margin-left: 20px;
        margin-right: 10px;
        flex-basis: 0;
        flex-grow: 1;
    }
    .username_description p{
        text-align: left;
        margin: 0;
    }
    input[type=number]::-webkit-inner-spin-button, 
    input[type=number]::-webkit-outer-spin-button { 
        -webkit-appearance: none; 
        margin: 0; 
    }
    .password input{
        padding-right: 40px
    }
</style>
