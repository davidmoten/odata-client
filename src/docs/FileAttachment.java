package microsoft.graph.generated.entity;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.davidmoten.odata.client.ContextPath;
import com.github.davidmoten.odata.client.ODataEntity;
import com.github.davidmoten.odata.client.Patchable;
import com.github.davidmoten.odata.client.RequestOptions;
import com.github.davidmoten.odata.client.Util;
import com.github.davidmoten.odata.client.internal.ChangedFields;
import com.github.davidmoten.odata.client.internal.RequestHelper;
import com.github.davidmoten.odata.client.internal.UnmappedFields;
import java.time.OffsetDateTime;
import java.util.Optional;
import microsoft.graph.generated.entity.Attachment;
import microsoft.graph.generated.schema.SchemaInfo;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({
    "@odata.type", 
    "contentId", 
    "contentLocation", 
    "contentBytes"})
public class FileAttachment extends Attachment implements ODataEntity {

    @JsonProperty("contentId")
    protected final String contentId;

    @JsonProperty("contentLocation")
    protected final String contentLocation;

    @JsonProperty("contentBytes")
    protected final byte[] contentBytes;

    @JsonCreator
    protected FileAttachment(
            @JacksonInject ContextPath contextPath, 
            @JacksonInject ChangedFields changedFields, 
            @JacksonInject UnmappedFields unmappedFields, 
            @JsonProperty("@odata.type") String odataType,
            @JsonProperty("id") String id,
            @JsonProperty("lastModifiedDateTime") OffsetDateTime lastModifiedDateTime,
            @JsonProperty("name") String name,
            @JsonProperty("contentType") String contentType,
            @JsonProperty("size") Integer size,
            @JsonProperty("isInline") Boolean isInline,
            @JsonProperty("contentId") String contentId,
            @JsonProperty("contentLocation") String contentLocation,
            @JsonProperty("contentBytes") byte[] contentBytes) {
        super(contextPath, changedFields, unmappedFields, odataType, id, lastModifiedDateTime, name, contentType, size, isInline);
        this.contentId = contentId;
        this.contentLocation = contentLocation;
        this.contentBytes = contentBytes;
    }

    public static Builder builderFileAttachment() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private OffsetDateTime lastModifiedDateTime;
        private String name;
        private String contentType;
        private Integer size;
        private Boolean isInline;
        private String contentId;
        private String contentLocation;
        private byte[] contentBytes;
        private ChangedFields changedFields = ChangedFields.EMPTY;

        Builder() {
            // prevent instantiation
        }

        public Builder id(String id) {
            this.id = id;
            this.changedFields = changedFields.add("id");
            return this;
        }

        public Builder lastModifiedDateTime(OffsetDateTime lastModifiedDateTime) {
            this.lastModifiedDateTime = lastModifiedDateTime;
            this.changedFields = changedFields.add("lastModifiedDateTime");
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            this.changedFields = changedFields.add("name");
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            this.changedFields = changedFields.add("contentType");
            return this;
        }

        public Builder size(Integer size) {
            this.size = size;
            this.changedFields = changedFields.add("size");
            return this;
        }

        public Builder isInline(Boolean isInline) {
            this.isInline = isInline;
            this.changedFields = changedFields.add("isInline");
            return this;
        }

        public Builder contentId(String contentId) {
            this.contentId = contentId;
            this.changedFields = changedFields.add("contentId");
            return this;
        }

        public Builder contentLocation(String contentLocation) {
            this.contentLocation = contentLocation;
            this.changedFields = changedFields.add("contentLocation");
            return this;
        }

        public Builder contentBytes(byte[] contentBytes) {
            this.contentBytes = contentBytes;
            this.changedFields = changedFields.add("contentBytes");
            return this;
        }

