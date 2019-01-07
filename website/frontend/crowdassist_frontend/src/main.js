// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import firebase from '@/firebase'
import config from '@/firebase/config'
import vuex from '@/vuex'
import VeeValidate, { Validator } from 'vee-validate'
import th from 'vee-validate/dist/locale/th';
import vueResource from 'vue-resource'
import '!style-loader!css-loader!./assets/css/style.css'
import '!style-loader!css-loader!../node_modules/croppie/croppie.css'
//import '!style-loader!css-loader!../node_modules/bulma/css/bulma.css'

Vue.config.productionTip = false
Vue.use(firebase)
Vue.use(vueResource)
const dict = {
  custom: {
    verify: {
      required: 'กรุณายอมรับข้อกำหนดและเงื่อนไขในการให้บริการ'
    },
    gender:{
      required: 'กรุณาเลือกเพศ'
    }
  }
};
Validator.localize('th', th);
Validator.localize('th', dict);
Vue.use(VeeValidate)
Vue.prototype.$firebase.initializeApp(config)

Vue.mixin({
  methods: {
    $click : (path) => {
      router.push(path)
    },
    $isNumber: function(evt) {
      evt = (evt) ? evt : window.event;
      var charCode = (evt.which) ? evt.which : evt.keyCode;
      if ((charCode > 31 && (charCode < 48 || charCode > 57))) {
        evt.preventDefault();;
      } else {
        return true;
      }
    }
  }
})
/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>',
  created(){ 
    Vue.prototype.$firebase.auth().onAuthStateChanged((firebaseUser)=>{
      if(!vuex.getters.getInRegister){
        vuex.commit("setProgress", true)
        if(firebaseUser){
          Vue.prototype.$firebase.database().ref("users").child(firebaseUser.uid).once("value").then( async (snapshot)=>{
            if(snapshot.val().role === "admin"){
              vuex.commit("setUser", firebaseUser)
              vuex.commit("setAuth", true)
              vuex.commit("setProgress",false)
            }else{
              vuex.commit("setProgress", false)
              await Vue.prototype.$firebase.auth().signOut()
            }
          })
        }else{
          vuex.commit("setProgress", false)
        }
      }
    })
  }
})
