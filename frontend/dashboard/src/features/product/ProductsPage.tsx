import { type FormEvent, useState } from 'react'
import { useCreateProductMutation, useGetProductsQuery } from './productApi'

export function ProductsPage() {
  const { data, isLoading, error } = useGetProductsQuery()
  const [createProduct, { isLoading: isCreating }] = useCreateProductMutation()

  const [name, setName] = useState('')
  const [category, setCategory] = useState('')

  async function handleCreate(event: FormEvent) {
    event.preventDefault()
    await createProduct({ name, category: category || undefined }).unwrap()
    setName('')
    setCategory('')
  }

  return (
    <section>
      <h1>Products</h1>
      <p className="muted">Catalog, SKUs, pricing, and variants.</p>

      <form className="inline" onSubmit={handleCreate}>
        <label>
          Name
          <input value={name} onChange={(e) => setName(e.target.value)} required />
        </label>
        <label>
          Category
          <input value={category} onChange={(e) => setCategory(e.target.value)} />
        </label>
        <button type="submit" disabled={isCreating}>
          Add product
        </button>
      </form>

      {isLoading && <p className="muted">Loading products…</p>}
      {error && <p className="error">Failed to load products.</p>}

      {data && (
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Category</th>
              <th>Status</th>
              <th>Variants</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            {data.content.map((product) => (
              <tr key={product.id}>
                <td>{product.name}</td>
                <td>{product.category ?? '—'}</td>
                <td>
                  <span className="badge">{product.status}</span>
                </td>
                <td>{product.variants.length}</td>
                <td>{new Date(product.createdAt).toLocaleDateString()}</td>
              </tr>
            ))}
            {data.content.length === 0 && (
              <tr>
                <td colSpan={5} className="muted">
                  No products yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      )}
    </section>
  )
}
