import { api } from '../../app/api'

export type ProductStatus = 'ACTIVE' | 'DISCONTINUED'

export interface Variant {
  id: number
  productId: number
  sku: string
  attributes: Record<string, string>
  price: string
  currency: string
}

export interface Product {
  id: number
  name: string
  description: string | null
  category: string | null
  status: ProductStatus
  variants: Variant[]
  createdAt: string
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

export interface CreateProductRequest {
  name: string
  description?: string
  category?: string
}

export interface AddVariantRequest {
  sku: string
  price: string
  currency?: string
  attributes?: Record<string, string>
}

export const productApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getProducts: builder.query<Page<Product>, void>({
      query: () => '/api/products?size=100',
      providesTags: ['Product'],
    }),
    createProduct: builder.mutation<Product, CreateProductRequest>({
      query: (body) => ({ url: '/api/products', method: 'POST', body }),
      invalidatesTags: ['Product'],
    }),
    addVariant: builder.mutation<Variant, { productId: number; body: AddVariantRequest }>({
      query: ({ productId, body }) => ({
        url: `/api/products/${productId}/variants`,
        method: 'POST',
        body,
      }),
      invalidatesTags: ['Product'],
    }),
  }),
})

export const { useGetProductsQuery, useCreateProductMutation, useAddVariantMutation } = productApi
