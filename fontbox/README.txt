====================================================
Apache FontBox <http://pdfbox.apache.org/>
====================================================

FontBox is an open source Java library for working with PDF fonts.

You need Java 5 (or higher) and Maven 2 <http://maven.apache.org/> to
build FontBox. The recommended build command is:

    mvn clean install

The default build will compile the Java sources and package the binary
classes into a jar package. If you have IKVM.NET <http://www.ikvm.net/>
installed, you can use the -Dikvm=... option to also build a .NET DLL.

    mvn clean install -Dikvm=/path/to/ikvm

See the Maven documentation for all the other available build options.

See the issue tracker at https://issues.apache.org/jira/browse/PDFBOX 
(component FontBox) for the full list of known issues and requested features.

FontBox is a subproject of Apache PDFBox. PDFBox is a project of the
Apache Software Foundation <http://www.apache.org/>.
 
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
