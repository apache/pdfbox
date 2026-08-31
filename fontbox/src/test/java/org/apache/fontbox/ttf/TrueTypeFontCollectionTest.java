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
package org.apache.fontbox.ttf;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import org.junit.Test;

public class TrueTypeFontCollectionTest
{
    @Test
    public void testMissingTtcHeader()
    {
        try
        {
            new TrueTypeCollection(new ByteArrayInputStream(new byte[4]));
        }
        catch (IOException ex)
        {
            // this is the expected behaviour
            assertEquals("Missing TTC header", ex.getMessage());
        }
        catch (Throwable throwable)
        {
            fail("Missing ttc header not detected!");
        }
    }

    @Test
    public void testNumberOfFonts()
    {
        byte[] payload = { 0x74, 0x74, 0x63, 0x66, 0x00, 0x00, 0x00, 0x00, 0x7F, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF };
        try
        {
            new TrueTypeCollection(new ByteArrayInputStream(payload));
        }
        catch (IOException ex)
        {
            // this is the expected behaviour
            assertEquals("Invalid number of fonts 2147483647", ex.getMessage());
        }
        catch (Throwable throwable)
        {
            fail("Invalid number of fonts not detected!");
        }
    }

    @Test
    public void testMingLiu() throws IOException
    {
        File file = new File("c:/windows/fonts/mingliu.ttc");
        assumeTrue(file.exists());
        checkTrueTypeCollection(file, "[MingLiU, PMingLiU, Ming-Lt-HKSCS-UNI-H]");
    }

    @Test
    public void testMsMincho() throws IOException
    {
        File file = new File("c:/windows/fonts/msmincho.ttc");
        assumeTrue(file.exists());
        checkTrueTypeCollection(file, "[MS-Mincho, MS-PMincho]");
    }

    @Test
    public void testSimSun() throws IOException
    {
        File file = new File("c:/windows/fonts/simsun.ttc");
        assumeTrue(file.exists());
        checkTrueTypeCollection(file, "[SimSun, NSimSun]");
    }

    @Test
    public void testLucidaGrande() throws IOException
    {
        File file = new File("/System/Library/Fonts/LucidaGrande.ttc");
        assumeTrue(file.exists());
        checkTrueTypeCollection(file, "[LucidaGrande, LucidaGrande-Bold, .LucidaGrandeUI, .LucidaGrandeUI-Bold]");
    }

    @Test
    public void testAvenir() throws IOException
    {
        File file = new File("/System/Library/Fonts/Avenir.ttc");
        assumeTrue(file.exists());
        checkTrueTypeCollection(file, 
                "[Avenir-Book, Avenir-BookOblique, Avenir-Black, Avenir-BlackOblique, "
                        + "Avenir-Heavy, Avenir-HeavyOblique, Avenir-Light, Avenir-LightOblique, "
                        + "Avenir-Medium, Avenir-MediumOblique, Avenir-Oblique, Avenir-Roman]");
    }

    private void checkTrueTypeCollection(File file, String expected) throws IOException
    {
        final TrueTypeCollection ttc = new TrueTypeCollection(file);
        final List<String> list = new ArrayList();
        ttc.processAllFonts(new TrueTypeCollection.TrueTypeFontProcessor()
        {
            @Override
            public void process(TrueTypeFont ttf) throws IOException
            {
                list.add(ttf.getName());
                TrueTypeFont ttfByName = ttc.getFontByName(ttf.getName());
                assertEquals(ttf.getName(), ttfByName.getName());
                ttfByName.close();
            }
        });
        assertEquals(expected, list.toString());
        ttc.close();
    }
}
