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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
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

@RunWith(Parameterized.class)
public class TestIsartorValidationFromClasspath
{

    protected static FileOutputStream isartorResultFile = null;

    protected String expectedError;

    protected String path;

    public TestIsartorValidationFromClasspath(String path, String error)
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
            InputStream input = this.getClass().getResourceAsStream(path);

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

            Assert.assertFalse(path + " : Isartor file should be invalid (" + path + ")", result.isValid());
            Assert.assertTrue(path + " : Should find at least one error", result.getErrorsList().size() > 0);

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
                    String log = path.replace(".pdf", "") + "#" + error.getErrorCode() + "#" + error.getDetails()
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
                document.close();
        }
    }

    @Parameters
    public static Collection<Object[]> initializeParameters() throws Exception
    {
        // load expected errors
        File f = new File("src/test/resources/expected_errors.txt");
        System.out.println(f.exists());
        InputStream expected = new FileInputStream(f);
        Properties props = new Properties();
        props.load(expected);
        IOUtils.closeQuietly(expected);
        // prepare config
        List<Object[]> data = new ArrayList<Object[]>();
        InputStream is = Class.class.getResourceAsStream("/Isartor testsuite.list");
        if (is != null)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while (line != null)
            {
                String fn = new File(line).getName();
                String error = new StringTokenizer(props.getProperty(fn), "//").nextToken().trim();
                Object[] tmp = new Object[] { "/" + line, error };
                data.add(tmp);
                line = reader.readLine();
            }
        }
        else
        {
            System.out.println("TestIsartorValidationFromClasspath2.initializeParameters(): No input files found");
        }
        return data;
    }
}
