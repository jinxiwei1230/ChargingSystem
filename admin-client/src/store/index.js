import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    userInfo: null,
    token: localStorage.getItem('admin-token') || ''
  },
  getters: {
    userInfo: state => state.userInfo,
    token: state => state.token,
    isLoggedIn: state => !!state.token
  },
  mutations: {
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
    },
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('admin-token', token)
    },
    CLEAR_USER(state) {
      state.userInfo = null
      state.token = ''
      localStorage.removeItem('admin-token')
    }
  },
  actions: {
    // 登录
    async login({ commit }, loginData) {
      try {
        // 这里应该调用API进行登录
        const response = await new Promise(resolve => {
          setTimeout(() => {
            resolve({
              token: 'mock-token',
              userInfo: {
                id: 1,
                username: loginData.username,
                role: 'admin'
              }
            })
          }, 1000)
        })
        
        commit('SET_TOKEN', response.token)
        commit('SET_USER_INFO', response.userInfo)
        return response
      } catch (error) {
        throw error
      }
    },
    // 登出
    logout({ commit }) {
      commit('CLEAR_USER')
    }
  }
})