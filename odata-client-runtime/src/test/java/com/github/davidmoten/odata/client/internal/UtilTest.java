package com.github.davidmoten.odata.client.internal;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.github.davidmoten.odata.client.ODataType;

public class UtilTest {
	
	@Test
	public void testODataTypeNameFromAnyString() {
		assertEquals("Edm.String", Util.odataTypeNameFromAny(String.class));
	}
	
	@Test
	public void testODataTypeNameFromAnyODataTypeTemp() {
		assertEquals("hello.there", Util.odataTypeNameFromAny(Temp.class));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testODataTypeNameFromAnyObject() {
		Util.odataTypeNameFromAny(Object.class);
	}
	
	public final static class Temp implements ODataType {
		
		protected Temp() {
			
		}

		@Override
		public String odataTypeName() {
			return "hello.there";
		}

		@Override
		public Map<String, Object> getUnmappedFields() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void postInject(boolean addKeysToContextPath) {
			// TODO Auto-generated method stub
			
		}
		
	}
	

}
