package com.opencredo.proxology.beans;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BeanProxyTest {

    public interface Person {

        static Person create() {
            return create("", 0);
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
    public void
    createProxy() {
        Person person = Person.create("Arthur Putey", 42);

        assertThat(person.getName(), equalTo("Arthur Putey"));
        assertThat(person.getAge(), equalTo(42));
    }

    @Test public void
    providesMeaningfulToString() {
        Person person = Person.create("Arthur Putey", 42);

        assertThat(person.toString(), containsString("name: Arthur Putey"));
        assertThat(person.toString(), containsString("age: 42"));
    }

    @Test public void
    testForEquality() {
        Person person = Person.create("Arthur Putey", 42);
        Person samePerson = Person.create("Arthur Putey", 42);
        Person differentPerson = Person.create("Martha Putey", 42);

        assertThat(person, equalTo(samePerson));
        assertThat(person, not(equalTo(differentPerson)));
    }
}
