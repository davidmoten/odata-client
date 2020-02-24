package com.github.davidmoten.odata.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.github.davidmoten.odata.client.HttpMethod;
import com.github.davidmoten.odata.client.PathStyle;
import com.github.davidmoten.odata.client.RequestHeader;
import com.github.davidmoten.odata.client.Serializer;
import com.github.davidmoten.odata.client.TestingService.ContainerBuilder;

import odata.test.container.DemoService;
import odata.test.entity.Customer;
import odata.test.entity.Employee;
import odata.test.entity.Person;
import odata.test.entity.PersonDetail;
import odata.test.entity.Product;

public class DemoServiceTest {

    @Test
    public void testTopLevelCollection() {
        DemoService client = createClient("/Products", "/response-products.json",
                RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION);
        List<Product> page = client.products().get().currentPage();
        assertEquals(11, page.size());
    }

    @Test
    public void testTopLevelCollectionReturnsSubClasses() {
        DemoService client = createClient("/Persons", "/response-persons.json",
                RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION);
        List<Person> page = client.persons().get().currentPage();
        assertEquals(7, page.size());
        assertTrue(page.get(0) instanceof Person);
        System.out.println(page.get(3));
        assertTrue(page.get(3) instanceof Customer);
        assertTrue(page.get(5) instanceof Employee);
    }

    @Test
    public void testTopLevelPersonDetails() {
        DemoService client = createClient("/PersonDetails", "/response-person-details.json",
                RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION);
        List<PersonDetail> page = client.personDetails().get().currentPage();
        assertEquals(7, page.size());
    }

    @Test
    public void testCollectionSelect() {
        DemoService client = createClient("/Products?$select=Name",
                "/response-products-select-name.json", RequestHeader.ACCEPT_JSON_METADATA_MINIMAL,
                RequestHeader.ODATA_VERSION);
        List<Product> page = client.products().select("Name").get().currentPage();
        assertEquals(11, page.size());
    }

    @Test
    public void testCollectionFilter() {
        DemoService client = createClient("/Products?$filter=Name%20eq%20'Bread'",
                "/response-products-filter-bread.json", RequestHeader.ACCEPT_JSON_METADATA_MINIMAL,
                RequestHeader.ODATA_VERSION);
        List<Product> page = client.products().filter("Name eq 'Bread'").get().currentPage();
        assertEquals(1, page.size());
    }

    @Test
    public void testCollectionFilterAndTop() {
        DemoService client = createClient("/Products?$top=3&$filter=Rating%20eq%203",
                "/response-products-filter-rating-3-top-3.json",
                RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION);
        List<Product> page = client.products().filter("Rating eq 3").top(3).get().currentPage();
        assertEquals(3, page.size());
    }

    @Test
    public void testOneItemFromTopLevelCollection() {
        DemoService client = createClient("/Products(1)", "/response-product.json",
                RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION);
        Product p = client.products(1).get();
        assertEquals("Milk", p.getName().get());
    }

    @Test
    public void testEntityCollectionIsIterable() {
        DemoService client = createClient("/Products", "/response-products.json",
                RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION);
        int count = 0;
        for (@SuppressWarnings("unused")
        Product p : client.products().get()) {
            count++;
        }
        assertEquals(11, count);
    }

    @Test
    public void serializeProduct() {
        Product p = Product //
                .builder() //
                .description("Lower fat milk") //
                .build();
        Serializer.INSTANCE.serialize(p);
        assertEquals("Lower fat milk", p.getDescription().get());
    }

    @Test
    public void testEntityPatch() {
        DemoService client = serviceBuilder()
                .expectResponse("/Products(1)", "/response-product.json",
                        RequestHeader.CONTENT_TYPE_JSON_METADATA_MINIMAL,
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .expectRequest("/Products(1)", "/request-product-patch.json", HttpMethod.PATCH,
                        RequestHeader.CONTENT_TYPE_JSON_METADATA_MINIMAL,
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .build();
        Product p = Product //
                .builder() //
                .description("Lowest fat milk") //
                .build();
        Product product = client.products(1).patch(p);
        assertEquals("Lowest fat milk", product.getDescription().get());
    }

    @Test
    public void testEntityPatchDirect() {
        DemoService client = serviceBuilder()
                .expectResponse("/Products(1)", "/response-product.json",
                        RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .expectRequest("/Products(1)", "/request-product-patch.json", HttpMethod.PATCH,
                        RequestHeader.ACCEPT_JSON, RequestHeader.CONTENT_TYPE_JSON_METADATA_MINIMAL,
                        RequestHeader.ODATA_VERSION) //
                .build();
        Product p = client.products(1).get();
        Product product = p.withDescription("Lowest fat milk").patch();
        assertEquals("Lowest fat milk", product.getDescription().get());
    }

    private static ContainerBuilder<DemoService> serviceBuilder() {
        return DemoService.test() //
                .baseUrl("https://services.odata.org/Experimental/OData/OData.svc") //
                .pathStyle(PathStyle.IDENTIFIERS_IN_ROUND_BRACKETS);
    }

    private static DemoService createClient(String path, String resource,
            RequestHeader... requestHeaders) {
        return serviceBuilder().expectResponse(path, resource, requestHeaders) //
                .build();
    }

}
