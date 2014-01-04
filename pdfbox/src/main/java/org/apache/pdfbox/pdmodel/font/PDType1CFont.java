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

package org.apache.pdfbox.pdmodel.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fontbox.afm.AFMParser;
import org.apache.fontbox.afm.FontMetric;
import org.apache.fontbox.cff.AFMFormatter;
import org.apache.fontbox.cff.charset.CFFCharset;
import org.apache.fontbox.cff.encoding.CFFEncoding;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;
import org.apache.fontbox.cff.Type1FontFormatter;
import org.apache.fontbox.util.BoundingBox;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.EncodingManager;
import org.apache.pdfbox.exceptions.WrappedIOException;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * This class represents a CFF/Type2 Font (aka Type1C Font).
 * @author Villu Ruusmann
 * @version $Revision: 10.0$
 */
public class PDType1CFont extends PDSimpleFont
{
    private CFFFont cffFont = null;

    private Map<Integer, String> codeToName = new HashMap<Integer, String>();

    private Map<Integer, String> codeToCharacter = new HashMap<Integer, String>();

    private Map<String, Integer> characterToCode = new HashMap<String, Integer>();

    private FontMetric fontMetric = null;

    private Font awtFont = null;

    private Map<String, Float> glyphWidths = new HashMap<String, Float>();

    private Map<String, Float> glyphHeights = new HashMap<String, Float>();

    private Float avgWidth = null;

    private PDRectangle fontBBox = null;

    private static final Log log = LogFactory.getLog(PDType1CFont.class);

    private static final byte[] SPACE_BYTES = {(byte)32};

    private COSDictionary fontDict = null;
    
    /**
     * Constructor.
     * @param fontDictionary the corresponding dictionary
     */
    public PDType1CFont( COSDictionary fontDictionary ) throws IOException
    {
        super( fontDictionary );
        fontDict = fontDictionary;
        load();
    }

    public int encodeToCID( byte[] bytes, int offset, int length )
    {
      if (length > 2)
      {
          return -1;
      }
      int code = bytes[offset] & 0xff;
      if (length == 2)
      {
          code = code * 256 + bytes[offset+1] & 0xff;
      }
      return code;
    }
    
    /**
     * {@inheritDoc}
     */
    public float getFontWidth( byte[] bytes, int offset, int length ) throws IOException
    {
        String name = getName(bytes, offset, length);
        if( name == null && !Arrays.equals(SPACE_BYTES, bytes) )
        {
            log.debug("No name for code " + (bytes[offset] & 0xff) + " in " + this.cffFont.getName());

            return 0;
        }

        Float width = (Float)this.glyphWidths.get(name);
        if( width == null )
        {
            width = Float.valueOf(getFontMetric().getCharacterWidth(name));
            this.glyphWidths.put(name, width);
        }

        return width.floatValue();
    }

    /**
     * {@inheritDoc}
     */
    public float getFontHeight( byte[] bytes, int offset, int length ) throws IOException
    {
        String name = getName(bytes, offset, length);
        if( name == null )
        {
            log.debug("No name for code " + (bytes[offset] & 0xff) + " in " + this.cffFont.getName());

            return 0;
        }

        Float height = (Float)this.glyphHeights.get(name);
        if( height == null )
        {
            height = Float.valueOf(getFontMetric().getCharacterHeight(name));
            this.glyphHeights.put(name, height);
        }

        return height.floatValue();
    }

    private String getName( byte[] bytes, int offset, int length )
    {
        if (length > 2)
        {
            return null;
        }
        
        int code = bytes[offset] & 0xff;
        if (length == 2)
        {
            code = code * 256 + bytes[offset+1] & 0xff;
        }

        return (String)this.codeToName.get(code);
    }

    /**
     * {@inheritDoc}
     */
    public float getStringWidth( String string ) throws IOException
    {
        float width = 0;

        for( int i = 0; i < string.length(); i++ )
        {
            String character = string.substring(i, i + 1);

            Integer code = getCode(character);
            if( code == null )
            {
                log.debug("No code for character " + character);

                return 0;
            }

            width += getFontWidth(new byte[]{(byte)code.intValue()}, 0, 1);
        }

        return width;
    }

    private Integer getCode( String character )
    {
        return (Integer)this.characterToCode.get(character);
    }


    /**
     * {@inheritDoc}
     */
    public float getAverageFontWidth() throws IOException
    {
        if( this.avgWidth == null )
        {
            this.avgWidth = Float.valueOf(getFontMetric().getAverageCharacterWidth());
        }

        return this.avgWidth.floatValue();
    }

    /**
     * {@inheritDoc}
     */
    public PDRectangle getFontBoundingBox() throws IOException
    {
        if( this.fontBBox == null )
        {
            this.fontBBox = new PDRectangle(getFontMetric().getFontBBox());
        }

        return this.fontBBox;
    }

