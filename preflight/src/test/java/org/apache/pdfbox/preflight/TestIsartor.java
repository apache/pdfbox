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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class TestIsartor
{

    protected static FileOutputStream isartorResultFile = null;

    protected String expectedError;

    protected File path;

    protected static Logger logger = Logger.getLogger(TestIsartor.class);

    public TestIsartor(File path, String error)
    {
        this.path = path;
        this.expectedError = error;
    }

    @BeforeClass
    public static void beforeClass() throws Exception
    {

        String irp = System.getProperty("isartor.results.path");
        if (irp != null)
        {
            File f = new File(irp);
            if (f.exists() && f.isFile())
            {
                f.delete();
                isartorResultFile = new FileOutputStream(f);
            }
            else if (!f.exists())
            {
                isartorResultFile = new FileOutputStream(f);
            }
            else
            {
                throw new IllegalArgumentException("Invalid result file : " + irp);
            }
        }
    }

    @AfterClass
    public static void afterClass() throws Exception
    {
        if (isartorResultFile != null)
        {
            IOUtils.closeQuietly(isartorResultFile);
        }
    }

    @Test()
    public void validate() throws Exception
    {
        PreflightDocument document = null;
        try
        {
            System.out.println(path);
            InputStream input = new FileInputStream(path);

            ValidationResult result = null;
            try
            {
                PreflightParser parser = new PreflightParser(new org.apache.pdfbox.preflight.utils.ByteArrayDataSource(
                        input));
                parser.parse();
                document = (PreflightDocument) parser.getPDDocument();
                document.validate();
                result = document.getResult();
            }
            catch (SyntaxValidationException e)
            {
                result = e.getResult();
            }

            assertFalse(path + " : Isartor file should be invalid (" + path + ")", result.isValid());
            assertTrue(path + " : Should find at least one error", result.getErrorsList().size() > 0);

            // could contain more than one error
            boolean found = false;
            for (ValidationError error : result.getErrorsList())
            {
                if (error.getErrorCode().equals(this.expectedError))
                {
                    found = true;
                    if (isartorResultFile == null)
                    {
                        break;
                    }
                }
                if (isartorResultFile != null)
                {
                    String log = path.getName().replace(".pdf", "") + "#" + error.getErrorCode() + "#" + error.getDetails()
                            + "\n";
                    isartorResultFile.write(log.getBytes());
                }
            }

            if (result.getErrorsList().size() > 1)
            {
                if (!found)
                {
                    StringBuilder message = new StringBuilder(100);
                    message.append(path).append(" : Invalid error code returned. Expected ");
                    message.append(this.expectedError).append(", found ");
                    for (ValidationError error : result.getErrorsList())
                    {
                        message.append(error.getErrorCode()).append(" ");
                    }
                    fail(message.toString());
                }
            }
            else
            {
                assertEquals(path + " : Invalid error code returned.", this.expectedError, result
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
                document.close();
        }
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> initializeParameters() throws Exception
    {
        // load expected errors
        File f = new File("src/test/resources/expected_errors.txt");
        InputStream expected = new FileInputStream(f);
        Properties props = new Properties();
        props.load(expected);
        IOUtils.closeQuietly(expected);
        // prepare config
        List<Object[]> data = new ArrayList<Object[]>();

        File isartor = new File("target/pdfs/Isartor testsuite/PDFA-1b");
            if (isartor.isDirectory()) {
            Collection<?> pdfFiles = FileUtils.listFiles(isartor,new String[] {"pdf","PDF"},true);
            for (Object  pdfFile : pdfFiles) {
                String fn = ((File)pdfFile).getName();
                String error = new StringTokenizer(props.getProperty(fn), "//").nextToken().trim();
                Object [] tmp = new Object [] {(File)pdfFile,error};
                data.add(tmp);
            }
        } else {
            logger.warn("Isartor data set not present, skipping Isartor validation");
        }
        return data;
    }
}
