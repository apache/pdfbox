/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.pdfbox.preflight.content;

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_CMYK;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE_RGB;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_TOO_MANY_GRAPHIC_STATES;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY;
import static org.apache.pdfbox.preflight.PreflightConstants.MAX_GRAPHIC_STATES;

import java.awt.color.ICC_ColorSpace;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.color.PDCIEBasedColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.apache.pdfbox.preflight.PreflightConfiguration;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.graphic.ColorSpaceHelper;
import org.apache.pdfbox.preflight.graphic.ColorSpaceHelperFactory;
import org.apache.pdfbox.preflight.graphic.ColorSpaceHelperFactory.ColorSpaceRestriction;
import org.apache.pdfbox.preflight.graphic.ColorSpaces;
import org.apache.pdfbox.preflight.graphic.ICCProfileWrapper;
import org.apache.pdfbox.preflight.utils.COSUtils;
import org.apache.pdfbox.preflight.utils.FilterHelper;
import org.apache.pdfbox.preflight.utils.RenderingIntents;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorN;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingColorN;
import org.apache.pdfbox.contentstream.operator.text.BeginText;
import org.apache.pdfbox.contentstream.operator.state.Concatenate;
import org.apache.pdfbox.contentstream.operator.text.EndText;
import org.apache.pdfbox.contentstream.operator.state.Restore;
import org.apache.pdfbox.contentstream.operator.state.Save;
import org.apache.pdfbox.contentstream.operator.text.MoveText;
import org.apache.pdfbox.contentstream.operator.text.MoveTextSetLeading;
import org.apache.pdfbox.contentstream.operator.text.NextLine;
import org.apache.pdfbox.contentstream.operator.text.SetCharSpacing;
import org.apache.pdfbox.contentstream.operator.text.SetFontAndSize;
import org.apache.pdfbox.contentstream.operator.text.SetTextHorizontalScaling;
import org.apache.pdfbox.contentstream.operator.state.SetLineCapStyle;
import org.apache.pdfbox.contentstream.operator.state.SetLineDashPattern;
import org.apache.pdfbox.contentstream.operator.state.SetLineJoinStyle;
import org.apache.pdfbox.contentstream.operator.state.SetLineWidth;
import org.apache.pdfbox.contentstream.operator.state.SetMatrix;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceCMYKColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorSpace;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceGrayColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceRGBColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceCMYKColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingColorSpace;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceGrayColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceRGBColor;
import org.apache.pdfbox.contentstream.operator.state.SetGraphicsStateParameters;
import org.apache.pdfbox.contentstream.operator.text.SetTextLeading;
import org.apache.pdfbox.contentstream.operator.text.SetTextRenderingMode;
import org.apache.pdfbox.contentstream.operator.text.SetTextRise;
import org.apache.pdfbox.contentstream.operator.text.SetWordSpacing;
import org.apache.pdfbox.cos.COSArray;

/**
 * This class inherits from org.apache.pdfbox.util.PDFStreamEngine to allow the validation of specific rules in
 * ContentStream.
 */
public abstract class PreflightStreamEngine extends PDFStreamEngine
{
    private enum ColorSpaceType
    {
        RGB, CMYK, ALL
    }

    protected PreflightContext context = null;
    protected COSDocument cosDocument = null;
    protected PDPage processedPage = null;

