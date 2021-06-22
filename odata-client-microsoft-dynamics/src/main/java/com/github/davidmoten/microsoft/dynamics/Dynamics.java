package com.github.davidmoten.microsoft.dynamics;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Supplier;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.microsoft.client.builder.Creator;
import com.github.davidmoten.microsoft.client.builder.MicrosoftClientBuilder;
import com.github.davidmoten.microsoft.client.builder.MicrosoftClientBuilder.UsernamePassword;
import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.HasContext;

import microsoft.dynamics.crm.schema.SchemaInfo;

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
         * @return builder
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

        public MicrosoftClientBuilder.Builder5<T> basicAuthentication(
                Supplier<UsernamePassword> usernamePassword) {
            return createBuilder().basicAuthentication(usernamePassword);
        }

        public MicrosoftClientBuilder.Builder5<T> basicAuthentication(
                String username, String password) {
            return basicAuthentication(() -> UsernamePassword.create(username, password));
        }

        public com.github.davidmoten.microsoft.client.builder.MicrosoftClientBuilder.Builder<T> tenantName(String tenantName) {
            return createBuilder().tenantName(tenantName);
        }

        private MicrosoftClientBuilder<T> createBuilder() {
            Creator<T> creator = context -> {
                try {
                    return b.serviceCls.getConstructor(Context.class).newInstance(context);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    throw new ClientException(e);
                }
            };
            return MicrosoftClientBuilder //
                    .baseUrl(b.baseUrl.get()) //
                    .creator(creator) //
                    .addSchema(SchemaInfo.INSTANCE) //
                    .build();
        }
    }

}
