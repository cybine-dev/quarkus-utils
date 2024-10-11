package de.cybine.quarkus;

import de.cybine.quarkus.data.customer.*;
import de.cybine.quarkus.data.library.*;
import de.cybine.quarkus.util.datasource.*;
import de.cybine.quarkus.util.test.*;
import io.quarkus.test.junit.*;
import lombok.*;
import org.junit.jupiter.api.*;

import java.util.*;

@QuarkusTest
@RequiredArgsConstructor
@DisplayName("DatasourceNamingStrategy")
@TestProfile(TestProfiles.Database.class)
class DatasourceNamingStrategyTest
{
    private GenericDatasourceService<LibraryEntity, Library>   libraryService;
    private GenericDatasourceService<CustomerEntity, Customer> customerService;

    @BeforeEach
    void setup( )
    {
        this.libraryService = GenericDatasourceService.forType(LibraryEntity.class, Library.class);
        this.customerService = GenericDatasourceService.forType(CustomerEntity.class, Customer.class);
    }

    @Test
    @DisplayName("can resolve default table name")
    void canResolveDefaultTableName( )
    {
        List<Customer> customers = Assertions.assertDoesNotThrow(
                ( ) -> this.customerService.fetch(DatasourceQuery.builder().build()));

        Assertions.assertEquals(3, customers.size());
    }

    @Test
    @DisplayName("can resolve replacement table name")
    void canResolveReplacementTableName( )
    {
        List<Library> libraries = Assertions.assertDoesNotThrow(
                ( ) -> this.libraryService.fetch(DatasourceQuery.builder().build()));

        Assertions.assertEquals(2, libraries.size());
    }
}
