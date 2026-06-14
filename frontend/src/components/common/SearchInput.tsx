// ---------------------------------------------------------------------------
// SearchInput — debounced search input for tables
// ---------------------------------------------------------------------------

import { useState, useEffect, useRef, useCallback } from 'react';
import { Input } from 'antd';
import { SearchOutlined } from '@ant-design/icons';

interface SearchInputProps {
  /** Current search value (controlled from parent) */
  value: string;
  /** Called with debounced value */
  onChange: (value: string) => void;
  /** Placeholder text */
  placeholder?: string;
  /** Debounce delay in ms (default 500) */
  debounceMs?: number;
  /** Input width */
  style?: React.CSSProperties;
}

export default function SearchInput({
  value,
  onChange,
  placeholder = 'Search…',
  debounceMs = 500,
  style,
}: SearchInputProps) {
  // Local state for instant typing feedback
  const [localValue, setLocalValue] = useState(value);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  // Track whether we are actively typing (to avoid overriding from parent while typing)
  const isTypingRef = useRef(false);

  // Sync from parent when value changes externally (e.g. clear)
  useEffect(() => {
    if (!isTypingRef.current) {
      setLocalValue(value);
    }
  }, [value]);

  const flush = useCallback(
    (val: string) => {
      if (timerRef.current) {
        clearTimeout(timerRef.current);
        timerRef.current = null;
      }
      isTypingRef.current = false;
      onChange(val);
    },
    [onChange],
  );

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const val = e.target.value;
      setLocalValue(val);
      isTypingRef.current = true;

      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }

      timerRef.current = setTimeout(() => {
        flush(val);
      }, debounceMs);
    },
    [flush, debounceMs],
  );

  const handleSearch = useCallback(
    (val: string) => {
      flush(val);
    },
    [flush],
  );

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }
    };
  }, []);

  return (
    <Input.Search
      placeholder={placeholder}
      prefix={<SearchOutlined />}
      style={{ width: 260, ...style }}
      value={localValue}
      onChange={handleChange}
      onSearch={handleSearch}
      allowClear
    />
  );
}
