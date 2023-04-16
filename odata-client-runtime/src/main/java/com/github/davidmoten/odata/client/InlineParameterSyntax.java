package com.github.davidmoten.odata.client;

import java.util.Map;
import java.util.Map.Entry;

import com.github.davidmoten.guavamini.annotations.VisibleForTesting;
import com.github.davidmoten.odata.client.internal.TypedObject;

final class InlineParameterSyntax {
    // see
    // http://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_InlineParameterSyntax
    // TODO investigate the format for encoding all of the Edm simple types (see
    // EdmSchemaInfo)
    // Types are (including collections of):
    // Edm.String -> String
    // Edm.Boolean -> Boolean
    // Edm.DateTimeOffset -> OffsetDateTime
    // Edm.Duration -> Duration
    // Edm.TimeOfDay -> LocalTime
    // Edm.Date -> LocalDate
    // Edm.Int32 -> Integer
    // Edm.Int16 -> Short
    // Edm.Byte -> UnsignedByte
    // Edm.SByte -> byte
    // Edm.Single -> Float
    // Edm.Double -> Double
    // Edm.Guid -> UUID
    // Edm.Int64 -> Long
    // Edm.Binary -> byte[]
    // null then contains Base64 content otherwise another field has the url
    // Edm.Stream", String
    // Edm.GeographyPoint", GeographyPoint
    // Edm.Decimal", BigDecimal

    // TODO unit test

    @VisibleForTesting
    static String encode(Serializer serializer, Map<String, TypedObject> parameters) {
        StringBuilder b = new StringBuilder();
        for (Entry<String, TypedObject> entry : parameters.entrySet()) {
            if (b.length() > 0) {
                b.append(",");
            }
            b.append(entry.getKey());
            b.append("=");
            // TODO Clients SHOULD use typed null values (e.g. null'Date') when passing null
            // parameter values in order to disambiguate requests.
            final String value;
            if (entry.getValue().object() == null) {
                value = "null'" + entry.getValue().typeWithNamespace() + "'";
            } else if ("Edm.String".equals(entry.getValue().typeWithNamespace())){
                value = "'" + entry.getValue().object() + "'";
            } else {
                value = serializer.serialize(entry.getValue().object());
            }
            b.append(value);
        }
        b.insert(0, "(");
        b.append(")");
        return b.toString();
    }
}
