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
import java.util.Optional;
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

        public BuilderHasSubject subject(String subject) {
            Preconditions.checkNotNull(subject);
            this.subject = subject;
            return new BuilderHasSubject(this);
        }

    }

    public static final class BuilderHasSubject {
        private final Builder b;

        BuilderHasSubject(Builder b) {
            this.b = b;
        }

        public BuilderHasBodyType bodyType(BodyType bodyType) {
            Preconditions.checkNotNull(bodyType);
            b.bodyType = bodyType;
            return new BuilderHasBodyType(b);
        }
    }

    public static final class BuilderHasBodyType {

        private final Builder b;

        BuilderHasBodyType(Builder b) {
            this.b = b;
        }

        public BuilderFinal body(String body) {
            Preconditions.checkNotNull(body);
            b.body = body;
            return new BuilderFinal(b);
        }
    }

    public static final class BuilderFinal {

        private static final int ATTACHMENT_SIZE_THRESHOLD = 3000000;
        private final Builder b;

        BuilderFinal(Builder b) {
            this.b = b;
        }

        public BuilderFinal from(String emailAddress) {
            Preconditions.checkNotNull(emailAddress);
            b.from = emailAddress;
            return new BuilderFinal(b);
        }

        public BuilderFinal to(String... emailAddresses) {
            Preconditions.checkNotNull(emailAddresses);
            return to(Arrays.asList(emailAddresses));
        }

        public BuilderFinal to(Iterable<String> emailAddresses) {
            Preconditions.checkNotNull(emailAddresses);
            for (String a : emailAddresses) {
                b.to.add(a);
            }
            return this;
        }

        public BuilderFinal cc(String... emailAddresses) {
            Preconditions.checkNotNull(emailAddresses);
            return cc(Arrays.asList(emailAddresses));
        }

        public BuilderFinal cc(Iterable<String> emailAddresses) {
            Preconditions.checkNotNull(emailAddresses);
            for (String a : emailAddresses) {
                b.cc.add(a);
            }
            return this;
        }

        public BuilderFinal bcc(String... emailAddresses) {
            Preconditions.checkNotNull(emailAddresses);
            return bcc(Arrays.asList(emailAddresses));
        }

        public BuilderFinal bcc(List<String> emailAddresses) {
            Preconditions.checkNotNull(emailAddresses);
            for (String a : emailAddresses) {
                b.bcc.add(a);
            }
            return this;
        }

        public BuilderFinal saveDraftToFolder(String draftFolder) {
            Preconditions.checkNotNull(draftFolder);
            b.draftFolder = draftFolder;
            return this;
        }
        
        public BuilderFinal header(String name, Optional<String> value) {
            Preconditions.checkNotNull(name);
            Preconditions.checkNotNull(value);
            if (value.isPresent()) {
                return header(name, value.get());
            } else {
                return this;
            }
        }
        
        public BuilderFinal header(String name, String value) {
            Preconditions.checkNotNull(name);
            Preconditions.checkNotNull(value);
            b.headers.add(new Header(name, value));
            return this;
        }
        
        public BuilderFinal attachments(List<Attachment> attachments) {
            Preconditions.checkNotNull(attachments);
            b.attachments.addAll(attachments);
            return this;
        }
        
        public BuilderFinal attachments(Attachment... attachments) {
            Preconditions.checkNotNull(attachments);
            b.attachments.addAll(Arrays.asList(attachments));
            return this;
        }

        public BuilderAttachmentHasLength attachment(String contentUtf8) {
            Preconditions.checkNotNull(contentUtf8);
            return new BuilderAttachment(this).contentTextUtf8(contentUtf8);
        }

        public BuilderAttachmentHasLength attachment(byte[] content) {
            Preconditions.checkNotNull(content);
            return new BuilderAttachment(this).bytes(content);
        }
        
        public BuilderAttachmentRequiresLength attachment(InputStream content) {
            Preconditions.checkNotNull(content);
            return new BuilderAttachment(this).inputStream(content);
        }
        
        public BuilderAttachmentHasLength attachment(File file) {
            Preconditions.checkNotNull(file);
            return new BuilderAttachment(this).file(file);
        }
        
        /**
         * Creates a message in the drafts folder, ready to be sent. Provides the delivery guarantee usefulness of two-phase send. 
         * Use the returned DraftMessage.send() method to send.
         * @param client
         * @return the created draft message
         */
        public DraftMessage create(GraphService client) {
            Message message = createMessage(client);
            return new DraftMessage(client, message, b); 
        }

        /**
         * Creates a message in the drafts folder and sends it. If you need two-phase
         * create and send use {@link BuilderFinal#create(GraphService)}.
         * 
         * @param client the client object
         */
        public void send(GraphService client) {
            Preconditions.checkNotNull(client);
            Message m = createMessage(client);
            send(client, m, b);
        }
        
        static void send(GraphService client, Message m, Builder b) {
            client //
                    .users(b.mailbox) //
                    .messages(m.getId().get()) //
                    .send() //
                    .call();
        }

        private Message createMessage(GraphService client) {
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
            Message m = drafts.messages().post(builder.build()).get();

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
            return m;
        }
    }
    
    public static final class DraftMessage {
        private final GraphService client;
        private final Message message;
        private final Builder b;

        DraftMessage(GraphService client, Message message, Builder b) {
            this.client = client;
            this.message = message;
            this.b = b;
        }
        
        public String id() {
            return message.getId().get();
        }
        
        public Message message() {
            return message;
        }
        
        public void send() {
            BuilderFinal.send(client, message, b);
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
        private final BuilderFinal sender;
        private String contentMimeType = DEFAULT_CONTENT_MIME_TYPE;
        private File file;
        private InputStream inputStream;
        private long length;
        private int chunkSize = DEFAULT_CHUNK_SIZE;
        private Retries retries = DEFAULT_RETRIES;

        BuilderAttachment(BuilderFinal sender) {
            this.sender = sender;
        }

        public BuilderAttachmentHasLength file(File file) {
            Preconditions.checkNotNull(file);
            this.file = file;
            this.name = file.getName();
            return new BuilderAttachmentHasLength(this);
        }

        public BuilderAttachmentRequiresLength inputStream(InputStream in) {
            Preconditions.checkNotNull(in);
            this.inputStream = in;
            return new BuilderAttachmentRequiresLength(this);
        }

        public BuilderAttachmentHasLength bytes(byte[] bytes) {
            Preconditions.checkNotNull(bytes);
            return inputStream(new ByteArrayInputStream(bytes)).length(bytes.length);
        }

        public BuilderAttachmentHasLength contentTextUtf8(String text) {
            Preconditions.checkNotNull(text);
            return bytes(text.getBytes(StandardCharsets.UTF_8)).contentMimeType("text/plain");
        }
        
        // this should not be public
        Attachment createAttachment() {
            return new Attachment(readTimeoutMs, name, contentMimeType, file, inputStream, length, chunkSize, retries);
        }

    }

    public static final class BuilderAttachmentRequiresLength {

        private final BuilderAttachment attachment;

        BuilderAttachmentRequiresLength(BuilderAttachment attachment) {
            this.attachment = attachment;
        }

        public BuilderAttachmentHasLength length(long length) {
            Preconditions.checkArgument(length >=0, "length must be >= 0");
            attachment.length = length;
            return new BuilderAttachmentHasLength(attachment);
        }
    }

    public static final class BuilderAttachmentHasLength {

        private final BuilderAttachment attachment;

        public BuilderAttachmentHasLength(BuilderAttachment attachment) {
            this.attachment = attachment;
        }

        public BuilderAttachmentHasLength contentMimeType(String mimeType) {
            Preconditions.checkNotNull(mimeType);
            attachment.contentMimeType = mimeType;
            return this;
        }

        public BuilderAttachmentHasLength readTimeout(long duration, TimeUnit unit) {
            Preconditions.checkArgument(duration > 0, "duration must be > 0");
            Preconditions.checkNotNull(unit);
            attachment.readTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public BuilderAttachmentHasLength chunkSize(int chunkSize) {
            Preconditions.checkArgument(chunkSize > 0, "chunkSize must be > 0");
            attachment.chunkSize = chunkSize;
            return this;
        }

        public BuilderAttachmentHasLength retries(Retries retries) {
            Preconditions.checkNotNull(retries);
            attachment.retries = retries;
            return this;
        }
        
        public BuilderAttachmentHasLength name(String name) {
            Preconditions.checkNotNull(name);
            attachment.name = name;
            return this;
        }

        public BuilderAttachmentHasLength attachment(File file) {
            Preconditions.checkNotNull(file);
            attachment.sender.b.attachments.add(attachment.createAttachment());
            return attachment.sender.attachment(file);
        }
        
        public BuilderAttachmentRequiresLength attachment(InputStream content) {
            Preconditions.checkNotNull(content);
            attachment.sender.b.attachments.add(attachment.createAttachment());
            return attachment.sender.attachment(content);
        }
        
        public BuilderAttachmentHasLength attachment(byte[] content) {
            Preconditions.checkNotNull(content);
            attachment.sender.b.attachments.add(attachment.createAttachment());
            return attachment.sender.attachment(content);
        }
        
        public BuilderAttachmentHasLength attachment(String content) {
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
