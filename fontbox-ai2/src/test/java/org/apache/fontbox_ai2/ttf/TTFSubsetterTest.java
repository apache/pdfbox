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
package org.apache.fontbox_ai2.ttf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class TTFSubsetterTest
{

    /**
     * Test of PDFBOX-2854.
     */
    @Test
    public void testEmptySubset() throws IOException
    {
        final File testFile = new File("src/test/resources/ttf/LiberationSans-Regular.ttf");
        TrueTypeFont x = new TTFParser().parse(testFile);
        TTFSubsetter ttfSubsetter = new TTFSubsetter(x);
        try
        {
            ttfSubsetter.writeToStream(new ByteArrayOutputStream());
            fail("IllegalStateException should be thrown");
        }
        catch (IllegalStateException e)
        {
            // ok
        }
    }

    /**
     * Test of PDFBOX-2854.
     */
    //@Test
    // enable when fixed
    public void testNonEmptySubset() throws IOException
    {
        final File testFile = new File("src/test/resources/ttf/LiberationSans-Regular.ttf");
        TrueTypeFont x = new TTFParser().parse(testFile);
        TTFSubsetter ttfSubsetter = new TTFSubsetter(x);
        ttfSubsetter.add('a');
        ttfSubsetter.writeToStream(new ByteArrayOutputStream());
    }

}
