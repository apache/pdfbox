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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.activation.FileDataSource;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestValidFiles
{

    private static final String RESULTS_FILE = "results.file";

    private static final String ISARTOR_FILES = "valid.files";

    protected static OutputStream isartorResultFile = null;

    protected File path;

    protected static Logger staticLogger = Logger.getLogger("Test");

    protected Logger logger = null;

    public TestValidFiles(File path)
    {
        this.path = path;
        this.logger = Logger.getLogger(path != null ? path.getName() : "dummy");
    }

    protected static Collection<Object[]> stopIfExpected() throws Exception
    {
        // throw new Exception("Test badly configured");
        List<Object[]> ret = new ArrayList<Object[]>();
        ret.add(new Object[] { null });
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
        // prepare config
        List<Object[]> data = new ArrayList<Object[]>();
        Collection<?> files = FileUtils.listFiles(root, new String[] { "pdf" }, true);

        for (Object object : files)
        {
            File file = (File) object;
            Object[] tmp = new Object[] { file };
            data.add(tmp);
        }
        return data;
    }

    @BeforeClass
    public static void beforeClass() throws Exception
    {
        String irp = System.getProperty(RESULTS_FILE);
        if (irp == null)
        {
            // no log file defined, use system.err
            System.err.println("No result file defined, will use standart error");
            isartorResultFile = System.err;
        }
        else
        {
            isartorResultFile = new FileOutputStream(irp);
        }
    }

    @AfterClass
    public static void afterClass() throws Exception
    {
        IOUtils.closeQuietly(isartorResultFile);
    }

    @Test()
    public void validate() throws Exception
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
            if (result.getErrorsList().size() > 0)
            {
                Assert.fail("File expected valid : " + path.getAbsolutePath());
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

}
