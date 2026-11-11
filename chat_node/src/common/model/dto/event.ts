


export interface EventMetadata{
    event: string;
    methodName: string | symbol;
}

export type EventMap = Map<string, string|symbol>;