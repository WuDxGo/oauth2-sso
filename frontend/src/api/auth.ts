import { api } from '@/utils/request'

// 登录请求参数
export interface LoginParams {
  username: string
  password: string
  grant_type: string
  client_id: string
  client_secret: string
}

// Token 响应
export interface TokenResponse {
  access_token: string
  token_type: string
  expires_in: number
  scope: string
}

// 用户信息
export interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  gender: number
  status: number
  roles: string[]
  authorities: string[]
}

// OAuth2 Token 获取（密码模式）
export const getToken = (params: LoginParams): Promise<TokenResponse> => {
  const formData = new URLSearchParams()
  formData.append('username', params.username)
  formData.append('password', params.password)
  formData.append('grant_type', 'password')
  formData.append('scope', 'read write')

  return fetch('/oauth2/token', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'Authorization': 'Basic ' + btoa(`${params.client_id}:${params.client_secret}`)
    },
    body: formData
  }).then(res => {
    if (!res.ok) {
      return res.json().then(err => Promise.reject(new Error(err.error_description || err.message || '登录失败')))
    }
    return res.json()
  })
}

// 获取当前用户信息
export const getCurrentUser = (): Promise<UserInfo> => {
  return api.get<UserInfo>('/users/me')
}
