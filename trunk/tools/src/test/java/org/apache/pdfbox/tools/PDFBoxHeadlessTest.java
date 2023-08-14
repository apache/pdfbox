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
package org.apache.pdfbox.tools;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for running the PDFBox CLI in a headless environment.
 *
 */
class PDFBoxHeadlessTest
{
    final PrintStream originalOut = System.out;
    final PrintStream originalErr = System.err;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @BeforeAll
    public static void setHeadless()
    {
        System.setProperty("java.awt.headless", "true");
    } 

    @BeforeEach
    public void setUpStreams()
    {
        out.reset();
        err.reset();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @AfterEach
    public void restoreStreams()
    {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
   
    @Test
    void isHeadlessTest()
    {
        assumeTrue(GraphicsEnvironment.isHeadless(), "couldn't set headless skipping test");
        assertTrue(GraphicsEnvironment.isHeadless());
    }

    @Test
    void isHeadlessPDFBoxTest()
    {
        final String[] args = {"debug"};
        assumeTrue(GraphicsEnvironment.isHeadless(), "couldn't set headless skipping test");
        assertDoesNotThrow(() -> {
            PDFBox.main(args);
        });
        assertTrue(err.toString().contains("Unmatched argument at index 0: 'debug'"));
    }
}
