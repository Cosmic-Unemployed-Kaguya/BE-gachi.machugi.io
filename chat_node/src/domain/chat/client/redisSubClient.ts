import Redis from "ioredis";
import { Service } from "typedi";
import config from "@common/config";
import logger from "@common/util/logger";
import { SUB_TOPICS } from "@enum/redisSubTopics";

@Service()
export class RedisSubClient {
    private subClient: Redis;

    constructor() {
        this.subClient = new Redis(config.redisSubServer);
    }

    // 방 구독, 필요한 모든 topic 일괄 구독
    public async onSubscribeRoom(roomIdx: number) {
        const channels = SUB_TOPICS.map((type) => `room:${roomIdx}:${type}`);
        await this.subClient.subscribe(...channels);
    }

    // 방 구독 해지
    public async unSubscribeRoom(roomIdx: number): Promise<void> {
        const channels = SUB_TOPICS.map((type) => `room:${roomIdx}:${type}`);
        await this.subClient.unsubscribe(...channels);
    }

    public async bindOnMessage(callback: (channel: string, message: string) => void | Promise<void>) {
        this.subClient.on("message", async (channel: string, message: string) => {
            try {
                await callback(channel, message);
            } catch (err) {
                logger.error(`[Redis 수신 처리 중 에러] ${channel}, 메시지: ${message}, ${err}`);
            }
        });
    }
}
