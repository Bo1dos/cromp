// ---------------------------------------------------------------------------
// useLanguage — convenience hook for locale + translations
// ---------------------------------------------------------------------------

import { useContext } from 'react';
import {
  LanguageContext,
  type LanguageContextValue,
  type Locale,
} from '@/context/LanguageContext';

export type { Locale };

export function useLanguage(): LanguageContextValue {
  const ctx = useContext(LanguageContext);
  if (!ctx) {
    throw new Error('useLanguage must be used within LanguageProvider');
  }
  return ctx;
}
