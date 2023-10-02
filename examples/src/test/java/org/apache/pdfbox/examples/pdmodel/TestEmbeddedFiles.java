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
package org.apache.pdfbox.examples.pdmodel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class TestEmbeddedFiles
{
    /**
     * Very basic test of embedding and extracting an attachment.
     *
     * @throws IOException 
     */
    @Test
    void testEmbeddedFiles() throws IOException
    {
        String outputFile = "target/test-output/EmbeddedFile.pdf";
        String embeddedFile = "target/test-output/Test.txt";

        new File("target/test-output").mkdirs();
        new File(outputFile).delete();
        new File(embeddedFile).delete();
        String[] args = { outputFile };
        EmbeddedFiles.main(args);
        ExtractEmbeddedFiles.main(args);
        byte[] bytes = Files.readAllBytes(Paths.get(embeddedFile));
        String content = new String(bytes);
        Assertions.assertEquals("This is the contents of the embedded file", content);
        new File(embeddedFile).delete();
        new File(outputFile).delete();
    }
}
