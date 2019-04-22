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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.pdmodel.graphics.state.PDTextState;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.font.container.FontContainer;
import org.apache.pdfbox.preflight.font.util.GlyphException;


import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_ENCODING_ERROR;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_UNKNOWN_FONT_REF;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_CONTENT_STREAM_UNSUPPORTED_OP;

public class PreflightContentStream extends PreflightStreamEngine
{
    public PreflightContentStream(PreflightContext _context, PDPage _page)
    {
        super(_context, _page);
    }

    /**
     * Process the validation of a PageContent (The page is initialized by the constructor)
     *
     * @throws ValidationException
     */
    public void validatePageContentStream() throws ValidationException
    {
        try
        {
            if (processedPage.hasContents())
            {
                processPage(processedPage);
            }
        }
        catch (ContentStreamException e)
        {
            context.addValidationError(new ValidationError(e.getErrorCode(), e.getMessage()));
        }
        catch (IOException e)
        {
            throw new ValidationException("Unable to check the Page ContentStream : " + e.getMessage(), e);
        }
    }

    /**
     * Process the validation of a XObject Form
     * 
     * @param form the PDFormXObject to be validated.
     * @throws ValidationException
     */
    public void validateXObjContentStream(PDFormXObject form) throws ValidationException
    {
        try
        {
            // workaround for preflight not finding widget annotation parent PDPage
            if (processedPage == null)
            {
                // dummy page, resource lookup may fail
                processChildStream(form, new PDPage()); 
            }
            else
            {
                processChildStream(form, processedPage);
            }
        }
        catch (ContentStreamException e)
        {
            context.addValidationError(new ValidationError(e.getErrorCode(), e.getMessage()));
        }
        catch (IOException e)
        {
            throw new ValidationException("Unable to check the XObject ContentStream : " + e.getMessage(), e);
        }
    }

    /**
     * Process the validation of a Tiling Pattern
     * 
     * @param pattern the PDTilingPattern to be validated.
     * @throws ValidationException
     */
    public void validatePatternContentStream(PDTilingPattern pattern) throws ValidationException
    {
        try
        {
            processChildStream(pattern, processedPage);
        }
        catch (ContentStreamException e)
        {
            context.addValidationError(new ValidationError(e.getErrorCode(), e.getMessage()));
        }
        catch (IOException e)
        {
            throw new ValidationException("Unable to check the Pattern ContentStream : " + e.getMessage(), e);
        }
    }

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException
    {
        super.processOperator(operator, operands);

        // todo: why are the checks below done here and not in OperatorProcessor classes?

        /*
         * Process Specific Validation. The Generic Processing is useless for PDF/A validation
         */
        if (OperatorName.BEGIN_INLINE_IMAGE.equals(operator.getName()))
        {
            validateInlineImageFilter(operator);
            validateInlineImageColorSpace(operator);
        }

        checkShowTextOperators(operator, operands);
        checkColorOperators(operator.getName());
        validateRenderingIntent(operator, operands);
        checkSetColorSpaceOperators(operator, operands);
        validateNumberOfGraphicStates(operator);
        validateDefaultColorSpace(operator);
    }

    @Override
    protected void unsupportedOperator(Operator operator, List<COSBase> arguments)
    {
        registerError("The operator \"" + operator.getName() + "\" isn't supported.",
                ERROR_SYNTAX_CONTENT_STREAM_UNSUPPORTED_OP);
    }

    /**
     * Process Text Validation. Depending on the operator parameter, this will either call
     * <code>validateStringDefinition</code> or <code>validateStringArray</code>.
     *
     * @param operator
     * @param arguments
     * @throws IOException
     */
    protected void checkShowTextOperators(Operator operator, List<?> arguments) throws IOException
    {
        String op = operator.getName();
        if (OperatorName.SHOW_TEXT.equals(op) || OperatorName.SHOW_TEXT_LINE.equals(op)
                || OperatorName.SHOW_TEXT_LINE_AND_SPACE.equals(op))
        {
            validateStringDefinition(operator, arguments);
        }

        if (OperatorName.SHOW_TEXT_ADJUSTED.equals(op))
        {
            validateStringArray(operator, arguments);
        }
    }

