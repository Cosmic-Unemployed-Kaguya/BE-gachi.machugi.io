import "reflect-metadata";
import Container, { Inject, Service } from "typedi";
import { RawData, Server } from "ws";
import { CustomSocket } from "../model/customSocket";
import { EventMap, EventMetadata } from "../model/event";
import { WsError } from "@common/error/wsError";
import { catchAsyncEvent } from "@common/error/wsErrorHandler";
import { BaseReq } from "@common/model/baseReq";

export interface WsHandlerSet {
    server: object;
    socket: object;
    message: object;
}
@Service()
export class WsLoader {
    private serverEvList: EventMetadata[];
    private socketEvList: EventMetadata[];
    private messageEvList: EventMap = new Map();

    private serverHandler: object;
    private socketHandler: object;
    private messageHandler: object;

    constructor(@Inject() private ws: Server) {}

    public async startLoadWs(EventHandlers: WsHandlerSet) {
        await this.bindServerEv(EventHandlers);
    }

    private async bindServerEv(EventHandlers: WsHandlerSet) {
        this.serverHandler = Container.get(EventHandlers.server);
        this.socketHandler = Container.get(EventHandlers.socket);
        this.messageHandler = Container.get(EventHandlers.message);

        this.serverEvList = Reflect.getMetadata("ws:server", this.serverHandler.constructor);
        this.socketEvList = Reflect.getMetadata("ws:socket", this.socketHandler.constructor);

        const rawMessageMeta: EventMetadata[] =
            Reflect.getMetadata("ws:message", this.messageHandler.constructor) || [];
        rawMessageMeta.forEach((ev) => {
            this.messageEvList.set(ev.event, ev.methodName);
        });

        this.serverEvList.forEach((serverEv) => {
            if (serverEv.event === "connection") {
                this.ws.on(
                    "connection",
                    catchAsyncEvent(async (socket: CustomSocket, request: any) => {
                        const onConnect = (this.serverHandler as any)[serverEv.methodName].bind(this.serverHandler);
                        await onConnect(socket, request);
                        this.bindSocketEv(socket);
                    }),
                );
            } else {
                const serverHandler = (this.serverHandler as any)[serverEv.methodName].bind(this.serverHandler);
                this.ws.on(serverEv.event, catchAsyncEvent(serverHandler));
            }
        });

        if (!this.serverEvList.some((ev) => ev.event === "connection")) {
            this.ws.on(
                "connection",
                catchAsyncEvent(async (socket: CustomSocket) => {
                    this.bindSocketEv(socket);
                }),
            );
        }
    }

    private bindSocketEv(socket: CustomSocket) {
        socket.on(
            "message",
            catchAsyncEvent(async (sock: CustomSocket, data: RawData) => {
                const strData = data.toString("utf-8");
                // const baseReq: BaseReq = JSON.parse(strData);
                const parsedData = JSON.parse(strData);
                const baseReq: BaseReq = await BaseReq.parseAsync(parsedData);

                await this.bindMessageEv(sock, baseReq);
            }).bind(null, socket),
        );

        this.socketEvList.forEach((socketEv) => {
            const socketHandler = (this.socketHandler as any)[socketEv.methodName].bind(this.socketHandler);
            socket.on(socketEv.event, catchAsyncEvent(socketHandler).bind(null, socket));
        });
    }

    private async bindMessageEv(socket: CustomSocket, baseReq: BaseReq) {
        const methodName = this.messageEvList.get(baseReq.event);

        if (!methodName) {
            throw WsError.fromType("NOT_FOUND_EVENT_ERROR");
        }

        const msgHandler = (this.messageHandler as any)[methodName].bind(this.messageHandler);
        await catchAsyncEvent(msgHandler)(socket, baseReq);
    }
}
