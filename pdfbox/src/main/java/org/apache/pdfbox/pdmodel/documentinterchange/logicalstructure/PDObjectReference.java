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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

/**
 * An object reference.
 * 
 * @author <a href="mailto:Johannes%20Koch%20%3Ckoch@apache.org%3E">Johannes Koch</a>
 * @version $Revision: $
 */
public class PDObjectReference implements COSObjectable
{

    public static final String TYPE = "OBJR";

    private COSDictionary dictionary;

    protected COSDictionary getCOSDictionary()
    {
        return this.dictionary;
    }

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
     * @param dictionary The existing dictionary.
     */
    public PDObjectReference(COSDictionary dictionary)
    {
        this.dictionary = dictionary;
    }

    /**
     * {@inheritDoc}
     */
    public COSBase getCOSObject()
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
        COSBase obj = this.getCOSDictionary().getDictionaryObject(COSName.OBJ);
        try
        {
            return PDAnnotation.createAnnotation(obj);
        }
        catch (IOException e)
        {
            // No Annotation
            try
            {
                return PDXObject.createXObject(obj);
            }
            catch (IOException e1)
            {
                // No XObject
                // TODO what else can be the target of the object reference?
            }
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
        this.getCOSDictionary().setItem(COSName.OBJ, annotation);
    }

    /**
     * Sets the referenced XObject.
     * 
     * @param xobject the referenced XObject
     */
    public void setReferencedObject(PDXObject xobject)
    {
        this.getCOSDictionary().setItem(COSName.OBJ, xobject);
    }

}
