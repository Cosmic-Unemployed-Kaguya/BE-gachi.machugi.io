import { CorrectAnswerRes } from "./correctAnswer";
import { ExitRoomRes } from "./exitRoom";
import { JoinSuccessRes } from "./joinRoom";
import { MessageRes } from "./message";
import { QuizData } from "./quiz";

// 이벤트 명과 DTO를 매핑하는 인터페이스
export interface ChatPayloadMap {
    chat: MessageRes;
    join_room: JoinSuccessRes;
    exit_room: ExitRoomRes;
    correct: CorrectAnswerRes;
    next_quiz: QuizData;
    game_start: unknown;
    game_over: unknown;
}

export type ChatEventType = keyof ChatPayloadMap;

export type BaseRes<E extends ChatEventType = ChatEventType> = {
    [K in E]: { event: K; data: ChatPayloadMap[K] };
}[E];
