import { createSlice } from '@reduxjs/toolkit'

export interface ProductState {
  status: 'idle' | 'loading' | 'succeeded' | 'failed'
}

const initialState: ProductState = { status: 'idle' }

const productSlice = createSlice({
  name: 'product',
  initialState,
  reducers: {},
})

export default productSlice.reducer
