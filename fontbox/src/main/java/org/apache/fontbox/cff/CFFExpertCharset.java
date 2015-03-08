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
 * This is specialized CFFCharset. It's used if the CharsetId of a font is set to 1.
 * 
 * @author Villu Ruusmann
 */
public final class CFFExpertCharset extends CFFCharset
{

    private CFFExpertCharset()
    {
        super(false);
    }

    /**
     * Returns an instance of the CFFExpertCharset class.
     * @return an instance of CFFExpertCharset
     */
    public static CFFExpertCharset getInstance()
    {
        return CFFExpertCharset.INSTANCE;
    }

    private static final CFFExpertCharset INSTANCE = new CFFExpertCharset();

    static
    {
        int gid = 0;
        INSTANCE.addSID(gid++, 0, ".notdef");
        INSTANCE.addSID(gid++, 1, "space");
        INSTANCE.addSID(gid++, 229, "exclamsmall");
        INSTANCE.addSID(gid++, 230, "Hungarumlautsmall");
        INSTANCE.addSID(gid++, 231, "dollaroldstyle");
        INSTANCE.addSID(gid++, 232, "dollarsuperior");
        INSTANCE.addSID(gid++, 233, "ampersandsmall");
        INSTANCE.addSID(gid++, 234, "Acutesmall");
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
        INSTANCE.addSID(gid++, 252, "questionsmall");
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
        INSTANCE.addSID(gid++, 271, "Circumflexsmall");
        INSTANCE.addSID(gid++, 272, "hyphensuperior");
        INSTANCE.addSID(gid++, 273, "Gravesmall");
        INSTANCE.addSID(gid++, 274, "Asmall");
        INSTANCE.addSID(gid++, 275, "Bsmall");
        INSTANCE.addSID(gid++, 276, "Csmall");
        INSTANCE.addSID(gid++, 277, "Dsmall");
        INSTANCE.addSID(gid++, 278, "Esmall");
        INSTANCE.addSID(gid++, 279, "Fsmall");
        INSTANCE.addSID(gid++, 280, "Gsmall");
        INSTANCE.addSID(gid++, 281, "Hsmall");
        INSTANCE.addSID(gid++, 282, "Ismall");
        INSTANCE.addSID(gid++, 283, "Jsmall");
        INSTANCE.addSID(gid++, 284, "Ksmall");
        INSTANCE.addSID(gid++, 285, "Lsmall");
        INSTANCE.addSID(gid++, 286, "Msmall");
        INSTANCE.addSID(gid++, 287, "Nsmall");
        INSTANCE.addSID(gid++, 288, "Osmall");
        INSTANCE.addSID(gid++, 289, "Psmall");
        INSTANCE.addSID(gid++, 290, "Qsmall");
        INSTANCE.addSID(gid++, 291, "Rsmall");
        INSTANCE.addSID(gid++, 292, "Ssmall");
        INSTANCE.addSID(gid++, 293, "Tsmall");
        INSTANCE.addSID(gid++, 294, "Usmall");
        INSTANCE.addSID(gid++, 295, "Vsmall");
        INSTANCE.addSID(gid++, 296, "Wsmall");
        INSTANCE.addSID(gid++, 297, "Xsmall");
        INSTANCE.addSID(gid++, 298, "Ysmall");
        INSTANCE.addSID(gid++, 299, "Zsmall");
        INSTANCE.addSID(gid++, 300, "colonmonetary");
        INSTANCE.addSID(gid++, 301, "onefitted");
        INSTANCE.addSID(gid++, 302, "rupiah");
        INSTANCE.addSID(gid++, 303, "Tildesmall");
        INSTANCE.addSID(gid++, 304, "exclamdownsmall");
        INSTANCE.addSID(gid++, 305, "centoldstyle");
        INSTANCE.addSID(gid++, 306, "Lslashsmall");
        INSTANCE.addSID(gid++, 307, "Scaronsmall");
        INSTANCE.addSID(gid++, 308, "Zcaronsmall");
        INSTANCE.addSID(gid++, 309, "Dieresissmall");
        INSTANCE.addSID(gid++, 310, "Brevesmall");
        INSTANCE.addSID(gid++, 311, "Caronsmall");
        INSTANCE.addSID(gid++, 312, "Dotaccentsmall");
        INSTANCE.addSID(gid++, 313, "Macronsmall");
        INSTANCE.addSID(gid++, 314, "figuredash");
        INSTANCE.addSID(gid++, 315, "hypheninferior");
        INSTANCE.addSID(gid++, 316, "Ogoneksmall");
        INSTANCE.addSID(gid++, 317, "Ringsmall");
        INSTANCE.addSID(gid++, 318, "Cedillasmall");
        INSTANCE.addSID(gid++, 158, "onequarter");
        INSTANCE.addSID(gid++, 155, "onehalf");
        INSTANCE.addSID(gid++, 163, "threequarters");
        INSTANCE.addSID(gid++, 319, "questiondownsmall");
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
        INSTANCE.addSID(gid++, 347, "Agravesmall");
        INSTANCE.addSID(gid++, 348, "Aacutesmall");
        INSTANCE.addSID(gid++, 349, "Acircumflexsmall");
        INSTANCE.addSID(gid++, 350, "Atildesmall");
        INSTANCE.addSID(gid++, 351, "Adieresissmall");
        INSTANCE.addSID(gid++, 352, "Aringsmall");
        INSTANCE.addSID(gid++, 353, "AEsmall");
        INSTANCE.addSID(gid++, 354, "Ccedillasmall");
        INSTANCE.addSID(gid++, 355, "Egravesmall");
        INSTANCE.addSID(gid++, 356, "Eacutesmall");
        INSTANCE.addSID(gid++, 357, "Ecircumflexsmall");
        INSTANCE.addSID(gid++, 358, "Edieresissmall");
        INSTANCE.addSID(gid++, 359, "Igravesmall");
        INSTANCE.addSID(gid++, 360, "Iacutesmall");
        INSTANCE.addSID(gid++, 361, "Icircumflexsmall");
        INSTANCE.addSID(gid++, 362, "Idieresissmall");
        INSTANCE.addSID(gid++, 363, "Ethsmall");
        INSTANCE.addSID(gid++, 364, "Ntildesmall");
        INSTANCE.addSID(gid++, 365, "Ogravesmall");
        INSTANCE.addSID(gid++, 366, "Oacutesmall");
        INSTANCE.addSID(gid++, 367, "Ocircumflexsmall");
        INSTANCE.addSID(gid++, 368, "Otildesmall");
        INSTANCE.addSID(gid++, 369, "Odieresissmall");
        INSTANCE.addSID(gid++, 370, "OEsmall");
        INSTANCE.addSID(gid++, 371, "Oslashsmall");
        INSTANCE.addSID(gid++, 372, "Ugravesmall");
        INSTANCE.addSID(gid++, 373, "Uacutesmall");
        INSTANCE.addSID(gid++, 374, "Ucircumflexsmall");
        INSTANCE.addSID(gid++, 375, "Udieresissmall");
        INSTANCE.addSID(gid++, 376, "Yacutesmall");
        INSTANCE.addSID(gid++, 377, "Thornsmall");
        INSTANCE.addSID(gid++, 378, "Ydieresissmall");
    }
}