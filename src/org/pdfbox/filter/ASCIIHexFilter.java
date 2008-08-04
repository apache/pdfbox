/**
 * Copyright (c) 2003, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.pdfbox.cos.COSDictionary;

import org.pdfbox.persistence.util.COSHEXTable;

/**
 * This is the used for the ASCIIHexDecode filter.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.9 $
 */
public class ASCIIHexFilter implements Filter
{

    /**
     * {@inheritDoc}
     */
    public void decode( InputStream compressedData, OutputStream result, COSDictionary options, int filterIndex ) throws IOException
    {
        int value =0;
        int firstByte = 0;
        int secondByte = 0;
        while( (firstByte = compressedData.read()) != -1 )
        {
            value = REVERSE_HEX[firstByte] * 16;
            secondByte = compressedData.read();
            if( secondByte >= 0 )
            {
                value += REVERSE_HEX[ secondByte ];
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
    public void encode( InputStream rawData, OutputStream result, COSDictionary options, int filterIndex ) throws IOException
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