import { api } from '../../app/api'

export interface Warehouse {
  id: number
  name: string
  location: string | null
}

export interface StockLevel {
  id: number
  warehouseId: number
  sku: string
  quantity: number
  reorderThreshold: number
  belowReorderThreshold: boolean
}

export interface CreateWarehouseRequest {
  name: string
  location?: string
}

export interface InitializeStockRequest {
  warehouseId: number
  sku: string
  quantity: number
  reorderThreshold: number
}

export const inventoryApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getWarehouses: builder.query<Warehouse[], void>({
      query: () => '/api/warehouses',
      providesTags: ['Warehouse'],
    }),
    createWarehouse: builder.mutation<Warehouse, CreateWarehouseRequest>({
      query: (body) => ({ url: '/api/warehouses', method: 'POST', body }),
      invalidatesTags: ['Warehouse'],
    }),
    getStockByWarehouse: builder.query<StockLevel[], number>({
      query: (warehouseId) => `/api/stock/warehouse/${warehouseId}`,
      providesTags: ['Stock'],
    }),
    initializeStock: builder.mutation<StockLevel, InitializeStockRequest>({
      query: (body) => ({ url: '/api/stock', method: 'POST', body }),
      invalidatesTags: ['Stock'],
    }),
    adjustStock: builder.mutation<StockLevel, { stockId: number; delta: number }>({
      query: ({ stockId, delta }) => ({
        url: `/api/stock/${stockId}/adjust`,
        method: 'PATCH',
        body: { delta },
      }),
      invalidatesTags: ['Stock'],
    }),
    getReorderAlerts: builder.query<StockLevel[], void>({
      query: () => '/api/stock/reorder-alerts',
      providesTags: ['Stock'],
    }),
  }),
})

export const {
  useGetWarehousesQuery,
  useCreateWarehouseMutation,
  useGetStockByWarehouseQuery,
  useInitializeStockMutation,
  useAdjustStockMutation,
  useGetReorderAlertsQuery,
} = inventoryApi
