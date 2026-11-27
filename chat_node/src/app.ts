import express from "express";
import "reflect-metadata";
import { ChatWsController } from "./domain/chat/controller/chatWsController";
import { ServerEventHandler } from "./domain/chat/middlewares/serverEventHandler";
import { SocketEventHandler } from "./domain/chat/middlewares/socketEventHandler";

async function startServer() {
    const app = express();

    const chatEventHandlers = {
        server: ServerEventHandler,
        socket: SocketEventHandler,
        message: ChatWsController,
    };

    // loader 호출
    await require("./common/loader").default({ expressApp: app }, chatEventHandlers);
}

startServer();
