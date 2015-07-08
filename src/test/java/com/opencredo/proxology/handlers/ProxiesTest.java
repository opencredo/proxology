package com.opencredo.proxology.handlers;

import com.opencredo.proxology.proxies.Proxies;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ProxiesTest {

    public interface Entity {
        long getId();
        void setId(long id);
    }

    public interface Person extends Entity {
        String getName();
        void setName(String name);
        int getAge();
        void setAge(int age);

        default String display() {
            return String.format("%s (%s)", getName(), getAge());
        }
    }

    public static final class PersonImpl implements Person {
        private long id;
        private String name;
        private int age;

        public PersonImpl() {
        }

        public PersonImpl(long id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        @Override
        public long getId() {
            return id;
        }

        @Override
        public void setId(long id) {
            this.id = id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getAge() {
            return age;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Person)) {
                return false;
            }
            Person otherPerson = (Person) o;
            return Objects.equals(id, otherPerson.getId())
                    && Objects.equals(name, otherPerson.getName())
                    && Objects.equals(age, otherPerson.getAge());
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, age);
        }

        @Override
        public String toString() {
            return "PersonImpl{" +
                    "id='" + id + '\'' +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }

    @Test public void
    interceptsCalls() {
        Person instance = new PersonImpl();

        List<String> callDetails = new ArrayList<>();
        MethodCallInterceptor interceptor = (proxy, method, args, handler) -> {
            Object result = handler.invoke(proxy, args);
            callDetails.add(String.format("%s: %s -> %s", method.getName(), Arrays.toString(args), result));
            return result;
        };

        Person proxy = Proxies.intercepting(instance, Person.class, interceptor);
        proxy.setId(13);
        proxy.setName("Arthur Putey");
        proxy.setAge(42);

        assertThat(proxy.display(), equalTo("Arthur Putey (42)"));
        System.out.println(callDetails);
        assertThat(callDetails, contains(
                "setId: [13] -> null",
                "setName: [Arthur Putey] -> null",
                "setAge: [42] -> null",
                "getName: null -> Arthur Putey",
                "getAge: null -> 42",
                "display: null -> Arthur Putey (42)"));
    }

    @Test public void
    handlesObjectMethods() {
        Person instance1 = new PersonImpl(1, "Arthur Putey", 42);
        Person instance2 = new PersonImpl(1, "Arthur Putey", 42);
        List<String> callDetails = new ArrayList<>();

        MethodCallInterceptor interceptor = (proxy, method, args, handler) -> {
            Object result = handler.invoke(proxy, args);
            callDetails.add(String.format("%s: %s -> %s", method.getName(), Arrays.toString(args), result));
            return result;
        };

        Person proxy = Proxies.intercepting(instance1, Person.class, interceptor);

        assertThat(proxy, equalTo(instance2));
        assertThat(proxy.hashCode(), equalTo(instance2.hashCode()));
        assertThat(proxy.toString(), equalTo(instance2.toString()));

        assertThat(callDetails, hasItems(containsString("equals"), containsString("hashCode"), containsString("toString")));
    }

    @Test public void
    equality() {
        Person instance1 = new PersonImpl(1, "Arthur Putey", 42);
        Person instance2 = new PersonImpl(1, "Arthur Putey", 42);
        List<String> callDetails = new ArrayList<>();

        MethodCallInterceptor interceptor = (proxy, method, args, handler) -> {
            Object result = handler.invoke(proxy, args);
            callDetails.add(String.format("%s: %s -> %s", method.getName(), Arrays.toString(args), result));
            return result;
        };

        Person proxy1 = Proxies.intercepting(instance1, Person.class, interceptor);

        assertThat(proxy1, equalTo(instance1));
        assertThat(instance1, equalTo(proxy1));
        assertThat(proxy1, equalTo(instance2));
        assertThat(instance2, equalTo(proxy1));
    }

    @Test public void
    beanMappingProxies() {
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("id", 1L);
        propertyMap.put("name", "Arthur Putey");
        propertyMap.put("age", 42);

        Person proxy1 = Proxies.beanWrapping(Person.class, propertyMap);
        Person proxy2 = Proxies.beanWrapping(Person.class, propertyMap);

        assertThat(proxy1, equalTo(proxy2));
        assertThat(proxy1.getId(), equalTo(1L));
        assertThat(proxy1.getName(), equalTo("Arthur Putey"));
        assertThat(proxy1.getAge(), equalTo(42));

        proxy1.setName("Alice Cowley");
        assertThat(proxy2.getName(), equalTo("Alice Cowley"));
    }


}
