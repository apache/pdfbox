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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.io.RandomAccessReadView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.apache.pdfbox.cos.TestCOSString.ESC_CHAR_STRING_PDF_FORMAT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link COSObject}.
 */
class TestCOSObject extends TestCOSBase implements ICOSParser
{
    static COSObjectKey key = new COSObjectKey( 121L, 0 );
    static COSString cosString;

    @BeforeAll
    static void setUp()
    {
        cosString = new COSString( "test string" );
        cosString.setKey( key );    // same key as the proxy object, because
            // this will be the dereferenced object.
        cosString.setDirect( true );    // If we were writing this as the value
            // in a COSDictionary (which we are not) we would use this object
            // directly and not create a reference for it.
        testCOSBase = new COSObject( cosString );
    }

     @Test
    void testGetCOSObject()
    {
        assert( testCOSBase.getCOSObject() instanceof COSObject );
    }

    @Test
    @Override
    void testIsSetDirect()
    {
        testCOSBase.setDirect(true);
        assertFalse(testCOSBase.isDirect());
        testCOSBase.setDirect(false);
        assertFalse(testCOSBase.isDirect());
    }

    @Test
    void testGetObject()
    {
        // if I'm not mistaken, a proxy object will /never/ be direct
        assertFalse( testCOSBase.isDirect() );
        COSBase base = ((COSObject) testCOSBase).getObject();
        // testCOSBase has no parser, so the object returned should be the
        // string object we initialized it with.
        assertEquals( cosString, base );
        assertTrue( ((COSObject) testCOSBase).isDereferenced() );


        final COSObject testCOSObject = new COSObject( key, this );

        // start by making sure that the test object is indirect and is not dereferenced.
        assertFalse( testCOSObject.isDereferenced() );

        // getObject should cause the referenced object to be dereferenced
        base = testCOSObject.getObject();
        assertTrue( testCOSObject.isDereferenced() );
        assertEquals( cosString, base );
    }

    /**
     * Test accept() - tests the interface for visiting a document at the COS level.
     * In the case of proxy {@link COSObject} the visitor is passed either to the
     * encapsulated object, if it is present or can be dereferenced, or to the
     * {@link COSNull#NULL} global object.
     */
    @Test
    void testAccept() throws IOException
    {
        String expected = "(" + ESC_CHAR_STRING_PDF_FORMAT + ")";
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        TestVisitor visitor = new TestVisitor(outStream);
        testCOSBase.accept( visitor );
        // the base test object encapsulates a string.
        assertEquals( expected, outStream.toString( StandardCharsets.ISO_8859_1 ));
        outStream.reset();
        // this new COSObject will dereference to the same string object as above.
        COSObject testCOSObject = new COSObject( key, this );
        testCOSObject.accept( visitor );
        assertEquals( expected, outStream.toString( StandardCharsets.ISO_8859_1 ));
        outStream.reset();
        testCOSObject.setToNull();
        testCOSObject.accept( visitor );
        assertEquals( "COSNull.NULL", outStream.toString( StandardCharsets.ISO_8859_1 ));
    }

    @Test
    void isCOSObjectNull()
    {
        COSObject testCOSObject = new COSObject( key, this );
        // The object has not been dereferenced, so it should still be null
        assertTrue( testCOSObject.isObjectNull());
        testCOSObject.getObject();  // This should dereference the object
        assertFalse( testCOSObject.isObjectNull());
        // this should set the encapsulated object to COSNull.NULL
        testCOSObject.setToNull();
        assertTrue( testCOSObject.isObjectNull());
        // set to null should have zeroed out the parser, so no further
        // dereferencing should be possible.
        COSBase base = testCOSObject.getObject();
        assertEquals( COSNull.NULL, base );
    }

    /**
     * A simple utility function to compare two byte arrays.
     * @param byteArr1 the expected byte array
     * @param byteArr2 the byte array being compared
     */
    @SuppressWarnings({"java:S5863"}) // don't flag tests for reflexivity
    protected void testByteArrays(byte[] byteArr1, byte[] byteArr2)
    {
        assertEquals(byteArr1.length, byteArr1.length);
        for (int i = 0; i < byteArr1.length; i++)
        {
            assertEquals(byteArr1[i], byteArr2[i]);
        }
    }

    @Override
    public COSBase dereferenceCOSObject( COSObject obj ) throws IOException
    {
        return cosString;
    }

    @Override
    public RandomAccessReadView createRandomAccessReadView( long startPosition, long streamLength ) throws IOException
    {
        return null;
    }
}
