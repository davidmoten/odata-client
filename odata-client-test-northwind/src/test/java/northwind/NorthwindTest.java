package northwind;

import org.junit.Ignore;
import org.junit.Test;

import odata.northwind.experimental.model.container.NorthwindEntities;

public class NorthwindTest {

    private static final String BASE_URL = "https://services.odata.org/Experimental/Northwind/Northwind.svc";

    @Test
    @Ignore
    public void testCanGetEntityWhenHasMultipleKeys() {
        NorthwindEntities client = NorthwindEntities //
                .test() //
                .baseUrl(BASE_URL) //
                .expectResponse("/Order_Details(OrderID=10248,ProductID=42)",
                        "/response-order-details-one-record.json")
                .build();

        client.order_Details().id("OrderID=10248,ProductID=42").get();
    }

}
