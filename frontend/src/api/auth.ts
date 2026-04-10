import { api } from '@/utils/request'

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

// 获取当前用户信息
export const getCurrentUser = (): Promise<UserInfo> => {
  return api.get<UserInfo>('/users/me')
}
