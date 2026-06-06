// ---------------------------------------------------------------------------
// Russian translations
// ---------------------------------------------------------------------------

import type { Translations } from './en';

const ru: Translations = {
  // ---- Common ----
  common: {
    save: 'Сохранить',
    cancel: 'Отмена',
    delete: 'Удалить',
    edit: 'Редактировать',
    create: 'Создать',
    search: 'Поиск',
    loading: 'Загрузка…',
    noData: 'Нет данных',
    confirm: 'Подтвердить',
    back: 'Назад',
    actions: 'Действия',
    status: 'Статус',
    name: 'Имя',
    description: 'Описание',
    enabled: 'Включено',
    disabled: 'Отключено',
  },

  // ---- Navigation ----
  nav: {
    dashboard: 'Панель',
    jobs: 'Задачи',
    executions: 'Запуски',
    secrets: 'Секреты',
    analytics: 'Аналитика',
    members: 'Участники',
    audit: 'Аудит',
    profile: 'Профиль',
    settings: 'Настройки',
    signOut: 'Выйти',
  },

  // ---- Profile ----
  profile: {
    title: 'Профиль',
    accountDetails: 'Данные аккаунта',
    userUuid: 'UUID пользователя',
    email: 'Email',
    firstName: 'Имя',
    lastName: 'Фамилия',
    displayName: 'Отображаемое имя',
    organisations: 'Организации',
    registered: 'Зарегистрирован',
    lastUpdated: 'Последнее обновление',
  },

  // ---- Settings ----
  settings: {
    title: 'Настройки',
    appearance: 'Оформление',
    language: 'Язык',
    notifications: 'Уведомления',
    theme: 'Тема',
    themeDescription: 'Выберите светлое или тёмное оформление приложения.',
    darkMode: 'Тёмная тема',
    darkActive: 'Тёмная тема активна',
    darkInactive: 'Переключить на тёмную тему',
    languageDescription: 'Выберите предпочитаемый язык интерфейса.',
    languageComingSoon: 'Поддержка нескольких языков (i18n) будет реализована в следующем обновлении.',
    notificationsComingSoon: 'Модуль уведомлений в разработке. Сохранённые настройки будут применены после его запуска.',
  },

  // ---- Notifications ----
  notifications: {
    channels: 'Каналы уведомлений',
    eventTypes: 'Типы событий',
    emailChannel: 'Email',
    emailDesc: 'Получать уведомления по email',
    inAppChannel: 'В приложении',
    inAppDesc: 'Показывать уведомления внутри приложения',
    webhookChannel: 'Webhook',
    webhookDesc: 'Отправлять уведомления на свой URL',
    jobFailed: 'Задача упала',
    jobFailedDesc: 'Когда выполнение задачи завершилось ошибкой',
    jobSucceeded: 'Задача выполнена',
    jobSucceededDesc: 'Когда выполнение задачи успешно',
    jobDisabled: 'Задача отключена',
    jobDisabledDesc: 'Когда задача отключена вручную',
    jobTimeout: 'Тайм-аут задачи',
    jobTimeoutDesc: 'Когда задача превысила время выполнения',
  },

  // ---- Dashboard ----
  dashboard: {
    totalJobs: 'Всего задач',
    activeJobs: 'Активные',
    disabledJobs: 'Отключены',
    failedToday: 'Ошибок сегодня',
    recentExecutions: 'Последние запуски',
    executionsByDay: 'Запуски по дням',
    statusDistribution: 'Распределение статусов',
    createJob: 'Создать задачу',
    viewAll: 'Смотреть все',
  },

  // ---- Jobs ----
  jobs: {
    title: 'Задачи',
    createJob: 'Создать задачу',
    editJob: 'Редактировать',
    deleteJob: 'Удалить задачу',
    runNow: 'Запустить',
    enable: 'Включить',
    disable: 'Отключить',
    overview: 'Обзор',
    history: 'История',
    executions: 'Запуски',
    schedule: 'Расписание',
    version: 'Версия',
    queue: 'Очередь',
    priority: 'Приоритет',
    timeout: 'Тайм-аут',
    retryPolicy: 'Политика повторов',
    maxAttempts: 'Макс. попыток',
    backoff: 'Задержка',
    httpMethod: 'HTTP метод',
    httpUrl: 'HTTP URL',
    headers: 'Заголовки',
    requestBody: 'Тело запроса',
  },
};

export default ru;
