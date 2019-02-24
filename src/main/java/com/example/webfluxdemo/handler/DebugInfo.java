package com.example.webfluxdemo.handler;

import com.example.resilience.connector.model.Result;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@ToString
@EqualsAndHashCode
@JsonInclude(NON_NULL)
public class DebugInfo
{
    private final String requestId;
    private final Collection<Result<?>> results;

    public DebugInfo(String requestId, Collection<Result<?>> results)
    {
        this.requestId = requestId;
        this.results = results;
    }

    public DebugInfo(String requestId)
    {
        this.requestId = requestId;
        this.results = null;
    }

    public String getRequestId()
    {
        return requestId;
    }

    public Collection<Result<?>> getResults()
    {
        return results;
    }
}
