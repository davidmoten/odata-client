package com.github.davidmoten.microsoft.authentication;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.microsoft.client.builder.Creator;
import com.github.davidmoten.microsoft.client.builder.MicrosoftClientBuilder;
import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.HasContext;
import com.github.davidmoten.odata.client.PathStyle;

public final class DynamicsCrmExample{

    private DynamicsCrmExample() {
        // prevent instantiation
    }

    public static <T extends HasContext> Builder<T> service(Class<T> serviceClass) {
        return new Builder<T>(serviceClass);
    }

    public static final class Builder<T extends HasContext> {

        private final Class<T> serviceCls;
        private Optional<String> baseUrl = Optional.empty();
        private PathStyle pathStyle = PathStyle.IDENTIFIERS_IN_ROUND_BRACKETS;

        Builder(Class<T> serviceClass) {
            Preconditions.checkNotNull(serviceClass);
            this.serviceCls = serviceClass;
        }

        public Builder<T> pathStyle(PathStyle pathStyle) {
            this.pathStyle = pathStyle;
            return this;
        }

        /**
         * Expected URL is like https://SOLUTION.crm4.dynamics.com.
         * @param baseUrl
         * @return builder
         */
        public BuilderWithBaseUrl<T> baseUrl(String baseUrl) {
            Preconditions.checkNotNull(baseUrl);
            this.baseUrl = Optional.of(baseUrl);
            return new BuilderWithBaseUrl<T>(this);
        }
    }

    public static final class BuilderWithBaseUrl<T extends HasContext> {

        private final Builder<T> b;

        public BuilderWithBaseUrl(Builder<T> b) {
            this.b = b;
        }
        
        public com.github.davidmoten.microsoft.client.builder.MicrosoftClientBuilder.Builder<T> tokenUrl(String tokenUrl) {
            return createBuilder().tokenUrl(tokenUrl);
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
                    // add your schema here
                    // addSchema(SchemaInfo.) //
                    .pathStyle(b.pathStyle) //
                    .build();
        }
    }
}
