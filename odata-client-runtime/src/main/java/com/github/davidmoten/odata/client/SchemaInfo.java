package com.github.davidmoten.odata.client;

public interface SchemaInfo {

    /**
     * Returns the class corresponding to the given OData type name. Returns null if
     * not present in the schema.
     * 
     * @param name OData type name
     * @return the class corresponding to the given OData type name, returns null if
     *         not present in the schema
     */
    Class<?> getClassFromTypeWithNamespace(String name);

}
