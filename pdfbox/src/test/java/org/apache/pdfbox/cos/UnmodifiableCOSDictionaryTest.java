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
package org.apache.pdfbox.cos;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Calendar;

import org.apache.pdfbox.pdmodel.font.encoding.Encoding;
import org.junit.jupiter.api.Test;

class UnmodifiableCOSDictionaryTest
{
    @Test
    void testUnmodifiableCOSDictionary()
    {
        final COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();
        try
        {
            unmodifiableCOSDictionary.clear();
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch(final UnsupportedOperationException exception)
        {
            // nothing to do
        }

        try
        {
            unmodifiableCOSDictionary.removeItem(COSName.A);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch(final UnsupportedOperationException exception)
        {
            // nothing to do
        }

        final COSDictionary cosDictionary = new COSDictionary();
        try
        {
            unmodifiableCOSDictionary.addAll(cosDictionary);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch(final UnsupportedOperationException exception)
        {
            // nothing to do
        }

        try
        {
            unmodifiableCOSDictionary.setFlag(COSName.A, 0, true);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch(final UnsupportedOperationException exception)
        {
            // nothing to do
        }

        try
        {
            unmodifiableCOSDictionary.setNeedToBeUpdated(true);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch(final UnsupportedOperationException exception)
        {
            // nothing to do
        }
    }

    @Test
    void testSetItem()
    {
        final COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();
        try
        {
            unmodifiableCOSDictionary.setItem(COSName.A, COSName.A);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch(final UnsupportedOperationException exception)
        {
            // nothing to do
        }
        
        final Encoding standardEncoding = Encoding.getInstance(COSName.STANDARD_ENCODING);
        try
        {
            unmodifiableCOSDictionary.setItem(COSName.A, standardEncoding);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch(final UnsupportedOperationException exception)
        {
            // nothing to do
        }
        
        try
        {
            unmodifiableCOSDictionary.setItem("A", COSName.A);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }

        try
        {
            unmodifiableCOSDictionary.setItem("A", standardEncoding);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch(final UnsupportedOperationException exception)
        {
            // nothing to do
        }
    }

    @Test
    void testSetBoolean()
    {
        final COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();
        try
        {
            unmodifiableCOSDictionary.setBoolean(COSName.A, true);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
        
        try
        {
            unmodifiableCOSDictionary.setBoolean("A", true);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
    }

    @Test
    void testSetName()
    {
        final COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();
        try
        {
            unmodifiableCOSDictionary.setName(COSName.A, "A");
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
        
        try
        {
            unmodifiableCOSDictionary.setName("A", "A");
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
    }

    @Test
    void testSetDate()
    {
        final COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();
        final Calendar calendar = Calendar.getInstance();
        try
        {
            unmodifiableCOSDictionary.setDate(COSName.A, calendar);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
        
        try
        {
            unmodifiableCOSDictionary.setDate("A", calendar);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
    }

    @Test
    void testSetEmbeddedDate()
    {
        final COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();
        final Calendar calendar = Calendar.getInstance();
        try
        {
            unmodifiableCOSDictionary.setEmbeddedDate("Embedded", COSName.A, calendar);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
        
        try
        {
            unmodifiableCOSDictionary.setEmbeddedDate("Embedded", "A", calendar);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
    }

    @Test
    void testSetString()
    {
        final COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();
        try
        {
            unmodifiableCOSDictionary.setString(COSName.A, "A");
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
        
        try
        {
            unmodifiableCOSDictionary.setString("A", "A");
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
    }

    @Test
    void testSetEmbeddedString()
    {
        final COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();
        try
        {
            unmodifiableCOSDictionary.setEmbeddedString("Embedded", COSName.A, "A");
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
        
        try
        {
            unmodifiableCOSDictionary.setEmbeddedString("Embedded", "A", "A");
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
    }

    @Test
    void testSetInt()
    {
        final COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();
        try
        {
            unmodifiableCOSDictionary.setInt(COSName.A, 0);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
        
        try
        {
            unmodifiableCOSDictionary.setInt("A", 0);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
    }

    @Test
    void testSetEmbeddedInt()
    {
        final COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();
        try
        {
            unmodifiableCOSDictionary.setEmbeddedInt("Embedded", COSName.A, 0);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
        
        try
        {
            unmodifiableCOSDictionary.setEmbeddedInt("Embedded", "A", 0);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
    }

    @Test
    void testSetLong()
    {
        final COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();
        try
        {
            unmodifiableCOSDictionary.setLong(COSName.A, 0);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
        
        try
        {
            unmodifiableCOSDictionary.setLong("A", 0);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
    }

    @Test
    void testSetFloat()
    {
        final COSDictionary unmodifiableCOSDictionary = new COSDictionary().asUnmodifiableDictionary();
        try
        {
            unmodifiableCOSDictionary.setFloat(COSName.A, 0);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
        
        try
        {
            unmodifiableCOSDictionary.setFloat("A", 0);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (final UnsupportedOperationException exception)
        {
            // nothing to do
        }
    }

}
