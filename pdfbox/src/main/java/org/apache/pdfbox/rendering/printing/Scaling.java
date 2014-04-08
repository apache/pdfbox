package org.apache.pdfbox.rendering.printing;

/**
 * Scale of the image on printed pages.
 *
 * @author John Hewson
 */
public enum Scaling
{
    /** Print the image at 100% scale. */
    ACTUAL_SIZE,

    /** Shrink the image to fit the page, if needed. */
    SHRINK_TO_FIT,

    /** Stretch the image to fill the page, if needed. */
    STRETCH_TO_FIT,

    /** Stretch or shrink the image to fill the page, as needed. */
    SCALE_TO_FIT
}
