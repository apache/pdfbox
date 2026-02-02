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
package org.apache.pdfbox.tools;

import java.io.PrintStream;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

/**
 * A simple command line utility to get the version of PDFBox.
 *
 * @author Ben Litchfield
 */
@Command(name = "version", header = "Gets the version of PDFBox")
final class Version implements Callable<Integer>, IVersionProvider
{
    // Expected for CLI app to write to System.out/System.err
    @SuppressWarnings("squid:S106")
    private final PrintStream SYSOUT;

    @Spec CommandSpec spec;

    /**
     * Get the version of PDFBox or unknown if it is not known.
     *
     * @return The version of pdfbox that is being used.
     */
    public String[] getVersion()
    {
        String version = org.apache.pdfbox.util.Version.getVersion();
        if (version != null)
        {
            return new String[] { spec.qualifiedName() + " [" + version + "]" };
        }
        else
        {
            return new String[] { "unknown" };
        }
    }

    /**
     * Constructor.
     */
    public Version()
    {
        SYSOUT = System.out;
    }

    /**
     * This will print out the version of PDF to System.out.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");
        
        int exitCode = new CommandLine(new Version()).execute(args);
        System.exit(exitCode);
    }

    public Integer call()
    {
        SYSOUT.println(getVersion()[0]);
        return 0;
    }
}
