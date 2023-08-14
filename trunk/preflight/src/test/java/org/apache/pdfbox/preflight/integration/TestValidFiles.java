package org.apache.pdfbox.preflight.integration;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class TestValidFiles
{

    private static final String RESULTS_FILE = "results.file";

    private static final String ISARTOR_FILES = "valid.files";

    protected static OutputStream isartorResultFile = null;

    protected File path;

    protected static final Log LOG = LogFactory.getLog(TestValidFiles.class);

    protected Log logger = null;

    protected static Collection<File> stopIfExpected() throws Exception
    {
        // throw new Exception("Test badly configured");
        List<File> ret = new ArrayList<>();
        ret.add(null);
        return ret;
    }

    public static Collection<File> initializeParameters() throws Exception
    {
        // find isartor files
        String isartor = System.getProperty(ISARTOR_FILES);
        if (isartor == null || isartor.isEmpty())
        {
            LOG.warn(ISARTOR_FILES + " (where are isartor pdf files) is not defined.");
            return stopIfExpected();
        }
        File root = new File(isartor);
        // load expected errors
        // prepare config
        List<File> data = new ArrayList<>();
        Collection<?> files = FileUtils.listFiles(root, new String[] { "pdf" }, true);

        for (Object object : files)
        {
            File file = (File) object;
            data.add(file);
        }
        return data;
    }

    @BeforeAll
    public static void beforeClass() throws Exception
    {
        String irp = System.getProperty(RESULTS_FILE);
        if (irp == null)
        {
            // no log file defined, use system.err
            System.err.println("No result file defined, will use standard error");
            isartorResultFile = System.err;
        }
        else
        {
            isartorResultFile = new FileOutputStream(irp);
        }
    }

    @AfterAll
    public static void afterClass() throws Exception
    {
        IOUtils.closeQuietly(isartorResultFile);
    }

    @ParameterizedTest
	@MethodSource("initializeParameters")
    void validate(File path) throws Exception
    {
        logger = LogFactory.getLog(path != null ? path.getName() : "dummy");
        if (path == null)
        {
            logger.warn("This is an empty test");
            return;
        }
        ValidationResult result = PreflightParser.validate(path);

        assertFalse(result.isValid(), path + " : Isartor file should be invalid (" + path + ")");
        assertTrue(result.getErrorsList().size() > 0, path + " : Should find at least one error");
        // could contain more than one error
        if (result.getErrorsList().size() > 0)
        {
            fail("File expected valid : " + path.getAbsolutePath());
        }
    }

}
