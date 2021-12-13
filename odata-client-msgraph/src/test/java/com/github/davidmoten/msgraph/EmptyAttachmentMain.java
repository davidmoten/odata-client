package com.github.davidmoten.msgraph;

import java.util.concurrent.TimeUnit;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.Attachment;
import odata.msgraph.client.entity.FileAttachment;

public class EmptyAttachmentMain {

	public static void main(String[] args) {
		String tenantName = System.getProperty("tenant");
		String mailbox = System.getProperty("mailbox");
		String clientId = System.getProperty("clientId");
		String clientSecret = System.getProperty("clientSecret");
		GraphService client = MsGraph //
				.tenantName(tenantName) //
				.clientId(clientId) //
				.clientSecret(clientSecret) //
				.connectTimeout(60, TimeUnit.SECONDS) //
				.readTimeout(60, TimeUnit.SECONDS) //
				.refreshBeforeExpiry(5, TimeUnit.MINUTES) //

				.build();
		Attachment att = client.users(mailbox) //
				.messages(
						"AQMkADQ3YjdiNWUxLTBmYWQtNDMwYy04Yzc0LTI0MDdmOWQ4NDFjNgBGAAAD4Rwe0e6XOE6Ck412HUUUTwcAUb5I0z9LnUy3cpFj0m9MUgAAAgEMAAAA3NEVJKXfYEuEjYE7msyHXwAEb4XEBwAAAA==") //
				.attachments().metadataFull() //
				.stream() //
				.findFirst() //
				.get();
		System.out.println(att.toString().replace(",", ",\n"));
		FileAttachment f = (FileAttachment) att;

		// if request header not set to identity then Graph API returns 0 bytes with
		// gzip content-type which is invalid and EOFException is thrown
		String content = f.getStream().get() //
				.requestHeader("Accept-Encoding", "identity") //
				.getStringUtf8();
		System.out.println("content=" + content);
	}

}
