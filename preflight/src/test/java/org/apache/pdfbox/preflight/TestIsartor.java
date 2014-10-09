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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.apache.pdfbox.preflight.utils.ByteArrayDataSource;
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
    private static final String FILTER_FILE = "isartor.filter";
    private static FileOutputStream isartorResultFile;
    private static final Log logger = LogFactory.getLog(TestIsartor.class);

    @Parameters(name = "{0}")
    public static Collection<Object[]> initializeParameters() throws Exception
    {
        String filter = System.getProperty(FILTER_FILE);

        // load expected errors
        File f = new File("src/test/resources/expected_errors.txt");
        InputStream expected = new FileInputStream(f);
        Properties props = new Properties();
        props.load(expected);
        IOUtils.closeQuietly(expected);

        // prepare config
        List<Object[]> data = new ArrayList<Object[]>();

        File isartor = new File("target/pdfs/Isartor testsuite/PDFA-1b");
        if (isartor.isDirectory())
        {
            Collection<?> pdfFiles = FileUtils.listFiles(isartor, new String[] {"pdf","PDF"}, true);
            for (Object  pdfFile : pdfFiles)
            {
                String fn = ((File)pdfFile).getName();
                if (filter == null || fn.contains(filter))
                {
                    String path = props.getProperty(fn);
                    String error = new StringTokenizer(path, "//").nextToken().trim();
                    data.add(new Object[] { (File)pdfFile, error });
                }
            }
        }
        else
        {
            fail("Isartor data set has not been downloaded! Try running Maven?");
        }
        return data;
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

    private final String expectedError;
    private final File file;

    public TestIsartor(File path, String error)
    {
        System.out.println("  " + path.getName());
        this.file = path;
        this.expectedError = error;
    }

    @Test()
    public void validate() throws Exception
    {
        PreflightDocument document = null;
        try
        {
            InputStream input = new FileInputStream(file);
            ValidationResult result;
            try
            {
                PreflightParser parser = new PreflightParser(new ByteArrayDataSource(input));
                parser.parse();
                document = (PreflightDocument) parser.getPDDocument();
                // to speeds up tests, skip validation of page count is over the limit
                if (document.getNumberOfPages() < 8191)
                {
                    document.validate();
                }
                result = document.getResult();
            }
            catch (SyntaxValidationException e)
            {
                result = e.getResult();
            }

            assertFalse(file.getName() + " : Isartor file should be invalid (expected " +
                    this.expectedError + ")", result.isValid());

            assertTrue(file.getName() + " : Should find at least one error",
                    result.getErrorsList().size() > 0);

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
                    String log = file.getName().replace(".pdf", "") + "#" + error.getErrorCode() +
                            "#" + error.getDetails() + "\n";
                    isartorResultFile.write(log.getBytes());
                }
            }

            if (result.getErrorsList().size() > 1)
            {
                if (!found)
                {
                    StringBuilder message = new StringBuilder(100);
                    for (ValidationError error : result.getErrorsList())
                    {
                        message.append(error.getErrorCode()).append(" ");
                    }
                    fail(String.format("%s : Invalid error code returned. Expected %s, found [%s]",
                            file.getName(), expectedError, message.toString().trim()));
                }
                // if one of the error code of the list is the expected one, we consider test valid
            }
            else
            {
                assertEquals(file.getName() + " : Invalid error code returned.", this.expectedError,
                        result.getErrorsList().get(0).getErrorCode());
            }
        }
        catch (Exception e)
        {
            fail(String.format("%s : %s raised , message=%s", file.getName(),
                    e.getClass().getSimpleName(), e.getMessage()));
        }
        finally
        {
            if (document != null)
            {
                document.close();
            }
        }
    }
}
