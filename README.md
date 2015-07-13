# proxology

A Java 8 library for working with dynamic proxies. Includes a basic ```MagicMatcher``` implementation.

```java
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
```

For more details see [New Tricks With Dynamic Proxies In Java 8](https://www.opencredo.com/2015/07/13/dynamic-proxies-java/).
