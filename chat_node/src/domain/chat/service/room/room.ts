import { WebSocket } from "ws";
import { CustomSocket } from "@common/model/customSocket";
import { BaseRes } from "@dto/base";

/**
 * 하나의 room을 관리하는 클래스
 * 1. room에 입장한 유저 관리
 * 2. room에 입장한 유저들에게 메시지 일괄 전송
 * 3. room의 정답 관리
 */
export class Room {
    private clients: Set<CustomSocket> = new Set();

    private currentAnswer: string[] = [];

    constructor() {}

    public getUsers() {
        return this.clients;
    }

    public enterUser(socket: CustomSocket) {
        this.clients.add(socket);
    }

    public exitUser(socket: CustomSocket) {
        this.clients.delete(socket);
    }

    public async sendMessage(data: BaseRes) {
        const message = JSON.stringify(data);
        this.clients.forEach((client) => {
            if (client.readyState === WebSocket.OPEN) {
                client.send(message);
            }
        });
    }

    public destroy() {
        this.clients.forEach((user) => {
            user.roomIdx = undefined;
        });
        this.clients.clear();
    }

    public setAnswer(answer: string[] | undefined) {
        this.currentAnswer = answer ?? [];
    }

    public checkAnswer(userAnswer: string): boolean {
        // 정답 확인 로직!

        // 1. 유저 입력값을 normalize
        const normalUserAnswer = this.normalizeAnswer(userAnswer);

        // 1.1 만약 없으면 false
        if (!normalUserAnswer || normalUserAnswer.length === 0) return false;

        // 2. 저장 되어있는 정답들을 normalize 하며 비교
        // .some > .map과 유사하게 배열 내 모든 값을 반복하지만, 하나라도 true가 나오면 true를 반환하며 순환 종료
        const correct = this.currentAnswer.some((answer) => {
            const normalAnswer = this.normalizeAnswer(answer);
            return normalAnswer === normalUserAnswer;
        });

        return correct;
    }

    private normalizeAnswer(answer: string): string | null {
        if (!answer) {
            return null;
        }

        // 모든 공백 제거, 소문자로 변환
        return answer.replace(/\s+/g, "").toLowerCase();
    }
}
