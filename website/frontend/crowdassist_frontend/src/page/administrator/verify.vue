<template>
    <div id="verify" class="_flex _column">
        <div class="header">
            <h1 style="float:left">Verify</h1>
            <ol class="breadcrumb">
                <li>
                    <a @click="$click({path:'dashboard'})">
                        <i class="fas fa-home" style="margin-right:1px"></i>
                            Home
                    </a>
                </li>
                <li>Verify</li>
            </ol>
        </div>
        <table class="table_flex">
            <tr>
                <th>#</th>
                <th>Name</th>
                <th>Role</th> 
                <th>Submitted</th>
                <th>Action</th>
            </tr>
            <tr v-for="(item, index) in data" v-bind:key="item.name">
                <td>{{index+1}}</td>
                <td>{{item.name}}</td> 
                <td>{{item.role}}</td>
                <td>{{formatDate(new Date(item.timestamp))}}</td>
                <td><button type="button" @click="click(item.username)">view</button></td>
            </tr>
        </table>
    </div>
</template>
<script>
import vuex from '../../vuex'
export default {
  name:"Verify",
  created(){
      vuex.commit("reset")
      vuex.dispatch("fetchVerifyDocument")
  },
  computed:{
      data:()=>{
          return vuex.getters.getData
      }
  },
  methods:{
      click(val){
          this.$click({name:'verify_detail',params: {username: val} })
      },
       formatDate:(date)=> {
            var monthNames = [
                "January", "February", "March",
                "April", "May", "June", "July",
                "August", "September", "October",
                "November", "December"
            ];

            var day = date.getDate();
            var monthIndex = date.getMonth();
            var year = date.getFullYear();
            var hours = date.getHours();
            var minutes = date.getMinutes();
            var sec = date.getSeconds();
            if(hours < 10){
                hours = "0"+hours
            }
            if(minutes < 10){
                minutes = "0"+minutes
            }
            if(sec < 10){
                sec = "0"+sec
            }
            return day + ' ' + monthNames[monthIndex] + ' ' + year + ' ' + hours + ":" + minutes + ":" + sec;
    }
  }
}
</script>
<style>
    .header{
        position: relative;
    }
    .header h1{
        display: inline-block;
    }
    #verify{
        padding: 20px
    }
    #verify h1{
        text-align: left
    }
</style>

