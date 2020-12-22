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

package org.apache.xmpbox.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.List;
import org.apache.xmpbox.DateConverter;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.XmpConstants;
import org.apache.xmpbox.type.AbstractField;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.Attribute;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.type.BooleanType;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.DateType;
import org.apache.xmpbox.type.IntegerType;
import org.apache.xmpbox.type.TextType;
import org.apache.xmpbox.type.TypeMapping;
import org.junit.jupiter.api.Test;

class XMPSchemaTest
{
    private final XMPMetadata parent = XMPMetadata.createXMPMetadata();
    private final XMPSchema schem = new XMPSchema(parent, "nsURI", "nsSchem");

    /**
     * Check if Bag (Unordered Array) management is ok
     * 
     * @throws IllegalArgumentException
     */
    @Test
    void testBagManagement() throws Exception
    {
        final String bagName = "BAGTEST";
        final String value1 = "valueOne";
        final String value2 = "valueTwo";
        schem.addBagValue(bagName, schem.getMetadata().getTypeMapping().createText(null, "rdf", "li", value1));
        schem.addQualifiedBagValue(bagName, value2);

        final List<String> values = schem.getUnqualifiedBagValueList(bagName);
        assertEquals(value1, values.get(0));
        assertEquals(value2, values.get(1));

        schem.removeUnqualifiedBagValue(bagName, value1);
        final List<String> values2 = schem.getUnqualifiedBagValueList(bagName);
        assertEquals(1, values2.size());
        assertEquals(value2, values2.get(0));

    }

    @Test
    void testArrayList() throws Exception
    {
        final XMPMetadata meta = XMPMetadata.createXMPMetadata();
        final ArrayProperty newSeq = meta.getTypeMapping().createArrayProperty(null, "nsSchem", "seqType", Cardinality.Seq);
        final TypeMapping tm = meta.getTypeMapping();
        final TextType li1 = tm.createText(null, "rdf", "li", "valeur1");
        final TextType li2 = tm.createText(null, "rdf", "li", "valeur2");
        newSeq.getContainer().addProperty(li1);
        newSeq.getContainer().addProperty(li2);
        schem.addProperty(newSeq);
        final List<AbstractField> list = schem.getUnqualifiedArrayList("seqType");
        assertTrue(list.contains(li1));
        assertTrue(list.contains(li2));
    }

    /**
     * Check if Seq (Ordered Array) management is ok
     * 
     * @throws IllegalArgumentException
     * @throws java.io.IOException
     */
    @Test
    void testSeqManagement() throws Exception
    {
        final Calendar date = Calendar.getInstance();
        final BooleanType bool = parent.getTypeMapping().createBoolean(null, "rdf", "li", true);
        final String textVal = "seqValue";
        final String seqName = "SEQNAME";

        schem.addUnqualifiedSequenceDateValue(seqName, date);
        schem.addUnqualifiedSequenceValue(seqName, bool);
        schem.addUnqualifiedSequenceValue(seqName, textVal);

        final List<Calendar> dates = schem.getUnqualifiedSequenceDateValueList(seqName);
        assertEquals(1, dates.size());
        assertEquals(date, dates.get(0));

        final List<String> values = schem.getUnqualifiedSequenceValueList(seqName);
        assertEquals(3, values.size());
        assertEquals(DateConverter.toISO8601(date), values.get(0));
        assertEquals(bool.getStringValue(), values.get(1));
        assertEquals(textVal, values.get(2));

        schem.removeUnqualifiedSequenceDateValue(seqName, date);
        assertEquals(0, schem.getUnqualifiedSequenceDateValueList(seqName).size());

        schem.removeUnqualifiedSequenceValue(seqName, bool);
        schem.removeUnqualifiedSequenceValue(seqName, textVal);

        assertEquals(0, schem.getUnqualifiedSequenceValueList(seqName).size());
    }

    @Test
    void rdfAboutTest()
    {
        assertEquals("",schem.getAboutValue());
        final String about = "about";
        schem.setAboutAsSimple(about);
        assertEquals(about, schem.getAboutValue());
        schem.setAboutAsSimple("");
        assertEquals("",schem.getAboutValue());
        schem.setAboutAsSimple(null);
        assertEquals("",schem.getAboutValue());
    }

