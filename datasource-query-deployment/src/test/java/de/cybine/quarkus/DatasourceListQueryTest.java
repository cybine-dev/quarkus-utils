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
@DisplayName("DatasourceQuery (List)")
@TestProfile(TestProfiles.Database.class)
class DatasourceListQueryTest
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
    @DisplayName("can fetch items")
    void canFetchItems( )
    {
        List<Asset> assets = this.assetService.fetch(DatasourceQuery.builder().build());

        Assertions.assertNotNull(assets);
        Assertions.assertEquals(4, assets.size());
    }

    @Test
    @DisplayName("can handle pagination")
    void canHandlePagination()
    {
        DatasourcePaginationInfo pagination1 = DatasourcePaginationInfo.builder().size(2).build();
        DatasourceQuery query1 = DatasourceQuery.builder().pagination(pagination1).build();
        List<Asset> assets1 = this.assetService.fetch(query1);

        Assertions.assertNotNull(assets1);
        Assertions.assertEquals(2, assets1.size());

        DatasourcePaginationInfo pagination2 = DatasourcePaginationInfo.builder().size(1).offset(2).build();
        DatasourceQuery query2 = DatasourceQuery.builder().pagination(pagination2).build();
        List<Asset> assets2 = this.assetService.fetch(query2);

        Assertions.assertNotNull(assets2);
        Assertions.assertEquals(1, assets2.size());

        Asset asset = assets2.get(0);
        Assertions.assertFalse(assets1.contains(asset));
    }
}
