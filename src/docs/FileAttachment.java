package microsoft.graph.generated.entity;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.NameValue;
import com.github.davidmoten.odata.client.ODataEntityType;
import com.github.davidmoten.odata.client.RequestOptions;
import com.github.davidmoten.odata.client.SelectBuilder;
import com.github.davidmoten.odata.client.StreamProvider;
import com.github.davidmoten.odata.client.Util;
import com.github.davidmoten.odata.client.annotation.Property;
import com.github.davidmoten.odata.client.internal.ChangedFields;
import com.github.davidmoten.odata.client.internal.RequestHelper;
import com.github.davidmoten.odata.client.internal.UnmappedFields;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonPropertyOrder({
    "@odata.type", 
    "contentId", 
    "contentLocation", 
    "contentBytes"})
@JsonInclude(Include.NON_NULL)
public class FileAttachment extends Attachment implements ODataEntityType {

    @Override
    public String odataTypeName() {
        return "microsoft.graph.fileAttachment";
    }

    @JsonProperty("contentId")
    protected String contentId;

    @JsonProperty("contentLocation")
    protected String contentLocation;

    @JsonProperty("contentBytes")
    protected byte[] contentBytes;

    protected FileAttachment() {
        super();
    }

    /**
     * Returns a builder which is used to create a new
     * instance of this class (given that this class is immutable).
     *
     * @return a new Builder for this class
     */
    // Suffix used on builder factory method to differentiate the method
    // from static builder methods on superclasses
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
        private ChangedFields changedFields = new ChangedFields();

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
            FileAttachment _x = new FileAttachment();
            _x.contextPath = null;
            _x.changedFields = changedFields;
            _x.unmappedFields = new UnmappedFields();
            _x.odataType = "microsoft.graph.fileAttachment";
            _x.id = id;
            _x.lastModifiedDateTime = lastModifiedDateTime;
            _x.name = name;
            _x.contentType = contentType;
            _x.size = size;
            _x.isInline = isInline;
            _x.contentId = contentId;
            _x.contentLocation = contentLocation;
            _x.contentBytes = contentBytes;
            return _x;
        }
    }

    public static final class Select<T> implements SelectBuilder<FileAttachment> {
        private final T caller;
        protected final List<String> list = new ArrayList<String>();

        public Select(T caller) {
            this.caller = caller;
        }

        public Select<T> contentId() {
            list.add("contentId");
            return this;
        }

        public Select<T> contentLocation() {
            list.add("contentLocation");
            return this;
        }

        public Select<T> contentBytes() {
            list.add("contentBytes");
            return this;
        }

        public T build() {
             return caller;
        }
    }

    @Override
    @JsonIgnore
    public ChangedFields getChangedFields() {
        return changedFields;
    }

    @Override
    public void postInject(boolean addKeysToContextPath) {
        if (addKeysToContextPath && id != null) {
            contextPath = contextPath.clearQueries().addKeys(new NameValue(id.toString()));
        }
    }

    @Property(name="contentId")
    @JsonIgnore
    public Optional<String> getContentId() {
        return Optional.ofNullable(contentId);
    }

    public FileAttachment withContentId(String contentId) {
        FileAttachment _x = _copy();
        _x.changedFields = changedFields.add("contentId");
        _x.odataType = Util.nvl(odataType, "microsoft.graph.fileAttachment");
        _x.contentId = contentId;
        return _x;
    }

    @Property(name="contentLocation")
    @JsonIgnore
    public Optional<String> getContentLocation() {
        return Optional.ofNullable(contentLocation);
    }

    public FileAttachment withContentLocation(String contentLocation) {
        FileAttachment _x = _copy();
        _x.changedFields = changedFields.add("contentLocation");
        _x.odataType = Util.nvl(odataType, "microsoft.graph.fileAttachment");
        _x.contentLocation = contentLocation;
        return _x;
    }

    @Property(name="contentBytes")
    @JsonIgnore
    public Optional<byte[]> getContentBytes() {
        return Optional.ofNullable(contentBytes);
    }

    public FileAttachment withContentBytes(byte[] contentBytes) {
        FileAttachment _x = _copy();
        _x.changedFields = changedFields.add("contentBytes");
        _x.odataType = Util.nvl(odataType, "microsoft.graph.fileAttachment");
        _x.contentBytes = contentBytes;
        return _x;
    }

    @JsonAnySetter
    private void setUnmappedField(String name, Object value) {
        if (unmappedFields == null) {
            unmappedFields = new UnmappedFields();
        }
        unmappedFields.put(name, value);
    }

    @Override
    @JsonIgnore
    public UnmappedFields getUnmappedFields() {
        return unmappedFields == null ? new UnmappedFields() : unmappedFields;
    }

    /**
     * If suitable metadata found a StreamProvider is returned otherwise returns
     * {@code Optional.empty()}. Normally for a stream to be available this entity
     * needs to have been hydrated with full metadata. Consider calling the builder
     * method {@code .metadataFull()} when getting this instance (either directly or
     * as part of a collection).
     *
     * @return StreamProvider if suitable metadata found otherwise returns
     *         {@code Optional.empty()}
     */
    @JsonIgnore
    public Optional<StreamProvider> getStream() {
        return RequestHelper.createStream(contextPath, this);
    }

    /**
     * Submits only changed fields for update and returns an 
     * immutable copy of {@code this} with changed fields reset.
     *
     * @return a copy of {@code this} with changed fields reset
     * @throws ClientException if HTTP response is not as expected
     */
    public FileAttachment patch() {
        RequestHelper.patch(this, contextPath, RequestOptions.EMPTY);
        FileAttachment _x = _copy();
        _x.changedFields = null;
        return _x;
    }

    /**
     * Submits all fields for update and returns an immutable copy of {@code this}
     * with changed fields reset (they were ignored anyway).
     *
     * @return a copy of {@code this} with changed fields reset
     * @throws ClientException if HTTP response is not as expected
     */
    public FileAttachment put() {
        RequestHelper.put(this, contextPath, RequestOptions.EMPTY);
        FileAttachment _x = _copy();
        _x.changedFields = null;
        return _x;
    }

    private FileAttachment _copy() {
        FileAttachment _x = new FileAttachment();
        _x.contextPath = contextPath;
        _x.changedFields = changedFields;
        _x.unmappedFields = unmappedFields;
        _x.odataType = odataType;
        _x.id = id;
        _x.lastModifiedDateTime = lastModifiedDateTime;
        _x.name = name;
        _x.contentType = contentType;
        _x.size = size;
        _x.isInline = isInline;
        _x.contentId = contentId;
        _x.contentLocation = contentLocation;
        _x.contentBytes = contentBytes;
        return _x;
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
