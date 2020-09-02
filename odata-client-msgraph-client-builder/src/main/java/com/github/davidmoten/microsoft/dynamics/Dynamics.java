package com.github.davidmoten.microsoft.dynamics;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Supplier;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.msgraph.builder.MsGraphClientBuilder;
import com.github.davidmoten.msgraph.builder.MsGraphClientBuilder.UsernamePassword;
import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.HasContext;

public final class Dynamics {

    private Dynamics() {
        // prevent instantiation
    }

    public static <T extends HasContext> Builder<T> service(Class<T> serviceClass) {
        return new Builder<T>(serviceClass);
    }

    public static final class Builder<T extends HasContext> {

        private final Class<T> serviceCls;
        private Optional<String> baseUrl = Optional.empty();

        Builder(Class<T> serviceClass) {
            Preconditions.checkNotNull(serviceClass);
            this.serviceCls = serviceClass;
        }

        /**
         * Expected URL is like https://SOLUTION.crm4.dynamics.com.
         * @param baseUrl
         * @return
         */
        public Builder3<T> baseUrl(String baseUrl) {
            Preconditions.checkNotNull(baseUrl);
            this.baseUrl = Optional.of(baseUrl);
            return new Builder3<T>(this);
        }

    }

    public static final class Builder3<T extends HasContext> {

        private final Builder<T> b;

        public Builder3(Builder<T> b) {
            this.b = b;
        }

        public com.github.davidmoten.msgraph.builder.MsGraphClientBuilder.Builder3<T> basicAuthentication(
                Supplier<UsernamePassword> usernamePassword) {
            return createBuilder().basicAuthentication(usernamePassword);
        }

        public com.github.davidmoten.msgraph.builder.MsGraphClientBuilder.Builder3<T> basicAuthentication(
                String username, String password) {
            return basicAuthentication(() -> UsernamePassword.create(username, password));
        }

        public MsGraphClientBuilder.Builder<T> tenantName(String tenantName) {
            return createBuilder().tenantName(tenantName);
        }

        private MsGraphClientBuilder<T> createBuilder() {
            return new MsGraphClientBuilder<T> //
            (b.baseUrl.get(), context -> {
                try {
                    return b.serviceCls.getConstructor(Context.class).newInstance(context);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    throw new ClientException(e);
                }
            });
        }
    }

}