    /**
     * {@inheritDoc}
     */
    public PDMatrix getFontMatrix()
    {
        if( fontMatrix == null )
        {
            List<Number> numbers = (List<Number>)this.cffFont.getProperty("FontMatrix");
            if( numbers != null && numbers.size() == 6 )
            {
                COSArray array = new COSArray();
                for(Number number : numbers)
                {
                    array.add(new COSFloat(number.floatValue()));
                }
                fontMatrix = new PDMatrix(array);
            }
            else
            {
                super.getFontMatrix();
            }
        }
        return fontMatrix;
    }

    /**
     * {@inheritDoc}
     */    
    public Font getawtFont() throws IOException
    {
        if (awtFont == null)
        {
            this.awtFont = prepareAwtFont(this.cffFont);
        }
        return awtFont;
    }
    
    private FontMetric getFontMetric() 
    {
        if (fontMetric == null)
        {
            try
            {
                fontMetric = prepareFontMetric(cffFont);
            }
            catch (IOException exception)
            {
                log.error("An error occured while extracting the font metrics!", exception);
            }
        }
        return fontMetric;
    }

    private void load() throws IOException
    {
        byte[] cffBytes = loadBytes();

        CFFParser cffParser = new CFFParser();
        List<CFFFont> fonts = cffParser.parse(cffBytes);

        String baseFontName = getBaseFont();
        if (fonts.size() > 1 && baseFontName != null)
        {
            for (CFFFont font: fonts) 
            {
                if (baseFontName.equals(font.getName())) 
                {
                    this.cffFont = font;
                    break;
                }
            }
        }
        if (this.cffFont == null) 
        {
            this.cffFont = (CFFFont)fonts.get(0);
        }

        CFFEncoding encoding = this.cffFont.getEncoding();
        PDFEncoding pdfEncoding = new PDFEncoding(encoding);

        CFFCharset charset = this.cffFont.getCharset();
        PDFCharset pdfCharset = new PDFCharset(charset);

        Map<String,byte[]> charStringsDict = this.cffFont.getCharStringsDict();
        Map<String,byte[]> pdfCharStringsDict = new LinkedHashMap<String,byte[]>();
        pdfCharStringsDict.put(".notdef", charStringsDict.get(".notdef"));

        Map<Integer,String> codeToNameMap = new LinkedHashMap<Integer,String>();

        Collection<CFFFont.Mapping> mappings = this.cffFont.getMappings();
        for( Iterator<CFFFont.Mapping> it = mappings.iterator(); it.hasNext();)
        {
            CFFFont.Mapping mapping = it.next();
            Integer code = Integer.valueOf(mapping.getCode());
            String name = mapping.getName();
            codeToNameMap.put(code, name);
        }

        Set<String> knownNames = new HashSet<String>(codeToNameMap.values());

        Map<Integer,String> codeToNameOverride = loadOverride();
        for( Iterator<Map.Entry<Integer, String>> it = (codeToNameOverride.entrySet()).iterator(); it.hasNext();)
        {
            Map.Entry<Integer, String> entry = it.next();
            Integer code = (Integer)entry.getKey();
            String name = (String)entry.getValue();
            if(knownNames.contains(name))
            {
                codeToNameMap.put(code, name);
            }
        }

        Map<String,String> nameToCharacter;
        try
        {
            // TODO remove access by reflection
            Field nameToCharacterField = Encoding.class.getDeclaredField("NAME_TO_CHARACTER");
            nameToCharacterField.setAccessible(true);
            nameToCharacter = (Map<String,String>)nameToCharacterField.get(null);
        }
        catch( Exception e )
        {
            throw new RuntimeException(e);
        }

        for( Iterator<Map.Entry<Integer,String>> it = (codeToNameMap.entrySet()).iterator(); it.hasNext();)
        {
            Map.Entry<Integer,String> entry = it.next();
            Integer code = (Integer)entry.getKey();
            String name = (String)entry.getValue();
            String uniName = "uni";
            String character = (String)nameToCharacter.get(name);
            if( character != null )
            {
                for( int j = 0; j < character.length(); j++ )
                {
                    uniName += hexString(character.charAt(j), 4);
                }
            }
            else
            {
                uniName += hexString(code.intValue(), 4);
                character = String.valueOf((char)code.intValue());
            }
            pdfEncoding.register(code.intValue(), code.intValue());
            pdfCharset.register(code.intValue(), uniName);
            this.codeToName.put(code, uniName);
            this.codeToCharacter.put(code, character);
            this.characterToCode.put(character, code);
            pdfCharStringsDict.put(uniName, charStringsDict.get(name));
        }

        this.cffFont.setEncoding(pdfEncoding);
        this.cffFont.setCharset(pdfCharset);
        charStringsDict.clear();
        charStringsDict.putAll(pdfCharStringsDict);
        Number defaultWidthX = (Number)this.cffFont.getProperty("defaultWidthX");
        this.glyphWidths.put(null, Float.valueOf(defaultWidthX.floatValue()));
    }