    public PreflightStreamEngine(PreflightContext context, PDPage page)
    {
        this.context = context;
        this.cosDocument = context.getDocument().getDocument();
        this.processedPage = page;

        // Graphics operators
        addOperator(new SetLineWidth());
        addOperator(new Concatenate());

        addOperator(new SetStrokingColorSpace());
        addOperator(new SetNonStrokingColorSpace());
        addOperator(new SetLineDashPattern());
        addOperator(new DrawObject());

        addOperator(new SetLineJoinStyle());
        addOperator(new SetLineCapStyle());
        addOperator(new SetStrokingDeviceCMYKColor());
        addOperator(new SetNonStrokingDeviceCMYKColor());

        addOperator(new SetNonStrokingDeviceRGBColor());
        addOperator(new SetStrokingDeviceRGBColor());

        addOperator(new SetNonStrokingDeviceGrayColor());
        addOperator(new SetStrokingDeviceGrayColor());

        addOperator(new SetStrokingColor());
        addOperator(new SetStrokingColorN());
        addOperator(new SetNonStrokingColor());
        addOperator(new SetNonStrokingColorN());

        // Graphics state
        addOperator(new Restore());
        addOperator(new Save());

        // Text operators
        addOperator(new BeginText());
        addOperator(new EndText());
        addOperator(new SetGraphicsStateParameters());
        addOperator(new SetFontAndSize());
        addOperator(new SetTextRenderingMode());
        addOperator(new SetMatrix());
        addOperator(new MoveText());
        addOperator(new NextLine());
        addOperator(new MoveTextSetLeading());
        addOperator(new SetCharSpacing());
        addOperator(new SetTextLeading());
        addOperator(new SetTextRise());
        addOperator(new SetWordSpacing());
        addOperator(new SetTextHorizontalScaling());

        /*
         * Do not use the PDFBox Operator, because of the PageDrawer class cast Or because the Operator doesn't exist
         */

        addOperator(new StubOperator("l"));
        addOperator(new StubOperator("re"));
        addOperator(new StubOperator("c"));
        addOperator(new StubOperator("y"));
        addOperator(new StubOperator("v"));
        addOperator(new StubOperator("n"));
        addOperator(new StubOperator("BI"));
        addOperator(new StubOperator("ID"));
        addOperator(new StubOperator("EI"));
        addOperator(new StubOperator("m"));
        addOperator(new StubOperator("W*"));
        addOperator(new StubOperator("W"));
        addOperator(new StubOperator("h"));

        addOperator(new StubOperator("Tj"));
        addOperator(new StubOperator("TJ"));
        addOperator(new StubOperator("'"));
        addOperator(new StubOperator("\""));

        addOperator(new StubOperator("b"));
        addOperator(new StubOperator("B"));
        addOperator(new StubOperator("b*"));
        addOperator(new StubOperator("B*"));

        addOperator(new StubOperator("BDC"));
        addOperator(new StubOperator("BMC"));
        addOperator(new StubOperator("DP"));
        addOperator(new StubOperator("EMC"));
        addOperator(new StubOperator("BX"));
        addOperator(new StubOperator("EX"));

        addOperator(new StubOperator("d0"));
        addOperator(new StubOperator("d1"));

        addOperator(new StubOperator("f"));
        addOperator(new StubOperator("F"));
        addOperator(new StubOperator("f*"));

        addOperator(new StubOperator("M"));
        addOperator(new StubOperator("MP"));

        addOperator(new StubOperator("i"));

        addOperator(new StubOperator("ri"));
        addOperator(new StubOperator("s"));
        addOperator(new StubOperator("S"));
        addOperator(new StubOperator("sh"));
    }

    /**
     * Check operands of the "ri" operator. Operands must exist in the RenderingIntent list.
     * (net.awl.edoc.pdfa.validation.utils.RenderingIntents)
     * 
     * @param operator
     *            the "ri" operator
     * @param arguments
     *            the "ri" operands
     * @throws ContentStreamException
     *             ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY if the operand is invalid
     */
    protected void validateRenderingIntent(Operator operator, List arguments) throws ContentStreamException
    {
        if ("ri".equals(operator.getName()))
        {
            String riArgument0 = "";
            if (arguments.get(0) instanceof COSName)
            {
                riArgument0 = ((COSName) arguments.get(0)).getName();
            }
            else if (arguments.get(0) instanceof String)
            {
                riArgument0 = (String) arguments.get(0);
            }

            if (!RenderingIntents.contains(riArgument0))
            {
                registerError("Unexpected value '" + arguments.get(0) + "' for ri operand. ",
                        ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY);
            }
        }
    }

