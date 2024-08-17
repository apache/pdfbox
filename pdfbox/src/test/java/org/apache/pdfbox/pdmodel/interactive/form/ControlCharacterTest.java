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
package org.apache.pdfbox.pdmodel.interactive.form;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test handling some special characters when setting a fields value.
 * 
 * Compare the results of setting the values using PDFBox with setting the values
 * via Acrobat using JavaScript and manual input.
 * 
 * The JavaScript used for acrobat is
 * 
 * <pre>
 * {@code
 * this.getField("acrobat-nul").value = "NUL\0NUL";
 * this.getField("acrobat-tab").value = "TAB\tTAB";
 * this.getField("acrobat-space").value = "SPACE SPACE";
 * this.getField("acrobat-cr").value = "CR\rCR";
 * this.getField("acrobat-lf").value = "LF\nLF";
 * this.getField("acrobat-crlf").value = "CRLF\r\nCRLF";
 * this.getField("acrobat-lfcr").value = "LFCR\n\rLFCR";
 * this.getField("acrobat-linebreak").value = "linebreak\u2028linebreak";
 * this.getField("acrobat-paragraphbreak").value = "paragraphbreak\u2029paragraphbreak";
 * }
 * </pre>
 * 
 * @see <a href="https://issues.apache.org/jira/browse/PDFBOX-3461">https://issues.apache.org/jira/browse/PDFBOX-3461</a>
 * 
 */
class ControlCharacterTest
{
    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/form");
    private static final String NAME_OF_PDF = "ControlCharacters.pdf";

    private PDDocument document;
    private PDAcroForm acroForm;

    @BeforeEach
    public void setUp() throws IOException
    {
        document = Loader.loadPDF(new File(IN_DIR, NAME_OF_PDF));
        acroForm = document.getDocumentCatalog().getAcroForm();
    }
    
    @Test
    void characterNUL() throws IOException
    {
        PDField field = acroForm.getField("pdfbox-nul");
        assertThrows(IllegalArgumentException.class, () -> field.setValue("NUL\0NUL"));
    }

    /*
     * No direct comparison to how Acrobat sets the value
     * as we don't position with tabs.
     */
    @Test
    void characterTAB() throws IOException
    {
        PDField field = acroForm.getField("pdfbox-tab");
        field.setValue("TAB\tTAB");

        List<String> pdfboxValues = getStringsFromStream(field);
        pdfboxValues.forEach(token -> assertEquals("TAB", token));
    }

    private static Stream<Arguments> provideParameters()
    {
        return Stream.of(
                Arguments.of("space", "SPACE SPACE"),
                Arguments.of("cr", "CR\rCR"),
                Arguments.of("lf", "LF\nLF"),
                Arguments.of("crlf", "CRLF\r\nCRLF"),
                Arguments.of("lfcr", "LFCR\n\rLFCR"),
                Arguments.of("linebreak", "linebreak\u2028linebreak"),
                Arguments.of("paragraphbreak", "paragraphbreak\u2029paragraphbreak")
        );
    }
    @ParameterizedTest
    @MethodSource("provideParameters")
    void testCharacter(String nameSuffix, String value) throws IOException
    {
        PDField field = acroForm.getField("pdfbox-" + nameSuffix);
        field.setValue(value);

        List<String> pdfboxValues = getStringsFromStream(field);
        List<String> acrobatValues = getStringsFromStream(acroForm.getField("acrobat-" + nameSuffix));

        assertEquals(pdfboxValues, acrobatValues);
    }

    @AfterEach
    public void tearDown() throws IOException
    {
        document.close();
    }
    
    private List<String> getStringsFromStream(PDField field) throws IOException
    {
        PDAnnotationWidget widget = field.getWidgets().get(0);
        PDFStreamParser parser = new PDFStreamParser(
                widget.getNormalAppearanceStream());
        
        List<Object> tokens = parser.parse();
        
        // TODO: improve the string output to better match
        // trimming as Acrobat adds spaces to strings
        // where we don't
        return tokens.stream() //
                .filter(t -> t instanceof COSString) //
                .map(t -> ((COSString) t).getString().trim()) //
                .collect(Collectors.toList());
    }
}
