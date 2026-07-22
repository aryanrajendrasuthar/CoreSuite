import { type FormEvent, useState } from 'react'
import { useLoginMutation, useRegisterMutation } from './authApi'

function isTotpRequired(error: unknown): boolean {
  if (error && typeof error === 'object' && 'data' in error) {
    const data = (error as { data?: unknown }).data
    if (data && typeof data === 'object' && 'totpRequired' in data) {
      return Boolean((data as { totpRequired?: boolean }).totpRequired)
    }
  }
  return false
}

export function LoginPage() {
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [totpCode, setTotpCode] = useState('')
  const [needsTotpCode, setNeedsTotpCode] = useState(false)
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
      await login({ email, password, totpCode: needsTotpCode ? totpCode : undefined }).unwrap()
    } catch (err) {
      if (isTotpRequired(err)) {
        setNeedsTotpCode(true)
        setError(totpCode ? 'Invalid code. Try again.' : 'Enter the 6-digit code from your authenticator app.')
        return
      }
      setError(mode === 'register' ? 'Could not create that account.' : 'Invalid email or password.')
    }
  }

  function switchMode() {
    setMode(mode === 'login' ? 'register' : 'login')
    setNeedsTotpCode(false)
    setTotpCode('')
    setError(null)
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
            disabled={needsTotpCode}
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
            disabled={needsTotpCode}
            style={{ width: '100%' }}
          />
        </label>
        {needsTotpCode && (
          <label>
            Authenticator code
            <br />
            <input
              type="text"
              inputMode="numeric"
              autoComplete="one-time-code"
              value={totpCode}
              onChange={(e) => setTotpCode(e.target.value)}
              maxLength={6}
              autoFocus
              required
              style={{ width: '100%' }}
            />
          </label>
        )}
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={isSubmitting}>
          {mode === 'login' ? 'Sign in' : 'Create account'}
        </button>
      </form>

      <p className="muted" style={{ marginTop: 16 }}>
        {mode === 'login' ? "Don't have an account? " : 'Already have an account? '}
        <button type="button" className="secondary" onClick={switchMode}>
          {mode === 'login' ? 'Create one' : 'Sign in'}
        </button>
      </p>
    </main>
  )
}