    /**
     * Valid the number of graphic states if the operator is the Save Graphic state operator ("q")
     * 
     * @param operator
     * @throws ContentStreamException
     */
    protected void validateNumberOfGraphicStates(Operator operator) throws ContentStreamException
    {
        if ("q".equals(operator.getName()))
        {
            int numberOfGraphicStates = this.getGraphicsStackSize();
            if (numberOfGraphicStates > MAX_GRAPHIC_STATES)
            {
                registerError("Too many graphic states", ERROR_GRAPHIC_TOO_MANY_GRAPHIC_STATES);
            }
        }
    }

    /**
     * Throw a ContentStreamException if the LZW filter is used in a InlinedImage.
     * 
     * @param operator the InlinedImage object (BI to EI)
     * @throws ContentStreamException
     */
    protected void validateInlineImageFilter(Operator operator) throws ContentStreamException
    {
        COSDictionary dict = operator.getImageParameters();
        /*
         * Search a Filter declaration in the InlinedImage dictionary. The LZWDecode Filter is forbidden.
         */
        COSBase filter = dict.getDictionaryObject(COSName.F, COSName.FILTER);
        FilterHelper
                .isAuthorizedFilter(context, COSUtils.getAsString(filter, this.context.getDocument().getDocument()));
    }

    /**
     * This method validates if the ColorSpace used by the InlinedImage is consistent with
     * the color space defined in OutputIntent dictionaries.
     * 
     * @param operator the InlinedImage object (BI to EI)
     * @throws IOException
     */
    protected void validateInlineImageColorSpace(Operator operator) throws IOException
    {
        COSDictionary dict = operator.getImageParameters();

        COSBase csInlinedBase = dict.getDictionaryObject(COSName.CS, COSName.COLORSPACE);
        ColorSpaceHelper csHelper = null;
        if (csInlinedBase != null)
        {
            if (COSUtils.isString(csInlinedBase, cosDocument))
            {
                // In InlinedImage only DeviceGray/RGB/CMYK and restricted Indexed
                // color spaces are allowed.
                String colorSpace = COSUtils.getAsString(csInlinedBase, cosDocument);
                ColorSpaces cs = null;

                try
                {
                    cs = ColorSpaces.valueOf(colorSpace);
                }
                catch (IllegalArgumentException e)
                {
                    // The color space is unknown. Try to access the resources dictionary,
                    // the color space can be a reference.
                    PDColorSpace pdCS = this.getResources().getColorSpace(COSName.getPDFName(colorSpace));
                    if (pdCS != null)
                    {
                        cs = ColorSpaces.valueOf(pdCS.getName());
                        csHelper = getColorSpaceHelper(pdCS);
                    }
                }

                if (cs == null)
                {
                    registerError("The ColorSpace " + colorSpace + " is unknown", ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY);
                    return;
                }
            }

            if (csHelper == null)
            {
                // convert to long names first
                csInlinedBase = toLongName(csInlinedBase);
                if (csInlinedBase instanceof COSArray && ((COSArray) csInlinedBase).size() > 1)
                {
                    COSArray srcArray = (COSArray) csInlinedBase;
                    COSBase csType = srcArray.get(0);
                    if (COSName.I.equals(csType) || COSName.INDEXED.equals(csType))
                    {
                        COSArray dstArray = new COSArray();
                        dstArray.addAll(srcArray);
                        dstArray.set(0, COSName.INDEXED);
                        dstArray.set(1, toLongName(srcArray.get(1)));
                        csInlinedBase = dstArray;
                    }
                }                
                PDColorSpace pdCS = PDColorSpace.create(csInlinedBase);
                csHelper = getColorSpaceHelper(pdCS);
            }
            csHelper.validate();
        }
    }

    private ColorSpaceHelper getColorSpaceHelper(PDColorSpace pdCS)
    {
        PreflightConfiguration cfg = context.getConfig();
        ColorSpaceHelperFactory csFact = cfg.getColorSpaceHelperFact();
        return csFact.getColorSpaceHelper(context, pdCS, ColorSpaceRestriction.ONLY_DEVICE);
    }
    
