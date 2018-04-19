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
import java.util.Set;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class PDStructureElementTest
{
    private static final File TARGETPDFDIR = new File("target/pdfs");

    /**
     * PDFBOX-4197: test that object references in array attributes of a PDStructureElement are caught.
     *
     * @throws IOException 
     */
    @Test
    public void testPDFBox4197() throws IOException
    {
        PDDocument doc = PDDocument.load(new File(TARGETPDFDIR, "PDFBOX-4197.pdf"));
        PDStructureTreeRoot structureTreeRoot = doc.getDocumentCatalog().getStructureTreeRoot();
        Set<Revisions<PDAttributeObject>> attributeSet = new HashSet<Revisions<PDAttributeObject>>();
        checkElement(structureTreeRoot.getK(), attributeSet);
        doc.close();

        // collect attributes and check their count.
        Assert.assertEquals(117, attributeSet.size());
        int cnt = 0;
        for (Revisions<PDAttributeObject> attributes : attributeSet)
        {
            cnt += attributes.size();
        }
        Assert.assertEquals(111, cnt); // this one was 105 before PDFBOX-4197 was fixed
    }

    // Each element can be an array, a dictionary or a number.
    // See PDF specification Table 323 - Entries in a structure element dictionary
    private void checkElement(COSBase base, Set<Revisions<PDAttributeObject>>attributeSet)
    {
        if (base instanceof COSArray)
        {
            for (COSBase base2 : (COSArray) base)
            {
                if (base2 instanceof COSObject)
                {
                    base2 = ((COSObject) base2).getObject();
                }
                checkElement(base2, attributeSet);
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
                //TODO: modify the test to also check for class names, if we ever have a file.
            }
            if (kdict.containsKey(COSName.K))
            {
                checkElement(kdict.getDictionaryObject(COSName.K), attributeSet);
            }
        }
    }    
}
