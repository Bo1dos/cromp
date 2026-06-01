// ---------------------------------------------------------------------------
// useBreakpoint — antd Grid-based responsive breakpoint hook
// ---------------------------------------------------------------------------

import { Grid } from 'antd';

const { useBreakpoint: useAntdBreakpoint } = Grid;

export interface BreakpointInfo {
  xs: boolean;
  sm: boolean;
  md: boolean;
  lg: boolean;
  xl: boolean;
  xxl: boolean;
  /** true when screen is below 'md' (768px) — mobile */
  isMobile: boolean;
  /** true when screen is between 'md' and 'lg' — tablet */
  isTablet: boolean;
  /** true when screen is 'lg' or above — desktop */
  isDesktop: boolean;
}

export function useBreakpoint(): BreakpointInfo {
  const screens = useAntdBreakpoint();

  return {
    xs: !!screens.xs,
    sm: !!screens.sm,
    md: !!screens.md,
    lg: !!screens.lg,
    xl: !!screens.xl,
    xxl: !!screens.xxl,
    isMobile: !screens.md && !screens.lg && !screens.xl && !screens.xxl,
    isTablet: !!screens.md && !screens.lg && !screens.xl && !screens.xxl,
    isDesktop: !!screens.lg || !!screens.xl || !!screens.xxl,
  };
}
