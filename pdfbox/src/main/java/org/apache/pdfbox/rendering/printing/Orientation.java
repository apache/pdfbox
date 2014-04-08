package org.apache.pdfbox.rendering.printing;

/**
 * Orientation of printed pages.
 *
 * @author John Hewson
 */
public enum Orientation
{
    /** Automatically select the orientation of each page based on its aspect ratio.  */
    AUTO,

    /** Print all pages as landscape. */
    LANDSCAPE,

    /** Print all pages as portrait. */
    PORTRAIT
}
