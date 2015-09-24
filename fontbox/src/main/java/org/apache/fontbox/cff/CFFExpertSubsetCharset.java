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

    private static final int CHAR_CODE = 0;
    private static final int CHAR_NAME = 1;
    
    /**
     * Table of character codes and their corresponding names.
     */
    private static final Object[][] CFF_EXPERT_SUBSET_CHARSET_TABLE = {
            {0, ".notdef"},
            {1, "space"},
            {231, "dollaroldstyle"},
            {232, "dollarsuperior"},
            {235, "parenleftsuperior"},
            {236, "parenrightsuperior"},
            {237, "twodotenleader"},
            {238, "onedotenleader"},
            {13, "comma"},
            {14, "hyphen"},
            {15, "period"},
            {99, "fraction"},
            {239, "zerooldstyle"},
            {240, "oneoldstyle"},
            {241, "twooldstyle"},
            {242, "threeoldstyle"},
            {243, "fouroldstyle"},
            {244, "fiveoldstyle"},
            {245, "sixoldstyle"},
            {246, "sevenoldstyle"},
            {247, "eightoldstyle"},
            {248, "nineoldstyle"},
            {27, "colon"},
            {28, "semicolon"},
            {249, "commasuperior"},
            {250, "threequartersemdash"},
            {251, "periodsuperior"},
            {253, "asuperior"},
            {254, "bsuperior"},
            {255, "centsuperior"},
            {256, "dsuperior"},
            {257, "esuperior"},
            {258, "isuperior"},
            {259, "lsuperior"},
            {260, "msuperior"},
            {261, "nsuperior"},
            {262, "osuperior"},
            {263, "rsuperior"},
            {264, "ssuperior"},
            {265, "tsuperior"},
            {266, "ff"},
            {109, "fi"},
            {110, "fl"},
            {267, "ffi"},
            {268, "ffl"},
            {269, "parenleftinferior"},
            {270, "parenrightinferior"},
            {272, "hyphensuperior"},
            {300, "colonmonetary"},
            {301, "onefitted"},
            {302, "rupiah"},
            {305, "centoldstyle"},
            {314, "figuredash"},
            {315, "hypheninferior"},
            {158, "onequarter"},
            {155, "onehalf"},
            {163, "threequarters"},
            {320, "oneeighth"},
            {321, "threeeighths"},
            {322, "fiveeighths"},
            {323, "seveneighths"},
            {324, "onethird"},
            {325, "twothirds"},
            {326, "zerosuperior"},
            {150, "onesuperior"},
            {164, "twosuperior"},
            {169, "threesuperior"},
            {327, "foursuperior"},
            {328, "fivesuperior"},
            {329, "sixsuperior"},
            {330, "sevensuperior"},
            {331, "eightsuperior"},
            {332, "ninesuperior"},
            {333, "zeroinferior"},
            {334, "oneinferior"},
            {335, "twoinferior"},
            {336, "threeinferior"},
            {337, "fourinferior"},
            {338, "fiveinferior"},
            {339, "sixinferior"},
            {340, "seveninferior"},
            {341, "eightinferior"},
            {342, "nineinferior"},
            {343, "centinferior"},
            {344, "dollarinferior"},
            {345, "periodinferior"},
            {346, "commainferior"}
    };
    
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
        for (Object[] charsetEntry : CFF_EXPERT_SUBSET_CHARSET_TABLE)
        {
            INSTANCE.addSID(gid++, (Integer) charsetEntry[CHAR_CODE], charsetEntry[CHAR_NAME].toString());
        }
    }
}