import axios from 'axios'
import router from '../router'
import { ElMessage } from 'element-plus'

/**
 * 错误代码对应消息
 *
 * @type {{"401": string, "403": string}}
 */
const errorMessage = {"403":"您没有权限访问", "401":"请重新认证账户"};

/**
 * 要忽略的的错误状态
 * @type {number[]}
 */
const ignoreErrorStatus = [404];


/**
 * 异常管理
 *
 * @param error 错误信息
 */
function httpError(error) {

    if (ignoreErrorStatus.filter(s => s === error.response.status).length === 0) {

        let message = error.response.data[process.env.VUE_APP_SERVER_ERROR_MESSAGE_FIELD];

        let code = error.response.data[process.env.VUE_APP_SERVER_ERROR_CODE_FIELD];

        if (message === undefined) {
            message = errorMessage[error.response.status]
        }

        if (message === undefined) {
            message = process.env.VUE_APP_HTTP_ERROR_MESSAGE;
        }

        if (error.response.status === 401) {
            router.push("/" + process.env.VUE_APP_LOGIN_PATH);
        }

        if (code !== undefined) {
            code = "[" + code + "] ";
        } else {
            code = "";
        }

        ElMessage.error(code + message);

    }

    return Promise.reject(error.response);
}

/**
 * http 响应拦截器
 *
 * @param response 响应信息
 *
 * @returns {*} 拦截信息
 */
function responseInterceptor(response) {

    if (response.status === 200 && response.data.executeCode === "200") {
        return response.data.data;
    }

    return response
}

/**
 * 添加 http 响应拦截器
 */
axios.interceptors.response.use(responseInterceptor, httpError);

/**
 * http 请求拦截器
 */
function requestInterceptor(config) {
    config.headers["X-FILTER-RESULT-ID"] = process.env.VUE_APP_X_FILTER_RESULT_ID;
    config.headers["X-DATA-VERSION"] = process.env.VUE_APP_X_DATA_VERSION;
    return config;
}

/**
 * 添加 http 请求拦截器
 */
axios.interceptors.request.use(requestInterceptor, httpError);

export default axios;