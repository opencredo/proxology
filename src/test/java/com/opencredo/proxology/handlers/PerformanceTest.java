package com.opencredo.proxology.handlers;

import com.opencredo.proxology.beans.BeanProxy;
import com.opencredo.proxology.proxies.Proxies;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

//@Ignore
public class PerformanceTest {

    private static final Map<String, Object> map1 = initialiseMap();
    private static final Map<String, Object> map2 = initialiseMap();
    private static final Person wrappingProxy1 = Person.wrapping(initialiseMap());
    private static final Person wrappingProxy2 = Person.wrapping(initialiseMap());
    private static final Person beanProxy1 = Person.create("Arthur Putey", 42);
    private static final Person beanProxy2 = Person.create("Arthur Putey", 42);

    @Rule
    public final ContiPerfRule rule = new ContiPerfRule();

    interface Person {
        static Person wrapping(Map<String, Object> values) {
            return Proxies.propertyMapping(Person.class, values);
        }

        static Person create(String name, int age) {
            Person person = BeanProxy.proxying(Person.class);
            person.setName(name);
            person.setAge(age);
            return person;
        }

        String getName();
        void setName(String name);

        int getAge();
        void setAge(int age);
    }

    @Test
    @PerfTest(invocations=1000, warmUp=200)
    public void readFromMap() {
        for (int i=0; i < 100000; i++) {
            String name = (String) map1.get("name");
            int age = (int) (Integer) map1.get("age");
            assertEquals(name, "Arthur Putey");
            assertEquals(age, 42);
        }
    }

    @Test
    @PerfTest(invocations=1000, warmUp=200)
    public void readFromWrappingProxy() {
        for (int i=0; i < 100000; i++) {
            String name = wrappingProxy1.getName();
            int age = wrappingProxy1.getAge();
            assertEquals(name, "Arthur Putey");
            assertEquals(age, 42);
        }
    }

    @Test
    @PerfTest(invocations=1000, warmUp=200)
    public void mapEquality() {
        for (int i=0; i < 10000; i++) {
            assertEquals(map1, map2);
        }
    }

    @Test
    @PerfTest(invocations=1000, warmUp=200)
    public void beanProxyEquality() {
        for (int i=0; i < 10000; i++) {
            assertEquals(beanProxy1, beanProxy2);
        }
    }

    @Test
    @PerfTest(invocations=1000, warmUp=200)
    public void wrappingProxyEquality() {
        for (int i=0; i < 10000; i++) {
            assertEquals(wrappingProxy1, wrappingProxy2);
        }
    }

    @Test
    @PerfTest(invocations=1000, warmUp=200)
    public void mapInitialisation() {
        for (int i=0; i < 10000; i++) {
            assertNotNull(initialiseMap());
        }
    }

    @Test
    @PerfTest(invocations=1000, warmUp=200)
    public void wrappingProxyInitialisation() {
        for (int i=0; i < 10000; i++) {
            assertNotNull(Person.wrapping(initialiseMap()));
        }
    }

    @Test
    @PerfTest(invocations=1000, warmUp=200)
    public void beanProxyInitialisation() {
        for (int i=0; i < 10000; i++) {
            assertNotNull(Person.create("Arthur Putey", 42));
        }
    }

    private static Map<String, Object> initialiseMap() {
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("name", "Arthur Putey");
        propertyMap.put("age", 42);
        return propertyMap;
    }

}
