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
package org.apache.pdfbox;


/**
 * Simple wrapper around all the command line utilities included in PDFBox.
 * Used as the main class in the runnable standalone PDFBox jar.
 *
 * @see <a href="https://issues.apache.org/jira/browse/PDFBOX-687">PDFBOX-687</a>
 */
public class PDFBox 
{

    /**
     * Main method.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) 
    {
        if (args.length > 0) 
        {
            String command = args[0];
            String[] arguments = new String[args.length - 1];
            System.arraycopy(args, 1, arguments, 0, arguments.length);
            boolean exitAfterCallingMain = true;
            try 
            {
                if (command.equals("ConvertColorspace")) 
                {
                    ConvertColorspace.main(arguments);
                } 
                else if (command.equals("Decrypt")) 
                {
                    Decrypt.main(arguments);
                } 
                else if (command.equals("Encrypt")) 
                {
                    Encrypt.main(arguments);
                } 
                else if (command.equals("ExtractText")) 
                {
                    ExtractText.main(arguments);
                } 
                else if (command.equals("ExtractImages")) 
                {
                    ExtractImages.main(arguments);
                } 
                else if (command.equals("OverlayPDF")) 
                {
                    OverlayPDF.main(arguments);
                } 
                else if (command.equals("PrintPDF")) 
                {
                    PrintPDF.main(arguments);
                } 
                else if (command.equals("PDFDebugger")) 
                {
                    PDFDebugger.main(arguments);
                    exitAfterCallingMain = false;
                } 
                else if (command.equals("PDFMerger")) 
                {
                    PDFMerger.main(arguments);
                } 
                else if (command.equals("PDFReader")) 
                {
                    PDFReader.main(arguments);
                    exitAfterCallingMain = false;
                } 
                else if (command.equals("PDFSplit")) 
                {
                    PDFSplit.main(arguments);
                } 
                else if (command.equals("PDFToImage")) 
                {
                    PDFToImage.main(arguments);
                } 
                else if (command.equals("TextToPDF")) 
                {
                    TextToPDF.main(arguments);
                } 
                else if (command.equals("WriteDecodedDoc")) 
                {
                    WriteDecodedDoc.main(arguments);
                }
                else 
                {
                    showMessageAndExit();
                }
                if (exitAfterCallingMain) 
                {
                    System.exit(0);
                }
            } 
            catch (Exception e) 
            {
                System.err.println(
                        command + " failed with the following exception:");
                e.printStackTrace();
                System.exit(1);
            }
        }
        else 
        {
            showMessageAndExit();
        }
    }

    private static void showMessageAndExit() 
    {
        System.err.println("PDFDBox version: \""+Version.getVersion()+ "\"");
        System.err.println("\nUsage: java pdfbox-app-x.y.z.jar <command> <args..>");
        System.err.println("\nPossible commands are:\n");
        System.err.println("  ConvertColorspace");
        System.err.println("  Decrypt");
        System.err.println("  Encrypt"); 
        System.err.println("  ExtractText"); 
        System.err.println("  ExtractImages"); 
        System.err.println("  OverlayPDF"); 
        System.err.println("  PrintPDF");
        System.err.println("  PDFDebugger"); 
        System.err.println("  PDFMerger");
        System.err.println("  PDFReader");
        System.err.println("  PDFSplit");
        System.err.println("  PDFToImage"); 
        System.err.println("  TextToPDF");
        System.err.println("  WriteDecodedDoc"); 
        System.exit(1);
    }
}
