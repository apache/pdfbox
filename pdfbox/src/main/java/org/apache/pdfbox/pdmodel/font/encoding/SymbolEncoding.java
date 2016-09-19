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
public class SymbolEncoding extends Encoding
{

    private static final int CHAR_CODE = 0;
    private static final int CHAR_NAME = 1;
    
    /**
     * Table of octal character codes and their corresponding names.
     */
    private static final Object[][] SYMBOL_ENCODING_TABLE = {
        {0101, "Alpha"},
        {0102, "Beta"},
        {0103, "Chi"},
        {0104, "Delta"},
        {0105, "Epsilon"},
        {0110, "Eta"},
        {0240, "Euro"},
        {0107, "Gamma"},
        {0301, "Ifraktur"},
        {0111, "Iota"},
        {0113, "Kappa"},
        {0114, "Lambda"},
        {0115, "Mu"},
        {0116, "Nu"},
        {0127, "Omega"},
        {0117, "Omicron"},
        {0106, "Phi"},
        {0120, "Pi"},
        {0131, "Psi"},
        {0302, "Rfraktur"},
        {0122, "Rho"},
        {0123, "Sigma"},
        {0124, "Tau"},
        {0121, "Theta"},
        {0125, "Upsilon"},
        {0241, "Upsilon1"},
        {0130, "Xi"},
        {0132, "Zeta"},
        {0300, "aleph"},
        {0141, "alpha"},
        {0046, "ampersand"},
        {0320, "angle"},
        {0341, "angleleft"},
        {0361, "angleright"},
        {0273, "approxequal"},
        {0253, "arrowboth"},
        {0333, "arrowdblboth"},
        {0337, "arrowdbldown"},
        {0334, "arrowdblleft"},
        {0336, "arrowdblright"},
        {0335, "arrowdblup"},
        {0257, "arrowdown"},
        {0276, "arrowhorizex"},
        {0254, "arrowleft"},
        {0256, "arrowright"},
        {0255, "arrowup"},
        {0275, "arrowvertex"},
        {0052, "asteriskmath"},
        {0174, "bar"},
        {0142, "beta"},
        {0173, "braceleft"},
        {0175, "braceright"},
        {0354, "bracelefttp"},
        {0355, "braceleftmid"},
        {0356, "braceleftbt"},
        {0374, "bracerighttp"},
        {0375, "bracerightmid"},
        {0376, "bracerightbt"},
        {0357, "braceex"},
        {0133, "bracketleft"},
        {0135, "bracketright"},
        {0351, "bracketlefttp"},
        {0352, "bracketleftex"},
        {0353, "bracketleftbt"},
        {0371, "bracketrighttp"},
        {0372, "bracketrightex"},
        {0373, "bracketrightbt"},
        {0267, "bullet"},
        {0277, "carriagereturn"},
        {0143, "chi"},
        {0304, "circlemultiply"},
        {0305, "circleplus"},
        {0247, "club"},
        {0072, "colon"},
        {0054, "comma"},
        {0100, "congruent"},
        {0343, "copyrightsans"},
        {0323, "copyrightserif"},
        {0260, "degree"},
        {0144, "delta"},
        {0250, "diamond"},
        {0270, "divide"},
        {0327, "dotmath"},
        {0070, "eight"},
        {0316, "element"},
        {0274, "ellipsis"},
        {0306, "emptyset"},
        {0145, "epsilon"},
        {0075, "equal"},
        {0272, "equivalence"},
        {0150, "eta"},
        {0041, "exclam"},
        {0044, "existential"},
        {0065, "five"},
        {0246, "florin"},
        {0064, "four"},
        {0244, "fraction"},
        {0147, "gamma"},
        {0321, "gradient"},
        {0076, "greater"},
        {0263, "greaterequal"},
        {0251, "heart"},
        {0245, "infinity"},
        {0362, "integral"},
        {0363, "integraltp"},
        {0364, "integralex"},
        {0365, "integralbt"},
        {0307, "intersection"},
        {0151, "iota"},
        {0153, "kappa"},
        {0154, "lambda"},
        {0074, "less"},
        {0243, "lessequal"},
        {0331, "logicaland"},
        {0330, "logicalnot"},
        {0332, "logicalor"},
        {0340, "lozenge"},
        {0055, "minus"},
        {0242, "minute"},
        {0155, "mu"},
        {0264, "multiply"},
        {0071, "nine"},
        {0317, "notelement"},
        {0271, "notequal"},
        {0313, "notsubset"},
        {0156, "nu"},
        {0043, "numbersign"},
        {0167, "omega"},
        {0166, "omega1"},
        {0157, "omicron"},
        {0061, "one"},
        {0050, "parenleft"},
        {0051, "parenright"},
        {0346, "parenlefttp"},
        {0347, "parenleftex"},
        {0350, "parenleftbt"},
        {0366, "parenrighttp"},
        {0367, "parenrightex"},
        {0370, "parenrightbt"},
        {0266, "partialdiff"},
        {0045, "percent"},
        {0056, "period"},
        {0136, "perpendicular"},
        {0146, "phi"},
        {0152, "phi1"},
        {0160, "pi"},
        {0053, "plus"},
        {0261, "plusminus"},
        {0325, "product"},
        {0314, "propersubset"},
        {0311, "propersuperset"},
        {0265, "proportional"},
        {0171, "psi"},
        {0077, "question"},
        {0326, "radical"},
        {0140, "radicalex"},
        {0315, "reflexsubset"},
        {0312, "reflexsuperset"},
        {0342, "registersans"},
        {0322, "registerserif"},
        {0162, "rho"},
        {0262, "second"},
        {0073, "semicolon"},
        {0067, "seven"},
        {0163, "sigma"},
        {0126, "sigma1"},
        {0176, "similar"},
        {0066, "six"},
        {0057, "slash"},
        {0040, "space"},
        {0252, "spade"},
        {0047, "suchthat"},
        {0345, "summation"},
        {0164, "tau"},
        {0134, "therefore"},
        {0161, "theta"},
        {0112, "theta1"},
        {0063, "three"},
        {0344, "trademarksans"},
        {0324, "trademarkserif"},
        {0062, "two"},
        {0137, "underscore"},
        {0310, "union"},
        {0042, "universal"},
        {0165, "upsilon"},
        {0303, "weierstrass"},
        {0170, "xi"},
        {0060, "zero"},
        {0172, "zeta"}       
    };
    
    /**
     * Singleton instance of this class.
     */
    public static final SymbolEncoding INSTANCE = new SymbolEncoding();

    /**
     * Constructor.
     */
    public SymbolEncoding()
    {
        for (Object[] encodingEntry : SYMBOL_ENCODING_TABLE)
        {
            add((Integer) encodingEntry[CHAR_CODE], encodingEntry[CHAR_NAME].toString());
        }
    }
    
    @Override
    public COSBase getCOSObject()
    {
        return COSName.getPDFName("SymbolEncoding");
    }

    @Override
    public String getEncodingName()
    {
        return "SymbolEncoding";
    }
}