    private byte[] loadBytes() throws IOException
    {
        PDFontDescriptor fd = getFontDescriptor();
        if( fd != null && fd instanceof PDFontDescriptorDictionary)
        {
            PDStream ff3Stream = ((PDFontDescriptorDictionary)fd).getFontFile3();
            if( ff3Stream != null )
            {
                ByteArrayOutputStream os = new ByteArrayOutputStream();

                InputStream is = ff3Stream.createInputStream();
                try
                {
                    byte[] buf = new byte[512];
                    while(true)
                    {
                        int count = is.read(buf);
                        if( count < 0 )
                        {
                            break;
                        }
                        os.write(buf, 0, count);
                    }
                }
                finally
                {
                    is.close();
                }

                return os.toByteArray();
            }
        }

        throw new IOException();
    }

    private Map<Integer,String> loadOverride() throws IOException
    {
        Map<Integer,String> result = new LinkedHashMap<Integer,String>();
        COSBase encoding = fontDict.getDictionaryObject(COSName.ENCODING);
        if( encoding instanceof COSName )
        {
            COSName name = (COSName)encoding;
            result.putAll(loadEncoding(name));
        }
        else if( encoding instanceof COSDictionary )
        {
            COSDictionary encodingDic = (COSDictionary)encoding;
            COSName baseName = (COSName)encodingDic.getDictionaryObject(COSName.BASE_ENCODING);
            if( baseName != null )
            {
                result.putAll(loadEncoding(baseName));
            }
            COSArray differences = (COSArray)encodingDic.getDictionaryObject(COSName.DIFFERENCES);
            if( differences != null )
            {
                result.putAll(loadDifferences(differences));
            }
        }

        return result;
    }

    private Map<Integer,String> loadEncoding(COSName name) throws IOException
    {
        Map<Integer,String> result = new LinkedHashMap<Integer,String>();
        Encoding encoding = EncodingManager.INSTANCE.getEncoding(name);
        for( Iterator<Map.Entry<Integer,String>> it = (encoding.getCodeToNameMap().entrySet()).iterator();
                    it.hasNext();)
        {
            Map.Entry<Integer,String> entry = it.next();
            result.put(entry.getKey(), (entry.getValue()));
        }

        return result;
    }

    private Map<Integer,String> loadDifferences(COSArray differences)
    {
        Map<Integer,String> result = new LinkedHashMap<Integer,String>();
        Integer code = null;
        for( int i = 0; i < differences.size(); i++)
        {
            COSBase element = differences.get(i);
            if( element instanceof COSNumber )
            {
                COSNumber number = (COSNumber)element;
                code = Integer.valueOf(number.intValue());
            } 
            else 
            {
                if( element instanceof COSName )
                {
                    COSName name = (COSName)element;
                    result.put(code, name.getName());
                    code = Integer.valueOf(code.intValue() + 1);
                }
            }
        }
        return result;
    }

    
    private static String hexString( int code, int length )
    {
        String string = Integer.toHexString(code);
        while(string.length() < length)
        {
            string = ("0" + string);
        }

        return string;
    }

    private FontMetric prepareFontMetric( CFFFont font ) throws IOException
    {
        byte[] afmBytes = AFMFormatter.format(font);

        InputStream is = new ByteArrayInputStream(afmBytes);
        try
        {
            AFMParser afmParser = new AFMParser(is);
            afmParser.parse();

            FontMetric result = afmParser.getResult();

            // Replace default FontBBox value with a newly computed one
            BoundingBox bounds = result.getFontBBox();
            List<Integer> numbers = Arrays.asList(
                    Integer.valueOf((int)bounds.getLowerLeftX()),
                    Integer.valueOf((int)bounds.getLowerLeftY()),
                    Integer.valueOf((int)bounds.getUpperRightX()),
                    Integer.valueOf((int)bounds.getUpperRightY())
                );
            font.addValueToTopDict("FontBBox", numbers);

            return result;
        }
        finally
        {
            is.close();
        }
    }

    private static Font prepareAwtFont( CFFFont font ) throws IOException
    {
        byte[] type1Bytes = Type1FontFormatter.format(font);

        InputStream is = new ByteArrayInputStream(type1Bytes);
        try
        {
            return Font.createFont(Font.TYPE1_FONT, is);
        }
        catch( FontFormatException ffe )
        {
            throw new WrappedIOException(ffe);
        }
        finally
        {
            is.close();
        }
    }

    /**
     * This class represents a PDFEncoding.
     *
     */
    private static class PDFEncoding extends CFFEncoding
    {

        private PDFEncoding( CFFEncoding parent )
        {
            Iterator<Entry> parentEntries = parent.getEntries().iterator();
            while(parentEntries.hasNext())
            {
                addEntry(parentEntries.next());
            }
        }

        public boolean isFontSpecific()
        {
            return true;
        }

    }

    /**
     * This class represents a PDFCharset.
     *
     */
    private static class PDFCharset extends CFFCharset
    {
        private PDFCharset( CFFCharset parent )
        {
            Iterator<Entry> parentEntries = parent.getEntries().iterator();
            while(parentEntries.hasNext())
            {
                addEntry(parentEntries.next());
            }
        }

        public boolean isFontSpecific()
        {
            return true;
        }

    }

}
