import { type FormEvent, useState } from 'react'
import {
  useAdjustStockMutation,
  useCreateWarehouseMutation,
  useGetReorderAlertsQuery,
  useGetStockByWarehouseQuery,
  useGetWarehousesQuery,
  useInitializeStockMutation,
} from './inventoryApi'

export function InventoryPage() {
  const { data: warehouses } = useGetWarehousesQuery()
  const [createWarehouse, { isLoading: isCreatingWarehouse }] = useCreateWarehouseMutation()
  const { data: alerts } = useGetReorderAlertsQuery()
  const [initializeStock, { isLoading: isInitializing }] = useInitializeStockMutation()
  const [adjustStock] = useAdjustStockMutation()

  const [warehouseName, setWarehouseName] = useState('')
  const [selectedWarehouseId, setSelectedWarehouseId] = useState<number | null>(null)
  const [sku, setSku] = useState('')
  const [quantity, setQuantity] = useState(0)
  const [reorderThreshold, setReorderThreshold] = useState(0)

  const { data: stockLevels } = useGetStockByWarehouseQuery(selectedWarehouseId ?? 0, {
    skip: selectedWarehouseId === null,
  })

  async function handleCreateWarehouse(event: FormEvent) {
    event.preventDefault()
    await createWarehouse({ name: warehouseName }).unwrap()
    setWarehouseName('')
  }

  async function handleInitializeStock(event: FormEvent) {
    event.preventDefault()
    if (selectedWarehouseId === null) return
    await initializeStock({ warehouseId: selectedWarehouseId, sku, quantity, reorderThreshold }).unwrap()
    setSku('')
    setQuantity(0)
    setReorderThreshold(0)
  }

  return (
    <>
      <section>
        <h1>Inventory</h1>
        <p className="muted">Warehouses, stock levels, and reorder alerts.</p>

        <form className="inline" onSubmit={handleCreateWarehouse}>
          <label>
            Warehouse name
            <input value={warehouseName} onChange={(e) => setWarehouseName(e.target.value)} required />
          </label>
          <button type="submit" disabled={isCreatingWarehouse}>
            Add warehouse
          </button>
        </form>
      </section>

      <section>
        <h2>Reorder alerts</h2>
        <table>
          <thead>
            <tr>
              <th>SKU</th>
              <th>Quantity</th>
              <th>Reorder threshold</th>
            </tr>
          </thead>
          <tbody>
            {alerts?.map((alert) => (
              <tr key={alert.id}>
                <td>{alert.sku}</td>
                <td className="error">{alert.quantity}</td>
                <td>{alert.reorderThreshold}</td>
              </tr>
            ))}
            {alerts?.length === 0 && (
              <tr>
                <td colSpan={3} className="muted">
                  Nothing below threshold.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </section>

      <section>
        <h2>Stock by warehouse</h2>
        <form className="inline">
          <label>
            Warehouse
            <select
              value={selectedWarehouseId ?? ''}
              onChange={(e) => setSelectedWarehouseId(e.target.value ? Number(e.target.value) : null)}
            >
              <option value="">Select…</option>
              {warehouses?.map((warehouse) => (
                <option key={warehouse.id} value={warehouse.id}>
                  {warehouse.name}
                </option>
              ))}
            </select>
          </label>
        </form>

        {selectedWarehouseId !== null && (
          <>
            <form className="inline" onSubmit={handleInitializeStock}>
              <label>
                SKU
                <input value={sku} onChange={(e) => setSku(e.target.value)} required />
              </label>
              <label>
                Quantity
                <input
                  type="number"
                  min={0}
                  value={quantity}
                  onChange={(e) => setQuantity(Number(e.target.value))}
                />
              </label>
              <label>
                Reorder threshold
                <input
                  type="number"
                  min={0}
                  value={reorderThreshold}
                  onChange={(e) => setReorderThreshold(Number(e.target.value))}
                />
              </label>
              <button type="submit" disabled={isInitializing}>
                Track stock
              </button>
            </form>

            <table>
              <thead>
                <tr>
                  <th>SKU</th>
                  <th>Quantity</th>
                  <th>Reorder threshold</th>
                  <th>Adjust</th>
                </tr>
              </thead>
              <tbody>
                {stockLevels?.map((stock) => (
                  <tr key={stock.id}>
                    <td>{stock.sku}</td>
                    <td>{stock.quantity}</td>
                    <td>{stock.reorderThreshold}</td>
                    <td>
                      <button
                        type="button"
                        className="secondary"
                        onClick={() => adjustStock({ stockId: stock.id, delta: -1 })}
                      >
                        −1
                      </button>{' '}
                      <button
                        type="button"
                        className="secondary"
                        onClick={() => adjustStock({ stockId: stock.id, delta: 1 })}
                      >
                        +1
                      </button>
                    </td>
                  </tr>
                ))}
                {stockLevels?.length === 0 && (
                  <tr>
                    <td colSpan={4} className="muted">
                      No stock tracked in this warehouse yet.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </>
        )}
      </section>
    </>
  )
}
