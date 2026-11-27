import { GrpcServer } from "@cosmic-unemployed-kaguya/grpc-express";
import { Inject } from "typedi";
import { RedisPubClient } from "../client/redisPubClient";
import { ChatNodeGrpcServiceService } from "@generated/machugi/chat/chat_node";

@GrpcServer(ChatNodeGrpcServiceService)
export default class ChatGrpcServer {
    constructor(@Inject() private redisPubClient: RedisPubClient) {}
}
