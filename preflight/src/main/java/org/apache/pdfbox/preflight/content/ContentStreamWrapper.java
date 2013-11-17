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

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_ENCODING_ERROR;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_UNKNOWN_FONT_REF;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_CONTENT_STREAM_UNSUPPORTED_OP;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.text.PDTextState;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.font.container.FontContainer;
import org.apache.pdfbox.preflight.font.util.GlyphException;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.operator.OperatorProcessor;

public class ContentStreamWrapper extends ContentStreamEngine
{

    public ContentStreamWrapper(PreflightContext _context, PDPage _page)
    {
        super(_context, _page);
    }

    /**
     * Process the validation of a PageContent (The page is initialized by the constructor)
     * 
     * @return A list of validation error. This list is empty if the validation succeed.
     * @throws ValidationException.
     */
    public void validPageContentStream() throws ValidationException
    {
        try
        {
            PDStream pstream = this.processeedPage.getContents();
            if (pstream != null)
            {
                processStream(processeedPage, processeedPage.findResources(), pstream.getStream());
            }
        }
        catch (ContentStreamException e)
        {
            context.addValidationError(new ValidationError(e.getErrorCode(), e.getMessage()));
        }
        catch (IOException e)
        {
            throw new ValidationException("Unable to check the ContentStream : " + e.getMessage(), e);
        }
    }

    /**
     * Process the validation of a XObject Form
     * 
     * @param xobj
     * @return A list of validation error. This list is empty if the validation succeed.
     * @throws ValidationException
     */
    public void validXObjContentStream(PDXObjectForm xobj) throws ValidationException
    {
        try
        {
            resetEnginContext();
            processSubStream(this.processeedPage, xobj.getResources(), xobj.getCOSStream());
        }
        catch (ContentStreamException e)
        {
            context.addValidationError(new ValidationError(e.getErrorCode(), e.getMessage()));
        }
        catch (IOException e)
        {
            throw new ValidationException("Unable to check the ContentStream : " + e.getMessage(), e);
        }
    }

    /**
     * Process the validation of a Tiling Pattern
     * 
     * @param pattern
     * @return A list of validation error. This list is empty if the validation succeed.
     * @throws ValidationException
     */
    public void validPatternContentStream(COSStream pattern) throws ValidationException
    {
        try
        {
            COSDictionary res = (COSDictionary) pattern.getDictionaryObject(COSName.RESOURCES);
            resetEnginContext();
            processSubStream(this.processeedPage, new PDResources(res), pattern);
        }
        catch (ContentStreamException e)
        {
            context.addValidationError(new ValidationError(e.getErrorCode(), e.getMessage()));
        }
        catch (IOException e)
        {
            throw new ValidationException("Unable to check the ContentStream : " + e.getMessage(), e);
        }
    }

