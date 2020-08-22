package com.github.davidmoten.microsoft.analytics;

import org.junit.Test;

import microsoft.vs.analytics.myorg.container.Container;

public class AnalyticsTest {

	@Test
	public void testCreateClientFromContainer() {
		String organization = "myorg";
		String version = "3.0";
		Container client = Analytics //
				.service(Container.class) //
				.baseUrl("https://analytics.dev.azure.com/" + organization + "/_odata/v" + version) //
				.tenantName("123456.onmicrosoft.com") //
				.clientId("clientId") //
				.clientSecret("clientSecret") //
				.build();
	}

}
