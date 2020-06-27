package com.github.davidmoten.odata.client;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.github.davidmoten.odata.client.internal.ChangedFields;

public class SerializerTest {

    @Test
    public void testDeserializeODataValue() {
        OffsetDateTime t = OffsetDateTime.parse("2007-12-03T10:15:30+01:00");
        String text = Util
                .utf8(SerializerTest.class.getResourceAsStream("/odata-value-offsetdatetime.json"));
        ContextPath cp = new ContextPath(null, null);
        @SuppressWarnings("unchecked")
        ODataValue<OffsetDateTime> o = Serializer.INSTANCE.deserializeWithParametricType( //
                text, //
                ODataValue.class, //
                OffsetDateTime.class, //
                cp, //
                false);
        assertEquals(t.toInstant().toEpochMilli(), o.value().toInstant().toEpochMilli());
    }

    @Test
    public void testSerializeChangesOnly() {
        Person person = new Person("user",null,"last").setFirstName("first");
        assertEquals("{\"FirstName\":\"first\"}",Serializer.INSTANCE.serializeChangesOnly(person));
    }
    
    @Test
    public void testOverrideJsonIncludeAnnotation() throws JsonProcessingException {
        ObjectMapper m = new ObjectMapper();
        m.setSerializationInclusion(Include.ALWAYS);
        m.setAnnotationIntrospector(IGNORE_JSON_INCLUDE_ANNOTATION);
        String json = m.writeValueAsString(new Thing());
        assertEquals("{\"name\":\"Bert\",\"address\":null}", json);
    }
    
    private static final JacksonAnnotationIntrospector IGNORE_JSON_INCLUDE_ANNOTATION = new JacksonAnnotationIntrospector() {

        @Override
        protected <A extends Annotation> A _findAnnotation(final Annotated annotated, final Class<A> annoClass) {
            if (!annotated.hasAnnotation(JsonInclude.class)) {
                return super._findAnnotation(annotated, annoClass);
            }
            return null;
        }
    };
    
    @JsonInclude(Include.NON_NULL)
    static final class Thing {
        @JsonProperty
        String name = "Bert";
        
        @JsonProperty
        String address = null;
    }

    @Test
    public void testSerializeChangesOnlyWithNullValue() {
        Person person = new Person("user",null,"last").setLastName(null);
        assertEquals("{\"LastName\":null}",Serializer.INSTANCE.serializeChangesOnly(person));
    }

    static final class Person implements ODataEntityType {

        @JsonProperty("UserName")
        protected String userName;

        @JsonProperty("FirstName")
        protected String firstName;

        @JsonProperty("LastName")
        protected String lastName;

        ChangedFields changedFields;

        public Person(String userName, String firstName, String lastName) {
            this.userName = userName;
            this.firstName = firstName;
            this.lastName = lastName;
            changedFields = new ChangedFields();
        }

        @Override
        public Map<String, Object> getUnmappedFields() {
            return Collections.emptyMap();
        }

        @Override
        public ChangedFields getChangedFields() {
            return changedFields;
        }

        @Override
        public void postInject(boolean addKeysToContextPath) {
            // do nothing
        }

        @Override
        public String odataTypeName() {
            return "person";
        }

        public Person setUserName(String userName) {
            this.userName = userName;
            changedFields = changedFields.add("UserName");
            return this;
        }

        public Person setFirstName(String firstName) {
            this.firstName = firstName;
            changedFields = changedFields.add("FirstName");
            return this;
        }

        public Person setLastName(String lastName) {
            this.lastName = lastName;
            changedFields = changedFields.add("LastName");
            return this;
        }

    }

}
