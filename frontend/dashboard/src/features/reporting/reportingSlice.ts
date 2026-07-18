import { createSlice } from '@reduxjs/toolkit'

export interface ReportingState {
  status: 'idle' | 'loading' | 'succeeded' | 'failed'
}

const initialState: ReportingState = { status: 'idle' }

const reportingSlice = createSlice({
  name: 'reporting',
  initialState,
  reducers: {},
})

export default reportingSlice.reducer
