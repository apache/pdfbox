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
package org.apache.pdfbox.pdmodel.font.encoding;

import org.apache.pdfbox.cos.COSBase;

/**
 * This is the Mac OS Roman encoding, which is similar to the
 * MacRomanEncoding with the addition of 15 entries
 */
public class MacOSRomanEncoding extends MacRomanEncoding
{

    private static final int CHAR_CODE = 0;
    private static final int CHAR_NAME = 1;
    
    /**
     * Table of octal character codes and their corresponding names
     * on top of {@link MacRomanEncoding}.
     */
    private static final Object[][] MAC_OS_ROMAN_ENCODING_TABLE = {
            {255, "notequal"},
            {260, "infinity"},
            {262, "lessequal"},
            {263, "greaterequal"},
            {266, "partialdiff"},
            {267, "summation"},
            {270, "product"},
            {271, "pi"},
            {272, "integral"},
            {275, "Omega"},
            {303, "radical"},
            {305, "approxequal"},
            {306, "Delta"},
            {327, "lozenge"},
            {333, "Euro"},
            {360, "apple"}    
    };
    
    /**
     * Singleton instance of this class.
     *
     * @since Apache PDFBox 2.0.0
     */
    public static final MacOSRomanEncoding INSTANCE = new MacOSRomanEncoding();

    /**
     * Constructor.
     */
    public MacOSRomanEncoding()
    {
        super();

        // differences and additions to MacRomanEncoding
        for (Object[] encodingEntry : MAC_OS_ROMAN_ENCODING_TABLE)
        {
            add((Integer) encodingEntry[CHAR_CODE], encodingEntry[CHAR_NAME].toString());
        }

    }

    @Override
    public COSBase getCOSObject()
    {
        return null;
    }
}
