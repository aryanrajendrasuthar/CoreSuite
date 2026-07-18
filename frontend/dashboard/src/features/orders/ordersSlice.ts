import { createSlice } from '@reduxjs/toolkit'

export interface OrdersState {
  status: 'idle' | 'loading' | 'succeeded' | 'failed'
}

const initialState: OrdersState = { status: 'idle' }

const ordersSlice = createSlice({
  name: 'orders',
  initialState,
  reducers: {},
})

export default ordersSlice.reducer
