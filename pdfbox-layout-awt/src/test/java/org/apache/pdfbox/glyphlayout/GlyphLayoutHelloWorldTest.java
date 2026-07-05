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
package org.apache.pdfbox.glyphlayout;

import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import org.apache.pdfbox.glyphlayout.examples.GlyphLayoutHelloWorld;

/**
 *
 * @author Tilman Hausherr
 */
public class GlyphLayoutHelloWorldTest extends TestBase
{
    @Test
    void testGlyphLayoutHelloWorld() throws IOException, FontFormatException, URISyntaxException
    {
        String outputName = "HelloWorld.pdf";
        String lohitBengaliPath = "/ttf/Lohit-Bengali.ttf";
        File file = new File(GlyphLayoutHelloWorldTest.class.getResource(lohitBengaliPath).toURI());
        System.out.println(file.getPath());
        String [] args = new String[]{ "target/" + outputName, "হ্যালো ওয়ার্ল্ড", file.getPath() };
        GlyphLayoutHelloWorld.main(args);
        checkRenderIdent(outputName);
    }
}
