package de.cybine.quarkus.util.converter;

import io.quarkus.test.*;
import jakarta.enterprise.inject.*;
import jakarta.inject.*;
import lombok.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;

import java.util.*;
import java.util.stream.*;

@DisplayName("Converter")
class ConverterTest
{
    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().withEmptyApplication();

    @Inject
    Instance<ConverterRegistry> registryInstance;

    @BeforeEach
    void setup( )
    {
        ConverterRegistry registry = this.registryInstance.get();
        registry.addConverter(
                new GenericConverter<>(String.class, Integer.class, (input, helper) -> Integer.parseInt(input)));

        registry.addConverter(new GenericConverter<>(Address.class, String.class,
                (input, helper) -> String.format("Address[street=%s, city=%s, country=%s]", input.getStreet(),
                        input.getCity(), input.getCountry())));

        registry.addConverter(new GenericConverter<>(Contact.class, String.class,
                (input, helper) -> String.format("Contact[email=%s]", input.getEmail())));

        registry.addConverter(new GenericConverter<>(Person.class, String.class,
                (input, helper) -> String.format("Person[firstname=%s, lastname=%s, address=%s, contact=%s]",
                        input.getFirstname(), input.getLastname(),
                        helper.toItem(Address.class, String.class).apply(input::getAddress),
                        helper.toItem(Contact.class, String.class).apply(input::getContact))));
    }

    @Test
    @DisplayName("can convert a single item with a simple type")
    void testSingleItemConversionWithSimpleType( )
    {
        ConverterRegistry registry = this.registryInstance.get();

        ConversionProcessor<String, Integer> converter = Assertions.assertDoesNotThrow(
                ( ) -> registry.getProcessor(String.class, Integer.class));

        ConversionResult<Integer> result = Assertions.assertDoesNotThrow(( ) -> converter.toItem("3"));

        Assertions.assertEquals(3, result.result());
    }

    @Test
    @DisplayName("can convert a list of items with a simple type")
    void testItemListConversionWithSimpleType( )
    {
        ConverterRegistry registry = this.registryInstance.get();

        ConversionProcessor<String, Integer> converter = Assertions.assertDoesNotThrow(
                ( ) -> registry.getProcessor(String.class, Integer.class));

        ConversionResult<List<Integer>> result = Assertions.assertDoesNotThrow(
                ( ) -> converter.toList(List.of("3", "4", "5")));

        Assertions.assertEquals(List.of(3, 4, 5), result.result());
    }

    @Test
    @DisplayName("can convert a set of items with a simple type")
    void testItemSetConversionWithSimpleType( )
    {
        ConverterRegistry registry = this.registryInstance.get();

        ConversionProcessor<String, Integer> converter = Assertions.assertDoesNotThrow(
                ( ) -> registry.getProcessor(String.class, Integer.class));

        ConversionResult<Set<Integer>> result = Assertions.assertDoesNotThrow(
                ( ) -> converter.toSet(List.of("3", "4", "5")));

        Assertions.assertEquals(Set.of(3, 4, 5), result.result());
    }

    @Test
    @DisplayName("can convert a custom collection of items with a simple type")
    void testItemCollectionConversionWithSimpleType( )
    {
        ConverterRegistry registry = this.registryInstance.get();

        ConversionProcessor<String, Integer> converter = Assertions.assertDoesNotThrow(
                ( ) -> registry.getProcessor(String.class, Integer.class));

        ConversionResult<List<Integer>> result = Assertions.assertDoesNotThrow(
                ( ) -> converter.toCollection(List.of("3", "4", "5"), Collections.emptyList(), Collectors.toList()));

        Assertions.assertEquals(List.of(3, 4, 5), result.result());
    }

    @Test
    @DisplayName("can convert a complex type")
    void testComplexTypeConversion( )
    {
        ConverterRegistry registry = this.registryInstance.get();

        ConversionProcessor<Person, String> converter = Assertions.assertDoesNotThrow(
                ( ) -> registry.getProcessor(Person.class, String.class));

        Address address = Address.builder().street("Sand Creek Rd").city("Laramie").country("USA").build();
        Contact contact = Contact.builder().email("john.doe@example.com").build();
        Person person = Person.builder().firstname("John").lastname("Doe").address(address).contact(contact).build();
        ConversionResult<String> result = Assertions.assertDoesNotThrow(( ) -> converter.toItem(person));

        String addressString = String.format("Address[street=%s, city=%s, country=%s]", address.getStreet(),
                address.getCity(), address.getCountry());
        String contactString = String.format("Contact[email=%s]", contact.getEmail());
        String personString = String.format("Person[firstname=%s, lastname=%s, address=%s, contact=%s]",
                person.getFirstname(), person.getLastname(), addressString, contactString);

        Assertions.assertEquals(personString, result.result());
    }

    @Test
    @DisplayName("can handle general max-depth constraints")
    void testComplexTypeConversionWithGeneralMaxDepthConstraint( )
    {
        ConverterRegistry registry = this.registryInstance.get();

        ConverterConstraint constraint = ConverterConstraint.builder().maxDepth(1).build();
        ConverterTree tree = ConverterTree.builder().constraint(constraint).build();
        ConversionProcessor<Person, String> converter = Assertions.assertDoesNotThrow(
                ( ) -> registry.getProcessor(Person.class, String.class, tree));

        Address address = Address.builder().street("Sand Creek Rd").city("Laramie").country("USA").build();
        Contact contact = Contact.builder().email("john.doe@example.com").build();
        Person person = Person.builder().firstname("John").lastname("Doe").address(address).contact(contact).build();
        ConversionResult<String> result = Assertions.assertDoesNotThrow(( ) -> converter.toItem(person));

        String personString = String.format("Person[firstname=%s, lastname=%s, address=%s, contact=%s]",
                person.getFirstname(), person.getLastname(), null, null);

        Assertions.assertEquals(personString, result.result());
    }

    @Test
    @DisplayName("can handle type-specific max-depth constraints")
    void testComplexTypeConversionWithTypeSpecificMaxDepthConstraint( )
    {
        ConverterRegistry registry = this.registryInstance.get();

        ConverterConstraint constraint = ConverterConstraint.builder().maxDepth(0).build();
        ConverterTree tree = ConverterTree.builder().typeConstraint(Contact.class, constraint).build();
        ConversionProcessor<Person, String> converter = Assertions.assertDoesNotThrow(
                ( ) -> registry.getProcessor(Person.class, String.class, tree));

        Address address = Address.builder().street("Sand Creek Rd").city("Laramie").country("USA").build();
        Contact contact = Contact.builder().email("john.doe@example.com").build();
        Person person = Person.builder().firstname("John").lastname("Doe").address(address).contact(contact).build();
        ConversionResult<String> result = Assertions.assertDoesNotThrow(( ) -> converter.toItem(person));

        String addressString = String.format("Address[street=%s, city=%s, country=%s]", address.getStreet(),
                address.getCity(), address.getCountry());
        String personString = String.format("Person[firstname=%s, lastname=%s, address=%s, contact=%s]",
                person.getFirstname(), person.getLastname(), addressString, null);

        Assertions.assertEquals(personString, result.result());
    }

    @Data
    @Builder(builderClassName = "Generator")
    public static class Address
    {
        private final String street;
        private final String city;
        private final String country;
    }

    @Data
    @Builder(builderClassName = "Generator")
    public static class Contact
    {
        private final String email;
    }

    @Data
    @Builder(builderClassName = "Generator")
    public static class Person
    {
        private final String firstname;
        private final String lastname;

        private final Address address;
        private final Contact contact;
    }
}
