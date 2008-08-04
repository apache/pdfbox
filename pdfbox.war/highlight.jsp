<%@ page import="org.pdfbox.pdmodel.PDDocument"%>
<%@ page import="org.pdfbox.util.PDFHighlighter"%>
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