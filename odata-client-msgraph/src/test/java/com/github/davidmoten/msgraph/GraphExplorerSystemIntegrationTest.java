package com.github.davidmoten.msgraph;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.User;

public class GraphExplorerSystemIntegrationTest {

	@Test
	public void testGetMeUser() {
		if (Util.sysIntEnabled()) {
			GraphService client = MsGraph.explorer().build();
			User user = client.me().select("id,identities").get();
			assertTrue(!user.getIdentities().toList().isEmpty());
		}
	}

	@Test
	public void testGetAttachments() {
		if (Util.sysIntEnabled()) {
			GraphService client = MsGraph.explorer().build();
			client //
					.me() //
					.messages() //
					.select("id") //
					.stream() //
					.flatMap(m -> m.getAttachments() //
							.select("name,contentType") //
							.stream()) //
					.limit(4) //
					.forEach(System.out::println);
		}
	}

}
