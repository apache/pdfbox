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

package org.apache.padaf.xmpbox.schema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.PropertyType;
import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.apache.padaf.xmpbox.type.BooleanType;
import org.apache.padaf.xmpbox.type.DateType;
import org.apache.padaf.xmpbox.type.IntegerType;
import org.apache.padaf.xmpbox.type.TextType;
import org.apache.padaf.xmpbox.type.ThumbnailType;
import org.junit.Test;

public abstract class AbstractXMPSchemaTest {

	protected XMPMetadata metadata;

	protected String property;

	protected String type;

	protected XMPSchema schema;

	protected Class<?> schemaClass;

	protected Object value;

	public AbstractXMPSchemaTest(String property, String type, Object value) {
		this.property = property;
		this.value = value;
		this.type = type;

	}

	public static Object[] wrapProperty(String name, String type, Object value) {
		if (type.equals("Boolean")) {
			Assert.assertTrue(value instanceof Boolean);
		} else if (type.equals("Text")) {
			Assert.assertTrue(value instanceof String);
		} else if (type.equals("Integer")) {
			Assert.assertTrue(value instanceof Integer);
		} else if (type.equals("Date")) {
			Assert.assertTrue(value instanceof Calendar);
		} else if (type.equals("URL")) {
			Assert.assertTrue(value instanceof String);
		}
		return new Object[] { name, type, value };
	}

	@Test
	public void testGetSetValue() throws Exception {
		if (type.equals("Text")) {
			testGetSetTextValue();
		} else if (type.equals("Boolean")) {
			testGetSetBooleanValue();
		} else if (type.equals("Integer")) {
			testGetSetIntegerValue();
		} else if (type.equals("Date")) {
			testGetSetDateValue();
		} else if (type.equals("seq Text")) {
			// do nothing
		} else if (type.equals("bag Text")) {
			// do nothing
		} else if (type.equals("bag ProperName")) {
			// do nothing
		} else if (type.equals("bag Xpath")) {
			// do nothing
		} else if (type.equals("seq Date")) {
			// do nothing
		} else if (type.equals("Lang Alt")) {
			// do nothing
		} else if (type.equals("Alt Thumbnail")) {
			// do nothing
		} else if (type.equals("URL")) {
			testGetSetTextValue();
		} else {
			throw new Exception("Unknown type : " + type);
		}
	}

