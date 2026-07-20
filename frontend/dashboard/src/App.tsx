import { NavLink, Navigate, Route, Routes } from 'react-router-dom'
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
        </ul>
      </nav>
      <Routes>
        <Route path="/" element={<Navigate to="/products" replace />} />
        <Route path="/products" element={<ProductsPage />} />
        <Route path="/customers" element={<CustomersPage />} />
        <Route path="/inventory" element={<InventoryPage />} />
        <Route path="/orders" element={<OrdersPage />} />
        <Route path="/reporting" element={<ReportingPage />} />
      </Routes>
    </main>
  )
}

export default App
