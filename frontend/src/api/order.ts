import { api } from '@/utils/request'

// 订单实体
export interface Order {
  id: number
  orderNo: string
  userId: number
  amount: number
  status: number
  description: string
  createTime: string
  updateTime: string
}

// 获取订单列表
export const getOrderList = (): Promise<Order[]> => {
  return api.get<Order[]>('/orders')
}

// 获取订单详情
export const getOrderById = (id: number): Promise<Order> => {
  return api.get<Order>(`/orders/${id}`)
}

// 创建订单
export const createOrder = (order: Partial<Order>): Promise<Order> => {
  return api.post<Order>('/orders', order)
}

// 更新订单
export const updateOrder = (id: number, order: Partial<Order>): Promise<Order> => {
  return api.put<Order>(`/orders/${id}`, order)
}

// 删除订单
export const deleteOrder = (id: number): Promise<void> => {
  return api.delete<void>(`/orders/${id}`)
}
