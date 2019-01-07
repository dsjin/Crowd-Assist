<template>
    <div class="_modal _flex _center_vertical _center_horizontal">
        <div class="_content _flex _center_vertical _center_horizontal _column">
            <p class="title _2em _underline">เลือกรูปภาพประจำตัว</p>
            <div id="imgWrapper" class="img_wrapper" ref="imgWrapper"></div>
            <label class="file-select">
                <div class="button">
                    <p>เลือกรูปภาพในเครื่อง</p>
                </div>
                <input type="file" @change="handleFileChange" />
            </label>
            <div class="select_image _flex _center_horizontal" :key="showModal">
                <div class="button select" @click="select()">
                    <p>ตกลง</p>
                </div>
                <div class="button" @click="close()">
                    <p>ยกเลิก</p>
                </div>
            </div>  
        </div>
    </div>
</template>
<script>
import vuex from "../vuex"
import croppie from 'croppie'
var reader = new FileReader();
var instant;
export default {
  name:"ImageModal",
  data(){
      return {
          image: File
      }
  },
  methods: {
    handleFileChange(e) {
      this.image = e.target.files[0]
      reader.readAsDataURL(this.image)
      reader.onload = function(event) {
          if(!instant){
                instant = new Croppie(document.getElementById('imgWrapper'), {
                        url:event.target.result,
                        customClass:"cropper",
                        viewport:{width: 250, height: 250,type:'square'}
                })
          }else{
              instant.bind({
                    url:event.target.result
              })
          }
      }
    },
    select: async()=>{
        await instant.result({
            type:'blob',
            size:{width:800,height:800},
            circle:false
        }).then((blob) => {
            vuex.commit('changeNested', {name1:'form',name2:'imageForm', val:blob})
        })
        await instant.result({
            type:'base64',
            size:{width:800,height:800},
            circle:false
        }).then((base64)=>{
            vuex.commit('change', {name:'image', val:base64})
            vuex.commit('change', {name:'showModal', val:false})
        })
    },
    close(){
        vuex.commit('change', {name:'showModal', val:false})
    }
  },
    computed:{
        showModal: function(){
            return vuex.getters.getShowModal
        }
    }
}
</script>
<style>
    .select_image .button {
        margin-top: 40px;
        margin-right: 50px;
        flex-grow: 1;
    }
    .select_image{
        width: 100%;
        position: relative;
    }
    .select_image .select{
        background-color: #00BFA5;
        margin-left: 50px;
    }
    .select_image .select:hover{
        background-color:#26A69A
    }
    .img_wrapper{
        width: 250px !important;
        height: 250px !important;
        margin: 50px auto !important;
        margin-bottom: 80px !important;
        background-color: #888181 !important;
    }
    .cropper{
        position: relative;
        background-color: #888181;
    }
    .file-select .button:hover{
        background-color: #546E7A
        
    }
    .file-select .button{
        padding-top: 0.8px;
        padding-bottom: 0.8px;
        margin-top: 0px;
        margin-left: 30%;
        margin-right: 30%;
        background-color:#78909C
    }
    .file-select .button p{
        font-family: 'Kanit', sans-serif;
        color: aliceblue
    }
    .file-select > input[type="file"] {
        display: none;
    }
    .file-select{
        width: 100%
    }
</style>