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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestIsartorValidation
{

    private static final String RESULTS_FILE = "results.file";

    private static final String EXPECTED_ERRORS = "isartor.errors";

    private static final String ISARTOR_FILES = "isartor.files";

    protected static final Log LOG = LogFactory.getLog(TestIsartorValidation.class);

    private static InvalidFileTester tester;

    @BeforeAll
    static void setup() throws Exception
    {
        tester = new InvalidFileTester(RESULTS_FILE);
    }

    @AfterAll
    static void closeDown() throws Exception
    {
        tester.after();
    }

    @ParameterizedTest
	@MethodSource("initializeParameters")
    void validate(File path, String expectedError) throws Exception
    {
        tester.validate(path, expectedError);
    }

    protected static Collection<Arguments> stopIfExpected() throws Exception
    {
        List<Arguments> ret = new ArrayList<>();
        ret.add(Arguments.of(null, null));
        return ret;
    }

    public static Collection<Arguments> initializeParameters() throws Exception
    {
        // find isartor files
        String isartor = System.getProperty(ISARTOR_FILES);
        if (isartor == null)
        {
            LOG.warn(ISARTOR_FILES + " (where are isartor pdf files) is not defined.");
            return stopIfExpected();
        }
        File root = new File(isartor);
        // load expected errors
        String expectedPath = System.getProperty(EXPECTED_ERRORS);
        if (expectedPath == null)
        {
            LOG.warn("'expected.errors' not defined, so cannot execute tests");
            return stopIfExpected();
        }
        File expectedFile = new File(expectedPath);
        if (!expectedFile.exists() || !expectedFile.isFile())
        {
            LOG.warn("'expected.errors' does not reference valid file, so cannot execute tests : "
                    + expectedFile.getAbsolutePath());
            return stopIfExpected();
        }
        InputStream expected = new FileInputStream(expectedPath);
        Properties props = new Properties();
        props.load(expected);
        IOUtils.closeQuietly(expected);
        // prepare config
        List<Arguments> data = new ArrayList<>();
        Collection<?> files = FileUtils.listFiles(root, new String[] { "pdf" }, true);

        for (Object object : files)
        {
            File file = (File) object;
            String fn = file.getName();
            if (props.getProperty(fn) != null)
            {
                String expectedError = new StringTokenizer(props.getProperty(fn), "//").nextToken().trim();
                data.add(Arguments.of(file, expectedError));
            }
            else
            {
                System.err.println("No expected result for this file, will not try to validate : "
                        + file.getAbsolutePath());
            }

        }
        return data;
    }
}
