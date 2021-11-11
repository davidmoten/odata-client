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

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.Retries;
import com.github.davidmoten.odata.client.StreamUploaderChunked;
import com.github.davidmoten.odata.client.internal.Util;

import odata.msgraph.client.complex.AttachmentItem;
import odata.msgraph.client.complex.EmailAddress;
import odata.msgraph.client.complex.InternetMessageHeader;
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
        Preconditions.checkNotNull(mailbox);
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
        private final List<Header> headers = new ArrayList<>();
        
        private String draftFolder = "Drafts";
        private BodyType bodyType;
        private final List<Attachment> attachments = new ArrayList<>();

        Builder(String mailbox) {
            this.mailbox = mailbox;
            this.from = mailbox;
        }

        public Builder1 subject(String subject) {
            Preconditions.checkNotNull(subject);
            this.subject = subject;
            return new Builder1(this);
        }

    }

    public static final class Builder1 {
        private final Builder b;

        Builder1(Builder b) {
            this.b = b;
        }

        public Builder2 bodyType(BodyType bodyType) {
            Preconditions.checkNotNull(bodyType);
            b.bodyType = bodyType;
            return new Builder2(b);
        }
    }

    public static final class Builder2 {

        private final Builder b;

        Builder2(Builder b) {
            this.b = b;
        }

        public Builder4 body(String body) {
            Preconditions.checkNotNull(body);
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

        public Builder4 from(String emailAddress) {
            Preconditions.checkNotNull(emailAddress);
            b.from = emailAddress;
            return new Builder4(b);
        }

        public Builder4 to(String... emailAddresses) {
            Preconditions.checkNotNull(emailAddresses);
            return to(Arrays.asList(emailAddresses));
        }

        public Builder4 to(Iterable<String> emailAddresses) {
            Preconditions.checkNotNull(emailAddresses);
            for (String a : emailAddresses) {
                b.to.add(a);
            }
            return this;
        }

        public Builder4 cc(String... emailAddresses) {
            Preconditions.checkNotNull(emailAddresses);
            return cc(Arrays.asList(emailAddresses));
        }

        public Builder4 cc(Iterable<String> emailAddresses) {
            Preconditions.checkNotNull(emailAddresses);
            for (String a : emailAddresses) {
                b.cc.add(a);
            }
            return this;
        }

        public Builder4 bcc(String... emailAddresses) {
            Preconditions.checkNotNull(emailAddresses);
            return bcc(Arrays.asList(emailAddresses));
        }

        public Builder4 bcc(List<String> emailAddresses) {
            Preconditions.checkNotNull(emailAddresses);
            for (String a : emailAddresses) {
                b.bcc.add(a);
            }
            return this;
        }

        public Builder4 saveDraftToFolder(String draftFolder) {
            Preconditions.checkNotNull(draftFolder);
            b.draftFolder = draftFolder;
            return this;
        }
        
        public Builder4 header(String name, String value) {
            Preconditions.checkNotNull(name);
            Preconditions.checkNotNull(value);
            b.headers.add(new Header(name, value));
            return this;
        }
        
        public Builder4 attachments(List<Attachment> attachments) {
            Preconditions.checkNotNull(attachments);
            b.attachments.addAll(attachments);
            return this;
        }
        
        public Builder4 attachments(Attachment... attachments) {
            Preconditions.checkNotNull(attachments);
            b.attachments.addAll(Arrays.asList(attachments));
            return this;
        }

        public Builder6 attachment(String contentUtf8) {
            Preconditions.checkNotNull(contentUtf8);
            return new BuilderAttachment(this).contentTextUtf8(contentUtf8);
        }

        public Builder6 attachment(byte[] content) {
            Preconditions.checkNotNull(content);
            return new BuilderAttachment(this).bytes(content);
        }
        
        public Builder5 attachment(InputStream content) {
            Preconditions.checkNotNull(content);
            return new BuilderAttachment(this).inputStream(content);
        }
        
        public Builder6 attachment(File file) {
            Preconditions.checkNotNull(file);
            return new BuilderAttachment(this).file(file);
        }

        public void send(GraphService client) {
            Preconditions.checkNotNull(client);
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
            if (!b.headers.isEmpty()) {
                List<InternetMessageHeader> headers = b.headers //
                    .stream() //
                    .map(x -> InternetMessageHeader //
                            .builder() //
                            .name(x.name) //
                            .value(x.value) //
                            .build()) //
                    .collect(Collectors.toList());
                builder = builder.internetMessageHeaders(headers);
            }
            Message m = drafts.messages().post(builder.build());

            // upload attachments
            for (Attachment a : b.attachments) {
                // Upload attachment to the new mail
                // We use different methods depending on the size of the attachment
                // because will fail if doesn't match the right size window
                long length;
                if (a.file != null) {
                    length = a.file.length();
                } else {
                    length = a.length;
                }
                if (length < ATTACHMENT_SIZE_THRESHOLD) {
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
                            .size(length) //
                            .build();

                    StreamUploaderChunked uploader = client //
                            .users(b.mailbox) //
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
                        uploader.upload(a.inputStream, length, a.chunkSize, a.retries);
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
    
    public static final class Attachment {
        final long readTimeoutMs;
        final String name;
        final String contentMimeType;
        final File file;
        final InputStream inputStream;
        final long length;
        final int chunkSize;
        final Retries retries;
        
        Attachment(long readTimeoutMs, String name, String contentMimeType, File file,
                InputStream inputStream, long length, int chunkSize, Retries retries) {
            this.readTimeoutMs = readTimeoutMs;
            this.name = name;
            this.contentMimeType = contentMimeType;
            this.file = file;
            this.inputStream = inputStream;
            this.length = length;
            this.chunkSize = chunkSize;
            this.retries = retries;
        }
        
        public static AttachmentBuilderHasLength file(File file) {
            Preconditions.checkNotNull(file);
            return new AttachmentBuilder().file(file);
        }

        public static AttachmentBuilderInputStream inputStream(InputStream in) {
            Preconditions.checkNotNull(in);
            return new AttachmentBuilder().inputStream(in);
        }

        public static AttachmentBuilderHasLength bytes(byte[] bytes) {
            Preconditions.checkNotNull(bytes);
            return new AttachmentBuilder().bytes(bytes);            
        }

        public static AttachmentBuilderHasLength contentTextUtf8(String text) {
            Preconditions.checkNotNull(text);
            return new AttachmentBuilder().contentTextUtf8(text);
        }
    
    }
    
    private static final int DEFAULT_READ_TIMEOUT_MS = -1; // use default
    private static final Retries DEFAULT_RETRIES = Retries.NONE;
    private static final int DEFAULT_CHUNK_SIZE = 512 * 1024;
    private static final String DEFAULT_CONTENT_MIME_TYPE = "application/octet-stream";
    private static final String DEFAULT_ATTACHMENT_NAME = "attachment";
    
    public static final class AttachmentBuilder {
        private long readTimeoutMs = DEFAULT_READ_TIMEOUT_MS; 
        private String name = DEFAULT_ATTACHMENT_NAME;
        private String contentMimeType = DEFAULT_CONTENT_MIME_TYPE;
        private File file;
        private InputStream inputStream;
        private long length;
        private int chunkSize = DEFAULT_CHUNK_SIZE;
        private Retries retries = DEFAULT_RETRIES;
        
        AttachmentBuilder() {
            // prevent public instantiation
        }
        
        public AttachmentBuilderHasLength file(File file) {
            Preconditions.checkNotNull(file);
            this.file = file;
            this.name = file.getName();
            return new AttachmentBuilderHasLength(this);
        }

        public AttachmentBuilderInputStream inputStream(InputStream in) {
            Preconditions.checkNotNull(in);
            this.inputStream = in;
            return new AttachmentBuilderInputStream(this);
        }

        public AttachmentBuilderHasLength bytes(byte[] bytes) {
            Preconditions.checkNotNull(bytes);
            return inputStream(new ByteArrayInputStream(bytes)).length(bytes.length);
        }

        public AttachmentBuilderHasLength contentTextUtf8(String text) {
            Preconditions.checkNotNull(text);
            return bytes(text.getBytes(StandardCharsets.UTF_8)).contentMimeType("text/plain");
        }
    }
    
    public static final class AttachmentBuilderInputStream {

        private final AttachmentBuilder b;

        AttachmentBuilderInputStream(AttachmentBuilder b) {
            this.b = b;
        }

        public AttachmentBuilderHasLength length(int length) {
            Preconditions.checkArgument(length >= 0, "length must be >=0");
            b.length = length;
            return new AttachmentBuilderHasLength(b);
        }
    }
    
    public static final class AttachmentBuilderHasLength {

        private final AttachmentBuilder b;

        AttachmentBuilderHasLength(AttachmentBuilder b) {
            this.b = b;
        }
        
        public AttachmentBuilderHasLength contentMimeType(String mimeType) {
            Preconditions.checkNotNull(mimeType);
            b.contentMimeType = mimeType;
            return this;
        }

        public AttachmentBuilderHasLength readTimeout(long duration, TimeUnit unit) {
            Preconditions.checkArgument(duration > 0, "duration must be greater than 0");
            Preconditions.checkNotNull(unit);
            b.readTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public AttachmentBuilderHasLength chunkSize(int chunkSize) {
            Preconditions.checkArgument(chunkSize > 0, "chunkSize must be greater than 0");
            b.chunkSize = chunkSize;
            return this;
        }

        public AttachmentBuilderHasLength retries(Retries retries) {
            Preconditions.checkNotNull(retries);
            b.retries = retries;
            return this;
        }
        
        public AttachmentBuilderHasLength name(String name) {
            Preconditions.checkNotNull(name);
            b.name = name;
            return this;
        }
        
        public Attachment build() {
            return new Attachment(b.readTimeoutMs, b.name, b.contentMimeType, b.file, b.inputStream, b.length, b.chunkSize, b.retries);
        }
    }

    public static final class BuilderAttachment {

        private long readTimeoutMs = DEFAULT_READ_TIMEOUT_MS; // use default
        private String name = DEFAULT_ATTACHMENT_NAME;
        private final Builder4 sender;
        private String contentMimeType = DEFAULT_CONTENT_MIME_TYPE;
        private File file;
        private InputStream inputStream;
        private long length;
        private int chunkSize = DEFAULT_CHUNK_SIZE;
        private Retries retries = DEFAULT_RETRIES;

        BuilderAttachment(Builder4 sender) {
            this.sender = sender;
        }

        public Builder6 file(File file) {
            Preconditions.checkNotNull(file);
            this.file = file;
            this.name = file.getName();
            return new Builder6(this);
        }

        public Builder5 inputStream(InputStream in) {
            Preconditions.checkNotNull(in);
            this.inputStream = in;
            return new Builder5(this);
        }

        public Builder6 bytes(byte[] bytes) {
            Preconditions.checkNotNull(bytes);
            return inputStream(new ByteArrayInputStream(bytes)).length(bytes.length);
        }

        public Builder6 contentTextUtf8(String text) {
            Preconditions.checkNotNull(text);
            return bytes(text.getBytes(StandardCharsets.UTF_8)).contentMimeType("text/plain");
        }
        
        // this should not be public
        Attachment createAttachment() {
            return new Attachment(readTimeoutMs, name, contentMimeType, file, inputStream, length, chunkSize, retries);
        }

    }

    public static final class Builder5 {

        private final BuilderAttachment attachment;

        Builder5(BuilderAttachment attachment) {
            this.attachment = attachment;
        }

        public Builder6 length(long length) {
            Preconditions.checkArgument(length >=0, "length must be >= 0");
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
            Preconditions.checkNotNull(mimeType);
            attachment.contentMimeType = mimeType;
            return this;
        }

        public Builder6 readTimeout(long duration, TimeUnit unit) {
            Preconditions.checkArgument(duration > 0, "duration must be > 0");
            Preconditions.checkNotNull(unit);
            attachment.readTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public Builder6 chunkSize(int chunkSize) {
            Preconditions.checkArgument(chunkSize > 0, "chunkSize must be > 0");
            attachment.chunkSize = chunkSize;
            return this;
        }

        public Builder6 retries(Retries retries) {
            Preconditions.checkNotNull(retries);
            attachment.retries = retries;
            return this;
        }
        
        public Builder6 name(String name) {
            Preconditions.checkNotNull(name);
            attachment.name = name;
            return this;
        }

        public Builder6 attachment(File file) {
            Preconditions.checkNotNull(file);
            attachment.sender.b.attachments.add(attachment.createAttachment());
            return attachment.sender.attachment(file);
        }
        
        public Builder5 attachment(InputStream content) {
            Preconditions.checkNotNull(content);
            attachment.sender.b.attachments.add(attachment.createAttachment());
            return attachment.sender.attachment(content);
        }
        
        public Builder6 attachment(byte[] content) {
            Preconditions.checkNotNull(content);
            attachment.sender.b.attachments.add(attachment.createAttachment());
            return attachment.sender.attachment(content);
        }
        
        public Builder6 attachment(String content) {
            Preconditions.checkNotNull(content);
            attachment.sender.b.attachments.add(attachment.createAttachment());
            return attachment.sender.attachment(content);
        }

        public void send(GraphService client) {
            Preconditions.checkNotNull(client);
            attachment.sender.b.attachments.add(attachment.createAttachment());
            attachment.sender.send(client);
        }

    }
    
    private static final class Header {
        final String name; 
        final String value;

        private Header(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
