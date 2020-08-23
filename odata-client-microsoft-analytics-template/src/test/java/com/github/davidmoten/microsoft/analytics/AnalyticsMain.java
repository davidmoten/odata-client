package com.github.davidmoten.microsoft.analytics;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.RequestHeader;

import microsoft.vs.analytics.myorg.container.Container;

public class AnalyticsMain {

	public static void main(String[] args) {

		// generate the classes from the metadata using the pom plugin
		// then create a client in code using those generated classes.
		// The class Container below is the service entry point (and is
		// in the container sub-package (generated).

		String organization = Preconditions.checkNotNull(System.getProperty("organization"));
		String project = "MyTestProject1";
		String username = Preconditions.checkNotNull(System.getProperty("username"));
		String password = Preconditions.checkNotNull(System.getProperty("password"));

		String version = "3.0";
		String baseUrl = "https://analytics.dev.azure.com/" + organization + "/" + project + "/_odata/v" + version;
		Container client = Analytics //
				.service(Container.class) //
				.baseUrl(baseUrl) //
				.tenantName("123456.onmicrosoft.com") //
				.clientId("not used") //
				.clientSecret("not used") //
				.authenticator((url, requestHeaders) -> {
					// some streaming endpoints object to auth so don't add header
					// if not on the base service
					if (url.toExternalForm().startsWith(baseUrl)) {
						// remove Authorization header if present
						List<RequestHeader> list = requestHeaders //
								.stream() //
								.filter(rh -> !rh.name().equalsIgnoreCase("Authorization")) //
								.collect(Collectors.toList());
						// add basic auth request header
						list.add(basicAuth(username, password));
						return list;
					} else {
						return requestHeaders;
					}
				}).build();
		client //
				.workItems() //
				.select("WorkItemId,State,CreatedDate") //
				.stream() //
				.filter(x -> x.getCreatedDate().isPresent()) //
				.sorted( (x,y) -> x.getCreatedDate().get().compareTo(y.getCreatedDate().get())) //
				.map(x -> x.getWorkItemId() + ", " + x.getState() + ", " +  x.getCreatedDate()) //
				.forEach(System.out::println);
	}

	private static RequestHeader basicAuth(String username, String password) {
		String s = username + ":" + password;
		String encoded = Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
		return RequestHeader.create("Authorization", "Basic " + encoded);
	}

}
