/*
 * Copyright 2017 The Apache Software Foundation.
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
package org.apache.fontbox.cff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import static junit.framework.TestCase.assertEquals;
import org.junit.Test;

/**
 *
 * @author Petr Slaby
 */
public class CFFParserTest
{
    /**
     * PDFBOX-4038: Test whether BlueValues and other delta encoded lists are read correctly. The
     * test file is from FOP-2432.
     *
     * @throws IOException 
     */
    @Test
    public void testDeltaLists() throws IOException
    {
        List<CFFFont> fonts = readFont("target/pdfs/SourceSansProBold.otf");
        CFFType1Font font = (CFFType1Font) fonts.get(0);
        List<Number> blues = (List<Number>)font.getPrivateDict().get("BlueValues");

        // Expected values found for this font
        assertNumberList("Blue values are different than expected: " + blues.toString(),                     
                new int[]{-12, 0, 496, 508, 578, 590, 635, 647, 652, 664, 701, 713}, blues);

        List<Number> otherBlues = (List<Number>)font.getPrivateDict().get("OtherBlues");
        assertNumberList("Other blues are different than expected: " + otherBlues.toString(),                     
                new int[]{-196, -184}, otherBlues);

        List<Number> familyBlues = (List<Number>)font.getPrivateDict().get("FamilyBlues");
        assertNumberList("Other blues are different than expected: " + familyBlues.toString(),                     
                new int[]{-12, 0, 486, 498, 574, 586, 638, 650, 656, 668, 712, 724}, familyBlues);

        List<Number> familyOtherBlues = (List<Number>)font.getPrivateDict().get("FamilyOtherBlues");
        assertNumberList("Other blues are different than expected: " + familyOtherBlues.toString(),                     
                new int[]{-217, -205}, familyOtherBlues);

        List<Number> stemSnapH = (List<Number>)font.getPrivateDict().get("StemSnapH");
        assertNumberList("StemSnapH values are different than expected: " + stemSnapH.toString(),                     
                new int[]{115}, stemSnapH);

        List<Number> stemSnapV = (List<Number>)font.getPrivateDict().get("StemSnapV");
        assertNumberList("StemSnapV values are different than expected: " + stemSnapV.toString(),                     
                new int[]{146, 150}, stemSnapV);
    }

    private List<CFFFont> readFont(String filename) throws IOException
    {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        Files.copy(Paths.get(filename), content);
        CFFParser parser = new CFFParser();
        return parser.parse(content.toByteArray());
    }

    private void assertNumberList(String message, int[] expected, List<Number> found)
    {
        assertEquals(message, expected.length, found.size());
        for (int i = 0; i < expected.length; i++)
        {
            assertEquals(message, expected[i], found.get(i).intValue());
        }
    }
}
