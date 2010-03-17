/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jempbox.xmp;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.apache.jempbox.impl.DateConverter;
import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchema;
import org.w3c.dom.Element;

/**
 * Tests for the XMPSchema class.
 * 
 * @author $Author: benlitchfield $
 * @version $Revision: 1.2 $ ($Date: 2007/02/28 02:30:30 $)
 * 
 */
public class XMPSchemaTest extends TestCase
{
    /**
     * Check whether the schema correctly sets the rdf:Description element.
     * 
     * @throws IOException Signals an error with the XMP processing.
     * @throws ParserConfigurationException Signals an error with the XMP processing.
     */
    public void testRDFDescription() throws IOException, ParserConfigurationException
    {
        // Check constructor using an element
        XMPMetadata xmp = new XMPMetadata();
        XMPSchema basic = new XMPSchema(xmp, "test", "http://test.com/test");

        assertNotNull(basic.getElement());
        assertEquals("rdf:Description", basic.getElement().getTagName());

        // Then Check using the Document Builder Factory
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Element e = builder.newDocument().createElement("rdf:Description");

        XMPSchema schema = new XMPSchema(e, "test");

        assertEquals(e, schema.getElement());
        assertEquals("rdf:Description", schema.getElement().getTagName());
    }

    /**
     * Test that text properties are correctly handeled.
     * 
     * @throws IOException Signals an error with the XMP processing.
     */
    public void testTextProperty() throws IOException
    {
        XMPMetadata xmp = new XMPMetadata();
        XMPSchema schema = new XMPSchema(xmp, "test", "http://test.com/test");

        schema.setTextProperty("test:title",
                "The advanced Flux-Compensation for Delawney-Separation");

        Element e = schema.getElement();
        assertEquals("The advanced Flux-Compensation for Delawney-Separation",
                e.getAttribute("test:title"));

        assertEquals("The advanced Flux-Compensation for Delawney-Separation",
                schema.getTextProperty("test:title"));

        schema.setTextProperty("test:title",
                "Bacon's Dictum and Healey's Heaven");

        e = schema.getElement();
        assertEquals("Bacon's Dictum and Healey's Heaven", e
                .getAttribute("test:title"));

        assertEquals("Bacon's Dictum and Healey's Heaven", schema
                .getTextProperty("test:title"));

        schema
                .setTextProperty(
                        "test:abstract",
                        "   The abstract\n can go \n \n on several" +
                        " \n lines with \n many \n\n empty ones in \n between.");
        assertEquals(
                "   The abstract\n can go \n \n on several" +
                " \n lines with \n many \n\n empty ones in \n between.",
                schema.getTextProperty("test:abstract"));
    }

    /**
     * Test that integer properties are correctly handled.
     * 
     * @throws IOException Signals an error with the XMP processing.
     */
    public void testIntegerProperty() throws IOException
    {
        XMPMetadata xmp = new XMPMetadata();
        XMPSchema schema = new XMPSchema(xmp, "test", "http://test.com/test");
        
        schema.setIntegerProperty("test:intvalue", new Integer(14));
        
        Element e = schema.getElement();
        assertEquals("14", e.getAttribute("test:intvalue"));
        
        assertEquals(new Integer(14),schema.getIntegerProperty("test:intvalue"));
        
        schema.setIntegerProperty("test:intvalue",new Integer(16));
        
        e = schema.getElement();
        assertEquals("16", e.getAttribute("test:intvalue"));
        
        assertEquals(new Integer(16), schema.getIntegerProperty("test:intvalue"));
    }

    /**
     * Check bag properties.
     * 
     * @throws IOException Signals an error with the XMP processing.
     */
    public void testBags() throws IOException
    {

        XMPMetadata xmp = new XMPMetadata();
        XMPSchema schema = new XMPSchema(xmp, "test", "http://test.com/test");

        schema.addBagValue("author", "Tom DeMarco");
        schema.addBagValue("author", "Kent Beck");
        {

            List<String> l = schema.getBagList("author");

            assertEquals(2, l.size());

            assertTrue(l.get(0).equals("Tom DeMarco")
                    || l.get(1).equals("Tom DeMarco"));
            assertTrue(l.get(0).equals("Kent Beck")
                    || l.get(1).equals("Kent Beck"));
        }
        {
            schema.removeBagValue("author", "Kent Beck");
            List<String> l = schema.getBagList("author");
            assertEquals(1, l.size());
            assertTrue(l.get(0).equals("Tom DeMarco"));
        }
        { // Already removed
            schema.removeBagValue("author", "Kent Beck");
            List<String> l = schema.getBagList("author");
            assertEquals(1, l.size());
            assertTrue(l.get(0).equals("Tom DeMarco"));
        }
        { // Duplicates allowed!
            schema.addBagValue("author", "Tom DeMarco");
            List<String> l = schema.getBagList("author");
            assertEquals(2, l.size());
            assertTrue(l.get(0).equals("Tom DeMarco"));
            assertTrue(l.get(1).equals("Tom DeMarco"));
        }
        { // Removes both
            schema.removeBagValue("author", "Tom DeMarco");
            List<String> l = schema.getBagList("author");
            assertEquals(0, l.size());
        }
    }

