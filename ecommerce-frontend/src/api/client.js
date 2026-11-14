import axios from 'axios'

// Base URL of the Spring Boot API (ecommerce-monolith), e.g. https://shop-api.onekampus.com
// Local development: http://localhost:8086
export const backendUrl = (import.meta.env.VITE_BACKEND_URL || 'http://localhost:8086').replace(/\/$/, '')

export const api = axios.create({ baseURL: backendUrl, timeout: 15000 })

// Errors come back as RFC 7807 problem details { title, status, detail, errors? }:
// surface the most useful message (first field error, detail, or title).
api.interceptors.response.use(
  (res) => res,
  (error) => {
    const body = error?.response?.data
    const fieldError = body?.errors && Object.values(body.errors)[0]
    const message = fieldError || body?.detail || body?.title || error.message
    return Promise.reject(Object.assign(new Error(message), { status: error?.response?.status, title: body?.title }))
  }
)

export const setAuthToken = (token) => {
  if (token) api.defaults.headers.common.Authorization = `Bearer ${token}`
  else delete api.defaults.headers.common.Authorization
}
