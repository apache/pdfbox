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
package org.apache.pdfbox.pdmodel.graphics.color;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.junit.jupiter.api.Test;

class PDIndexedTest
{

    /**
     * Test of factory method for PDFBOX-6192.
     */
    @Test
    void testFactory()
    {
        final PDColorSpace baseColorspace = PDDeviceRGB.INSTANCE;
        // define 6 color values
        final int hival = 5;
        // create s string containing 6 RGB values. Spaces are added for a better readability
        final String stringLookupData = "AA1166 112233 000000 FEDC01 4561FE DC34DA" //
                .replace(" ", "");
        // expected written string for COSArray
        final String outputString = "/Indexed /DeviceRGB 5 <" + stringLookupData + ">";

        try
        {
            byte[] lookupData = COSString.parseHex(stringLookupData).getBytes();
            PDIndexed pdIndexed = PDIndexed.create(baseColorspace, hival, lookupData);
            COSArray indexedCOSArray = ((COSArray) pdIndexed.getCOSObject());
            assertEquals(hival, ((COSNumber) indexedCOSArray.getObject(2)).intValue(),
                    "unexpected value for hival");
            assertEquals(COSName.INDEXED.getName(), pdIndexed.getName(),
                    "unexpected value for name");
            assertEquals(baseColorspace, pdIndexed.getBaseColorSpace(),
                    "unexpected value for base colorspace");
            String lookupDataString = ((COSString) indexedCOSArray.getObject(3)).toHexString();
            assertEquals(stringLookupData, lookupDataString, "unexpected value for lookup data");

            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            PDResources resources = new PDResources();
            resources.add(pdIndexed);
            page.setResources(resources);
            document.addPage(page);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos, CompressParameters.NO_COMPRESSION);
            document.close();
            String pdfAsString = baos.toString();
            assertTrue(pdfAsString.contains(outputString), "output doesn't match expected string");
        }
        catch (IOException e)
        {
            fail("Unexpected exception", e);
        }
    }

    /**
     * Test parameter of factory method.
     */
    @Test
    void testFactoryParameterChecks()
    {
        final PDColorSpace baseColorspace = PDDeviceRGB.INSTANCE;
        // empty lookupData as placeholder
        final byte[] lookupDataEmpty = new byte[5];
        // define 6 color values
        final int hival = 5;
        // create s string containing 6 RGB values. Spaces are added for a better readability
        final String stringLookupData = "AA1166 112233 000000 FEDC01 4561FE DC34DA" //
                .replace(" ", "");
        byte[] lookupData = null;
        try
        {
            lookupData = COSString.parseHex(stringLookupData).getBytes();
        }
        catch (IOException e)
        {
            fail("Unexpected exception", e);
        }

        // check lookupData not null
        assertThrows(IllegalArgumentException.class,
                () -> PDIndexed.create(baseColorspace, 0, null));
        // check base colorspace not null
        assertThrows(IllegalArgumentException.class,
                () -> PDIndexed.create(null, 0, lookupDataEmpty));
        // check hival not negative
        assertThrows(IllegalArgumentException.class,
                () -> PDIndexed.create(baseColorspace, -1, lookupDataEmpty));
        // check hival <= 255
        assertThrows(IllegalArgumentException.class,
                () -> PDIndexed.create(baseColorspace, 256, lookupDataEmpty));
        // check minimum size of lookupData array: (hival + 1) * numberOfComponents of base colorspace
        assertThrows(IllegalArgumentException.class,
                () -> PDIndexed.create(baseColorspace, hival, lookupDataEmpty));

        // everything is fine
        final byte[] lookupDataOK = lookupData;
        assertDoesNotThrow(() -> PDIndexed.create(baseColorspace, hival, lookupDataOK));
    }

}
