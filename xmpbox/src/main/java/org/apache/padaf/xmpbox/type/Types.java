package org.apache.padaf.xmpbox.type;

public enum Types {

	Structured(false,null,null),
	DefinedType(false,null,null),

	// basic
	Text(true,null,TextType.class),
	Date(true,null,DateType.class),
	Boolean(true,null,BooleanType.class),
	Integer(true,null,IntegerType.class),
	Real(true,null,RealType.class),
	
	ProperName(true,Text,ProperNameType.class),
	Locale(true,Text,LocaleType.class),
	AgentName(true,Text,AgentNameType.class),
	GUID(true,Text,GUIDType.class),
	XPath(true,Text,XPathType.class),
	Part(true,Text,PartType.class),
	URL(true,Text,URLType.class),
	URI(true,Text,URIType.class),
	Choice(true,Text,ChoiceType.class),
	MIMEType(true,Text,MIMEType.class),
	LangAlt(true,Text,TextType.class),
	RenditionClass(true,Text,RenditionClassType.class),

	Layer(false,Structured,LayerType.class),
	Thumbnail(false,Structured,ThumbnailType.class),
	ResourceEvent(false,Structured,ResourceEventType.class),
	ResourceRef(false,Structured,ResourceRefType.class),
	Version(false,Structured,VersionType.class),
	PDFASchema(false,Structured,PDFASchemaType.class),
	PDFAField(false,Structured,PDFAFieldType.class),
	PDFAProperty(false,Structured,PDFAPropertyType.class),
	PDFAType(false,Structured,PDFATypeType.class),
	Job(false,Structured,JobType.class);

	// For defined types

	private boolean simple;
	
	private Types basic;
	
	private Class<? extends AbstractField> clz;
	
	private Types (boolean s, Types b, Class<? extends AbstractField> c) {
		this.simple = s;
		this.basic = b;
		this.clz = c;
	}

	public boolean isSimple() {
		return simple;
	}
	
	public boolean isBasic () {
		return basic==null;
	}
	
	public boolean isStructured () {
		return basic==Structured;
	}
	
	public Types getBasic () {
		return basic;
	}
	
	public Class<? extends AbstractField> getImplementingClass () {
		return clz;
	}
	
}
