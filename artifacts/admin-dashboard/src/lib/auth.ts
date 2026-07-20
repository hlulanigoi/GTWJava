import { setAuthTokenGetter } from '@workspace/api-client-react';

export function initAuth() {
  setAuthTokenGetter(() => localStorage.getItem('auth_token'));
}

export function setAuthToken(token: string | null) {
  if (token) {
    localStorage.setItem('auth_token', token);
  } else {
    localStorage.removeItem('auth_token');
  }
}

export function getAuthToken(): string | null {
  return localStorage.getItem('auth_token');
}