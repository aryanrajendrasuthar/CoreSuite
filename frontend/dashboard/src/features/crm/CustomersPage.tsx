import { type FormEvent, useState } from 'react'
import {
  useCreateCustomerMutation,
  useCreateSegmentMutation,
  useGetCustomersQuery,
  useGetSegmentsQuery,
} from './crmApi'

export function CustomersPage() {
  const { data: customers, isLoading, error } = useGetCustomersQuery()
  const [createCustomer, { isLoading: isCreatingCustomer }] = useCreateCustomerMutation()
  const { data: segments } = useGetSegmentsQuery()
  const [createSegment, { isLoading: isCreatingSegment }] = useCreateSegmentMutation()

  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [segmentName, setSegmentName] = useState('')
  const [segmentTags, setSegmentTags] = useState('')

  async function handleCreateCustomer(event: FormEvent) {
    event.preventDefault()
    await createCustomer({ fullName, email }).unwrap()
    setFullName('')
    setEmail('')
  }

  async function handleCreateSegment(event: FormEvent) {
    event.preventDefault()
    const requiredTags = segmentTags
      .split(',')
      .map((tag) => tag.trim())
      .filter(Boolean)
    await createSegment({ name: segmentName, requiredTags }).unwrap()
    setSegmentName('')
    setSegmentTags('')
  }

  return (
    <>
      <section>
        <h1>Customers</h1>
        <p className="muted">Profiles and tag-based segmentation.</p>

        <form className="inline" onSubmit={handleCreateCustomer}>
          <label>
            Full name
            <input value={fullName} onChange={(e) => setFullName(e.target.value)} required />
          </label>
          <label>
            Email
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </label>
          <button type="submit" disabled={isCreatingCustomer}>
            Add customer
          </button>
        </form>

        {isLoading && <p className="muted">Loading customers…</p>}
        {error && <p className="error">Failed to load customers.</p>}

        {customers && (
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Company</th>
                <th>Tags</th>
              </tr>
            </thead>
            <tbody>
              {customers.content.map((customer) => (
                <tr key={customer.id}>
                  <td>{customer.fullName}</td>
                  <td>{customer.email}</td>
                  <td>{customer.company ?? '—'}</td>
                  <td>
                    {customer.tags.map((tag) => (
                      <span className="badge" key={tag} style={{ marginRight: 4 }}>
                        {tag}
                      </span>
                    ))}
                  </td>
                </tr>
              ))}
              {customers.content.length === 0 && (
                <tr>
                  <td colSpan={4} className="muted">
                    No customers yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </section>

      <section>
        <h2>Segments</h2>
        <form className="inline" onSubmit={handleCreateSegment}>
          <label>
            Segment name
            <input value={segmentName} onChange={(e) => setSegmentName(e.target.value)} required />
          </label>
          <label>
            Required tags (comma-separated)
            <input value={segmentTags} onChange={(e) => setSegmentTags(e.target.value)} required />
          </label>
          <button type="submit" disabled={isCreatingSegment}>
            Add segment
          </button>
        </form>

        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Required tags</th>
            </tr>
          </thead>
          <tbody>
            {segments?.map((segment) => (
              <tr key={segment.id}>
                <td>{segment.name}</td>
                <td>{segment.requiredTags.join(', ')}</td>
              </tr>
            ))}
            {segments?.length === 0 && (
              <tr>
                <td colSpan={2} className="muted">
                  No segments yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </section>
    </>
  )
}
