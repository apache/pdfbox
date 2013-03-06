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

package org.apache.pdfbox.preflight.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.persistence.util.COSObjectKey;
import org.apache.pdfbox.preflight.utils.COSUtils;
import org.junit.Test;

public class TestCOSUtils
{

    @Test
    public void testIsInteger()
    {
        try
        {
            COSObject co = new COSObject(new COSInteger(10));
            co.setGenerationNumber(COSInteger.ZERO);
            co.setObjectNumber(new COSInteger(10));

            assertFalse(COSUtils.isInteger(co, new IOCOSDocument()));

            COSDocument doc = new COSDocument();
            addToXref(doc, new COSObjectKey(co), 1000);
            COSUtils.isInteger(co, doc);
            doc.close();
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void testIsFloat()
    {
        try
        {
            COSObject co = new COSObject(new COSFloat(10.0f));
            co.setGenerationNumber(COSInteger.ZERO);
            co.setObjectNumber(new COSInteger(10));

            assertFalse(COSUtils.isFloat(co, new IOCOSDocument()));

            COSDocument doc = new COSDocument();
            addToXref(doc, new COSObjectKey(co), 1000);
            COSUtils.isFloat(co, doc);
            doc.close();
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void testIsString()
    {
        try
        {
            COSObject co = new COSObject(new COSString(""));
            co.setGenerationNumber(COSInteger.ZERO);
            co.setObjectNumber(new COSInteger(10));

            assertFalse(COSUtils.isString(co, new IOCOSDocument()));

            COSDocument doc = new COSDocument();
            addToXref(doc, new COSObjectKey(co), 1000);
            COSUtils.isString(co, doc);
            doc.close();
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void testIsStream()
    {
        try
        {
            COSObject co = new COSObject(new COSStream(null));
            co.setGenerationNumber(COSInteger.ZERO);
            co.setObjectNumber(new COSInteger(10));

            assertFalse(COSUtils.isStream(co, new IOCOSDocument()));

            COSDocument doc = new COSDocument();
            addToXref(doc, new COSObjectKey(co), 1000);
            COSUtils.isStream(co, doc);
            doc.close();
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void testIsDictionary()
    {
        try
        {
            COSObject co = new COSObject(new COSDictionary());
            co.setGenerationNumber(COSInteger.ZERO);
            co.setObjectNumber(new COSInteger(10));

            assertFalse(COSUtils.isDictionary(co, new IOCOSDocument()));

            COSDocument doc = new COSDocument();
            addToXref(doc, new COSObjectKey(co), 1000);
            COSUtils.isDictionary(co, doc);
            doc.close();
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void testIsArray()
    {
        try
        {
            COSObject co = new COSObject(new COSArray());
            co.setGenerationNumber(COSInteger.ZERO);
            co.setObjectNumber(new COSInteger(10));

            assertFalse(COSUtils.isArray(co, new IOCOSDocument()));

            COSDocument doc = new COSDocument();
            addToXref(doc, new COSObjectKey(co), 1000);
            COSUtils.isArray(co, doc);
            doc.close();
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCloseCOSDocumentNull()
    {
        COSUtils.closeDocumentQuietly((COSDocument) null);
    }

    @Test
    public void testClosePDDocumentNull()
    {
        COSUtils.closeDocumentQuietly((PDDocument) null);
    }

    @Test
    public void testCloseCOSDocumentIO()
    {
        try
        {
            COSUtils.closeDocumentQuietly(new IOCOSDocument());
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }

    protected void addToXref(COSDocument doc, COSObjectKey key, long value)
    {
        Map<COSObjectKey, Long> xrefTable = new HashMap<COSObjectKey, Long>(1);
        xrefTable.put(key, value);
        doc.addXRefTable(xrefTable);
    }

    /**
     * Class used to check the catch block in COSUtils methods
     */
    private class IOCOSDocument extends COSDocument
    {

        IOCOSDocument() throws IOException
        {
            super();
        }

        IOCOSDocument(File scratchDir) throws IOException
        {
            super(scratchDir);
        }

        IOCOSDocument(RandomAccess file)
        {
            super(file);
        }

        @Override
        public void close() throws IOException
        {
            super.close();
            throw new IOException("Exception for code coverage");
        }

        @Override
        public COSObject getObjectFromPool(COSObjectKey key) throws IOException
        {
            super.close();
            throw new IOException("Exception for code coverage");
        }
    }
}
