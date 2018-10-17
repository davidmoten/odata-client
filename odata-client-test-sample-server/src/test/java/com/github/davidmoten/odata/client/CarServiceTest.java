package com.github.davidmoten.odata.client;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.apache.olingo.server.sample.CarsServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.StdErrLog;
import org.junit.Test;

import com.github.davidmoten.odata.client.internal.ApacheHttpClientHttpService;

import olingo.odata.sample.container.Container;
import olingo.odata.sample.entity.Car;

public class CarServiceTest {

    @Test
    public void testWithDefaultHttpService() throws Exception {
        Path basePath = new Path("http://localhost:8090/cars.svc", PathStyle.IDENTIFIERS_IN_ROUND_BRACKETS);
        HttpService service = HttpService.createDefaultService(basePath, m -> m);
        checkServiceCalls(service).stop();
    }

    @Test
    public void testWithApacheHttpService() throws Exception {
        Path basePath = new Path("http://localhost:8090/cars.svc", PathStyle.IDENTIFIERS_IN_ROUND_BRACKETS);
        HttpService service = new ApacheHttpClientHttpService(basePath);
        checkServiceCalls(service).stop();
    }

    private static Server checkServiceCalls(HttpService service) throws Exception {
        Container c = new Container(new Context(Serializer.INSTANCE, service));
        Server server = startServer();

        // test get collection
        List<Car> list = c.cars().get().toList();
        list.stream().forEach(car -> System.out.println(car.getModel().orElse("") + " at $"
                + car.getCurrency().orElse("") + " " + car.getPrice().map(BigDecimal::toString).orElse("?")));
        assertEquals(5, list.size());
        assertEquals("F1 W03", list.get(0).getModel().orElse(null));

        // get single entity
        {
            Car car = c.cars().id("1").get();
            assertEquals("F1 W03", car.getModel().get());

            // test patch
            // TODO HttpUrlConnection does not support PATCH verb
            if (false) {
                car.withPrice(Optional.of(BigDecimal.valueOf(123456))).patch();
                car = c.cars().id("1").get();
                assertEquals(123456, car.getPrice().get());
            }

            // test put
            // TODO implement update in CarServlet
            if (false) {
                car.withPrice(Optional.of(BigDecimal.valueOf(123))).put();
                car = c.cars().id("1").get();
                assertEquals(123, car.getPrice().get());
            }
        }

        // create (post)
        // TODO support create in servlet
        if (false) {
            Car car2 = Car.builder().model("Tesla").modelYear("2018").price(BigDecimal.valueOf(50000)).currency("AUD")
                    .build();
            Car car = c.cars().post(car2);
            System.out.println("newId = " + car.getId().get());
        }
        return server;
    }

    private static Server startServer() throws Exception {
        StdErrLog logger = new StdErrLog();
        logger.setDebugEnabled(false);
        Log.setLog(logger);

        Server server = new Server(8090);

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setSessionHandler(new SessionHandler());
        handler.addServlet(CarsServlet.class, "/cars.svc/*");
        server.setHandler(handler);
        server.start();
        return server;
    }

}
