package com.github.davidmoten.odata.client.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.util.ClassUtil;

public final class InjectableValuesFromFactories
        extends InjectableValues
        implements java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        protected final Map<String,Callable<?>> _values;
        
        public InjectableValuesFromFactories() {
            this(new HashMap<String,Callable<?>>());
        }

        public InjectableValuesFromFactories(Map<String, Callable<?>> values) {
            _values = values;
        }

        public InjectableValuesFromFactories addValue(String key, Callable<?> value) {
            _values.put(key, value);
            return this;
        }

        public InjectableValuesFromFactories addValue(Class<?> classKey, Callable<?> value) {
            _values.put(classKey.getName(), value);
            return this;
        }
        
        @Override
        public Object findInjectableValue(Object valueId, DeserializationContext ctxt,
                BeanProperty forProperty, Object beanInstance) throws JsonMappingException
        {
            if (!(valueId instanceof String)) {
                ctxt.reportBadDefinition(ClassUtil.classOf(valueId),
                        String.format(
                        "Unrecognized inject value id type (%s), expecting String",
                        ClassUtil.classNameOf(valueId)));
            }
            String key = (String) valueId;
            Callable<?> callable = _values.get(key);
            Object ob;
            try {
                ob = callable.call();
            } catch (Exception e) {
                throw new RuntimeException("callable threw when creating value", e);
            }
            if (ob == null && !_values.containsKey(key)) {
                throw new IllegalArgumentException("No injectable id with value '"+key+"' found (for property '"+forProperty.getName()+"')");
            }
            return ob;
        }
    }