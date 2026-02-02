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
package org.apache.pdfbox.pdmodel.fdf;

import java.io.IOException;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.w3c.dom.Element;

/**
 * This represents a Text FDF annotation.
 *
 * @author Ben Litchfield
 * @author Johanneke Lamberink
 */
public class FDFAnnotationText extends FDFAnnotation
{
    /**
     * COS Model value for SubType entry.
     */
    public static final String SUBTYPE = "Text";

    /**
     * Default constructor.
     */
    public FDFAnnotationText()
    {
        super();
        annot.setName(COSName.SUBTYPE, SUBTYPE);
    }

    /**
     * Constructor.
     *
     * @param a An existing FDF Annotation.
     */
    public FDFAnnotationText(COSDictionary a)
    {
        super(a);
    }

    /**
     * Constructor.
     *
     * @param element An XFDF element.
     *
     * @throws IOException If there is an error extracting information from the element.
     */
    public FDFAnnotationText(Element element) throws IOException
    {
        super(element);
        annot.setName(COSName.SUBTYPE, SUBTYPE);
        String icon = element.getAttribute("icon");
        if (icon != null && !icon.isEmpty())
        {
            setIcon(element.getAttribute("icon"));
        }
        String state = element.getAttribute("state");
        if (state != null && !state.isEmpty())
        {
            String statemodel = element.getAttribute("statemodel");
            if (statemodel != null && !statemodel.isEmpty())
            {
                setState(element.getAttribute("state"));
                setStateModel(element.getAttribute("statemodel"));
            }
        }
    }

    /**
     * This will set the icon (and hence appearance, AP taking precedence) For this annotation. See the
     * PDAnnotationText.NAME_XXX constants for valid values.
     *
     * @param icon The name of the annotation
     */
    public final void setIcon(String icon)
    {
        annot.setName(COSName.NAME, icon);
    }

    /**
     * This will retrieve the icon (and hence appearance, AP taking precedence) For this annotation. The default is
     * NOTE.
     *
     * @return The name of this annotation, see the PDAnnotationText.NAME_XXX constants.
     */
    public String getIcon()
    {
        return annot.getNameAsString(COSName.NAME, PDAnnotationText.NAME_NOTE);
    }

    /**
     * This will retrieve the annotation state.
     * 
     * @return the annotation state
     */
    public String getState()
    {
        return annot.getString(COSName.STATE);
    }

    /**
     * This will set the annotation state.
     * 
     * @param state the annotation state
     */
    public final void setState(String state)
    {
        annot.setString(COSName.STATE, state);
    }

    /**
     * This will retrieve the annotation state model.
     * 
     * @return the annotation state model
     */
    public String getStateModel()
    {
        return annot.getString(COSName.STATE_MODEL);
    }

    /**
     * This will set the annotation state model. Allowed values are "Marked" and "Review"
     * 
     * @param stateModel the annotation state model
     */
    public final void setStateModel(String stateModel)
    {
        annot.setString(COSName.STATE_MODEL, stateModel);
    }
}
