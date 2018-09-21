package com.github.davidmoten.odata.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.PathStyle;
import com.github.davidmoten.odata.client.Serializer;
import com.github.davidmoten.odata.client.Service;
import com.github.davidmoten.odata.client.TestingService;
import com.github.davidmoten.odata.client.TestingService.Builder;

import odata.test.container.DemoService;
import odata.test.entity.Product;

public class SystemTest {

    @Test
    public void testTopLevelCollection() {
        DemoService client = createClient("/Products", "/response-products.json");
        List<Product> page = client.products().get().currentPage();
        assertEquals(11, page.size());
    }

    @Test
    public void testOneItemFromTopLevelCollection() {
        DemoService client = createClient("/Products(1)", "/response-product.json");
        Product p = client.products("1").get();
        assertEquals("Milk", p.getName().get());
    }

    private DemoService client(Builder b) {
        return new DemoService(new Context(Serializer.DEFAULT, b.build()));
    }

    private static TestingService.Builder serviceBuilder() {
        return TestingService //
                .baseUrl("https://services.odata.org/Experimental/OData/OData.svc") //
                .pathStyle(PathStyle.IDENTIFIERS_IN_ROUND_BRACKETS);
    }

    private static DemoService createClient(String path, String resource) {
        Service service = serviceBuilder() //
                .replyWithResource(path, resource) //
                .build();
        return new DemoService(new Context(Serializer.DEFAULT, service));
    }

}
