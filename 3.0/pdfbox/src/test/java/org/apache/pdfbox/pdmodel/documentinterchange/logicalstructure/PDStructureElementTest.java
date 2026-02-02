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
import java.util.List;
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
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;

import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDLayoutAttributeObject;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDTableAttributeObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        int cnt = attributeSet.stream().map(Revisions::size).reduce(0, Integer::sum);
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

        for (Revisions<PDAttributeObject> r : attributeSet)
        {
            // check a few that we know
            if (r.size() >= 2)
            {
                // e.g. in Root/StructTreeRoot/K/[2]/K/[14]/K/[5]/K/[0]/K/[2]/A
                // and     Root/StructTreeRoot/K/[2]/K/[14]/K/[5]/K/[2]/K/[0]/A
                // and     Root/StructTreeRoot/K/[2]/K/[14]/K/[5]/K/[2]/K/[2]/A
                PDTableAttributeObject obj0 = (PDTableAttributeObject) r.getObject(0);
                assertEquals("Table", obj0.getOwner());
                assertEquals(2, obj0.getColSpan());
                PDLayoutAttributeObject obj1 = (PDLayoutAttributeObject) r.getObject(1);
                assertEquals("Layout", obj1.getOwner());
                assertTrue(((Float) obj1.getWidth()) == 166.375f || ((Float) obj1.getWidth()) == 246.75f);
                assertTrue(((Float) obj1.getHeight()) == 14f || ((Float) obj1.getHeight()) == 17f);
                assertEquals("Start", obj1.getInlineAlign());
                assertTrue("After".equals(obj1.getBlockAlign()) || "Before".equals(obj1.getBlockAlign()));
                assertEquals(0, r.getRevisionNumber(0));
                assertEquals(0, r.getRevisionNumber(1));
            }
        }

        // collect attributes and check their count.
        assertEquals(72, attributeSet.size());
        int cnt = attributeSet.stream().map(Revisions::size).reduce(0, Integer::sum);
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
                        assertTrue(classMap.containsKey(className), "'" + className + "' not in ClassMap " + classMap);
                    }
                }
            }
            if (kdict.containsKey(COSName.K))
            {
                checkElement(kdict.getDictionaryObject(COSName.K), attributeSet, classMap, classSet);
            }
        }
    }    

    @Test
    void testSimple()
    {
        PDStructureElement structureElement = new PDStructureElement("S", null);
        assertEquals(PDStructureElement.TYPE, structureElement.getType());
        assertEquals("S", structureElement.getStructureType());
        assertNull(structureElement.getParent());
        structureElement.setStructureType("T");
        assertEquals("T", structureElement.getStructureType());
        structureElement.setElementIdentifier("Ident");
        assertEquals("Ident", structureElement.getElementIdentifier());
        structureElement.setRevisionNumber(33);
        assertEquals(33, structureElement.getRevisionNumber());
        structureElement.incrementRevisionNumber();
        assertEquals(34, structureElement.getRevisionNumber());
        assertThrows(IllegalArgumentException.class, () -> structureElement.setRevisionNumber(-1));
        structureElement.setTitle("Title");
        assertEquals("Title", structureElement.getTitle());
        structureElement.setLanguage("Klingon");
        assertEquals("Klingon", structureElement.getLanguage());
        structureElement.setAlternateDescription("Alto");
        assertEquals("Alto", structureElement.getAlternateDescription());
        structureElement.setActualText("Actual");
        assertEquals("Actual", structureElement.getActualText());
        structureElement.setExpandedForm("ExpF");
        assertEquals("ExpF", structureElement.getExpandedForm());
        assertThrows(IllegalArgumentException.class, () -> structureElement.appendKid(-1));
        structureElement.appendKid(0);
        PDMarkedContentReference mcr1 = new PDMarkedContentReference();
        mcr1.setMCID(1);
        structureElement.appendKid(mcr1);
        PDMarkedContentReference mcr2 = new PDMarkedContentReference();
        mcr2.setMCID(2);
        PDMarkedContent mc2 = PDMarkedContent.create(COSName.S, mcr2.getCOSObject());
        structureElement.appendKid(mc2);
        PDMarkedContentReference mcrSubZero = new PDMarkedContentReference();
        assertThrows(IllegalArgumentException.class, () -> mcrSubZero.setMCID(-1));
        mcrSubZero.getCOSObject().setInt(COSName.MCID, -1);
        PDMarkedContent mcSubZero = PDMarkedContent.create(COSName.S, mcrSubZero.getCOSObject());
        assertThrows(IllegalArgumentException.class, () -> structureElement.appendKid(mcSubZero));
        List<Object> kids = structureElement.getKids();
        assertEquals(3, kids.size());
        assertEquals(0, kids.get(0));
        mcr1 = (PDMarkedContentReference) kids.get(1);
        assertEquals(PDMarkedContentReference.TYPE, mcr1.getCOSObject().getNameAsString(COSName.TYPE));
        assertEquals(1, mcr1.getMCID());
        assertEquals(2, kids.get(2));
    }
}
