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
package org.apache.pdfbox.multipdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * Utility class used to clone PDF objects. It keeps track of objects it has already cloned.
 *
 */
public class PDFCloneUtility
{
    private static final Log LOG = LogFactory.getLog(PDFCloneUtility.class);

    private final PDDocument destination;
    private final Map<COSBase, COSBase> clonedVersion = new HashMap<>();
    private final Set<COSBase> clonedValues = new HashSet<>();
    // It might be useful to use IdentityHashMap like in PDFBOX-4477 for speed,
    // but we need a really huge file to test this. A test with the file from PDFBOX-4477
    // did not show a noticeable speed difference.

    /**
     * Creates a new instance for the given target document.
     * 
     * @param dest the destination PDF document that will receive the clones
     */
    PDFCloneUtility(PDDocument dest)
    {
        this.destination = dest;
    }

    /**
     * Returns the destination PDF document this cloner instance is set up for.
     * 
     * @return the destination PDF document
     */
    PDDocument getDestination()
    {
        return this.destination;
    }

    /**
     * Deep-clones the given object for inclusion into a different PDF document identified by the destination parameter.
     * 
     * Expert use only, don’t use it if you don’t know exactly what you are doing.
     * 
     * @param base the initial object as the root of the deep-clone operation
     * @return the cloned instance of the base object
     * @throws IOException if an I/O error occurs
     */
    @SuppressWarnings("unchecked")
    public <TCOSBase extends COSBase> TCOSBase cloneForNewDocument(TCOSBase base) throws IOException
    {
        if (base == null)
        {
            return null;
        }
        COSBase retval = clonedVersion.get(base);
        if (retval != null)
        {
            // we are done, it has already been converted.
            return (TCOSBase) retval;
        }
        if (clonedValues.contains(base))
        {
            // Don't clone a clone
            return base;
        }
        retval = cloneCOSBaseForNewDocument(base);
        clonedVersion.put(base, retval);
        clonedValues.add(retval);
        return (TCOSBase) retval;
    }

    COSBase cloneCOSBaseForNewDocument(COSBase base) throws IOException
    {
        if (base instanceof COSObject)
        {
            return cloneForNewDocument(((COSObject) base).getObject());
        }
        if (base instanceof COSArray)
        {
            return cloneCOSArray((COSArray) base);
        }
        if (base instanceof COSStream)
        {
            return cloneCOSStream((COSStream) base);
        }
        if (base instanceof COSDictionary)
        {
            return cloneCOSDictionary((COSDictionary) base);
        }
        return base;
    }

    private COSArray cloneCOSArray(COSArray array) throws IOException
    {
        COSArray newArray = new COSArray();
        for (int i = 0; i < array.size(); i++)
        {
            COSBase value = array.get(i);
            if (hasSelfReference(array, value))
            {
                newArray.add(newArray);
            }
            else
            {
                newArray.add(cloneForNewDocument(value));
            }
        }
        return newArray;
    }

    private COSStream cloneCOSStream(COSStream stream) throws IOException
    {
        COSStream newStream = destination.getDocument().createCOSStream();
        try (OutputStream output = newStream.createRawOutputStream();
                InputStream input = stream.createRawInputStream())
        {
            input.transferTo(output);
        }
        clonedVersion.put(stream, newStream);
        for (Map.Entry<COSName, COSBase> entry : stream.entrySet())
        {
            COSBase value = entry.getValue();
            if (hasSelfReference(stream, value))
            {
                newStream.setItem(entry.getKey(), newStream);
            }
            else
            {
                newStream.setItem(entry.getKey(), cloneForNewDocument(value));
            }
        }
        return newStream;
    }

    private COSDictionary cloneCOSDictionary(COSDictionary dictionary) throws IOException
    {
        COSDictionary newDictionary = new COSDictionary();
        clonedVersion.put(dictionary, newDictionary);
        for (Map.Entry<COSName, COSBase> entry : dictionary.entrySet())
        {
            COSBase value = entry.getValue();
            if (hasSelfReference(dictionary, value))
            {
                newDictionary.setItem(entry.getKey(), newDictionary);
            }
            else
            {
                newDictionary.setItem(entry.getKey(), cloneForNewDocument(value));
            }
        }
        return newDictionary;
    }

    /**
     * Merges two objects of the same type by deep-cloning its members. <br>
     * Base and target must be instances of the same class.
     * 
     * @param base the base object to be cloned
     * @param target the merge target
     * @throws IOException if an I/O error occurs
     */
    void cloneMerge(final COSObjectable base, COSObjectable target) throws IOException
    {
        if (base == null || base == target)
        {
            return;
        }
        cloneMergeCOSBase(base.getCOSObject(), target.getCOSObject());
    }

    private void cloneMergeCOSBase(final COSBase source, final COSBase target) throws IOException
    {
        COSBase sourceBase = source instanceof COSObject ? ((COSObject) source).getObject()
                : source;
        COSBase targetBase = target instanceof COSObject ? ((COSObject) target).getObject()
                : target;
        if (sourceBase instanceof COSArray && targetBase instanceof COSArray)
        {
            COSArray array = (COSArray) sourceBase;
            for (int i = 0; i < array.size(); i++)
            {
                ((COSArray) targetBase).add(cloneForNewDocument(array.get(i)));
            }
        }
        else if (sourceBase instanceof COSDictionary && targetBase instanceof COSDictionary)
        {
            COSDictionary sourceDict = (COSDictionary) sourceBase;
            COSDictionary targetDict = (COSDictionary) targetBase;
            for (Map.Entry<COSName, COSBase> entry : sourceDict.entrySet())
            {
                COSName key = entry.getKey();
                COSBase value = entry.getValue();
                if (targetDict.getItem(key) != null)
                {
                    cloneMerge(value, targetDict.getItem(key));
                }
                else
                {
                    targetDict.setItem(key, cloneForNewDocument(value));
                }
            }
        }
    }

    /**
     * Check whether an element (of an array or a dictionary) points to its parent.
     *
     * @param parent COSArray or COSDictionary
     * @param value an element
     */
    private boolean hasSelfReference(COSBase parent, COSBase value)
    {
        if (value instanceof COSObject)
        {
            COSBase actual = ((COSObject) value).getObject();
            if (actual == parent)
            {
                COSObject cosObj = ((COSObject) value);
                LOG.warn(parent.getClass().getSimpleName() + " object has a reference to itself: "
                        + cosObj.getObjectNumber() + " " + cosObj.getGenerationNumber() + " R");
                return true;
            }
        }
        return false;
    }
}
