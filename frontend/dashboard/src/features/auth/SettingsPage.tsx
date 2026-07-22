import { type FormEvent, useState } from 'react'
import { useDisableTotpMutation, useEnableTotpMutation, useGetMeQuery, useSetupTotpMutation } from './authApi'

export function SettingsPage() {
  const { data: user } = useGetMeQuery()
  const [setupTotp, { isLoading: isSettingUp }] = useSetupTotpMutation()
  const [enableTotp, { isLoading: isEnabling }] = useEnableTotpMutation()
  const [disableTotp, { isLoading: isDisabling }] = useDisableTotpMutation()

  const [pendingSecret, setPendingSecret] = useState<{ secret: string; otpAuthUri: string } | null>(null)
  const [code, setCode] = useState('')
  const [disableCode, setDisableCode] = useState('')
  const [error, setError] = useState<string | null>(null)

  async function handleStartSetup() {
    setError(null)
    const result = await setupTotp().unwrap()
    setPendingSecret(result)
    setCode('')
  }

  async function handleConfirmEnable(event: FormEvent) {
    event.preventDefault()
    setError(null)
    try {
      await enableTotp({ code }).unwrap()
      setPendingSecret(null)
      setCode('')
    } catch {
      setError('Invalid code. Try again.')
    }
  }

  async function handleDisable(event: FormEvent) {
    event.preventDefault()
    setError(null)
    try {
      await disableTotp({ code: disableCode }).unwrap()
      setDisableCode('')
    } catch {
      setError('Invalid code. Try again.')
    }
  }

  if (!user) {
    return null
  }

  return (
    <section>
      <h1>Account settings</h1>
      <p className="muted">Signed in as {user.email}.</p>

      <h2>Two-factor authentication</h2>
      {user.totpEnabled ? (
        <>
          <p className="muted">Two-factor authentication is enabled on your account.</p>
          <form className="inline" onSubmit={handleDisable}>
            <label>
              Authenticator code
              <input
                type="text"
                inputMode="numeric"
                autoComplete="one-time-code"
                value={disableCode}
                onChange={(e) => setDisableCode(e.target.value)}
                maxLength={6}
                required
              />
            </label>
            <button type="submit" disabled={isDisabling}>
              Disable
            </button>
          </form>
        </>
      ) : pendingSecret ? (
        <>
          <p className="muted">
            Add this to your authenticator app (Google Authenticator, 1Password, etc.), then confirm with a
            generated code.
          </p>
          <p>
            <strong>Secret:</strong> <code>{pendingSecret.secret}</code>
          </p>
          <p style={{ wordBreak: 'break-all' }}>
            <strong>Setup URI:</strong> <code>{pendingSecret.otpAuthUri}</code>
          </p>
          <form className="inline" onSubmit={handleConfirmEnable}>
            <label>
              Authenticator code
              <input
                type="text"
                inputMode="numeric"
                autoComplete="one-time-code"
                value={code}
                onChange={(e) => setCode(e.target.value)}
                maxLength={6}
                required
                autoFocus
              />
            </label>
            <button type="submit" disabled={isEnabling}>
              Confirm and enable
            </button>
          </form>
        </>
      ) : (
        <>
          <p className="muted">Two-factor authentication is not enabled on your account.</p>
          <button type="button" onClick={handleStartSetup} disabled={isSettingUp}>
            Enable two-factor authentication
          </button>
        </>
      )}
      {error && <p className="error">{error}</p>}
    </section>
  )
}
