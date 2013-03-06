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
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestInvalidFiles extends AbstractInvalidFileTester
{

    private static final String RESULTS_FILE = "results.file";

    private static final String EXPECTED_ERRORS = "invalid.errors";

    private static final String ISARTOR_FILES = "invalid.files";

    protected static Logger staticLogger = Logger.getLogger("Test");

    public TestInvalidFiles(File path, String error)
    {
        super(path, error);
    }

    protected static Collection<Object[]> stopIfExpected() throws Exception
    {
        List<Object[]> ret = new ArrayList<Object[]>();
        ret.add(new Object[] { null, null });
        return ret;
    }

    @Parameters
    public static Collection<Object[]> initializeParameters() throws Exception
    {
        // find isartor files
        String isartor = System.getProperty(ISARTOR_FILES);
        if (isartor == null)
        {
            staticLogger.warn(ISARTOR_FILES + " (where are isartor pdf files) is not defined.");
            return stopIfExpected();
        }
        File root = new File(isartor);
        // load expected errors
        Properties props = new Properties();
        String expectedPath = System.getProperty(EXPECTED_ERRORS);
        if (expectedPath == null)
        {
            staticLogger.warn(EXPECTED_ERRORS + " not defined, only check if file is invalid");
        }
        else
        {
            File expectedFile = new File(expectedPath);
            if (!expectedFile.exists() || !expectedFile.isFile())
            {
                staticLogger.warn("'expected.errors' does not reference valid file, so cannot execute tests : "
                        + expectedFile.getAbsolutePath());
                return stopIfExpected();
            }
            InputStream expected = new FileInputStream(expectedPath);
            props.load(expected);
            IOUtils.closeQuietly(expected);
        }
        // prepare config
        List<Object[]> data = new ArrayList<Object[]>();
        Collection<?> files = FileUtils.listFiles(root, new String[] { "pdf" }, true);

        for (Object object : files)
        {
            File file = (File) object;
            String fn = file.getName();
            if (props.getProperty(fn) != null)
            {
                String error = new StringTokenizer(props.getProperty(fn), "//").nextToken().trim();
                Object[] tmp = new Object[] { file, error };
                data.add(tmp);
            }
            else
            {
                // no expected error
                Object[] tmp = new Object[] { file, null };
                data.add(tmp);
            }

        }
        return data;
    }

    @Override
    protected String getResultFileKey()
    {
        return RESULTS_FILE;
    }

}
