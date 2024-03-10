package de.cybine.quarkus.util.converter;

import de.cybine.quarkus.data.mail.address.*;
import de.cybine.quarkus.data.mail.domain.*;
import de.cybine.quarkus.data.mail.forwarding.*;
import de.cybine.quarkus.data.mail.mailbox.*;
import de.cybine.quarkus.data.mail.tls.*;
import de.cybine.quarkus.data.mail.user.*;
import de.cybine.quarkus.exception.converter.*;
import io.quarkus.test.*;
import jakarta.enterprise.inject.*;
import jakarta.inject.*;
import org.jboss.shrinkwrap.api.*;
import org.jboss.shrinkwrap.api.spec.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;

import java.math.*;
import java.util.*;
import java.util.stream.*;

@DisplayName("Converter Module")
class ConverterTest
{
    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().setArchiveProducer(
            ( ) -> ShrinkWrap.create(JavaArchive.class)
                             .addClasses(MailAddress.class, MailAddressAction.class, MailAddressEntity.class,
                                     MailAddressId.class, MailAddressMapper.class)
                             .addClasses(MailDomain.class, MailDomainAction.class, MailDomainCreationConverter.class,
                                     MailDomainCreationParams.class, MailDomainEntity.class, MailDomainId.class,
                                     MailDomainMapper.class)
                             .addClasses(MailForwarding.class, MailForwardingEntity.class, MailForwardingMapper.class)
                             .addClasses(Mailbox.class, MailboxEntity.class, MailboxId.class, MailboxMapper.class)
                             .addClasses(MailTLSPolicy.class, MailTLSPolicyEntity.class, MailTLSPolicyId.class,
                                     MailTLSPolicyMapper.class, MailTLSPolicyType.class)
                             .addClasses(MailUser.class, MailUserEntity.class, MailUserId.class, MailUserMapper.class));

    @Inject
    Instance<ConverterRegistry> registryInstance;

    @BeforeEach
    void setup( )
    {
        ConverterRegistry registry = this.registryInstance.get();

        registry.addConverter(new MailDomainCreationConverter());

        registry.addEntityMapper(new MailAddressMapper());
        registry.addEntityMapper(new MailDomainMapper());
        registry.addEntityMapper(new MailForwardingMapper());
        registry.addEntityMapper(new MailboxMapper());
        registry.addEntityMapper(new MailTLSPolicyMapper());
        registry.addEntityMapper(new MailUserMapper());

        registry.addConverter(
                new GenericConverter<>(String.class, Integer.class, (input, helper) -> Integer.parseInt(input)));
    }

    @Test
    @DisplayName("can resolve registered converter")
    void testConverterRetrieval( )
    {
        ConverterRegistry registry = this.registryInstance.get();

        ConversionProcessor<MailAddress, MailAddressEntity> converter = Assertions.assertDoesNotThrow(
                ( ) -> registry.getProcessor(MailAddress.class, MailAddressEntity.class));

        Assertions.assertDoesNotThrow(( ) -> converter.toItem(MailAddress.builder().name("test").build()));
    }

    @Test
    @DisplayName("can register converters")
    void testConverterRegistration( )
    {
        ConverterRegistry registry = this.registryInstance.get();

        ConversionProcessor<Integer, String> converter = Assertions.assertDoesNotThrow(
                ( ) -> registry.getProcessor(Integer.class, String.class));

        Assertions.assertThrows(UnknownConverterException.class, ( ) -> converter.toItem(1));

        registry.addConverter(new GenericConverter<>(Integer.class, String.class, (input, helper) -> input.toString()));

        Assertions.assertDoesNotThrow(( ) -> converter.toItem(1));
    }

