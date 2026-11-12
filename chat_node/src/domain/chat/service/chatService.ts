import { Inject, Service } from "typedi";
import { RoomGrpcClient } from "../client/roomClient";
import { RedisRepository } from "../repository/redisRepository";
import { RoomManager } from "./room/roomManager";
import { WsError } from "@common/error/wsError";
import { CustomSocket } from "@common/model/customSocket";
import { CorrectAnswerRes } from "@dto/correctAnswer";
import { ExitRoomRes } from "@dto/exitRoom";
import { JoinRoomReq, JoinSuccessRes } from "@dto/joinRoom";
import { MessageReq, MessageRes } from "@dto/message";
import { QuizAnswerReq } from "@dto/quiz";

@Service()
export class ChatService {
    constructor(
        @Inject() private redisRepository: RedisRepository,
        @Inject() private roomManager: RoomManager,
        @Inject() private roomGrpcClient: RoomGrpcClient,
    ) {}

    // 입장 검증 이후 입장 처리
    public async joinRoom(
        socket: CustomSocket,
        req: JoinRoomReq,
    ): Promise<JoinSuccessRes> {
        // 1.티켓 확인
        if (
            !(await this.redisRepository.checkEnterTicket(
                socket.userIdx,
                req.roomIdx,
                req.ticketUuid,
            ))
        ) {
            throw WsError.fromType("INVALID_REQUEST");
        }

        // 2.입장
        this.roomManager.joinRoom(req.roomIdx, socket);

        return { userNickname: socket.userNickname } as JoinSuccessRes;
    }

    // 일반 채팅과 정답 구분
    public async chat(
        socket: CustomSocket,
        req: MessageReq,
    ): Promise<[true, CorrectAnswerRes] | [false, MessageRes]> {

        if (!socket.roomIdx) {
            throw WsError.fromType("NOT_IN_ROOM");
        }

        // 정답 확인 로직
        const room = this.roomManager.getRoom(socket.roomIdx!);

        if (
            room.checkAnswer(req.msg) &&
            // 정답일시 :redis를 통해 선점(SET + NX + EX)
            (await this.redisRepository.setWithExpiration(
                socket.roomIdx!,
                socket.userIdx,
            ))
        ) {
            // 선점 성공 시
            return [
                true,
                {
                    msg: req.msg,
                    userNickname: socket.userNickname,
                    userIdx: socket.userIdx,
                } as CorrectAnswerRes,
            ];
        } else {
            // 그 외(1등 아님, 오답 등)
            return [
                false,
                {
                    msg: req.msg,
                    userNickname: socket.userNickname,
                } as MessageRes,
            ];
        }
    }

    public async exitRoom(socket: CustomSocket): Promise<ExitRoomRes> {

        if (!socket.roomIdx) {
            throw WsError.fromType("NOT_IN_ROOM");
        }

        const roomIdx: number = socket.roomIdx;

        // 1. 퇴장
        this.roomManager.exitRoom(socket);

        // 2. room 서비스에 알림
        try {
            await this.roomGrpcClient.exitUser({
                roomIdx: roomIdx,
                userIdx: socket.userIdx,
            });
        } catch (err) {
            throw WsError.fromType("INTERNAL_CONNECTION_ERROR");
        }

        // 3. 반환
        return { userNickname: socket.userNickname } as ExitRoomRes;
    }

    // 정답 세팅 및 lock 해제 (다음 문제를 위한 준비)
    public async setAnswerAndReleaseLock(roomIdx: number, req: QuizAnswerReq) {
        
        const room = this.roomManager.getRoom(roomIdx);

        room.setAnswer(req.answer);

        // 정답자 lock 해제
        await this.redisRepository.releaseLock(roomIdx);
    }
}
