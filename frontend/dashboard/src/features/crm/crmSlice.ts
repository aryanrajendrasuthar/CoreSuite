import { createSlice } from '@reduxjs/toolkit'

export interface CrmState {
  status: 'idle' | 'loading' | 'succeeded' | 'failed'
}

const initialState: CrmState = { status: 'idle' }

const crmSlice = createSlice({
  name: 'crm',
  initialState,
  reducers: {},
})

export default crmSlice.reducer
