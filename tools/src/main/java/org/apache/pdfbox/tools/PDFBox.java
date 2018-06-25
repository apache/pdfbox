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

/**
 * Simple wrapper around all the command line utilities included in PDFBox.
 * Used as the main class in the runnable standalone PDFBox jar.
 *
 * @see <a href="https://issues.apache.org/jira/browse/PDFBOX-687">PDFBOX-687</a>
 */
public final class PDFBox 
{

    private PDFBox()
    {
    }
    
    /**
     * Main method.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) throws Exception
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        if (args.length > 0) 
        {
            String command = args[0];
            String[] arguments = new String[args.length - 1];
            System.arraycopy(args, 1, arguments, 0, arguments.length);
            boolean exitAfterCallingMain = true;
            switch (command)
            {
                case "Decrypt":
                    Decrypt.main(arguments);
                    break;
                case "Encrypt":
                    Encrypt.main(arguments);
                    break;
                case "ExtractText":
                    ExtractText.main(arguments);
                    break;
                case "ExtractImages":
                    ExtractImages.main(arguments);
                    break;
                case "OverlayPDF":
                    OverlayPDF.main(arguments);
                    break;
                case "PrintPDF":
                    PrintPDF.main(arguments);
                    break;
                case "PDFDebugger":
                case "PDFReader":
                    PDFDebugger.main(arguments);
                    exitAfterCallingMain = false;
                    break;
                case "PDFMerger":
                    PDFMerger.main(arguments);
                    break;
                case "PDFSplit":
                    PDFSplit.main(arguments);
                    break;
                case "PDFToImage":
                    PDFToImage.main(arguments);
                    break;
                case "TextToPDF":
                    TextToPDF.main(arguments);
                    break;
                case "WriteDecodedDoc":
                    WriteDecodedDoc.main(arguments);
                    break;
                default:
                    showMessageAndExit();
                    break;
            }
            if (exitAfterCallingMain)
            {
                System.exit(0);
            }
        }
        else 
        {
            showMessageAndExit();
        }
    }

    private static void showMessageAndExit() 
    {
        String message = "PDFBox version: \""+ Version.getVersion()+ "\""
                + "\nUsage: java -jar pdfbox-app-x.y.z.jar <command> <args..>\n"
                + "\nPossible commands are:\n"
                + "  Decrypt\n"
                + "  Encrypt\n"
                + "  ExtractText\n"
                + "  ExtractImages\n"
                + "  OverlayPDF\n"
                + "  PrintPDF\n"
                + "  PDFDebugger\n"
                + "  PDFMerger\n"
                + "  PDFReader\n"
                + "  PDFSplit\n"
                + "  PDFToImage\n"
                + "  TextToPDF\n"
                + "  WriteDecodedDoc";
        
        System.err.println(message);
        System.exit(1);
    }
}
