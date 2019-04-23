Apache PDFBox <http://pdfbox.apache.org/>
===================================================

The Apache PDFBox library is an open source Java tool for working with PDF 
documents. This project allows creation of new PDF documents, manipulation 
of existing documents and the ability to extract content from documents.
PDFBox also includes several command line utilities. PDFBox is published
under the Apache License, Version 2.0.

PDFBox is a project of the Apache Software Foundation <http://www.apache.org/>.

Binary Downloads
----------------

You can download binary versions for releases currently under development or older
releases from our [Download Page](http://pdfbox.apache.org/download.cgi).

Build
-----

You need Java 6 (or higher) and Maven 2 <http://maven.apache.org/> to
build PDFBox. The recommended build command is:

    mvn clean install

The default build will compile the Java sources and package the binary
classes into jar packages. See the Maven documentation for all the
other available build options.

Contribute
----------

There are various ways to help us improve PDFBox. 

- look at the [Issue Tracker](https://issues.apache.org/jira/browse/PDFBOX) to help us fix bugs.
- answer questions on our [Users Mailing List](http://pdfbox.apache.org/mailinglists.html "Subscribe to Mailing List").
- help us enhance the [Examples](https://svn.apache.org/repos/asf/pdfbox/trunk/examples/)
- help us to enhance the [PDFBox Documentation](https://git-wip-us.apache.org/repos/asf/pdfbox-docs)
or on [GitHub](https://github.com/apache/pdfbox-docs). 

Support
-------

**Please follow the guidelines at our [Support Page](http://pdfbox.apache.org/support.html).**

If you have questions about how to use PDFBox do ask on the
[Users Mailing List](/mailinglists.html "Subscribe to Mailing List").
This will get you help from the entire community.

The PDFBox examples and the test code in the sources will also provide additional information.

And there are additional resources available on sites such as
[Stack Overflow](http://stackoverflow.com/search?q=pdfbox "Stack Overflow").

If you are sure you have found a bug the please report the issue in our 
[Issue Tracker](https://issues.apache.org/jira/browse/PDFBOX). 

Known Limitations and Problems
------------------------------

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
   not sort the text.

License (see also LICENSE.txt)
------------------------------

Collective work: Copyright 2015 The Apache Software Foundation.

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

Export control
--------------

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

**Apache PDFBox uses the Java Cryptography Architecture (JCA) and the
Bouncy Castle libraries for handling encryption in PDF documents.**
