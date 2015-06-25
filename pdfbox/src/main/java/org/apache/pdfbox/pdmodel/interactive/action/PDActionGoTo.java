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
package org.apache.pdfbox.pdmodel.interactive.action;

import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;

/**
 * This represents a go-to action that can be executed in a PDF document.
 *
 * @author Ben Litchfield
 * @author Panagiotis Toumasis
 */
public class PDActionGoTo extends PDAction
{
    /**
     * This type of action this object represents.
     */
    public static final String SUB_TYPE = "GoTo";

    /**
     * Default constructor.
     */
    public PDActionGoTo()
    {
        super();
        setSubType( SUB_TYPE );
    }

    /**
     * Constructor.
     *
     * @param a The action dictionary.
     */
    public PDActionGoTo( COSDictionary a )
    {
        super( a );
    }

    /**
     * This will get the destination to jump to.
     *
     * @return The D entry of the specific go-to action dictionary.
     *
     * @throws IOException If there is an error creating the destination.
     */
    public PDDestination getDestination() throws IOException
    {
        return PDDestination.create(getCOSObject().getDictionaryObject(COSName.D));
    }

    /**
     * This will set the destination to jump to.
     *
     * @param d The destination.
     * 
     * @throws IllegalArgumentException if the destination is not a page dictionary object.
     */
    public void setDestination( PDDestination d )
    {
        if (d instanceof PDPageDestination)
        {
            PDPageDestination pageDest = (PDPageDestination) d;
            COSArray destArray = pageDest.getCOSObject();
            if (destArray.size() >= 1)
            {
                COSBase page = destArray.getObject(0);
                if (!(page instanceof COSDictionary))
                {
                    throw new IllegalArgumentException("Destination of a GoTo action must be "
                            + "a page dictionary object");
                }
            }
        }
        getCOSObject().setItem(COSName.D, d);
    }
}
