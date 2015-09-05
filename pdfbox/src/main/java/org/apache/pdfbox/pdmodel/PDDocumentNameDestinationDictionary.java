/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel;

import java.io.IOException;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;

/**
 * This encapsulates the "dictionary of names and corresponding destinations" for the /Dests entry
 * in the document catalog.
 *
 * @author Tilman Hausherr
 */
public class PDDocumentNameDestinationDictionary implements COSObjectable
{
    private final COSDictionary nameDictionary;

     /**
     * Constructor.
     *
     * @param dict The dictionary of names and corresponding destinations.
     */
    public PDDocumentNameDestinationDictionary(COSDictionary dict)
    {
        this.nameDictionary = dict;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos dictionary for this object.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return nameDictionary;
    }
    
    /**
     * Returns the destination corresponding to the parameter.
     *
     * @param name The destination name.
     * @return The destination for that name, or null if there isn't any.
     * 
     * @throws IOException if something goes wrong when creating the destination object.
     */
    public PDDestination getDestination(String name) throws IOException
    {
        COSBase item = nameDictionary.getDictionaryObject(name);

        // "The value of this entry shall be a dictionary in which each key is a destination name
        // and the corresponding value is either an array defining the destination (...) 
        // or a dictionary with a D entry whose value is such an array."                
        if (item instanceof COSArray)
        {
            return PDDestination.create(item);
        }
        else if (item instanceof COSDictionary)
        {
            COSDictionary dict = (COSDictionary) item;
            if (dict.containsKey(COSName.D))
            {
                return PDDestination.create(dict.getDictionaryObject(COSName.D));
            }
        }
        return null;
    }

}
