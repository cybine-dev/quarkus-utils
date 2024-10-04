package de.cybine.quarkus;

import de.cybine.quarkus.data.asset.*;
import de.cybine.quarkus.data.customer.*;
import de.cybine.quarkus.data.library.*;
import de.cybine.quarkus.data.rental.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;
import de.cybine.quarkus.util.test.*;
import io.quarkus.test.junit.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.junit.jupiter.api.*;

import java.util.*;

@Slf4j
@QuarkusTest
@RequiredArgsConstructor
@DisplayName("DatasourceQuery (Options)")
@TestProfile(TestProfiles.Database.class)
class DatasourceOptionsQueryTest
{
    private GenericDatasourceService<AssetEntity, Asset>       assetService;
    private GenericDatasourceService<RentalEntity, Rental>     rentalService;
    private GenericDatasourceService<LibraryEntity, Library>   libraryService;
    private GenericDatasourceService<CustomerEntity, Customer> customerService;

    private final ConverterRegistry converterRegistry;

    @BeforeEach
    void setup( )
    {
        this.assetService = GenericDatasourceService.forType(AssetEntity.class, Asset.class);
        this.rentalService = GenericDatasourceService.forType(RentalEntity.class, Rental.class);
        this.libraryService = GenericDatasourceService.forType(LibraryEntity.class, Library.class);
        this.customerService = GenericDatasourceService.forType(CustomerEntity.class, Customer.class);
    }

    @Test
    @DisplayName("can fetch single option field")
    void canFetchSingleOption( )
    {
        DatasourceQuery query = DatasourceQuery.builder().field(AssetEntity_.LIBRARY_ID.getName()).build();

        List<Map<String, Object>> options = this.assetService.fetchOptions(query);

        Assertions.assertNotNull(options);
        Assertions.assertFalse(options.isEmpty());
        Assertions.assertEquals(1, options.get(0).entrySet().size());

        List<Long> libraryIds = options.stream()
                                       .map(item -> (Long) item.get(AssetEntity_.LIBRARY_ID.getName()))
                                       .toList();

        Assertions.assertEquals(1, options.size());
        Assertions.assertTrue(libraryIds.contains(1L));
    }

    @Test
    @DisplayName("can fetch multiple option fields")
    void canFetchMultipleOptions( )
    {
        DatasourceQuery query = DatasourceQuery.builder()
                                               .field(AssetEntity_.NAME.getName())
                                               .field(AssetEntity_.LIBRARY_ID.getName())
                                               .build();

        List<Map<String, Object>> options = this.assetService.fetchOptions(query);

        Assertions.assertNotNull(options);
        Assertions.assertFalse(options.isEmpty());
        Assertions.assertEquals(2, options.get(0).entrySet().size());

        List<Long> libraryIds = options.stream()
                                       .map(item -> (Long) item.get(AssetEntity_.LIBRARY_ID.getName()))
                                       .distinct()
                                       .toList();

        Assertions.assertEquals(1, libraryIds.size());
        Assertions.assertTrue(libraryIds.contains(1L));

        List<String> names = options.stream().map(item -> (String) item.get(AssetEntity_.NAME.getName())).toList();
        Assertions.assertEquals(4, names.size());
        Assertions.assertTrue(names.contains("Pixels"));
    }
}
