package com.github.davidmoten.msgraph;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.davidmoten.odata.client.Retries;
import com.github.davidmoten.odata.client.StreamUploaderChunked;
import com.github.davidmoten.odata.client.internal.Util;

import odata.msgraph.client.complex.AttachmentItem;
import odata.msgraph.client.complex.EmailAddress;
import odata.msgraph.client.complex.ItemBody;
import odata.msgraph.client.complex.Recipient;
import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.FileAttachment;
import odata.msgraph.client.entity.Message;
import odata.msgraph.client.entity.request.MailFolderRequest;
import odata.msgraph.client.enums.AttachmentType;
import odata.msgraph.client.enums.BodyType;

public final class Email {

	public static Builder mailbox(String mailbox) {
		return new Builder(mailbox);
	}

	public static final class Builder {

		private final String mailbox;
		private String subject;
		private String body;
		private String from;
		private final List<String> to = new ArrayList<>();
		private final List<String> cc = new ArrayList<>();
		private final List<String> bcc = new ArrayList<>();
		private String draftFolder = "Drafts";
		private BodyType bodyType;
		private final List<BuilderAttachment> attachments = new ArrayList<>();

		Builder(String mailbox) {
			this.mailbox = mailbox;
			this.from = mailbox;
		}

		Builder2 subject(String subject) {
			this.subject = subject;
			return new Builder2(this);
		}

	}

	public static final class Builder11 {
		private final Builder b;

		Builder11(Builder b) {
			this.b = b;
		}

		public Builder2 bodyType(BodyType bodyType) {
			b.bodyType = bodyType;
			return new Builder2(b);
		}
	}

	public static final class Builder2 {

		private final Builder b;

		Builder2(Builder b) {
			this.b = b;
		}

		Builder4 body(String body) {
			b.body = body;
			return new Builder4(b);
		}
	}

	public static final class Builder4 {

		private static final int ATTACHMENT_SIZE_THRESHOLD = 3000000;
		private final Builder b;

		Builder4(Builder b) {
			this.b = b;
		}

		Builder4 from(String emailAddress) {
			b.from = emailAddress;
			return new Builder4(b);
		}

		Builder4 to(String... emailAddresses) {
			return to(Arrays.asList(emailAddresses));
		}

		Builder4 to(Iterable<String> emailAddresses) {
			for (String a : emailAddresses) {
				b.to.add(a);
			}
			return this;
		}

		Builder4 cc(String... emailAddresses) {
			return cc(Arrays.asList(emailAddresses));
		}

		Builder4 cc(Iterable<String> emailAddresses) {
			for (String a : emailAddresses) {
				b.cc.add(a);
			}
			return this;
		}

		Builder4 bcc(String... emailAddresses) {
			return bcc(Arrays.asList(emailAddresses));
		}

		Builder4 bcc(List<String> emailAddresses) {
			for (String a : emailAddresses) {
				b.bcc.add(a);
			}
			return this;
		}

		Builder4 saveDraftToFolder(String draftFolder) {
			b.draftFolder = draftFolder;
			return this;
		}

		Builder6 attachment(String contentUtf8) {
			return new BuilderAttachment(this).contentTextUtf8(contentUtf8);
		}

		Builder6 attachment(byte[] content) {
			return new BuilderAttachment(this).bytes(content);
		}
		
		Builder5 attachment(InputStream content) {
			return new BuilderAttachment(this).inputStream(content);
		}
		
		Builder6 attachment(File file) {
			return new BuilderAttachment(this).file(file);
		}

