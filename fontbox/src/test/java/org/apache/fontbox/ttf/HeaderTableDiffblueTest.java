package org.apache.fontbox.ttf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.*;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class HeaderTableDiffblueTest {

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>default or parameterless constructor of {@link HeaderTable}
     *   <li>{@link HeaderTable#setCheckSumAdjustment(long)}
     *   <li>{@link HeaderTable#setCreated(Calendar)}
     *   <li>{@link HeaderTable#setFlags(int)}
     *   <li>{@link HeaderTable#setFontDirectionHint(short)}
     *   <li>{@link HeaderTable#setFontRevision(float)}
     *   <li>{@link HeaderTable#setGlyphDataFormat(short)}
     *   <li>{@link HeaderTable#setIndexToLocFormat(short)}
     *   <li>{@link HeaderTable#setLowestRecPPEM(int)}
     *   <li>{@link HeaderTable#setMacStyle(int)}
     *   <li>{@link HeaderTable#setMagicNumber(long)}
     *   <li>{@link HeaderTable#setModified(Calendar)}
     *   <li>{@link HeaderTable#setUnitsPerEm(int)}
     *   <li>{@link HeaderTable#setVersion(float)}
     *   <li>{@link HeaderTable#setXMax(short)}
     *   <li>{@link HeaderTable#setXMin(short)}
     *   <li>{@link HeaderTable#setYMax(short)}
     *   <li>{@link HeaderTable#setYMin(short)}
     *   <li>{@link HeaderTable#getCheckSumAdjustment()}
     *   <li>{@link HeaderTable#getCreated()}
     *   <li>{@link HeaderTable#getFlags()}
     *   <li>{@link HeaderTable#getFontDirectionHint()}
     *   <li>{@link HeaderTable#getFontRevision()}
     *   <li>{@link HeaderTable#getGlyphDataFormat()}
     *   <li>{@link HeaderTable#getIndexToLocFormat()}
     *   <li>{@link HeaderTable#getLowestRecPPEM()}
     *   <li>{@link HeaderTable#getMacStyle()}
     *   <li>{@link HeaderTable#getMagicNumber()}
     *   <li>{@link HeaderTable#getModified()}
     *   <li>{@link HeaderTable#getUnitsPerEm()}
     *   <li>{@link HeaderTable#getVersion()}
     *   <li>{@link HeaderTable#getXMax()}
     *   <li>{@link HeaderTable#getXMin()}
     *   <li>{@link HeaderTable#getYMax()}
     *   <li>{@link HeaderTable#getYMin()}
     * </ul>
     */
    @Test
    void testGettersAndSetters() {
        // Arrange and Act
        HeaderTable actualHeaderTable = new HeaderTable();
        long checkSumAdjustmentValue = 42L;
        actualHeaderTable.setCheckSumAdjustment( checkSumAdjustmentValue );
        GregorianCalendar createdValue = new GregorianCalendar( 1, 1, 1 );

        actualHeaderTable.setCreated( createdValue );
        int flagsValue = 42;
        actualHeaderTable.setFlags( flagsValue );
        short fontDirectionHintValue = (short) 1;
        actualHeaderTable.setFontDirectionHint( fontDirectionHintValue );
        float fontRevisionValue = 10.0f;
        actualHeaderTable.setFontRevision( fontRevisionValue );
        short glyphDataFormatValue = (short) 1;
        actualHeaderTable.setGlyphDataFormat( glyphDataFormatValue );
        short indexToLocFormatValue = (short) 1;
        actualHeaderTable.setIndexToLocFormat( indexToLocFormatValue );
        int lowestRecPPEMValue = 42;
        actualHeaderTable.setLowestRecPPEM( lowestRecPPEMValue );
        int macStyleValue = 42;
        actualHeaderTable.setMacStyle( macStyleValue );
        long magicNumberValue = 42L;
        actualHeaderTable.setMagicNumber( magicNumberValue );
        GregorianCalendar modifiedValue = new GregorianCalendar( 1, 1, 1 );

        actualHeaderTable.setModified( modifiedValue );
        int unitsPerEmValue = 42;
        actualHeaderTable.setUnitsPerEm( unitsPerEmValue );
        float versionValue = 10.0f;
        actualHeaderTable.setVersion( versionValue );
        short maxValue = (short) 1;
        actualHeaderTable.setXMax( maxValue );
        short minValue = (short) 1;
        actualHeaderTable.setXMin( minValue );
        short maxValue2 = (short) 1;
        actualHeaderTable.setYMax( maxValue2 );
        short minValue2 = (short) 1;
        actualHeaderTable.setYMin( minValue2 );
        long actualCheckSumAdjustment = actualHeaderTable.getCheckSumAdjustment();
        Calendar actualCreated = actualHeaderTable.getCreated();
        int actualFlags = actualHeaderTable.getFlags();
        short actualFontDirectionHint = actualHeaderTable.getFontDirectionHint();
        float actualFontRevision = actualHeaderTable.getFontRevision();
        short actualGlyphDataFormat = actualHeaderTable.getGlyphDataFormat();
        short actualIndexToLocFormat = actualHeaderTable.getIndexToLocFormat();
        int actualLowestRecPPEM = actualHeaderTable.getLowestRecPPEM();
        int actualMacStyle = actualHeaderTable.getMacStyle();
        long actualMagicNumber = actualHeaderTable.getMagicNumber();
        Calendar actualModified = actualHeaderTable.getModified();
        int actualUnitsPerEm = actualHeaderTable.getUnitsPerEm();
        float actualVersion = actualHeaderTable.getVersion();
        short actualXMax = actualHeaderTable.getXMax();
        short actualXMin = actualHeaderTable.getXMin();
        short actualYMax = actualHeaderTable.getYMax();
        short actualYMin = actualHeaderTable.getYMin();

        // Assert that nothing has changed
        assertEquals( 10.0f, actualFontRevision );
        assertEquals( 10.0f, actualVersion );
        assertEquals( (short) 1, actualFontDirectionHint );
        assertEquals( (short) 1, actualGlyphDataFormat );
        assertEquals( (short) 1, actualIndexToLocFormat );
        assertEquals( (short) 1, actualXMax );
        assertEquals( (short) 1, actualXMin );
        assertEquals( (short) 1, actualYMax );
        assertEquals( (short) 1, actualYMin );
        assertEquals( 42, actualFlags );
        assertEquals( 42, actualLowestRecPPEM );
        assertEquals( 42, actualMacStyle );
        assertEquals( 42, actualUnitsPerEm );
        assertEquals( 42L, actualCheckSumAdjustment );
        assertEquals( 42L, actualMagicNumber );
        assertEquals( actualCreated, actualModified );
        assertSame( createdValue, actualCreated );
        assertSame( modifiedValue, actualModified );
    }

    /**
     * Method under test: {@link HeaderTable#read(TrueTypeFont, TTFDataStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testRead() throws IOException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.io.EOFException
        //       at org.apache.fontbox.ttf.TTFDataStream.readUnsignedInt(TTFDataStream.java:137)
        //       at org.apache.fontbox.ttf.HeaderTable.read(HeaderTable.java:79)
        //   See https://diff.blue/R013 to resolve this issue.

        // Arrange
        HeaderTable headerTable = new HeaderTable();
        TrueTypeFont ttf = new TrueTypeFont(
                new RandomAccessReadDataStream( new ByteArrayInputStream( "AXAXAXAX".getBytes( "UTF-8" ) ) ) );
        RandomAccessReadDataStream data = new RandomAccessReadDataStream(
                new ByteArrayInputStream( "AXAXAXAX".getBytes( "UTF-8" ) ) );

        // Act
        headerTable.read( ttf, data );
    }

    /**
     * Method under test: {@link HeaderTable#read(TrueTypeFont, TTFDataStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testRead2() throws IOException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.io.EOFException
        //       at org.apache.fontbox.ttf.TTFDataStream.readUnsignedShort(TTFDataStream.java:154)
        //       at org.apache.fontbox.ttf.TTFDataStream.readSignedShort(TTFDataStream.java:201)
        //       at org.apache.fontbox.ttf.TTFDataStream.read32Fixed(TTFDataStream.java:49)
        //       at org.apache.fontbox.ttf.HeaderTable.read(HeaderTable.java:77)
        //   See https://diff.blue/R013 to resolve this issue.

        // Arrange
        HeaderTable headerTable = new HeaderTable();
        TrueTypeFont ttf = new TrueTypeFont(
                new RandomAccessReadDataStream( new ByteArrayInputStream( "AXAXAXAX".getBytes( "UTF-8" ) ) ) );
        RandomAccessReadDataStream data = new RandomAccessReadDataStream( new ByteArrayInputStream( new byte[]{} ) );

        // Act
        headerTable.read( ttf, data );
    }

    /**
     * Method under test: {@link HeaderTable#read(TrueTypeFont, TTFDataStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testRead3() throws IOException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.io.EOFException
        //       at org.apache.fontbox.ttf.TTFDataStream.readUnsignedShort(TTFDataStream.java:154)
        //       at org.apache.fontbox.ttf.HeaderTable.read(HeaderTable.java:81)
        //   See https://diff.blue/R013 to resolve this issue.

        // Arrange
        HeaderTable headerTable = new HeaderTable();
        TrueTypeFont ttf = new TrueTypeFont(
                new RandomAccessReadDataStream( new ByteArrayInputStream( "AXAXAXAX".getBytes( "UTF-8" ) ) ) );
        RandomAccessReadDataStream data = new RandomAccessReadDataStream(
                new ByteArrayInputStream( "A\bA\bA\bA\bA\bA\bA\bA\b".getBytes( StandardCharsets.UTF_8 ) ) );

        // Act
        headerTable.read( ttf, data );
    }

    /**
     * Method under test: {@link HeaderTable#read(TrueTypeFont, TTFDataStream)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testRead4() throws IOException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.io.EOFException
        //       at org.apache.fontbox.ttf.TTFDataStream.readUnsignedShort(TTFDataStream.java:154)
        //       at org.apache.fontbox.ttf.TTFDataStream.readSignedShort(TTFDataStream.java:201)
        //       at org.apache.fontbox.ttf.HeaderTable.read(HeaderTable.java:85)
        //   See https://diff.blue/R013 to resolve this issue.

        // Arrange
        HeaderTable headerTable = new HeaderTable();
        TrueTypeFont ttf = new TrueTypeFont(
                new RandomAccessReadDataStream( new ByteArrayInputStream( "AXAXAXAX".getBytes( "UTF-8" ) ) ) );
        RandomAccessReadDataStream data = new RandomAccessReadDataStream(
                new ByteArrayInputStream( "A\bA\bA\bA\bA\bA\bA\bA\bA\bA\bA\bA\b".getBytes( "UTF-8" ) ) );

        // Act
        headerTable.read( ttf, data );
    }
}
