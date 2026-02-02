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
package org.apache.pdfbox.pdmodel.fdf;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionFactory;
import org.apache.pdfbox.pdmodel.interactive.action.PDAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.util.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This represents an FDF field that is part of the FDF document.
 *
 * @author Ben Litchfield
 */
public class FDFField implements COSObjectable
{
    private final COSDictionary field;

    /**
     * Default constructor.
     */
    public FDFField()
    {
        field = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param f The FDF field.
     */
    public FDFField(COSDictionary f)
    {
        field = f;
    }

    /**
     * This will create an FDF field from an XFDF XML document.
     *
     * @param fieldXML The XML document that contains the XFDF data.
     * @throws IOException If there is an error reading from the dom.
     */
    public FDFField(Element fieldXML) throws IOException
    {
        this();
        this.setPartialFieldName(fieldXML.getAttribute("name"));
        NodeList nodeList = fieldXML.getChildNodes();
        List<FDFField> kids = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Node node = nodeList.item(i);
            if (node instanceof Element)
            {
                Element child = (Element) node;
                switch (child.getTagName())
                {
                    case "value":
                        setValue(XMLUtil.getNodeValue(child));
                        break;
                    case "value-richtext":
                        setRichText(new COSString(XMLUtil.getNodeValue(child)));
                        break;
                    case "field":
                        kids.add(new FDFField(child));
                        break;
                    default:
                        break;
                }
            }
        }
        if (kids.size() > 0)
        {
            setKids(kids);
        }

    }

    /**
     * This will write this element as an XML document.
     *
     * @param output The stream to write the xml to.
     *
     * @throws IOException If there is an error writing the XML.
     */
    public void writeXML(Writer output) throws IOException
    {
        output.write("<field name=\"");
        output.write(getPartialFieldName());
        output.write("\">\n");

        Object value = getValue();

        if (value instanceof String)
        {
            output.write("<value>");
            output.write(escapeXML((String) value));
            output.write("</value>\n");
        }
        else if (value instanceof List)
        {
            List<String> items = (List<String>) value;
            for (String item : items)
            {
                output.write("<value>");
                output.write(escapeXML(item));
                output.write("</value>\n");
            }
        }

        String rt = getRichText();
        if (rt != null)
        {
            output.write("<value-richtext>");
            output.write(escapeXML(rt));
            output.write("</value-richtext>\n");
        }
        List<FDFField> kids = getKids();
        if (kids != null)
        {
            for (FDFField kid : kids)
            {
                kid.writeXML(output);
            }
        }
        output.write("</field>\n");
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return field;
    }

    /**
     * This will get the list of kids. This will return a list of FDFField objects.
     * This will return null if the underlying list is null.
     *
     * @return The list of kids.
     */
    public List<FDFField> getKids()
    {
        COSArray kids = field.getCOSArray(COSName.KIDS);
        if (kids != null)
        {
            List<FDFField> actuals = new ArrayList<>(kids.size());
            for (int i = 0; i < kids.size(); i++)
            {
                actuals.add(new FDFField((COSDictionary) kids.getObject(i)));
            }
            return new COSArrayList<>(actuals, kids);
        }
        return null;
    }

    /**
     * This will set the list of kids.
     *
     * @param kids A list of FDFField objects.
     */
    public final void setKids(List<FDFField> kids)
    {
        field.setItem(COSName.KIDS, new COSArray(kids));
    }

    /**
     * This will get the "T" entry in the field dictionary. A partial field name.
     * Where the fully qualified field name is a concatenation of the parent's fully qualified
     * field name and "." as a separator. For example<br>
     * Address.State<br>
     * Address.City<br>
     *
     * @return The partial field name.
     */
    public String getPartialFieldName()
    {
        return field.getString(COSName.T);
    }

    /**
     * This will set the partial field name.
     *
     * @param partial The partial field name.
     */
    public void setPartialFieldName(String partial)
    {
        field.setString(COSName.T, partial);
    }

