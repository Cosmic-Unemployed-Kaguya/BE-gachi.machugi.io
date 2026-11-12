import { Inject, Service } from "typedi";
import { CustomSocket } from "../../../common/model/customSocket";
import { ChatService } from "../service/chatService";
import logger from "@common/util/logger";
import { SocketEvent } from "@decorator/socketEvent";

@Service()
export class SocketEventHandler {
    constructor(@Inject() private chatService: ChatService) {}

    @SocketEvent("close")
    public async close(socket: CustomSocket, code: number, reason: Buffer) {
        
        await this.chatService.exitRoom(socket);

        logger.debug(` 연결 종료 : ${socket.userNickname}`);
    }

    @SocketEvent("error")
    public error(socket: CustomSocket, error: Error) {
        logger.error(error);
    }
}
