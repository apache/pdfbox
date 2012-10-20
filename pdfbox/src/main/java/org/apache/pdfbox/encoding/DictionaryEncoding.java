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
package org.apache.pdfbox.encoding;

import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;

/**
 * This will perform the encoding from a dictionary.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.13 $
 */
public class DictionaryEncoding extends Encoding
{
    private COSDictionary encoding = null;

    /**
     * Constructor.
     *
     * @param fontEncoding The encoding dictionary.
     *
     * @throws IOException If there is a problem getting the base font.
     */
    public DictionaryEncoding( COSDictionary fontEncoding ) throws IOException
    {
        encoding = fontEncoding;

        //first set up the base encoding
        //The previious value WinAnsiEncoding() has been changed to StandardEnding
        //see p 389 of the PDF 1.5 ref�rence table 5.11 entries in a dictionary encoding
        //"If this entry is absent, the Differences entry describes differences from an implicit
        //base encoding. For a font program that is embedded in the PDF file, the
        //implicit base encoding is the font program�s built-in encoding, as described
        //above and further elaborated in the sections on specific font types below. Otherwise,
        //for a nonsymbolic font, it is StandardEncoding, and for a symbolic font, it
        //is the font�s built-in encoding."

        // The default base encoding is standardEncoding
        Encoding baseEncoding = StandardEncoding.INSTANCE;
        COSName baseEncodingName =
            (COSName) encoding.getDictionaryObject(COSName.BASE_ENCODING);
        if (baseEncodingName != null) {
            baseEncoding =
                EncodingManager.INSTANCE.getEncoding(baseEncodingName);
        }

        nameToCode.putAll( baseEncoding.nameToCode );
        codeToName.putAll( baseEncoding.codeToName );


        //now replace with the differences.
        COSArray differences = (COSArray)encoding.getDictionaryObject( COSName.DIFFERENCES );
        int currentIndex = -1;
        for( int i=0; differences != null && i<differences.size(); i++ )
        {
            COSBase next = differences.getObject( i );
            if( next instanceof COSNumber )
            {
                currentIndex = ((COSNumber)next).intValue();
            }
            else if( next instanceof COSName )
            {
                COSName name = (COSName)next;
                addCharacterEncoding( currentIndex++, name.getName() );
            }
        }
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return encoding;
    }
}
