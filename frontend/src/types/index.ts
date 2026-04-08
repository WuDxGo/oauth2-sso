export interface User {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  gender: number
  status: number
  roles: string[]
}

export interface Result<T = any> {
  code: number
  message: string
  data: T
}
