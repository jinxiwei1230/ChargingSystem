import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../views/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/Home.vue')
      },
      {
        path: 'charging-request',
        name: 'ChargingRequest',
        component: () => import('../views/ChargingRequest.vue')
      },
      {
        path: 'queue-status',
        name: 'QueueStatus',
        component: () => import('../views/QueueStatus.vue')
      },
      {
        path: 'charging-details',
        name: 'ChargingDetails',
        component: () => import('../views/ChargingDetails.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue')
      }
    ]
  }
]

const router = new VueRouter({
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userInfo = localStorage.getItem('userInfo')
  if (to.path === '/login' || to.path === '/register') {
    next()
  } else {
    if (!userInfo) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router