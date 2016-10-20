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
package org.apache.pdfbox.pdmodel.interactive.annotation;

import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionFactory;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDAppearanceHandler;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDLinkAppearanceHandler;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;

/**
 * This is the class that represents a link annotation.
 *
 * @author Ben Litchfield
 * @author Paul King
 */
public class PDAnnotationLink extends PDAnnotation
{

    /**
     * Constant values of the Text as defined in the PDF 1.6 reference Table 8.19.
     */
    public static final String HIGHLIGHT_MODE_NONE = "N";
    /**
     * Constant values of the Text as defined in the PDF 1.6 reference Table 8.19.
     */
    public static final String HIGHLIGHT_MODE_INVERT = "I";
    /**
     * Constant values of the Text as defined in the PDF 1.6 reference Table 8.19.
     */
    public static final String HIGHLIGHT_MODE_OUTLINE = "O";
    /**
     * Constant values of the Text as defined in the PDF 1.6 reference Table 8.19.
     */
    public static final String HIGHLIGHT_MODE_PUSH = "P";

    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "Link";
    
    /**
     * Custom apperance handler to generate an appearance stream.
     */
    private PDAppearanceHandler customAppearanceHandler;

    /**
     * Constructor.
     */
    public PDAnnotationLink()
    {
        getCOSObject().setItem(COSName.SUBTYPE, COSName.getPDFName(SUB_TYPE));
    }

    /**
     * Creates a Link annotation from a COSDictionary, expected to be a correct object definition.
     *
     * @param field the PDF objet to represent as a field.
     */
    public PDAnnotationLink(COSDictionary field)
    {
        super(field);
    }

    /**
     * Get the action to be performed when this annotation is to be activated. Either this or the
     * destination entry should be set, but not both.
     *
     * @return The action to be performed when this annotation is activated.
     */
    public PDAction getAction()
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.A);
        if (base instanceof COSDictionary)
        {
            return PDActionFactory.createAction((COSDictionary) base);
        }
        return null;
    }

    /**
     * Set the annotation action. Either this or the destination entry should be set, but not both.
     *
     * @param action The annotation action.
     *
     */
    public void setAction(PDAction action)
    {
        this.getCOSObject().setItem(COSName.A, action);
    }

    /**
     * This will set the border style dictionary, specifying the width and dash pattern used in drawing the line.
     *
     * @param bs the border style dictionary to set. 
     * 
     */
    public void setBorderStyle(PDBorderStyleDictionary bs)
    {
        this.getCOSObject().setItem(COSName.BS, bs);
    }

    /**
     * This will retrieve the border style dictionary, specifying the width and dash pattern used in
     * drawing the line.
     *
     * @return the border style dictionary.
     */
    public PDBorderStyleDictionary getBorderStyle()
    {
        COSBase bs = getCOSObject().getDictionaryObject(COSName.BS);
        if (bs instanceof COSDictionary)
        {
            return new PDBorderStyleDictionary((COSDictionary) bs);
        }
        return null;
    }

    /**
     * Get the destination to be displayed when the annotation is activated. Either this or the
     * action entry should be set, but not both.
     *
     * @return The destination for this annotation.
     *
     * @throws IOException If there is an error creating the destination.
     */
    public PDDestination getDestination() throws IOException
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.DEST);
        return PDDestination.create(base);
    }

    /**
     * The new destination value. Either this or the action entry should be set, but not both.
     *
     * @param dest The updated destination.
     */
    public void setDestination(PDDestination dest)
    {
        getCOSObject().setItem(COSName.DEST, dest);
    }

    /**
     * Set the highlight mode for when the mouse is depressed. See the HIGHLIGHT_MODE_XXX constants.
     *
     * @return The string representation of the highlight mode.
     */
    public String getHighlightMode()
    {
        return getCOSObject().getNameAsString(COSName.H, HIGHLIGHT_MODE_INVERT);
    }

    /**
     * Set the highlight mode. See the HIGHLIGHT_MODE_XXX constants.
     *
     * @param mode The new highlight mode.
     */
    public void setHighlightMode(String mode)
    {
        getCOSObject().setName(COSName.H, mode);
    }

    /**
     * This will set the previous URI action, in case it needs to be retrieved at later date.
     *
     * @param pa The previous URI.
     */
    public void setPreviousURI(PDActionURI pa)
    {
        getCOSObject().setItem("PA", pa);
    }

    /**
     * This will set the previous URI action, in case it's needed.
     *
     * @return The previous URI.
     */
    public PDActionURI getPreviousURI()
    {
        COSBase base = getCOSObject().getDictionaryObject("PA");
        if (base instanceof COSDictionary)
        {
            return new PDActionURI((COSDictionary) base);
        }
        return null;
    }

    /**
     * This will set the set of quadpoints which encompass the areas of this annotation which will activate.
     *
     * @param quadPoints an array representing the set of area covered.
     */
    public void setQuadPoints(float[] quadPoints)
    {
        COSArray newQuadPoints = new COSArray();
        newQuadPoints.setFloatArray(quadPoints);
        getCOSObject().setItem(COSName.QUADPOINTS, newQuadPoints);
    }

    /**
     * This will retrieve the set of quadpoints which encompass the areas of this annotation which will activate.
     *
     * @return An array of floats representing the quad points.
     */
    public float[] getQuadPoints()
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.QUADPOINTS);
        if (base instanceof COSArray)
        {
            return ((COSArray) base).toFloatArray();
        }
        // Should never happen as this is a required item
        return null; 
    }
    
    /**
     * Set a custom appearance handler for generating the annotations appearance streams.
     * 
     * @param customAppearanceHandler
     */
    public void setCustomAppearanceHandler(PDAppearanceHandler customAppearanceHandler)
    {
        this.customAppearanceHandler = customAppearanceHandler;
    }
    
    public void constructAppearances()
    {
        if (customAppearanceHandler == null)
        {
            PDLinkAppearanceHandler appearanceHandler = new PDLinkAppearanceHandler(this);
            appearanceHandler.generateAppearanceStreams();
        }
        else
        {
            customAppearanceHandler.generateAppearanceStreams();
        }
    }
}
