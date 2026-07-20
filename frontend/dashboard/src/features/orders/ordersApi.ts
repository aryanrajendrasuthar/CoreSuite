import { api } from '../../app/api'
import type { Page } from '../product/productApi'

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED'

export interface OrderLineItem {
  id: number
  sku: string
  quantity: number
  unitPrice: string
  subtotal: string
}

export interface Order {
  id: number
  customerId: number
  status: OrderStatus
  currency: string
  totalAmount: string
  lineItems: OrderLineItem[]
  createdAt: string
}

export interface OrderStatusHistoryEntry {
  id: number
  fromStatus: OrderStatus | null
  toStatus: OrderStatus
  note: string | null
  changedAt: string
}

export interface CreateOrderRequest {
  customerId: number
  currency?: string
  lineItems: { sku: string; quantity: number; unitPrice: string }[]
}

export const ordersApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getOrders: builder.query<Page<Order>, void>({
      query: () => '/api/orders?size=100',
      providesTags: ['Order'],
    }),
    createOrder: builder.mutation<Order, CreateOrderRequest>({
      query: (body) => ({ url: '/api/orders', method: 'POST', body }),
      invalidatesTags: ['Order'],
    }),
    updateOrderStatus: builder.mutation<Order, { orderId: number; toStatus: OrderStatus; note?: string }>({
      query: ({ orderId, toStatus, note }) => ({
        url: `/api/orders/${orderId}/status`,
        method: 'PATCH',
        body: { toStatus, note },
      }),
      invalidatesTags: ['Order'],
    }),
    getOrderHistory: builder.query<OrderStatusHistoryEntry[], number>({
      query: (orderId) => `/api/orders/${orderId}/history`,
      providesTags: ['Order'],
    }),
  }),
})

export const {
  useGetOrdersQuery,
  useCreateOrderMutation,
  useUpdateOrderStatusMutation,
  useGetOrderHistoryQuery,
} = ordersApi
