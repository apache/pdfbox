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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class ExtractEmbeddedFilesTest
{
    /**
     * Test that the correct attachments are extracted from a portable collection.
     *
     * @throws IOException 
     */
    @Test
    void testExtractEmbeddedFiles() throws IOException
    {
        String dir = "target/test-output";
        new File(dir).mkdirs();
        String collectionFilename = dir + "/PortableCollection.pdf";
        String attachment1Filename = dir + "/Test1.txt";
        String attachment2Filename = dir + "/Test2.txt";
        String[] args = new String[] { collectionFilename };
        CreatePortableCollection.main(args);
        ExtractEmbeddedFiles.main(args);
        byte[] ba1 = Files.readAllBytes(new File(attachment1Filename).toPath());
        byte[] ba2 = Files.readAllBytes(new File(attachment2Filename).toPath());
        String s1 = new String(ba1, StandardCharsets.US_ASCII);
        String s2 = new String(ba2, StandardCharsets.US_ASCII);
        assertEquals("This is the contents of the first embedded file", s1);
        assertEquals("This is the contents of the second embedded file", s2);
        Files.delete(Paths.get(collectionFilename));
        Files.delete(Paths.get(attachment1Filename));
        Files.delete(Paths.get(attachment2Filename));
    }
}
