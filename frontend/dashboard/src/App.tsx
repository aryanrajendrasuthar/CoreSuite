const DOMAINS = ['Product', 'CRM', 'Inventory', 'Orders', 'Reporting'] as const

function App() {
  return (
    <main>
      <h1>CoreSuite</h1>
      <p>Enterprise business management platform — Phase 0 scaffold.</p>
      <nav>
        <ul>
          {DOMAINS.map((domain) => (
            <li key={domain}>{domain}</li>
          ))}
        </ul>
      </nav>
    </main>
  )
}

export default App
