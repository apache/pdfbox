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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.activation.FileDataSource;

import junit.framework.Assert;

import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestValidDirectory
{

    protected File target = null;

    public TestValidDirectory(File file)
    {
        this.target = file;
    }

    @Test
    public void validate() throws Exception
    {
        PreflightDocument document = null;
        System.out.println(target);
        ValidationResult result = null;
        try
        {
            PreflightParser parser = new PreflightParser(new FileDataSource(target));
            parser.parse();
            document = (PreflightDocument) parser.getPDDocument();
            document.validate();
            result = document.getResult();
        }
        catch (SyntaxValidationException e)
        {
            result = e.getResult();
        }
        finally
        {
            if (document != null)
            {
                document.close();
            }
        }
        Assert.assertTrue("Validation of " + target, result.isValid());
    }

    @Parameters
    public static Collection<Object[]> initializeParameters() throws Exception
    {
        // check directory
        File directory = null;
        String pdfPath = System.getProperty("pdfa.valid", null);
        if ("${user.pdfa.valid}".equals(pdfPath))
        {
            pdfPath = null;
        }
        if (pdfPath != null)
        {
            directory = new File(pdfPath);
            if (!directory.exists())
                throw new Exception("directory does not exists : " + directory.getAbsolutePath());
            if (!directory.isDirectory())
                throw new Exception("not a directory : " + directory.getAbsolutePath());
        }
        else
        {
            System.err.println("System property 'pdfa.valid' not defined, will not run TestValidaDirectory");
        }
        // create list
        if (directory == null)
        {
            return new ArrayList<Object[]>(0);
        }
        else
        {
            File[] files = directory.listFiles();
            List<Object[]> data = new ArrayList<Object[]>(files.length);
            for (File file : files)
            {
                if (file.isFile())
                {
                    data.add(new Object[] { file });
                }
            }
            return data;
        }
    }

}
