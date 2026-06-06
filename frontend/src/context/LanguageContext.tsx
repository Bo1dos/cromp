// ---------------------------------------------------------------------------
// LanguageContext — locale selection (en / ru) with localStorage persistence
// ---------------------------------------------------------------------------

import {
  createContext,
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';

import { en, ru, type Translations } from '@/i18n';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------
export type Locale = 'en' | 'ru';

export interface LanguageContextValue {
  locale: Locale;
  setLocale: (l: Locale) => void;
  t: Translations;
}

// ---------------------------------------------------------------------------
// Storage
// ---------------------------------------------------------------------------
const LOCALE_KEY = 'caas-locale';

function readStoredLocale(): Locale {
  try {
    const stored = localStorage.getItem(LOCALE_KEY);
    if (stored === 'en' || stored === 'ru') return stored;
  } catch {
    // ignore
  }
  return 'en';
}

function persistLocale(l: Locale) {
  try {
    localStorage.setItem(LOCALE_KEY, l);
  } catch {
    // ignore
  }
}

const translations: Record<Locale, Translations> = { en, ru };

// ---------------------------------------------------------------------------
// Context
// ---------------------------------------------------------------------------
export const LanguageContext = createContext<LanguageContextValue | undefined>(
  undefined,
);

// ---------------------------------------------------------------------------
// Provider
// ---------------------------------------------------------------------------
export function LanguageProvider({ children }: { children: ReactNode }) {
  const [locale, setLocaleState] = useState<Locale>(readStoredLocale);

  const setLocale = useCallback((l: Locale) => {
    setLocaleState(l);
    persistLocale(l);
  }, []);

  // Apply lang attribute to <html> for CSS / accessibility
  useEffect(() => {
    document.documentElement.setAttribute('lang', locale);
  }, [locale]);

  const value = useMemo<LanguageContextValue>(
    () => ({
      locale,
      setLocale,
      t: translations[locale],
    }),
    [locale, setLocale],
  );

  return (
    <LanguageContext.Provider value={value}>
      {children}
    </LanguageContext.Provider>
  );
}