    // deliver the long name of a device colorspace, or the parameter
    private COSBase toLongName(COSBase cs)
    {
        if (COSName.RGB.equals(cs))
        {
            return COSName.DEVICERGB;
        }
        if (COSName.CMYK.equals(cs))
        {
            return COSName.DEVICECMYK;
        }
        if (COSName.G.equals(cs))
        {
            return COSName.DEVICEGRAY;
        }
        return cs;
    }
    
    /**
     * This method validates if the ColorOperator can be used with the color space
     * defined in OutputIntent dictionaries.
     * 
     * @param operation the color operator
     * @throws ContentStreamException
     */
    protected void checkColorOperators(String operation) throws ContentStreamException
    {
        PDColorSpace cs = getColorSpace(operation);

        if (("rg".equals(operation) || "RG".equals(operation)) 
                && !validColorSpace(cs, ColorSpaceType.RGB))
        {
            registerError("The operator \"" + operation + "\" can't be used with CMYK Profile",
                    ERROR_GRAPHIC_INVALID_COLOR_SPACE_RGB);
            return;
        }
        if (("k".equals(operation) || "K".equals(operation)) 
                && !validColorSpace(cs, ColorSpaceType.CMYK))
        {
            registerError("The operator \"" + operation + "\" can't be used with RGB Profile",
                    ERROR_GRAPHIC_INVALID_COLOR_SPACE_CMYK);
            return;
        }
        if (("g".equals(operation) || "G".equals(operation)
                || "f".equals(operation) || "F".equals(operation) || "f*".equals(operation)
                || "B".equals(operation) || "B*".equals(operation) || "b".equals(operation) || "b*".equals(operation))
                && !validColorSpace(cs, ColorSpaceType.ALL))
        {
            registerError("The operator \"" + operation + "\" can't be used without Color Profile",
                    ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING);
        }
    }

    private boolean validColorSpace(PDColorSpace colorSpace, ColorSpaceType expectedIccType)
            throws ContentStreamException
    {
        if (colorSpace == null)
        {
            return validColorSpaceDestOutputProfile(expectedIccType);
        }
        else
        {
            return isDeviceIndependent(colorSpace, expectedIccType) ||
                   validColorSpaceDestOutputProfile(expectedIccType);
        }
    }

    /*
     * Check if the ColorProfile provided by the DestOutputProfile entry isn't null and
     * if the ColorSpace represented by the Profile has the right type (RGB or CMYK)
     * 
     * @param expectedType
     * @return
     */
    private boolean validColorSpaceDestOutputProfile(ColorSpaceType expectedType)
            throws ContentStreamException
    {
        boolean result = false;
        ICCProfileWrapper profileWrapper;
        try
        {
            profileWrapper = ICCProfileWrapper.getOrSearchICCProfile(context);
            if (profileWrapper != null)
            {
                switch (expectedType)
                {
                case RGB:
                    result = profileWrapper.isRGBColorSpace();
                    break;
                case CMYK:
                    result = profileWrapper.isCMYKColorSpace();
                    break;
                default:
                    result = true;
                    break;
                }
            }
        }
        catch (ValidationException e)
        {
            throw new ContentStreamException(e);
        }
        return result;
    }

    /*
     * Return true if the given ColorSpace is an independent device ColorSpace.
     * If the color space is an ICCBased, check the embedded profile color (RGB or CMYK)
     */
    private boolean isDeviceIndependent(PDColorSpace cs, ColorSpaceType expectedIccType)
    {
        if (cs instanceof PDICCBased)
        {
            int type = ((PDICCBased)cs).getColorSpaceType();
            switch (expectedIccType)
            {
                case RGB: return type == ICC_ColorSpace.TYPE_RGB;
                case CMYK: return type == ICC_ColorSpace.TYPE_CMYK;
                default: return true;
            }
        }
        else if (cs instanceof PDSeparation)
        {
            return isDeviceIndependent(((PDSeparation)cs).getAlternateColorSpace(),
                    expectedIccType);
        }
        else
        {
            return cs instanceof PDCIEBasedColorSpace;
        }
    }

