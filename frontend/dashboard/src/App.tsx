import { NavLink, Navigate, Route, Routes } from 'react-router-dom'
import { useGetMeQuery, useLogoutMutation } from './features/auth/authApi'
import { LoginPage } from './features/auth/LoginPage'
import { SettingsPage } from './features/auth/SettingsPage'
import { CustomersPage } from './features/crm/CustomersPage'
import { InventoryPage } from './features/inventory/InventoryPage'
import { OrdersPage } from './features/orders/OrdersPage'
import { ProductsPage } from './features/product/ProductsPage'
import { ReportingPage } from './features/reporting/ReportingPage'

const NAV_LINKS = [
  { to: '/products', label: 'Product' },
  { to: '/customers', label: 'CRM' },
  { to: '/inventory', label: 'Inventory' },
  { to: '/orders', label: 'Orders' },
  { to: '/reporting', label: 'Reporting' },
]

function App() {
  const { data: user, isLoading, isError } = useGetMeQuery()
  const [logout] = useLogoutMutation()

  if (isLoading) {
    return (
      <main>
        <p className="muted">Loading…</p>
      </main>
    )
  }

  if (isError || !user) {
    return <LoginPage />
  }

  return (
    <main>
      <h1>CoreSuite</h1>
      <nav>
        <ul>
          {NAV_LINKS.map((link) => (
            <li key={link.to}>
              <NavLink to={link.to} className={({ isActive }) => (isActive ? 'active' : undefined)}>
                {link.label}
              </NavLink>
            </li>
          ))}
          <li className="muted" style={{ marginLeft: 'auto' }}>
            <NavLink to="/settings" className={({ isActive }) => (isActive ? 'active' : undefined)}>
              {user.email}
            </NavLink>
          </li>
          <li>
            <button type="button" className="secondary" onClick={() => logout()}>
              Sign out
            </button>
          </li>
        </ul>
      </nav>
      <Routes>
        <Route path="/" element={<Navigate to="/products" replace />} />
        <Route path="/products" element={<ProductsPage />} />
        <Route path="/customers" element={<CustomersPage />} />
        <Route path="/inventory" element={<InventoryPage />} />
        <Route path="/orders" element={<OrdersPage />} />
        <Route path="/reporting" element={<ReportingPage />} />
        <Route path="/settings" element={<SettingsPage />} />
      </Routes>
    </main>
  )
}

export default App
