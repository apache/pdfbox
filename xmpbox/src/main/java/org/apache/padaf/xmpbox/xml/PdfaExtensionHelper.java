package org.apache.padaf.xmpbox.xml;

import java.util.List;
import java.util.Map;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.PDFAExtensionSchema;
import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.apache.padaf.xmpbox.schema.XMPSchemaFactory;
import org.apache.padaf.xmpbox.type.AbstractField;
import org.apache.padaf.xmpbox.type.AbstractSimpleProperty;
import org.apache.padaf.xmpbox.type.AbstractStructuredType;
import org.apache.padaf.xmpbox.type.ArrayProperty;
import org.apache.padaf.xmpbox.type.DefinedStructuredType;
import org.apache.padaf.xmpbox.type.PDFAFieldType;
import org.apache.padaf.xmpbox.type.PDFAPropertyType;
import org.apache.padaf.xmpbox.type.PDFASchemaType;
import org.apache.padaf.xmpbox.type.PDFATypeType;
import org.apache.padaf.xmpbox.type.PropMapping;
import org.apache.padaf.xmpbox.type.StructuredType;
import org.apache.padaf.xmpbox.type.TypeDescription;
import org.apache.padaf.xmpbox.type.TypeMapping;
import org.apache.padaf.xmpbox.xml.XmpParsingException.ErrorType;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public final class PdfaExtensionHelper {

	private PdfaExtensionHelper() {}

	public static void validateNaming (XMPMetadata meta, Element description) throws XmpParsingException {
		NamedNodeMap nnm = description.getAttributes();
		for (int i=0; i < nnm.getLength(); i++) {
			Attr attr = (Attr)nnm.item(i);
			checkNamespaceDeclaration(attr,PDFAExtensionSchema.class);
			checkNamespaceDeclaration(attr,PDFAFieldType.class);
			checkNamespaceDeclaration(attr,PDFAPropertyType.class);
			checkNamespaceDeclaration(attr,PDFASchemaType.class);
			checkNamespaceDeclaration(attr,PDFATypeType.class);
		}
	}

	private static void checkNamespaceDeclaration (Attr attr, Class<? extends AbstractStructuredType> clz) throws XmpParsingException {
		String prefix = attr.getLocalName();
		String namespace = attr.getValue();
		String cprefix = clz.getAnnotation(StructuredType.class).preferedPrefix();
		String cnamespace = clz.getAnnotation(StructuredType.class).namespace();
		// check extension 
		if (cprefix.equals(prefix) && !cnamespace.equals(namespace)) {
			throw new XmpParsingException(ErrorType.InvalidPdfaSchema, "Invalid PDF/A namespace definition");
		} // else good match
		if (cnamespace.equals(namespace) && !cprefix.equals(prefix)) {
			throw new XmpParsingException(ErrorType.InvalidPdfaSchema, "Invalid PDF/A namespace definition");
		} // else good match

	}



	public static  void populateSchemaMapping (XMPMetadata meta) 
			throws XmpParsingException {
		List<XMPSchema> schems = meta.getAllSchemas();
		TypeMapping tm = meta.getTypeMapping();
		StructuredType stPdfaExt = PDFAExtensionSchema.class.getAnnotation(StructuredType.class);
		for (XMPSchema xmpSchema : schems) {
			if (xmpSchema.getNamespace().equals(stPdfaExt.namespace())) {
				// ensure the prefix is the preferred one (cannot use other definition)
				if (!xmpSchema.getPrefix().equals(stPdfaExt.preferedPrefix())) {
					throw new XmpParsingException(ErrorType.InvalidPrefix, "Found invalid prefix for PDF/A extension, found '"+
							xmpSchema.getPrefix()+"', should be '"+stPdfaExt.preferedPrefix()+"'"
							);
				}
				// create schema and types
				PDFAExtensionSchema pes = (PDFAExtensionSchema)xmpSchema;
				ArrayProperty sp = pes.getSchemasProperty();
				for (AbstractField af: sp.getAllProperties()) {
					if (af instanceof PDFASchemaType) {
						PDFASchemaType st = (PDFASchemaType)af;
						String namespaceUri = st.getNamespaceURI();
						String prefix = st.getPrefixValue();
						ArrayProperty properties = st.getProperty();
						ArrayProperty valueTypes = st.getValueType();
						XMPSchemaFactory xsf = tm.getSchemaFactory(namespaceUri);
						// retrieve namespaces
						if (xsf==null) {
							// create namespace with no field
							tm.addNewNameSpace(namespaceUri,prefix);
							xsf = tm.getSchemaFactory(namespaceUri);
						}
						// populate value type
						if (valueTypes!=null) {
							for (AbstractField af2 : valueTypes.getAllProperties()) {
								if (af2 instanceof PDFATypeType) {
									PDFATypeType type = (PDFATypeType)af2;
									String ttype= type.getType();
									String tns = type.getNamespaceURI();
									String tprefix = type.getPrefixValue();
									String tdescription = type.getDescription();
									ArrayProperty fields = type.getFields();
									if (ttype==null || tns==null || tprefix==null || tdescription==null) {
										// all fields are mandatory
										throw new XmpParsingException(ErrorType.RequiredProperty,"Missing field in type definition");
									}
									// create the structured type
									DefinedStructuredType structuredType = new DefinedStructuredType(meta, tns, tprefix,null); // TODO maybe a name exists
									if (fields!=null) {
										List<AbstractField> definedFields = fields.getAllProperties();
										for (AbstractField af3 : definedFields) {
											if (af3 instanceof PDFAFieldType) {
												PDFAFieldType field = (PDFAFieldType)af3;
												String fName = field.getName();
												String fDescription = field.getDescription();
												String fValueType = field.getValueType();
												if (fName==null || fDescription==null || fValueType==null) {
													throw new XmpParsingException(ErrorType.RequiredProperty,"Missing field in field definition");
												}
												// create the type
												TypeDescription<AbstractSimpleProperty> vtd = tm.getSimpleDescription(fValueType);
												if (vtd!=null) {
													// a type is found
													String ftype = vtd.getType();
													structuredType.addProperty(fName, ftype);
												} else {
													// TODO could fValueType be a structured type ?
													// unknown type
													throw new XmpParsingException(ErrorType.NoValueType, "Type not defined : "+fValueType);
												}
											} // else TODO
										}
									}
									// add the structured type to list
									TypeDescription<AbstractStructuredType> td = new TypeDescription<AbstractStructuredType>(ttype, null, DefinedStructuredType.class);
									PropMapping pm = new PropMapping(structuredType.getNamespace());
									for (Map.Entry<String, String> entry : structuredType.getDefinedProperties().entrySet()) {
										pm.addNewProperty(entry.getKey(), entry.getValue());
									}
									td.setProperties(pm);
									meta.getTypeMapping().addToStructuredMaps(td,tns);
								}
							}	
						}
						// populate properties
						for (AbstractField af2 : properties.getAllProperties()) {
							if (af2 instanceof PDFAPropertyType) {
								PDFAPropertyType property = (PDFAPropertyType)af2;
								String pname = property.getName();
								String ptype = property.getValueType();
								String pdescription = property.getDescription();
								String pCategory = property.getCategory();
								// check all mandatory fields are OK
								if (pname==null || ptype==null || pdescription==null || pCategory==null) {
									// all fields are mandatory
									throw new XmpParsingException(ErrorType.RequiredProperty,"Missing field in property definition");
								}
								// check ptype existance
								String etype = ptype.equals("Lang Alt")?ptype:tm.isArrayType(ptype)?tm.getTypeInArray(ptype):ptype;
								if (tm.isSimpleType(etype) || tm.isStructuredType(etype) ) {
									xsf.getPropertyDefinition().addNewProperty(pname, ptype);
								} else {
									throw new XmpParsingException(ErrorType.NoValueType, "Type not defined : "+ptype+" ("+etype+")");
								}
								
							} // TODO unmanaged ?
						}
					} // TODO unmanaged ?
				}
			}
		}
	}


}
