$Id$

How to generate the PDFBox website
====================================

- Install Apache Forrest (for example, Forrest Trunk)
- "ant -f publish.xml" builds the website and uploads it to the SVN
  repository.
- "svn up" in /www/incubator.apache.org/pdfbox on people.apache.org

Notes:
- "publish.xml" represents the configuration for ForrestBot which is used
  manually here until this can be automated.