// ---------------------------------------------------------------------------
// English translations
// ---------------------------------------------------------------------------

const en = {
  // ---- Common ----
  common: {
    save: 'Save',
    cancel: 'Cancel',
    delete: 'Delete',
    edit: 'Edit',
    create: 'Create',
    search: 'Search',
    loading: 'Loading…',
    noData: 'No data',
    confirm: 'Confirm',
    back: 'Back',
    actions: 'Actions',
    status: 'Status',
    name: 'Name',
    description: 'Description',
    enabled: 'Enabled',
    disabled: 'Disabled',
  },

  // ---- Navigation ----
  nav: {
    dashboard: 'Dashboard',
    jobs: 'Jobs',
    executions: 'Executions',
    secrets: 'Secrets',
    analytics: 'Analytics',
    members: 'Members',
    audit: 'Audit',
    profile: 'Profile',
    settings: 'Settings',
    signOut: 'Sign Out',
  },

  // ---- Profile ----
  profile: {
    title: 'Profile',
    accountDetails: 'Account Details',
    userUuid: 'User UUID',
    email: 'Email',
    firstName: 'First Name',
    lastName: 'Last Name',
    displayName: 'Display Name',
    organisations: 'Organisations',
    registered: 'Registered',
    lastUpdated: 'Last Updated',
  },

  // ---- Settings ----
  settings: {
    title: 'Settings',
    appearance: 'Appearance',
    language: 'Language',
    notifications: 'Notifications',
    theme: 'Theme',
    themeDescription: 'Choose between light and dark appearance for the application.',
    darkMode: 'Dark Mode',
    darkActive: 'Dark theme is active',
    darkInactive: 'Switch to dark theme',
    languageDescription: 'Select your preferred language for the application interface.',
    languageComingSoon: 'Multi-language support (i18n) will be implemented in a future update.',
    notificationsComingSoon: 'Notification preferences are now synchronized with the server.',
  },

  // ---- Notifications ----
  notifications: {
    title: 'Notifications',
    all: 'All',
    channels: 'Notification Channels',
    eventTypes: 'Event Types',
    emailChannel: 'Email',
    emailDesc: 'Receive notifications via email',
    inAppChannel: 'In-App',
    inAppDesc: 'Show notifications inside the application',
    webhookChannel: 'Webhook',
    webhookDesc: 'Send notifications to a custom URL',
    jobFailed: 'Job Failed',
    jobFailedDesc: 'When a job execution fails',
    jobSucceeded: 'Job Succeeded',
    jobSucceededDesc: 'When a job execution succeeds',
    jobDisabled: 'Job Disabled',
    jobDisabledDesc: 'When a job is manually disabled',
    jobTimeout: 'Job Timeout',
    jobTimeoutDesc: 'When a job exceeds its timeout',
    markAllRead: 'Mark all read',
    viewAll: 'View all',
    empty: 'No notifications yet',
    noNotifications: 'No notifications',
    unread: 'Unread',
    read: 'Read',
  },

  // ---- Webhooks ----
  webhooks: {
    title: 'Webhooks',
    addWebhook: 'Add Webhook',
    editWebhook: 'Edit Webhook',
    url: 'Webhook URL',
    urlPlaceholder: 'https://hooks.example.com/cromp',
    eventTypes: 'Event Types',
    secret: 'Secret',
    secretWarning: 'Save this secret now — it won\'t be shown again!',
    copySecret: 'Copy Secret',
    rotateSecret: 'Rotate',
    deliveries: 'Delivery History',
    noDeliveries: 'No deliveries yet',
    deleteConfirm: 'Delete this webhook?',
    rotateConfirm: 'Rotate the secret? This will invalidate the old one.',
    noWebhooks: 'No webhooks configured',
  },

  // ---- Dashboard ----
  dashboard: {
    totalJobs: 'Total Jobs',
    activeJobs: 'Active',
    disabledJobs: 'Disabled',
    failedToday: 'Failed Today',
    recentExecutions: 'Recent Executions',
    executionsByDay: 'Executions by Day',
    statusDistribution: 'Status Distribution',
    createJob: 'Create Job',
    viewAll: 'View All',
  },

  // ---- Jobs ----
  jobs: {
    title: 'Jobs',
    createJob: 'Create Job',
    editJob: 'Edit Job',
    deleteJob: 'Delete Job',
    runNow: 'Run Now',
    enable: 'Enable',
    disable: 'Disable',
    overview: 'Overview',
    history: 'History',
    executions: 'Executions',
    schedule: 'Schedule',
    version: 'Version',
    queue: 'Queue',
    priority: 'Priority',
    timeout: 'Timeout',
    retryPolicy: 'Retry Policy',
    maxAttempts: 'Max Attempts',
    backoff: 'Backoff',
    httpMethod: 'HTTP Method',
    httpUrl: 'HTTP URL',
    headers: 'Headers',
    requestBody: 'Request Body',
  },
};

export default en;
export type Translations = typeof en;
