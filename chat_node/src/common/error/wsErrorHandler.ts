import { WebSocket } from "ws";
import { WsError } from "./wsError";
import { wsErrorConverter } from "./wsErrorConvertor";
import logger from "@common/util/logger";

export interface BaseError {
    code: string;
    message: string;
}

export async function sendError(ws: WebSocket, error: WsError) {
    ws.send(
        JSON.stringify({
            event: "ERROR",
            data: {
                code: error.code,
                message: error.message,
            } as BaseError,
        }),
    );
}

export const catchAsyncEvent = (fn: Function) => {
    return async (socket: WebSocket, ...args: any[]) => {
        try {
            await fn(socket, ...args);
        } catch (error) {
            logger.error(error);
            const wsError: WsError = wsErrorConverter(error);
            await sendError(socket, wsError);
        }
    };
};
