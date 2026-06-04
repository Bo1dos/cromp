import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { notification } from 'antd';

// ---------------------------------------------------------------------------
// Module-level state — updated by AuthContext / OrganizationContext
// This avoids coupling Axios interceptors to React context.
// ---------------------------------------------------------------------------
let _activeOrgId: string | null = null;
let _accessToken: string | null = localStorage.getItem('caas-access-token');

/** Called by OrganizationContext to keep the interceptor in sync. */
export function setActiveOrgId(id: string | null) {
  _activeOrgId = id;
}

/** Read the current org ID (used by interceptors). */
export function getActiveOrgId(): string | null {
  return _activeOrgId;
}

/** Called by AuthContext after login/register/select-org to update the token. */
export function setAccessToken(token: string | null) {
  _accessToken = token;
  if (token) {
    localStorage.setItem('caas-access-token', token);
  } else {
    localStorage.removeItem('caas-access-token');
  }
}

/** Read the current access token (used by interceptors). */
export function getAccessToken(): string | null {
  return _accessToken;
}

const apiClient = axios.create({
  baseURL: '/api/v1',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ---------------------------------------------------------------------------
// Request interceptor — attaches Authorization & X-Organization-ID headers
// ---------------------------------------------------------------------------
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    if (_accessToken && config.headers) {
      config.headers['Authorization'] = `Bearer ${_accessToken}`;
    }
    if (_activeOrgId && config.headers) {
      config.headers['X-Organization-ID'] = _activeOrgId;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error),
);

// ---------------------------------------------------------------------------
// Response interceptor — global auth / error handling with notifications
// ---------------------------------------------------------------------------
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    // Prevent duplicate notifications for the same error
    // (some hooks already show their own messages for specific mutations)
    const alreadyHandled =
      error.config?.headers?.['X-Error-Handled'] === 'true';

    if (error.response) {
      const { status } = error.response;

      switch (status) {
        case 401:
          if (!alreadyHandled) {
            notification.error({
              message: 'Session expired',
              description: 'Redirecting to login…',
              duration: 3,
            });
          }
          // Redirect after short delay so the notification is visible
          setTimeout(() => {
            window.location.href = '/login';
          }, 1500);
          break;
        case 403:
          if (!alreadyHandled) {
            notification.warning({
              message: 'Access denied',
              description: 'You do not have permission to perform this action.',
            });
          }
          break;
        default:
          if (status >= 500) {
            console.error('[API] Server error.', error);
            if (!alreadyHandled) {
              notification.error({
                message: 'Server error',
                description: 'Please try again later.',
              });
            }
          }
          break;
      }
    } else {
      // Network error — no response received
      console.error('[API] Network or request setup error.', error);
      if (!alreadyHandled) {
        notification.error({
          message: 'Cannot connect to server',
          description: 'Check your connection and try again.',
        });
      }
    }
    return Promise.reject(error);
  },
);

export default apiClient;
