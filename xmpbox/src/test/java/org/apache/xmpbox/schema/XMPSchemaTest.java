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

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import junit.framework.Assert;

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
import org.junit.Before;
import org.junit.Test;

public class XMPSchemaTest
{

    protected XMPMetadata parent;
    protected XMPSchema schem;

    @Before
    public void resetDocument() throws Exception
    {
        parent = XMPMetadata.createXMPMetadata();
        schem = new XMPSchema(parent, "nsURI", "nsSchem");

    }

    /**
     * Check if Bag (Unordered Array) management is ok
     * 
     * @throws InappropriateTypeException
     */
    @Test
    public void testBagManagement() throws Exception
    {
        String bagName = "BAGTEST";
        String value1 = "valueOne";
        String value2 = "valueTwo";
        schem.addBagValue(bagName, schem.getMetadata().getTypeMapping().createText(null, "rdf", "li", value1));
        schem.addQualifiedBagValue(bagName, value2);

        List<String> values = schem.getUnqualifiedBagValueList(bagName);
        Assert.assertEquals(value1, values.get(0));
        Assert.assertEquals(value2, values.get(1));

        schem.removeUnqualifiedBagValue(bagName, value1);
        List<String> values2 = schem.getUnqualifiedBagValueList(bagName);
        Assert.assertEquals(1, values2.size());
        Assert.assertEquals(value2, values2.get(0));

    }

