/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.examples.ant;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.tools.ExtractText;
import org.apache.tools.ant.Task;

import org.apache.tools.ant.types.FileSet;

/**
 * This is an Ant task that will allow pdf documents to be converted using an Ant task.
 *
 * @author Ben Litchfield
 */
public class PDFToTextTask extends Task
{
    private final List<FileSet> fileSets = new ArrayList<>();

    /**
     * Adds a set of files (nested fileset attribute).
     *
     * @param set Another fileset to add.
     */
    public void addFileset(final FileSet set )
    {
        fileSets.add( set );
    }

    /**
     * This will perform the execution.
     */
    @Override
    public void execute()
    {
        log( "PDFToTextTask executing" );

        fileSets.stream().
            map(fileSet -> fileSet.getDirectoryScanner(getProject())).
            forEachOrdered(dirScanner ->
            {
                dirScanner.scan();
                for (final String file : dirScanner.getIncludedFiles())
                {
                    final File f = new File(dirScanner.getBasedir(), file);
                    log("processing: " + f.getAbsolutePath());
                    final String pdfFile = f.getAbsolutePath();
                    if (pdfFile.toUpperCase().endsWith(".PDF"))
                    {
                        final String textFile = pdfFile.substring(0, pdfFile.length() - 3) + "txt";
                        ExtractText.main(new String[] {pdfFile, textFile});
                    }
                }
            });
    }
}
