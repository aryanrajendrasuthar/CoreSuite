import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export const api = createApi({
  reducerPath: 'api',
  // The session lives in an HttpOnly cookie set by api-gateway; without
  // 'include' the browser won't send it on these cross-origin (but same-site)
  // requests, and every call would look unauthenticated.
  baseQuery: fetchBaseQuery({ baseUrl, credentials: 'include' }),
  tagTypes: ['Product', 'Customer', 'Segment', 'Warehouse', 'Stock', 'Order', 'CurrentUser'],
  endpoints: () => ({}),
})
