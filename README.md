AI2 Fork of PdfBox
==================

This fork exists because some projects (science-parse) depend on the latest release of Apache PdfBox and some projects (figure-extractor) depend on a pre-release version. This is a fork of the pre-release version (https://github.com/apache/pdfbox) with packages renamed so that we may use both versions.

To release

    pushd parent && mvn install && popd
    pushd fontbox-ai2 && mvn install && popd
    pushd pdfbox-ai2 && mvn install && popd

Afterwards, manually upload the artifacts into nexus:

- Go to http://utility.allenai.org:8081/nexus/#view-repositories;thirdparty~uploadPanel
- Select "Artifact Upload" tab on bottom pane
- Hit "Select POM to Upload" and select .pom file from your local .m2 directory
- Hit "Select Artifact(s) to Upload" and select .jar (and -sources.jar) from local .m2
- Hit "Add Artifact" for each jar file selected
- Hit "Upload Artifact(s)" to complete the upload
- Repeat for all three projects: parent, fontbox-ai2, pdfbox-ai2
