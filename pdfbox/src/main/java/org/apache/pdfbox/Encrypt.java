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

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.PublicKeyProtectionPolicy;
import org.apache.pdfbox.pdmodel.encryption.PublicKeyRecipient;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

/**
 * This will read a document from the filesystem, encrypt it and and then write
 * the results to the filesystem. <br/><br/>
 *
 * @author  <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.9 $
 */
public class Encrypt
{
    private Encrypt()
    {
    }

    /**
     * This is the entry point for the application.
     *
     * @param args The command-line arguments.
     *
     * @throws Exception If there is an error decrypting the document.
     */
    public static void main( String[] args ) throws Exception
    {
        Encrypt encrypt = new Encrypt();
        encrypt.encrypt( args );
    }

    private void encrypt( String[] args ) throws Exception
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
            String userPassword = "";
            String ownerPassword = "";

            int keyLength = 40;

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
                document = PDDocument.load( infile );

                if( !document.isEncrypted() )
                {
                    if( certFile != null )
                    {
                        PublicKeyProtectionPolicy ppp = new PublicKeyProtectionPolicy();
                        PublicKeyRecipient recip = new PublicKeyRecipient();
                        recip.setPermission(ap);


                        CertificateFactory cf = CertificateFactory.getInstance("X.509");
                        InputStream inStream = new FileInputStream(certFile);
                        X509Certificate certificate = (X509Certificate)cf.generateCertificate(inStream);
                        inStream.close();

                        recip.setX509(certificate);

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
        System.err.println( "usage: java -jar pdfbox-app-x.y.z.jar Encrypt [options] <inputfile> [outputfile]" );
        System.err.println( "   -O <password>                            " +
                                            "Set the owner password(ignored if cert is set)" );
        System.err.println( "   -U <password>                            " +
                                            "Set the user password(ignored if cert is set)" );
        System.err.println( "   -certFile <path to cert>                 Path to X.509 certificate" );
        System.err.println( "   -canAssemble <true|false>                Set the assemble permission" );
        System.err.println( "   -canExtractContent <true|false>          Set the extraction permission" );
        System.err.println( "   -canExtractForAccessibility <true|false> Set the extraction permission" );
        System.err.println( "   -canFillInForm <true|false>              Set the fill in form permission" );
        System.err.println( "   -canModify <true|false>                  Set the modify permission" );
        System.err.println( "   -canModifyAnnotations <true|false>       Set the modify annots permission" );
        System.err.println( "   -canPrint <true|false>                   Set the print permission" );
        System.err.println( "   -canPrintDegraded <true|false>           Set the print degraded permission" );
        System.err.println( "   -keyLength <length>                      The length of the key in bits(40)" );
        System.err.println( "\nNote: By default all permissions are set to true!" );
        System.exit( 1 );
    }

}
