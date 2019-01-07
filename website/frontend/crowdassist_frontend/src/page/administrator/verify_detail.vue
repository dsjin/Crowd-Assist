<template>
  <div id="verifyDetail" class="_flex _column">
    <div class="header">
      <h1 style="float:left">User Detail</h1>
        <ol class="breadcrumb">
              <li>
                <a @click="$click({name:'dashboard'})">
                  <i class="fas fa-home" style="margin-right:1px"></i>
                      Home
                </a>
              </li>
              <li>
                <a @click="$click({name:'verify'})" style="margin-left: -5px;">
                  Verify
                </a>
              </li>
              <li>{{username}}</li>
        </ol>
    </div>
    <div id="verify_section1">
      <h3>รายละเอียด User</h3>
      <div class="_flex">
        <table class="table_flex">
            <tr>
                <th class="column1"></th>
                <th>รายละเอียด</th>
            </tr>
            <tr>
                <td class="column1">ชื่อ - นามสกุล</td>
                <td>{{data.name}}</td>
            </tr>
            <tr>
                <td class="column1">วันเกิด</td>
                <td>{{formatDate(new Date(data.dateOfBirth))}}</td>
            </tr>
            <tr>
                <td class="column1">หมายเลขบัตรประชาชน</td>
                <td>{{data.idCard}}</td>
            </tr>
            <tr>
                <td class="column1">เพศ</td>
                <td>{{data.gender==="male"?"ชาย":"หญิง"}}</td>
            </tr>
            <tr>
              <td class="column1">Role</td>
              <td>{{data.role}}</td>
            </tr>
            <tr>
              <td class="column1">วันที่ส่งคำร้อง</td>
              <td>{{formatDate(new Date(data.timestamp))}}</td>
            </tr>
          </table>
      </div>
    </div>  
    <div id="verify_section2">
        <h3>หลักฐานอื่นๆ</h3>
        <div class="_flex _wrap" style="margin-top:-30px">
          <Card v-for="(item, key) in data.referencePath" v-bind:key="key" :title="key" :data="item">
          </Card>
        </div>
      </div>
      <div class="_flex _center_horizontal">
        <div class="button select" @click="verify()">
          <p>
            อนุมัติ
          </p>
        </div>
        <div class="button" @click="discard()">
          <p>
            ไม่อนุมัติ
          </p>
        </div>
      </div>
  </div>
</template>
<script>
import vuex from '../../vuex'
const VerifyCard = resolve => {
 require.ensure(['../../components/administrator/VerifyCard'], () => {
 resolve(require('../../components/administrator/VerifyCard.vue'))
 })
}
import key from '../../firebase/auth_key.js'
export default {
  name:"VerifyDetail",
  mounted(){
  },
  components:{
      "Card":VerifyCard
  },
  created(){
    vuex.dispatch("getVerifyDocument", this.$router.currentRoute.params.username) 
  },
  computed:{
    username:function(){
      return this.$router.currentRoute.params.username
    },
    data:function(){
      return vuex.getters.getCurrent
    }
  },
  methods:{
    formatDate:(date)=> {
            var monthNames = [
                "มกราคม", "กุมภาพันธ์", "มีนาคม",
                "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม",
                "สิงหาคม", "กันยายน", "ตุลาคม",
                "พฤษจิกายน", "ธันวาคม"
            ];

            var day = date.getDate();
            var monthIndex = date.getMonth();
            var year = date.getFullYear();
            return day + ' ' + monthNames[monthIndex] + ' ' + (year+543);
    },
    verify:function(){
      const auth_key = "key="+key
      const payload = {
          "to" : "/topics/"+this.data.username,
          "priority" : "high",
          "notification" : {
            "body" : "เริ่มต้นใช้งานแอพพลิเคชันได้เลย",
            "title" : "บัญชีผู้ใช้งานของคุณได้รับการอนุมัติแล้ว",
            "icon": "myicon",
            "sound": "default"
          },
          "data":{
            "verify":true
          }
      }
      const header = {
        "Content-Type": "application/json",
        "Authorization": auth_key
      }
      Promise.all([
        this.$firebase.firestore().collection("verify").doc(this.data.uid).update({
          verify: true
        }),
        this.$firebase.database().ref("users/"+this.data.uid).update({
          firstName:this.data.firstName,
          lastName:this.data.lastName,
          dateOfBirth:this.data.dateOfBirth,
          gender:this.data.gender,
          idCard:this.data.idCard,
          verify:true
        }),
        this.$http.post('https://fcm.googleapis.com/fcm/send', payload, {headers: header})
      ]).then(()=>{
        this.$click({name:'verify'})
      })
    },
    unverify:()=>{
      this.$firebase.firestore().collection("verify").doc(this.data.id).delete().then(()=>{
        this.$click({name:'verify'})
      })
    }
  }
}
</script>
<style>
  #verifyDetail .button{
    margin-left: 10px;
    margin-right: 10px;
    padding-left: 5%;
    padding-right: 5%
  }
  #verifyDetail .select{
    background-color: #00BFA5;
  }
  #verifyDetail .select:hover{
    background-color:#26A69A
  }
  #verifyDetail{
    padding: 20px
  }
  #verify_section1,#verify_section2{
    margin-bottom: 20px
  }
  #verify_section1 h3, #verify_section2 h3{
    text-align: left;
    margin: 0;
    margin-bottom: 20px
  }
  #verify_section1 .table_flex td{
    text-align: left
  }
  .table_flex > tr > td{
    padding-left: 10px;
    padding-right: 10px
  }
  .column1{
    width: 20%
  }
</style>
