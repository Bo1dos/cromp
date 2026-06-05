// ---------------------------------------------------------------------------
// AuthContext — global authentication state & actions
// ---------------------------------------------------------------------------

import {
  createContext,
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { notification } from 'antd';

import * as authApi from '@/api/auth.api';
import * as usersApi from '@/api/users.api';
import { setAccessToken } from '@/api/client';
import type { UserResponse } from '@/types/user';
import type { LoginRequest, RegisterRequest } from '@/types/auth';

// ---------------------------------------------------------------------------
// Storage keys
// ---------------------------------------------------------------------------
const USER_ID_KEY = 'caas-user-id';

// ---------------------------------------------------------------------------
// Context shape
// ---------------------------------------------------------------------------
export interface AuthContextValue {
  user: UserResponse | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, displayName: string) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

// ---------------------------------------------------------------------------
// Provider
// ---------------------------------------------------------------------------
export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [user, setUser] = useState<UserResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // ---- Restore session on mount -------------------------------------------
  useEffect(() => {
    const storedUserId = localStorage.getItem(USER_ID_KEY);
    if (!storedUserId) {
      setIsLoading(false);
      return;
    }

    let cancelled = false;

    usersApi
      .getMe(storedUserId)
      .then((u) => {
        if (!cancelled) setUser(u);
      })
      .catch(() => {
        // 401 or any error → session expired / invalid
        localStorage.removeItem(USER_ID_KEY);
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // ---- Login mutation -----------------------------------------------------
  const loginMutation = useMutation({
    mutationFn: (req: LoginRequest) => authApi.login(req),
    onSuccess: (data) => {
      setAccessToken(data.accessToken);
      setUser(data.user);
      localStorage.setItem(USER_ID_KEY, data.user.userUuid);
    },
    onError: (err: Error) => {
      notification.error({
        message: 'Login failed',
        description: err.message || 'Invalid credentials. Please try again.',
      });
    },
  });

  // ---- Register mutation --------------------------------------------------
  const registerMutation = useMutation({
    mutationFn: (req: RegisterRequest) => authApi.register(req),
    onSuccess: (data) => {
      setAccessToken(data.accessToken);
      setUser(data.user);
      localStorage.setItem(USER_ID_KEY, data.user.userUuid);
    },
    onError: (err: Error) => {
      notification.error({
        message: 'Registration failed',
        description: err.message || 'Could not create account. Please try again.',
      });
    },
  });

  // ---- Public API ---------------------------------------------------------
  const login = useCallback(
    async (email: string, password: string) => {
      try {
        await loginMutation.mutateAsync({ email, password });
        navigate('/select-organization', { replace: true });
      } catch {
        // error notification already shown by onError handler
      }
    },
    [loginMutation, navigate],
  );

  const register = useCallback(
    async (email: string, password: string, displayName: string) => {
      try {
        await registerMutation.mutateAsync({ email, password, displayName });
        navigate('/select-organization', { replace: true });
      } catch {
        // error notification already shown by onError handler
      }
    },
    [registerMutation, navigate],
  );

  const logout = useCallback(() => {
    queryClient.clear();
    // Clear all auth-related localStorage keys but preserve theme preference
    const theme = localStorage.getItem('caas-theme');
    localStorage.clear();
    if (theme) {
      localStorage.setItem('caas-theme', theme);
    }
    setAccessToken(null);
    setUser(null);
    navigate('/login', { replace: true });
  }, [navigate, queryClient]);

  // ---- Memoised context value ---------------------------------------------
  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      isLoading,
      login,
      register,
      logout,
    }),
    [user, isLoading, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
