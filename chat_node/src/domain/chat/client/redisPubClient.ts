import Redis from "ioredis";
import { Service } from "typedi";
import config from "../../../common/config";
import { BaseRes, ChatEventType, ChatPayloadMap } from "../model/dto/base";

@Service()
export class RedisPubClient {
    private pubClient: Redis;

    constructor() {
        this.pubClient = new Redis(config.redisPubServer);
    }

    // redis에 메시지 발행
    // 제네릭이 붙어있지만 실제로 사용할때에는 타입이 자동으로 매핑되기에  직접 타입을 지정할 필요는 없음
    // 이벤트명과 DTO가 어긋나는 경우를 방지하기 위함
    public async publishMessage<E extends ChatEventType>(roomIdx: number, event: E, data: ChatPayloadMap[E]) {
        const payload: BaseRes<E> = {
            event: event,
            data: data,
        };

        this.pubClient.publish(`room:${roomIdx}:base`, JSON.stringify(payload));
    }

    public async correctMessage(roomIdx: number, data: ChatPayloadMap["correct"]) {
        const payload: BaseRes<"correct"> = {
            event: "correct",
            data: data,
        };

        this.pubClient.publish(`room:${roomIdx}:correct`, JSON.stringify(payload));
    }
}
