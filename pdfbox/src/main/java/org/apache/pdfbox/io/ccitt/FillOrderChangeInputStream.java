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

/* $Id$ */

package org.apache.pdfbox.io.ccitt;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This filtering input stream does a fill order change required for certain TIFF images.
 * @version $Revision$
 */
public class FillOrderChangeInputStream extends FilterInputStream
{

    /**
     * Main constructor.
     * @param in the underlying input stream
     */
    public FillOrderChangeInputStream(InputStream in)
    {
        super(in);
    }

    /** {@inheritDoc} */
    public int read(byte[] b, int off, int len) throws IOException
    {
        int result = super.read(b, off, len);
        if (result > 0)
        {
            int endpos = off + result;
            for (int i = off; i < endpos; i++)
            {
                b[i] = FLIP_TABLE[b[i] & 0xff];
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    public int read() throws IOException
    {
        int b = super.read();
        if (b < 0)
        {
            return b;
        }
        else
        {
            return FLIP_TABLE[b] & 0xff;
        }
    }

    // Table to be used when fillOrder = 2, for flipping bytes.
    // Copied from the TIFFFaxDecoder class
    private static final byte[] FLIP_TABLE = {
     0,  -128,    64,   -64,    32,   -96,    96,   -32,
    16,  -112,    80,   -48,    48,   -80,   112,   -16,
     8,  -120,    72,   -56,    40,   -88,   104,   -24,
    24,  -104,    88,   -40,    56,   -72,   120,    -8,
     4,  -124,    68,   -60,    36,   -92,   100,   -28,
    20,  -108,    84,   -44,    52,   -76,   116,   -12,
    12,  -116,    76,   -52,    44,   -84,   108,   -20,
    28,  -100,    92,   -36,    60,   -68,   124,    -4,
     2,  -126,    66,   -62,    34,   -94,    98,   -30,
    18,  -110,    82,   -46,    50,   -78,   114,   -14,
    10,  -118,    74,   -54,    42,   -86,   106,   -22,
    26,  -102,    90,   -38,    58,   -70,   122,    -6,
     6,  -122,    70,   -58,    38,   -90,   102,   -26,
    22,  -106,    86,   -42,    54,   -74,   118,   -10,
    14,  -114,    78,   -50,    46,   -82,   110,   -18,
    30,   -98,    94,   -34,    62,   -66,   126,    -2,
     1,  -127,    65,   -63,    33,   -95,    97,   -31,
    17,  -111,    81,   -47,    49,   -79,   113,   -15,
     9,  -119,    73,   -55,    41,   -87,   105,   -23,
    25,  -103,    89,   -39,    57,   -71,   121,    -7,
     5,  -123,    69,   -59,    37,   -91,   101,   -27,
    21,  -107,    85,   -43,    53,   -75,   117,   -11,
    13,  -115,    77,   -51,    45,   -83,   109,   -19,
    29,   -99,    93,   -35,    61,   -67,   125,    -3,
     3,  -125,    67,   -61,    35,   -93,    99,   -29,
    19,  -109,    83,   -45,    51,   -77,   115,   -13,
    11,  -117,    75,   -53,    43,   -85,   107,   -21,
    27,  -101,    91,   -37,    59,   -69,   123,    -5,
     7,  -121,    71,   -57,    39,   -89,   103,   -25,
    23,  -105,    87,   -41,    55,   -73,   119,    -9,
    15,  -113,    79,   -49,    47,   -81,   111,   -17,
    31,   -97,    95,   -33,    63,   -65,   127,    -1,
    };
    // end
}