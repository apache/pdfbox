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

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;

/**
 * Utility class for form-related tests.
 */
public class TestUtils
{
    /**
     * Extract strings from a PDF field's normal appearance stream.
     *
     * @param field the PDF field
     * @return list of strings found in the appearance stream
     * @throws IOException if an error occurs while parsing the stream
     */
    public static List<String> getStringsFromStream(PDField field) throws IOException
    {
        PDAnnotationWidget widget = field.getWidgets().get(0);
        PDFStreamParser parser = new PDFStreamParser(widget.getNormalAppearanceStream());
        
        List<Object> tokens = parser.parse();
        
        // TODO: improve the string output to better match
        // trimming as Acrobat adds spaces to strings
        // where we don't
        return tokens.stream() //
                .filter(COSString.class::isInstance) //
                .map(COSString.class::cast) //
                .map(COSString::getString) //
                .map(String::trim) //
                .collect(Collectors.toList());
    }
}
