package com.github.davidmoten.odata.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.davidmoten.odata.client.HttpRequestOptions;
import com.github.davidmoten.odata.client.RequestHeader;

import test5.container.Test5Service;
import test5.entity.Product;

public class CustomRequestTest {

	@Test
	public void testCustomRequestGet() {
		Test5Service client = Test5Service.test() //
				.expectRequest("/Products/1") //
				.withResponse("/response-product-1.json") //
				.withRequestHeaders(RequestHeader.ACCEPT_JSON,
						RequestHeader.ODATA_VERSION) //
				.build();
		Product p = client._custom().get("https://testing.com/Products/1", Product.class,
				HttpRequestOptions.EMPTY);
		System.out.println(p);
		assertEquals(1, (int) p.getID().get());
	}
}
