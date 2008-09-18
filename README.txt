===================================================
Apache PDFBox <http://incubator.apache.org/pdfbox/>
===================================================

PDFBox is an open source Java library for working with PDF documents.

You need Apache Ant <http://ant.apache.org/> to build PDFBox. Once you
have installed Ant, you can build the sources by running "ant" in
this directory.

The Ant build will create a Checkstyle report if you have Checkstyle
<http://checkstyle.sourceforge.net/> installed. Set the checkstyle.home.dir
property in a build-local.properties file to enable the report.

The Ant build will build the PDFBox web site if you have Apache Forrest
<http://forrest.apache.org/> installed. Set the FORREST_HOME environment
variable to enable the web site build.

Known Limitations and Problems
==============================

1. You get text like "G38G43G36G51G5" instead of what you expect when you are
   extracting text. This is because the characters are a meaningless internal
   encoding that point to glyphs that are embedded in the PDF document. The
   only way to access the text is to use OCR. This may be a future
   enhancement.

2. You get an error message like "java.io.IOException: Can't handle font width"
   this MIGHT be due to the fact that you don't have the Resources directory
   in your classpath. The easiest solution is to simply include the
   apache-pdfbox-x.x.x.jar in your classpath.

See the issue tracker at https://issues.apache.org/jira/browse/PDFBOX for
the full list of known issues and requested features.

Disclaimer
==========

Apache PDFBox is an effort undergoing incubation at The Apache Software
Foundation (ASF), sponsored by the Apache Incubator PMC. Incubation is
required of all newly accepted projects until a further review indicates
that the infrastructure, communications, and decision making process have
stabilized in a manner consistent with other successful ASF projects. While
incubation status is not necessarily a reflection of the completeness or
stability of the code, it does indicate that the project has yet to be fully
endorsed by the ASF.

See http://incubator.apache.org/projects/pdfbox.html for the current
incubation status of the Apache PDFBox project.

License (see also LICENSE.txt)
==============================

Collective work: Copyright 2008 The Apache Software Foundation.

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
