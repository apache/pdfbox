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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.color.PDCIEBasedColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
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
import org.apache.pdfbox.util.operator.DrawObject;
import org.apache.pdfbox.util.operator.Operator;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.operator.text.BeginText;
import org.apache.pdfbox.util.operator.state.Concatenate;
import org.apache.pdfbox.util.operator.text.EndText;
import org.apache.pdfbox.util.operator.state.Restore;
import org.apache.pdfbox.util.operator.state.Save;
import org.apache.pdfbox.util.operator.text.MoveText;
import org.apache.pdfbox.util.operator.text.MoveTextSetLeading;
import org.apache.pdfbox.util.operator.text.NextLine;
import org.apache.pdfbox.util.operator.OperatorProcessor;
import org.apache.pdfbox.util.operator.text.SetCharSpacing;
import org.apache.pdfbox.util.operator.text.SetHorizontalTextScaling;
import org.apache.pdfbox.util.operator.state.SetLineCapStyle;
import org.apache.pdfbox.util.operator.state.SetLineDashPattern;
import org.apache.pdfbox.util.operator.state.SetLineJoinStyle;
import org.apache.pdfbox.util.operator.state.SetLineWidth;
import org.apache.pdfbox.util.operator.state.SetMatrix;
import org.apache.pdfbox.util.operator.color.SetNonStrokingDeviceCMYKColor;
import org.apache.pdfbox.util.operator.color.SetNonStrokingColor;
import org.apache.pdfbox.util.operator.color.SetNonStrokingColorSpace;
import org.apache.pdfbox.util.operator.color.SetNonStrokingDeviceRGBColor;
import org.apache.pdfbox.util.operator.color.SetStrokingDeviceCMYKColor;
import org.apache.pdfbox.util.operator.color.SetStrokingColor;
import org.apache.pdfbox.util.operator.color.SetStrokingColorSpace;
import org.apache.pdfbox.util.operator.color.SetStrokingDeviceRGBColor;
import org.apache.pdfbox.util.operator.text.SetTextFont;
import org.apache.pdfbox.util.operator.text.SetTextLeading;
import org.apache.pdfbox.util.operator.text.SetTextRenderingMode;
import org.apache.pdfbox.util.operator.text.SetTextRise;
import org.apache.pdfbox.util.operator.text.SetWordSpacing;

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

    protected PDPage processeedPage = null;

    protected Map<String, OperatorProcessor> contentStreamEngineOperators = new HashMap<String, OperatorProcessor>();

    public PreflightStreamEngine(PreflightContext _context, PDPage _page)
    {
        super();
        this.context = _context;
        this.cosDocument = _context.getDocument().getDocument();
        this.processeedPage = _page;

        // Graphics operators
        registerOperatorProcessor("w", new SetLineWidth());
        registerOperatorProcessor("cm", new Concatenate());

        registerOperatorProcessor("CS", new SetStrokingColorSpace());
        registerOperatorProcessor("cs", new SetNonStrokingColorSpace());
        registerOperatorProcessor("d", new SetLineDashPattern());
        registerOperatorProcessor("Do", new DrawObject());

        registerOperatorProcessor("j", new SetLineJoinStyle());
        registerOperatorProcessor("J", new SetLineCapStyle());
        registerOperatorProcessor("K", new SetStrokingDeviceCMYKColor());
        registerOperatorProcessor("k", new SetNonStrokingDeviceCMYKColor());

        registerOperatorProcessor("rg", new SetNonStrokingDeviceRGBColor());
        registerOperatorProcessor("RG", new SetStrokingDeviceRGBColor());

        registerOperatorProcessor("SC", new SetStrokingColor());
        registerOperatorProcessor("SCN", new SetStrokingColor());
        registerOperatorProcessor("sc", new SetNonStrokingColor());
        registerOperatorProcessor("scn", new SetNonStrokingColor());

        // Graphics state
        registerOperatorProcessor("Q", new Restore());
        registerOperatorProcessor("q", new Save());

        // Text operators
        registerOperatorProcessor("BT", new BeginText());
        registerOperatorProcessor("ET", new EndText());
        registerOperatorProcessor("Tf", new SetTextFont());
        registerOperatorProcessor("Tr", new SetTextRenderingMode());
        registerOperatorProcessor("Tm", new SetMatrix());
        registerOperatorProcessor("Td", new MoveText());
        registerOperatorProcessor("T*", new NextLine());
        registerOperatorProcessor("TD", new MoveTextSetLeading());
        registerOperatorProcessor("Tc", new SetCharSpacing());
        registerOperatorProcessor("TL", new SetTextLeading());
        registerOperatorProcessor("Ts", new SetTextRise());
        registerOperatorProcessor("Tw", new SetWordSpacing());
        registerOperatorProcessor("Tz", new SetHorizontalTextScaling());

        /*
         * Do not use the PDFBox Operator, because of the PageDrawer class cast Or because the Operator doesn't exist
         */
        StubOperator stubOp = new StubOperator();
        registerOperatorProcessor("l", stubOp);
        registerOperatorProcessor("re", stubOp);
        registerOperatorProcessor("c", stubOp);
        registerOperatorProcessor("y", stubOp);
        registerOperatorProcessor("v", stubOp);
        registerOperatorProcessor("n", stubOp);
        registerOperatorProcessor("BI", stubOp);
        registerOperatorProcessor("ID", stubOp);
        registerOperatorProcessor("EI", stubOp);
        registerOperatorProcessor("m", stubOp);
        registerOperatorProcessor("W*", stubOp);
        registerOperatorProcessor("W", stubOp);
        registerOperatorProcessor("h", stubOp);

        registerOperatorProcessor("Tj", stubOp);
        registerOperatorProcessor("TJ", stubOp);
        registerOperatorProcessor("'", stubOp);
        registerOperatorProcessor("\"", stubOp);

        registerOperatorProcessor("b", stubOp);
        registerOperatorProcessor("B", stubOp);
        registerOperatorProcessor("b*", stubOp);
        registerOperatorProcessor("B*", stubOp);

        registerOperatorProcessor("BDC", stubOp);
        registerOperatorProcessor("BMC", stubOp);
        registerOperatorProcessor("DP", stubOp);
        registerOperatorProcessor("EMC", stubOp);
        registerOperatorProcessor("BX", stubOp);
        registerOperatorProcessor("EX", stubOp);

        registerOperatorProcessor("d0", stubOp);
        registerOperatorProcessor("d1", stubOp);

        registerOperatorProcessor("f", stubOp);
        registerOperatorProcessor("F", stubOp);
        registerOperatorProcessor("f*", stubOp);

        registerOperatorProcessor("g", stubOp);
        registerOperatorProcessor("G", stubOp);

        registerOperatorProcessor("M", stubOp);
        registerOperatorProcessor("MP", stubOp);

        registerOperatorProcessor("gs", stubOp);
        registerOperatorProcessor("h", stubOp);
        registerOperatorProcessor("i", stubOp);

        registerOperatorProcessor("ri", stubOp);
        registerOperatorProcessor("s", stubOp);
        registerOperatorProcessor("S", stubOp);
        registerOperatorProcessor("sh", stubOp);
    }

    @Override
    public final void registerOperatorProcessor(String operator, OperatorProcessor op)
    {
        super.registerOperatorProcessor(operator, op);
        contentStreamEngineOperators.put(operator, op);
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
    protected void validRenderingIntent(Operator operator, List arguments) throws ContentStreamException
    {
        if ("ri".equals(operator.getOperation()))
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
    protected void validNumberOfGraphicStates(Operator operator) throws ContentStreamException
    {
        if ("q".equals(operator.getOperation()))
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
    protected void validImageFilter(Operator operator) throws ContentStreamException
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
     * @throws ContentStreamException
     */
    protected void validImageColorSpace(Operator operator) throws IOException
    {
        COSDictionary dict = operator.getImageParameters();

        COSBase csInlinedBase = dict.getItem(COSName.CS);
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
                    Map<String, PDColorSpace> colorSpaces = this.getResources().getColorSpaces();
                    if (colorSpaces != null)
                    {
                        PDColorSpace pdCS = colorSpaces.get(colorSpace);
                        if (pdCS != null)
                        {
                            cs = ColorSpaces.valueOf(pdCS.getName());
                            PreflightConfiguration cfg = context.getConfig();
                            ColorSpaceHelperFactory csFact = cfg.getColorSpaceHelperFact();
                            csHelper = csFact.getColorSpaceHelper(context, pdCS, ColorSpaceRestriction.ONLY_DEVICE);
                        }
                    }
                }

                if (cs == null)
                {
                    registerError("The ColorSpace is unknown", ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY);
                    return;
                }
            }

            if (csHelper == null)
            {
                PDColorSpace pdCS = PDColorSpace.create(csInlinedBase);
                PreflightConfiguration cfg = context.getConfig();
                ColorSpaceHelperFactory csFact = cfg.getColorSpaceHelperFact();
                csHelper = csFact.getColorSpaceHelper(context, pdCS, ColorSpaceRestriction.ONLY_DEVICE);
            }

            csHelper.validate();
        }
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

        if ("rg".equals(operation) || "RG".equals(operation))
        {
            if (!validColorSpace(cs, ColorSpaceType.RGB))
            {
                registerError("The operator \"" + operation + "\" can't be used with CMYK Profile",
                        ERROR_GRAPHIC_INVALID_COLOR_SPACE_RGB);
                return;
            }
        }

        if ("k".equals(operation) || "K".equals(operation))
        {
            if (!validColorSpace(cs, ColorSpaceType.CMYK))
            {
                registerError("The operator \"" + operation + "\" can't be used with RGB Profile",
                        ERROR_GRAPHIC_INVALID_COLOR_SPACE_CMYK);
                return;
            }
        }

        if ("g".equals(operation) || "G".equals(operation))
        {
            if (!validColorSpace(cs, ColorSpaceType.ALL))
            {
                registerError("The operator \"" + operation + "\" can't be used without Color Profile",
                        ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING);
                return;
            }
        }

        if ("f".equals(operation) || "F".equals(operation) || "f*".equals(operation) || "B".equals(operation)
                || "B*".equals(operation) || "b".equals(operation) || "b*".equals(operation))
        {
            if (!validColorSpace(cs, ColorSpaceType.ALL))
            {
                // The default fill color needs an OutputIntent
                registerError("The operator \"" + operation + "\" can't be used without Color Profile",
                        ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING);
            }
        }
    }

    private boolean validColorSpace(PDColorSpace colorSpace, ColorSpaceType expectedType)
            throws ContentStreamException
    {
        if (colorSpace == null)
        {
            return validColorSpaceDestOutputProfile(expectedType);
        }
        else
        {
            return isDeviceIndependent(colorSpace, expectedType) ||
                   validColorSpaceDestOutputProfile(expectedType);
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
    private boolean isDeviceIndependent(PDColorSpace cs, ColorSpaceType expectedType)
    {
        if (cs instanceof PDICCBased)
        {
            int type = ((PDICCBased)cs).getColorSpaceType();
            switch (expectedType)
            {
                case RGB: return type == ICC_ColorSpace.TYPE_RGB;
                case CMYK: return type == ICC_ColorSpace.TYPE_CMYK;
                default: return true;
            }
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
        if (!("CS".equals(operator.getOperation()) || "cs".equals(operator.getOperation())))
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
            colorSpaceName = ((COSString) arguments.get(0)).toString();
        }
        else if (arguments.get(0) instanceof COSName)
        {
            colorSpaceName = ((COSName) arguments.get(0)).getName();
        }
        else
        {
            registerError("The operand doesn't have the expected type", ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY);
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
            Map<String, PDColorSpace> colorSpaces = this.getResources().getColorSpaces();
            if (colorSpaces != null)
            {
                PDColorSpace pdCS = colorSpaces.get(colorSpaceName);
                if (pdCS != null)
                {
                    cs = ColorSpaces.valueOf(pdCS.getName());
                    PreflightConfiguration cfg = context.getConfig();
                    ColorSpaceHelperFactory csFact = cfg.getColorSpaceHelperFact();
                    csHelper = csFact.getColorSpaceHelper(context, pdCS, ColorSpaceRestriction.NO_RESTRICTION);
                }
            }
        }

        if (cs == null)
        {
            registerError("The ColorSpace is unknown", ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY);
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
        registerError(msg, errorCode, false);
    }
    
    protected void registerError(String msg, String errorCode, boolean warning)
    {
        ValidationError error = new ValidationError(errorCode, msg);
        error.setWarning(warning);
        this.context.addValidationError(error);
    }
}
