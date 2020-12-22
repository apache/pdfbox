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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.PublicKeyProtectionPolicy;
import org.apache.pdfbox.pdmodel.encryption.PublicKeyRecipient;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * This will read a document from the filesystem, encrypt it and and then write
 * the results to the filesystem.
 *
 * @author  Ben Litchfield
 */
@Command(name = "Encrypt", description = "Encrypt a PDF file.")
public final class Encrypt implements Callable<Integer>
{
    // Expected for CLI app to write to System.out/Sytem.err
    @SuppressWarnings("squid:S106")
    private static final PrintStream SYSERR = System.err;

    @Option(names = "-O", description = "set the owner password (ignored if certFile is set)")
    private String ownerPassword;

    @Option(names = "-U", description = "set the user password (ignored if certFile is set)")
    private String userPassword;

    @Option(names = "-certFile", paramLabel="certFile", description = "Path to X.509 certificate (repeat both if needed)")
    private List<File> certFileList = new ArrayList<>();

    @Option(names = "-canAssemble", description = "set the assemble permission (default: ${DEFAULT-VALUE})")
    private boolean canAssembleDocument = true;

    @Option(names = "-canExtractContent", description = "set the extraction permission (default: ${DEFAULT-VALUE})")
    private boolean canExtractContent = true;

    @Option(names = "-canExtractForAccessibility", description = "set the extraction permission (default: ${DEFAULT-VALUE})")
    private boolean canExtractForAccessibility = true;

    @Option(names = "-canFillInForm", description = "set the form fill in permission (default: ${DEFAULT-VALUE})")
    private boolean canFillInForm = true;

    @Option(names = "-canModify", description = "set the modify permission (default: ${DEFAULT-VALUE})")
    private boolean canModify = true;

    @Option(names = "-canModifyAnnotations", description = "set the modify annots permission (default: ${DEFAULT-VALUE})")
    private boolean canModifyAnnotations = true;

    @Option(names = "-canPrint", description = "set the print permission (default: ${DEFAULT-VALUE})")
    private boolean canPrint = true;

    @Option(names = "-canPrintDegraded", description = "set the print degraded permission (default: ${DEFAULT-VALUE})")
    private boolean canPrintDegraded = true;

    @Option(names = "-keyLength", description = "Key length in bits (valid values: 40, 128 or 256) (default: ${DEFAULT-VALUE})")
    private int keyLength = 256;

    @Parameters(paramLabel = "inputfile", arity="1", description = "the PDF file to encyrpt.")
    private File infile;

    @Parameters(paramLabel = "outputfile", index = "1", description = "the encrypted PDF file. If left blank the original file will be overwritten.")
    private File outfile;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    private Encrypt()
    {
    }

    /**
     * This is the entry point for the application.
     *
     * @param args The command-line arguments.
     *
     * @throws IOException If there is an error decrypting the document.
     * @throws CertificateException If there is an error with a certificate.
     */
    public static void main(final String[] args )
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        final int exitCode = new CommandLine(new Encrypt()).execute(args);
        System.exit(exitCode);
    }

    public Integer call()
    {
        final AccessPermission ap = new AccessPermission();
        ap.setCanAssembleDocument(canAssembleDocument);
        ap.setCanExtractContent(canExtractContent);
        ap.setCanExtractForAccessibility(canExtractForAccessibility);
        ap.setCanFillInForm(canFillInForm);
        ap.setCanModify(canModify);
        ap.setCanModifyAnnotations(canModifyAnnotations);
        ap.setCanPrint(canPrint);
        ap.setCanPrintDegraded(canPrintDegraded);

        if (outfile == null)
        {
            outfile = infile;
        }

        try (PDDocument document = Loader.loadPDF(infile))
        {
            if( !document.isEncrypted() )
            {
                if (!certFileList.isEmpty())
                {
                    final PublicKeyProtectionPolicy ppp = new PublicKeyProtectionPolicy();
                    final PublicKeyRecipient recip = new PublicKeyRecipient();
                    recip.setPermission(ap);

                    final CertificateFactory cf = CertificateFactory.getInstance("X.509");

                    for (final File certFile : certFileList)
                    {
                        try (InputStream inStream = new FileInputStream(certFile))
                        {
                            final X509Certificate certificate = (X509Certificate) cf.generateCertificate(inStream);
                            recip.setX509(certificate);
                        }
                        ppp.addRecipient(recip);
                    }

                    ppp.setEncryptionKeyLength(keyLength);

                    document.protect(ppp);
                }
                else
                {
                    final StandardProtectionPolicy spp =
                        new StandardProtectionPolicy(ownerPassword, userPassword, ap);
                    spp.setEncryptionKeyLength(keyLength);
                    document.protect(spp);
                }
                document.save( outfile );
            }
            else
            {
                SYSERR.println( "Error: Document is already encrypted." );
            }
        }
        catch (IOException | CertificateException ex)
        {
            SYSERR.println( "Error encrypting PDF: " + ex.getMessage());
            return 4;
        }
        return 0;
    }
}
