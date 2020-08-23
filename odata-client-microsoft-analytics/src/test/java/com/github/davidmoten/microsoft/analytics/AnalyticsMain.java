package com.github.davidmoten.microsoft.analytics;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.RequestOptions;

import microsoft.vs.analytics.v3.myorg.container.Container;

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
		String baseUrl = "https://analytics.dev.azure.com/" + organization + "/_odata/v" + version;
		
		Container client = Analytics //
				.service(Container.class) //
				.baseUrl(baseUrl) //
				.basicAuthentication(username, password) //
				.build();
		
		// System.out.println(client._custom().getString(baseUrl + "/$metadata", RequestOptions.EMPTY));
		
		// print out work items
		client //
				.workItems() //
				.select("WorkItemId,State,CreatedDate") //
				.stream() //
				.filter(x -> x.getCreatedDate().isPresent()) //
				.sorted( (x,y) -> x.getCreatedDate().get().compareTo(y.getCreatedDate().get())) //
				.map(x -> x.getWorkItemId() + ", " + x.getState() + ", " +  x.getCreatedDate()) //
				.forEach(System.out::println);
		
		// print out areas
		client.areas().forEach(System.out::println);
	}

}
