import "reflect-metadata";
import { EventMetadata } from "../model/event";
import { ServerEventType } from "../model/serverEvents";

export function ServerEvent(event: ServerEventType): MethodDecorator {
    return function (
        target: any,
        property: string | symbol,
        descriptor: PropertyDescriptor,
    ) {
        // 1. 먼저 등록 된 이벤트 메서드 있는지?
        const serverEv: EventMetadata[] =
            Reflect.getMetadata("ws:server", target.constructor) || [];

        // 2. 메타데이터에 이벤트명, 함수 이름 등록
        serverEv.push({ event: event, methodName: property });
        Reflect.defineMetadata("ws:server", serverEv, target.constructor);
    };
}
