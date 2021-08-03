import {createApp } from 'vue'

import axios from './http/index'
import VueAxios from 'vue-axios'
import Application from './App.vue'

import router from './router'
import store from './store'

import ElementPlus from 'element-plus'
import locale from 'element-plus/lib/locale/lang/zh-cn'

import './element-variables.scss'

import moment from "moment";

const app = createApp(Application)
moment.locale("zh-cn")
app.config.globalProperties.$moment = moment;

app.config.globalProperties.formUrlencoded = function(json) {
    let param = new URLSearchParams();

    for (let j in json) {
        param.append(j, json[j]);
    }

    return param;
}

app.use(VueAxios, axios).use(store).use(router).use(ElementPlus, { locale }).mount('#app')