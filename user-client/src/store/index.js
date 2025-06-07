import Vue from 'vue'
import Vuex from 'vuex'
import { login } from '@/api/user'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
    token: localStorage.getItem('token') || '',
    chargingStatus: null,
    queueInfo: null,
  },
  mutations: {
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      if (userInfo) {
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
        localStorage.setItem('userId', userInfo.id)
      } else {
        localStorage.removeItem('userInfo')
        localStorage.removeItem('userId')
      }
    },
    SET_TOKEN(state, token) {
      state.token = token
      if (token) {
        localStorage.setItem('token', token)
      } else {
        localStorage.removeItem('token')
      }
    },
    SET_CHARGING_STATUS(state, status) {
      state.chargingStatus = status
    },
    SET_QUEUE_INFO(state, info) {
      state.queueInfo = info
    },
  },
  actions: {
    async login({ commit }, { username, password }) {
      try {
        const response = await login(username, password)
        if (response.data.code === 200) {
          const userInfo = response.data.data
          commit('SET_TOKEN', userInfo.token)
          commit('SET_USER_INFO', userInfo)
          return Promise.resolve(response) // 确保登录成功时 resolve
        } else {
          return Promise.reject(new Error(response.data.message || '登录失败'))
        }
      } catch (error) {
        return Promise.reject(error) // 捕获错误
      }
    },
    logout({ commit }) {
      commit('SET_TOKEN', '')
      commit('SET_USER_INFO', null)
      commit('SET_CHARGING_STATUS', null)
      commit('SET_QUEUE_INFO', null)
    },
  },
  getters: {
    isLoggedIn: state => !!state.token,
    userInfo: state => state.userInfo,
    userId: state => (state.userInfo ? state.userInfo.id : null),
    chargingStatus: state => state.chargingStatus,
    queueInfo: state => state.queueInfo,
  },
})