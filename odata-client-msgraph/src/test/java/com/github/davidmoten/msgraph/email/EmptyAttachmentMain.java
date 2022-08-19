package com.github.davidmoten.msgraph.email;

import java.util.concurrent.TimeUnit;

import com.github.davidmoten.msgraph.MsGraph;

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
//		client.users(mailbox) //
//		.mailFolders("inbox") //
//		.messages() //
//		.stream() //
//		.limit(0) //
//		.forEach(x -> System.out.println(x.getSubject().orElse("unknown subject") + "\n" + x.getId().orElse("")));
		
		Attachment att = client.users(mailbox) //
				.messages(
						"AQMkADQ3YjdiNWUxLTBmYWQtNDMwYy04Yzc0LTI0MDdmOWQ4NDFjNgBGAAAD4Rwe0e6XOE6Ck412HUUUTwcAUb5I0z9LnUy3cpFj0m9MUgAAAgEMAAAA3NEVJKXfYEuEjYE7msyHXwAFFI2O2wAAAA==") //
				.attachments().metadataFull() //
				.stream() //
				.findFirst() //
				.get();
		
		System.out.println(att.toString().replace(",", ",\n"));
		FileAttachment f = (FileAttachment) att;

		// if request header not set to identity then Graph API returns 0 bytes with
		// gzip content-type which is invalid and EOFException is thrown
		String content = f.getStream().get() //
//				.requestHeader("Accept-Encoding", "identity") //
				.getStringUtf8();
		System.out.println("content=" + content);
		System.out.println("if this message seen then zero byte attachment was downloaded successfully (and Microsoft have fixed their bug)");
	}

}
