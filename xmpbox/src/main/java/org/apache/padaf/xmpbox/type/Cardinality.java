package org.apache.padaf.xmpbox.type;

public enum Cardinality {

	Simple(false),
	Bag(true),
	Seq(true),
	Alt(true);
	
	private boolean array;
	
	private Cardinality (boolean a) {
		this.array = a;
	}
	
	public boolean isArray () {
		return this.array;
	}
	
}
