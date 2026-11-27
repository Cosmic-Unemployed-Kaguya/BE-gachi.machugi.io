export const SocketEvent = {
    CLOSE: "close",
    ERROR: "error",
    UPGRADE: "upgrade",
    MESSAGE: "message",
    OPEN: "open",
    PING: "ping",
    PONG: "pong",
    REDIRECT: "redirect",
    UNEXPECTED_RESPONSE: "unexpected-response",
} as const;

export type SocketEventType = (typeof SocketEvent)[keyof typeof SocketEvent];
