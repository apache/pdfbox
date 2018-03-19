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
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.filespecification.PDFileSpecification;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;

/**
 * This represents a embedded go-to action that can be executed in a PDF document.
 *
 * @author Ben Litchfield
 * @author Panagiotis Toumasis
 * @author Tilman Hausherr
 */
public class PDActionEmbeddedGoTo extends PDAction
{
    /**
     * This type of action this object represents.
     */
    public static final String SUB_TYPE = "GoToE";

    /**
     * Default constructor.
     */
    public PDActionEmbeddedGoTo()
    {
        setSubType(SUB_TYPE);
    }

    /**
     * Constructor.
     *
     * @param a The action dictionary.
     */
    public PDActionEmbeddedGoTo(COSDictionary a)
    {
        super(a);
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
    public void setDestination(PDDestination d)
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
                    throw new IllegalArgumentException("Destination of a GoToE action must be "
                            + "a page dictionary object");
                }
            }
        }
        getCOSObject().setItem(COSName.D, d);
    }

    /**
     * This will get the file in which the destination is located.
     *
     * @return The F entry of the specific embedded go-to action dictionary.
     *
     * @throws IOException If there is an error creating the file spec.
     */
    public PDFileSpecification getFile() throws IOException
    {
        return PDFileSpecification.createFS(getCOSObject().getDictionaryObject(COSName.F));
    }

    /**
     * This will set the file in which the destination is located.
     *
     * @param fs The file specification.
     */
    public void setFile(PDFileSpecification fs)
    {
        getCOSObject().setItem(COSName.F, fs);
    }

    /**
     * This will specify whether to open the destination document in a new window, in the same
     * window, or behave in accordance with the current user preference.
     *
     * @return A flag specifying how to open the destination document.
     */
    public OpenMode getOpenInNewWindow()
    {
        if (getCOSObject().getDictionaryObject(COSName.NEW_WINDOW) instanceof COSBoolean)
        {
            COSBoolean b = (COSBoolean) getCOSObject().getDictionaryObject(COSName.NEW_WINDOW);
            return b.getValue() ? OpenMode.NEW_WINDOW : OpenMode.SAME_WINDOW;
        }
        return OpenMode.USER_PREFERENCE;
    }

    /**
     * This will specify whether to open the destination document in a new window.
     *
     * @param value The flag value.
     */
    public void setOpenInNewWindow(OpenMode value)
    {
        if (null == value)
        {
            getCOSObject().removeItem(COSName.NEW_WINDOW);
            return;
        }
        switch (value)
        {
            case USER_PREFERENCE:
                getCOSObject().removeItem(COSName.NEW_WINDOW);
                break;
            case SAME_WINDOW:
                getCOSObject().setBoolean(COSName.NEW_WINDOW, false);
                break;
            case NEW_WINDOW:
                getCOSObject().setBoolean(COSName.NEW_WINDOW, true);
                break;
            default:
                // shouldn't happen unless the enum type is changed
                break;
        }
    }

    /**
     * Get the target directory.
     *
     * @return the target directory or null if there is none.
     */
    public PDTargetDirectory getTargetDirectory()
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.T);
        if (base instanceof COSDictionary)
        {
            return new PDTargetDirectory((COSDictionary) base);
        }
        return null;
    }

    /**
     * Sets the target directory.
     * 
     * @param targetDirectory the target directory.
     */
    public void setTargetDirectory(PDTargetDirectory targetDirectory)
    {
        getCOSObject().setItem(COSName.T, targetDirectory);
    }
}
