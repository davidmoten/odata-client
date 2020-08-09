package com.github.davidmoten.msgraph;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.Message;

public class GraphSystemIntegrationTest {

	private static boolean enabled() {
		return "true".equalsIgnoreCase(System.getProperty("sysint"));
	}

	@Test
	public void testGetUserFromMsGraphV1Endpoint() {
		if (enabled()) {
			GraphService client = MsGraph //
					.tenantName(System.getProperty("msgraph.sysint.tenant.name")) //
					.clientId(System.getProperty("msgraph.sysint.client.id")) //
					.clientSecret(System.getProperty("msgraph.sysint.client.secret")) //
					.refreshBeforeExpiry(5, TimeUnit.MINUTES) //
					.build();
			Message m = client.users(System.getProperty("msgraph.sysint.user")).messages().select("id").get().stream().findFirst().get();
			System.out.println(m.getId().get());
		}
	}

}
