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

package org.apache.pdfbox.preflight.process;

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_STREAM_DAMAGED;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_STREAM_FX_KEYS;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_STREAM_INVALID_FILTER;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_STREAM_LENGTH_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_STREAM_LENGTH_MISSING;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.utils.COSUtils;
import org.apache.pdfbox.preflight.utils.FilterHelper;
import org.apache.pdfbox.util.Charsets;

public class StreamValidationProcess extends AbstractProcess
{

    @Override
    public void validate(PreflightContext ctx) throws ValidationException
    {
        PDDocument pdfDoc = ctx.getDocument();
        COSDocument cDoc = pdfDoc.getDocument();

        List<?> lCOSObj = cDoc.getObjects();
        for (Object o : lCOSObj)
        {
            COSObject cObj = (COSObject) o;
            
            // If this object represents a Stream, the Dictionary must contain the Length key
            COSBase cBase = cObj.getObject();
            if (cBase instanceof COSStream)
            {
                validateStreamObject(ctx, cObj);
            }
        }
    }

    public void validateStreamObject(PreflightContext context, COSObject cObj) throws ValidationException
    {
        COSStream streamObj = (COSStream) cObj.getObject();

        // ---- Check dictionary entries
        // ---- Only the Length entry is mandatory
        // ---- In a PDF/A file, F, FFilter and FDecodeParms are forbidden
        checkDictionaryEntries(context, streamObj);
        // ---- check stream length
        checkStreamLength(context, cObj);
        // ---- Check the Filter value(s)
        checkFilters(streamObj, context);
    }

    /**
     * This method checks if one of declared Filter is LZWdecode. If LZW is found, the result list is updated with an
     * error code.
     * 
     * @param stream the stream to check.
     * @param context the preflight context.
     */
    protected void checkFilters(COSStream stream, PreflightContext context)
    {
        COSBase bFilter = stream.getDictionaryObject(COSName.FILTER);
        if (bFilter != null)
        {
            COSDocument cosDocument = context.getDocument().getDocument();
            if (COSUtils.isArray(bFilter, cosDocument))
            {
                COSArray afName = (COSArray) bFilter;
                for (int i = 0; i < afName.size(); ++i)
                {
                    FilterHelper.isAuthorizedFilter(context, afName.getString(i));
                }
            }
            else if (bFilter instanceof COSName)
            {
                String fName = ((COSName) bFilter).getName();
                FilterHelper.isAuthorizedFilter(context, fName);
            }
            else
            {
                // ---- The filter type is invalid
                addValidationError(context, new ValidationError(ERROR_SYNTAX_STREAM_INVALID_FILTER,
                        "Filter should be a Name or an Array"));
            }
        }
        // else Filter entry is optional
    }

    private boolean readUntilStream(InputStream ra) throws IOException
    {
        boolean search = true;
        boolean maybe = false;
        int lastChar = -1;
        do
        {
            int c = ra.read();
            switch (c)
            {
            case 's':
                maybe = true;
                lastChar = c;
                break;
            case 't':
                if (maybe && lastChar == 's')
                {
                    lastChar = c;
                }
                else
                {
                    maybe = false;
                    lastChar = -1;
                }
                break;
            case 'r':
                if (maybe && lastChar == 't')
                {
                    lastChar = c;
                }
                else
                {
                    maybe = false;
                    lastChar = -1;
                }
                break;
            case 'e':
                if (maybe && lastChar == 'r')
                {
                    lastChar = c;
                }
                else
                {
                    maybe = false;
                }
                break;
            case 'a':
                if (maybe && lastChar == 'e')
                {
                    lastChar = c;
                }
                else
                {
                    maybe = false;
                }
                break;
            case 'm':
                if (maybe && lastChar == 'a')
                {
                    return true;
                }
                else
                {
                    maybe = false;
                }
                break;
            case -1:
                search = false;
                break;
            default:
                maybe = false;
                break;
            }
        } while (search);
        return false;
    }

