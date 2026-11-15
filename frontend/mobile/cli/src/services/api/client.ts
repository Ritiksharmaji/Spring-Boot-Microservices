import axios, {AxiosError} from 'axios';
import {env} from '@/config/env';

/** Error thrown by every API call: the most useful server message plus the HTTP status (if any). */
export class ApiError extends Error {
  constructor(message: string, readonly status?: number) {
    super(message);
    this.name = 'ApiError';
  }
}

/** Axios instance for the Spring Boot API (backend/monolith). Paths include the `/api` prefix. */
export const apiClient = axios.create({
  baseURL: env.apiUrl,
  timeout: 30_000,
});

/** Sets or clears `Authorization: Bearer <jwt>` (called by AuthContext). */
export const setAuthToken = (token: string | null): void => {
  if (token) {
    apiClient.defaults.headers.common.Authorization = `Bearer ${token}`;
  } else {
    delete apiClient.defaults.headers.common.Authorization;
  }
};

interface ProblemDetail {
  title?: string;
  detail?: string;
  errors?: Record<string, string>;
}

// Errors are RFC 7807 problem details {title, status, detail, errors?}: show the first field
// error, else the detail, else the title.
apiClient.interceptors.response.use(
  response => response,
  (error: AxiosError<ProblemDetail>) => {
    const body = error.response?.data;
    const fieldError = body?.errors ? Object.values(body.errors)[0] : undefined;
    const message = fieldError || body?.detail || body?.title || error.message || 'Request failed';
    return Promise.reject(new ApiError(message, error.response?.status));
  },
);
