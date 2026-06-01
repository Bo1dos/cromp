// ---------------------------------------------------------------------------
// Module declarations for untyped dependencies
// ---------------------------------------------------------------------------

declare module 'react-cron-generator' {
  import type { FC } from 'react';

  interface CronGeneratorProps {
    onChange?: (value: string, getVal?: () => string) => void;
    value?: string;
    showResultText?: boolean;
    showResultCron?: boolean;
  }

  const CronGenerator: FC<CronGeneratorProps>;
  export default CronGenerator;
}
