package org.apache.padaf.xmpbox.type;

public enum Types {
	// basic
	Text(true),
	Date(true),
	Boolean(true),
	Integer(true),
	Real(true),
	
	ProperName(true),
	Locale(true),
	AgentName(true),
	GUID(true),
	XPath(true),
	Part(true),
	URL(true),
	URI(true),
	Choice(true),
	MIMEType(true),
	LangAlt(true),
	RenditionClass(true),
	
	Layer(false),
	Thumbnail(false),
	ResourceEvent(false),
	ResourceRef(false),
	Version(false),
	PDFASchema(false),
	PDFAField(false),
	PDFAProperty(false),
	PDFAType(false),
	Job(false),
	
	// For defined types
	DefinedType(false);

	private boolean simple;
	
	private Types (boolean s) {
		this.simple = s;
	}

	public boolean isSimple() {
		return simple;
	}
	
	
	
}
