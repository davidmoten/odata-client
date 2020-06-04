package com.github.davidmoten.odata.client;

import static org.junit.Assert.assertEquals;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    public void testSerializeChangesOnlyWithNullValue() {
        Person person = new Person("user",null,"last").setLastName(null);
        assertEquals("{\"LastName\":null}",Serializer.INSTANCE.serializeChangesOnly(person));
    }

    static final class Person implements ODataEntityType {

        @JsonProperty("UserName")
        public String userName;

        @JsonProperty("FirstName")
        String firstName;

        @JsonProperty("LastName")
        String lastName;

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
