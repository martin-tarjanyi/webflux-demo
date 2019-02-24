package com.example.webfluxdemo.logcontext;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

@Component
public class LogWebFilter implements WebFilter
{
    public static final String REQUEST_ID = "requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain)
    {
        return chain.filter(exchange)
                    .subscriberContext(Context.of(
                            REQUEST_ID, UUID.randomUUID().toString()
                    ));
    }
}

