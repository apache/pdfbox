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
package org.apache.pdfbox_ai2.pdmodel.font.encoding;

import org.apache.pdfbox_ai2.cos.COSBase;

/**
 * This is the Mac OS Roman encoding, which is similar to the
 * MacRomanEncoding with the addition of 15 entries
 */
public class MacOSRomanEncoding extends MacRomanEncoding
{

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
        add(255, "notequal");
        add(260, "infinity");
        add(262, "lessequal");
        add(263, "greaterequal");
        add(266, "partialdiff");
        add(267, "summation");
        add(270, "product");
        add(271, "pi");
        add(272, "integral");
        add(275, "Omega");
        add(303, "radical");
        add(305, "approxequal");
        add(306, "Delta");
        add(327, "lozenge");
        add(333, "Euro");
        add(360, "apple");
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return null;
    }
}
