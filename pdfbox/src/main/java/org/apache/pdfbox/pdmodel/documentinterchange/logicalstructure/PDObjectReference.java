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
package org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationUnknown;

/**
 * An object reference.
 * <p>
 * This is described as "Entries in an object reference dictionary" in the PDF specification.
 * 
 * @author Johannes Koch
 */
public class PDObjectReference implements COSObjectable
{
    /**
     * Log instance.
     */
    private static final Logger LOG = LogManager.getLogger(PDObjectReference.class);

    /**
     * TYPE of this object.
     */
    public static final String TYPE = "OBJR";

    private final COSDictionary dictionary;

    /**
     * Default Constructor.
     *
     */
    public PDObjectReference()
    {
        this.dictionary = new COSDictionary();
        this.dictionary.setName(COSName.TYPE, TYPE);
    }

    /**
     * Constructor for an existing object reference.
     *
     * @param theDictionary The existing dictionary.
     */
    public PDObjectReference(COSDictionary theDictionary)
    {
        dictionary = theDictionary;
    }

    /**
     * Returns the underlying dictionary.
     * 
     * @return the dictionary
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return this.dictionary;
    }

    /**
     * Gets a higher-level object for the referenced object.
     * Currently this method may return a {@link PDAnnotation},
     * a {@link PDXObject} or <code>null</code>.
     * 
     * @return a higher-level object for the referenced object
     */
    public COSObjectable getReferencedObject()
    {
        COSDictionary objDictionary = getCOSObject().getCOSDictionary(COSName.OBJ);
        if (objDictionary == null)
        {
            return null;
        }
        try
        {
            if (objDictionary instanceof COSStream)
            {
                PDXObject xobject = PDXObject.createXObject(objDictionary, null); // <-- TODO: valid?
                if (xobject != null)
                {
                    return xobject;
                }
            }
            PDAnnotation annotation = PDAnnotation.createAnnotation(objDictionary);
            /*
             * COSName.TYPE is optional, so if annotation is of type unknown and
             * COSName.TYPE is not COSName.ANNOT it still may be an annotation.
             * TODO shall we return the annotation object instead of null?
             * what else can be the target of the object reference?
             */
            if (!(annotation instanceof PDAnnotationUnknown) 
                    || COSName.ANNOT.equals(objDictionary.getCOSName(COSName.TYPE)))
            {
                return annotation;
            }
        }
        catch (IOException exception)
        {
            LOG.debug("Couldn't get the referenced object - returning null instead", exception);
            // this can only happen if the target is an XObject.
        }
        return null;
    }

    /**
     * Sets the referenced annotation.
     * 
     * @param annotation the referenced annotation
     */
    public void setReferencedObject(PDAnnotation annotation)
    {
        this.getCOSObject().setItem(COSName.OBJ, annotation);
    }

    /**
     * Sets the referenced XObject.
     * 
     * @param xobject the referenced XObject
     */
    public void setReferencedObject(PDXObject xobject)
    {
        this.getCOSObject().setItem(COSName.OBJ, xobject);
    }

    /**
     * Get the page on which the object shall be rendered.
     *
     * @return the referenced page or null.
     */
    public PDPage getPage()
    {
        COSDictionary pageDict = this.getCOSObject().getCOSDictionary(COSName.PG);
        if (pageDict != null)
        {
            return new PDPage(pageDict);
        }
        return null;
    }

    /**
     * Sets the page on which the object shall be rendered. This is optional and overrides the /PG
     * entry in the structure element containing the object reference; shall be used if the
     * structure element contained no such entry.
     *
     * @param page
     */
    public void setPage(PDPage page)
    {
        this.getCOSObject().setItem(COSName.PG, page);
    }
}