    protected void checkStreamLength(PreflightContext context, COSObject cObj) throws ValidationException
    {
        COSStream streamObj = (COSStream) cObj.getObject();
        int length = streamObj.getInt(COSName.LENGTH);
        InputStream ra = null;
        try
        {
            ra = context.getSource().getInputStream();
            Long offset = context.getDocument().getDocument().getXrefTable().get(new COSObjectKey(cObj));

            // ---- go to the beginning of the object
            long skipped = 0;
            if (offset != null)
            {
                while (skipped != offset)
                {
                    long curSkip = ra.skip(offset - skipped);
                    if (curSkip < 0)
                    {
                        addValidationError(context, new ValidationError(ERROR_SYNTAX_STREAM_DAMAGED, "Unable to skip bytes in the PDFFile to check stream length"));
                        return;
                    }
                    skipped += curSkip;
                }

                // ---- go to the stream key word
                if (readUntilStream(ra))
                {
                    int c = ra.read();
                    if (c == '\r')
                    {
                        ra.read();
                    }
                    // else c is '\n' no more character to read

                    // ---- Here is the true beginning of the Stream Content.
                    // ---- Read the given length of bytes and check the 10 next bytes
                    // ---- to see if there are endstream.
                    byte[] buffer = new byte[1024];
                    int nbBytesToRead = length;

                    do
                    {
                        int cr;
                        if (nbBytesToRead > 1024)
                        {
                            cr = ra.read(buffer, 0, 1024);
                        }
                        else
                        {
                            cr = ra.read(buffer, 0, nbBytesToRead);
                        }
                        if (cr == -1)
                        {
                            addStreamLengthValidationError(context, cObj, length, "");
                            return;
                        }
                        else
                        {
                            nbBytesToRead -= cr;
                        }
                    }
                    while (nbBytesToRead > 0);

                    int len = "endstream".length() + 2;
                    byte[] buffer2 = new byte[len];
                    for (int i = 0; i < len; ++i)
                    {
                        buffer2[i] = (byte) ra.read();
                    }

                    // ---- check the content of 10 last characters
                    String endStream = new String(buffer2, Charsets.ISO_8859_1);
                    if (buffer2[0] == '\r' && buffer2[1] == '\n')
                    {
                        if (!endStream.contains("endstream"))
                        {
                            addStreamLengthValidationError(context, cObj, length, endStream);
                        }
                    }
                    else if (buffer2[0] == '\r' && buffer2[1] == 'e')
                    {
                        if (!endStream.contains("endstream"))
                        {
                            addStreamLengthValidationError(context, cObj, length, endStream);
                        }
                    }
                    else if (buffer2[0] == '\n' && buffer2[1] == 'e')
                    {
                        if (!endStream.contains("endstream"))
                        {
                            addStreamLengthValidationError(context, cObj, length, endStream);
                        }
                    }
                    else
                    {
                        if (!endStream.startsWith("endStream"))
                        {
                             addStreamLengthValidationError(context, cObj, length, endStream);
                        }
                    }
                }
                else
                {
                    addStreamLengthValidationError(context, cObj, length, "");
                }
            }
        }
        catch (IOException e)
        {
            throw new ValidationException("Unable to read a stream to validate: " + e.getMessage(), e);
        }
        finally
        {
            IOUtils.closeQuietly(ra);
        }
    }

    /**
     * Check dictionary entries. Only the Length entry is mandatory. In a PDF/A file, F, FFilter and FDecodeParms are
     * forbidden
     * 
     * @param context the preflight context.
     * @param streamObj the stream to check.
     */
    protected void checkDictionaryEntries(PreflightContext context, COSStream streamObj)
    {
        boolean len = streamObj.containsKey(COSName.LENGTH);
        boolean f = streamObj.containsKey(COSName.F);
        boolean ffilter = streamObj.containsKey(COSName.F_FILTER);
        boolean fdecParams = streamObj.containsKey(COSName.F_DECODE_PARMS);

        if (!len)
        {
            addValidationError(context, new ValidationError(ERROR_SYNTAX_STREAM_LENGTH_MISSING,
                    "Stream length is missing"));
        }

        if (f || ffilter || fdecParams)
        {
            addValidationError(context, new ValidationError(ERROR_SYNTAX_STREAM_FX_KEYS,
                    "F, FFilter or FDecodeParms keys are present in the stream dictionary"));
        }
    }
    
    private void addStreamLengthValidationError(PreflightContext context, COSObject cObj, int length, String endStream)
    {
        addValidationError(context, new ValidationError(ERROR_SYNTAX_STREAM_LENGTH_INVALID,
                "Stream length is invalid [cObj=" + cObj + "; defined length=" + length + "; buffer2=" + endStream + "]"));
    }

}
