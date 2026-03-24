import request from '@/utils/request'

export interface Client {
  id?: string
  clientId: string
  clientName: string
  clientSecret?: string
  clientAuthenticationMethods?: string[]
  authorizationGrantTypes?: string[]
  redirectUris?: string[]
  scopes?: string[]
  requireConsent?: boolean
}

/**
 * 获取所有客户端
 */
export function getClientList() {
  return request({
    url: '/clients',
    method: 'get'
  })
}

/**
 * 获取单个客户端
 */
export function getClient(clientId: string) {
  return request({
    url: `/clients/${clientId}`,
    method: 'get'
  })
}

/**
 * 创建客户端
 */
export function createClient(data: Client) {
  return request({
    url: '/clients',
    method: 'post',
    data
  })
}

/**
 * 更新客户端
 */
export function updateClient(clientId: string, data: Client) {
  return request({
    url: `/clients/${clientId}`,
    method: 'put',
    data
  })
}

/**
 * 删除客户端
 */
export function deleteClient(clientId: string) {
  return request({
    url: `/clients/${clientId}`,
    method: 'delete'
  })
}
