
export const MessageEvent = {
    JOIN_ROOM : 'join_room',
    CHAT : 'chat',
    EXIT_ROOM : 'exit_room'
} as const;
export type MessageEventType = typeof MessageEvent[keyof typeof MessageEvent]; 
