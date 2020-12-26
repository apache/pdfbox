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

import org.apache.pdfbox.debugger.PDFDebugger;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Simple wrapper around all the command line utilities included in PDFBox.
 * Used as the main class in the runnable standalone PDFBox jar.
 */
@Command(name="PDFBox", subcommands = {
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
}, versionProvider = Version.class, mixinStandardHelpOptions = true)
public final class PDFBox implements Runnable
{
    // Expected for CLI app to write to System.out/Sytem.err
    @SuppressWarnings("squid:S106")
    private static final PrintStream SYSOUT = System.out;
  
    /**
     * Main method.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        CommandLine cmd = new CommandLine(new PDFBox());
        cmd.execute(args);
    
        if (args.length == 0) { cmd.usage(SYSOUT); }
    }

    @Override
    public void run()
    {
        // stub method to please the Runnable interface needed by picocli
    }
}
