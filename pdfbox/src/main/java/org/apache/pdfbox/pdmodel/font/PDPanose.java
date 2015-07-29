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

package org.apache.pdfbox.pdmodel.font;

import java.util.Arrays;

/**
 * Represents the "Panose" entry of a FontDescriptor's Style dictionary. This is a sequence of 12
 * bytes which contain both the TTF sFamilyClass and PANOSE classification bytes. 
 *
 * @author John Hewson
 */
public class PDPanose
{
    private final byte[] bytes;

    public PDPanose(byte[] bytes)
    {
        this.bytes = bytes;
    }

    /**
     * The font family class and subclass ID bytes, given in the sFamilyClass field of the
     * “OS/2” table in a TrueType font.
     * 
     * @see <a href="http://www.microsoft.com/typography/otspec/ibmfc.htm">http://www.microsoft.com/typography/otspec/ibmfc.htm</a>
     */
    public int getFamilyClass()
    {
        return bytes[0] << 8 | bytes[1]; 
    }

    /**
     * Ten bytes for the PANOSE classification number for the font.
     * 
     * @see <a href="http://www.monotype.com/services/pan1">http://www.monotype.com/services/pan1</a>
     */
    public PDPanoseClassification getPanose()
    {
        byte[] panose = Arrays.copyOfRange(bytes, 2, 12);
        return new PDPanoseClassification(panose);
    }
}
