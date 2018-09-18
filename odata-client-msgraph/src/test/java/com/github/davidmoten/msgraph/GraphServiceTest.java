package com.github.davidmoten.msgraph;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.PathStyle;
import com.github.davidmoten.odata.client.Serializer;
import com.github.davidmoten.odata.client.Service;
import com.github.davidmoten.odata.client.TestingService;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.User;

public class GraphServiceTest {

    @Test
    public void testGetEntity() {
        Service service = TestingService //
                .baseUrl("https://testing.com") //
                .pathStyle(PathStyle.IDENTIFIERS_AS_SEGMENTS) //
                .replyWithResource("https://testing.com/users/1", "/response-user.json") //
                .build();
        Context c = new Context(Serializer.DEFAULT, service);
        User user = new GraphService(c).users("1").get();
        assertEquals("Conf Room Adams", user.getDisplayName().get());
        assertEquals(1, user.getBusinessPhones().values().size());
        assertEquals("+61 2 1234567", user.getBusinessPhones().values().get(0));
    }
}
