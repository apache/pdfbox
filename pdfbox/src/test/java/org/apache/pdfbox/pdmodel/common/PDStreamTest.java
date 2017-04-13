/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.common;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class PDStreamTest
{
    
    /**
     * Test for null filter list (PDFBOX-2948)
     */
    @Test
    public void testCreateInputStreamNullFilters() throws Exception
    {
        PDDocument doc = new PDDocument();
        InputStream is = new ByteArrayInputStream(new byte[] { 12, 34, 56, 78 });
        PDStream pdStream = new PDStream(doc, is, (COSArray) null);
        Assert.assertNull(pdStream.getFilters());
        List<String> stopFilters = new ArrayList<>();
        stopFilters.add(COSName.DCT_DECODE.toString());
        stopFilters.add(COSName.DCT_DECODE_ABBREVIATION.toString());
        
        is = pdStream.createInputStream(stopFilters);
        Assert.assertEquals(12,is.read());
        Assert.assertEquals(34,is.read());
        Assert.assertEquals(56,is.read());
        Assert.assertEquals(78,is.read());
        Assert.assertEquals(-1,is.read());
        
        doc.close();
    } 
    
    /**
     * Test for empty filter list
     */
    @Test
    public void testCreateInputStreamEmptyFilters() throws Exception
    {
        PDDocument doc = new PDDocument();
        InputStream is = new ByteArrayInputStream(new byte[] { 12, 34, 56, 78 });
        PDStream pdStream = new PDStream(doc, is, new COSArray());
        Assert.assertEquals(0,pdStream.getFilters().size());
        List<String> stopFilters = new ArrayList<>();
        stopFilters.add(COSName.DCT_DECODE.toString());
        stopFilters.add(COSName.DCT_DECODE_ABBREVIATION.toString());
        
        is = pdStream.createInputStream(stopFilters);
        Assert.assertEquals(12,is.read());
        Assert.assertEquals(34,is.read());
        Assert.assertEquals(56,is.read());
        Assert.assertEquals(78,is.read());
        Assert.assertEquals(-1,is.read());
        
        doc.close();
    }
    
    /**
     * Test for null stop filters
     */
    @Test
    public void testCreateInputStreamNullStopFilters() throws Exception
    {
        PDDocument doc = new PDDocument();
        InputStream is = new ByteArrayInputStream(new byte[] { 12, 34, 56, 78 });
        PDStream pdStream = new PDStream(doc, is, new COSArray());
        Assert.assertEquals(0, pdStream.getFilters().size());

        is = pdStream.createInputStream(null);
        Assert.assertEquals(12, is.read());
        Assert.assertEquals(34, is.read());
        Assert.assertEquals(56, is.read());
        Assert.assertEquals(78,is.read());
        Assert.assertEquals(-1,is.read());
        
        doc.close();
    }

}