    @Test
    void testBadRdfAbout() throws Exception
    {
        assertThrows(BadFieldValueException.class, () -> {
	        schem.setAbout(new Attribute(null, "about", ""));
	    });
    }

    @Test
    void testSetSpecifiedSimpleTypeProperty() throws Exception
    {
        final String prop = "testprop";
        final String val = "value";
        final String val2 = "value2";
        schem.setTextPropertyValueAsSimple(prop, val);
        assertEquals(val, schem.getUnqualifiedTextPropertyValue(prop));
        schem.setTextPropertyValueAsSimple(prop, val2);
        assertEquals(val2, schem.getUnqualifiedTextPropertyValue(prop));
        schem.setTextPropertyValueAsSimple(prop, null);
        assertNull(schem.getUnqualifiedTextProperty(prop));
    }

    @Test
    void testSpecifiedSimplePropertyFormer() throws Exception
    {
        final String prop = "testprop";
        final String val = "value";
        final String val2 = "value2";
        schem.setTextPropertyValueAsSimple(prop, val);
        final TextType text = schem.getMetadata().getTypeMapping().createText(null, schem.getPrefix(), prop, "value2");
        schem.setTextProperty(text);
        assertEquals(val2, schem.getUnqualifiedTextPropertyValue(prop));
        assertEquals(text, schem.getUnqualifiedTextProperty(prop));
    }

    @Test
    void testAsSimpleMethods() throws Exception
    {
        final String bool = "bool";
        final boolean boolVal = true;

        final String date = "date";
        final Calendar dateVal = Calendar.getInstance();

        final String integ = "integer";
        final Integer i = 1;

        final String langprop = "langprop";
        final String lang = "x-default";
        final String langVal = "langVal";

        final String bagprop = "bagProp";
        final String bagVal = "bagVal";

        final String seqprop = "SeqProp";
        final String seqPropVal = "seqval";

        final String seqdate = "SeqDate";

        final String prefSchem = "";

        schem.setBooleanPropertyValueAsSimple(bool, boolVal);
        schem.setDatePropertyValueAsSimple(date, dateVal);
        schem.setIntegerPropertyValueAsSimple(integ, i);
        schem.setUnqualifiedLanguagePropertyValue(langprop, lang, langVal);
        schem.addBagValueAsSimple(bagprop, bagVal);
        schem.addUnqualifiedSequenceValue(seqprop, seqPropVal);
        schem.addSequenceDateValueAsSimple(seqdate, dateVal);

        assertEquals(Boolean.valueOf(boolVal), schem.getBooleanProperty(prefSchem + bool).getValue());
        assertEquals(dateVal, schem.getDateProperty(prefSchem + date).getValue());
        assertEquals("" + i, schem.getIntegerProperty(prefSchem + integ).getStringValue());
        assertEquals(langVal, schem.getUnqualifiedLanguagePropertyValue(langprop, lang));
        assertTrue(schem.getUnqualifiedBagValueList(bagprop).contains(bagVal));
        assertTrue(schem.getUnqualifiedSequenceValueList(seqprop).contains(seqPropVal));
        assertTrue(schem.getUnqualifiedSequenceDateValueList(seqdate).contains(dateVal));
        assertTrue(schem.getUnqualifiedLanguagePropertyLanguagesValue(langprop).contains(lang));

        assertEquals(boolVal, schem.getBooleanPropertyValueAsSimple(bool).booleanValue());
        assertEquals(dateVal, schem.getDatePropertyValueAsSimple(date));
        assertEquals(i, schem.getIntegerPropertyValueAsSimple(integ));
        assertEquals(langVal, schem.getUnqualifiedLanguagePropertyValue(langprop, lang));
        assertTrue(schem.getUnqualifiedBagValueList(bagprop).contains(bagVal));
        assertTrue(schem.getUnqualifiedSequenceValueList(seqprop).contains(seqPropVal));
        assertTrue(schem.getUnqualifiedSequenceDateValueList(seqdate).contains(dateVal));
        assertTrue(schem.getUnqualifiedLanguagePropertyLanguagesValue(langprop).contains(lang));
    }

