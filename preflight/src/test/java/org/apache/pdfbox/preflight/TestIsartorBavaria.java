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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class TestIsartorBavaria
{
    private static final String FILTER_FILE = "isartor.filter";
    private static final String SKIP_BAVARIA = "skip-bavaria";
    private static FileOutputStream isartorResultFile;

    public static Collection<Object[]> initializeParameters() throws Exception
    {
        String filter = System.getProperty(FILTER_FILE);
        String skipBavaria = System.getProperty(SKIP_BAVARIA);

        // load expected errors
        File f = new File("src/test/resources/expected_errors.txt");
        InputStream expected = new FileInputStream(f);
        Properties props = new Properties();
        props.load(expected);
        IOUtils.closeQuietly(expected);

        // prepare config
        List<Object[]> data = new ArrayList<>();

        File isartor = new File("target/pdfs/Isartor testsuite/PDFA-1b");
        if (isartor.isDirectory())
        {
            Collection<?> pdfFiles = FileUtils.listFiles(isartor, new String[] {"pdf","PDF"}, true);
            for (Object pdfFile : pdfFiles)
            {
                String fn = ((File)pdfFile).getName();
                if (filter == null || fn.contains(filter))
                {
                    String path = props.getProperty(fn);
                    String error = new StringTokenizer(path, "//").nextToken().trim();
                    String[] errTab = error.split(",");
                    Set<String> errorSet = new HashSet<>(Arrays.asList(errTab));
                    data.add(new Object[] { pdfFile, errorSet } );
                }
            }
        }
        else
        {
            fail("Isartor data set has not been downloaded! Try running Maven?");
        }
        
        if ("false".equals(skipBavaria))
        {
            File bavaria = new File("target/pdfs/Bavaria testsuite");
            if (bavaria.isDirectory())
            {
                Collection<?> pdfFiles = FileUtils.listFiles(bavaria, new String[] {"pdf","PDF"}, true);
                for (Object pdfFile : pdfFiles)
                {
                    String fn = ((File) pdfFile).getName();
                    if (filter == null || fn.contains(filter))
                    {
                        String path = props.getProperty(fn);
                        Set<String> errorSet = new HashSet<>();
                        if (!path.isEmpty())
                        {
                            String error = new StringTokenizer(path, "//").nextToken().trim();
                            errorSet.addAll(Arrays.asList(error.split(",")));
                        }
                        data.add(new Object[] { pdfFile, errorSet } );
                    }
                }
            }
            else
            {
                fail("Bavaria data set has not been downloaded! Try running Maven?");
            }
        }
        else
        {
            System.out.println("Bavaria tests are skipped. You can enable them in Maven with -Dskip-bavaria=false");
            System.out.println("About the tests: http://www.pdflib.com/knowledge-base/pdfa/validation-report/");
        }
        return data;
    }

    @BeforeAll
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

    @AfterAll
    public static void afterClass() throws Exception
    {
        if (isartorResultFile != null)
        {
            IOUtils.closeQuietly(isartorResultFile);
        }
    }

    @ParameterizedTest
	@MethodSource("initializeParameters")
    void validate(File file, Set<String> expectedErrorSet) throws Exception
    {
        ValidationResult result = PreflightParser.validate(file);
        if (result != null)
        {
            if (expectedErrorSet.isEmpty())
            {
                Set<String> errorSet = new HashSet<>();
                for (ValidationError error : result.getErrorsList())
                {
                    errorSet.add(error.getErrorCode());
                }
                StringBuilder message = new StringBuilder();
                message.append(file.getName());
                message.append(" : PDF/A file should be valid, but has error");
                if (errorSet.size() > 1)
                {
                    message.append('s');
                }
                message.append(':');
                for (String errMsg : errorSet)
                {
                    message.append(' ');
                    message.append(errMsg);
                }
                assertTrue(result.isValid(), message.toString());
                assertTrue(result.getErrorsList().isEmpty(), message.toString());
            }
            else
            {
                assertFalse(result.isValid(), file.getName() + " : PDF/A file should be invalid (expected "
                    + expectedErrorSet + ")"); // TODO

                assertTrue(result.getErrorsList().size() > 0, file.getName() + " : Should find at least one error");

                // each expected error should occur
                boolean logged = false;
                boolean allFound = true;
                for (String expectedError : expectedErrorSet)
                {
                    boolean oneFound = false;
                    for (ValidationError error : result.getErrorsList())
                    {
                        if (error.getErrorCode().equals(expectedError))
                        {
                            oneFound = true;
                        }
                        if (isartorResultFile != null && !logged)
                        {
                            String log = file.getName().replace(".pdf", "") + "#"
                                    + error.getErrorCode() + "#" + error.getDetails() + "\n";
                            isartorResultFile.write(log.getBytes());
                        }
                    }
                    if (!oneFound)
                    {
                        allFound = false;
                        break;
                    }

                    // log only in the first inner loop
                    logged = true;
                }
                if (!allFound)
                {
                    Set<String> errorSet = new HashSet<>();
                    for (ValidationError error : result.getErrorsList())
                    {
                        errorSet.add(error.getErrorCode());
                    }
                    StringBuilder message = new StringBuilder();
                    for (String errMsg : errorSet)
                    {
                        if (message.length() > 0)
                        {
                            message.append(", ");
                        }
                        message.append(errMsg);
                    }
                    fail(String.format("%s : Invalid error code returned. Expected %s, found [%s]",
                            file.getName(), expectedErrorSet, message.toString().trim()));
                }
                // if each of the expected errors are found, we consider test valid
            }
        }
    }
}
