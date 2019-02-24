package com.example.webfluxdemo.model;

import com.example.webfluxdemo.handler.DebugInfo;
import com.example.webfluxdemo.handler.IDebugAware;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Response implements IDebugAware
{
    private final DebugInfo debugInfo;
    private final Object result;

    public Response(Object result)
    {
        this.debugInfo = null;
        this.result = result;
    }

    private Response(DebugInfo debugInfo, Object result)
    {
        this.debugInfo = debugInfo;
        this.result = result;
    }

    @Override
    public IDebugAware addDebugInfo(DebugInfo debugInfo)
    {
        return new Response(debugInfo, result);
    }

    public DebugInfo getDebugInfo()
    {
        return debugInfo;
    }

    public Object getResult()
    {
        return result;
    }
}
