import { configureStore } from '@reduxjs/toolkit'
import productReducer from '../features/product/productSlice'
import crmReducer from '../features/crm/crmSlice'
import inventoryReducer from '../features/inventory/inventorySlice'
import ordersReducer from '../features/orders/ordersSlice'
import reportingReducer from '../features/reporting/reportingSlice'

export const store = configureStore({
  reducer: {
    product: productReducer,
    crm: crmReducer,
    inventory: inventoryReducer,
    orders: ordersReducer,
    reporting: reportingReducer,
  },
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch
