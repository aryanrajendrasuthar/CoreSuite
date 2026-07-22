import { api } from '../../app/api'

export interface User {
  id: number
  email: string
  roles: string[]
  totpEnabled: boolean
}

export interface Credentials {
  email: string
  password: string
  totpCode?: string
}

export interface TotpSetup {
  secret: string
  otpAuthUri: string
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
    setupTotp: builder.mutation<TotpSetup, void>({
      query: () => ({ url: '/api/auth/totp/setup', method: 'POST' }),
    }),
    enableTotp: builder.mutation<User, { code: string }>({
      query: (body) => ({ url: '/api/auth/totp/enable', method: 'POST', body }),
      invalidatesTags: ['CurrentUser'],
    }),
    disableTotp: builder.mutation<User, { code: string }>({
      query: (body) => ({ url: '/api/auth/totp/disable', method: 'POST', body }),
      invalidatesTags: ['CurrentUser'],
    }),
  }),
})

export const {
  useGetMeQuery,
  useRegisterMutation,
  useLoginMutation,
  useLogoutMutation,
  useSetupTotpMutation,
  useEnableTotpMutation,
  useDisableTotpMutation,
} = authApi
