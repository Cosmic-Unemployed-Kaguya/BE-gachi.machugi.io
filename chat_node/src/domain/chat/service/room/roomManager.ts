import { Inject, Service } from "typedi";
import { Room } from "./room";
import { WsError } from "@common/error/wsError";
import { CustomSocket } from "@common/model/customSocket";
import { RedisSubClient } from "@domain/chat/client/redisSubClient";
import { BaseRes } from "@dto/base";

/**
 * 여러 Room을 Idx별로 관리
 * 1. 각 Room끼리의 연결, 이동
 * 2. Room 생성, 삭제 등 생명주기 관리
 */
@Service()
export class RoomManager {
    constructor(@Inject() private redisSubClient: RedisSubClient) {}

    // Map < roomIdx , Room >
    private rooms: Map<number, Room> = new Map();

    public async sendMessage(roomIdx: number, data: BaseRes) {
        const room = this.rooms.get(roomIdx);

        // 해당 서버에 존재하지 않는 room.
        if (!room) {
            throw WsError.fromType("ROOM_NOT_FOUND");
        }

        await room.sendMessage(data);
    }

    public createRoom(roomIdx: number) {
        const room = this.rooms.get(roomIdx);
        if (room) {
            throw WsError.fromType("ROOM_ALREADY_EXISTS");
        }

        this.createRoomAndSub(roomIdx);
    }

    public joinRoom(roomIdx: number, socket: CustomSocket) {
        // 이미 해당 방에 들어가 있는 경우
        if (socket.roomIdx == roomIdx) {
            throw WsError.fromType("ALREADY_IN_ROOM");
        }
        // 다른 방에 들어가 있는 경우
        if (socket.roomIdx) {
            // 퇴장 먼저 하셈 처리
            // 여기서 바로 옮겨줄지? 사용자가 퇴장 후 재입장 하도록 할지?
            // 일단 바로 옮겨줍시다

            const prevRoom = this.rooms.get(socket.roomIdx);

            if (!prevRoom) throw WsError.fromType("ROOM_NOT_FOUND");

            socket.roomIdx = undefined;
            prevRoom.exitUser(socket);
        }

        const room = this.rooms.get(roomIdx);

        // 없으면 방 새로 만들고 입장
        // 실제로 방이 존재한다는 신뢰를 바탕으로 진행
        if (!room) {
            const newRoom = this.createRoomAndSub(roomIdx);

            newRoom.enterUser(socket);
            socket.roomIdx = roomIdx;
        } else {
            room.enterUser(socket);
            socket.roomIdx = roomIdx;
        }
    }

    public exitRoom(socket: CustomSocket) {
        if (!socket.roomIdx) {
            throw WsError.fromType("NOT_IN_ROOM");
        }

        const room = this.rooms.get(socket.roomIdx);
        if (!room) {
            throw WsError.fromType("ROOM_NOT_FOUND");
        }

        socket.roomIdx = undefined;
        room.exitUser(socket);
    }

    public deleteRoom(roomIdx: number) {
        const room = this.rooms.get(roomIdx);

        // 해당 서버에 해당 방이 없을 수 도 있음
        if (!room) return;

        // room 삭제 (일단 이정도만 하면 메모리에서 지워지는듯?)
        room.destroy();
        this.rooms.delete(roomIdx);

        // 구독 해지
        this.redisSubClient.unSubscribeRoom(roomIdx);
    }

    public getRoom(roomIdx: number): Room {
        const room = this.rooms.get(roomIdx);
        if (!room) {
            throw WsError.fromType("ROOM_NOT_FOUND");
        }
        return room;
    }

    private createRoomAndSub(roomIdx: number): Room {
        const newRoom = new Room();
        this.rooms.set(roomIdx, newRoom);
        // 해당 방 구독
        this.redisSubClient.onSubscribeRoom(roomIdx);

        return newRoom;
    }
}
