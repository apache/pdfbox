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
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;

/**
 * Soft mask.
 *
 * @author Kühn & Weyh Software, GmbH
 */
public final class PDSoftMask implements COSObjectable
{
    /**
     * Creates a new soft mask.
     *
     * @param dictionary SMask
     */
    public static PDSoftMask create(COSBase dictionary)
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
            return new PDSoftMask((COSDictionary) dictionary);
        }
        else
        {
            LOG.warn("Invalid SMask " + dictionary);
            return null;
        }
    }

    private static final Log LOG = LogFactory.getLog(PDSoftMask.class);

    private final COSDictionary dictionary;
    private COSName subType = null;
    private PDTransparencyGroup group = null;
    private COSArray backdropColor = null;
    private PDFunction transferFunction = null;

    /**
     * Creates a new soft mask.
     */
    public PDSoftMask(COSDictionary dictionary)
    {
        super();
        this.dictionary = dictionary;
    }

    @Override
    public COSDictionary getCOSObject()
    {
        return dictionary;
    }

    /**
     * Returns the subtype of the soft mask (Alpha, Luminosity) - S entry
     */
    public COSName getSubType()
    {
        if (subType == null)
        {
            subType = (COSName) getCOSObject().getDictionaryObject(COSName.S);
        }
        return subType;
    }

    /**
     * Returns the G entry of the soft mask object
     * 
     * @return form containing the transparency group
     * @throws IOException
     */
    public PDTransparencyGroup getGroup() throws IOException
    {
        if (group == null)
        {
            COSBase cosGroup = getCOSObject().getDictionaryObject(COSName.G);
            if (cosGroup != null)
            {
                group = (PDTransparencyGroup) PDXObject.createXObject(cosGroup, null);
            }
        }
        return group;
    }

    /**
     * Returns the backdrop color.
     */
    public COSArray getBackdropColor()
    {
        if (backdropColor == null)
        {
            backdropColor = (COSArray) getCOSObject().getDictionaryObject(COSName.BC);
        }
        return backdropColor;
    }

    /**
     * Returns the transfer function.
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
}
