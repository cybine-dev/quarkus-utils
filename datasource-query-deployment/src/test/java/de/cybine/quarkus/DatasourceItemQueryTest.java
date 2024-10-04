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

@Slf4j
@QuarkusTest
@RequiredArgsConstructor
@DisplayName("DatasourceQuery (Item)")
@TestProfile(TestProfiles.Database.class)
class DatasourceItemQueryTest
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
    @DisplayName("can fetch item")
    void canFetchItem( )
    {
        Asset asset = this.assetService.fetchSingle(DatasourceQuery.builder().build()).orElse(null);
        Assertions.assertNotNull(asset);
    }
}