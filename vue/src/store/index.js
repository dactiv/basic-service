import { createStore } from 'vuex'

export default createStore({
  state: {
    // 存储用户信息
    user: localStorage.getItem('user') ? localStorage.getItem('user') : ''
  },
  mutations: {

  },
  actions: {
  },
  modules: {
  }
})
