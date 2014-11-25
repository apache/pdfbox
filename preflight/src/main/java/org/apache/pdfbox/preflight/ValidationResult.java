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

package org.apache.pdfbox.preflight;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmpbox.XMPMetadata;

/**
 * Object returned by the validate method of the PDFValidator. This object contains a boolean to know if the PDF is
 * PDF/A-1<I>x</I> compliant. If the document isn't PDF/A-1<I>x</I> a list of errors is provided.
 */
public class ValidationResult
{
    /**
     * Boolean to know if the PDF is a valid PDF/A
     */
    private boolean isValid = false;

    /**
     * Errors to know why the PDF isn't valid. If the PDF is valid, this list is empty.
     */
    private List<ValidationError> lErrors = new ArrayList<ValidationError>();

    /**
     * Object representation of the XMPMetaData contained by the pdf file This attribute can be null if the Validation
     * fails.
     */
    private XMPMetadata xmpMetaData = null;

    /**
     * Create a Validation result object
     * 
     * @param isValid
     */
    public ValidationResult(boolean isValid)
    {
        this.isValid = isValid;
    }

    /**
     * Create a Validation Result object. This constructor force the isValid to false and add the given error to the
     * list or ValidationErrors.
     * 
     * @param error
     *            if error is null, no error is added to the list.
     */
    public ValidationResult(ValidationError error)
    {
        this.isValid = false;
        if (error != null)
        {
            this.lErrors.add(error);
        }
    }

    /**
     * Create a Validation Result object. This constructor force the isValid to false and add all the given errors to
     * the list or ValidationErrors.
     * 
     * @param errors
     *            if error is null, no error is added to the list.
     */
    public ValidationResult(List<ValidationError> errors)
    {
        this.isValid = false;
        this.lErrors = errors;
    }

    /**
     * Add the ValidationError object of the otherResult in the Error list of the current object. Apply a logical AND on
     * the isValid boolean.
     * 
     * @param otherResult
     */
    public void mergeResult(ValidationResult otherResult)
    {
        if (otherResult != null)
        {
            this.lErrors.addAll(otherResult.getErrorsList());
            this.isValid &= otherResult.isValid();
        }
    }

    /**
     * @return the xmpMetaData
     */
    public XMPMetadata getXmpMetaData()
    {
        return xmpMetaData;
    }

    /**
     * @param xmpMetaData
     *            the xmpMetaData to set
     */
    void setXmpMetaData(XMPMetadata xmpMetaData)
    {
        this.xmpMetaData = xmpMetaData;
    }

    /**
     * @return true if the PDF is valid,false otherwise
     */
    public boolean isValid()
    {
        return isValid;
    }

    /**
     * Add error to the list of ValidationError. If the given error is null, this method does nothing
     * 
     * @param error
     */
    public void addError(ValidationError error)
    {
        if (error != null)
        {
            this.isValid &= error.isWarning();
            this.lErrors.add(error);
        }
    }

    /**
     * Add a set of errors to the list of ValidationError. If the given list is null, this method does nothing.
     * 
     * @param errors
     */
    public void addErrors(List<ValidationError> errors)
    {
        if (errors != null)
        {
            for (ValidationError validationError : errors)
            {
                addError(validationError);
            }
        }
    }

    /**
     * @return the list of validation errors
     */
    public List<ValidationError> getErrorsList()
    {
        return this.lErrors;
    }

    /**
     * This Class represents an error of validation. It contains an error code and an error explanation.
     */
    public static class ValidationError
    {
        /**
         * Error identifier. This error code can be used as identifier to internationalize the logging message using
         * i18n.
         */
        private String errorCode;

        /**
         * Error details
         */
        private String details;

        /**
         * false: this error can't be ignored; true: this error can be ignored
         */
        private boolean isWarning = false;

        // TODO Add here COSObject or the PDObject that is linked to the error may a automatic fix can be done.

        /**
         * Always record the place in the source code where the ValidationError
         * was created, in case the ValidationError was not caused by a
         * Throwable.
         */
        private Throwable t = null;

        /**
         * Get the place where the ValidationError was created, useful if the
         * ValidationError was not caused by a Throwable.
         *
         * @return The place where the ValidationError was created.
         */
        public Throwable getThrowable()
        {
            return t;
        }

        /**
         * The underlying cause if the ValidationError was caused by a Throwable.
         */
        private Throwable cause = null;

