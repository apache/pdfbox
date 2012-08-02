/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.padaf.xmpbox.type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.parser.XMPDocumentBuilder;
import org.junit.Test;

public abstract class AbstractStructuredTypeTester {

	protected XMPMetadata xmp;
	
	protected String fieldName;
	
	protected String type;
	
	protected Class<? extends AbstractStructuredType> clz;
	
	protected TypeMapping typeMapping = null;
	
	protected XMPDocumentBuilder builder;
	
	public void before () throws Exception {
		builder = new XMPDocumentBuilder();
		xmp = builder.createXMPMetadata();
		typeMapping = builder.getTypeMapping();
	}
	
	public AbstractStructuredTypeTester (Class<? extends AbstractStructuredType> clz, String fieldName, String type) {
		this.clz = clz;
		this.fieldName = fieldName;
		this.type = type;
	}
	
	protected abstract AbstractStructuredType getStructured ();
	
	@Test
	public void testInitializedToNull() throws Exception {
		// default method
		Assert.assertNull(getStructured().getProperty(fieldName));
		// accessor
    	Method get = clz.getMethod(TypeTestingHelper.calculateSimpleGetter(fieldName), new Class[0]);
    	Object result = get.invoke(getStructured(), new Object [0]);
    	Assert.assertNull(result);
		
	}
	
	@Test
	public void testSettingValue() throws Exception {
		TypeDescription td =typeMapping.getTypeDescription(type);
		Object value = TypeTestingHelper.getJavaValue(td);
		getStructured().addSimpleProperty(fieldName, value);
		Assert.assertNotNull(getStructured().getProperty(fieldName));
		// check other properties not modified
		List<Field> fields = TypeTestingHelper.getXmpFields(clz);
		for (Field field : fields) {
			// do not check the current name
			String name = field.get(null).toString();
			if (!name.equals(fieldName)) {
				Assert.assertNull(getStructured().getProperty(name));
			}
		}
	}

	@Test
	public void testPropertyType() throws Exception {
		TypeDescription td =typeMapping.getTypeDescription(type);
		Object value = TypeTestingHelper.getJavaValue(td);
		getStructured().addSimpleProperty(fieldName, value);
		Assert.assertNotNull(getStructured().getProperty(fieldName));
		// check property type
		AbstractSimpleProperty asp = getStructured().getProperty(fieldName);
		Assert.assertEquals(td.getTypeClass(),asp.getClass());
	}

	
//	protected List<Field> getXmpFields () {
//		Field [] fields = clz.getFields();
//		List<Field> result = new ArrayList<Field>(fields.length);
//		for (Field field : fields) {
//			if (field.getAnnotation(PropertyType.class)!=null) {
//				result.add(field);
//			}
//		}
//		return result;
//	}

    @Test
    public void testSetter () throws Exception {
    	String setter = TypeTestingHelper.calculateSimpleSetter(fieldName);
    	TypeDescription td = typeMapping.getTypeDescription(type);
    	Object value = TypeTestingHelper.getJavaValue(td);
    	Method set = clz.getMethod(setter, new Class<?>[] {TypeTestingHelper.getJavaType(td)} );
    	set.invoke(getStructured(), new Object [] {value});
    	// check property set
    	Assert.assertEquals(value, getStructured().getProperty(fieldName).getObjectValue());
    	// check getter
    	Method get = clz.getMethod(TypeTestingHelper.calculateSimpleGetter(fieldName), new Class[0]);
    	Object result = get.invoke(getStructured(), new Object [0]);
//    	Assert.assertEquals(getJavaType(td),result.getClass());
    	Assert.assertTrue(TypeTestingHelper.getJavaType(td).isAssignableFrom(result.getClass()));
    	Assert.assertEquals(value, result);
    	
    }
    
    
    

	
}
