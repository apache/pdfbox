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

import java.util.Iterator;
import java.util.HashMap;

/**
 *  EncodingConversionManager maintains relationship between PDF encoding name
 *  and respective EncodingConverter instance. Those PDF encoding name like
 *  GBK-EUC-H should be converted to java charset name before constructing a
 *  java string instance
 *  
 *  @author  Pin Xue (http://www.pinxue.net), Holly Lee (holly.lee (at) gmail.com)
 *  @version $Revision: 1.0 $
 */
public class EncodingConversionManager
{
       /**
        *  Mapping from PDF encoding name to EncodingConverter instance.
        */
       private static HashMap encodingMap = new HashMap();

       private EncodingConversionManager()
       {
       }

       /**
        *  Initialize the encodingMap before anything calls us.
        */
       static {

           // Add CJK encodings to map
           Iterator it = CJKEncodings.getEncodingIterator();

           while ( it.hasNext() ) 
           {
               String encodingName = (String)(it.next());
               encodingMap.put(encodingName, new CJKConverter(encodingName));
           }
           // If there is any other encoding conversions, please add it here.

       }

       /**
        *  Get converter from given encoding name. If no converted defined,
        *  a null is returned.
        *  
        *  @param encoding search for a converter for the given encoding name
        *  @return the converter for the given encoding name
        */
       public static final EncodingConverter getConverter(String encoding)
       {
           return (EncodingConverter)(encodingMap.get(encoding));
       }


}