    @Test
    public void testArrayList() throws Exception
    {
        XMPMetadata meta = XMPMetadata.createXMPMetadata();
        ArrayProperty newSeq = meta.getTypeMapping().createArrayProperty(null, "nsSchem", "seqType", Cardinality.Seq);
        TypeMapping tm = meta.getTypeMapping();
        TextType li1 = tm.createText(null, "rdf", "li", "valeur1");
        TextType li2 = tm.createText(null, "rdf", "li", "valeur2");
        newSeq.getContainer().addProperty(li1);
        newSeq.getContainer().addProperty(li2);
        schem.addProperty(newSeq);
        List<AbstractField> list = schem.getUnqualifiedArrayList("seqType");
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
    public void testSeqManagement() throws Exception
    {
        Calendar date = Calendar.getInstance();
        BooleanType bool = parent.getTypeMapping().createBoolean(null, "rdf", "li", true);
        String textVal = "seqValue";
        String seqName = "SEQNAME";

        schem.addUnqualifiedSequenceDateValue(seqName, date);
        schem.addUnqualifiedSequenceValue(seqName, bool);
        schem.addUnqualifiedSequenceValue(seqName, textVal);

        List<Calendar> dates = schem.getUnqualifiedSequenceDateValueList(seqName);
        Assert.assertEquals(1, dates.size());
        Assert.assertEquals(date, dates.get(0));

        List<String> values = schem.getUnqualifiedSequenceValueList(seqName);
        Assert.assertEquals(3, values.size());
        Assert.assertEquals(DateConverter.toISO8601(date), values.get(0));
        Assert.assertEquals(bool.getStringValue(), values.get(1));
        Assert.assertEquals(textVal, values.get(2));

        schem.removeUnqualifiedSequenceDateValue(seqName, date);
        Assert.assertEquals(0, schem.getUnqualifiedSequenceDateValueList(seqName).size());

        schem.removeUnqualifiedSequenceValue(seqName, bool);
        schem.removeUnqualifiedSequenceValue(seqName, textVal);

        Assert.assertEquals(0, schem.getUnqualifiedSequenceValueList(seqName).size());
    }

    @Test
    public void rdfAboutTest()
    {
        Assert.assertEquals("",schem.getAboutValue());
        String about = "about";
        schem.setAboutAsSimple(about);
        Assert.assertEquals(about, schem.getAboutValue());
        schem.setAboutAsSimple("");
        Assert.assertEquals("",schem.getAboutValue());
        schem.setAboutAsSimple(null);
        Assert.assertEquals("",schem.getAboutValue());
    }

    @Test(expected = BadFieldValueException.class)
    public void testBadRdfAbout() throws Exception
    {
        schem.setAbout(new Attribute(null, "about", ""));
    }

    @Test
    public void testSetSpecifiedSimpleTypeProperty() throws Exception
    {
        String prop = "testprop";
        String val = "value";
        String val2 = "value2";
        schem.setTextPropertyValueAsSimple(prop, val);
        Assert.assertEquals(val, schem.getUnqualifiedTextPropertyValue(prop));
        schem.setTextPropertyValueAsSimple(prop, val2);
        Assert.assertEquals(val2, schem.getUnqualifiedTextPropertyValue(prop));
        schem.setTextPropertyValueAsSimple(prop, null);
        Assert.assertNull(schem.getUnqualifiedTextProperty(prop));
    }

    @Test
    public void testSpecifiedSimplePropertyFormer() throws Exception
    {
        String prop = "testprop";
        String val = "value";
        String val2 = "value2";
        schem.setTextPropertyValueAsSimple(prop, val);
        TextType text = schem.getMetadata().getTypeMapping().createText(null, schem.getPrefix(), prop, "value2");
        schem.setTextProperty(text);
        Assert.assertEquals(val2, schem.getUnqualifiedTextPropertyValue(prop));
        Assert.assertEquals(text, schem.getUnqualifiedTextProperty(prop));
    }

    @Test
    public void testAsSimpleMethods() throws Exception
    {
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

        String prefSchem = "";

        schem.setBooleanPropertyValueAsSimple(bool, boolVal);
        schem.setDatePropertyValueAsSimple(date, dateVal);
        schem.setIntegerPropertyValueAsSimple(integ, i);
        schem.setUnqualifiedLanguagePropertyValue(langprop, lang, langVal);
        schem.addBagValueAsSimple(bagprop, bagVal);
        schem.addUnqualifiedSequenceValue(seqprop, seqPropVal);
        schem.addSequenceDateValueAsSimple(seqdate, dateVal);

        Assert.assertEquals(Boolean.valueOf(boolVal), schem.getBooleanProperty(prefSchem + bool).getValue());
        Assert.assertEquals(dateVal, schem.getDateProperty(prefSchem + date).getValue());
        Assert.assertEquals("" + i, schem.getIntegerProperty(prefSchem + integ).getStringValue());
        Assert.assertEquals(langVal, schem.getUnqualifiedLanguagePropertyValue(langprop, lang));
        Assert.assertTrue(schem.getUnqualifiedBagValueList(bagprop).contains(bagVal));
        Assert.assertTrue(schem.getUnqualifiedSequenceValueList(seqprop).contains(seqPropVal));
        Assert.assertTrue(schem.getUnqualifiedSequenceDateValueList(seqdate).contains(dateVal));
        Assert.assertTrue(schem.getUnqualifiedLanguagePropertyLanguagesValue(langprop).contains(lang));

        Assert.assertEquals(boolVal, schem.getBooleanPropertyValueAsSimple(bool).booleanValue());
        Assert.assertEquals(dateVal, schem.getDatePropertyValueAsSimple(date));
        Assert.assertEquals(i, schem.getIntegerPropertyValueAsSimple(integ));
        Assert.assertEquals(langVal, schem.getUnqualifiedLanguagePropertyValue(langprop, lang));
        Assert.assertTrue(schem.getUnqualifiedBagValueList(bagprop).contains(bagVal));
        Assert.assertTrue(schem.getUnqualifiedSequenceValueList(seqprop).contains(seqPropVal));
        Assert.assertTrue(schem.getUnqualifiedSequenceDateValueList(seqdate).contains(dateVal));
        Assert.assertTrue(schem.getUnqualifiedLanguagePropertyLanguagesValue(langprop).contains(lang));
    }

    /**
     * Test All common simple properties management in XMPSchema
     * 
     * @throws InappropriateTypeException
     * @throws BadFieldValueException
     */
    @Test
    public void testProperties() throws Exception
    {

        Assert.assertEquals("nsURI", schem.getNamespace());

        // In real cases, rdf ns will be declared before !
        schem.addNamespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");

        String aboutVal = "aboutTest";
        schem.setAboutAsSimple(aboutVal);
        Assert.assertEquals(aboutVal, schem.getAboutValue());

        Attribute about = new Attribute(XmpConstants.RDF_NAMESPACE, "about", "YEP");
        schem.setAbout(about);
        Assert.assertEquals(about, schem.getAboutAttribute());

        String textProp = "textProp";
        String textPropVal = "TextPropTest";
        schem.setTextPropertyValue(textProp, textPropVal);
        Assert.assertEquals(textPropVal, schem.getUnqualifiedTextPropertyValue(textProp));

        TextType text = parent.getTypeMapping().createText(null, "nsSchem", "textType", "GRINGO");
        schem.setTextProperty(text);
        Assert.assertEquals(text, schem.getUnqualifiedTextProperty("textType"));

        Calendar dateVal = Calendar.getInstance();
        String date = "nsSchem:dateProp";
        schem.setDatePropertyValue(date, dateVal);
        Assert.assertEquals(dateVal, schem.getDatePropertyValue(date));

        DateType dateType = parent.getTypeMapping().createDate(null, "nsSchem", "dateType", Calendar.getInstance());
        schem.setDateProperty(dateType);
        Assert.assertEquals(dateType, schem.getDateProperty("dateType"));

        String bool = "nsSchem:booleanTestProp";
        Boolean boolVal = false;
        schem.setBooleanPropertyValue(bool, boolVal);
        Assert.assertEquals(boolVal, schem.getBooleanPropertyValue(bool));

        BooleanType boolType = parent.getTypeMapping().createBoolean(null, "nsSchem", "boolType", false);
        schem.setBooleanProperty(boolType);
        Assert.assertEquals(boolType, schem.getBooleanProperty("boolType"));

        String intProp = "nsSchem:IntegerTestProp";
        Integer intPropVal = 5;
        schem.setIntegerPropertyValue(intProp, intPropVal);
        Assert.assertEquals(intPropVal, schem.getIntegerPropertyValue(intProp));

        IntegerType intType = parent.getTypeMapping().createInteger(null, "nsSchem", "intType", 5);
        schem.setIntegerProperty(intType);
        Assert.assertEquals(intType, schem.getIntegerProperty("intType"));

        // Check bad type verification
        boolean ok = false;
        try
        {
            schem.getIntegerProperty("boolType");
        }
        catch (IllegalArgumentException e)
        {
            ok = true;
        }
        Assert.assertEquals(true, ok);
        ok = false;
        try
        {
            schem.getUnqualifiedTextProperty("intType");
        }
        catch (IllegalArgumentException e)
        {
            ok = true;
        }
        Assert.assertEquals(true, ok);
        ok = false;
        try
        {
            schem.getDateProperty("textType");
        }
        catch (IllegalArgumentException e)
        {
            ok = true;
        }
        Assert.assertEquals(true, ok);
        ok = false;
        try
        {
            schem.getBooleanProperty("dateType");
        }
        catch (IllegalArgumentException e)
        {
            ok = true;
        }

    }

    @Test
    public void testAltProperties() throws Exception
    {
        String altProp = "AltProp";

        String defaultLang = "x-default";
        String defaultVal = "Default Language";

        String usLang = "en-us";
        String usVal = "American Language";

        String frLang = "fr-fr";
        String frVal = "Lang française";

        schem.setUnqualifiedLanguagePropertyValue(altProp, usLang, usVal);
        schem.setUnqualifiedLanguagePropertyValue(altProp, defaultLang, defaultVal);
        schem.setUnqualifiedLanguagePropertyValue(altProp, frLang, frVal);

        Assert.assertEquals(defaultVal, schem.getUnqualifiedLanguagePropertyValue(altProp, defaultLang));
        Assert.assertEquals(frVal, schem.getUnqualifiedLanguagePropertyValue(altProp, frLang));
        Assert.assertEquals(usVal, schem.getUnqualifiedLanguagePropertyValue(altProp, usLang));

        List<String> languages = schem.getUnqualifiedLanguagePropertyLanguagesValue(altProp);
        // default language must be in first place
        Assert.assertEquals(defaultLang, languages.get(0));

        Assert.assertTrue(languages.contains(usLang));
        Assert.assertTrue(languages.contains(frLang));

        // Test replacement/removal

        frVal = "Langue française";

        schem.setUnqualifiedLanguagePropertyValue(altProp, frLang, frVal);
        Assert.assertEquals(frVal, schem.getUnqualifiedLanguagePropertyValue(altProp, frLang));

        schem.setUnqualifiedLanguagePropertyValue(altProp, frLang, null);
        languages = schem.getUnqualifiedLanguagePropertyLanguagesValue(altProp);
        Assert.assertFalse(languages.contains(frLang));
        schem.setUnqualifiedLanguagePropertyValue(altProp, frLang, frVal);

    }

    /**
     * check if merging is ok
     * 
     * @throws InappropriateTypeException
     * @throws IOException
     */
    @Test
    public void testMergeSchema() throws Exception
    {
        String bagName = "bagName";
        String seqName = "seqName";
        String qseqName = "test:" + seqName;
        String altName = "AltProp";
        String qaltName = "test:" + altName;

        String valBagSchem1 = "BagvalSchem1";
        String valBagSchem2 = "BagvalSchem2";

        String valSeqSchem1 = "seqvalSchem1";
        String valSeqSchem2 = "seqvalSchem2";

        String valAltSchem1 = "altvalSchem1";
        String langAltSchem1 = "x-default";

        String valAltSchem2 = "altvalSchem2";
        String langAltSchem2 = "fr-fr";

        XMPSchema schem1 = new XMPSchema(parent, "http://www.test.org/schem/", "test");
        schem1.addQualifiedBagValue(bagName, valBagSchem1);
        schem1.addUnqualifiedSequenceValue(seqName, valSeqSchem1);
        schem1.setUnqualifiedLanguagePropertyValue(altName, langAltSchem1, valAltSchem1);

        XMPSchema schem2 = new XMPSchema(parent, "http://www.test.org/schem/", "test");
        schem2.addQualifiedBagValue(bagName, valBagSchem2);
        schem2.addUnqualifiedSequenceValue(seqName, valSeqSchem2);
        schem2.setUnqualifiedLanguagePropertyValue(altName, langAltSchem2, valAltSchem2);

        schem1.merge(schem2);

        // Check if all values are present
        Assert.assertEquals(valAltSchem2, schem1.getUnqualifiedLanguagePropertyValue(altName, langAltSchem2));
        Assert.assertEquals(valAltSchem1, schem1.getUnqualifiedLanguagePropertyValue(altName, langAltSchem1));

        List<String> bag = schem1.getUnqualifiedBagValueList(bagName);

        Assert.assertTrue(bag.contains(valBagSchem1));
        Assert.assertTrue(bag.contains(valBagSchem2));

        List<String> seq = schem1.getUnqualifiedSequenceValueList(seqName);
        Assert.assertTrue(seq.contains(valSeqSchem1));
        Assert.assertTrue(seq.contains(valSeqSchem1));

    }

    @Test
    public void testListAndContainerAccessor() throws Exception
    {
        String boolname = "bool";
        boolean boolVal = true;
        BooleanType bool = parent.getTypeMapping().createBoolean(null, schem.getPrefix(), boolname, boolVal);
        Attribute att = new Attribute(XmpConstants.RDF_NAMESPACE, "test", "vgh");
        schem.setAttribute(att);
        schem.setBooleanProperty(bool);

        Assert.assertEquals(schem.getAllProperties(), schem.getAllProperties());
        Assert.assertTrue(schem.getAllProperties().contains(bool));
        Assert.assertTrue(schem.getAllAttributes().contains(att));

        Assert.assertEquals(bool, schem.getProperty(boolname));

    }
}
