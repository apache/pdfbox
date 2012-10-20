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
package org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;

/**
 * An object for four colours.
 *
 * @author <a href="mailto:Johannes%20Koch%20%3Ckoch@apache.org%3E">Johannes Koch</a>
 * @version $Revision: $
 */
public class PDFourColours implements COSObjectable
{

    private COSArray array;

    public PDFourColours()
    {
        this.array = new COSArray();
        this.array.add(COSNull.NULL);
        this.array.add(COSNull.NULL);
        this.array.add(COSNull.NULL);
        this.array.add(COSNull.NULL);
    }

    public PDFourColours(COSArray array)
    {
        this.array = array;
        // ensure that array has 4 items
        if (this.array.size() < 4)
        {
            for (int i = (this.array.size() - 1); i < 4; i++)
            {
                this.array.add(COSNull.NULL);
            }
        }
    }


    /**
     * Gets the colour for the before edge.
     * 
     * @return the colour for the before edge
     */
    public PDGamma getBeforeColour()
    {
        return this.getColourByIndex(0);
    }

    /**
     * Sets the colour for the before edge.
     * 
     * @param colour the colour for the before edge
     */
    public void setBeforeColour(PDGamma colour)
    {
        this.setColourByIndex(0, colour);
    }

    /**
     * Gets the colour for the after edge.
     * 
     * @return the colour for the after edge
     */
    public PDGamma getAfterColour()
    {
        return this.getColourByIndex(1);
    }

    /**
     * Sets the colour for the after edge.
     * 
     * @param colour the colour for the after edge
     */
    public void setAfterColour(PDGamma colour)
    {
        this.setColourByIndex(1, colour);
    }

    /**
     * Gets the colour for the start edge.
     * 
     * @return the colour for the start edge
     */
    public PDGamma getStartColour()
    {
        return this.getColourByIndex(2);
    }

    /**
     * Sets the colour for the start edge.
     * 
     * @param colour the colour for the start edge
     */
    public void setStartColour(PDGamma colour)
    {
        this.setColourByIndex(2, colour);
    }

    /**
     * Gets the colour for the end edge.
     * 
     * @return the colour for the end edge
     */
    public PDGamma getEndColour()
    {
        return this.getColourByIndex(3);
    }

    /**
     * Sets the colour for the end edge.
     * 
     * @param colour the colour for the end edge
     */
    public void setEndColour(PDGamma colour)
    {
        this.setColourByIndex(3, colour);
    }


    /**
     * {@inheritDoc}
     */
    public COSBase getCOSObject()
    {
        return this.array;
    }


    /**
     * Gets the colour by edge index.
     * 
     * @param index edge index
     * @return the colour
     */
    private PDGamma getColourByIndex(int index)
    {
        PDGamma retval = null;
        COSBase item = this.array.getObject(index);
        if (item instanceof COSArray)
        {
            retval = new PDGamma((COSArray) item);
        }
        return retval;
    }

    /**
     * Sets the colour by edge index.
     * 
     * @param index the edge index
     * @param colour the colour
     */
    private void setColourByIndex(int index, PDGamma colour)
    {
        COSBase base;
        if (colour == null)
        {
            base = COSNull.NULL;
        }
        else
        {
            base = colour.getCOSArray();
        }
        this.array.set(index, base);
    }

}
