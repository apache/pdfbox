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

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import junit.framework.Assert;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.parser.DateConverter;
import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.apache.padaf.xmpbox.type.AbstractField;
import org.apache.padaf.xmpbox.type.Attribute;
import org.apache.padaf.xmpbox.type.BadFieldValueException;
import org.apache.padaf.xmpbox.type.BooleanType;
import org.apache.padaf.xmpbox.type.ComplexProperty;
import org.apache.padaf.xmpbox.type.DateType;
import org.apache.padaf.xmpbox.type.IntegerType;
import org.apache.padaf.xmpbox.type.TextType;
import org.junit.Before;
import org.junit.Test;

public class XMPSchemaTest {

	protected XMPMetadata parent;
	protected XMPSchema schem;

	@Before
	public void resetDocument() throws Exception {
		parent = new XMPMetadata();
		schem = new XMPSchema(parent, "nsSchem", "nsURI");

	}

	/**
	 * Check if Bag (Unordered Array) management is ok
	 * 
	 * @throws InappropriateTypeException
	 */
	@Test
	public void testBagManagement() throws Exception {
		String bagName = "nsSchem:BAGTEST";
		String value1 = "valueOne";
		String value2 = "valueTwo";
		schem.addBagValue(bagName, new TextType(parent, "rdf", "li", value1));
		schem.addBagValue(bagName, value2);

		List<String> values = schem.getBagValueList(bagName);
		Assert.assertEquals(value1, values.get(0));
		Assert.assertEquals(value2, values.get(1));

		schem.removeBagValue(bagName, value1);
		List<String> values2 = schem.getBagValueList(bagName);
		Assert.assertEquals(1, values2.size());
		Assert.assertEquals(value2, values2.get(0));

		/*
		 * System.out.println("Bag Management :");
		 * parent.getFuturOwner().appendChild(schem.getElement()); try {
		 * XMLUtil.save(parent.getFuturOwner(), System.out, "UTF-8"); } catch
		 * (TransformerException e) {
		 * 
		 * e.printStackTrace(); } System.out.println("------------------");
		 */

	}

	@Test
	public void testArrayList() throws Exception {
		ComplexProperty newSeq = new ComplexProperty(parent, "nsSchem",
				"seqType", ComplexProperty.ORDERED_ARRAY);
		TextType li1 = new TextType(parent, "rdf", "li", "valeur1");
		TextType li2 = new TextType(parent, "rdf", "li", "valeur2");
		newSeq.getContainer().addProperty(li1);
		newSeq.getContainer().addProperty(li2);
		schem.addProperty(newSeq);
		List<AbstractField> list = schem.getArrayList("nsSchem:seqType");
		Assert.assertTrue(list.contains(li1));
		Assert.assertTrue(list.contains(li2));

	}

	/**
	 * Check if Seq (Ordered Array) management is ok
	 * 
	 * @throws InappropriateTypeException
	 * @throws IOException
	 */
	@Test
	public void testSeqManagement() throws Exception {
		Calendar date = Calendar.getInstance();
		BooleanType bool = new BooleanType(parent, "rdf", "li", "True");
		String textVal = "seqValue";
		String seqName = "nsSchem:SEQNAME";

		schem.addSequenceDateValue(seqName, date);
		schem.addSequenceValue(seqName, bool);
		schem.addSequenceValue(seqName, textVal);

		List<Calendar> dates = schem.getSequenceDateValueList(seqName);
		Assert.assertEquals(1, dates.size());
		Assert.assertEquals(date, dates.get(0));

		List<String> values = schem.getSequenceValueList(seqName);
		Assert.assertEquals(3, values.size());
		Assert.assertEquals(DateConverter.toISO8601(date), values.get(0));
		Assert.assertEquals(bool.getStringValue(), values.get(1));
		Assert.assertEquals(textVal, values.get(2));

		/*
		 * System.out.println("Seq Management :");
		 * parent.getFuturOwner().appendChild(schem.getElement()); try {
		 * XMLUtil.save(parent.getFuturOwner(), System.out, "UTF-8"); } catch
		 * (TransformerException e) {
		 * 
		 * e.printStackTrace(); } System.out.println("------------------");
		 */

		schem.removeSequenceDateValue(seqName, date);
		Assert.assertEquals(0, schem.getSequenceDateValueList(seqName).size());

		schem.removeSequenceValue(seqName, bool);
		schem.removeSequenceValue(seqName, textVal);

		Assert.assertEquals(0, schem.getSequenceValueList(seqName).size());
	}

