import { useGetKpisQuery } from './reportingApi'

export function ReportingPage() {
  const { data, isLoading, error } = useGetKpisQuery()

  return (
    <section>
      <h1>Reporting</h1>
      <p className="muted">Order and inventory KPIs, aggregated live from order-service and inventory-service.</p>

      {isLoading && <p className="muted">Loading KPIs…</p>}
      {error && <p className="error">Failed to load KPIs.</p>}

      {data && (
        <>
          <div className="stat-grid">
            <div className="card stat-tile">
              <p className="stat-label">Total orders</p>
              <p className="stat-value">{data.totalOrders}</p>
            </div>
            <div className="card stat-tile">
              <p className="stat-label">Total revenue</p>
              <p className="stat-value">${data.totalRevenue}</p>
            </div>
            <div className="card stat-tile">
              <p className="stat-label">Low stock alerts</p>
              <p className="stat-value">{data.lowStockCount}</p>
            </div>
          </div>

          <h2>Orders by status</h2>
          <table>
            <thead>
              <tr>
                <th>Status</th>
                <th>Count</th>
              </tr>
            </thead>
            <tbody>
              {Object.entries(data.ordersByStatus).map(([status, count]) => (
                <tr key={status}>
                  <td>
                    <span className="badge">{status}</span>
                  </td>
                  <td>{count}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <h2>Low stock items</h2>
          <table>
            <thead>
              <tr>
                <th>SKU</th>
                <th>Quantity</th>
                <th>Reorder threshold</th>
              </tr>
            </thead>
            <tbody>
              {data.lowStockItems.map((item) => (
                <tr key={item.sku}>
                  <td>{item.sku}</td>
                  <td className="error">{item.quantity}</td>
                  <td>{item.reorderThreshold}</td>
                </tr>
              ))}
              {data.lowStockItems.length === 0 && (
                <tr>
                  <td colSpan={3} className="muted">
                    Nothing below threshold.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </>
      )}
    </section>
  )
}
