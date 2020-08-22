package com.github.davidmoten.microsoft.analytics;

import org.junit.Test;

import microsoft.vs.analytics.myorg.container.Container;

public class AnalyticsTest {

	@Test
	public void testCreateClientFromContainer() {

		// generate the classes from the metadata using the pom plugin
		// then create a client in code using those generated classes.
		// The class Container below is the service entry point (and is
		// in the container sub-package (generated).

		String organization = "myorg";
		String version = "3.0";
		@SuppressWarnings("unused")
		Container client = Analytics //
				.service(Container.class) //
				.baseUrl("https://analytics.dev.azure.com/" + organization + "/_odata/v" + version) //
				.tenantName("123456.onmicrosoft.com") //
				.clientId("clientId") //
				.clientSecret("clientSecret") //
				.build();
	}

}
