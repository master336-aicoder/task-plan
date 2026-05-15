import axios, { AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

let suppressError = false

request.interceptors.request.use(
  (config) => {
    if (config.data instanceof FormData) {
      config.headers = {
        ...config.headers,
        'Content-Type': 'multipart/form-data'
      }
    }
    suppressError = false
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== 200000) {
      if (!suppressError) {
        ElMessage.error(res.msg || '请求失败')
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res.data
  },
  (error) => {
    if (!suppressError) {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export const suppressErrors = () => {
  suppressError = true
}

export default request
