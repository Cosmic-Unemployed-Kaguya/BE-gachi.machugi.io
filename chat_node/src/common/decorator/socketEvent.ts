
import { SocketEventType } from "@common/model/enums/socketEvents";
import { EventMetadata } from "@dto/event";
import 'reflect-metadata';



export function SocketEvent( event : SocketEventType ) : MethodDecorator {
    return function (
        target: any,
        property: string | symbol,
        descriptor: PropertyDescriptor
    ){
        // 1. 먼저 등록 된 이벤트 메서드 있는지?
        const socketEv: EventMetadata[] = Reflect.getMetadata('ws:socket', target.constructor) || [];
        
        // 2. 메타데이터에 이벤트명, 함수 이름 등록
        socketEv.push({ event : event , methodName : property })
        Reflect.defineMetadata('ws:socket', socketEv, target.constructor )

    }


}