    /**
     * Test adding and removing from a sequence list.
     * 
     * @throws IOException Signals an error with the XMP processing.
     */
    public void testSeqList() throws IOException
    {
        XMPMetadata xmp = new XMPMetadata();
        XMPSchema schema = new XMPSchema(xmp, "test", "http://test.com/test");

        schema.addSequenceValue("author", "Tom DeMarco");
        schema.addSequenceValue("author", "Kent Beck");
        {

            List<String> l = schema.getSequenceList("author");

            assertEquals(2, l.size());

            assertEquals("Tom DeMarco", l.get(0));
            assertEquals("Kent Beck", l.get(1));
        }
        {
            schema.removeSequenceValue("author", "Tom DeMarco");
            List<String> l = schema.getSequenceList("author");
            assertEquals(1, l.size());
            assertTrue(l.get(0).equals("Kent Beck"));
        }
        { // Already removed
            schema.removeSequenceValue("author", "Tom DeMarco");
            List<String> l = schema.getSequenceList("author");
            assertEquals(1, l.size());
            assertTrue(l.get(0).equals("Kent Beck"));
        }
        { // Duplicates allowed!
            schema.addSequenceValue("author", "Kent Beck");
            List<String> l = schema.getSequenceList("author");
            assertEquals(2, l.size());
            assertTrue(l.get(0).equals("Kent Beck"));
            assertTrue(l.get(1).equals("Kent Beck"));
        }
        { // Remvoes all
            schema.removeSequenceValue("author", "Kent Beck");
            List<String> l = schema.getSequenceList("author");
            assertEquals(0, l.size());
        }
    }

    /**
     * Compares two dates.
     * 
     * @param expected The expected date.
     * @param actual The actual date.
     */
    public void assertEquals(Calendar expected, Calendar actual)
    {
        assertEquals(expected.get(Calendar.YEAR), actual.get(Calendar.YEAR));
        assertEquals(expected.get(Calendar.MONTH), actual.get(Calendar.MONTH));
        assertEquals(expected.get(Calendar.DAY_OF_MONTH), 
                     actual.get(Calendar.DAY_OF_MONTH));
        assertEquals(expected.get(Calendar.HOUR), actual.get(Calendar.HOUR));
        assertEquals(expected.get(Calendar.MINUTE), actual.get(Calendar.MINUTE));
        assertEquals(expected.get(Calendar.SECOND), actual.get(Calendar.SECOND));
        assertEquals(expected.get(Calendar.ZONE_OFFSET) + expected.get( Calendar.DST_OFFSET ), 
                     actual.get(Calendar.ZONE_OFFSET) + actual.get( Calendar.DST_OFFSET ));
        assertEquals(expected.getTimeInMillis(), actual.getTimeInMillis());

    }
    
    /**
     * Test ISO-8601 date conversion.
     * 
     * @throws IOException If the conversion did not work as expected.
     */
    public void testDateConversionNegativeTimeZone() throws IOException
    {
        Calendar c1 = Calendar.getInstance();
        c1.setTimeZone( TimeZone.getTimeZone("GMT-5"));
        c1.set(Calendar.MILLISECOND, 0);
        String convertedDate = DateConverter.toISO8601(c1);
        Calendar converted = DateConverter.toCalendar( convertedDate );
        assertEquals( c1, converted);
    }
    
    /**
     * Test ISO-8601 date conversion.
     * 
     * @throws IOException If the conversion did not work as expected.
     */
    public void testDateConversionPositiveTimeZone() throws IOException
    {
        Calendar c1 = Calendar.getInstance( TimeZone.getTimeZone("Australia/Sydney ") );
        c1.clear();
        c1.set(2007, 1, 27, 13, 12, 15);
        String convertedDate = DateConverter.toISO8601(c1);
        Calendar converted = DateConverter.toCalendar( convertedDate );
        
        assertEquals( c1, converted);
    }

    /**
     * Tests adding and removing from a date list.
     * 
     * @throws IOException Signals an error with the XMP processing.
     */
    public void testDateList() throws IOException
    {
        XMPMetadata xmp = new XMPMetadata();
        XMPSchema schema = new XMPSchema(xmp, "test", "http://test.com/test");

        Calendar c1 = Calendar.getInstance();
        c1.set(1999, 11, 31, 0, 0, 0);
        c1.set(Calendar.MILLISECOND, 0);

        Calendar c2 = Calendar.getInstance();
        c2.set(2000, 11, 31, 0, 0, 0);
        c2.set(Calendar.MILLISECOND, 0);
        // System.out.println( DateConverter.toISO8601(c1));

        schema.addSequenceDateValue("test:importantDates", c1);
        schema.addSequenceDateValue("test:importantDates", c2);

        List<Calendar> l = schema.getSequenceDateList("test:importantDates");

        assertEquals(2, l.size());

        assertEquals(c1, (Calendar) l.get(0));
        assertEquals(c2, (Calendar) l.get(1));

        schema.removeSequenceDateValue("test:importantDates", c1);

        l = schema.getSequenceDateList("test:importantDates");

        assertEquals(1, l.size());

        assertEquals(c2, (Calendar) l.get(0));

        // Already removed
        schema.removeSequenceDateValue("test:importantDates", c1);
        l = schema.getSequenceDateList("test:importantDates");
        assertEquals(1, l.size());
        assertEquals(c2, (Calendar) l.get(0));

        // Duplicates Allowed
        schema.addSequenceDateValue("test:importantDates", c2);
        l = schema.getSequenceDateList("test:importantDates");
        assertEquals(2, l.size());
        assertEquals(c2, (Calendar) l.get(0));
        assertEquals(c2, (Calendar) l.get(1));

        // Remvoes all
        schema.removeSequenceDateValue("test:importantDates", c2);
        l = schema.getSequenceDateList("test:importantDates");
        assertEquals(0, l.size());
    }
}
