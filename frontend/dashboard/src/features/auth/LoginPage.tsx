import { type FormEvent, useState } from 'react'
import { useLoginMutation, useRegisterMutation } from './authApi'

export function LoginPage() {
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)

  const [login, { isLoading: isLoggingIn }] = useLoginMutation()
  const [register, { isLoading: isRegistering }] = useRegisterMutation()

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    try {
      if (mode === 'register') {
        await register({ email, password }).unwrap()
      }
      await login({ email, password }).unwrap()
    } catch {
      setError(mode === 'register' ? 'Could not create that account.' : 'Invalid email or password.')
    }
  }

  const isSubmitting = isLoggingIn || isRegistering

  return (
    <main style={{ maxWidth: 360, margin: '80px auto' }}>
      <h1>CoreSuite</h1>
      <p className="muted">{mode === 'login' ? 'Sign in to continue.' : 'Create the first account.'}</p>

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        <label>
          Email
          <br />
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            style={{ width: '100%' }}
          />
        </label>
        <label>
          Password
          <br />
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            minLength={12}
            required
            style={{ width: '100%' }}
          />
        </label>
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={isSubmitting}>
          {mode === 'login' ? 'Sign in' : 'Create account'}
        </button>
      </form>

      <p className="muted" style={{ marginTop: 16 }}>
        {mode === 'login' ? "Don't have an account? " : 'Already have an account? '}
        <button
          type="button"
          className="secondary"
          onClick={() => {
            setMode(mode === 'login' ? 'register' : 'login')
            setError(null)
          }}
        >
          {mode === 'login' ? 'Create one' : 'Sign in'}
        </button>
      </p>
    </main>
  )
}
