import { api } from '../../app/api'

export interface StockAlert {
  sku: string
  quantity: number
  reorderThreshold: number
}

export interface Kpis {
  totalOrders: number
  totalRevenue: string
  ordersByStatus: Record<string, number>
  lowStockCount: number
  lowStockItems: StockAlert[]
}

export const reportingApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getKpis: builder.query<Kpis, void>({
      query: () => '/api/reports/kpis',
    }),
  }),
})

export const { useGetKpisQuery } = reportingApi
