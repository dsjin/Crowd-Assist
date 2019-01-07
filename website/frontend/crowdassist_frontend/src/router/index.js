import Vue from 'vue'
import vuex from '../vuex'
import Router from 'vue-router'
import Main from '@/page/Main'
import OfficerRegister from '@/page/OfficerRegister'
import Dashboard from '@/page/administrator/dashboard'
import Verify from '@/page/administrator/verify'
import VerifyDetail from '@/page/administrator/verify_detail'
import Login from '@/page/administrator/login'
import Admin from '@/page/administrator/admin'
import Complete from '@/page/OfficerRegisterComplete'

Vue.use(Router)
var router = new Router({
  routes: [
    {
      path: '/',
      name: 'Main',
      component: Main,
      meta: {title: 'Crowd Assist'}
    },
    {
      path: '/register/officer',
      name: 'OfficerRegister',
      component: OfficerRegister,
      meta: {title: 'Register | Officer'}
    },
    {
      path: '/register/complete',
      name: 'Complete',
      component: Complete,
      meta: {title: 'Register | Complete'}
    },
    {
      path:'/admin',
      name:'admin',
      component: Admin,
      children: [
        {
          path:'dashboard',
          name:'dashboard',
          component: Dashboard,
          meta:{auth : true},
          meta: {title: 'Dashboard', auth : true}
        },
        {
          path:'verify',
          component: Verify,
          name:'verify',
          meta:{auth : true},
          meta: {title: 'Verify', auth : true}
        },
        {
          path: 'verify/:username',
          component: VerifyDetail,
          name:"verify_detail",
          meta:{auth : true},
          meta: {title: 'Verify | Detail', auth : true}
        }
      ]
    },

    {
      path:'/login',
      component: Login,
      name:'login',
      meta: {title: 'Login'}
    }
  ]
})
export default router
router.beforeEach((to, from, next) => {
  
  if(to.meta.auth&&!vuex.getters.getAuth){
    next('/login')
  }else if(to.name === "login" && vuex.getters.getAuth){
    next({path : "/admin/dashboard"})
  }else if(to.name === "Complete" && !vuex.getters.getComplete){
    next({path : "/"})
  }else{
    if(to.name === "admin"){
      if(!vuex.getters.getAuth){
        next({path : "/login"})
      }else{
        next({path : "/admin/dashboard"})
      }
    }else{
      document.title = to.meta.title
      next()
    }
  }
})