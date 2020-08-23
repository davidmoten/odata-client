package com.github.davidmoten.microsoft.analytics;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Supplier;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.msgraph.builder.MsGraphClientBuilder;
import com.github.davidmoten.msgraph.builder.MsGraphClientBuilder.UsernamePassword;
import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.HasContext;

public final class Analytics {

	//
//	private static final String SERVICE_BASE_URL = "https://analytics.dev.azure.com/amsadevelopment/_odata/v3.0";

	private Analytics() {
		// prevent instantiation
	}

	public static <T extends HasContext> Builder<T> service(Class<T> serviceClass) {
		return new Builder<T>(serviceClass);
	}

	public static final class Builder<T extends HasContext> {

		private final Class<T> serviceCls;
		private Optional<String> baseUrl = Optional.empty();
		private Optional<String> organization = Optional.empty();
		private Optional<String> project = Optional.empty();
		private Optional<String> version;

		Builder(Class<T> serviceClass) {
			Preconditions.checkNotNull(serviceClass);
			this.serviceCls = serviceClass;
		}

		public Builder3<T> baseUrl(String baseUrl) {
			Preconditions.checkNotNull(baseUrl);
			this.baseUrl = Optional.of(baseUrl);
			return new Builder3<T>(this);
		}

		public Builder2<T> organization(String organization) {
			Preconditions.checkNotNull(organization);
			this.organization = Optional.of(organization);
			return new Builder2<T>(this);
		}

	}

	public static final class Builder2<T extends HasContext> {

		private final Builder<T> b;

		Builder2(Builder<T> b) {
			this.b = b;
		}

		public Builder2<T> project(String project) {
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
		public Builder3<T> version(String version) {
			Preconditions.checkNotNull(version);
			b.version = Optional.of(version);
			return new Builder3<T>(b);
		}

	}

	public static final class Builder3<T extends HasContext> {

		private static final String ANALYTICS_DEFAULT_VERSION = "v3.0";
		private static final String ANALYTICS_BASE_URL = "https://analytics.dev.azure.com/";
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
			String baseUrl = b.baseUrl.orElseGet(() -> {
				return ANALYTICS_BASE_URL + b.organization.get() //
						+ b.project.map(x -> "/" + x).orElse("") //
						+ "/_odata" //
						+ b.version.map(x -> "/" + x).orElse(ANALYTICS_DEFAULT_VERSION);
			});
			return new MsGraphClientBuilder<T> //
			(baseUrl, context -> {
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
