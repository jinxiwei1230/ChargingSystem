import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    userInfo: null,
    token: localStorage.getItem('token') || '',
    chargingStatus: null,
    queueInfo: null
  },
  mutations: {
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
    },
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    SET_CHARGING_STATUS(state, status) {
      state.chargingStatus = status
    },
    SET_QUEUE_INFO(state, info) {
      state.queueInfo = info
    }
  },
  actions: {
    // 登录
    async login({ commit }, userInfo) {
      // 这里应该调用登录API
      const token = 'mock-token' // 模拟token
      commit('SET_TOKEN', token)
      commit('SET_USER_INFO', userInfo)
    },
    // 登出
    logout({ commit }) {
      commit('SET_TOKEN', '')
      commit('SET_USER_INFO', null)
      localStorage.removeItem('token')
    }
  },
  getters: {
    isLoggedIn: state => !!state.token,
    userInfo: state => state.userInfo,
    chargingStatus: state => state.chargingStatus,
    queueInfo: state => state.queueInfo
  }
}) 