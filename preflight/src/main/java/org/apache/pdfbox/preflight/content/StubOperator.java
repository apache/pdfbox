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

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_ARRAY_TOO_LONG;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_LITERAL_TOO_LONG;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_NAME_TOO_LONG;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_NUMERIC_RANGE;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_TOO_MANY_ENTRIES;
import static org.apache.pdfbox.preflight.PreflightConstants.MAX_ARRAY_ELEMENTS;
import static org.apache.pdfbox.preflight.PreflightConstants.MAX_DICT_ENTRIES;
import static org.apache.pdfbox.preflight.PreflightConstants.MAX_NAME_SIZE;
import static org.apache.pdfbox.preflight.PreflightConstants.MAX_NEGATIVE_FLOAT;
import static org.apache.pdfbox.preflight.PreflightConstants.MAX_POSITIVE_FLOAT;
import static org.apache.pdfbox.preflight.PreflightConstants.MAX_STRING_LENGTH;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.operator.OperatorProcessor;

/**
 * This implementation of OperatorProcessor allow the operator validation according PDF/A rules without compute the
 * operator actions.
 */
public class StubOperator extends OperatorProcessor
{

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pdfbox.util.operator.OperatorProcessor#process(org.apache.pdfbox .util.PDFOperator,
     * java.util.List)
     */
    @Override
    public void process(PDFOperator operator, List<COSBase> arguments) throws IOException
    {
        String op = operator.getOperation();
        if ("S".equals(op))
        {
            checkNoOperands(arguments);
        }
        else if ("B".equals(op))
        {
            checkNoOperands(arguments);
        }
        else if ("f".equals(op))
        {
            checkNoOperands(arguments);
        }
        else if ("F".equals(op))
        {
            checkNoOperands(arguments);
        }
        else if ("f*".equals(op))
        {
            checkNoOperands(arguments);
        }
        else if ("b".equals(op))
        {
            checkNoOperands(arguments);
        }
        else if ("B*".equals(op))
        {
            checkNoOperands(arguments);
        }
        else if ("b*".equals(op))
        {
            checkNoOperands(arguments);
        }
        else if ("s".equals(op))
        {
            checkNoOperands(arguments);
        }
        else if ("EMC".equals(op))
        {
            checkNoOperands(arguments);
        }
        else if ("BMC".equals(op))
        {
            checkStringOperands(arguments, 1);
        }
        else if ("BDC".equals(op))
        {
            checkTagAndPropertyOperands(arguments);
        }
        else if ("DP".equals(op))
        {
            checkTagAndPropertyOperands(arguments);
        }
        else if ("c".equals(op))
        {
            checkNumberOperands(arguments, 6);
        }
        else if ("v".equals(op))
        {
            checkNumberOperands(arguments, 4);
        }
        else if ("y".equals(op))
        {
            checkNumberOperands(arguments, 4);
        }
        else if ("d0".equals(op))
        {
            checkNumberOperands(arguments, 2);
        }
        else if ("d1".equals(op))
        {
            checkNumberOperands(arguments, 6);
        }
        else if ("g".equals(op))
        {
            checkNumberOperands(arguments, 1);
        }
        else if ("G".equals(op))
        {
            checkNumberOperands(arguments, 1);
        }
        else if ("gs".equals(op))
        {
            checkStringOperands(arguments, 1);
        }
        else if ("h".equals(op))
        {
            checkNoOperands(arguments);
        }
        else if ("i".equals(op))
        {
            checkNumberOperands(arguments, 1);
        }
        else if ("l".equals(op))
        {
            checkNumberOperands(arguments, 2);
        }
        else if ("m".equals(op))
        {
            checkNumberOperands(arguments, 2);
        }
        else if ("M".equals(op))
        {
            checkNumberOperands(arguments, 1);
        }
        else if ("MP".equals(op))
        {
            checkStringOperands(arguments, 1);
        }
        else if ("n".equals(op))
        {
            checkNoOperands(arguments);
        }
        else if ("re".equals(op))
        {
            checkNumberOperands(arguments, 4);
        }
        else if ("ri".equals(op))
        {
            checkStringOperands(arguments, 1);
        }
        else if ("s".equals(op))
        {
            checkNoOperands(arguments);
        }
        else if ("S".equals(op))
        {
            checkNoOperands(arguments);
        }
        else if ("sh".equals(op))
        {
            checkStringOperands(arguments, 1);
        }
        else if ("'".equals(op))
        {
            checkStringOperands(arguments, 1);
        }
        else if ("Tj".equals(op))
        {
            checkStringOperands(arguments, 1);
        }
        else if ("TJ".equals(op))
        {
            checkArrayOperands(arguments, 1);
        }
        else if ("W".equals(op))
        {
            checkNoOperands(arguments);
        }
        else if ("W*".equals(op))
        {
            checkNoOperands(arguments);
        }
        else if ("\"".equals(op))
        {
            checkNumberOperands(arguments.subList(0, 2), 2);
            checkStringOperands(arguments.subList(2, arguments.size()), 1);
        }
        // else
        // ---- Some operators are processed by PDFBox Objects.
        // ---- Other operators are authorized but it isn't used.

    }

    /**
     * If the arguments list of Operator isn't empty, this method throws a ContentStreamException.
     * 
     * @param arguments
     * @throws ContentStreamException
     */
    private void checkNoOperands(List<COSBase> arguments) throws ContentStreamException
    {
        if (arguments != null && !arguments.isEmpty())
        {
            throw createInvalidArgumentsError();
        }
    }

    /**
     * If the arguments list of Operator doesn't have String parameter, this method throws a ContentStreamException.
     * 
     * @param arguments
     * @param length
     * @throws ContentStreamException
     */
    private void checkStringOperands(List<COSBase> arguments, int length) throws ContentStreamException
    {
        if (arguments == null || arguments.isEmpty() || arguments.size() != length)
        {
            throw createInvalidArgumentsError();
        }

        for (int i = 0; i < length; ++i)
        {
            COSBase arg = arguments.get(i);
            if (!(arg instanceof COSName) && !(arg instanceof COSString))
            {
                throw createInvalidArgumentsError();
            }

            if (arg instanceof COSName && ((COSName) arg).getName().length() > MAX_NAME_SIZE)
            {
                throw createLimitError(ERROR_SYNTAX_NAME_TOO_LONG, "A Name operand is too long");
            }

            if (arg instanceof COSString && ((COSString) arg).getString().getBytes().length > MAX_STRING_LENGTH)
            {
                throw createLimitError(ERROR_SYNTAX_LITERAL_TOO_LONG, "A String operand is too long");
            }
        }
    }

    /**
     * If the arguments list of Operator doesn't have Array parameter, this method throws a ContentStreamException.
     * 
     * @param arguments
     * @param length
     * @throws ContentStreamException
     */
    private void checkArrayOperands(List<COSBase> arguments, int length) throws ContentStreamException
    {
        if (arguments == null || arguments.isEmpty() || arguments.size() != length)
        {
            throw createInvalidArgumentsError();
        }

        for (int i = 0; i < length; ++i)
        {
            COSBase arg = arguments.get(i);
            if (!(arg instanceof COSArray))
            {
                throw createInvalidArgumentsError();
            }

            if (((COSArray) arg).size() > MAX_ARRAY_ELEMENTS)
            {
                throw createLimitError(ERROR_SYNTAX_ARRAY_TOO_LONG, "Array has " + ((COSArray) arg).size()
                        + " elements");
            }
        }
    }

    /**
     * If the arguments list of Operator doesn't have Number parameters (Int, float...), this method throws a
     * ContentStreamException.
     * 
     * @param arguments
     *            the arguments list to check
     * @param length
     *            the expected size of the list
     * @throws ContentStreamException
     */
    private void checkNumberOperands(List<COSBase> arguments, int length) throws ContentStreamException
    {
        if (arguments == null || arguments.isEmpty() || arguments.size() != length)
        {
            throw createInvalidArgumentsError();
        }

        for (int i = 0; i < length; ++i)
        {
            COSBase arg = arguments.get(i);
            if (!(arg instanceof COSFloat) && !(arg instanceof COSInteger))
            {
                throw createInvalidArgumentsError();
            }

            if (arg instanceof COSInteger
                    && (((COSInteger) arg).longValue() > Integer.MAX_VALUE || ((COSInteger) arg).longValue() < Integer.MIN_VALUE))
            {
                throw createLimitError(ERROR_SYNTAX_NUMERIC_RANGE, "Invalid integer range in a Number operands");
            }

            if (arg instanceof COSFloat
                    && (((COSFloat) arg).doubleValue() > MAX_POSITIVE_FLOAT || ((COSFloat) arg).doubleValue() < MAX_NEGATIVE_FLOAT))
            {
                throw createLimitError(ERROR_SYNTAX_NUMERIC_RANGE, "Invalid float range in a Number operands");
            }
        }
    }

    /**
     * The given arguments list is valid only if the first argument is a Tag (A String) and if the second argument is a
     * String or a Dictionary
     * 
     * @param arguments
     * @throws ContentStreamException
     */
    private void checkTagAndPropertyOperands(List<COSBase> arguments) throws ContentStreamException
    {
        if (arguments == null || arguments.isEmpty() || arguments.size() != 2)
        {
            throw createInvalidArgumentsError();
        }

        COSBase arg = arguments.get(0);
        if (!(arg instanceof COSName) && !(arg instanceof COSString))
        {
            throw createInvalidArgumentsError();
        }

        if (arg instanceof COSName && ((COSName) arg).getName().length() > MAX_NAME_SIZE)
        {
            throw createLimitError(ERROR_SYNTAX_NAME_TOO_LONG, "A Name operand is too long");
        }

        if (arg instanceof COSString && ((COSString) arg).getString().getBytes().length > MAX_STRING_LENGTH)
        {
            throw createLimitError(ERROR_SYNTAX_LITERAL_TOO_LONG, "A String operand is too long");
        }

        COSBase arg2 = arguments.get(1);
        if (!(arg2 instanceof COSName) && !(arg2 instanceof COSString) && !(arg2 instanceof COSDictionary))
        {
            throw createInvalidArgumentsError();
        }

        if (arg2 instanceof COSName && ((COSName) arg2).getName().length() > MAX_NAME_SIZE)
        {
            throw createLimitError(ERROR_SYNTAX_NAME_TOO_LONG, "A Name operand is too long");
        }

        if (arg2 instanceof COSString && ((COSString) arg2).getString().getBytes().length > MAX_STRING_LENGTH)
        {
            throw createLimitError(ERROR_SYNTAX_LITERAL_TOO_LONG, "A String operand is too long");
        }

        if (arg2 instanceof COSDictionary && ((COSDictionary) arg2).size() > MAX_DICT_ENTRIES)
        {
            throw createLimitError(ERROR_SYNTAX_TOO_MANY_ENTRIES, "Dictionary has " + ((COSDictionary) arg2).size()
                    + " entries");
        }
    }

    /**
     * Create a ContentStreamException with ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT.
     * 
     * @return
     */
    private ContentStreamException createInvalidArgumentsError()
    {
        ContentStreamException ex = new ContentStreamException("Invalid arguments");
        ex.setErrorCode(ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT);
        return ex;
    }

    /**
     * Create a ContentStreamException with ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT.
     * 
     * @return
     */
    private ContentStreamException createLimitError(String errorCode, String details)
    {
        ContentStreamException ex = new ContentStreamException(details);
        ex.setErrorCode(errorCode);
        return ex;
    }
}
