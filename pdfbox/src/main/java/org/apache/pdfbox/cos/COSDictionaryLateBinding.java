/*
 *  Copyright 2011 adam.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.pdfbox.cos;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdfparser.ConformingPDFParser;

/**
 *
 * @author adam
 */
public class COSDictionaryLateBinding extends COSDictionary {
    public static final Log log = LogFactory.getLog(COSDictionaryLateBinding.class);
    ConformingPDFParser parser;

    public COSDictionaryLateBinding(ConformingPDFParser parser) {
        super();
        this.parser = parser;
    }

    /**
     * This will get an object from this dictionary.  If the object is a reference then it will
     * dereference it and get it from the document.  If the object is COSNull then
     * null will be returned.
     * @param key The key to the object that we are getting.
     * @return The object that matches the key.
     */
    @Override
    public COSBase getDictionaryObject(COSName key) {
        COSBase retval = items.get(key);
        if(retval instanceof COSObject) {
            int objectNumber = ((COSObject)retval).getObjectNumber().intValue();
            int generation = ((COSObject)retval).getGenerationNumber().intValue();
            try {
                retval = parser.getObject(objectNumber, generation);
            } catch(Exception e) {
                log.warn("Unable to read information for object " + objectNumber);
            }
        }
        if(retval instanceof COSNull) {
            retval = null;
        }
        return retval;
    }
}
