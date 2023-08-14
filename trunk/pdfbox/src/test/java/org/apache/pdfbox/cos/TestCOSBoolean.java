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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.pdfwriter.COSWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unittests for {@link COSBoolean}
 */
class TestCOSBoolean extends TestCOSBase
{
    final COSBoolean cosBooleanTrue = COSBoolean.TRUE;
    final COSBoolean cosBooleanFalse = COSBoolean.FALSE;

    @BeforeAll
    static void setUp()
    {
        testCOSBase = COSBoolean.TRUE;
    }

    @Test
    void testGetValue()
    {
        assertTrue(cosBooleanTrue.getValue());
        assertFalse(cosBooleanFalse.getValue());
    }

    @Test
    void testGetValueAsObject()
    {
        assertTrue(cosBooleanTrue.getValueAsObject() instanceof Boolean);
        assertEquals(Boolean.TRUE, cosBooleanTrue.getValueAsObject());
        assertTrue(cosBooleanFalse.getValueAsObject() instanceof Boolean);
        assertEquals(Boolean.FALSE, cosBooleanFalse.getValueAsObject());
    }

    @Test
    void testGetBoolean()
    {
        assertEquals(cosBooleanTrue, COSBoolean.getBoolean(Boolean.TRUE));
        assertEquals(cosBooleanFalse, COSBoolean.getBoolean(Boolean.FALSE));
    }

    @Test
    void testEquals()
    {
        COSBoolean test1 = COSBoolean.TRUE;
        COSBoolean test2 = COSBoolean.TRUE;
        COSBoolean test3 = COSBoolean.TRUE;
        // Reflexive (x == x)
        assertEquals(test1, test1);
        // Symmetric is preserved ( x==y then y===x)
        assertEquals(test2, test1);
        assertEquals(test1, test2);
        // Transitive (if x==y && y==z then x===z)
        assertEquals(test1, test2);
        assertEquals(test2, test3);
        assertEquals(test1, test3);

        assertNotEquals(COSBoolean.TRUE, COSBoolean.FALSE);
        // same 'value' but different type
        assertNotEquals(Boolean.TRUE, COSBoolean.TRUE);
        assertNotEquals(Boolean.FALSE, COSBoolean.FALSE);
        assertNotEquals(true, COSBoolean.TRUE);
        assertNotEquals(true, COSBoolean.FALSE);
    }

    @Override
    @Test
    void testAccept()
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        COSWriter visitor = new COSWriter(outStream);
        int index = 0;
        try
        {
            cosBooleanTrue.accept(visitor);
            testByteArrays(String.valueOf(cosBooleanTrue).getBytes(StandardCharsets.ISO_8859_1), outStream.toByteArray());
            outStream.reset();
            cosBooleanFalse.accept(visitor);
            testByteArrays(String.valueOf(cosBooleanFalse).getBytes(StandardCharsets.ISO_8859_1), outStream.toByteArray());
            outStream.reset();
        }
        catch (Exception e)
        {
            fail("Failed to write " + index + " exception: " + e.getMessage());
        }
    }
}
