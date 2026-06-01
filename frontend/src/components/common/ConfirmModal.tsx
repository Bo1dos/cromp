// ---------------------------------------------------------------------------
// ConfirmModal — unified confirmation modal wrapping Ant Design Modal.confirm
// ---------------------------------------------------------------------------

import { Modal } from 'antd';
import { ExclamationCircleOutlined } from '@ant-design/icons';

interface ConfirmModalOptions {
  /** Dialog title */
  title: string;
  /** Dialog body content */
  content: string;
  /** Called when user confirms */
  onOk: () => void | Promise<void>;
  /** Whether this is a destructive action (uses danger styling) */
  danger?: boolean;
  /** Custom OK button text */
  okText?: string;
  /** Custom Cancel button text */
  cancelText?: string;
}

/**
 * Opens a unified confirmation modal.
 *
 * Usage:
 *   ConfirmModal.show({
 *     title: 'Delete job?',
 *     content: 'This action cannot be undone.',
 *     danger: true,
 *     onOk: () => deleteJob(),
 *   });
 */
export function showConfirm({
  title,
  content,
  onOk,
  danger = false,
  okText,
  cancelText,
}: ConfirmModalOptions) {
  Modal.confirm({
    title,
    icon: <ExclamationCircleOutlined />,
    content,
    okText: okText ?? (danger ? 'Delete' : 'Confirm'),
    cancelText: cancelText ?? 'Cancel',
    okType: danger ? 'danger' : 'primary',
    centered: true,
    onOk: () => {
      const result = onOk();
      // Support both sync and async handlers
      if (result instanceof Promise) {
        return result;
      }
    },
  });
}

const ConfirmModal = { show: showConfirm };
export default ConfirmModal;