    /**
     * This will get the value for the field. The return type will either be: <ul>
     * <li>String: for Checkboxes, Radio Button, Textfields</li>
     * <li>List&lt;String&gt;: for a Choice Field</li>
     * </ul>
     * @return The value of the field.
     * @throws IOException If there is an error getting the value.
     */
    public Object getValue() throws IOException
    {
        COSBase value = field.getDictionaryObject(COSName.V);
        if (value instanceof COSName)
        {
            return ((COSName) value).getName();
        }
        else if (value instanceof COSArray)
        {
            return ((COSArray) value).toCOSStringStringList();
        }
        else if (value instanceof COSString)
        {
            return ((COSString) value).getString();
        }
        else if (value instanceof COSStream)
        {
            return ((COSStream) value).toTextString();
        }
        else if (value != null)
        {
            throw new IOException("Error: Unknown type for field import: " + value);
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns the COS value of this field.
     *
     * @return The COS value of the field, maybe <code>null</code>.
     * @throws IOException If there is an error getting the value.
     */
    public COSBase getCOSValue() throws IOException
    {
        COSBase value = field.getDictionaryObject(COSName.V);
        if (value instanceof COSName //
                || value instanceof COSArray //
                || value instanceof COSString //
                || value instanceof COSStream)
        {
            return value;
        }
        if (value != null)
        {
            throw new IOException("Error: Unknown type for field import: " + value);
        }
        return null;
    }

    /**
     * You should pass a String, or a List&lt;String&gt; to set the value.
     *
     * @param value The value that should populate when imported.
     *
     * @throws IOException If there is an error setting the value.
     */
    public void setValue(Object value) throws IOException
    {
        COSBase cos = null;
        if (value instanceof List)
        {
            cos = COSArray.ofCOSStrings((List<String>) value);
        }
        else if (value instanceof String)
        {
            cos = new COSString((String) value);
        }
        else if (value instanceof COSObjectable)
        {
            cos = ((COSObjectable) value).getCOSObject();
        }
        else if (value != null)
        {
            throw new IOException("Error: Unknown type for field import: " + value);
        }
        field.setItem(COSName.V, cos);
    }

    /**
     * Sets the COS value of this field.
     *
     * @param value COS value.
     */
    public void setValue(COSBase value)
    {
        field.setItem(COSName.V, value);
    }

    /**
     * This will get the 'Ff' entry of the FDF field dictionary.
     * If it is not present then this method will return null.
     *
     * @return The field flags.
     */
    public Integer getFieldFlags()
    {
        Integer retval = null;
        COSNumber ff = (COSNumber) field.getDictionaryObject(COSName.FF);
        if (ff != null)
        {
            retval = ff.intValue();
        }
        return retval;
    }

    /**
     * This will set the field flags that are associated with this field.
     * The 'Ff' entry in the FDF field dictionary.
     *
     * @param ff The new value for the field flags.
     */
    public void setFieldFlags(Integer ff)
    {
        COSInteger value = null;
        if (ff != null)
        {
            value = COSInteger.get(ff);
        }
        field.setItem(COSName.FF, value);
    }

    /**
     * This will set the field flags that are associated with this field.
     * The 'Ff' entry in the FDF field dictionary.
     *
     * @param ff The new value for the field flags.
     */
    public void setFieldFlags(int ff)
    {
        field.setInt(COSName.FF, ff);
    }

    /**
     * This will get the 'SetFf' entry of the FDF field dictionary.
     * If it is not present then this method will return null.
     *
     * @return The field flags.
     */
    public Integer getSetFieldFlags()
    {
        Integer retval = null;
        COSNumber ff = (COSNumber) field.getDictionaryObject(COSName.SET_FF);
        if (ff != null)
        {
            retval = ff.intValue();
        }
        return retval;
    }

    /**
     * This will set the field flags that are associated with this field.
     * The 'SetFf' entry in the FDF field dictionary.
     *
     * @param ff The new value for the "set field flags".
     */
    public void setSetFieldFlags(Integer ff)
    {
        COSInteger value = null;
        if (ff != null)
        {
            value = COSInteger.get(ff);
        }
        field.setItem(COSName.SET_FF, value);
    }

    /**
     * This will set the field flags that are associated with this field.
     * The 'SetFf' entry in the FDF field dictionary.
     *
     * @param ff The new value for the "set field flags".
     */
    public void setSetFieldFlags(int ff)
    {
        field.setInt(COSName.SET_FF, ff);
    }

    /**
     * This will get the 'ClrFf' entry of the FDF field dictionary.
     * If it is not present then this method will return null.
     *
     * @return The field flags.
     */
    public Integer getClearFieldFlags()
    {
        Integer retval = null;
        COSNumber ff = (COSNumber) field.getDictionaryObject(COSName.CLR_FF);
        if (ff != null)
        {
            retval = ff.intValue();
        }
        return retval;
    }

    /**
     * This will set the field flags that are associated with this field.
     * The 'ClrFf' entry in the FDF field dictionary.
     *
     * @param ff The new value for the "clear field flags".
     */
    public void setClearFieldFlags(Integer ff)
    {
        COSInteger value = null;
        if (ff != null)
        {
            value = COSInteger.get(ff);
        }
        field.setItem(COSName.CLR_FF, value);
    }

    /**
     * This will set the field flags that are associated with this field.
     * The 'ClrFf' entry in the FDF field dictionary.
     *
     * @param ff The new value for the "clear field flags".
     */
    public void setClearFieldFlags(int ff)
    {
        field.setInt(COSName.CLR_FF, ff);
    }

    /**
     * This will get the 'F' entry of the FDF field dictionary.
     * If it is not present then this method will return null.
     *
     * @return The widget field flags.
     */
    public Integer getWidgetFieldFlags()
    {
        Integer retval = null;
        COSNumber f = (COSNumber) field.getDictionaryObject(COSName.F);
        if (f != null)
        {
            retval = f.intValue();
        }
        return retval;
    }

    /**
     * This will set the widget field flags that are associated with this field.
     * The 'F' entry in the FDF field dictionary.
     *
     * @param f The new value for the field flags.
     */
    public void setWidgetFieldFlags(Integer f)
    {
        COSInteger value = null;
        if (f != null)
        {
            value = COSInteger.get(f);
        }
        field.setItem(COSName.F, value);
    }

    /**
     * This will set the field flags that are associated with this field.
     * The 'F' entry in the FDF field dictionary.
     *
     * @param f The new value for the field flags.
     */
    public void setWidgetFieldFlags(int f)
    {
        field.setInt(COSName.F, f);
    }

    /**
     * This will get the 'SetF' entry of the FDF field dictionary.
     * If it is not present then this method will return null.
     *
     * @return The field flags.
     */
    public Integer getSetWidgetFieldFlags()
    {
        Integer retval = null;
        COSNumber ff = (COSNumber) field.getDictionaryObject(COSName.SET_F);
        if (ff != null)
        {
            retval = ff.intValue();
        }
        return retval;
    }

    /**
     * This will set the widget field flags that are associated with this field.
     * The 'SetF' entry in the FDF field dictionary.
     *
     * @param ff The new value for the "set widget field flags".
     */
    public void setSetWidgetFieldFlags(Integer ff)
    {
        COSInteger value = null;
        if (ff != null)
        {
            value = COSInteger.get(ff);
        }
        field.setItem(COSName.SET_F, value);
    }

    /**
     * This will set the widget field flags that are associated with this field.
     * The 'SetF' entry in the FDF field dictionary.
     *
     * @param ff The new value for the "set widget field flags".
     */
    public void setSetWidgetFieldFlags(int ff)
    {
        field.setInt(COSName.SET_F, ff);
    }

    /**
     * This will get the 'ClrF' entry of the FDF field dictionary.
     * If it is not present then this method will return null.
     *
     * @return The widget field flags.
     */
    public Integer getClearWidgetFieldFlags()
    {
        Integer retval = null;
        COSNumber ff = (COSNumber) field.getDictionaryObject(COSName.CLR_F);
        if (ff != null)
        {
            retval = ff.intValue();
        }
        return retval;
    }

    /**
     * This will set the field flags that are associated with this field.
     * The 'ClrF' entry in the FDF field dictionary.
     *
     * @param ff The new value for the "clear widget field flags".
     */
    public void setClearWidgetFieldFlags(Integer ff)
    {
        COSInteger value = null;
        if (ff != null)
        {
            value = COSInteger.get(ff);
        }
        field.setItem(COSName.CLR_F, value);
    }

    /**
     * This will set the field flags that are associated with this field.
     * The 'ClrF' entry in the FDF field dictionary.
     *
     * @param ff The new value for the "clear field flags".
     */
    public void setClearWidgetFieldFlags(int ff)
    {
        field.setInt(COSName.CLR_F, ff);
    }

    /**
     * This will get the appearance dictionary that specifies the appearance of a pushbutton field.
     *
     * @return The AP entry of this dictionary.
     */
    public PDAppearanceDictionary getAppearanceDictionary()
    {
        COSDictionary dict = field.getCOSDictionary(COSName.AP);
        return dict != null ? new PDAppearanceDictionary(dict) : null;
    }

    /**
     * This will set the appearance dictionary.
     *
     * @param ap The appearance dictionary.
     */
    public void setAppearanceDictionary(PDAppearanceDictionary ap)
    {
        field.setItem(COSName.AP, ap);
    }

    /**
     * This will get named page references.
     *
     * @return The named page references or <code>null</code>.
     */
    public FDFNamedPageReference getAppearanceStreamReference()
    {
        COSDictionary ref = field.getCOSDictionary(COSName.AP_REF);
        return ref != null ? new FDFNamedPageReference(ref) : null;
    }

    /**
     * This will set the named page references.
     *
     * @param ref The named page references.
     */
    public void setAppearanceStreamReference(FDFNamedPageReference ref)
    {
        field.setItem(COSName.AP_REF, ref);
    }

    /**
     * This will get the icon fit that is associated with this field.
     *
     * @return The 'IF' entry.
     */
    public FDFIconFit getIconFit()
    {
        COSDictionary dic = field.getCOSDictionary(COSName.IF);
        return dic != null ? new FDFIconFit(dic) : null;
    }

    /**
     * This will set the icon fit 'IF' entry.
     *
     * @param fit The icon fit object.
     */
    public void setIconFit(FDFIconFit fit)
    {
        field.setItem(COSName.IF, fit);
    }

    /**
     * This will return a list of options for a choice field.
     * The values in the list will be 1 of 2 types:<ul>
     *     <li><code>String</code> </li>
     *     <li>{@link FDFOptionElement}</li>
     * </ul>
     *
     * @return A list of all options.
     */
    public List<Object> getOptions()
    {
        COSArray array = field.getCOSArray(COSName.OPT);
        if (array != null)
        {
            List<Object> objects = new ArrayList<>(array.size());
            for (int i = 0; i < array.size(); i++)
            {
                COSBase next = array.getObject(i);
                if (next instanceof COSString)
                {
                    objects.add(((COSString) next).getString());
                }
                else
                {
                    COSArray value = (COSArray) next;
                    objects.add(new FDFOptionElement(value));
                }
            }
            return new COSArrayList<>(objects, array);
        }
        return null;
    }

    /**
     * This will set the options for the choice field.
     * The objects in the list should either be <code>String</code> or {@link FDFOptionElement}.
     *
     * @param options The options to set.
     */
    public void setOptions(List<Object> options)
    {
        COSArray value = COSArrayList.converterToCOSArray(options);
        field.setItem(COSName.OPT, value);
    }

    /**
     * This will get the action that is associated with this field.
     *
     * @return The 'A' entry in the FDF field dictionary.
     */
    public PDAction getAction()
    {
        return PDActionFactory.createAction(field.getCOSDictionary(COSName.A));
    }

    /**
     * This will set the action that is associated with this field.
     *
     * @param a The new action.
     */
    public void setAction(PDAction a)
    {
        field.setItem(COSName.A, a);
    }

    /**
     * This will get a list of additional actions that will get executed based on events.
     *
     * @return The 'AA' entry in this FDF field dictionary or null.
     */
    public PDAdditionalActions getAdditionalActions()
    {
        COSDictionary dict = field.getCOSDictionary(COSName.AA);
        return dict != null ? new PDAdditionalActions(dict) : null;
    }

    /**
     * This will set the additional actions that are associated with this field.
     *
     * @param aa The additional actions.
     */
    public void setAdditionalActions(PDAdditionalActions aa)
    {
        field.setItem(COSName.AA, aa);
    }

    /**
     * This will set the rich text that is associated with this field.
     *
     * @return The rich text XHTML stream.
     */
    public String getRichText()
    {
        COSBase rv = field.getDictionaryObject(COSName.RV);
        if (rv == null)
        {
            return null;
        }
        else if (rv instanceof COSString)
        {
            return ((COSString) rv).getString();
        }
        else
        {
            return ((COSStream) rv).toTextString();
        }
    }

    /**
     * This will set the rich text value.
     *
     * @param rv The rich text value for the stream.
     */
    public void setRichText(COSString rv)
    {
        field.setItem(COSName.RV, rv);
    }

    /**
     * This will set the rich text value.
     *
     * @param rv The rich text value for the stream.
     */
    public void setRichText(COSStream rv)
    {
        field.setItem(COSName.RV, rv);
    }

    /**
     * Escape special characters.
     *
     * @param input the string to be escaped.
     *
     * @return the resulting string
     */
    private String escapeXML(String input)
    {
        StringBuilder escapedXML = new StringBuilder();
        for (int i = 0; i < input.length(); i++)
        {
            char c = input.charAt(i);
            switch (c)
            {
            case '<':
                escapedXML.append("&lt;");
                break;
            case '>':
                escapedXML.append("&gt;");
                break;
            case '\"':
                escapedXML.append("&quot;");
                break;
            case '&':
                escapedXML.append("&amp;");
                break;
            case '\'':
                escapedXML.append("&apos;");
                break;
            default:
                if (c > 0x7e)
                {
                    escapedXML.append("&#").append((int) c).append(';');
                }
                else
                {
                    escapedXML.append(c);
                }
            }
        }
        return escapedXML.toString();
    }
}
