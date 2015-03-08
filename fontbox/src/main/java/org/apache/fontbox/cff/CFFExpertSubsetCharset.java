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
package org.apache.fontbox.cff;


/**
 * This is specialized CFFCharset. It's used if the CharsetId of a font is set to 2.
 * 
 * @author Villu Ruusmann
 */
public final class CFFExpertSubsetCharset extends CFFCharset
{

    private CFFExpertSubsetCharset()
    {
        super(false);
    }

    /**
     * Returns an instance of the CFFExpertSubsetCharset class.
     * @return an instance of CFFExpertSubsetCharset
     */
    public static CFFExpertSubsetCharset getInstance()
    {
        return CFFExpertSubsetCharset.INSTANCE;
    }

    private static final CFFExpertSubsetCharset INSTANCE = new CFFExpertSubsetCharset();

    static
    {
        int gid = 0;
        INSTANCE.addSID(gid++, 0, ".notdef");
        INSTANCE.addSID(gid++, 1, "space");
        INSTANCE.addSID(gid++, 231, "dollaroldstyle");
        INSTANCE.addSID(gid++, 232, "dollarsuperior");
        INSTANCE.addSID(gid++, 235, "parenleftsuperior");
        INSTANCE.addSID(gid++, 236, "parenrightsuperior");
        INSTANCE.addSID(gid++, 237, "twodotenleader");
        INSTANCE.addSID(gid++, 238, "onedotenleader");
        INSTANCE.addSID(gid++, 13, "comma");
        INSTANCE.addSID(gid++, 14, "hyphen");
        INSTANCE.addSID(gid++, 15, "period");
        INSTANCE.addSID(gid++, 99, "fraction");
        INSTANCE.addSID(gid++, 239, "zerooldstyle");
        INSTANCE.addSID(gid++, 240, "oneoldstyle");
        INSTANCE.addSID(gid++, 241, "twooldstyle");
        INSTANCE.addSID(gid++, 242, "threeoldstyle");
        INSTANCE.addSID(gid++, 243, "fouroldstyle");
        INSTANCE.addSID(gid++, 244, "fiveoldstyle");
        INSTANCE.addSID(gid++, 245, "sixoldstyle");
        INSTANCE.addSID(gid++, 246, "sevenoldstyle");
        INSTANCE.addSID(gid++, 247, "eightoldstyle");
        INSTANCE.addSID(gid++, 248, "nineoldstyle");
        INSTANCE.addSID(gid++, 27, "colon");
        INSTANCE.addSID(gid++, 28, "semicolon");
        INSTANCE.addSID(gid++, 249, "commasuperior");
        INSTANCE.addSID(gid++, 250, "threequartersemdash");
        INSTANCE.addSID(gid++, 251, "periodsuperior");
        INSTANCE.addSID(gid++, 253, "asuperior");
        INSTANCE.addSID(gid++, 254, "bsuperior");
        INSTANCE.addSID(gid++, 255, "centsuperior");
        INSTANCE.addSID(gid++, 256, "dsuperior");
        INSTANCE.addSID(gid++, 257, "esuperior");
        INSTANCE.addSID(gid++, 258, "isuperior");
        INSTANCE.addSID(gid++, 259, "lsuperior");
        INSTANCE.addSID(gid++, 260, "msuperior");
        INSTANCE.addSID(gid++, 261, "nsuperior");
        INSTANCE.addSID(gid++, 262, "osuperior");
        INSTANCE.addSID(gid++, 263, "rsuperior");
        INSTANCE.addSID(gid++, 264, "ssuperior");
        INSTANCE.addSID(gid++, 265, "tsuperior");
        INSTANCE.addSID(gid++, 266, "ff");
        INSTANCE.addSID(gid++, 109, "fi");
        INSTANCE.addSID(gid++, 110, "fl");
        INSTANCE.addSID(gid++, 267, "ffi");
        INSTANCE.addSID(gid++, 268, "ffl");
        INSTANCE.addSID(gid++, 269, "parenleftinferior");
        INSTANCE.addSID(gid++, 270, "parenrightinferior");
        INSTANCE.addSID(gid++, 272, "hyphensuperior");
        INSTANCE.addSID(gid++, 300, "colonmonetary");
        INSTANCE.addSID(gid++, 301, "onefitted");
        INSTANCE.addSID(gid++, 302, "rupiah");
        INSTANCE.addSID(gid++, 305, "centoldstyle");
        INSTANCE.addSID(gid++, 314, "figuredash");
        INSTANCE.addSID(gid++, 315, "hypheninferior");
        INSTANCE.addSID(gid++, 158, "onequarter");
        INSTANCE.addSID(gid++, 155, "onehalf");
        INSTANCE.addSID(gid++, 163, "threequarters");
        INSTANCE.addSID(gid++, 320, "oneeighth");
        INSTANCE.addSID(gid++, 321, "threeeighths");
        INSTANCE.addSID(gid++, 322, "fiveeighths");
        INSTANCE.addSID(gid++, 323, "seveneighths");
        INSTANCE.addSID(gid++, 324, "onethird");
        INSTANCE.addSID(gid++, 325, "twothirds");
        INSTANCE.addSID(gid++, 326, "zerosuperior");
        INSTANCE.addSID(gid++, 150, "onesuperior");
        INSTANCE.addSID(gid++, 164, "twosuperior");
        INSTANCE.addSID(gid++, 169, "threesuperior");
        INSTANCE.addSID(gid++, 327, "foursuperior");
        INSTANCE.addSID(gid++, 328, "fivesuperior");
        INSTANCE.addSID(gid++, 329, "sixsuperior");
        INSTANCE.addSID(gid++, 330, "sevensuperior");
        INSTANCE.addSID(gid++, 331, "eightsuperior");
        INSTANCE.addSID(gid++, 332, "ninesuperior");
        INSTANCE.addSID(gid++, 333, "zeroinferior");
        INSTANCE.addSID(gid++, 334, "oneinferior");
        INSTANCE.addSID(gid++, 335, "twoinferior");
        INSTANCE.addSID(gid++, 336, "threeinferior");
        INSTANCE.addSID(gid++, 337, "fourinferior");
        INSTANCE.addSID(gid++, 338, "fiveinferior");
        INSTANCE.addSID(gid++, 339, "sixinferior");
        INSTANCE.addSID(gid++, 340, "seveninferior");
        INSTANCE.addSID(gid++, 341, "eightinferior");
        INSTANCE.addSID(gid++, 342, "nineinferior");
        INSTANCE.addSID(gid++, 343, "centinferior");
        INSTANCE.addSID(gid++, 344, "dollarinferior");
        INSTANCE.addSID(gid++, 345, "periodinferior");
        INSTANCE.addSID(gid++, 346, "commainferior");
    }
}