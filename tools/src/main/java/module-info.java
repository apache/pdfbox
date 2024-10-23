module org.apache.pdfbox.tools {
    requires java.desktop;
    requires org.apache.logging.log4j;
    requires info.picocli;
    requires org.apache.commons.io;
    requires org.apache.pdfbox.io;
    requires org.apache.pdfbox;
    requires org.apache.pdfbox.debugger;
    requires org.apache.fontbox;

    exports org.apache.pdfbox.tools;
    exports org.apache.pdfbox.tools.imageio;
}