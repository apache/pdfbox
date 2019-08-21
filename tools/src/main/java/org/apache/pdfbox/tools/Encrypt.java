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
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.PublicKeyProtectionPolicy;
import org.apache.pdfbox.pdmodel.encryption.PublicKeyRecipient;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

/**
 * This will read a document from the filesystem, encrypt it and and then write
 * the results to the filesystem.
 *
 * @author  Ben Litchfield
 */
public final class Encrypt
{
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
    public static void main( String[] args ) throws IOException, CertificateException
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        Encrypt encrypt = new Encrypt();
        encrypt.encrypt( args );
    }

    private void encrypt( String[] args ) throws IOException, CertificateException
    {
        if( args.length < 1 )
        {
            usage();
        }
        else
        {
            AccessPermission ap = new AccessPermission();

            String infile = null;
            String outfile = null;
            String certFile = null;
            @SuppressWarnings({"squid:S2068"})
            String userPassword = "";
            @SuppressWarnings({"squid:S2068"})
            String ownerPassword = "";

            int keyLength = 256;

            PDDocument document = null;

            try
            {
                for( int i=0; i<args.length; i++ )
                {
                    String key = args[i];
                    if( key.equals( "-O" ) )
                    {
                        ownerPassword = args[++i];
                    }
                    else if( key.equals( "-U" ) )
                    {
                        userPassword = args[++i];
                    }
                    else if( key.equals( "-canAssemble" ) )
                    {
                        ap.setCanAssembleDocument(args[++i].equalsIgnoreCase( "true" ));
                    }
                    else if( key.equals( "-canExtractContent" ) )
                    {
                        ap.setCanExtractContent( args[++i].equalsIgnoreCase( "true" ) );
                    }
                    else if( key.equals( "-canExtractForAccessibility" ) )
                    {
                        ap.setCanExtractForAccessibility( args[++i].equalsIgnoreCase( "true" ) );
                    }
                    else if( key.equals( "-canFillInForm" ) )
                    {
                        ap.setCanFillInForm( args[++i].equalsIgnoreCase( "true" ) );
                    }
                    else if( key.equals( "-canModify" ) )
                    {
                        ap.setCanModify( args[++i].equalsIgnoreCase( "true" ) );
                    }
                    else if( key.equals( "-canModifyAnnotations" ) )
                    {
                        ap.setCanModifyAnnotations( args[++i].equalsIgnoreCase( "true" ) );
                    }
                    else if( key.equals( "-canPrint" ) )
                    {
                        ap.setCanPrint( args[++i].equalsIgnoreCase( "true" ) );
                    }
                    else if( key.equals( "-canPrintDegraded" ) )
                    {
                        ap.setCanPrintDegraded( args[++i].equalsIgnoreCase( "true" ) );
                    }
                    else if( key.equals( "-certFile" ) )
                    {
                        certFile = args[++i];
                    }
                    else if( key.equals( "-keyLength" ) )
                    {
                        try
                        {
                            keyLength = Integer.parseInt( args[++i] );
                        }
                        catch( NumberFormatException e )
                        {
                            throw new NumberFormatException(
                                "Error: -keyLength is not an integer '" + args[i] + "'" );
                        }
                    }
                    else if( infile == null )
                    {
                        infile = key;
                    }
                    else if( outfile == null )
                    {
                        outfile = key;
                    }
                    else
                    {
                        usage();
                    }
                }
                if( infile == null )
                {
                    usage();
                }
                if( outfile == null )
                {
                    outfile = infile;
                }
                document = PDDocument.load( new File(infile) );

                if( !document.isEncrypted() )
                {
                    if( certFile != null )
                    {
                        PublicKeyProtectionPolicy ppp = new PublicKeyProtectionPolicy();
                        PublicKeyRecipient recip = new PublicKeyRecipient();
                        recip.setPermission(ap);


                        CertificateFactory cf = CertificateFactory.getInstance("X.509");
                        
                        try (InputStream inStream = new FileInputStream(certFile))
                        {
                            X509Certificate certificate = (X509Certificate) cf.generateCertificate(inStream);
                            recip.setX509(certificate);
                        }                 

                        ppp.addRecipient(recip);

                        ppp.setEncryptionKeyLength(keyLength);

                        document.protect(ppp);
                    }
                    else
                    {
                        StandardProtectionPolicy spp =
                            new StandardProtectionPolicy(ownerPassword, userPassword, ap);
                        spp.setEncryptionKeyLength(keyLength);
                        document.protect(spp);
                    }
                    document.save( outfile );
                }
                else
                {
                    System.err.println( "Error: Document is already encrypted." );
                }
            }
            finally
            {
                if( document != null )
                {
                    document.close();
                }
            }
        }
    }

    /**
     * This will print a usage message.
     */
    private static void usage()
    {
        String message = "Usage: java -jar pdfbox-app-x.y.z.jar Encrypt [options] <inputfile> [outputfile]\n"
                + "\nOptions:\n"
                + "  -O <password>                            : Set the owner password (ignored if certFile is set)\n"
                + "  -U <password>                            : Set the user password (ignored if certFile is set)\n"
                + "  -certFile <path to cert>                 : Path to X.509 certificate\n"
                + "  -canAssemble <true|false>                : Set the assemble permission\n"
                + "  -canExtractContent <true|false>          : Set the extraction permission\n"
                + "  -canExtractForAccessibility <true|false> : Set the extraction permission\n"
                + "  -canFillInForm <true|false>              : Set the fill in form permission\n"
                + "  -canModify <true|false>                  : Set the modify permission\n"
                + "  -canModifyAnnotations <true|false>       : Set the modify annots permission\n"
                + "  -canPrint <true|false>                   : Set the print permission\n"
                + "  -canPrintDegraded <true|false>           : Set the print degraded permission\n"
                + "  -keyLength <length>                      : Key length in bits "
                + "(valid values: 40, 128 or 256, default is 256)\n"
                + "\nNote: By default all permissions are set to true!";
        
        System.err.println(message);
        System.exit(1);
    }

}
