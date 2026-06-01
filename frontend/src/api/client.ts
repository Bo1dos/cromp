import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';

const apiClient = axios.create({
  baseURL: '/api/v1',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ---------------------------------------------------------------------------
// Request interceptor — attaches X-Organization-ID from localStorage
// TODO: replace localStorage read with a proper context/state once org selection
//       is wired through React context.
// ---------------------------------------------------------------------------
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const orgId = localStorage.getItem('X-Organization-ID');
    if (orgId && config.headers) {
      config.headers['X-Organization-ID'] = orgId;
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
