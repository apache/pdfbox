/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.pdfbox.preflight;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.parser.PreflightParser;

public class Benchmark
{

    /**
     * @param args
     */
    public static void main(final String[] args) throws Exception
    {
        if (args.length < 3)
        {
            System.err.println("Usage : Benchmark loop resultFile <file1 ... filen|dir>");
            System.exit(255);
        }

        final Integer loop = Integer.parseInt(args[0]);
        final FileWriter resFile = new FileWriter(new File(args[1]));

        final List<File> lfd = new ArrayList<>();
        for (int i = 2; i < args.length; ++i)
        {
            final File fi = new File(args[i]);
            if (fi.isDirectory())
            {
                final Collection<File> cf = FileUtils.listFiles(fi, null, true); // Get All files contained by the dir
                lfd.addAll(cf);
            }
            else
            {
                lfd.add(fi);
            }
        }

        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.Z");

        final long startGTime = System.currentTimeMillis();

        final int size = lfd.size();
        for (int i = 0; i < loop; i++)
        {
            final File file = lfd.get(i % size);
            final long startLTime = System.currentTimeMillis();
            final ValidationResult result = PreflightParser.validate(file);
            if (!result.isValid())
            {
                resFile.write(file.getAbsolutePath() + " isn't PDF/A\n");
                for (final ValidationError error : result.getErrorsList())
                {
                    resFile.write(error.getErrorCode() + " : " + error.getDetails() + "\n");
                }
            }
            final long endLTime = System.currentTimeMillis();
            resFile.write(file.getName() + " (ms) : " + (endLTime - startLTime) + "\n");
            resFile.flush();
        }

        final long endGTime = System.currentTimeMillis();

        resFile.write("Start : " + sdf.format(new Date(startGTime)) + "\n");
        resFile.write("End : " + sdf.format(new Date(endGTime)) + "\n");
        resFile.write("Duration (ms) : " + (endGTime - startGTime) + "\n");
        resFile.write("Average (ms) : " + (int) ((endGTime - startGTime) / loop) + "\n");

        System.out.println("Start : " + sdf.format(new Date(startGTime)));
        System.out.println("End : " + sdf.format(new Date(endGTime)));
        System.out.println("Duration (ms) : " + (endGTime - startGTime));
        System.out.println("Average (ms) : " + (int) ((endGTime - startGTime) / loop));
        resFile.flush();
        IOUtils.closeQuietly(resFile);
    }
}
