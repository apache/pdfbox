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

package org.apache.xmpbox.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.List;

import org.apache.xmpbox.XMPMetadata;
import org.junit.jupiter.api.Test;

/**
 * Test MetaData Objects for simple properties
 * 
 * @author a183132
 * 
 */
class TestSimpleMetadataProperties
{

    private final XMPMetadata parent = XMPMetadata.createXMPMetadata();

    /**
     * Check the detection of a bad type
     * 
     * @throws IllegalArgumentException
     */
    @Test
    void testBooleanBadTypeDetection()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            new BooleanType(parent, null, "test", "boolean", "Not a Boolean");
        });
    }

    /**
     * Check the detection of a bad type
     * 
     * @throws IllegalArgumentException
     */
    @Test
    void testDateBadTypeDetection()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            new DateType(parent, null, "test", "date", "Bad Date");
        });
    }

    /**
     * Check the detection of a bad type
     * 
     * @throws IllegalArgumentException
     */
    @Test
    void testIntegerBadTypeDetection()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            new IntegerType(parent, null, "test", "integer", "Not an int");
        });
    }

    /**
     * Check the detection of a bad type
     * 
     * @throws IllegalArgumentException
     */
    @Test
    void testRealBadTypeDetection() throws Exception
    {
        assertThrows(IllegalArgumentException.class, () -> {
            new RealType(parent, null, "test", "real", "Not a real");
        });
    }

    /**
     * Check the detection of a bad type
     * 
     * @throws IllegalArgumentException
     */
    @Test
    void testTextBadTypeDetection() throws Exception
    {
        Calendar calendar = Calendar.getInstance();
        assertThrows(IllegalArgumentException.class, () -> {
            new TextType(parent, null, "test", "text", calendar);
        });   
    }

    /**
     * Check if information between objects and the element generated are equals
     * 
     * @throws Exception
     */
    @Test
    void testElementAndObjectSynchronization() throws Exception
    {
        boolean boolv = true;
        Calendar datev = Calendar.getInstance();
        int integerv = 1;
        float realv = Float.parseFloat("1.69");
        String textv = "TEXTCONTENT";
        BooleanType bool = parent.getTypeMapping().createBoolean(null, "test", "boolean", boolv);
        DateType date = parent.getTypeMapping().createDate(null, "test", "date", datev);
        IntegerType integer = parent.getTypeMapping().createInteger(null, "test", "integer", integerv);
        RealType real = parent.getTypeMapping().createReal(null, "test", "real", realv);
        TextType text = parent.getTypeMapping().createText(null, "test", "text", textv);

        assertEquals(boolv, bool.getValue());
        assertEquals(datev, date.getValue());
        assertEquals(Integer.valueOf(integerv), integer.getValue());
        assertEquals(realv, real.getValue(), 0);
        assertEquals(textv, text.getStringValue());

    }

    /**
     * Check the creation from string attributes
     * 
     * @throws Exception
     */
    @Test
    void testCreationFromString() throws Exception
    {
        String boolv = "False";
        String datev = "2010-03-22T14:33:11+01:00";
        String integerv = "10";
        String realv = "1.92";
        String textv = "text";

        BooleanType bool = new BooleanType(parent, null, "test", "boolean", boolv);
        DateType date = new DateType(parent, null, "test", "date", datev);
        IntegerType integer = new IntegerType(parent, null, "test", "integer", integerv);
        RealType real = new RealType(parent, null, "test", "real", realv);
        TextType text = new TextType(parent, null, "test", "text", textv);

        assertEquals(boolv, bool.getStringValue());
        assertEquals(datev, date.getStringValue());
        assertEquals(integerv, integer.getStringValue());
        assertEquals(realv, real.getStringValue());
        assertEquals(textv, text.getStringValue());
    }

    /**
     * Check creation when a namespace is specified
     * 
     * @throws Exception
     */
    @Test
    void testObjectCreationWithNamespace() throws Exception
    {
        String ns = "http://www.test.org/pdfa/";
        BooleanType bool = parent.getTypeMapping().createBoolean(ns, "test", "boolean", true);
        DateType date = parent.getTypeMapping().createDate(ns, "test", "date", Calendar.getInstance());
        IntegerType integer = parent.getTypeMapping().createInteger(ns, "test", "integer", 1);
        RealType real = parent.getTypeMapping().createReal(ns, "test", "real", (float) 1.6);
        TextType text = parent.getTypeMapping().createText(ns, "test", "text", "TEST");

        assertEquals(ns, bool.getNamespace());
        assertEquals(ns, date.getNamespace());
        assertEquals(ns, integer.getNamespace());
        assertEquals(ns, real.getNamespace());
        assertEquals(ns, text.getNamespace());

    }

    /**
     * Throw IllegalArgumentException
     * 
     * @throws IllegalArgumentException
     */
    @Test
    void testExceptionWithCause() throws Exception
    {
        Throwable throwable = new Throwable();
        assertThrows(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException("TEST", throwable);
        });
    }

    /**
     * Check if attributes management works
     * 
     * @throws Exception
     */
    @Test
    void testAttribute() throws Exception
    {

        IntegerType integer = new IntegerType(parent, null, "test", "integer", 1);
        Attribute value = new Attribute("http://www.test.org/test/", "value1", "StringValue1");
        Attribute value2 = new Attribute("http://www.test.org/test/", "value2", "StringValue2");

        integer.setAttribute(value);

        // System.out.println(value.getQualifiedName());

        assertEquals(value, integer.getAttribute(value.getName()));
        assertTrue(integer.containsAttribute(value.getName()));

        // Replacement check

        integer.setAttribute(value2);
        assertEquals(value2, integer.getAttribute(value2.getName()));

        integer.removeAttribute(value2.getName());
        assertFalse(integer.containsAttribute(value2.getName()));

        // Attribute with namespace Creation checking
        Attribute valueNS = new Attribute("http://www.tefst2.org/test/", "value2", "StringValue.2");
        integer.setAttribute(valueNS);
        Attribute valueNS2 = new Attribute("http://www.test2.org/test/", "value2", "StringValueTwo");
        integer.setAttribute(valueNS2);

        List<Attribute> atts = integer.getAllAttributes();
        /*
         * for (Attribute attribute : atts) { System.out.println(attribute.getLocalName ()+" :"+attribute.getValue()); }
         */
        assertFalse(atts.contains(valueNS));
        assertTrue(atts.contains(valueNS2));

    }

}
