// ---------------------------------------------------------------------------
// SearchInput — debounced search input for tables
// ---------------------------------------------------------------------------

import { useRef, useCallback } from 'react';
import { Input } from 'antd';
import { SearchOutlined } from '@ant-design/icons';

interface SearchInputProps {
  /** Current search value */
  value: string;
  /** Called with debounced value */
  onChange: (value: string) => void;
  /** Placeholder text */
  placeholder?: string;
  /** Debounce delay in ms (default 300) */
  debounceMs?: number;
  /** Input width */
  style?: React.CSSProperties;
}

export default function SearchInput({
  value,
  onChange,
  placeholder = 'Search…',
  debounceMs = 300,
  style,
}: SearchInputProps) {
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const val = e.target.value;

      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }

      timerRef.current = setTimeout(() => {
        onChange(val);
      }, debounceMs);
    },
    [onChange, debounceMs],
  );

  const handleSearch = useCallback(
    (val: string) => {
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }
      onChange(val);
    },
    [onChange],
  );

  return (
    <Input.Search
      placeholder={placeholder}
      prefix={<SearchOutlined />}
      style={{ width: 260, ...style }}
      value={value}
      onChange={handleChange}
      onSearch={handleSearch}
      allowClear
    />
  );
}
