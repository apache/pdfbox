package org.apache.pdfbox.preflight.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.parser.PreflightParser;

public class InvalidFileTester
{
    private static final Log LOG = LogFactory.getLog(InvalidFileTester.class);

    /**
     * where result information is pushed
     */
    protected OutputStream outputResult = null;

    /**
     * carry the path of the file validated during current test
     */
    protected File path;

    /**
     * Prepare the test for one file
     * 
     * @param resultKeyFile the result key property file
     * @throws Exception
     */
    public InvalidFileTester(String resultKeyFile) throws Exception
    {
        before(resultKeyFile);
    }

    public final void validate(File path, String expectedError) throws Exception
    {
        if (path == null)
        {
            LOG.warn("This is an empty test");
            return;
        }
        ValidationResult result = PreflightParser.validate(path);
        assertFalse(result.isValid(), path + " : Isartor file should be invalid (" + path + ")");
        assertTrue(result.getErrorsList().size() > 0, path + " : Should find at least one error");
        // could contain more than one error
        boolean found = false;
        if (expectedError != null)
        {
            for (ValidationError error : result.getErrorsList())
            {
                if (error.getErrorCode().equals(expectedError))
                {
                    found = true;
                    if (outputResult == null)
                    {
                        break;
                    }
                }
                if (outputResult != null)
                {
                    String log = path.getName().replace(".pdf", "") + "#" + error.getErrorCode()
                            + "#" + error.getDetails() + "\n";
                    outputResult.write(log.getBytes());
                }
            }
        }

        if (result.getErrorsList().size() > 0)
        {
            if (expectedError == null)
            {
                LOG.info("File invalid as expected (no expected code) :"
                        + this.path.getAbsolutePath());
            }
            else if (!found)
            {
                StringBuilder message = new StringBuilder(100);
                message.append(path).append(" : Invalid error code returned. Expected ");
                message.append(expectedError).append(", found ");
                for (ValidationError error : result.getErrorsList())
                {
                    message.append(error.getErrorCode()).append(" ");
                }
                fail(message.toString());
            }
        }
        else
        {
            assertEquals(path + " : Invalid error code returned.", expectedError,
                    result.getErrorsList().get(0).getErrorCode());
        }
    }

    public void before(String resultKeyFile) throws Exception
    {
        String irp = System.getProperty(resultKeyFile);

        if (irp == null)
        {
            // no log file defined, use system.err
            outputResult = System.err;
        }
        else
        {
            outputResult = new FileOutputStream(irp);
        }
    }

    public void after() throws Exception
    {
        IOUtils.closeQuietly(outputResult);
    }

}
