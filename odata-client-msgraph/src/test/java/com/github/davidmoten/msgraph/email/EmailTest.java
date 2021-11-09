package com.github.davidmoten.msgraph.email;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import com.github.davidmoten.msgraph.Email;
import com.github.davidmoten.odata.client.Retries;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.enums.BodyType;

public final class EmailTest {

	@Test
	@Ignore
	public void testSendMailCompiles() {
		GraphService client = null;
		Email //
				.mailbox("sender@marathon.com") //
				.subject("Hi there") //
				.bodyType(BodyType.TEXT) //
				.body("Just a quick test") //
				.to("dave@gmail.com") //
				.bcc("sarah@gmail.com", "andrew@gmail.com") //
				.header("x-security-classification", "OFFICIAL") //
				.attachment(new File("info.txt")) //
				.contentMimeType("text/plain") //
				.readTimeout(10, TimeUnit.MINUTES) //
				.retries(Retries.builder().maxRetries(3).build()) //
				.name("more-info.txt") //
				.attachment("some info for you") //
				.name("info2.txt") //
				.chunkSize(512 * 1024) //
				.attachment(new byte[] {})
				.contentMimeType("text/plain")
				.name("empty.txt")
				.attachment(new ByteArrayInputStream(new byte[] {0, 1})) //
				.length(2) //
				.name("two-bytes") //
				.contentMimeType("application/octet-stream") //
				.send(client);
	}

}
