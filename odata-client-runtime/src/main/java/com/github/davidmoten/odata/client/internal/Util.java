package com.github.davidmoten.odata.client.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;

import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.ODataType;

public final class Util {

    public static String readString(InputStream in, Charset charset) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return new String(result.toByteArray(), charset);
    }

    public static String trimTrailingSlash(String url) {
        if (!url.isEmpty() && url.charAt(url.length() - 1) == '/') {
            return url.substring(0, url.length() - 2);
        } else {
            return url;
        }
    }
    
    @SuppressWarnings("unchecked")
	public static <T extends ODataType> String odataTypeName(Class<T> cls) {
        try {
        	Constructor<?> c = null;
        	Constructor<?>[] constructors = cls.getDeclaredConstructors();
            for (Constructor<?> constructor : constructors) {
            	if (constructor.getParameterCount() == 0) {
            	    constructor.setAccessible(true);
            	    c = constructor;
            	}
            }
            T o = (T) c.newInstance();
            return o.odataTypeName();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | SecurityException e) {
            throw new ClientException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
	public static <T> String odataTypeNameFromAny(Class<T> cls) {
        if (ODataType.class.isAssignableFrom(cls)) {
            return odataTypeName((Class<? extends ODataType>) cls);
        } else {
            return EdmSchemaInfo.INSTANCE.getTypeWithNamespaceFromClass(cls);
        }
    }

}
