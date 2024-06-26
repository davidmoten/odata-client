package com.github.davidmoten.microsoft.authentication;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.HasContext;

public class DynamicsCrmExampleTest {

    @Test
    public void testCompiles() {
        DynamicsCrmExample //
                .service(TheService.class) //
                .baseUrl("https://onprem") //
                .tokenUrl("https://onprem/token/oauth2") //
                .resource("https://theresource") //
                .scope("https://theresource/.default") //
                .clientId("clientId") //
                .clientSecret("secret") //
                .connectTimeout(30, TimeUnit.SECONDS) //
                .readTimeout(5, TimeUnit.MINUTES) //
                .build();
    }

    public static final class TheService implements HasContext {

        private final Context context;
        
        public TheService(Context context) {
            this.context = context;
        }
        
        @Override
        public Context _context() {
            return context;
        }

    }

}
