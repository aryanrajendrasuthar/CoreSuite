import { api } from '../../app/api'
import type { Page } from '../product/productApi'

export interface Customer {
  id: number
  fullName: string
  email: string
  phone: string | null
  company: string | null
  tags: string[]
  createdAt: string
}

export interface Segment {
  id: number
  name: string
  description: string | null
  requiredTags: string[]
}

export interface CreateCustomerRequest {
  fullName: string
  email: string
  phone?: string
  company?: string
}

export interface CreateSegmentRequest {
  name: string
  description?: string
  requiredTags: string[]
}

export const crmApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getCustomers: builder.query<Page<Customer>, void>({
      query: () => '/api/customers?size=100',
      providesTags: ['Customer'],
    }),
    createCustomer: builder.mutation<Customer, CreateCustomerRequest>({
      query: (body) => ({ url: '/api/customers', method: 'POST', body }),
      invalidatesTags: ['Customer'],
    }),
    addCustomerTag: builder.mutation<Customer, { customerId: number; tag: string }>({
      query: ({ customerId, tag }) => ({
        url: `/api/customers/${customerId}/tags`,
        method: 'POST',
        body: { tag },
      }),
      invalidatesTags: ['Customer'],
    }),
    getSegments: builder.query<Segment[], void>({
      query: () => '/api/segments',
      providesTags: ['Segment'],
    }),
    createSegment: builder.mutation<Segment, CreateSegmentRequest>({
      query: (body) => ({ url: '/api/segments', method: 'POST', body }),
      invalidatesTags: ['Segment'],
    }),
  }),
})

export const {
  useGetCustomersQuery,
  useCreateCustomerMutation,
  useAddCustomerTagMutation,
  useGetSegmentsQuery,
  useCreateSegmentMutation,
} = crmApi
