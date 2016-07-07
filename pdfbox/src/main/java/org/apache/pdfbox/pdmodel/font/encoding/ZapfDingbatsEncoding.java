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
public class ZapfDingbatsEncoding extends Encoding
{

    private static final int CHAR_CODE = 0;
    private static final int CHAR_NAME = 1;
    
    /**
     * Table of octal character codes and their corresponding names.
     */
    private static final Object[][] ZAPFDINGBATS_ENCODING_TABLE = {
        {040, "space"},
        {041, "a1"},
        {042, "a2"},
        {043, "a202"},
        {044, "a3"},
        {045, "a4"},
        {046, "a5"},
        {047, "a119"},
        {050, "a118"},
        {051, "a117"},
        {052, "a11"},
        {053, "a12"},
        {054, "a13"},
        {055, "a14"},
        {056, "a15"},
        {057, "a16"},
        {060, "a105"},
        {061, "a17"},
        {062, "a18"},
        {063, "a19"},
        {064, "a20"},
        {065, "a21"},
        {066, "a22"},
        {067, "a23"},
        {070, "a24"},
        {071, "a25"},
        {072, "a26"},
        {073, "a27"},
        {074, "a28"},
        {075, "a6"},
        {076, "a7"},
        {077, "a8"},
        {0100, "a9"},
        {0101, "a10"},
        {0102, "a29"},
        {0103, "a30"},
        {0104, "a31"},
        {0105, "a32"},
        {0106, "a33"},
        {0107, "a34"},
        {0110, "a35"},
        {0111, "a36"},
        {0112, "a37"},
        {0113, "a38"},
        {0114, "a39"},
        {0115, "a40"},
        {0116, "a41"},
        {0117, "a42"},
        {0120, "a43"},
        {0121, "a44"},
        {0122, "a45"},
        {0123, "a46"},
        {0124, "a47"},
        {0125, "a48"},
        {0126, "a49"},
        {0127, "a50"},
        {0130, "a51"},
        {0131, "a52"},
        {0132, "a53"},
        {0133, "a54"},
        {0134, "a55"},
        {0135, "a56"},
        {0136, "a57"},
        {0137, "a58"},
        {0140, "a59"},
        {0141, "a60"},
        {0142, "a61"},
        {0143, "a62"},
        {0144, "a63"},
        {0145, "a64"},
        {0146, "a65"},
        {0147, "a66"},
        {0150, "a67"},
        {0151, "a68"},
        {0152, "a69"},
        {0153, "a70"},
        {0154, "a71"},
        {0155, "a72"},
        {0156, "a73"},
        {0157, "a74"},
        {0160, "a203"},
        {0161, "a75"},
        {0162, "a204"},
        {0163, "a76"},
        {0164, "a77"},
        {0165, "a78"},
        {0166, "a79"},
        {0167, "a81"},
        {0170, "a82"},
        {0171, "a83"},
        {0172, "a84"},
        {0173, "a97"},
        {0174, "a98"},
        {0175, "a99"},
        {0176, "a100"},
        {0241, "a101"},
        {0242, "a102"},
        {0243, "a103"},
        {0244, "a104"},
        {0245, "a106"},
        {0246, "a107"},
        {0247, "a108"},
        {0250, "a112"},
        {0251, "a111"},
        {0252, "a110"},
        {0253, "a109"},
        {0254, "a120"},
        {0255, "a121"},
        {0256, "a122"},
        {0257, "a123"},
        {0260, "a124"},
        {0261, "a125"},
        {0262, "a126"},
        {0263, "a127"},
        {0264, "a128"},
        {0265, "a129"},
        {0266, "a130"},
        {0267, "a131"},
        {0270, "a132"},
        {0271, "a133"},
        {0272, "a134"},
        {0273, "a135"},
        {0274, "a136"},
        {0275, "a137"},
        {0276, "a138"},
        {0277, "a139"},
        {0300, "a140"},
        {0301, "a141"},
        {0302, "a142"},
        {0303, "a143"},
        {0304, "a144"},
        {0305, "a145"},
        {0306, "a146"},
        {0307, "a147"},
        {0310, "a148"},
        {0311, "a149"},
        {0312, "a150"},
        {0313, "a151"},
        {0314, "a152"},
        {0315, "a153"},
        {0316, "a154"},
        {0317, "a155"},
        {0320, "a156"},
        {0321, "a157"},
        {0322, "a158"},
        {0323, "a159"},
        {0324, "a160"},
        {0325, "a161"},
        {0326, "a163"},
        {0327, "a164"},
        {0330, "a196"},
        {0331, "a165"},
        {0332, "a192"},
        {0333, "a166"},
        {0334, "a167"},
        {0335, "a168"},
        {0336, "a169"},
        {0337, "a170"},
        {0340, "a171"},
        {0341, "a172"},
        {0342, "a173"},
        {0343, "a162"},
        {0344, "a174"},
        {0345, "a175"},
        {0346, "a176"},
        {0347, "a177"},
        {0350, "a178"},
        {0351, "a179"},
        {0352, "a193"},
        {0353, "a180"},
        {0354, "a199"},
        {0355, "a181"},
        {0356, "a200"},
        {0357, "a182"},
        {0361, "a201"},
        {0362, "a183"},
        {0363, "a184"},
        {0364, "a197"},
        {0365, "a185"},
        {0366, "a194"},
        {0367, "a198"},
        {0370, "a186"},
        {0371, "a195"},
        {0372, "a187"},
        {0373, "a188"},
        {0374, "a189"},
        {0375, "a190"},
        {0376, "a191"}
    };
    
    /**
     * Singleton instance of this class.
     */
    public static final ZapfDingbatsEncoding INSTANCE = new ZapfDingbatsEncoding();

    /**
     * Constructor.
     */
    public ZapfDingbatsEncoding()
    {
        for (Object[] encodingEntry : ZAPFDINGBATS_ENCODING_TABLE)
        {
            add((Integer) encodingEntry[CHAR_CODE], encodingEntry[CHAR_NAME].toString());
        }
    }
    
    @Override
    public COSBase getCOSObject()
    {
        return COSName.getPDFName("ZapfDingbatsEncoding");
    }

    @Override
    public String getEncodingName()
    {
        return "ZapfDingbatsEncoding";
    }
}