	@Test
	public void testGetSetProperty() throws Exception {
		if (value instanceof String) {
			testGetSetTextProperty();
		} else if (type.equals("Boolean")) {
			testGetSetBooleanProperty();
		} else if (type.equals("Integer")) {
			testGetSetIntegerProperty();
		} else if (type.equals("Date")) {
			testGetSetDateProperty();
		} else if (type.equals("seq Text")) {
			testGetSetTextListValue("seq");
		} else if (type.equals("bag Text")) {
			testGetSetTextListValue("bag");
		} else if (type.equals("bag ProperName")) {
			testGetSetTextListValue("bag");
		} else if (type.equals("bag Xpath")) {
			testGetSetTextListValue("bag");
		} else if (type.equals("seq Date")) {
			testGetSetDateListValue("seq");
		} else if (type.equals("Lang Alt")) {
			testGetSetLangAltValue();
		} else if (type.equals("Alt Thumbnail")) {
			testGetSetThumbnail();
		} else {
			throw new Exception("Unknown type : " + value.getClass());
		}
		Field[] fields = schemaClass.getFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(PropertyType.class)) {
				if (!field.get(schema).equals(property)) {
					PropertyType pt = field.getAnnotation(PropertyType.class);
					if (pt.propertyType().equals("Lang Alt")) {
						// do not check method existence
					} else if (pt.propertyType().equals("Alt Thumbnail")) {
						// do not check method existence
					} else {
						// type test
						String getName = "get"
								+ firstUpper(field.get(schema).toString());
						Method getMethod = schemaClass.getMethod(getName);
						Assert.assertNull(getName
								+ " should return null when testing "
								+ property, getMethod.invoke(schema));
						// value test
						String getNameValue = getName + "Value";
						getMethod = schemaClass.getMethod(getNameValue);
						Assert.assertNotNull(getNameValue
								+ " method should exist", getMethod);
						Assert.assertNull(getNameValue
								+ " should return null when testing "
								+ property, getMethod.invoke(schema));
					}
				}
			}
		}
	}

	protected String firstUpper(String name) {
		StringBuilder sb = new StringBuilder(name.length());
		sb.append(name.substring(0, 1).toUpperCase());
		sb.append(name.substring(1));
		return sb.toString();
	}

	protected String setMethod(String prop) {
		String fu = firstUpper(prop);
		StringBuilder sb = new StringBuilder(3 + prop.length());
		sb.append("set").append(fu);
		return sb.toString();
	}

	protected String addMethod(String prop) {
		String fu = firstUpper(prop);
		StringBuilder sb = new StringBuilder(3 + prop.length());
		sb.append("add").append(fu);
		return sb.toString();
	}

	protected String getMethod(String prop) {
		String fu = firstUpper(prop);
		StringBuilder sb = new StringBuilder(3 + prop.length());
		sb.append("get").append(fu);
		return sb.toString();
	}

	protected String setValueMethod(String prop) {
		String fu = firstUpper(prop);
		StringBuilder sb = new StringBuilder(8 + prop.length());
		sb.append("set").append(fu).append("Value");
		return sb.toString();
	}

	protected String getValueMethod(String prop) {
		String fu = firstUpper(prop);
		StringBuilder sb = new StringBuilder(8 + prop.length());
		sb.append("get").append(fu).append("Value");
		return sb.toString();
	}

	protected String addToValueMethod(String prop) {
		String fu = firstUpper(prop);
		StringBuilder sb = new StringBuilder(10 + prop.length());
		sb.append("addTo").append(fu).append("Value");
		return sb.toString();

	}

	protected void testGetSetBooleanProperty() throws Exception {
		String setName = setMethod(property);
		String getName = getMethod(property);

		BooleanType bt = new BooleanType(metadata, schema.getLocalPrefix(),
				property, value);
		Method setMethod = schemaClass.getMethod(setName, BooleanType.class);
		Method getMethod = schemaClass.getMethod(getName);

		setMethod.invoke(schema, bt);
		Boolean found = ((BooleanType) getMethod.invoke(schema)).getValue();
		Assert.assertEquals(value, found);

	}

	protected void testGetSetDateProperty() throws Exception {
		String setName = setMethod(property);
		String getName = getMethod(property);

		DateType dt = new DateType(metadata, schema.getLocalPrefix(), property,
				value);
		Method setMethod = schemaClass.getMethod(setName, DateType.class);
		Method getMethod = schemaClass.getMethod(getName);

		setMethod.invoke(schema, dt);
		Calendar found = ((DateType) getMethod.invoke(schema)).getValue();
		Assert.assertEquals(value, found);
	}

	protected void testGetSetIntegerProperty() throws Exception {
		String setName = setMethod(property);
		String getName = getMethod(property);

		IntegerType it = new IntegerType(metadata, schema.getLocalPrefix(),
				property, value);
		Method setMethod = schemaClass.getMethod(setName, IntegerType.class);
		Method getMethod = schemaClass.getMethod(getName);

		setMethod.invoke(schema, it);
		Integer found = ((IntegerType) getMethod.invoke(schema)).getValue();
		Assert.assertEquals(value, found);
	}

	protected void testGetSetTextProperty() throws Exception {
		String setName = setMethod(property);
		String getName = getMethod(property);

		TextType tt = new TextType(metadata, schema.getLocalPrefix(), property,
				value);
		Method setMethod = schemaClass.getMethod(setName, TextType.class);
		Method getMethod = schemaClass.getMethod(getName);

		setMethod.invoke(schema, tt);
		String found = ((TextType) getMethod.invoke(schema)).getStringValue();
		Assert.assertEquals(value, found);

	}

	protected void testGetSetTextListValue(String tp) throws Exception {
		String setName = addToValueMethod(property);
		String getName = getValueMethod(property);
		String[] svalue = (String[]) value;
		Arrays.sort(svalue);
		// push all
		Method setMethod = schemaClass.getMethod(setName, String.class);
		for (String string : svalue) {
			setMethod.invoke(schema, string);
		}
		// retrieve
		Method getMethod = schemaClass.getMethod(getName);
		List<String> fields = (List<String>) getMethod.invoke(schema);
		for (String field : fields) {
			Assert.assertTrue(field + " should be found in list", Arrays
					.binarySearch(svalue, field) >= 0);
		}
	}

	protected void testGetSetDateListValue(String tp) throws Exception {
		String setName = addToValueMethod(property);
		String getName = getValueMethod(property);
		Calendar[] svalue = (Calendar[]) value;
		Arrays.sort(svalue);
		// push all
		Method setMethod = schemaClass.getMethod(setName, Calendar.class);
		for (Calendar inst : svalue) {
			setMethod.invoke(schema, inst);
		}
		// retrieve
		Method getMethod = schemaClass.getMethod(getName);
		List<Calendar> fields = (List<Calendar>) getMethod.invoke(schema);
		for (Calendar field : fields) {
			Assert.assertTrue(field + " should be found in list", Arrays
					.binarySearch(svalue, field) >= 0);
		}
	}

	protected void testGetSetThumbnail() throws Exception {
		String addName = addMethod(property);
		String getName = getMethod(property);
		Method setMethod = schemaClass.getMethod(addName, Integer.class,
				Integer.class, String.class, String.class);
		Method getMethod = schemaClass.getMethod(getName);
		/*
		 * <xapGImg:height>162</xapGImg:height>
		 * <xapGImg:width>216</xapGImg:width>
		 * <xapGImg:format>JPEG</xapGImg:format>
		 * <xapGImg:image>/9j/4AAQSkZJRgABAgEASABIAAD</xapGImg:image>
		 */
		Integer height = 162;
		Integer width = 400;
		String format = "JPEG";
		String img = "/9j/4AAQSkZJRgABAgEASABIAAD";
		setMethod.invoke(schema, height, width, format, img);
		List<ThumbnailType> found = ((List<ThumbnailType>) getMethod
				.invoke(schema));
		Assert.assertTrue(found.size() == 1);
		ThumbnailType t1 = found.get(0);
		Assert.assertEquals(height, t1.getHeight());
		Assert.assertEquals(width, t1.getWidth());
		Assert.assertEquals(format, t1.getFormat());
		Assert.assertEquals(img, t1.getImg());

	}

	protected void testGetSetLangAltValue() throws Exception {
		String setName = addToValueMethod(property);
		String getName = getValueMethod(property);
		Map<String, String> svalue = (Map<String, String>) value;
		// push all
		Method setMethod = schemaClass.getMethod(setName, String.class,
				String.class);

		for (Map.Entry<String, String> inst : svalue.entrySet()) {
			setMethod.invoke(schema, inst.getKey(), inst.getValue());
		}
		// retrieve
		String getLanguagesName = "get" + firstUpper(property) + "Languages";
		Method getLanguages = schemaClass.getMethod(getLanguagesName);
		List<String> lgs = (List<String>) getLanguages.invoke(schema);
		for (String string : lgs) {
			Method getMethod = schemaClass.getMethod(getName, String.class);
			String res = (String) getMethod.invoke(schema, string);
			Assert.assertEquals(res, svalue.get(string));
		}
	}

	protected void testGetSetURLValue() throws Exception {
		String setName = addToValueMethod(property);
		String getName = getValueMethod(property);
		String svalue = (String) value;
		// push all
		Method setMethod = schemaClass.getMethod(setName, String.class,
				String.class);
		setMethod.invoke(schema, property, svalue);

		// retrieve
		String getLanguagesName = "get" + firstUpper(property) + "Languages";
		Method getLanguages = schemaClass.getMethod(getLanguagesName);
		List<String> lgs = (List<String>) getLanguages.invoke(schema);
		for (String string : lgs) {
			Method getMethod = schemaClass.getMethod(getName, String.class);
			String res = (String) getMethod.invoke(schema, string);
			Assert.assertEquals(res, svalue);
		}
	}

	protected void testGetSetTextValue() throws Exception {
		String setName = setValueMethod(property);
		String getName = getValueMethod(property);

		Method setMethod = schemaClass.getMethod(setName, String.class);
		Method getMethod = schemaClass.getMethod(getName);

		setMethod.invoke(schema, value);
		String found = (String) getMethod.invoke(schema);

		Assert.assertEquals(value, found);
	}

	protected void testGetSetBooleanValue() throws Exception {
		String setName = setValueMethod(property);
		String getName = getValueMethod(property);

		Method setMethod = schemaClass.getMethod(setName, Boolean.class);
		Method getMethod = schemaClass.getMethod(getName);

		setMethod.invoke(schema, value);
		Boolean found = (Boolean) getMethod.invoke(schema);

		Assert.assertEquals(value, found);
	}

	protected void testGetSetDateValue() throws Exception {
		String setName = setValueMethod(property);
		String getName = getValueMethod(property);

		Method setMethod = schemaClass.getMethod(setName, Calendar.class);
		Method getMethod = schemaClass.getMethod(getName);

		setMethod.invoke(schema, value);
		Calendar found = (Calendar) getMethod.invoke(schema);

		Assert.assertEquals(value, found);
	}

	protected void testGetSetIntegerValue() throws Exception {
		String setName = setValueMethod(property);
		String getName = getValueMethod(property);

		Method setMethod = schemaClass.getMethod(setName, Integer.class);
		Method getMethod = schemaClass.getMethod(getName);

		setMethod.invoke(schema, value);
		Integer found = (Integer) getMethod.invoke(schema);

		Assert.assertEquals(value, found);
	}
}
