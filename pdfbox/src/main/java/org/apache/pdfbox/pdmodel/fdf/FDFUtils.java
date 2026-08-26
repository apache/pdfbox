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
package org.apache.pdfbox.pdmodel.fdf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FDFUtils {

    private static final Logger LOG = LogManager.getLogger(FDFDocument.class);

    /**
     * Escapes special characters for use in XML 1.0.
     * <p>
     * Characters that are not permitted in XML 1.0 are replaced with the
     * Unicode replacement character (U+FFFD). If one or more characters are
     * replaced, the number of replacements is logged at INFO level.
     * </p>
     *
     * @param input the string to be escaped.
     *
     * @return the resulting string with XML special characters escaped and
     *         characters invalid in XML 1.0 replaced with U+FFFD.
     */
    static String escapeXML10(String input)
    {
        StringBuilder escapedXML = new StringBuilder();
        int invalidCount = 0;
        int i = 0;
        while (i < input.length())
        {
            int cp = input.codePointAt(i);
            int charCount = Character.charCount(cp);

            if (!isValidXML10Char(cp))
            {
                invalidCount++;
                escapedXML.append('\uFFFD');
                i += charCount;
                continue;
            }

            switch (cp)
            {
            case '<':
                escapedXML.append("&lt;");
                break;
            case '>':
                escapedXML.append("&gt;");
                break;
            case '\"':
                escapedXML.append("&quot;");
                break;
            case '&':
                escapedXML.append("&amp;");
                break;
            case '\'':
                escapedXML.append("&apos;");
                break;
            default:
                if (cp > 0x7e)
                {
                    escapedXML.append("&#").append(cp).append(';');
                }
                else
                {
                    escapedXML.appendCodePoint(cp);
                }
            }
            i += charCount;
        }

        if (invalidCount > 0 && LOG.isInfoEnabled())
        {
            LOG.info("Replaced " + invalidCount + " character(s) invalid in XML 1.0 with U+FFFD");
        }


        return escapedXML.toString();
    }

    private static boolean isValidXML10Char(int cp)
    {
        return cp == 0x9 || cp == 0xA || cp == 0xD
            || (cp >= 0x20 && cp <= 0xD7FF)
            || (cp >= 0xE000 && cp <= 0xFFFD)
            || (cp >= 0x10000 && cp <= 0x10FFFF);
    }
}
