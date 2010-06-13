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
            if(isEOD(firstByte))
            {
                break;
            }
       
            if(REVERSE_HEX[firstByte] == -1)
            {
                log.error("Invalid Hex Code; int: " + firstByte + " char: " + (char) firstByte);
            }
            value = REVERSE_HEX[firstByte] * 16;
            secondByte = compressedData.read();
       
            if(isEOD(secondByte)) 
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

    private static final int[] REVERSE_HEX =
    {
        -1, //0
        -1, //1
        -1, //2
        -1, //3
        -1, //4
        -1, //5
        -1, //6
        -1, //7
        -1, //8
        -1, //9
        -1, //10
        -1, //11
        -1, //12
        -1, //13
        -1, //14
        -1, //15
        -1, //16
        -1, //17
        -1, //18
        -1, //19
        -1, //20
        -1, //21
        -1, //22
        -1, //23
        -1, //24
        -1, //25
        -1, //26
        -1, //27
        -1, //28
        -1, //29
        -1, //30
        -1, //31
        -1, //32
        -1, //33
        -1, //34
        -1, //35
        -1, //36
        -1, //37
        -1, //38
        -1, //39
        -1, //40
        -1, //41
        -1, //42
        -1, //43
        -1, //44
        -1, //45
        -1, //46
        -1, //47
         0, //48
         1, //49
         2, //50
         3, //51
         4, //52
         5, //53
         6, //54
         7, //55
         8, //56
         9, //57
        -1, //58
        -1, //59
        -1, //60
        -1, //61
        -1, //62
        -1, //63
        -1, //64
        10, //65
        11, //66
        12, //67
        13, //68
        14, //69
        15, //70
        -1, //71
        -1, //72
        -1, //73
        -1, //74
        -1, //75
        -1, //76
        -1, //77
        -1, //78
        -1, //79
        -1, //80
        -1, //81
        -1, //82
        -1, //83
        -1, //84
        -1, //85
        -1, //86
        -1, //87
        -1, //88
        -1, //89
        -1, //90
        -1, //91
        -1, //92
        -1, //93
        -1, //94
        -1, //95
        -1, //96
        10, //97
        11, //98
        12, //99
        13, //100
        14, //101
        15, //102
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
