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

import static org.apache.pdfbox.cos.TestVisitor.ESC_CHAR_STRING_PDF_FORMAT;
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

    /**
     * TODO: It seems that the "direct" flag indicates that this object can be
     * added as a COSDictionary value directly, otherwise a new referenced object
     * should be created. If so, {@link COSObject}s (which simply proxy other
     * {@link COSBase}-derived objects) should never be "direct," and {@link COSObject}
     * should be modified to enforce this rule.
     */
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
    void testIsCOSObjectNull()
    {
        COSObject testCOSObject = new COSObject( key, this );
        // The object has not been dereferenced, so this method should
        // return true.
        assertTrue( testCOSObject.isObjectNull());
        testCOSObject.getObject();  // This should dereference the object
        assertFalse( testCOSObject.isObjectNull());

        // this should set the encapsulated object to COSNull.NULL
        // TODO: should COSObject.setToNull() also clear the COSObjectKey?
        testCOSObject.setToNull();

        // isObjectNull() does not test for a COSNull object but only tests
        // if the encapsulated reference actually is null. Thus, if the
        // encapsulated object is COSNull it will still return false.
        // TODO: is this the intention?
        assertFalse( testCOSObject.isObjectNull());

        // set to null should have zeroed out the parser, so no further
        // dereferencing should be possible.
        COSBase base = testCOSObject.getObject();
        assertEquals( COSNull.NULL, base );
    }

    @Test
    public void testNullObjectConstructor()
    {
        COSObject testCOSObject = new COSObject( COSNull.NULL, this );
        assertTrue( testCOSObject.isDereferenced());
        assertFalse( testCOSObject.isObjectNull()); // TODO: Is this really the intention?
        COSBase base = testCOSObject.getObject();
        assertEquals( COSNull.NULL, base );
    }

    @Override
    public COSBase dereferenceCOSObject( COSObject obj ) throws IOException
    {
        return cosString;
    }

    /**
     * Unused but required by ICOSParser interface
     *
     * @param startPosition start position within the underlying random access read
     * @param streamLength stream length
     * @return  null
     * @throws IOException
     */
    @Override
    public RandomAccessReadView createRandomAccessReadView( long startPosition, long streamLength ) throws IOException
    {
        return null;
    }
}