    public final void resetEnginContext()
    {
        this.setGraphicsState(new PDGraphicsState());
        this.setTextMatrix(null);
        this.setTextLineMatrix(null);
        this.getGraphicsStack().clear();
        // this.streamResourcesStack.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pdfbox.util.PDFStreamEngine#processOperator(org.apache.pdfbox .util.PDFOperator, java.util.List)
     */
    protected void processOperator(PDFOperator operator, List arguments) throws IOException
    {
        /*
         * Here is a copy of the super method because the else block is different. (If the operator is unknown, throw an
         * exception)
         */
        String operation = operator.getOperation();
        OperatorProcessor processor = (OperatorProcessor) contentStreamEngineOperators.get(operation);
        if (processor != null)
        {
            processor.setContext(this);
            processor.process(operator, arguments);
        }
        else
        {
            registerError("The operator \"" + operation + "\" isn't supported.",
                    ERROR_SYNTAX_CONTENT_STREAM_UNSUPPORTED_OP);
            return;
        }

        /*
         * Process Specific Validation. The Generic Processing is useless for PDFA validation
         */
        if ("BI".equals(operation))
        {
            validImageFilter(operator);
            validImageColorSpace(operator);
        }

        checkShowTextOperators(operator, arguments);
        checkColorOperators(operation);
        validRenderingIntent(operator, arguments);
        checkSetColorSpaceOperators(operator, arguments);
        validNumberOfGraphicStates(operator);
    }

    /**
     * Process Text Validation. According to the operator one of the both method will be called.
     * (validStringDefinition(PDFOperator operator, List<?> arguments) / validStringArray(PDFOperator operator, List<?>
     * arguments))
     * 
     * @param operator
     * @param arguments
     * @throws ContentStreamException
     * @throws IOException
     */
    protected void checkShowTextOperators(PDFOperator operator, List<?> arguments) throws ContentStreamException,
            IOException
    {
        String op = operator.getOperation();
        if ("Tj".equals(op) || "'".equals(op) || "\"".equals(op))
        {
            validStringDefinition(operator, arguments);
        }

        if ("TJ".equals(op))
        {
            validStringArray(operator, arguments);
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
     * @throws ContentStreamException
     * @throws IOException
     */
    private void validStringDefinition(PDFOperator operator, List<?> arguments) throws ContentStreamException,
            IOException
    {
        /*
         * For a Text operator, the arguments list should contain only one COSString object
         */
        if ("\"".equals(operator.getOperation()))
        {
            if (arguments.size() != 3)
            {
                registerError("Invalid argument for the operator : " + operator.getOperation(),
                        ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT);
                return;
            }
            Object arg0 = arguments.get(0);
            Object arg1 = arguments.get(1);
            Object arg2 = arguments.get(2);
            if (!(arg0 instanceof COSInteger || arg0 instanceof COSFloat)
                    || !(arg1 instanceof COSInteger || arg1 instanceof COSFloat))
            {
                registerError("Invalid argument for the operator : " + operator.getOperation(),
                        ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT);
                return;
            }

            if (arg2 instanceof COSString)
            {
                validText(((COSString) arg2).getBytes());
            }
            else
            {
                registerError("Invalid argument for the operator : " + operator.getOperation(),
                        ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT);
                return;
            }
        }
        else
        {
            Object objStr = arguments.get(0);
            if (objStr instanceof COSString)
            {
                validText(((COSString) objStr).getBytes());
            }
            else if (!(objStr instanceof COSInteger))
            {
                registerError("Invalid argument for the operator : " + operator.getOperation(),
                        ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT);
                return;
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
     * @throws ContentStreamException
     * @throws IOException
     */
    private void validStringArray(PDFOperator operator, List<?> arguments) throws ContentStreamException, IOException
    {
        for (Object object : arguments)
        {
            if (object instanceof COSArray)
            {
                validStringArray(operator, ((COSArray) object).toList());
            }
            else if (object instanceof COSString)
            {
                validText(((COSString) object).getBytes());
            }
            else if (!(object instanceof COSInteger || object instanceof COSFloat))
            {
                registerError("Invalid argument for the operator : " + operator.getOperation(),
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
    public void validText(byte[] string) throws IOException
    {
        // TextSize accessible through the TextState
        PDTextState textState = getGraphicsState().getTextState();
        final int renderingMode = textState.getRenderingMode();
        final PDFont font = textState.getFont();
        if (font == null)
        {
            // Unable to decode the Text without Font
            registerError("Text operator can't be process without Font", ERROR_FONTS_UNKNOWN_FONT_REF);
            return;
        }

        FontContainer fontContainer = context.getFontContainer(font.getCOSObject());
        if (renderingMode == 3 && (fontContainer == null || !fontContainer.isEmbeddedFont()))
        {
            // font not embedded and rendering mode is 3. Valid case and nothing to check
            return;
        }
        else if (fontContainer == null)
        {
            // Font Must be embedded if the RenderingMode isn't 3
            registerError(font.getBaseFont() + " is unknown wasn't found by the FontHelperValdiator",
                    ERROR_FONTS_UNKNOWN_FONT_REF);
            return;
        }
        else if (!fontContainer.isValid() && !fontContainer.errorsAleadyMerged())
        {
            context.addValidationErrors(fontContainer.getAllErrors());
            fontContainer.setErrorsAleadyMerged(true);
            return;
        }
        if (!fontContainer.isValid() && fontContainer.errorsAleadyMerged())
        {
            // font already computed
            return;
        }

        int codeLength = 1;
        for (int i = 0; i < string.length; i += codeLength)
        {
            // explore the string to detect character code (length can be 1 or 2 bytes)
            int cid = -1;
            codeLength = 1;
            try
            {
                // according to the encoding, extract the character identifier
                cid = font.encodeToCID(string, i, codeLength);
                if (cid == -1 && i + 1 < string.length)
                {
                    // maybe a multibyte encoding
                    codeLength++;
                    cid = font.encodeToCID(string, i, codeLength);
                }
                fontContainer.checkGlyphWith(cid);
            }
            catch (IOException e)
            {
                registerError("Encoding can't interpret the character code", ERROR_FONTS_ENCODING_ERROR);
                return;
            }
            catch (GlyphException e)
            {
                if (renderingMode != 3)
                {
                    registerError(e.getMessage(), e.getErrorCode());
                    return;
                }
            }
        }
    }
}
