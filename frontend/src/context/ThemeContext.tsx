// ---------------------------------------------------------------------------
// ThemeContext — light / dark theme with localStorage persistence
// ---------------------------------------------------------------------------

import {
  createContext,
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { ConfigProvider, theme as antTheme } from 'antd';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------
export type ThemeMode = 'light' | 'dark';

export interface ThemeContextValue {
  theme: ThemeMode;
  toggleTheme: () => void;
  setTheme: (t: ThemeMode) => void;
}

// ---------------------------------------------------------------------------
// Storage key
// ---------------------------------------------------------------------------
const THEME_KEY = 'caas-theme';

function readStoredTheme(): ThemeMode {
  try {
    const stored = localStorage.getItem(THEME_KEY);
    if (stored === 'dark' || stored === 'light') return stored;
  } catch {
    // localStorage not available
  }
  return 'light';
}

function persistTheme(t: ThemeMode) {
  try {
    localStorage.setItem(THEME_KEY, t);
  } catch {
    // ignore
  }
}

// ---------------------------------------------------------------------------
// Context
// ---------------------------------------------------------------------------
export const ThemeContext = createContext<ThemeContextValue | undefined>(
  undefined,
);

// ---------------------------------------------------------------------------
// Provider — wraps children with Ant Design ConfigProvider + dark algorithm
// ---------------------------------------------------------------------------
export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setThemeState] = useState<ThemeMode>(readStoredTheme);

  const toggleTheme = useCallback(() => {
    setThemeState((prev) => {
      const next = prev === 'light' ? 'dark' : 'light';
      persistTheme(next);
      return next;
    });
  }, []);

  const setTheme = useCallback((t: ThemeMode) => {
    setThemeState(t);
    persistTheme(t);
  }, []);

  // Apply theme class to <html> for CSS-variable scoping
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  const value = useMemo<ThemeContextValue>(
    () => ({ theme, toggleTheme, setTheme }),
    [theme, toggleTheme, setTheme],
  );

  return (
    <ThemeContext.Provider value={value}>
      <ConfigProvider
        theme={{
          algorithm:
            theme === 'dark'
              ? antTheme.darkAlgorithm
              : antTheme.defaultAlgorithm,
          token: {
            colorPrimary: '#1677ff',
            borderRadius: 6,
          },
        }}
      >
        {children}
      </ConfigProvider>
    </ThemeContext.Provider>
  );
}