	@Test
	public void rdfAboutTest() {
		Assert.assertNull(schem.getAboutValue());
		String about = "about";
		schem.setAboutAsSimple(about);
		Assert.assertEquals(about, schem.getAboutValue());
		schem.setAboutAsSimple(null);
		Assert.assertNull(schem.getAboutValue());
	}

	@Test(expected = BadFieldValueException.class)
	public void testBadRdfAbout() throws Exception {
		schem.setAbout(new Attribute(null, "bill", "about", ""));
	}

	@Test
	public void testSetSpecifiedSimpleTypeProperty() throws Exception {
		String prop = "testprop";
		String val = "value";
		String val2 = "value2";
		schem.setTextPropertyValueAsSimple(prop, val);
		Assert.assertEquals(val, schem.getTextPropertyValueAsSimple(prop));
		schem.setTextPropertyValueAsSimple(prop, val2);
		Assert.assertEquals(val2, schem.getTextPropertyValueAsSimple(prop));
		schem.setTextPropertyValueAsSimple(prop, null);
		Assert.assertNull(schem.getTextPropertyValueAsSimple(prop));
	}

	@Test
	public void testSpecifiedSimplePropertyFormer() throws Exception {
		String prop = "testprop";
		String val = "value";
		String val2 = "value2";
		schem.setTextPropertyValueAsSimple(prop, val);
		TextType text = new TextType(parent, schem.getPrefix(), prop, "value2");
		schem.setTextProperty(text);
		Assert.assertEquals(val2, schem.getTextPropertyValueAsSimple(prop));
		Assert.assertEquals(text, schem.getTextProperty(schem.getPrefix() + ":"
				+ prop));
	}

	@Test
	public void testAsSimpleMethods() throws Exception {
		String bool = "bool";
		boolean boolVal = true;

		String date = "date";
		Calendar dateVal = Calendar.getInstance();

		String integ = "integer";
		Integer i = 1;

		String langprop = "langprop";
		String lang = "x-default";
		String langVal = "langVal";

		String bagprop = "bagProp";
		String bagVal = "bagVal";

		String seqprop = "SeqProp";
		String seqPropVal = "seqval";

		String seqdate = "SeqDate";

		String prefSchem = schem.getPrefix() + ":";

		schem.setBooleanPropertyValueAsSimple(bool, boolVal);
		schem.setDatePropertyValueAsSimple(date, dateVal);
		schem.setIntegerPropertyValueAsSimple(integ, i);
		schem.setLanguagePropertyValueAsSimple(langprop, lang, langVal);
		schem.addBagValueAsSimple(bagprop, bagVal);
		schem.addSequenceValueAsSimple(seqprop, seqPropVal);
		schem.addSequenceDateValueAsSimple(seqdate, dateVal);

		Assert.assertEquals(boolVal, schem.getBooleanProperty(prefSchem + bool)
				.getValue());
		Assert.assertEquals(dateVal, schem.getDateProperty(prefSchem + date)
				.getValue());
		Assert.assertEquals("" + i, schem.getIntegerProperty(prefSchem + integ)
				.getStringValue());
		Assert.assertEquals(langVal, schem.getLanguagePropertyValue(prefSchem
				+ langprop, lang));
		Assert.assertTrue(schem.getBagValueList(prefSchem + bagprop).contains(
				bagVal));
		Assert.assertTrue(schem.getSequenceValueList(prefSchem + seqprop)
				.contains(seqPropVal));
		Assert.assertTrue(schem.getSequenceDateValueList(prefSchem + seqdate)
				.contains(dateVal));
		Assert.assertTrue(schem.getLanguagePropertyLanguagesValue(
				prefSchem + langprop).contains(lang));

		Assert.assertEquals(boolVal, schem
				.getBooleanPropertyValueAsSimple(bool).booleanValue());
		Assert.assertEquals(dateVal, schem.getDatePropertyValueAsSimple(date));
		Assert.assertEquals(i, schem.getIntegerPropertyValueAsSimple(integ));
		Assert.assertEquals(langVal, schem.getLanguagePropertyValueAsSimple(
				langprop, lang));
		Assert.assertTrue(schem.getBagValueListAsSimple(bagprop).contains(
				bagVal));
		Assert.assertTrue(schem.getSequenceValueListAsSimple(seqprop).contains(
				seqPropVal));
		Assert.assertTrue(schem.getSequenceDateValueListAsSimple(seqdate)
				.contains(dateVal));
		Assert.assertTrue(schem.getLanguagePropertyLanguagesValueAsSimple(
				langprop).contains(lang));
	}