    /**
     * Test All common simple properties management in XMPSchema
     * 
     * @throws IllegalArgumentException
     * @throws BadFieldValueException
     */
    @Test
    void testProperties() throws Exception
    {

        assertEquals("nsURI", schem.getNamespace());

        // In real cases, rdf ns will be declared before !
        schem.addNamespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");

        final String aboutVal = "aboutTest";
        schem.setAboutAsSimple(aboutVal);
        assertEquals(aboutVal, schem.getAboutValue());

        final Attribute about = new Attribute(XmpConstants.RDF_NAMESPACE, "about", "YEP");
        schem.setAbout(about);
        assertEquals(about, schem.getAboutAttribute());

        final String textProp = "textProp";
        final String textPropVal = "TextPropTest";
        schem.setTextPropertyValue(textProp, textPropVal);
        assertEquals(textPropVal, schem.getUnqualifiedTextPropertyValue(textProp));

        final TextType text = parent.getTypeMapping().createText(null, "nsSchem", "textType", "GRINGO");
        schem.setTextProperty(text);
        assertEquals(text, schem.getUnqualifiedTextProperty("textType"));

        final Calendar dateVal = Calendar.getInstance();
        final String date = "nsSchem:dateProp";
        schem.setDatePropertyValue(date, dateVal);
        assertEquals(dateVal, schem.getDatePropertyValue(date));

        final DateType dateType = parent.getTypeMapping().createDate(null, "nsSchem", "dateType", Calendar.getInstance());
        schem.setDateProperty(dateType);
        assertEquals(dateType, schem.getDateProperty("dateType"));

        final String bool = "nsSchem:booleanTestProp";
        final Boolean boolVal = false;
        schem.setBooleanPropertyValue(bool, boolVal);
        assertEquals(boolVal, schem.getBooleanPropertyValue(bool));

        final BooleanType boolType = parent.getTypeMapping().createBoolean(null, "nsSchem", "boolType", false);
        schem.setBooleanProperty(boolType);
        assertEquals(boolType, schem.getBooleanProperty("boolType"));

        final String intProp = "nsSchem:IntegerTestProp";
        final Integer intPropVal = 5;
        schem.setIntegerPropertyValue(intProp, intPropVal);
        assertEquals(intPropVal, schem.getIntegerPropertyValue(intProp));

        final IntegerType intType = parent.getTypeMapping().createInteger(null, "nsSchem", "intType", 5);
        schem.setIntegerProperty(intType);
        assertEquals(intType, schem.getIntegerProperty("intType"));

        // Check bad type verification
        boolean ok = false;
        try
        {
            schem.getIntegerProperty("boolType");
        }
        catch (final BadFieldValueException e)
        {
            ok = true;
        }
        assertTrue(ok);
        ok = false;
        try
        {
            schem.getUnqualifiedTextProperty("intType");
        }
        catch (final BadFieldValueException e)
        {
            ok = true;
        }
        assertTrue(ok);
        ok = false;
        try
        {
            schem.getDateProperty("textType");
        }
        catch (final BadFieldValueException e)
        {
            ok = true;
        }
        assertTrue(ok);
        ok = false;
        try
        {
            schem.getBooleanProperty("dateType");
        }
        catch (final BadFieldValueException e)
        {
            ok = true;
        }

    }

