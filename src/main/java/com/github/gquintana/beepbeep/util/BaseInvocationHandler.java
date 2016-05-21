package com.github.gquintana.beepbeep.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Generate delegating proxy implementation
 */
public abstract class BaseInvocationHandler<T> implements InvocationHandler {
    protected final T delegate;

    public BaseInvocationHandler(T delegate) {
        this.delegate = delegate;
    }

    protected Object delegate(Method method, Object ... args) throws Throwable {
        return method.invoke(delegate, args);
    }

    public <T> T newProxy(Class<T> proxyInterface) {
        return proxyInterface.cast(Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class[]{proxyInterface},
            this));
    }

}
