import { Inject, Service } from "typedi";
import { RedisSubClient } from "../client/redisSubClient";
import { BaseRes } from "../model/dto/base";
import { QuizAnswerReq } from "../model/dto/quiz";
import { ChatService } from "../service/chatService";
import { RoomManager } from "../service/room/roomManager";
import logger from "@common/util/logger";

@Service()
export class ChatRedisSubController {
    constructor(
        @Inject() private redisSubClient: RedisSubClient,
        @Inject() private roomManager: RoomManager,
        @Inject() private chatService: ChatService,
    ) {
        this.redisSubClient.bindOnMessage(this.onMessage.bind(this));
    }

    // redis에서 메시지 수신 시
    // 당장은 case가 두개밖에 없어서 이렇게 씀. 후에 고...칠까..?
    private async onMessage(channel: string, message: string) {
        
        logger.debug(`[Redis 수신] 채널: ${channel}, 원본 메시지: ${message}`);

        // channel = room:{roomIdx}:{subTopic}
        const [, roomIdxStr, subTopic] = channel.split(":");

        const roomIdx: number = Number(roomIdxStr);

        const payload: BaseRes = JSON.parse(message);

        switch (payload.event) {
            // game에게 다음 문제 요청받으면, 유저한테 문제 던지면서 저장된 정답 수정
            case "next_quiz":
                const req: QuizAnswerReq = await QuizAnswerReq.parseAsync(
                    payload.data,
                );

                // room에 정답 저장 및 lock 해제
                await this.chatService.setAnswerAndReleaseLock(roomIdx, req);

                const res: BaseRes = {
                    event: "next_quiz",
                    data: {
                        problemText: req.problemText,
                        problemUrl: req.problemUrl,
                        sortOrder: req.sortOrder,
                        type: req.type,
                    },
                };

                // client에 다음 문제 전송
                await this.roomManager.sendMessage(roomIdx, res);

                break;

            default:
                // 그 외 이벤트는 그대로 client에 전송
                await this.roomManager.sendMessage(roomIdx, payload);
                break;
        }
    }
}
