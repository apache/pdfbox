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

package org.apache.pdfbox.encoding.conversion;

import org.apache.fontbox.cmap.CMap;
import java.io.UnsupportedEncodingException;


/**
 *  CJKConverter converts encodings defined in CJKEncodings.
 *
 *  @author  Pin Xue (http://www.pinxue.net), Holly Lee (holly.lee (at) gmail.com)
 *  @version $Revision: 1.0 $
 */
public class CJKConverter implements EncodingConverter
{
    // The encoding
    private String encodingName = null;    
    // The java charset name
    private String charsetName = null;


    /**
     *  Constructs a CJKConverter from a PDF encoding name.
     *  
     *  @param encoding the encoding to be used
     */
    public CJKConverter(String encoding)
    {
        encodingName = encoding;
        charsetName = CJKEncodings.getCharset(encoding);
    }

   /**
    *  Convert a string. It occurs when a cmap lookup returned
    *  converted bytes successfully, but we still need to convert its
    *  encoding. The parameter s is constructs as one byte or a UTF-16BE
    *  encoded string.
    *
    *  Note: pdfbox set string to UTF-16BE charset before calling into
    *  this.
    *  
    *  {@inheritDoc}
    */
    public String convertString(String s)
    {
        if ( s.length() == 1 )
        {
            return s;
        }

        if ( charsetName.equalsIgnoreCase("UTF-16BE") )
        {
            return s;
        }

        try 
        {
            return new String(s.getBytes("UTF-16BE"), charsetName);
        }
        catch ( UnsupportedEncodingException uee ) 
        {
            return s;   
        }
    }

   /**
    *  Convert bytes to a string. We just convert bytes within
    *  coderange defined in CMap.
    *
    *  {@inheritDoc}
    */
    public String convertBytes(byte [] c, int offset, int length, CMap cmap)
    {
        if ( cmap != null ) 
        {
            try 
            {
                if ( cmap.isInCodeSpaceRanges(c, offset, length) )
                {
                    return new String(c, offset, length, charsetName);
                }
                else
                {
                    return null;
                }

            }
            catch ( UnsupportedEncodingException uee ) 
            {
                return new String(c, offset, length);
            }
        }
        // No cmap?
        return null;
    }

}
