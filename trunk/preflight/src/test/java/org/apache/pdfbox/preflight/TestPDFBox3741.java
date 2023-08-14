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
package org.apache.pdfbox.preflight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class TestPDFBox3741
{
    /**
     * Test whether use of default colorspace without output intent for text output is detected.
     *
     * @throws IOException
     */
    @Test
    void testPDFBox3741() throws IOException
    {
        ValidationResult result = PreflightParser
                .validate(new File("src/test/resources/PDFBOX-3741.pdf"));
        // Error should be:
        // 2.4.3: Invalid Color space, /DeviceGray default for operator "Tj" can't be used without Color Profile
        assertFalse(result.isValid(), "File PDFBOX-3741.pdf should be detected as not PDF/A-1b");
        assertEquals(1, result.getErrorsList().size(), "List should contain one result");
        assertEquals("2.4.3", result.getErrorsList().get(0).getErrorCode());
    }
}