    /**
     * Process Text Validation for the Operands of a Tj, "'" and "\"" operator.
     * 
     * If the validation fails for an unexpected reason, a IOException is thrown. If the validation fails due to
     * validation error, a ContentStreamException is thrown. (Use the ValidationError attribute to know the cause)
     * 
     * @param operator
     * @param arguments
     * @throws IOException
     */
    private void validateStringDefinition(Operator operator, List<?> arguments) throws IOException
    {
        /*
         * For a Text operator, the arguments list should contain only one COSString object
         */
        if (OperatorName.SHOW_TEXT_LINE_AND_SPACE.equals(operator.getName()))
        {
            if (arguments.size() != 3)
            {
                registerError("Invalid argument for the operator : " + operator.getName(),
                        ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT);
                return;
            }
            Object arg0 = arguments.get(0);
            Object arg1 = arguments.get(1);
            Object arg2 = arguments.get(2);
            if (!(arg0 instanceof COSInteger || arg0 instanceof COSFloat)
                    || !(arg1 instanceof COSInteger || arg1 instanceof COSFloat))
            {
                registerError("Invalid argument for the operator : " + operator.getName(),
                        ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT);
                return;
            }

            if (arg2 instanceof COSString)
            {
                validateText(((COSString) arg2).getBytes());
            }
            else
            {
                registerError("Invalid argument for the operator : " + operator.getName(),
                        ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT);
            }
        }
        else
        {
            Object objStr = arguments.get(0);
            if (objStr instanceof COSString)
            {
                validateText(((COSString) objStr).getBytes());
            }
            else if (!(objStr instanceof COSInteger))
            {
                registerError("Invalid argument for the operator : " + operator.getName(),
                        ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT);
            }
        }
    }

    /**
     * Process Text Validation for the Operands of a TJ operator.
     * 
     * If the validation fails for an unexpected reason, a IOException is thrown. If the validation fails due to
     * validation error, a ContentStreamException is thrown. (Use the ValidationError attribute to know the cause)
     * 
     * @param operator
     * @param arguments
     * @throws IOException
     */
    private void validateStringArray(Operator operator, List<?> arguments) throws IOException
    {
        for (Object object : arguments)
        {
            if (object instanceof COSArray)
            {
                validateStringArray(operator, ((COSArray) object).toList());
            }
            else if (object instanceof COSString)
            {
                validateText(((COSString) object).getBytes());
            }
            else if (!(object instanceof COSInteger || object instanceof COSFloat))
            {
                registerError("Invalid argument for the operator : " + operator.getName(),
                        ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT);
                return;
            }
        }
    }

    /**
     * Process the validation of a Text operand contains in a ContentStream This validation checks that :
     * <UL>
     * <li>The font isn't missing if the Rendering Mode isn't 3
     * <li>The font metrics are consistent
     * <li>All character used in the text are defined in the font program.
     * </UL>
     * 
     * @param string
     * @throws IOException
     */
    public void validateText(byte[] string) throws IOException
    {
        // TextSize accessible through the TextState
        PDTextState textState = getGraphicsState().getTextState();
        final RenderingMode renderingMode = textState.getRenderingMode();
        final PDFont font = textState.getFont();
        if (font == null)
        {
            // Unable to decode the Text without Font
            registerError("Text operator can't be processed without a Font", ERROR_FONTS_UNKNOWN_FONT_REF);
            return;
        }

        FontContainer<?> fontContainer = context.getFontContainer(font.getCOSObject());
        if (renderingMode == RenderingMode.NEITHER && (fontContainer == null || !fontContainer.isEmbeddedFont()))
        {
            // font not embedded and rendering mode is 3. Valid case and nothing to check
            return;
        }
        else if (fontContainer == null)
        {
            // Font Must be embedded if the RenderingMode isn't 3
            if (font.getName() == null)
            {
                registerError("invalid font dictionary", ERROR_FONTS_UNKNOWN_FONT_REF);
            }
            else
            {
                registerError("font '" + font.getName() + "' is missing", ERROR_FONTS_UNKNOWN_FONT_REF);
            }
            return;
        }
        else if (!fontContainer.isValid() && !fontContainer.errorsAleadyMerged())
        {
            context.addValidationErrors(fontContainer.getAllErrors());
            fontContainer.setErrorsAlreadyMerged(true);
            return;
        }
        if (!fontContainer.isValid() && fontContainer.errorsAleadyMerged())
        {
            // font already computed
            return;
        }

        InputStream in = new ByteArrayInputStream(string);
        while (in.available() > 0)
        {
            try
            {
                int code = font.readCode(in);
                fontContainer.checkGlyphWidth(code);
            }
            catch (IOException e)
            {
                registerError("Encoding can't interpret the character code", ERROR_FONTS_ENCODING_ERROR, e);
                return;
            }
            catch (GlyphException e)
            {
                if (renderingMode != RenderingMode.NEITHER)
                {
                    registerError(e.getMessage(), e.getErrorCode(), e);
                    return;
                }
            }
        }
    }
}
