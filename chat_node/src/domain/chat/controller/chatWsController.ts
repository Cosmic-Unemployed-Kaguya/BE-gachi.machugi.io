import { Inject, Service } from "typedi";
import { CustomSocket } from "../../../common/model/customSocket";
import { RedisPubClient } from "../client/redisPubClient";
import { JoinRoomReq, JoinSuccessRes } from "../model/dto/joinRoom";
import { MessageReq } from "../model/dto/message";
import { ChatService } from "../service/chatService";
import logger from "@common/util/logger";
import { MessageEvent } from "@decorator/messageEvent";

/**
 * 웹프레임워크의 Controller 격.
 * 요청/응답 데이터에 대한 관리.
 */
@Service()
export class ChatWsController {
    constructor(
        @Inject() private redisPubClient: RedisPubClient,
        @Inject() private chatService: ChatService,
    ) {}

    @MessageEvent("join_room", JoinRoomReq)
    public async joinRoom(socket: CustomSocket, req: JoinRoomReq) {

        const res: JoinSuccessRes = await this.chatService.joinRoom(
            socket,
            req,
        );

        // redis로 전파
        await this.redisPubClient.publishMessage(req.roomIdx, "join_room", res);

        logger.debug(`입장 : ${socket.userNickname}  : ${req.roomIdx}`);
    }

    @MessageEvent("chat", MessageReq)
    public async chat(socket: CustomSocket, req: MessageReq) {

        const data = await this.chatService.chat(socket, req);

        if (data[0] === true) {
            await this.redisPubClient.correctMessage(socket.roomIdx!, data[1]);
        } else {
            await this.redisPubClient.publishMessage(
                socket.roomIdx!,
                "chat",
                data[1],
            );
        }

        logger.debug(`채팅 : ${socket.userNickname}  : ${req.msg}`);
    }

    @MessageEvent("exit_room")
    public async exitRoom(socket: CustomSocket) {
        
        const res = await this.chatService.exitRoom(socket);

        await this.redisPubClient.publishMessage(
            socket.roomIdx!,
            "exit_room",
            res,
        );

        logger.debug(`퇴장 : ${socket.userNickname}`);
    }
}