	/**
	 * Test All common simple properties management in XMPSchema
	 * 
	 * @throws InappropriateTypeException
	 * @throws BadFieldValueException
	 */
	@Test
	public void testProperties() throws Exception {

		Assert.assertEquals("nsURI", schem.getNamespaceValue());

		// In real cases, rdf ns will be declared before !
		schem.setAttribute(new Attribute("http://www.w3.org/2000/xmlns/",
				"xmlns", "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));

		String aboutVal = "aboutTest";
		schem.setAboutAsSimple(aboutVal);
		Assert.assertEquals(aboutVal, schem.getAboutValue());

		Attribute about = new Attribute(null, "rdf", "about", "YEP");
		schem.setAbout(about);
		Assert.assertEquals(about, schem.getAboutAttribute());

		String textProp = "nsSchem:textProp";
		String textPropVal = "TextPropTest";
		schem.setTextPropertyValue(textProp, textPropVal);
		Assert.assertEquals(textPropVal, schem.getTextPropertyValue(textProp));

		TextType text = new TextType(parent, "nsSchem", "textType", "GRINGO");
		schem.setTextProperty(text);
		Assert.assertEquals(text, schem.getTextProperty("nsSchem:textType"));

		Calendar dateVal = Calendar.getInstance();
		String date = "nsSchem:dateProp";
		schem.setDatePropertyValue(date, dateVal);
		Assert.assertEquals(dateVal, schem.getDatePropertyValue(date));

		DateType dateType = new DateType(parent, "nsSchem", "dateType",
				Calendar.getInstance());
		schem.setDateProperty(dateType);
		Assert
				.assertEquals(dateType, schem
						.getDateProperty("nsSchem:dateType"));

		String bool = "nsSchem:booleanTestProp";
		Boolean boolVal = false;
		schem.setBooleanPropertyValue(bool, boolVal);
		Assert.assertEquals(boolVal, schem.getBooleanPropertyValue(bool));

		BooleanType boolType = new BooleanType(parent, "nsSchem", "boolType",
				false);
		schem.setBooleanProperty(boolType);
		Assert.assertEquals(boolType, schem
				.getBooleanProperty("nsSchem:boolType"));

		String intProp = "nsSchem:IntegerTestProp";
		Integer intPropVal = 5;
		schem.setIntegerPropertyValue(intProp, intPropVal);
		Assert.assertEquals(intPropVal, schem.getIntegerPropertyValue(intProp));

		IntegerType intType = new IntegerType(parent, "nsSchem", "intType", 5);
		schem.setIntegerProperty(intType);
		Assert.assertEquals(intType, schem
				.getIntegerProperty("nsSchem:intType"));

		// Check bad type verification
		boolean ok = false;
		try {
			schem.getIntegerProperty("nsSchem:boolType");
		} catch (IllegalArgumentException e) {
			ok = true;
		}
		Assert.assertEquals(true, ok);
		ok = false;
		try {
			schem.getTextProperty("nsSchem:intType");
		} catch (IllegalArgumentException e) {
			ok = true;
		}
		Assert.assertEquals(true, ok);
		ok = false;
		try {
			schem.getDateProperty("nsSchem:textType");
		} catch (IllegalArgumentException e) {
			ok = true;
		}
		Assert.assertEquals(true, ok);
		ok = false;
		try {
			schem.getBooleanProperty("nsSchem:dateType");
		} catch (IllegalArgumentException e) {
			ok = true;
		}

		/*
		 * System.out.println("Simple Properties Management :");
		 * parent.getFuturOwner().appendChild(schem.getElement()); try {
		 * XMLUtil.save(parent.getFuturOwner(), System.out, "UTF-8"); } catch
		 * (TransformerException e) {
		 * 
		 * e.printStackTrace(); } System.out.println("------------------");
		 */

	}

	@Test
	public void testAltProperties() throws Exception {
		String altProp = "nsSchem:AltProp";

		String defaultLang = "x-default";
		String defaultVal = "Default Language";

		String usLang = "en-us";
		String usVal = "American Language";

		String frLang = "fr-fr";
		String frVal = "Lang française";

		schem.setLanguagePropertyValue(altProp, usLang, usVal);
		schem.setLanguagePropertyValue(altProp, defaultLang, defaultVal);
		schem.setLanguagePropertyValue(altProp, frLang, frVal);

		Assert.assertEquals(defaultVal, schem.getLanguagePropertyValue(altProp,
				defaultLang));
		Assert.assertEquals(frVal, schem.getLanguagePropertyValue(altProp,
				frLang));
		Assert.assertEquals(usVal, schem.getLanguagePropertyValue(altProp,
				usLang));

		List<String> languages = schem
				.getLanguagePropertyLanguagesValue(altProp);
		// default language must be in first place
		Assert.assertEquals(defaultLang, languages.get(0));

		Assert.assertTrue(languages.contains(usLang));
		Assert.assertTrue(languages.contains(frLang));

		// Test replacement/removal

		frVal = "Langue française";

		schem.setLanguagePropertyValue(altProp, frLang, frVal);
		Assert.assertEquals(frVal, schem.getLanguagePropertyValue(altProp,
				frLang));

		schem.setLanguagePropertyValue(altProp, frLang, null);
		languages = schem.getLanguagePropertyLanguagesValue(altProp);
		Assert.assertFalse(languages.contains(frLang));
		schem.setLanguagePropertyValue(altProp, frLang, frVal);

		/*
		 * System.out.println("Alternatives lang Management :");
		 * parent.getFuturOwner().appendChild(schem.getElement()); try {
		 * XMLUtil.save(parent.getFuturOwner(), System.out, "UTF-8"); } catch
		 * (TransformerException e) {
		 * 
		 * e.printStackTrace(); } System.out.println("------------------");
		 */

	}

	/**
	 * check if merging is ok
	 * 
	 * @throws InappropriateTypeException
	 * @throws IOException
	 */
	@Test
	public void testMergeSchema() throws Exception {
		String bagName = "test:bagName";
		String seqName = "test:seqName";
		String altName = "test:AltProp";

		String valBagSchem1 = "BagvalSchem1";
		String valBagSchem2 = "BagvalSchem2";

		String valSeqSchem1 = "seqvalSchem1";
		String valSeqSchem2 = "seqvalSchem2";

		String valAltSchem1 = "altvalSchem1";
		String langAltSchem1 = "x-default";

		String valAltSchem2 = "altvalSchem2";
		String langAltSchem2 = "fr-fr";

		XMPSchema schem1 = new XMPSchema(parent, "test",
				"http://www.test.org/schem/");
		schem1.addBagValue(bagName, valBagSchem1);
		schem1.addSequenceValue(seqName, valSeqSchem1);
		schem1.setLanguagePropertyValue(altName, langAltSchem1, valAltSchem1);

		XMPSchema schem2 = new XMPSchema(parent, "test",
				"http://www.test.org/schem/");
		schem2.addBagValue(bagName, valBagSchem2);
		schem2.addSequenceValue(seqName, valSeqSchem2);
		schem2.setLanguagePropertyValue(altName, langAltSchem2, valAltSchem2);

		schem1.merge(schem2);

		// Check if all values are present
		Assert.assertEquals(valAltSchem2, schem1.getLanguagePropertyValue(
				altName, langAltSchem2));
		Assert.assertEquals(valAltSchem1, schem1.getLanguagePropertyValue(
				altName, langAltSchem1));

		List<String> bag = schem1.getBagValueList(bagName);

		Assert.assertTrue(bag.contains(valBagSchem1));
		Assert.assertTrue(bag.contains(valBagSchem2));

		List<String> seq = schem1.getSequenceValueList(seqName);
		Assert.assertTrue(seq.contains(valSeqSchem1));
		Assert.assertTrue(seq.contains(valSeqSchem1));

	}

	@Test
	public void testListAndContainerAccessor() throws Exception {
		String boolname = "bool";
		boolean boolVal = true;
		BooleanType bool = new BooleanType(parent, schem.getLocalPrefix(),
				boolname, boolVal);
		Attribute att = new Attribute(null, "rdf", "test", "vgh");
		schem.setAttribute(att);
		schem.setBooleanProperty(bool);

		Assert.assertEquals(schem.getAllProperties(), schem.getContent()
				.getAllProperties());
		Assert.assertTrue(schem.getAllProperties().contains(bool));
		Assert.assertTrue(schem.getAllAttributes().contains(att));

		Assert.assertEquals(bool, schem.getPropertyAsSimple(boolname));
		Assert.assertEquals(bool, schem.getProperty(schem
				.getLocalPrefixWithSeparator()
				+ boolname));

	}
}
