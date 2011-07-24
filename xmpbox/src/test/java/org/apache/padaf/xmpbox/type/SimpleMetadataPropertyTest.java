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

import java.util.Calendar;
import java.util.List;


import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.type.Attribute;
import org.apache.padaf.xmpbox.type.BooleanType;
import org.apache.padaf.xmpbox.type.DateType;
import org.apache.padaf.xmpbox.type.IntegerType;
import org.apache.padaf.xmpbox.type.RealType;
import org.apache.padaf.xmpbox.type.TextType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * Test MetaData Objects for simple properties
 * 
 * @author a183132
 * 
 */
public class SimpleMetadataPropertyTest {

	protected XMPMetadata parent;

	@Before
	public void resetDocument() throws Exception {
		parent = new XMPMetadata();
	}

	/**
	 * Check the detection of a bad type
	 * 
	 * @throws InappropriateTypeException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testBooleanBadTypeDetection() {
		new BooleanType(parent, "test", "booleen", "Not a Boolean");
	}

	/**
	 * Check the detection of a bad type
	 * 
	 * @throws InappropriateTypeException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testDateBadTypeDetection() {
		new DateType(parent, "test", "date", "Bad Date");
	}

	/**
	 * Check the detection of a bad type
	 * 
	 * @throws InappropriateTypeException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIntegerBadTypeDetection() {
		new IntegerType(parent, "test", "integer", "Not an int");
	}

	/**
	 * Check the detection of a bad type
	 * 
	 * @throws InappropriateTypeException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRealBadTypeDetection() throws Exception {
		new RealType(parent, "test", "real", "Not a real");
	}

	/**
	 * Check the detection of a bad type
	 * 
	 * @throws InappropriateTypeException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testTextBadTypeDetection() throws Exception {
		new TextType(parent, "test", "text", Calendar.getInstance());
	}

	/**
	 * Check if information between objects and the elment generated are equals
	 * 
	 * @throws Exception
	 */
	@Test
	public void testElementAndObjectSynchronization() throws Exception {
		boolean boolv = true;
		Calendar datev = Calendar.getInstance();
		int integerv = 1;
		float realv = Float.parseFloat("1.69");
		String textv = "TEXTCONTENT";
		BooleanType bool = new BooleanType(parent, "test", "booleen", boolv);
		DateType date = new DateType(parent, "test", "date", datev);
		IntegerType integer = new IntegerType(parent, "test", "integer",
				integerv);
		RealType real = new RealType(parent, "test", "real", realv);
		TextType text = new TextType(parent, "test", "text", textv);

		Assert.assertEquals(bool.getNamespace(), bool.getElement()
				.getNamespaceURI());
		Assert.assertEquals(bool.getPrefix() + ":" + bool.getPropertyName(),
				bool.getElement().getNodeName());
		Assert.assertEquals(bool.getQualifiedName(), bool.getElement()
				.getNodeName());
		Assert.assertEquals(boolv, bool.getValue());
		Assert.assertEquals(datev, date.getValue());
		Assert.assertEquals(integerv, integer.getValue());
		Assert.assertEquals(realv, real.getValue(), 0);
		Assert.assertEquals(textv, text.getStringValue());

	}

	/**
	 * Check Object creation from corresponding Java type
	 * 
	 * @throws Exception
	 */
	@Test
	public void testObjectCreationFromJavaType() throws Exception {
		BooleanType bool = new BooleanType(parent, "test", "booleen", true);
		DateType date = new DateType(parent, "test", "date", Calendar
				.getInstance());
		IntegerType integer = new IntegerType(parent, "test", "integer", 1);
		RealType real = new RealType(parent, "test", "real", (float) 1.6);
		TextType text = new TextType(parent, "test", "text", "TEST");

		Element e = parent.getFuturOwner().createElement("TEST");
		parent.getFuturOwner().appendChild(e);
		e.appendChild(bool.getElement());
		e.appendChild(date.getElement());
		e.appendChild(integer.getElement());
		e.appendChild(real.getElement());
		e.appendChild(text.getElement());

		// XMLUtil.save(parent.getFuturOwner(), System.out, "UTF-8");

	}

