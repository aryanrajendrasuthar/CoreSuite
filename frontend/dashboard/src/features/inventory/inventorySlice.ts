import { createSlice } from '@reduxjs/toolkit'

export interface InventoryState {
  status: 'idle' | 'loading' | 'succeeded' | 'failed'
}

const initialState: InventoryState = { status: 'idle' }

const inventorySlice = createSlice({
  name: 'inventory',
  initialState,
  reducers: {},
})

export default inventorySlice.reducer
