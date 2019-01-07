<template>
    <div id="admin">
        <Navbar></Navbar>
        <Sidebar></Sidebar>
        <transition name="fade" mode="out-in">
            <ImageViewer v-show="showModal"></ImageViewer>
        </transition>
        <div class="app-main">
            <transition name="fade" mode="out-in">
                <router-view/>
            </transition>
        </div>
    </div>
</template>

<script>
    const ImageViewer = resolve => {
        require.ensure(['../../components/ImageViewer'], () => {
        resolve(require('../../components/ImageViewer.vue'))
        })
    }
    const Navbar = resolve => {
        require.ensure(["../../components/administrator/Navbar"], () => {
        resolve(require("../../components/administrator/Navbar.vue"))
        })
    }
    const Sidebar = resolve => {
        require.ensure(["../../components/administrator/Sidebar"], () => {
        resolve(require("../../components/administrator/Sidebar.vue"))
        })
    }
    import vuex from '../../vuex'
    export default{
        name: "Admin",
        components:{
            "Navbar":Navbar,
            "Sidebar":Sidebar,
            "ImageViewer":ImageViewer
        },
        mounted(){
            document.querySelector('body').style.backgroundColor = "#FFFFFF"
        },
        computed:{
            showModal:function(){
                return vuex.getters.getImageViewer.showModal
            }
        },
        watch:{
            showModal: function(value){
                document.querySelector('body').style.overflow = this.showModal? 'hidden' : null
            }
        }
    }
</script>
<style>
    #admin{
        height: 100vh;
    }
    .app-main{
        padding-top: 80px;
        margin-left: 200px;
        transform: translate3d(0, 0, 0);
        font-family: 'Kanit', sans-serif;
    }
</style>
