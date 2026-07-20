import { type FormEvent, useState } from 'react'
import { useGetCustomersQuery } from '../crm/crmApi'
import { useCreateOrderMutation, useGetOrdersQuery, useUpdateOrderStatusMutation } from './ordersApi'
import type { OrderStatus } from './ordersApi'

const STATUSES: OrderStatus[] = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED']

interface DraftLineItem {
  sku: string
  quantity: number
  unitPrice: string
}

export function OrdersPage() {
  const { data: orders, isLoading, error } = useGetOrdersQuery()
  const { data: customers } = useGetCustomersQuery()
  const [createOrder, { isLoading: isCreating }] = useCreateOrderMutation()
  const [updateStatus, { error: statusError }] = useUpdateOrderStatusMutation()

  const [customerId, setCustomerId] = useState<number | null>(null)
  const [lineItems, setLineItems] = useState<DraftLineItem[]>([])
  const [sku, setSku] = useState('')
  const [quantity, setQuantity] = useState(1)
  const [unitPrice, setUnitPrice] = useState('')

  function addLineItem() {
    if (!sku || !unitPrice) return
    setLineItems([...lineItems, { sku, quantity, unitPrice }])
    setSku('')
    setQuantity(1)
    setUnitPrice('')
  }

  async function handleCreateOrder(event: FormEvent) {
    event.preventDefault()
    if (customerId === null || lineItems.length === 0) return
    await createOrder({ customerId, lineItems }).unwrap()
    setCustomerId(null)
    setLineItems([])
  }

  return (
    <section>
      <h1>Orders</h1>
      <p className="muted">Lifecycle, status transitions, and history.</p>

      <form className="inline" onSubmit={handleCreateOrder}>
        <label>
          Customer
          <select
            value={customerId ?? ''}
            onChange={(e) => setCustomerId(e.target.value ? Number(e.target.value) : null)}
          >
            <option value="">Select…</option>
            {customers?.content.map((customer) => (
              <option key={customer.id} value={customer.id}>
                {customer.fullName}
              </option>
            ))}
          </select>
        </label>
        <button type="submit" disabled={isCreating || lineItems.length === 0}>
          Place order ({lineItems.length} item{lineItems.length === 1 ? '' : 's'})
        </button>
      </form>

      <form className="inline">
        <label>
          SKU
          <input value={sku} onChange={(e) => setSku(e.target.value)} />
        </label>
        <label>
          Quantity
          <input type="number" min={1} value={quantity} onChange={(e) => setQuantity(Number(e.target.value))} />
        </label>
        <label>
          Unit price
          <input value={unitPrice} onChange={(e) => setUnitPrice(e.target.value)} placeholder="9.99" />
        </label>
        <button type="button" className="secondary" onClick={addLineItem}>
          Add line item
        </button>
      </form>

      {lineItems.length > 0 && (
        <ul>
          {lineItems.map((item, index) => (
            <li key={index}>
              {item.quantity} × {item.sku} @ {item.unitPrice}
            </li>
          ))}
        </ul>
      )}

      {isLoading && <p className="muted">Loading orders…</p>}
      {error && <p className="error">Failed to load orders.</p>}
      {statusError && <p className="error">That status transition isn't allowed.</p>}

      {orders && (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Customer</th>
              <th>Status</th>
              <th>Total</th>
              <th>Update status</th>
            </tr>
          </thead>
          <tbody>
            {orders.content.map((order) => (
              <tr key={order.id}>
                <td>{order.id}</td>
                <td>{order.customerId}</td>
                <td>
                  <span className="badge">{order.status}</span>
                </td>
                <td>
                  {order.currency} {order.totalAmount}
                </td>
                <td>
                  <select
                    value=""
                    onChange={(e) => {
                      const toStatus = e.target.value as OrderStatus
                      if (toStatus) updateStatus({ orderId: order.id, toStatus })
                    }}
                  >
                    <option value="">Change to…</option>
                    {STATUSES.filter((status) => status !== order.status).map((status) => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </select>
                </td>
              </tr>
            ))}
            {orders.content.length === 0 && (
              <tr>
                <td colSpan={5} className="muted">
                  No orders yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      )}
    </section>
  )
}
