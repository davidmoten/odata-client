package com.github.davidmoten.odata.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.HttpMethod;
import com.github.davidmoten.odata.client.PathStyle;
import com.github.davidmoten.odata.client.Serializer;
import com.github.davidmoten.odata.client.Service;
import com.github.davidmoten.odata.client.TestingService;
import com.github.davidmoten.odata.client.TestingService.Builder;

import odata.test.container.DemoService;
import odata.test.entity.Customer;
import odata.test.entity.Employee;
import odata.test.entity.Person;
import odata.test.entity.PersonDetail;
import odata.test.entity.Product;

public class DemoServiceTest {

    @Test
    public void testTopLevelCollection() {
        DemoService client = createClient("/Products", "/response-products.json");
        List<Product> page = client.products().get().values();
        assertEquals(11, page.size());
    }

    @Test
    public void testTopLevelCollectionReturnsSubClasses() {
        DemoService client = createClient("/Persons", "/response-persons.json");
        List<Person> page = client.persons().get().values();
        assertEquals(7, page.size());
        assertTrue(page.get(0) instanceof Person);
        System.out.println(page.get(3));
        assertTrue(page.get(3) instanceof Customer);
        assertTrue(page.get(5) instanceof Employee);
    }

    @Test
    public void testTopLevelPersonDetails() {
        DemoService client = createClient("/PersonDetails", "/response-person-details.json");
        List<PersonDetail> page = client.personDetails().get().values();
        assertEquals(7, page.size());
    }

    @Test
    public void testCollectionSelect() {
        DemoService client = createClient("/Products?$select=Name",
                "/response-products-select-name.json");
        List<Product> page = client.products().select("Name").get().values();
        assertEquals(11, page.size());
    }

    @Test
    public void testCollectionFilter() {
        DemoService client = createClient("/Products?$filter=Name%20eq%20'Bread'",
                "/response-products-filter-bread.json");
        List<Product> page = client.products().filter("Name eq 'Bread'").get().values();
        assertEquals(1, page.size());
    }

    @Test
    public void testCollectionFilterAndTop() {
        DemoService client = createClient("/Products?$top=3&$filter=Rating%20eq%203",
                "/response-products-filter-rating-3-top-3.json");
        List<Product> page = client.products().filter("Rating eq 3").top(3).get().values();
        assertEquals(3, page.size());
    }

    @Test
    public void testOneItemFromTopLevelCollection() {
        DemoService client = createClient("/Products(1)", "/response-product.json");
        Product p = client.products("1").get();
        assertEquals("Milk", p.getName().get());
    }

    @Test
    public void testEntityCollectionIsIterable() {
        DemoService client = createClient("/Products", "/response-products.json");
        int count = 0;
        for (Product p : client.products().get()) {
            count++;
        }
        assertEquals(11, count);
    }

    @Test
    public void serializeProduct() {
        Product p = Product //
                .builder() //
                .build() //
                .withDescription(Optional.of("Lower fat milk"));
        Serializer.DEFAULT.serialize(p);
        assertEquals("Lower fat milk", p.getDescription().get());
    }

    @Test
    public void testEntityPatch() {
        DemoService client = client(serviceBuilder().expectRequest("/Products(1)",
                "/request-product-patch.json", HttpMethod.PATCH));
        Product p = Product //
                .builder() //
                .build() //
                .withDescription(Optional.of("Lower fat milk"));
        Product product = client.products("1") //
                .patch(p);
        assertEquals("Lower fat milk", product.getDescription().get());
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
