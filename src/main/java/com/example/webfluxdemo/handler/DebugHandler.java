package com.example.webfluxdemo.handler;

import com.example.resilience.connector.logging.LogContext;
import com.example.webfluxdemo.logcontext.LogWebFilter;
import org.springframework.core.MethodParameter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import static com.example.resilience.connector.util.MonoOperators.mapWithContext;

@Component
public class DebugHandler extends ResponseBodyResultHandler
{

    protected DebugHandler(
            ServerCodecConfigurer codecConfigurer,
            RequestedContentTypeResolver contentTypeResolver)
    {
        super(codecConfigurer.getWriters(), contentTypeResolver);
        setOrder(1);
    }

    @Override
    public boolean supports(HandlerResult result)
    {
        boolean isMono = result.getReturnType().resolve() == Mono.class;
        boolean isDebugAwareResponse = IDebugAware.class.isAssignableFrom(result.getReturnType().resolveGeneric(0));

        return isMono && isDebugAwareResponse;
    }

    @Override
    protected Mono<Void> writeBody(Object body, MethodParameter bodyParameter, ServerWebExchange exchange)
    {
        MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
        boolean debugEnabled = "true".equals(queryParams.getFirst("debug"));

        Mono<IDebugAware> populatedBody = ((Mono<IDebugAware>) body)
                .<IDebugAware>transform(mapWithContext((debugAware, context) -> populate(debugAware, context, debugEnabled)))
                .subscriberContext(
                        Context.of(
                                LogContext.class, LogContext.create())
                );

        return super.writeBody(populatedBody, bodyParameter, exchange);
    }

    private IDebugAware populate(IDebugAware debugAware, Context context, boolean debugEnabled)
    {
        String requestId = context.<String>getOrEmpty(LogWebFilter.REQUEST_ID).orElse(null);
        LogContext logContext = context.<LogContext>getOrEmpty(LogContext.class).orElseGet(LogContext::create);

        DebugInfo debugInfo = debugEnabled ?
                new DebugInfo(requestId, logContext.getResults()) :
                new DebugInfo(requestId);

        return debugAware.addDebugInfo(debugInfo);
    }
}
