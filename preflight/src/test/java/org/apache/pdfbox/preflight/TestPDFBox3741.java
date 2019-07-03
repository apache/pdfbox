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

import java.io.IOException;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.apache.pdfbox.preflight.parser.PreflightParser;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class TestPDFBox3741
{
    /**
     * Test whether use of default colorspace without output intent for text output is detected.
     *
     * @throws IOException
     */
    @Test
    public void testPDFBox3741() throws IOException
    {
        DataSource ds = new FileDataSource("src/test/resources/PDFBOX-3741.pdf");
        PreflightParser parser = new PreflightParser(ds);
        parser.parse();
        PreflightDocument document = parser.getPreflightDocument();
        document.validate();
        ValidationResult result = document.getResult();
        document.close();

        // Error should be:
        // 2.4.3: Invalid Color space, /DeviceGray default for operator "Tj" can't be used without Color Profile
        Assert.assertFalse("File PDFBOX-3741.pdf should be detected as not PDF/A-1b", result.isValid());
        Assert.assertEquals("List should contain one result", 1, result.getErrorsList().size());
        Assert.assertEquals("2.4.3", result.getErrorsList().get(0).getErrorCode());
    }
}
