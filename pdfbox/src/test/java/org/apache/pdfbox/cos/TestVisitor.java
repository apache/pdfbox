package org.apache.pdfbox.cos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TestVisitor implements ICOSVisitor
{
    // TODO: make these statics package private in the other classes so we can
    //  consume them but remain in sync.
    /**
     * The true boolean token.
     */
    private static final byte[] TRUE_BYTES = { 116, 114, 117, 101 }; // "true".getBytes("ISO-8859-1")
    /**
     * The false boolean token.
     */
    private static final byte[] FALSE_BYTES = { 102, 97, 108, 115, 101 }; // "false".getBytes("ISO-8859-1")

    static final String ESC_CHAR_STRING_PDF_FORMAT =    // We can probably change this to an arbitrary string
            "\\( test#some\\) escaped< \\\\chars>!~1239857 ";

    private final ByteArrayOutputStream output;

    public TestVisitor( ByteArrayOutputStream outStream )
    {
        output = outStream;
    }

    @Override
    public void visitFromArray( COSArray obj ) throws IOException
    {
        // TODO: Write something to the output buffer just so we know that the visitor got called.
    }

    @Override
    public void visitFromBoolean( COSBoolean cosBoolean ) throws IOException
    {
        if( cosBoolean.getValue() )
        {
            output.write( TRUE_BYTES );
        }
        else
        {
            output.write( FALSE_BYTES );
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