        public FileAttachment build() {
            return new FileAttachment(null, changedFields, UnmappedFields.EMPTY, "microsoft.graph.fileAttachment", id, lastModifiedDateTime, name, contentType, size, isInline, contentId, contentLocation, contentBytes);
        }
    }

    public ChangedFields getChangedFields() {
        return changedFields;
    }

    public Optional<String> getContentId() {
        return Optional.ofNullable(contentId);
    }

    public FileAttachment.Patched withContentId(String contentId) {
        return new FileAttachment.Patched(contextPath, changedFields.add("contentId"), unmappedFields, Util.nvl(odataType, "microsoft.graph.fileAttachment"), id, lastModifiedDateTime, name, contentType, size, isInline, contentId, contentLocation, contentBytes);
    }

    public Optional<String> getContentLocation() {
        return Optional.ofNullable(contentLocation);
    }

    public FileAttachment.Patched withContentLocation(String contentLocation) {
        return new FileAttachment.Patched(contextPath, changedFields.add("contentLocation"), unmappedFields, Util.nvl(odataType, "microsoft.graph.fileAttachment"), id, lastModifiedDateTime, name, contentType, size, isInline, contentId, contentLocation, contentBytes);
    }

    public Optional<byte[]> getContentBytes() {
        return Optional.ofNullable(contentBytes);
    }

    public FileAttachment.Patched withContentBytes(byte[] contentBytes) {
        return new FileAttachment.Patched(contextPath, changedFields.add("contentBytes"), unmappedFields, Util.nvl(odataType, "microsoft.graph.fileAttachment"), id, lastModifiedDateTime, name, contentType, size, isInline, contentId, contentLocation, contentBytes);
    }

    @JsonAnySetter
    private void setUnmappedField(String name, Object value) {
        if (unmappedFields == null) {
            unmappedFields = new UnmappedFields();
        }
        unmappedFields.put(name, value);
    }

    @Override
    public UnmappedFields getUnmappedFields() {
        return unmappedFields == null ? UnmappedFields.EMPTY : unmappedFields;
    }

    public static final class Patched extends FileAttachment implements Patchable<FileAttachment> {

        @JsonCreator
        protected Patched(
                @JacksonInject ContextPath contextPath, 
                @JacksonInject ChangedFields changedFields, 
                @JacksonInject UnmappedFields unmappedFields, 
                @JsonProperty("@odata.type") String odataType,
                @JsonProperty("id") String id,
                @JsonProperty("lastModifiedDateTime") OffsetDateTime lastModifiedDateTime,
                @JsonProperty("name") String name,
                @JsonProperty("contentType") String contentType,
                @JsonProperty("size") Integer size,
                @JsonProperty("isInline") Boolean isInline,
                @JsonProperty("contentId") String contentId,
                @JsonProperty("contentLocation") String contentLocation,
                @JsonProperty("contentBytes") byte[] contentBytes) {
            super(contextPath, changedFields, unmappedFields, odataType, id, lastModifiedDateTime, name, contentType, size, isInline, contentId, contentLocation, contentBytes);
        }

        @Override
        public FileAttachment patch() {
            RequestHelper.patch(this, contextPath, RequestOptions.EMPTY,  SchemaInfo.INSTANCE);
            // pass null for changedFields to reset it
            return new FileAttachment(contextPath, null, unmappedFields, odataType, id, lastModifiedDateTime, name, contentType, size, isInline, contentId, contentLocation, contentBytes);
        }

        @Override
        public FileAttachment put() {
            RequestHelper.put(this, contextPath, RequestOptions.EMPTY,  SchemaInfo.INSTANCE);
            // pass null for changedFields to reset it
            return new FileAttachment(contextPath, null, unmappedFields, odataType, id, lastModifiedDateTime, name, contentType, size, isInline, contentId, contentLocation, contentBytes);
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("FileAttachment[");
        b.append("id=");
        b.append(this.id);
        b.append(", ");
        b.append("lastModifiedDateTime=");
        b.append(this.lastModifiedDateTime);
        b.append(", ");
        b.append("name=");
        b.append(this.name);
        b.append(", ");
        b.append("contentType=");
        b.append(this.contentType);
        b.append(", ");
        b.append("size=");
        b.append(this.size);
        b.append(", ");
        b.append("isInline=");
        b.append(this.isInline);
        b.append(", ");
        b.append("contentId=");
        b.append(this.contentId);
        b.append(", ");
        b.append("contentLocation=");
        b.append(this.contentLocation);
        b.append(", ");
        b.append("contentBytes=");
        b.append(this.contentBytes);
        b.append("]");
        b.append(",unmappedFields=");
        b.append(unmappedFields);
        b.append(",odataType=");
        b.append(odataType);
        return b.toString();
    }
}
