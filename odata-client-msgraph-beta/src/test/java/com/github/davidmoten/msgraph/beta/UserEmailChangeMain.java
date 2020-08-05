package com.github.davidmoten.msgraph.beta;

import java.util.List;

import odata.msgraph.client.beta.complex.ObjectIdentity;
import odata.msgraph.client.beta.container.GraphService;
import odata.msgraph.client.beta.entity.User;

public class UserEmailChangeMain {

	public static void main(String[] args) {
		// for compilation only, not running
		GraphService client = MsGraph.explorer().build();
		User me = client.me().get();
		List<ObjectIdentity> ids = me.getIdentities().toList();
		ids.add(ObjectIdentity.builder().issuerAssignedId("blah").issuerAssignedId("id").build());
	    me = me.withIdentities(ids).patch();
	}
	
}
