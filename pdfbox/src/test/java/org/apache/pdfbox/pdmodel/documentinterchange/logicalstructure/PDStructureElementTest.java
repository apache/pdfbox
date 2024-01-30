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
package org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure;


import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class PDStructureElementTest
{
    private static final File TARGETPDFDIR = new File("target/pdfs");

    /**
     * PDFBOX-4197: test that object references in array attributes of a PDStructureElement are caught.
     *
     * @throws IOException 
     */
    @Test
    void testPDFBox4197() throws IOException
    {
        Set<Revisions<PDAttributeObject>> attributeSet = new HashSet<>();
        Set<String> classSet = new HashSet<>();
        try (PDDocument doc = Loader.loadPDF(new File(TARGETPDFDIR, "PDFBOX-4197.pdf")))
        {
            PDStructureTreeRoot structureTreeRoot = doc.getDocumentCatalog().getStructureTreeRoot();
            checkElement(structureTreeRoot.getK(), attributeSet, structureTreeRoot.getClassMap(), classSet);
        }

        // collect attributes and check their count.
        assertEquals(117, attributeSet.size());
        int cnt = attributeSet.stream().map(attributes -> attributes.size()).reduce(0, Integer::sum);
        assertEquals(111, cnt); // this one was 105 before PDFBOX-4197 was fixed
        assertEquals(0, classSet.size());
    }

    /**
     * Check that all classes are caught and are in the /ClassMap
     *
     * @throws IOException 
     */
    @Test
    void testClassMap() throws IOException
    {
        Set<Revisions<PDAttributeObject>> attributeSet = new HashSet<>();
        Set<String> classSet = new HashSet<>();
        try (PDDocument doc = Loader.loadPDF(
                RandomAccessReadBuffer.createBufferFromStream(PDStructureElementTest.class
                        .getResourceAsStream("PDFBOX-2725-878725.pdf"))))
        {
            PDStructureTreeRoot structureTreeRoot = doc.getDocumentCatalog().getStructureTreeRoot();
            checkElement(structureTreeRoot.getK(), attributeSet, structureTreeRoot.getClassMap(), classSet);
        }

        // collect attributes and check their count.
        assertEquals(72, attributeSet.size());
        int cnt = attributeSet.stream().map(attributes -> attributes.size()).reduce(0, Integer::sum);
        assertEquals(45, cnt);
        assertEquals(10, classSet.size());
    }

    // Each element can be an array, a dictionary or a number.
    // See PDF specification Table 323 - Entries in a structure element dictionary
    private void checkElement(COSBase base, Set<Revisions<PDAttributeObject>>attributeSet,
            Map<String, Object> classMap, Set<String> classSet)
    {
        if (base instanceof COSArray)
        {
            for (COSBase base2 : (COSArray) base)
            {
                if (base2 instanceof COSObject)
                {
                    base2 = ((COSObject) base2).getObject();
                }
                checkElement(base2, attributeSet, classMap, classSet);
            }
        }
        else if (base instanceof COSDictionary)
        {
            COSDictionary kdict = (COSDictionary) base;
            if (kdict.containsKey(COSName.PG))
            {
                PDStructureElement structureElement = new PDStructureElement(kdict);
                Revisions<PDAttributeObject> attributes = structureElement.getAttributes();
                attributeSet.add(attributes);
                Revisions<String> classNames = structureElement.getClassNames();

                // "If both the A and C entries are present and a given attribute is specified by both, 
                // the one specified by the A entry shall take precedence."
                if (kdict.containsKey(COSName.C) && !kdict.containsKey(COSName.A))
                {
                    for (int i = 0; i < classNames.size(); ++i)
                    {
                        String className = classNames.getObject(i);
                        classSet.add(className);
                        Assertions.assertTrue(classMap.containsKey(className), "'" + className + "' not in ClassMap " + classMap);
                    }
                }
            }
            if (kdict.containsKey(COSName.K))
            {
                checkElement(kdict.getDictionaryObject(COSName.K), attributeSet, classMap, classSet);
            }
        }
    }    
}
