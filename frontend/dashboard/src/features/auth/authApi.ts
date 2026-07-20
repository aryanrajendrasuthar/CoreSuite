import { api } from '../../app/api'

export interface User {
  id: number
  email: string
  roles: string[]
}

export interface Credentials {
  email: string
  password: string
}

export const authApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getMe: builder.query<User, void>({
      query: () => '/api/auth/me',
      providesTags: ['CurrentUser'],
    }),
    register: builder.mutation<User, Credentials>({
      query: (body) => ({ url: '/api/auth/register', method: 'POST', body }),
    }),
    login: builder.mutation<User, Credentials>({
      query: (body) => ({ url: '/api/auth/login', method: 'POST', body }),
      invalidatesTags: ['CurrentUser'],
    }),
    logout: builder.mutation<void, void>({
      query: () => ({ url: '/api/auth/logout', method: 'POST' }),
      invalidatesTags: ['CurrentUser'],
    }),
  }),
})

export const { useGetMeQuery, useRegisterMutation, useLoginMutation, useLogoutMutation } = authApi