	/**
	 * Check the creation from string attributes
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreationFromString() throws Exception {
		String boolv = "False";
		String datev = "2010-03-22T14:33:11+01:00";
		String integerv = "10";
		String realv = "1.92";
		String textv = "text";

		BooleanType bool = new BooleanType(parent, "test", "booleen", boolv);
		DateType date = new DateType(parent, "test", "date", datev);
		IntegerType integer = new IntegerType(parent, "test", "integer",
				integerv);
		RealType real = new RealType(parent, "test", "real", realv);
		TextType text = new TextType(parent, "test", "text", textv);

		Assert.assertEquals(boolv, bool.getStringValue());
		Assert.assertEquals(datev, date.getStringValue());
		Assert.assertEquals(integerv, integer.getStringValue());
		Assert.assertEquals(realv, real.getStringValue());
		Assert.assertEquals(textv, text.getStringValue());
	}

	/**
	 * Check creation when a namespace is specified
	 * 
	 * @throws Exception
	 */
	@Test
	public void testObjectCreationWithNamespace() throws Exception {
		String ns = "http://www.test.org/pdfa/";
		BooleanType bool = new BooleanType(parent, ns, "test", "booleen", true);
		DateType date = new DateType(parent, ns, "test", "date", Calendar
				.getInstance());
		IntegerType integer = new IntegerType(parent, ns, "test", "integer", 1);
		RealType real = new RealType(parent, ns, "test", "real", (float) 1.6);
		TextType text = new TextType(parent, ns, "test", "text", "TEST");

		Assert.assertEquals(ns, bool.getNamespace());
		Assert.assertEquals(ns, date.getNamespace());
		Assert.assertEquals(ns, integer.getNamespace());
		Assert.assertEquals(ns, real.getNamespace());
		Assert.assertEquals(ns, text.getNamespace());

		Element e = parent.getFuturOwner().createElement("TEST");
		parent.getFuturOwner().appendChild(e);
		e.appendChild(bool.getElement());
		e.appendChild(date.getElement());
		e.appendChild(integer.getElement());
		e.appendChild(real.getElement());
		e.appendChild(text.getElement());

		// XMLUtil.save(parent.getFuturOwner(), System.out, "UTF-8");

	}

	/**
	 * Throw InappropriateType Exception
	 * 
	 * @throws InappropriateTypeException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testExceptionWithCause() throws Exception {
		throw new IllegalArgumentException("TEST", new Throwable());
	}

	/**
	 * Check if attributes management works
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAttribute() throws Exception {

		IntegerType integer = new IntegerType(parent, "test", "integer", 1);
		Attribute value = new Attribute("http://www.test.org/test/", "test",
				"value1", "StringValue1");
		Attribute value2 = new Attribute(null, "test", "value2", "StringValue2");

		integer.setAttribute(value);

		// System.out.println(value.getQualifiedName());

		Assert.assertEquals(value, integer.getAttribute(value
				.getQualifiedName()));
		Assert.assertTrue(integer.containsAttribute(value.getQualifiedName()));

		// Replacement check

		integer.setAttribute(value2);
		Assert.assertEquals(value2, integer.getAttribute(value2
				.getQualifiedName()));

		integer.removeAttribute(value2.getQualifiedName());
		Assert
				.assertFalse(integer.containsAttribute(value2
						.getQualifiedName()));

		// Attribute with namespace Creation checking
		Attribute valueNS = new Attribute("http://www.tefst2.org/test/",
				"test2", "value2", "StringValue.2");
		integer.setAttribute(valueNS);
		Attribute valueNS2 = new Attribute("http://www.test2.org/test/",
				"test2", "value2", "StringValueTwo");
		integer.setAttribute(valueNS2);

		List<Attribute> atts = integer.getAllAttributes();
		/*
		 * for (Attribute attribute : atts) {
		 * System.out.println(attribute.getLocalName
		 * ()+" :"+attribute.getValue()); }
		 */
		Assert.assertFalse(atts.contains(valueNS));
		Assert.assertTrue(atts.contains(valueNS2));

		parent.getFuturOwner().appendChild(integer.getElement());
		// XMLUtil.save(parent.getFuturOwner(), System.out, "UTF-8");
	}

}
