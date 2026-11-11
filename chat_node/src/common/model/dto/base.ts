

// export interface BaseReq {
//     event : string;
//     data? : any;
// }

import { z } from 'zod';
import { SubEventType } from '../enums/redisSubEvents';

export const BaseReq = z.object({
    // 빈 문자열 방지
    event: z.string().min(1, "이벤트(event) 값은 필수입니다."),
    
    data: z.any().optional(), 
});

export type BaseReq = z.infer<typeof BaseReq>;

export interface BaseRes {
    event : SubEventType;
    data? : any;
}

export interface BaseError {
    code:string;
    message: string;
}