package org.bukkit.plugin.messaging;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.bukkit.entity.Player;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class TestPlayer implements InvocationHandler {
    private static interface MethodHandler {
        Object handle(TestPlayer server, Object[] args);
    }

    private static final Constructor<? extends Player> constructor;
    private static final Object2ObjectOpenHashMap<Method, MethodHandler> methods = new Object2ObjectOpenHashMap<Method, MethodHandler>();
    static {
        try {
            /*
             * methods.put(Player.class.getMethod("methodName"),
             * new MethodHandler() {
             * public Object handle(TestPlayer server, Object[] args) {
             * }
             * });
             */
            constructor = Proxy.getProxyClass(Player.class.getClassLoader(), Player.class).asSubclass(Player.class)
                    .getConstructor(InvocationHandler.class);
        } catch (Throwable t) {
            throw new Error(t);
        }
    }

    private TestPlayer() {
    };

    public static Player getInstance() {
        try {
            return constructor.newInstance(new TestPlayer());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) {
        MethodHandler handler = methods.get(method);
        if (handler != null) {
            return handler.handle(this, args);
        }
        throw new UnsupportedOperationException(String.valueOf(method));
    }
}
