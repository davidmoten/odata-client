package com.github.davidmoten.odata.client;

import java.util.Map;
import java.util.Map.Entry;

import com.github.davidmoten.guavamini.annotations.VisibleForTesting;
import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class FunctionRequestReturningNonCollection<T>
        extends ActionFunctionRequestBase<FunctionRequestReturningNonCollection<T>> {

    private final Class<T> returnClass;
    private final SchemaInfo schemaInfo;

    public FunctionRequestReturningNonCollection(ContextPath contextPath, Class<T> returnClass,
            Map<String, Object> parameters, SchemaInfo schemaInfo) {
        super(parameters, contextPath);
        this.returnClass = returnClass;
        this.schemaInfo = schemaInfo;
    }

    public T get() {
        return RequestHelper.get( //
                contextPath.addSegment(toInlineParameterSyntax(contextPath.context().serializer(), parameters)), //
                returnClass, //
                this, //
                schemaInfo);
    }

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
    // Edm.Guid -> String
    // Edm.Int64 -> Long
    // Edm.Binary -> byte[]
    // null then contains Base64 content otherwise another field has the url
    // Edm.Stream", String
    // Edm.GeographyPoint", GeographyPoint
    // Edm.Decimal", BigDecimal

    // TODO unit test
    // parameter values in order to disambiguate requests.
    @VisibleForTesting
    static String toInlineParameterSyntax(Serializer serializer, Map<String, Object> parameters) {
        StringBuilder b = new StringBuilder();
        for (Entry<String, Object> entry : parameters.entrySet()) {
            if (b.length() > 0) {
                b.append(",");
            }
            b.append(entry.getKey());
            b.append("=");
            // TODO Clients SHOULD use typed null values (e.g. null'Date') when passing null
            b.append(serializer.serialize(entry.getValue()));
        }
        b.insert(0, "(");
        b.append(")");
        return b.toString();
    }

}