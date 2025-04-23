/*
 * Copyright 2025 The Apache Software Foundation.
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
package org.apache.pdfbox.examples.pdmodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.util.Charsets;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class ExtractEmbeddedFilesTest
{
    /**
     * Test that the correct attachments are extracted from a portable collection.
     *
     * @throws IOException 
     */
    @Test
    public void testExtractEmbeddedFiles() throws IOException
    {
        String dir = "target/test-output";
        String collectionFilename = dir + "/PortableCollection.pdf";
        String attachment1Filename = dir + "/Test1.txt";
        String attachment2Filename = dir + "/Test2.txt";
        String[] args = new String[] { collectionFilename };
        CreatePortableCollection.main(args);
        ExtractEmbeddedFiles.main(args);
        InputStream is1 = new FileInputStream(attachment1Filename);
        InputStream is2 = new FileInputStream(attachment2Filename);
        byte[] ba1 = IOUtils.toByteArray(is1);
        byte[] ba2 = IOUtils.toByteArray(is2);
        is1.close();
        is2.close();
        String s1 = new String(ba1, Charsets.US_ASCII);
        String s2 = new String(ba2, Charsets.US_ASCII);
        assertEquals("This is the contents of the first embedded file", s1);
        assertEquals("This is the contents of the second embedded file", s2);
        new File(collectionFilename).delete();
        new File(attachment1Filename).delete();
        new File(attachment2Filename).delete();
    }
}
