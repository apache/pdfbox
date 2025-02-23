package org.apache.pdfbox.cos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.apache.pdfbox.cos.TestCOSString.ESC_CHAR_STRING_PDF_FORMAT;

public class TestVisitor implements ICOSVisitor
{
    private final ByteArrayOutputStream output;
    public TestVisitor( ByteArrayOutputStream outStream )
    {
        output = outStream;
    }

    @Override
    public void visitFromArray( COSArray obj ) throws IOException
    {
        // Write something to the output buffer just so we know that the visitor got called.

    }

    @Override
    public void visitFromBoolean( COSBoolean cosBoolean ) throws IOException
    {
        if( cosBoolean.getValue() )
        {
            output.write( COSBoolean.TRUE_BYTES );
        }
        else
        {
            output.write( COSBoolean.FALSE_BYTES );
        }

    }

    @Override
    public void visitFromDictionary( COSDictionary obj ) throws IOException
    {
        // Write something to the output buffer just so we know that the visitor got called.

    }

    @Override
    public void visitFromDocument( COSDocument obj ) throws IOException
    {
        // Write something to the output buffer just so we know that the visitor got called.

    }

    @Override
    public void visitFromFloat( COSFloat cosFloat ) throws IOException
    {
        // Write something to the output buffer just so we know that the visitor got called.
        output.write( cosFloat.toString().getBytes( StandardCharsets.ISO_8859_1 ) );
    }

    @Override
    public void visitFromInt( COSInteger cosInteger ) throws IOException
    {
        // Write something to the output buffer just so we know that the visitor got called.
        output.write( Integer.toString( cosInteger.intValue() ).getBytes( StandardCharsets.ISO_8859_1 ) );
    }

    @Override
    public void visitFromName( COSName obj ) throws IOException
    {
        throw new IOException();
    }

    @Override
    public void visitFromNull( COSNull obj ) throws IOException
    {
        output.write( "COSNull.NULL".getBytes( StandardCharsets.ISO_8859_1 ));
    }

    @Override
    public void visitFromStream( COSStream obj ) throws IOException
    {

    }

    @Override
    public void visitFromString( COSString cosString ) throws IOException
    {
        if (cosString.getForceHexForm())
        {
            output.write( ("<" + cosString.toHexString() + ">").getBytes( StandardCharsets.ISO_8859_1 ));
        }
        else
        {
            output.write( ("(" + ESC_CHAR_STRING_PDF_FORMAT + ")").getBytes( StandardCharsets.ISO_8859_1 ));
        }
    }
}
