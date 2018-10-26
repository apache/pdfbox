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
package org.apache.pdfbox.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;

import org.apache.pdfbox.persistence.util.COSHEXTable;

/**
 * This is the used for the ASCIIHexDecode filter.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.9 $
 */
public class ASCIIHexFilter implements Filter
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(ASCIIHexFilter.class);
    /**
     * Whitespace.
     *   0  0x00  Null (NUL)
     *   9  0x09  Tab (HT)
     *  10  0x0A  Line feed (LF)
     *  12  0x0C  Form feed (FF)
     *  13  0x0D  Carriage return (CR)
     *  32  0x20  Space (SP)  
     */

    private boolean isWhitespace(int c) 
    {
        return c == 0 || c == 9 || c == 10 || c == 12 || c == 13 || c == 32;
    }
    
    private boolean isEOD(int c) 
    {
        return (c == 62); // '>' - EOD
    }
    
    /**
      * {@inheritDoc}
      */
    public void decode( InputStream compressedData, OutputStream result, COSDictionary options, int filterIndex ) 
        throws IOException 
    {
        int value = 0;
        int firstByte = 0;
        int secondByte = 0;
        while ((firstByte = compressedData.read()) != -1) 
        {
            // always after first char
            while(isWhitespace(firstByte))
            {
                firstByte = compressedData.read();
            }
            if (firstByte == -1 || isEOD(firstByte))
            {
                break;
            }
       
            if(REVERSE_HEX[firstByte] == -1)
            {
                log.error("Invalid Hex Code; int: " + firstByte + " char: " + (char) firstByte);
            }
            value = REVERSE_HEX[firstByte] * 16;
            secondByte = compressedData.read();
       
            if (secondByte == -1 || isEOD(secondByte)) 
            {
                // second value behaves like 0 in case of EOD
                result.write( value );
                break;
            }
            if(secondByte >= 0) 
            {
                if(REVERSE_HEX[secondByte] == -1)
                {
                    log.error("Invalid Hex Code; int: " + secondByte + " char: " + (char) secondByte);
                }
                value += REVERSE_HEX[secondByte];
            }
            result.write( value );
        }
        result.flush();
    }

    private static final int[] REVERSE_HEX = {
      /*   0 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /*  10 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /*  20 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /*  30 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /*  40 */  -1, -1, -1, -1, -1, -1, -1, -1,  0,  1,
      /*  50 */   2,  3,  4,  5,  6,  7,  8,  9, -1, -1,
      /*  60 */  -1, -1, -1, -1, -1, 10, 11, 12, 13, 14,
      /*  70 */  15, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /*  80 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /*  90 */  -1, -1, -1, -1, -1, -1, -1, 10, 11, 12,
      /* 100 */  13, 14, 15, -1, -1, -1, -1, -1, -1, -1,
      /* 110 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /* 120 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /* 130 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /* 140 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /* 150 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /* 160 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /* 170 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /* 180 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /* 190 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /* 200 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /* 210 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /* 220 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /* 230 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /* 240 */  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      /* 250 */  -1, -1, -1, -1, -1, -1
    };

    /**
     * {@inheritDoc}
     */
    public void encode( InputStream rawData, OutputStream result, COSDictionary options, int filterIndex ) 
        throws IOException
    {
        int byteRead = 0;
        while( (byteRead = rawData.read()) != -1 )
        {
            int value = (byteRead+256)%256;
            result.write( COSHEXTable.TABLE[value] );
        }
        result.flush();
    }
}
