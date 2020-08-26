package com.github.davidmoten.msgraph;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import com.github.davidmoten.odata.client.Retries;

import odata.msgraph.client.container.GraphService;

public final class EmailTest {

	@Test
	@Ignore
	public void testSendMailCompiles() {
		GraphService client = null;
		Email //
				.mailbox("sender@marathon.com") //
				.subject("Hi there") //
				.body("Just a quick test") //
				.to("dave@gmail.com") //
				.bcc("sarah@gmail.com", "andrew@gmail.com") //
				.attachment(new File("info.txt")) //
				.contentMimeType("text/plain") //
				.readTimeout(10, TimeUnit.MINUTES) //
				.retries(Retries.builder().maxRetries(3).build()) //
				.name("more-info.txt") //
				.attachment("some info for you") //
				.name("info2.txt") //
				.chunkSize(512 * 1024) //
				.send(client);
	}

}