    /*
     * Return the current color space used by the operation
     */
    private PDColorSpace getColorSpace(String operation)
    {
        if (getGraphicsState() == null)
        {
            return null;
        }

        if (operation.equals("rg") || operation.equals("g") || operation.equals("k") ||
            operation.equals("f") || operation.equals("F") || operation.equals("f*"))
        {
            // non stroking operator
            return getGraphicsState().getNonStrokingColorSpace();
        }
        else
        {
            // stroking operator
            return getGraphicsState().getStrokingColorSpace();
        }
    }

    /**
     * This method validates if the ColorSpace used as operand is consistent with
     * the color space defined in OutputIntent dictionaries.
     * 
     * @param operator
     * @param arguments
     * @throws IOException
     */
    protected void checkSetColorSpaceOperators(Operator operator, List<?> arguments) throws IOException
    {
        if (!("CS".equals(operator.getName()) || "cs".equals(operator.getName())))
        {
            return;
        }

        String colorSpaceName;
        if (arguments.get(0) instanceof String)
        {
            colorSpaceName = (String) arguments.get(0);
        }
        else if (arguments.get(0) instanceof COSString)
        {
            colorSpaceName = (arguments.get(0)).toString();
        }
        else if (arguments.get(0) instanceof COSName)
        {
            colorSpaceName = ((COSName) arguments.get(0)).getName();
        }
        else
        {
            registerError("The operand " + arguments.get(0) + " for colorSpace operator " + 
                    operator.getName() + " doesn't have the expected type", 
                    ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY);
            return;
        }

        ColorSpaceHelper csHelper = null;
        ColorSpaces cs = null;
        try
        {
            cs = ColorSpaces.valueOf(colorSpaceName);
        }
        catch (IllegalArgumentException e)
        {
            /*
             * The color space is unknown. Try to access the resources dictionary, the color space can be a reference.
             */
            PDColorSpace pdCS = this.getResources().getColorSpace(COSName.getPDFName(colorSpaceName));
            if (pdCS != null)
            {
                cs = ColorSpaces.valueOf(pdCS.getName());
                PreflightConfiguration cfg = context.getConfig();
                ColorSpaceHelperFactory csFact = cfg.getColorSpaceHelperFact();
                csHelper = csFact.getColorSpaceHelper(context, pdCS, ColorSpaceRestriction.NO_RESTRICTION);
            }
        }

        if (cs == null)
        {
            registerError("The ColorSpace " + colorSpaceName + " is unknown", ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY);
            return;
        }

        if (csHelper == null)
        {
            PDColorSpace pdCS = PDColorSpace.create(COSName.getPDFName(colorSpaceName));
            PreflightConfiguration cfg = context.getConfig();
            ColorSpaceHelperFactory csFact = cfg.getColorSpaceHelperFact();
            csHelper = csFact.getColorSpaceHelper(context, pdCS, ColorSpaceRestriction.NO_RESTRICTION);
        }

        csHelper.validate();
    }

    /**
     * Add a validation error into the PreflightContext
     * 
     * @param msg
     *            exception details
     * @param errorCode
     *            the error code.
     */
    protected void registerError(String msg, String errorCode)
    {
        registerError(msg, errorCode, null);
    }

    public void registerError(String msg, String errorCode, Throwable cause)
    {
        registerError(msg, errorCode, false, cause);
    }
    
    protected void registerError(String msg, String errorCode, boolean warning)
    {
        registerError(msg, errorCode, warning, null);
    }

    public void registerError(String msg, String errorCode, boolean warning,
            Throwable cause)
    {
        ValidationError error = new ValidationError(errorCode, msg, cause);
        error.setWarning(warning);
        this.context.addValidationError(error);
    }
}
