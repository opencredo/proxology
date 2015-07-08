package com.opencredo.proxology;

import com.opencredo.proxology.proxies.Proxies;

import java.io.IOException;
import java.util.HashMap;

public class Application {

    interface Wrapper {
        String getValue();
        void setValue(String value);
    }

    public static void main(String[] args) throws IOException {
        System.in.read();
        Wrapper wrapper = Proxies.beanWrapping(Wrapper.class, new HashMap<>());
        wrapper.setValue("0");
        for (int i = 0; i < 10000000; i++) {
            int value = Integer.valueOf(wrapper.getValue());
            wrapper.setValue(Integer.toString(value + 1));
        }
        System.out.println(wrapper);
    }
}