    @Test
    @DisplayName("can register entity mappers")
    void testEntityMapperRegistration( )
    {
        ConverterRegistry registry = this.registryInstance.get();

        ConversionProcessor<UUID, String> uuidConverter = Assertions.assertDoesNotThrow(
                ( ) -> registry.getProcessor(UUID.class, String.class));

        ConversionProcessor<String, UUID> stringConverter = Assertions.assertDoesNotThrow(
                ( ) -> registry.getProcessor(String.class, UUID.class));

        Assertions.assertThrows(UnknownConverterException.class, ( ) ->
        {
            uuidConverter.toItem(UUID.randomUUID());
            stringConverter.toItem(UUID.randomUUID().toString());
        });

        GenericConverter<UUID, String> uuidToString = new GenericConverter<>(UUID.class, String.class,
                (input, helper) -> input.toString());
        GenericConverter<String, UUID> stringToUuid = new GenericConverter<>(String.class, UUID.class,
                (input, helper) -> UUID.fromString(input));

        registry.addEntityMapper(new GenericEntityMapper<>(UUID.class, String.class, uuidToString, stringToUuid));

        Assertions.assertDoesNotThrow(( ) ->
        {
            uuidConverter.toItem(UUID.randomUUID());
            stringConverter.toItem(UUID.randomUUID().toString());
        });
    }

    @Test
    @DisplayName("can detect missing root converters")
    void testMissingRootConverterCheck( )
    {
        ConverterRegistry registry = this.registryInstance.get();

        Assertions.assertTrue(registry.getProcessor(String.class, String.class).hasUnsatisfiedDependencies());
        Assertions.assertFalse(registry.getProcessor(String.class, Integer.class).hasUnsatisfiedDependencies());
    }

