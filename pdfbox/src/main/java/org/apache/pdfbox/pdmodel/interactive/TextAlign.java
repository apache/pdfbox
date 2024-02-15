package org.apache.pdfbox.pdmodel.interactive;

public enum TextAlign
{
    LEFT(0), CENTER(1), RIGHT(2), JUSTIFY(4);

    private final int alignment;

    private TextAlign(int alignment)
    {
        this.alignment = alignment;
    }

    int getTextAlign()
    {
        return alignment;
    }

    public static TextAlign valueOf(int alignment)
    {
        for (TextAlign textAlignment : TextAlign.values())
        {
            if (textAlignment.getTextAlign() == alignment)
            {
                return textAlignment;
            }
        }
        return TextAlign.LEFT;
    }
}
