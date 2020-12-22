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
package org.apache.pdfbox.multipdf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

/**
 * Test suite for PageExtractor.
 *
 * This is just some simple tests based on a test document.  It merely ensures
 * that the correct number of pages are extracted as this is virtually the only
 * thing which could go wrong when coping pages from one PDF to a new one.
 *
 * @author Adam Nichols
 */
class PageExtractorTest
{
    
    private void closeDoc(final PDDocument doc)
    {
        if(doc != null)
        {
            try
            {
                doc.close(); 
            }
            catch(final Exception e)
            {
                /* Can't do much about this... */ 
            }
        }
    }
    
    /**
     * Test of extract method, of class org.apache.pdfbox.util.PageExtractor.
     */
    @Test
    void testExtract() throws Exception
    {
        PDDocument sourcePdf = null;
        PDDocument result = null;
        try
        {
            // this should work for most users
            sourcePdf = Loader.loadPDF(new File("src/test/resources/input/cweb.pdf"));
            PageExtractor instance = new PageExtractor(sourcePdf);
            result = instance.extract();
            assertEquals(sourcePdf.getNumberOfPages(), result.getNumberOfPages());
            closeDoc(result);
            
            instance = new PageExtractor(sourcePdf, 1, 1);
            result = instance.extract();
            assertEquals(1, result.getNumberOfPages());
            closeDoc(result);
            
            instance = new PageExtractor(sourcePdf, 1, 5);
            result = instance.extract();
            assertEquals(5, result.getNumberOfPages());
            closeDoc(result);
            
            instance = new PageExtractor(sourcePdf, 5, 10);
            result = instance.extract();
            assertEquals(6, result.getNumberOfPages());
            closeDoc(result);
            
            instance = new PageExtractor(sourcePdf, 2, 1);
            result = instance.extract();
            assertEquals(0, result.getNumberOfPages());
            closeDoc(result);
        }
        finally
        {
            closeDoc(sourcePdf);
            closeDoc(result);
        }
    }    
}
