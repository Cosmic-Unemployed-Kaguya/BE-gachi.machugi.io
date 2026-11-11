
export const ServerEvent = {
    CONNECTION: 'connection',
    ERROR: 'error',
    HEADERS: 'headers',
    CLOSE: 'close',
    LISTENING: 'listening',
    WS_CLIENT_ERROR: 'wsClientError'
} as const;

export type ServerEventType = typeof ServerEvent[keyof typeof ServerEvent];

