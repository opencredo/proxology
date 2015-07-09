package com.opencredo.proxology.matchers;

import com.opencredo.proxology.handlers.InvocationHandlers;
import com.opencredo.proxology.handlers.MethodCallHandler;
import com.opencredo.proxology.handlers.MethodInterpreter;
import com.opencredo.proxology.handlers.early.UnboundDispatchingMethodInterpreter;
import com.opencredo.proxology.proxies.Proxies;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MagicMatcher<S> extends TypeSafeDiagnosingMatcher<S> implements MethodInterpreter {

    public static <S, T extends Matcher<S>> T proxying(Class<T> proxyClass) {
        MagicMatcher<S> magicMatcher = new MagicMatcher<>();
        return Proxies.simpleProxy(
                proxyClass,
                InvocationHandlers.handlingDefaultMethods(
                        UnboundDispatchingMethodInterpreter.forClasses(Object.class, Matcher.class).bind(magicMatcher))
                        .orElse(magicMatcher)
                );
    }

    private final Map<String, Matcher<?>> propertyMatchers = new HashMap<>();

    @Override
    protected boolean matchesSafely(S item, Description description) {
        BeanInfo info = Unsafely.invoke(() -> Introspector.getBeanInfo(item.getClass()));
        Map<String, Method> propertyMap = Stream.of(info.getPropertyDescriptors()).collect(Collectors.toMap(PropertyDescriptor::getName, PropertyDescriptor::getReadMethod));

        boolean matched = true;
        for (Map.Entry<String, Matcher<?>> propertyMatcher : propertyMatchers.entrySet()) {
            Method getter = propertyMap.get(propertyMatcher.getKey());
            if (getter == null) {
                matched = false;
                description.appendText("\n").appendText(propertyMatcher.getKey()).appendText(": not found in ").appendValue(item.getClass());
                continue;
            }

            Object propertyValue = Unsafely.invoke(() -> getter.invoke(item));
            if (!propertyMatcher.getValue().matches(propertyValue)) {
                matched = false;
                propertyMatcher.getValue().describeMismatch(
                        propertyValue,
                        description.appendText("\n").appendText(propertyMatcher.getKey()).appendText(": "));
            }
        }

        return matched;
    }

    private interface Unsafely<T> {
        static <T> T invoke(Unsafely<T> f) {
            try {
                return f.run();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        T run() throws Throwable;
    }

    @Override
    public void describeTo(Description description) {
        propertyMatchers.entrySet().forEach(entry -> {
            description.appendText("\n").appendText(entry.getKey()).appendText(": ").appendDescriptionOf(entry.getValue());
        });
    }

    @Override
    public MethodCallHandler interpret(Method method) {
        return (proxy, args) -> {
            propertyMatchers.put(getPropertyName(method.getName()), getMatcher(args[0]));
            return proxy;
        };
    }

    private String getPropertyName(String methodName) {
        return methodName.substring(4, 5).toLowerCase() + methodName.substring(5);
    }

    private Matcher<?> getMatcher(Object arg) {
        if (arg instanceof Matcher) {
            return (Matcher<?>) arg;
        }
        return Matchers.equalTo(arg);
    }
}
