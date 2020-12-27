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

import org.apache.pdfbox.debugger.PDFDebugger;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

/**
 * Simple wrapper around all the command line utilities included in PDFBox.
 * Used as the main class in the runnable standalone PDFBox jar.
 */
@Command(name="PDFBox",
    customSynopsis = "PDFBox [COMMAND] [OPTIONS]",
    footer = {
        "See 'PDFBox help <command>' to read about a specific subcommand."
    },
    subcommands = {
        PDFDebugger.class,
        Decrypt.class,
        Encrypt.class,
        ExtractText.class,
        ExtractImages.class,
        OverlayPDF.class,
        PrintPDF.class,
        PDFMerger.class,
        PDFSplit.class,
        PDFToImage.class,
        ImageToPDF.class,
        TextToPDF.class,
        WriteDecodedDoc.class,
        CommandLine.HelpCommand.class
    },
    versionProvider = Version.class)
public final class PDFBox implements Runnable
{
    @Spec CommandLine.Model.CommandSpec spec;

      /**
     * Main method.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        new CommandLine(new PDFBox()).execute(args);
    }

    @Override
    public void run()
    {
        throw new ParameterException(spec.commandLine(), "Error: Subcommand required");
    }
}
