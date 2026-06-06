// ---------------------------------------------------------------------------
// ErrorBoundary — React 18 class-based error boundary for the entire app
// ---------------------------------------------------------------------------

import { Component, type ErrorInfo, type ReactNode } from 'react';
import { Button, Result } from 'antd';

interface ErrorBoundaryProps {
  children: ReactNode;
  /** Optional fallback UI override */
  fallback?: ReactNode;
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
}

export default class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    // Log to console (in future — send to monitoring service like Sentry)
    console.error('[ErrorBoundary] Uncaught error:', error);
    console.error('[ErrorBoundary] Component stack:', info.componentStack);
  }

  handleReload = () => {
    // Clear the error state and remount children
    this.setState({ hasError: false, error: null });
  };

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback;
      }

      return (
        <div
          style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            minHeight: '100vh',
            padding: 24,
            background: 'var(--color-bg-layout, #f5f5f5)',
          }}
        >
          <Result
            status="500"
            title="Something went wrong"
            subTitle={
              this.state.error?.message ??
              'An unexpected error occurred. Please try reloading the page.'
            }
            extra={
              <Button type="primary" onClick={this.handleReload}>
                Reload
              </Button>
            }
          />
        </div>
      );
    }

    return this.props.children;
  }
}
