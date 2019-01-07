<template>
    <div class="card box" @click="goTo">
        <div class="title">{{data.title}}</div>
        <p><i class="far fa-user-circle fa-4x"></i></p>
        <p class="number">{{data.count}}</p>
        <p>รายการ</p>
    </div>
</template>
<script>
export default {
  name:"Card",
  props:['data'],
  methods:{
      goTo:function(){
          switch(this.data.name){
              case "verify":
                this.$click({name:'verify'})
                break
          }
      }
  }
}
</script>
<style>
    .card{
        flex-basis: 0;
        flex-grow: 1;
        padding: 20px;
        margin-right: 10px;
        height: min-content;
        max-width: 45%;
        cursor: pointer;
    }
    .card .title{
        text-align: left
    }
    .number{
        font-size: 2em
    }
</style>

