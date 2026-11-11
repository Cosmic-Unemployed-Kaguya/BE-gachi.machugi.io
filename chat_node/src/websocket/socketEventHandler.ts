import { CustomSocket } from "@dto/customSocket";
import { ExitRoomRes } from "@dto/exitRoom";
import logger from "@common/util/logger";
import { SocketEvent } from "@decorator/socketEvent";
import { Inject, Service } from "typedi";
import { RedisPubClient } from "../redis/redisPubClient";
import { RoomManager } from "../room/roomManager";
import { RoomGrpcClient } from '@common/grpc-client/roomClient';
import { BaseRes } from "@dto/base";


@Service()
export class SocketEventHandler{
    
    constructor(
        @Inject() private roomManager : RoomManager,
        @Inject() private redisPubClient : RedisPubClient,
        @Inject() private roomGrpcClient :RoomGrpcClient
    ){}


    @SocketEvent('close')
    public async close(socket : CustomSocket , code: number, reason: Buffer){
        
        const roomIdx = socket.roomIdx;
        
        // 끊어진 유저가 특정 방에 들어가 있었다면?
        if (roomIdx){

            // 1. 퇴장
            this.roomManager.exitRoom(socket);

            // 2. 해당 방 유저들에게 퇴장 알림
            const res : BaseRes = {
                event: "exit_room",
                data : {userNickname: socket.userNickname} as ExitRoomRes
            }

            this.redisPubClient.publishMessage(roomIdx , res);

            try{
                // 3. room 서비스에 알림
                await this.roomGrpcClient.exitUser({roomIdx : roomIdx, userIdx : socket.userIdx})
            }catch(err){
                logger.error('room서비스와 통신 중 에러 : ' + err);
            }

        }


        logger.info(`${socket.userNickname} 님과의 연결이 종료되었습니다.`)
        logger.debug(`CLOSE. uid: ${socket.userIdx}, code : ${code}, reason : ${reason}`)
    }

    @SocketEvent('error')
    public error(socket : CustomSocket, error: Error){
        logger.error(error);
        
    }

}