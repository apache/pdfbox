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
package org.apache.pdfbox.glyphlayout.awt;

import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import org.apache.pdfbox.glyphlayout.examples.GlyphLayoutHelloWorldAWT;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

/**
 *
 * @author Tilman Hausherr
 */
@EnabledForJreRange(min = JRE.JAVA_9)
class GlyphLayoutHelloWorldAWTTest extends TestBase
{
    @Test
    void testGlyphLayoutHelloWorldAWT() throws IOException, FontFormatException, URISyntaxException
    {
        String outputName = "HelloWorld.pdf";
        String lohitBengaliPath = "/ttf/Lohit-Bengali.ttf";
        File file = new File(GlyphLayoutHelloWorldAWTTest.class.getResource(lohitBengaliPath).toURI());
        String [] args = new String[]{ "target/" + outputName, "হ্যালো ওয়ার্ল্ড", file.getPath() };
        GlyphLayoutHelloWorldAWT.main(args);
        checkRenderIdent(outputName);
    }
}
