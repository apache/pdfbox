module org.apache.pdfbox.debugger {
    requires org.apache.pdfbox;
    requires java.desktop;
    requires org.apache.pdfbox.io;
    requires org.apache.logging.log4j;
    requires org.apache.fontbox;
    requires info.picocli;
    requires java.prefs;
    requires org.apache.logging.log4j.core;
    exports org.apache.pdfbox.debugger;
    exports org.apache.pdfbox.debugger.colorpane;
    exports org.apache.pdfbox.debugger.flagbitspane;
    exports org.apache.pdfbox.debugger.fontencodingpane;
    exports org.apache.pdfbox.debugger.hexviewer;
    exports org.apache.pdfbox.debugger.pagepane;
    exports org.apache.pdfbox.debugger.streampane;
    exports org.apache.pdfbox.debugger.streampane.tooltip;
    exports org.apache.pdfbox.debugger.stringpane;
    exports org.apache.pdfbox.debugger.treestatus;
    exports org.apache.pdfbox.debugger.ui;
    exports org.apache.pdfbox.debugger.ui.textsearcher;
}