AI2 Fork of PdfBox
==================

This fork exists because some projects (science-parse) depend on the latest release of Apache PdfBox and some projects (figure-extractor) depend on a pre-release version. This is a fork of the pre-release version (https://github.com/apache/pdfbox) with packages renamed so that we may use both versions.

To release

    pushd parent && mvn install && popd
    pushd fontbox-ai2 && mvn install && popd
    pushd pdfbox-ai2 && mvn install && popd
