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
package org.apache.pdfbox.pdmodel.font;

import org.apache.pdfbox.cos.COSDictionary;

import java.io.IOException;

/**
 * Type 1 Multiple Master Font.
 *
 * @author Ben Litchfield
 */
public class PDMMType1Font extends PDType1Font
{
    /**
     * Creates an MMType1Font from a Font dictionary in a PDF.
     *
     * @param fontDictionary font dictionary
     * 
     * @throws IOException if the font could not be read
     */
    public PDMMType1Font(COSDictionary fontDictionary) throws IOException
    {
        super(fontDictionary);
    }
}
