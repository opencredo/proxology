package com.opencredo.proxology.builders;

import com.opencredo.proxology.proxies.Proxies;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class BuilderTest {

    public interface Address {
        static Address with(List<String> addressLines, String postcode) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("addressLines", addressLines);
            properties.put("postcode", postcode);
            return Proxies.propertyMapping(Address.class, properties);
        }

        List<String> getAddressLines();
        String getPostcode();
    }

    public interface Person {
        static Person with(long id, String name, int age, Address address) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("id", id);
            properties.put("name", name);
            properties.put("age", age);
            properties.put("address", address);
            return Proxies.propertyMapping(Person.class, properties);
        }

        String getName();
        int getAge();
        Address getAddress();
    }

    public interface TestPerson extends Template<Person, TestPerson.Builder> {
        static Builder builder() {
            return Template.builderFor(TestPerson.class);
        }

        interface Builder extends Supplier<Person> {
            Builder withId(long id);
            Builder withName(String name);
            Builder withAge(int age);
            Builder withAddress(String...address);
        }

        long getId(long defaultValue);
        String getName(String defaultValue);
        int getAge(int defaultValue);
        List<String> getAddress(String...defaultValue);

        @Override
        default Person get() {
            List<String> rawAddress = getAddress("23 Acacia Avenue", "Sunderland", "VB6 5UX");

            return Person.with(
                getId(0),
                getName("Mr Default"),
                getAge(42),
                Address.with(
                    rawAddress.subList(0, rawAddress.size() - 1),
                    rawAddress.get(rawAddress.size() - 1)));
        }
    }

    @Test
    public void buildInstanceFromTemplateUsingDefaultValues() {
        Person person = TestPerson.builder().get();
        assertThat(person.getName(), equalTo("Mr Default"));
        assertThat(person.getAddress().getAddressLines(), contains("23 Acacia Avenue", "Sunderland"));
    }

    @Test
    public void buildInstanceFromTemplateOverridingDefaultValues() {
        Person person = TestPerson.builder()
                .withName("Mr Special")
                .withAddress("16 Pellucid Drive", "Halesowen", "RA8 81T")
                .get();
        assertThat(person.getName(), equalTo("Mr Special"));
        assertThat(person.getAddress().getAddressLines(), contains("16 Pellucid Drive", "Halesowen"));
    }
}
