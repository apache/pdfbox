package org.apache.pdfbox.preflight.integration;

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

import javax.activation.FileDataSource;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public abstract class AbstractInvalidFileTester
{

    /**
     * where result information are pushed
     */
    protected OutputStream outputResult = null;

    /**
     * carry the expected error with the current test
     */
    protected final String expectedError;

    /**
     * carry the path of the file validated during current test
     */
    protected File path;

    protected static Logger staticLogger = Logger.getLogger("Test");

    protected Logger logger = null;

    /**
     * Prepare the test for one file
     * 
     * @param path
     *            pdf/a file to test
     * @param error
     *            expected error for this test
     */
    public AbstractInvalidFileTester(File path, String error)
    {
        this.path = path;
        this.expectedError = error;
        this.logger = Logger.getLogger(this.getClass());
    }

    @Test()
    public final void validate() throws Exception
    {
        if (path == null)
        {
            logger.warn("This is an empty test");
            return;
        }
        PreflightDocument document = null;
        try
        {
            FileDataSource bds = new FileDataSource(path);
            PreflightParser parser = new PreflightParser(bds);
            parser.parse();
            document = parser.getPreflightDocument();
            document.validate();

            ValidationResult result = document.getResult();
            Assert.assertFalse(path + " : Isartor file should be invalid (" + path + ")", result.isValid());
            Assert.assertTrue(path + " : Should find at least one error", result.getErrorsList().size() > 0);
            // could contain more than one error
            boolean found = false;
            if (this.expectedError != null)
            {
                for (ValidationError error : result.getErrorsList())
                {
                    if (error.getErrorCode().equals(this.expectedError))
                    {
                        found = true;
                        if (outputResult == null)
                        {
                            break;
                        }
                    }
                    if (outputResult != null)
                    {
                        String log = path.getName().replace(".pdf", "") + "#" + error.getErrorCode() + "#"
                                + error.getDetails() + "\n";
                        outputResult.write(log.getBytes());
                    }
                }
            }

            if (result.getErrorsList().size() > 0)
            {
                if (this.expectedError == null)
                {
                    logger.info("File invalid as expected (no expected code) :" + this.path.getAbsolutePath());
                }
                else if (!found)
                {
                    StringBuilder message = new StringBuilder(100);
                    message.append(path).append(" : Invalid error code returned. Expected ");
                    message.append(this.expectedError).append(", found ");
                    for (ValidationError error : result.getErrorsList())
                    {
                        message.append(error.getErrorCode()).append(" ");
                    }
                    Assert.fail(message.toString());
                }
            }
            else
            {
                Assert.assertEquals(path + " : Invalid error code returned.", this.expectedError, result
                        .getErrorsList().get(0).getErrorCode());
            }
        }
        catch (ValidationException e)
        {
            throw new Exception(path + " :" + e.getMessage(), e);
        }
        finally
        {
            if (document != null)
            {
                document.close();
            }
        }
    }

    protected abstract String getResultFileKey();

    @Before
    public void before() throws Exception
    {
        String irp = System.getProperty(getResultFileKey());

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

    @After
    public void after() throws Exception
    {
        IOUtils.closeQuietly(outputResult);
    }

}
