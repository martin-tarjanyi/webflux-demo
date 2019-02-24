package com.example.webfluxdemo.logcontext;

import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import static java.util.stream.Collectors.toMap;

public class MdcContextLifter<T> implements CoreSubscriber<T>
{
    private CoreSubscriber<T> coreSubscriber;

    public MdcContextLifter(CoreSubscriber<T> coreSubscriber) {
        this.coreSubscriber = coreSubscriber;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        coreSubscriber.onSubscribe(subscription);
    }

    @Override
    public void onNext(T obj) {
        copyToMdc(coreSubscriber.currentContext());
        coreSubscriber.onNext(obj);
    }

    @Override
    public void onError(Throwable t) {
        copyToMdc(coreSubscriber.currentContext());
        coreSubscriber.onError(t);
    }

    @Override
    public void onComplete() {
        coreSubscriber.onComplete();
    }

    @Override
    public Context currentContext() {
        return coreSubscriber.currentContext();
    }

    private void copyToMdc(Context context) {

        if (context.isEmpty())
        {
            MDC.clear();
        } else
        {
            var map = context.stream().collect(toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));

            MDC.setContextMap(map);
        }
    }

}
