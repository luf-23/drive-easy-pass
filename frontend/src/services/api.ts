import { logout, useAuth } from '../stores/auth'

export async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const { token } = useAuth()
  const headers = new Headers(options?.headers)
  headers.set('Content-Type', 'application/json')

  // Always go through Vite proxy in dev; avoid duplicating /api for legacy callers.
  const normalizedUrl = url.startsWith('/api/') ? url : `/api${url}`

  if (token.value) {
    headers.set('Authorization', `Bearer ${token.value}`)
  }

  const response = await fetch(normalizedUrl, {
    ...options,
    headers,
  })

  if (!response.ok) {
    const message = await readError(response)
    if (response.status === 401) {
      logout()
    }
    throw new Error(message || `Request failed: ${response.status}`)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}

async function readError(response: Response) {
  try {
    const body = await response.json()
    return body.message as string
  } catch {
    return ''
  }
}
