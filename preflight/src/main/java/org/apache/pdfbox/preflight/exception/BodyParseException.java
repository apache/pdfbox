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

package org.apache.pdfbox.preflight.exception;

import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.javacc.PDFParserConstants;
import org.apache.pdfbox.preflight.javacc.ParseException;

/**
 * This Exception is thrown if an validation error occurs during the javacc validation in the PDF Body.
 * 
 * Error codes provided by this exception should start by 1.2 or 1.0.
 */
public class BodyParseException extends PdfParseException
{

    /*
     * (non-Javadoc)
     * 
     * @see net.awl.edoc.pdfa.validation.PdfParseException#PdfParseException(net.awl
     * .edoc.pdfa.validation.ParseException)
     */
    public BodyParseException(ParseException e)
    {
        super(e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.awl.edoc.pdfa.validation.PdfParseException#PdfParseException(java.lang .String,java.lang.String)
     */
    public BodyParseException(String message, String code)
    {
        super(message, code);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.awl.edoc.pdfa.validation.PdfParseException#PdfParseException(java.lang .String)
     */
    public BodyParseException(String message)
    {
        super(message);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.awl.edoc.pdfa.validation.PdfParseException#getErrorCode()
     */
    @Override
    public String getErrorCode()
    {
        if (this.errorCode != null)
        {
            return this.errorCode;
        }

        // default error code
        this.errorCode = PreflightConstants.ERROR_SYNTAX_BODY;

        if (!isTokenMgrError)
        {
            // it is a parse error, according to the ExpectedTokens data
            // some error code can be returned
            if (this.expectedTokenSequences != null)
            {
                // check object delimiters error
                for (int i = 0; i < this.expectedTokenSequences.length; ++i)
                {
                    // Check only the first Expected token on each array.
                    // Others can be check if some choice can start by the same token
                    // in this case, a factorization is possible
                    switch (this.expectedTokenSequences[i][0])
                    {
                    case PDFParserConstants.START_OBJECT:
                    case PDFParserConstants.END_OBJECT:
                        this.errorCode = PreflightConstants.ERROR_SYNTAX_OBJ_DELIMITER;
                        break;
                    }
                }
                // add here other error code
            }
        }

        return errorCode;
    }
}
