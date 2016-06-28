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
import org.apache.pdfbox.cos.COSName;

/**
 * This is an interface to a text encoder.
 */
public class MacExpertEncoding extends Encoding
{

    private static final int CHAR_CODE = 0;
    private static final int CHAR_NAME = 1;
    
    /**
     * Table of octal character codes and their corresponding names.
     */
    private static final Object[][] MAC_EXPERT_ENCODING_TABLE = {
        {0276, "AEsmall"},
        {0207, "Aacutesmall"},
        {0211, "Acircumflexsmall"},
        {047, "Acutesmall"},
        {0212, "Adieresissmall"},
        {0210, "Agravesmall"},
        {0214, "Aringsmall"},
        {0141, "Asmall"},
        {0213, "Atildesmall"},
        {0363, "Brevesmall"},
        {0142, "Bsmall"},
        {0256, "Caronsmall"},
        {0215, "Ccedillasmall"},
        {0311, "Cedillasmall"},
        {0136, "Circumflexsmall"},
        {0143, "Csmall"},
        {0254, "Dieresissmall"},
        {0372, "Dotaccentsmall"},
        {0144, "Dsmall"},
        {0216, "Eacutesmall"},
        {0220, "Ecircumflexsmall"},
        {0221, "Edieresissmall"},
        {0217, "Egravesmall"},
        {0145, "Esmall"},
        {0104, "Ethsmall"},
        {0146, "Fsmall"},
        {0140, "Gravesmall"},
        {0147, "Gsmall"},
        {0150, "Hsmall"},
        {042, "Hungarumlautsmall"},
        {0222, "Iacutesmall"},
        {0224, "Icircumflexsmall"},
        {0225, "Idieresissmall"},
        {0223, "Igravesmall"},
        {0151, "Ismall"},
        {0152, "Jsmall"},
        {0153, "Ksmall"},
        {0302, "Lslashsmall"},
        {0154, "Lsmall"},
        {0364, "Macronsmall"},
        {0155, "Msmall"},
        {0156, "Nsmall"},
        {0226, "Ntildesmall"},
        {0317, "OEsmall"},
        {0227, "Oacutesmall"},
        {0231, "Ocircumflexsmall"},
        {0232, "Odieresissmall"},
        {0362, "Ogoneksmall"},
        {0230, "Ogravesmall"},
        {0277, "Oslashsmall"},
        {0157, "Osmall"},
        {0233, "Otildesmall"},
        {0160, "Psmall"},
        {0161, "Qsmall"},
        {0373, "Ringsmall"},
        {0162, "Rsmall"},
        {0247, "Scaronsmall"},
        {0163, "Ssmall"},
        {0271, "Thornsmall"},
        {0176, "Tildesmall"},
        {0164, "Tsmall"},
        {0234, "Uacutesmall"},
        {0236, "Ucircumflexsmall"},
        {0237, "Udieresissmall"},
        {0235, "Ugravesmall"},
        {0165, "Usmall"},
        {0166, "Vsmall"},
        {0167, "Wsmall"},
        {0170, "Xsmall"},
        {0264, "Yacutesmall"},
        {0330, "Ydieresissmall"},
        {0171, "Ysmall"},
        {0275, "Zcaronsmall"},
        {0172, "Zsmall"},
        {046, "ampersandsmall"},
        {0201, "asuperior"},
        {0365, "bsuperior"},
        {0251, "centinferior"},
        {043, "centoldstyle"},
        {0202, "centsuperior"},
        {072, "colon"},
        {0173, "colonmonetary"},
        {054, "comma"},
        {0262, "commainferior"},
        {0370, "commasuperior"},
        {0266, "dollarinferior"},
        {044, "dollaroldstyle"},
        {045, "dollarsuperior"},
        {0353, "dsuperior"},
        {0245, "eightinferior"},
        {070, "eightoldstyle"},
        {0241, "eightsuperior"},
        {0344, "esuperior"},
        {0326, "exclamdownsmall"},
        {041, "exclamsmall"},
        {0126, "ff"},
        {0131, "ffi"},
        {0132, "ffl"},
        {0127, "fi"},
        {0320, "figuredash"},
        {0114, "fiveeighths"},
        {0260, "fiveinferior"},
        {065, "fiveoldstyle"},
        {0336, "fivesuperior"},
        {0130, "fl"},
        {0242, "fourinferior"},
        {064, "fouroldstyle"},
        {0335, "foursuperior"},
        {057, "fraction"},
        {055, "hyphen"},
        {0137, "hypheninferior"},
        {0321, "hyphensuperior"},
        {0351, "isuperior"},
        {0361, "lsuperior"},
        {0367, "msuperior"},
        {0273, "nineinferior"},
        {071, "nineoldstyle"},
        {0341, "ninesuperior"},
        {0366, "nsuperior"},
        {053, "onedotenleader"},
        {0112, "oneeighth"},
        {0174, "onefitted"},
        {0110, "onehalf"},
        {0301, "oneinferior"},
        {061, "oneoldstyle"},
        {0107, "onequarter"},
        {0332, "onesuperior"},
        {0116, "onethird"},
        {0257, "osuperior"},
        {0133, "parenleftinferior"},
        {050, "parenleftsuperior"},
        {0135, "parenrightinferior"},
        {051, "parenrightsuperior"},
        {056, "period"},
        {0263, "periodinferior"},
        {0371, "periodsuperior"},
        {0300, "questiondownsmall"},
        {077, "questionsmall"},
        {0345, "rsuperior"},
        {0175, "rupiah"},
        {073, "semicolon"},
        {0115, "seveneighths"},
        {0246, "seveninferior"},
        {067, "sevenoldstyle"},
        {0340, "sevensuperior"},
        {0244, "sixinferior"},
        {066, "sixoldstyle"},
        {0337, "sixsuperior"},
        {040, "space"},
        {0352, "ssuperior"},
        {0113, "threeeighths"},
        {0243, "threeinferior"},
        {063, "threeoldstyle"},
        {0111, "threequarters"},
        {075, "threequartersemdash"},
        {0334, "threesuperior"},
        {0346, "tsuperior"},
        {052, "twodotenleader"},
        {0252, "twoinferior"},
        {062, "twooldstyle"},
        {0333, "twosuperior"},
        {0117, "twothirds"},
        {0274, "zeroinferior"},
        {060, "zerooldstyle"},
        {0342, "zerosuperior"}
    };
    
    /**
     * Singleton instance of this class.
     */
    public static final MacExpertEncoding INSTANCE = new MacExpertEncoding();

    /**
     * Constructor.
     */
    public MacExpertEncoding()
    {
        for (Object[] encodingEntry : MAC_EXPERT_ENCODING_TABLE)
        {
            add((Integer) encodingEntry[CHAR_CODE], encodingEntry[CHAR_NAME].toString());
        }
    }
    
    @Override
    public COSBase getCOSObject()
    {
        return COSName.MAC_EXPERT_ENCODING;
    }

    @Override
    public String getEncodingName()
    {
        return "MacExpertEncoding";
    }
}