        /**
         * Get the underlying cause if the ValidationError was caused by a
         * Throwable.
         *
         * @return The underlying cause if the ValidationError was caused by a
         * Throwable, or null if not.
         */
        public Throwable getCause()
        {
            return cause;
        }

        /**
         * Create a validation error with the given error code
         * 
         * @param errorCode
         */
        public ValidationError(String errorCode)
        {
            this.errorCode = errorCode;
            if (errorCode.startsWith(PreflightConstants.ERROR_SYNTAX_COMMON))
            {
                this.details = "Syntax error";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_SYNTAX_HEADER))
            {
                this.details = "Header Syntax error";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_SYNTAX_BODY))
            {
                this.details = "Body Syntax error";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_SYNTAX_CROSS_REF))
            {
                this.details = "CrossRef Syntax error";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_SYNTAX_TRAILER))
            {
                this.details = "Trailer Syntax error";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_GRAPHIC_INVALID))
            {
                this.details = "Invalid graphics object";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_GRAPHIC_TRANSPARENCY))
            {
                this.details = "Invalid graphics transparency";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_GRAPHIC_UNEXPECTED_KEY))
            {
                this.details = "Unexpected key in Graphic object definition";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_GRAPHIC_INVALID_COLOR_SPACE))
            {
                this.details = "Invalid Color space";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_FONTS_INVALID_DATA))
            {
                this.details = "Invalid Font definition";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_FONTS_DAMAGED))
            {
                this.details = "Font damaged";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_FONTS_GLYPH))
            {
                this.details = "Glyph error";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_TRANSPARENCY_EXT_GRAPHICAL_STATE))
            {
                this.details = "Transparency error";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_ANNOT_MISSING_FIELDS))
            {
                this.details = "Missing field in an annotation definition";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_ANNOT_FORBIDDEN_ELEMENT))
            {
                this.details = "Forbidden field in an annotation definition";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_ANNOT_INVALID_ELEMENT))
            {
                this.details = "Invalid field value in an annotation definition";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_ACTION_INVALID_ACTIONS))
            {
                this.details = "Invalid action definition";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_ACTION_FORBIDDEN_ACTIONS))
            {
                this.details = "Action is forbidden";
            }
            else if (errorCode.startsWith(PreflightConstants.ERROR_METADATA_MAIN))
            {
                this.details = "Error on MetaData";
            }
            else
            {
                // default Unkown error
                this.details = "Unknown error";
            }
            t = new Exception();
        }

        /**
         * Create a validation error with the given error code and the error
         * explanation.
         *
         * @param errorCode the error code
         * @param details the error explanation
         * @param cause the error cause
         */
        public ValidationError(String errorCode, String details, Throwable cause)
        {
            this(errorCode);
            if (details != null)
            {
                StringBuilder sb = new StringBuilder(this.details.length() + details.length() + 2);
                sb.append(this.details).append(", ").append(details);
                this.details = sb.toString();
            }
            this.cause = cause;
            t = new Exception();
        }

        /**
         * Create a validation error with the given error code and the error
         * explanation.
         *
         * @param errorCode the error code
         * @param details the error explanation
         */
        public ValidationError(String errorCode, String details)
        {
            this(errorCode, details, null);
        }        

        /**
         * @return the error code
         */
        public String getErrorCode()
        {
            return errorCode;
        }

        /**
         * @return the error explanation
         */
        public String getDetails()
        {
            return details;
        }

        /**
         * Set the error explanation
         * 
         * @param details
         */
        public void setDetails(String details)
        {
            this.details = details;
        }

        public boolean isWarning()
        {
            return isWarning;
        }

        public void setWarning(boolean isWarning)
        {
            this.isWarning = isWarning;
        }

        @Override
        public int hashCode() {
            return errorCode.hashCode();
        }

        @Override
        public boolean equals (Object o) {
            if (o instanceof ValidationError) {
                ValidationError ve = (ValidationError)o;
                // check errorCode
                if (errorCode==null && ve.errorCode!=null) {
                    return false;
                } else if (!errorCode.equals(ve.errorCode)) {
                    return false;
                }  else if (!details.equals(ve.details)) {
                    return false;
                }
                // check warning
                return isWarning==ve.isWarning;
                
            } else {
                return false;
            }
        }

    }
}
