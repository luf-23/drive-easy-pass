import { logout, useAuth } from '../stores/auth'

const apiBase = 'http://localhost:8080'

export async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const { token } = useAuth()
  const headers = new Headers(options?.headers)
  headers.set('Content-Type', 'application/json')

  if (token.value) {
    headers.set('Authorization', `Bearer ${token.value}`)
  }

  const response = await fetch(`${apiBase}${url}`, {
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
