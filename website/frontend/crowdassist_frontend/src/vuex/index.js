import Vue from 'vue'
import Vuex from 'vuex'
import Officer from './modules/officer_register.js'
import Firebase from './modules/firebase.js'
import AdminConsole from './modules/admin_console.js'
Vue.use(Vuex)
var data = new Vuex.Store({
  state: {},
  mutations : {},
  getters: {},
  actions: {},
  modules: {
    Officer,
    Firebase,
    AdminConsole
  }
})

export default data