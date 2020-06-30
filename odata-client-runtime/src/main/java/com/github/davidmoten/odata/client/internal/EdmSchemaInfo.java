package com.github.davidmoten.odata.client.internal;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.github.davidmoten.odata.client.SchemaInfo;
import com.github.davidmoten.odata.client.edm.GeographyPoint;
import com.github.davidmoten.odata.client.edm.UnsignedByte;

public enum EdmSchemaInfo implements SchemaInfo {

    INSTANCE;

    private final Map<String, Class<?>> map;
    private final Map<Class<?>, String> reverseMap;

    private EdmSchemaInfo() {
        map = new HashMap<>();
        map.put("Edm.String", String.class);
        map.put("Edm.Boolean", Boolean.class);
        map.put("Edm.DateTimeOffset", OffsetDateTime.class);
        map.put("Edm.Duration", Duration.class);
        map.put("Edm.TimeOfDay", LocalTime.class);
        map.put("Edm.Date", LocalDate.class);
        map.put("Edm.Int32", Integer.class);
        map.put("Edm.Int16", Short.class);
        map.put("Edm.Byte", UnsignedByte.class);
        map.put("Edm.SByte", byte.class);
        map.put("Edm.Single", Float.class);
        map.put("Edm.Double", Double.class);
        map.put("Edm.Guid", String.class);
        map.put("Edm.Int64", Long.class);
        map.put("Edm.Binary", byte[].class);

        // if is null then contains Base64 content otherwise another field has the url
        map.put("Edm.Stream", String.class);
        map.put("Edm.GeographyPoint", GeographyPoint.class);
        map.put("Edm.Decimal", BigDecimal.class);
        reverseMap = new HashMap<>();
        for (Entry<String, Class<?>> entry:map.entrySet()) {
            reverseMap.put(entry.getValue(), entry.getKey());
        }
    }

    @Override
    public Class<?> getClassFromTypeWithNamespace(String name) {
        Class<?> cls = map.get(name);
        if (cls == null) {
            throw new RuntimeException("unhandled type: " + name);
        }
        return cls;
    }
    
    public String getTypeWithNamespaceFromClass(Class<?> cls) {
        String t = reverseMap.get(cls);
        if (t == null) {
            throw new IllegalArgumentException(cls + " not mappable to Edm type");
        }
        return t;
    }

}
