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

import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for running the PDFBox CLI in a headless environment.
 *
 */
class PDFBoxNonHeadlessTest
{
    final PrintStream originalOut = System.out;
    final PrintStream originalErr = System.err;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();

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
    void isNonHeadlessTest()
    {
        assumeFalse(GraphicsEnvironment.isHeadless(), "running in a headless environment skipping test");
        assertFalse(GraphicsEnvironment.isHeadless());
    }

    @Test
    void isNonHeadlessPDFBoxTest()
    {
        final String[] args = {"debug"};
        assumeFalse(GraphicsEnvironment.isHeadless(), "running in a headless environment skipping test");
        assertDoesNotThrow(() -> {
            PDFBox.main(args);
        });
        assertFalse(err.toString().contains("Unmatched argument at index 0: 'debug'"));
    }
}
