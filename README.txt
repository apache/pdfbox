===================================================
Apache PDFBox <http://pdfbox.apache.org/>
===================================================

PDFBox is an open source Java library for working with PDF documents.
This project allows creation of new PDF documents, manipulation of
existing documents and the ability to extract content from documents.
PDFBox also includes several command line utilities. PDFBox is published
under the Apache License, Version 2.0.

You need Java 5 (or higher) and Maven 2 <http://maven.apache.org/> to
build PDFBox. The recommended build command is:

    mvn clean install

The default build will compile the Java sources and package the binary
classes into jar packages. See the Maven documentation for all the
other available build options.

There is also an Ant build that you can use to build the same binaries.
The Ant build can also produce .NET DLLs if you have IKVM.NET
<http://www.ikvm.net/> installed. See the build.xml file in the pdfbox
subdirectory for details.

PDFBox is a project of the Apache Software Foundation <http://www.apache.org/>.

Known Limitations and Problems
==============================

See the issue tracker at https://issues.apache.org/jira/browse/PDFBOX for
the full list of known issues and requested features. Some of the more
commont issues are:

1. You get text like "G38G43G36G51G5" instead of what you expect when you are
   extracting text. This is because the characters are a meaningless internal
   encoding that point to glyphs that are embedded in the PDF document. The
   only way to access the text is to use OCR. This may be a future
   enhancement.

2. You get an error message like "java.io.IOException: Can't handle font width"
   this MIGHT be due to the fact that you don't have the
   org/apache/pdfbox/resources directory in your classpath. The easiest
   solution is to simply include the apache-pdfbox-x.x.x.jar in your classpath.

3. You get text that has the correct characters, but in the wrong
   order.  This mght be because you have not enabled sorting.  The text
   in PDF files is stored in chunks and the chunks do not need to be stored 
   in the order that they are displayed on a page.  By default, PDFBox does 
   not sort the text.  Also, if you have text in a language that reads right
   to left (such as Arabic or Hebrew), make sure you have the ICU4J jar file
   in your classpath. This library is needed to properly hande right to
   left text.

License (see also LICENSE.txt)
==============================

Collective work: Copyright 2011 The Apache Software Foundation.

Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Unmodifiable files
==================

Apache PDFBox contains the Adobe glyph list file that may be redistributed
only in *unmodified* form. See the LICENSE file for the exact licensing
conditions.

Export control
==============

This distribution includes cryptographic software.  The country in  which
you currently reside may have restrictions on the import,  possession, use,
and/or re-export to another country, of encryption software.  BEFORE using
any encryption software, please  check your country's laws, regulations and
policies concerning the import, possession, or use, and re-export of
encryption software, to  see if this is permitted.  See
<http://www.wassenaar.org/> for more information.

The U.S. Government Department of Commerce, Bureau of Industry and
Security (BIS), has classified this software as Export Commodity Control
Number (ECCN) 5D002.C.1, which includes information security software using
or performing cryptographic functions with asymmetric algorithms.  The form
and manner of this Apache Software Foundation distribution makes it eligible
for export under the License Exception ENC Technology Software Unrestricted
(TSU) exception (see the BIS Export Administration Regulations, Section
740.13) for both object code and source code.

The following provides more details on the included cryptographic software:

    Apache PDFBox uses the Java Cryptography Architecture (JCA) and the
    Bouncy Castle libraries for handling encryption in PDF documents.
