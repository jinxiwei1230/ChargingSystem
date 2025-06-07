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
        path: 'charging-piles',
        name: 'ChargingPiles',
        component: () => import('../views/ChargingPiles.vue')
      },
      {
        path: 'charging-details',
        name: 'ChargingDetails',
        component: () => import('../views/ChargingDetails.vue')
      },
      {
        path: 'charging-queue',
        name: 'ChargingQueue',
        component: () => import('../views/ChargingQueue.vue')
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/Settings.vue')
      }
    ]
  }
]

const router = new VueRouter({
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin-token')
  
  // 如果访问登录页
  if (to.path === '/login') {
    // 如果已经登录，重定向到首页
    if (token) {
      next('/')
    } else {
      next()
    }
    return
  }
  
  // 如果未登录且访问的不是登录页，重定向到登录页
  if (!token) {
    next('/login')
    return
  }
  
  // 其他情况正常放行
  next()
})

export default router