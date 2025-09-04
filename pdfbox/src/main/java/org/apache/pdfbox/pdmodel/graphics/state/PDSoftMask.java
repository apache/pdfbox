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
package org.apache.pdfbox.pdmodel.graphics.state;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.ResourceCache;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.apache.pdfbox.util.Matrix;

/**
 * Soft mask.
 *
 * @author Kühn &amp; Weyh Software GmbH
 */
public final class PDSoftMask implements COSObjectable
{
    /**
     * Creates a new soft mask.
     *
     * @param dictionary SMask
     * 
     * @return the newly created instance of PDSoftMask
     */
    public static PDSoftMask create(COSBase dictionary)
    {
        return create(dictionary, null);
    }

    /**
     * Creates a new soft mask.
     *
     * @param dictionary SMask
     * @param resourceCache Resource cache, may be null.
     * 
     * @return the newly created instance of PDSoftMask
     */
    public static PDSoftMask create(COSBase dictionary, ResourceCache resourceCache)
    {
        if (dictionary instanceof COSName)
        {
            if (COSName.NONE.equals(dictionary))
            {
                return null;
            }
            else
            {
                LOG.warn("Invalid SMask " + dictionary);
                return null;
            }
        }
        else if (dictionary instanceof COSDictionary)
        {
            return new PDSoftMask((COSDictionary) dictionary, resourceCache);
        }
        else
        {
            LOG.warn("Invalid SMask " + dictionary);
            return null;
        }
    }

    private static final Log LOG = LogFactory.getLog(PDSoftMask.class);

    private final COSDictionary dictionary;
    private final ResourceCache resourceCache;
    private COSName subType = null;
    private PDTransparencyGroup group = null;
    private COSArray backdropColor = null;
    private PDFunction transferFunction = null;

    /**
     * To allow a soft mask to know the CTM at the time of activation of the ExtGState.
     */
    private Matrix ctm;

    /**
     * Creates a new soft mask.
     *
     * @param dictionary The soft mask dictionary.
     */
    public PDSoftMask(COSDictionary dictionary)
    {
        this(dictionary, null);
    }

    /**
     * Creates a new soft mask.
     *
     * @param dictionary The soft mask dictionary.
     * @param resourceCache Resource cache, may be null.
     */
    public PDSoftMask(COSDictionary dictionary, ResourceCache resourceCache)
    {
        this.dictionary = dictionary;
        this.resourceCache = resourceCache;
    }

    @Override
    public COSDictionary getCOSObject()
    {
        return dictionary;
    }

    /**
     * Returns the subtype of the soft mask (Alpha, Luminosity) - S entry
     * 
     * @return the subtype of the soft mask
     */
    public COSName getSubType()
    {
        if (subType == null)
        {
            subType = getCOSObject().getCOSName(COSName.S);
        }
        return subType;
    }

    /**
     * Returns the G entry of the soft mask object
     * 
     * @return form containing the transparency group
     * @throws IOException if the group could not be read
     */
    public PDTransparencyGroup getGroup() throws IOException
    {
        if (group == null)
        {
            COSBase cosGroup = getCOSObject().getDictionaryObject(COSName.G);
            if (cosGroup != null)
            {
                PDResources resources = new PDResources(new COSDictionary(), resourceCache);
                PDXObject x = PDXObject.createXObject(cosGroup, resources);
                if (x instanceof PDTransparencyGroup)
                {
                    group = (PDTransparencyGroup) x;
                }
            }
        }
        return group;
    }

    /**
     * Returns the backdrop color.
     * 
     * @return the backdrop color
     */
    public COSArray getBackdropColor()
    {
        if (backdropColor == null)
        {
            backdropColor = getCOSObject().getCOSArray(COSName.BC);
        }
        return backdropColor;
    }

    /**
     * Returns the transfer function.
     * 
     * @return the transfer function
     * @throws IOException If we are unable to create the PDFunction object.
     */
    public PDFunction getTransferFunction() throws IOException
    {
        if (transferFunction == null)
        {
            COSBase cosTF = getCOSObject().getDictionaryObject(COSName.TR);
            if (cosTF != null)
            {
                transferFunction = PDFunction.create(cosTF);
            }
        }
        return transferFunction;
    }

    /**
     * Set the CTM that is valid at the time the ExtGState was activated.
     *
     * @param ctm the transformation matrix
     */
    void setInitialTransformationMatrix(Matrix ctm)
    {
        this.ctm = ctm;
    }

    /**
     * Returns the CTM at the time the ExtGState was activated.
     *
     * @return the transformation matrix
     */
    public Matrix getInitialTransformationMatrix()
    {
        return ctm;
    }
}
