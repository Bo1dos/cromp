import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';

// ---------------------------------------------------------------------------
// Module-level org ID — updated by OrganizationContext (not localStorage)
// This avoids coupling Axios interceptors to React context.
// ---------------------------------------------------------------------------
let _activeOrgId: string | null = null;

/** Called by OrganizationContext to keep the interceptor in sync. */
export function setActiveOrgId(id: string | null) {
  _activeOrgId = id;
}

/** Read the current org ID (used by interceptors). */
export function getActiveOrgId(): string | null {
  return _activeOrgId;
}

const apiClient = axios.create({
  baseURL: '/api/v1',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ---------------------------------------------------------------------------
// Request interceptor — attaches X-Organization-ID from module-level state
// ---------------------------------------------------------------------------
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    if (_activeOrgId && config.headers) {
      config.headers['X-Organization-ID'] = _activeOrgId;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error),
);

// ---------------------------------------------------------------------------
// Response interceptor — global auth / error handling
// ---------------------------------------------------------------------------
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response) {
      const { status } = error.response;

      switch (status) {
        case 401:
          // Not authenticated → redirect to login
          window.location.href = '/login';
          break;
        case 403:
          console.error('[API] Forbidden — insufficient permissions.', error);
          break;
        default:
          if (status >= 500) {
            console.error('[API] Server error.', error);
          }
          break;
      }
    } else {
      console.error('[API] Network or request setup error.', error);
    }
    return Promise.reject(error);
  },
);

export default apiClient;
