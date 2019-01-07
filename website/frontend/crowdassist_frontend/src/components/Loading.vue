<template>
  <div id="loading" class="_modal _flex _center_vertical _center_horizontal">
      <div class="loading_box _flex _column _center_vertical _center_horizontal">
          <div class="loader">
            <span></span>
          </div>
          <p>Loading</p>
      </div>
  </div>
</template>
<script>
export default {
    name:"Loading"  
}
</script>
<style>
    #loading{
        background: rgba(0,0,0,0.7) ;
    }
    .loading_box{
        width: 200px;
        height: 200px;
        background-color: aliceblue;
        border-radius: 5px
    }
    .loading_box p{
        margin-top: 30px
    }
    .loader{
        position: relative;
        height: 70px;
        width: 70px;
        animation: rotate 2s cubic-bezier(0.770, 0.000, 0.175, 1.000) infinite;
    }
    .loader span:before{
        content: "";
        display: block;
        position: absolute;
        top: 0;
        left: 0;
        bottom: auto;
        right: 0;
        margin: auto;
        width: 20px;
        height: 20px;
        background-color: black;
        border-radius: 50%;
        animation: scale-1 2s cubic-bezier(0.770, 0.000, 0.175, 1.000) infinite;
    }
    .loader span:after{
        content: "";
        display: block;
        position: absolute;
        top: auto;
        left: 0;
        bottom: 0;
        right: 0;
        margin: auto;
        width: 20px;
        height: 20px;
        background-color: black;
        border-radius: 50%;
        animation: scale-1 2s cubic-bezier(0.770, 0.000, 0.175, 1.000) infinite;
    }
    .loader:before{
        content: "";
        display: block;
        position: absolute;
        top: 0;
        left: 0;
        bottom: 0;
        right: auto;
        margin: auto;
        width: 15px;
        height: 15px;
        background-color: black;
        border-radius: 50%;
        animation: scale-1 2s cubic-bezier(0.770, 0.000, 0.175, 1.000) infinite;
    }
    .loader:after{
        position: absolute;
        right: 0;
        top:0;
        bottom:0;
        display: block;
        left: auto;
        margin: auto;
        content: "";
        width: 15px;
        height: 15px;
        background-color: black;
        border-radius: 50%;
        animation: scale-1 2s cubic-bezier(0.770, 0.000, 0.175, 1.000) infinite;
    }
    @keyframes scale-1{
        0% {
            transform:scale(0.7);
        }
        50% {
            transform:scale(1);
        }
        100%{
            transform: scale(0.7)
        }
    }
    @keyframes scale-2{
        0% {
            transform:scale(1);
        }
        50% {
            transform:scale(1.2);
        }
        100%{
            transform: scale(1)
        }
    }
    @keyframes rotate {
	    0%   { transform: rotate(0deg); }
	    100% { transform: rotate(360deg); }
    }
</style>
