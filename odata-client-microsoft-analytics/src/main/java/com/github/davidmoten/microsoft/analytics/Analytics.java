package com.github.davidmoten.microsoft.analytics;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Supplier;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.microsoft.client.builder.Creator;
import com.github.davidmoten.microsoft.client.builder.MicrosoftClientBuilder;
import com.github.davidmoten.microsoft.client.builder.MicrosoftClientBuilder.BuilderWithBasicAuthentication;
import com.github.davidmoten.microsoft.client.builder.MicrosoftClientBuilder.UsernamePassword;
import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.HasContext;

public final class Analytics {

    //
    //	private static final String SERVICE_BASE_URL = "https://analytics.dev.azure.com/amsadevelopment/_odata/v3.0";

    private Analytics() {
        // prevent instantiation
    }

    public static <T extends HasContext> BuilderWithServiceClass<T> service(Class<T> serviceClass) {
        return new BuilderWithServiceClass<T>(serviceClass);
    }

    public static final class BuilderWithServiceClass<T extends HasContext> {

        private final Class<T> serviceCls;
        private Optional<String> baseUrl = Optional.empty();
        private Optional<String> organization = Optional.empty();
        private Optional<String> project = Optional.empty();
        private Optional<String> version;

        BuilderWithServiceClass(Class<T> serviceClass) {
            Preconditions.checkNotNull(serviceClass);
            this.serviceCls = serviceClass;
        }

        public BuilderWithBaseUrl<T> baseUrl(String baseUrl) {
            Preconditions.checkNotNull(baseUrl);
            this.baseUrl = Optional.of(baseUrl);
            return new BuilderWithBaseUrl<T>(this);
        }

        public BuilderWithOrganization<T> organization(String organization) {
            Preconditions.checkNotNull(organization);
            this.organization = Optional.of(organization);
            return new BuilderWithOrganization<T>(this);
        }

    }

    public static final class BuilderWithOrganization<T extends HasContext> {

        private final BuilderWithServiceClass<T> b;

        BuilderWithOrganization(BuilderWithServiceClass<T> b) {
            this.b = b;
        }

        public BuilderWithOrganization<T> project(String project) {
            Preconditions.checkNotNull(project);
            b.project = Optional.of(project);
            return this;
        }

        /**
         * Sets the version of the API. Expected values look like 'v1.0', 'v2.0',
         * 'v3.0', 'v4.0-preview'.
         * 
         * @param version version to use
         * @return this
         */
        public BuilderWithBaseUrl<T> version(String version) {
            Preconditions.checkNotNull(version);
            b.version = Optional.of(version);
            return new BuilderWithBaseUrl<T>(b);
        }

    }

    public static final class BuilderWithBaseUrl<T extends HasContext> {

        private static final String ANALYTICS_DEFAULT_VERSION = "v3.0";
        private static final String ANALYTICS_BASE_URL = "https://analytics.dev.azure.com/";
        private final BuilderWithServiceClass<T> b;

        public BuilderWithBaseUrl(BuilderWithServiceClass<T> b) {
            this.b = b;
        }

        public BuilderWithBasicAuthentication<T> basicAuthentication(
                Supplier<UsernamePassword> usernamePassword) {
            return createBuilder().basicAuthentication(usernamePassword);
        }

        public BuilderWithBasicAuthentication<T> basicAuthentication(String username,
                String password) {
            return basicAuthentication(() -> UsernamePassword.create(username, password));
        }

        public com.github.davidmoten.microsoft.client.builder.MicrosoftClientBuilder.Builder<T> tenantName(
                String tenantName) {
            return createBuilder().tenantName(tenantName);
        }

        private MicrosoftClientBuilder<T> createBuilder() {
            String baseUrl = b.baseUrl.orElseGet(() -> {
                return ANALYTICS_BASE_URL + b.organization.get() //
                        + b.project.map(x -> "/" + x).orElse("") //
                        + "/_odata" //
                        + b.version.map(x -> "/" + x).orElse(ANALYTICS_DEFAULT_VERSION);
            });
            Creator<T> creator = context -> {
                try {
                    return b.serviceCls.getConstructor(Context.class).newInstance(context);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    throw new ClientException(e);
                }
            };
            return MicrosoftClientBuilder //
                    .baseUrl(baseUrl) //
                    .creator(creator) //
                    .addSchema(microsoft.vs.analytics.v1.model.schema.SchemaInfo.INSTANCE) //
                    .addSchema(microsoft.vs.analytics.v1.myorg.schema.SchemaInfo.INSTANCE) //
                    .addSchema(microsoft.vs.analytics.v2.model.schema.SchemaInfo.INSTANCE) //
                    .addSchema(microsoft.vs.analytics.v2.myorg.schema.SchemaInfo.INSTANCE) //
                    .addSchema(microsoft.vs.analytics.v3.model.schema.SchemaInfo.INSTANCE) //
                    .addSchema(microsoft.vs.analytics.v3.myorg.schema.SchemaInfo.INSTANCE) //
                    .addSchema(microsoft.vs.analytics.v4.model.schema.SchemaInfo.INSTANCE) //
                    .addSchema(microsoft.vs.analytics.v4.schema.SchemaInfo.INSTANCE) //
                    .build();
        }
    }

}
