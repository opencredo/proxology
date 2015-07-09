package com.opencredo.proxology.matchers;

import com.opencredo.proxology.proxies.Proxies;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

public class MagicMatcherTest {

    public interface Person {
        static Person create(String name, int age) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", name);
            properties.put("age", age);
            return Proxies.propertyMapping(Person.class, properties);
        }

        String getName();
        int getAge();
    }

    public interface PersonMatcher extends Matcher<Person> {
        static PersonMatcher aPerson() {
            return MagicMatcher.proxying(PersonMatcher.class);
        }

        PersonMatcher withName(String expected);
        PersonMatcher withName(Matcher<String> matcher);
        PersonMatcher withAge(int expected);
        PersonMatcher withAge(Matcher<Integer> ageMatcher);
    }

    @Test public void
    matchesLiterals() {
        assertThat(Person.create("Arthur Putey", 42), PersonMatcher.aPerson()
            .withName("Arthur Putey")
            .withAge(42));
    }

    @Test public void
    matchesWithMatchers() {
        assertThat(Person.create("Arthur Putey", 42), PersonMatcher.aPerson()
            .withName(Matchers.containsString("Putey"))
            .withAge(Matchers.greaterThan(40)));
    }
}
