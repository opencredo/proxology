package com.opencredo.proxology.beans;

import com.opencredo.proxology.proxies.Proxies;
import com.opencredo.proxology.utils.EqualisableByState;

public final class BeanProxy {

    private BeanProxy() {
    }

    static <T> T proxying(Class<T> proxyClass) {
        BeanProxySchema schema = BeanProxySchema.forClass(proxyClass);
        BeanProxyStorage storage = schema.createStorage();

        return Proxies.simpleProxy(proxyClass, storage.getMethodInterpreter(), EqualisableByState.class);
    }

}
