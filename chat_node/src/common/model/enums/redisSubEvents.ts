export const SUB_EVENTS = {
    CHAT:'chat',
    JOIN_ROOM:'join_room',
    EXIT_ROOM:'exit_room',
    GAME_START:'game_start',
    CORRECT:'correct',
    NEXT_QUIZ:'next_quiz',
    GAME_OVER: 'game_over'
} as const;

export type SubEventType = typeof SUB_EVENTS[keyof typeof SUB_EVENTS];