		void send(GraphService client) {
			MailFolderRequest drafts = client //
					.users(b.mailbox) //
					.mailFolders(b.draftFolder);
			odata.msgraph.client.entity.Message.Builder builder = Message //
					.builderMessage() //
					.subject(b.subject) //
					.body(ItemBody.builder() //
							.content(b.body) //
							.contentType(b.bodyType) //
							.build()) //
					.from(Recipient.builder() //
							.emailAddress(EmailAddress //
									.builder() //
									.address(b.from) //
									.build()) //
							.build());
			if (!b.to.isEmpty()) {
				builder = builder.toRecipients(recipients(b.to));
			}
			if (!b.cc.isEmpty()) {
				builder = builder.ccRecipients(recipients(b.cc));
			}
			if (!b.bcc.isEmpty()) {
				builder = builder.ccRecipients(recipients(b.bcc));
			}
			Message m = drafts.messages().post(builder.build());

			// upload attachments
			for (BuilderAttachment a : b.attachments) {
				// Upload attachment to the new mail
				// We use different methods depending on the size of the attachment
				// because will fail if doesn't match the right size window
				if (a.file != null) {
					a.length = a.file.length();
				}
				if (a.length < ATTACHMENT_SIZE_THRESHOLD) {
					final byte[] contentBytes;
					if (a.file != null) {
						try {
							contentBytes = Files.readAllBytes(a.file.toPath());
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					} else {
						contentBytes = Util.read(a.inputStream);
					}
					client.users(b.mailbox) //
							.messages(m.getId().get()) //
							.attachments() //
							.post(FileAttachment.builderFileAttachment() //
									.name(a.name) //
									.contentBytes(contentBytes) //
									.contentType(a.contentMimeType) //
									.build());
				} else {
					AttachmentItem ai = AttachmentItem //
							.builder() //
							.attachmentType(AttachmentType.FILE) //
							.contentType(a.contentMimeType) //
							.name(a.name) //
							.size(a.length) //
							.build();

					StreamUploaderChunked uploader = client //
							.users(a.sender.b.mailbox) //
							.messages(m.getId().get()) //
							.attachments() //
							.createUploadSession(ai) //
							.get() //
							.putChunked();
					if (a.readTimeoutMs == -1) {
						uploader = uploader.readTimeout(a.readTimeoutMs, TimeUnit.MILLISECONDS);
					}
					if (a.file != null) {
						uploader.upload(a.file, a.chunkSize, a.retries);
					} else {
						uploader.upload(a.inputStream, a.length, a.chunkSize, a.retries);
					}
				}
			}
			client //
					.users(b.mailbox) //
					.messages(m.getId().get()) //
					.send() //
					.call();
		}
	}

	private static List<Recipient> recipients(List<String> emailAddresses) {
		return emailAddresses.stream().map(x -> recipient(x)) //
				.collect(Collectors.toList());
	}

	private static Recipient recipient(String emailAddress) {
		return Recipient.builder() //
				.emailAddress(EmailAddress.builder() //
						.address(emailAddress) //
						.build()) //
				.build();
	}

	public static final class BuilderAttachment {

		private long readTimeoutMs = -1; // use default
		private String name = "attachment";
		private final Builder4 sender;
		private String contentMimeType = "application/octet-stream";
		private File file;
		private InputStream inputStream;
		private long length;
		private int chunkSize = 512 * 1024;
		private Retries retries = Retries.NONE;

		public BuilderAttachment(Builder4 sender) {
			this.sender = sender;
		}

		Builder6 file(File file) {
			this.file = file;
			this.name = file.getName();
			return new Builder6(this);
		}

		Builder5 inputStream(InputStream in) {
			this.inputStream = in;
			return new Builder5(this);
		}

		Builder6 bytes(byte[] bytes) {
			return inputStream(new ByteArrayInputStream(bytes)).length(bytes.length);
		}

		Builder6 contentTextUtf8(String text) {
			return bytes(text.getBytes(StandardCharsets.UTF_8)).contentMimeType("text/plain");
		}

	}

	public static final class Builder5 {

		private final BuilderAttachment attachment;

		Builder5(BuilderAttachment attachment) {
			this.attachment = attachment;
		}

		public Builder6 length(long length) {
			attachment.length = length;
			return new Builder6(attachment);
		}
	}

	public static final class Builder6 {

		private final BuilderAttachment attachment;

		public Builder6(BuilderAttachment attachment) {
			this.attachment = attachment;
		}

		public Builder6 contentMimeType(String mimeType) {
			attachment.contentMimeType = mimeType;
			return this;
		}

		public Builder6 readTimeout(long duration, TimeUnit unit) {
			attachment.readTimeoutMs = unit.toMillis(duration);
			return this;
		}

		public Builder6 chunkSize(int chunkSize) {
			attachment.chunkSize = chunkSize;
			return this;
		}

		public Builder6 retries(Retries retries) {
			attachment.retries = retries;
			return this;
		}
		
		public Builder6 name(String name) {
			attachment.name = name;
			return this;
		}

		public Builder6 attachment(File file) {
			attachment.sender.b.attachments.add(attachment);
			return attachment.sender.attachment(file);
		}
		
		public Builder5 attachment(InputStream content) {
			attachment.sender.b.attachments.add(attachment);
			return attachment.sender.attachment(content);
		}
		
		public Builder6 attachment(byte[] content) {
			attachment.sender.b.attachments.add(attachment);
			return attachment.sender.attachment(content);
		}
		
		public Builder6 attachment(String content) {
			attachment.sender.b.attachments.add(attachment);
			return attachment.sender.attachment(content);
		}

		public void send(GraphService client) {
			attachment.sender.b.attachments.add(attachment);
			attachment.sender.send(client);
		}

	}
}
