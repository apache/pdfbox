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
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;

/**
 * An artifact marked content.
 *
 * @author <a href="mailto:Johannes%20Koch%20%3Ckoch@apache.org%3E">Johannes Koch</a>
 * @version $Revision: $
 *
 */
public class PDArtifactMarkedContent extends PDMarkedContent
{

    public PDArtifactMarkedContent(COSDictionary properties)
    {
        super(COSName.ARTIFACT, properties);
    }


    /**
     * Gets the type (Type).
     * 
     * @return the type
     */
    public String getType()
    {
        return this.getProperties().getNameAsString(COSName.TYPE);
    }

    /**
     * Gets the artifact's bounding box (BBox).
     * 
     * @return the artifact's bounding box
     */
    public PDRectangle getBBox()
    {
        PDRectangle retval = null;
        COSArray a = (COSArray) this.getProperties().getDictionaryObject(
            COSName.BBOX);
        if (a != null)
        {
            retval = new PDRectangle(a);
        }
        return retval;
    }

    /**
     * Is the artifact attached to the top edge?
     * 
     * @return <code>true</code> if the artifact is attached to the top edge,
     * <code>false</code> otherwise
     */
    public boolean isTopAttached()
    {
        return this.isAttached("Top");
    }

    /**
     * Is the artifact attached to the bottom edge?
     * 
     * @return <code>true</code> if the artifact is attached to the bottom edge,
     * <code>false</code> otherwise
     */
    public boolean isBottomAttached()
    {
        return this.isAttached("Bottom");
    }

    /**
     * Is the artifact attached to the left edge?
     * 
     * @return <code>true</code> if the artifact is attached to the left edge,
     * <code>false</code> otherwise
     */
    public boolean isLeftAttached()
    {
        return this.isAttached("Left");
    }

    /**
     * Is the artifact attached to the right edge?
     * 
     * @return <code>true</code> if the artifact is attached to the right edge,
     * <code>false</code> otherwise
     */
    public boolean isRightAttached()
    {
        return this.isAttached("Right");
    }

    /**
     * Gets the subtype (Subtype).
     * 
     * @return the subtype
     */
    public String getSubtype()
    {
        return this.getProperties().getNameAsString(COSName.SUBTYPE);
    }


    /**
     * Is the artifact attached to the given edge?
     * 
     * @param edge the edge
     * @return <code>true</code> if the artifact is attached to the given edge,
     * <code>false</code> otherwise
     */
    private boolean isAttached(String edge)
    {
        COSArray a = (COSArray) this.getProperties().getDictionaryObject(
            COSName.ATTACHED);
        if (a != null)
        {
            for (int i = 0; i < a.size(); i++)
            {
                if (edge.equals(a.getName(i)))
                {
                    return true;
                }
            }
        }
        return false;
    }

}
