<%--
 ! Licensed to the Apache Software Foundation (ASF) under one or more
 ! contributor license agreements.  See the NOTICE file distributed with
 ! this work for additional information regarding copyright ownership.
 ! The ASF licenses this file to You under the Apache License, Version 2.0
 ! (the "License"); you may not use this file except in compliance with
 ! the License.  You may obtain a copy of the License at
 !
 !      http://www.apache.org/licenses/LICENSE-2.0
 !
 ! Unless required by applicable law or agreed to in writing, software
 ! distributed under the License is distributed on an "AS IS" BASIS,
 ! WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ! See the License for the specific language governing permissions and
 ! limitations under the License.
 --%>
<%@ page import="org.apache.pdfbox.pdmodel.PDDocument"%>
<%@ page import="org.apache.pdfbox.util.PDFHighlighter"%>
<%@ page import="java.net.URL"%>

<%
    long start = System.currentTimeMillis();
    response.setHeader("Cache-Control","no-cache") ;
    response.setHeader("Pragma","no-cache") ;
    response.setDateHeader("Expires",0);

    String pdfURLString = request.getParameter( "pdf" );
    String highlightWords = request.getParameter( "words" );

    URL pdfURL = new URL( pdfURLString );
    PDDocument doc = null;
    try
    {
        doc = PDDocument.load( pdfURL.openStream() );
        PDFHighlighter highlighter = new PDFHighlighter();
        highlighter.generateXMLHighlight( doc, highlightWords.split( " " ), out );
    }
    finally
    {
        if( doc != null )
        {
            doc.close();
        }
    }
    long stop = System.currentTimeMillis();
    System.out.println( "Highlighter time:" +(stop-start) );
%>
