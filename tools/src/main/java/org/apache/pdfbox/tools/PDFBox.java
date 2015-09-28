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
            boolean exitAfterCallingMain = true;
            if (command.equals("Decrypt"))
            {
                Decrypt.main(copyCommandlineArgs(args));
            }
            else if (command.equals("Encrypt"))
            {
                Encrypt.main(copyCommandlineArgs(args));
            }
            else if (command.equals("ExtractText"))
            {
                ExtractText.main(copyCommandlineArgs(args));
            }
            else if (command.equals("ExtractImages"))
            {
                ExtractImages.main(copyCommandlineArgs(args));
            }
            else if (command.equals("OverlayPDF"))
            {
                OverlayPDF.main(copyCommandlineArgs(args));
            }
            else if (command.equals("PrintPDF"))
            {
                PrintPDF.main(copyCommandlineArgs(args));
            }
            else if (command.equals("PDFDebugger"))
            {
                PDFDebugger.main(copyCommandlineArgs(args));
                exitAfterCallingMain = false;
            }
            else if (command.equals("PDFMerger"))
            {
                PDFMerger.main(copyCommandlineArgs(args));
            }
            else if (command.equals("PDFReader"))
            {
                PDFDebugger.main(copyCommandlineArgs(args, "-viewpages"));
                exitAfterCallingMain = false;
            }
            else if (command.equals("PDFSplit"))
            {
                PDFSplit.main(copyCommandlineArgs(args));
            }
            else if (command.equals("PDFToImage"))
            {
                PDFToImage.main(copyCommandlineArgs(args));
            }
            else if (command.equals("TextToPDF"))
            {
                TextToPDF.main(copyCommandlineArgs(args));
            }
            else if (command.equals("WriteDecodedDoc"))
            {
                WriteDecodedDoc.main(copyCommandlineArgs(args));
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
        else 
        {
            showMessageAndExit();
        }
    }

    private static String[] copyCommandlineArgs(String[] args)
    {
        // copy all arguments but the first
        String[] arguments = new String[args.length - 1];
        System.arraycopy(args, 1, arguments, 0, arguments.length);
        return arguments;
    }
    
    private static String[] copyCommandlineArgs(String[] args, String arg0)
    {
        // copy all arguments, replace the first with the given new one
        String[] arguments = new String[args.length];
        System.arraycopy(args, 0, arguments, 0, arguments.length);
        arguments[0] = arg0;
        return arguments;
    }

    private static void showMessageAndExit() 
    {
        String message = "PDFBox version: \""+ Version.getVersion()+ "\""
                + "\nUsage: java pdfbox-app-x.y.z.jar <command> <args..>\n"
                + "\nPossible commands are:\n"
                + "  ConvertColorspace\n"
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
