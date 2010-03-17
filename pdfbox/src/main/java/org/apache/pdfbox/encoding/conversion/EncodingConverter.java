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

/**
 *  EncodingConverter converts string or characters in one encoding, which is specified in PDF
 *  file, to another string with respective java charset. The mapping from
 *  PDF encoding name to java charset name is maintained by EncodingConversionManager

 *  @author  Pin Xue (http://www.pinxue.net), Holly Lee (holly.lee (at) gmail.com)
 *  @version $Revision: 1.0 $
 */
public interface EncodingConverter
{
       /**
        *  Convert a string.
        *  
        *  @param s the string to be converted
        *  @return the converted string
        */
       public String convertString(String s);

       /**
        *  Convert bytes to a string.
        *
        *  @param c the byte array to be converted
        *  @param offset the starting offset of the array
        *  @param length the number of bytes
        *  @param cmap the cmap to be used for conversion   
        *  @return the converted string
        */
       public String convertBytes(byte [] c, int offset, int length, CMap cmap);
}