    @Test
    void testAltProperties() throws Exception
    {
        final String altProp = "AltProp";

        final String defaultLang = "x-default";
        final String defaultVal = "Default Language";

        final String usLang = "en-us";
        final String usVal = "American Language";

        final String frLang = "fr-fr";
        String frVal = "Lang française";

        schem.setUnqualifiedLanguagePropertyValue(altProp, usLang, usVal);
        schem.setUnqualifiedLanguagePropertyValue(altProp, defaultLang, defaultVal);
        schem.setUnqualifiedLanguagePropertyValue(altProp, frLang, frVal);

        assertEquals(defaultVal, schem.getUnqualifiedLanguagePropertyValue(altProp, defaultLang));
        assertEquals(frVal, schem.getUnqualifiedLanguagePropertyValue(altProp, frLang));
        assertEquals(usVal, schem.getUnqualifiedLanguagePropertyValue(altProp, usLang));

        List<String> languages = schem.getUnqualifiedLanguagePropertyLanguagesValue(altProp);
        // default language must be in first place
        assertEquals(defaultLang, languages.get(0));

        assertTrue(languages.contains(usLang));
        assertTrue(languages.contains(frLang));

        // Test replacement/removal

        frVal = "Langue française";

        schem.setUnqualifiedLanguagePropertyValue(altProp, frLang, frVal);
        assertEquals(frVal, schem.getUnqualifiedLanguagePropertyValue(altProp, frLang));

        schem.setUnqualifiedLanguagePropertyValue(altProp, frLang, null);
        languages = schem.getUnqualifiedLanguagePropertyLanguagesValue(altProp);
        assertFalse(languages.contains(frLang));
        schem.setUnqualifiedLanguagePropertyValue(altProp, frLang, frVal);

    }

    /**
     * check if merging is ok
     * 
     * @throws IllegalArgumentException
     * @throws java.io.IOException
     */
    @Test
    void testMergeSchema() throws Exception
    {
        final String bagName = "bagName";
        final String seqName = "seqName";
        final String altName = "AltProp";

        final String valBagSchem1 = "BagvalSchem1";
        final String valBagSchem2 = "BagvalSchem2";

        final String valSeqSchem1 = "seqvalSchem1";
        final String valSeqSchem2 = "seqvalSchem2";

        final String valAltSchem1 = "altvalSchem1";
        final String langAltSchem1 = "x-default";

        final String valAltSchem2 = "altvalSchem2";
        final String langAltSchem2 = "fr-fr";

        final XMPSchema schem1 = new XMPSchema(parent, "http://www.test.org/schem/", "test");
        schem1.addQualifiedBagValue(bagName, valBagSchem1);
        schem1.addUnqualifiedSequenceValue(seqName, valSeqSchem1);
        schem1.setUnqualifiedLanguagePropertyValue(altName, langAltSchem1, valAltSchem1);

        final XMPSchema schem2 = new XMPSchema(parent, "http://www.test.org/schem/", "test");
        schem2.addQualifiedBagValue(bagName, valBagSchem2);
        schem2.addUnqualifiedSequenceValue(seqName, valSeqSchem2);
        schem2.setUnqualifiedLanguagePropertyValue(altName, langAltSchem2, valAltSchem2);

        schem1.merge(schem2);

        // Check if all values are present
        assertEquals(valAltSchem2, schem1.getUnqualifiedLanguagePropertyValue(altName, langAltSchem2));
        assertEquals(valAltSchem1, schem1.getUnqualifiedLanguagePropertyValue(altName, langAltSchem1));

        final List<String> bag = schem1.getUnqualifiedBagValueList(bagName);

        assertTrue(bag.contains(valBagSchem1));
        assertTrue(bag.contains(valBagSchem2));

        final List<String> seq = schem1.getUnqualifiedSequenceValueList(seqName);
        assertTrue(seq.contains(valSeqSchem1));
        assertTrue(seq.contains(valSeqSchem1));

    }

    @Test
    void testListAndContainerAccessor() throws Exception
    {
        final String boolname = "bool";
        final boolean boolVal = true;
        final BooleanType bool = parent.getTypeMapping().createBoolean(null, schem.getPrefix(), boolname, boolVal);
        final Attribute att = new Attribute(XmpConstants.RDF_NAMESPACE, "test", "vgh");
        schem.setAttribute(att);
        schem.setBooleanProperty(bool);

        assertEquals(schem.getAllProperties(), schem.getAllProperties());
        assertTrue(schem.getAllProperties().contains(bool));
        assertTrue(schem.getAllAttributes().contains(att));

        assertEquals(bool, schem.getProperty(boolname));

    }
}