    @Test
    @DisplayName("can detect missing relation converters")
    void testMissingRelationConverterCheck( )
    {
        ConverterRegistry registry = this.registryInstance.get();

        registry.addConverter(
                new GenericConverter<>(BigInteger.class, Integer.class, (input, helper) -> input.intValue(),
                        metadata -> metadata.withRelation(BigInteger.class, Long.class)));

        Assertions.assertTrue(registry.getProcessor(BigInteger.class, Integer.class).hasUnsatisfiedDependencies());
        Assertions.assertFalse(
                registry.getProcessor(MailDomain.class, MailDomainEntity.class).hasUnsatisfiedDependencies());
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
    @DisplayName("can execute a combination of converters")
    void testConversionChain( )
    {
        ConverterRegistry registry = this.registryInstance.get();

        ConversionProcessor<MailDomainCreationParams, MailDomainEntity> converter = registry.getProcessor(
                MailDomainCreationParams.class).withIntermediary(MailDomain.class).withOutput(MailDomainEntity.class);

        MailDomainCreationParams param = MailDomainCreationParams.builder()
                                                                 .domain("example.com")
                                                                 .action(MailDomainAction.NONE)
                                                                 .build();

        MailDomainEntity result = Assertions.assertDoesNotThrow(( ) -> converter.toItem(param).result());

        Assertions.assertEquals("example.com", result.getDomain());
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

        ConversionProcessor<MailDomain, MailDomainEntity> converter = Assertions.assertDoesNotThrow(
                ( ) -> registry.getProcessor(MailDomain.class, MailDomainEntity.class));

        String username = "noreply";
        String addressName = "noreply";
        String domainName = "example.com";

        MailUser user = MailUser.builder().username(username).isEnabled(true).build();
        MailAddress address = MailAddress.builder().name(addressName).senders(Set.of(user)).build();
        MailDomain domain = MailDomain.builder().domain(domainName).addresses(Set.of(address)).build();
        ConversionResult<MailDomainEntity> result = Assertions.assertDoesNotThrow(( ) -> converter.toItem(domain));

        MailUserEntity userEntity = MailUserEntity.builder().username(username).isEnabled(true).build();
        MailAddressEntity addressEntity = MailAddressEntity.builder()
                                                           .name(addressName)
                                                           .senders(Set.of(userEntity))
                                                           .build();
        MailDomainEntity domainEntity = MailDomainEntity.builder()
                                                        .domain(domainName)
                                                        .addresses(Set.of(addressEntity))
                                                        .build();

        Assertions.assertEquals(domainEntity.toString(), result.result().toString());
    }

    @Test
    @DisplayName("can handle general max-depth constraints")
    void testComplexTypeConversionWithGeneralMaxDepthConstraint( )
    {
        ConverterRegistry registry = this.registryInstance.get();

        ConverterConstraint constraint = ConverterConstraint.builder().maxDepth(1).build();
        ConverterTree tree = ConverterTree.builder().constraint(constraint).build();
        ConversionProcessor<MailDomain, MailDomainEntity> converter = Assertions.assertDoesNotThrow(
                ( ) -> registry.getProcessor(MailDomain.class, MailDomainEntity.class, tree));

        String username = "noreply";
        String addressName = "noreply";
        String domainName = "example.com";

        MailUser user = MailUser.builder().username(username).isEnabled(true).build();
        MailAddress address = MailAddress.builder().name(addressName).senders(Set.of(user)).build();
        MailDomain domain = MailDomain.builder().domain(domainName).addresses(Set.of(address)).build();
        ConversionResult<MailDomainEntity> result = Assertions.assertDoesNotThrow(( ) -> converter.toItem(domain));

        MailDomainEntity domainEntity = MailDomainEntity.builder().domain(domainName).build();

        Assertions.assertEquals(domainEntity.toString(), result.result().toString());
    }

    @Test
    @DisplayName("can handle type-specific max-depth constraints")
    void testComplexTypeConversionWithTypeSpecificMaxDepthConstraint( )
    {
        ConverterRegistry registry = this.registryInstance.get();

        ConverterConstraint constraint = ConverterConstraint.builder().maxDepth(0).build();
        ConverterTree tree = ConverterTree.builder().typeConstraint(MailUser.class, constraint).build();
        ConversionProcessor<MailDomain, MailDomainEntity> converter = Assertions.assertDoesNotThrow(
                ( ) -> registry.getProcessor(MailDomain.class, MailDomainEntity.class, tree));

        String username = "noreply";
        String addressName = "noreply";
        String domainName = "example.com";

        MailUser user = MailUser.builder().username(username).isEnabled(true).build();
        MailAddress address = MailAddress.builder().name(addressName).senders(Set.of(user)).build();
        MailDomain domain = MailDomain.builder().domain(domainName).addresses(Set.of(address)).build();
        ConversionResult<MailDomainEntity> result = Assertions.assertDoesNotThrow(( ) -> converter.toItem(domain));

        MailAddressEntity addressEntity = MailAddressEntity.builder().name(addressName).build();
        MailDomainEntity domainEntity = MailDomainEntity.builder()
                                                        .domain(domainName)
                                                        .addresses(Set.of(addressEntity))
                                                        .build();

        Assertions.assertEquals(domainEntity.toString(), result.result().toString());
    }

    @Test
    @DisplayName("can detect duplicates in conversion chain")
    void testDuplicateDetection( )
    {
        ConverterRegistry registry = this.registryInstance.get();

        ConversionProcessor<MailDomain, MailDomainEntity> converter = Assertions.assertDoesNotThrow(
                ( ) -> registry.getProcessor(MailDomain.class, MailDomainEntity.class));

        String username = "noreply";
        String addressName = "noreply";
        String domainName = "example.com";

        MailUser user = MailUser.builder().username(username).isEnabled(true).build();
        MailDomain duplicateDomain = MailDomain.builder().id(MailDomainId.of(1L)).domain(domainName).build();
        MailAddress address = MailAddress.builder()
                                         .name(addressName)
                                         .senders(Set.of(user))
                                         .domain(duplicateDomain)
                                         .build();
        MailDomain domain = MailDomain.builder()
                                      .id(MailDomainId.of(1L))
                                      .domain(domainName)
                                      .addresses(Set.of(address))
                                      .build();

        ConversionResult<MailDomainEntity> result = Assertions.assertDoesNotThrow(( ) -> converter.toItem(domain));

        MailUserEntity userEntity = MailUserEntity.builder().username(username).isEnabled(true).build();
        MailAddressEntity addressEntity = MailAddressEntity.builder()
                                                           .name(addressName)
                                                           .senders(Set.of(userEntity))
                                                           .build();
        MailDomainEntity domainEntity = MailDomainEntity.builder()
                                                        .id(1L)
                                                        .domain(domainName)
                                                        .addresses(Set.of(addressEntity))
                                                        .build();

        Assertions.assertEquals(domainEntity.toString(), result.result().toString());
    }
}
