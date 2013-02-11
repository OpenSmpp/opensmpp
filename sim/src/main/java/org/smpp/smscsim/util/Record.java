/*
 * Copyright (c) 1996-2001
 * Logica Mobile Networks Limited
 * All rights reserved.
 *
 * This software is distributed under Logica Open Source License Version 1.0
 * ("Licence Agreement"). You shall use it and distribute only in accordance
 * with the terms of the License Agreement.
 *
 */
package org.smpp.smscsim.util;

import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * The class <code>Record</code> represents a set of <code>Attribute</code>s.
 * It's used in <code>Table</code> class. It can represent various types of
 * data, e.g. user settings, config parameters etc. When used in
 * <code>Table</code>, different records might have different attributes
 * -- attributes with different names.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 * @see Table
 * @see Attribute
 */
public class Record {
	/**
	 * The list of the attributes of this record.
	 */
	private List<Attribute> attributes = new LinkedList<Attribute>();

	/**
	 * Only default constructor present -- the record is empty by default
	 * (doesn't contain any attributes).
	 */
	public Record() {
	}

	/**
	 * Sets value of the attribute with given name to given value.
	 * @param name the name (key) of the attribute to set
	 * @param value the new value of the attribute
	 * @see Attribute
	 */
	public synchronized void set(String name, String value) {
		Attribute attr = get(name);
		if (attr == null) {
			attr = new Attribute(name);
			attributes.add(attr);
		}
		attr.setValue(value);
	}

	/**
	 * Adds another attribute to the current record. If the attribute is
	 * already present in the record, replaces the value of the existing
	 * attribute with the value of the provided attribute.
	 * Note that this function makes a copy of the provided atribute.
	 * @param attr the new attribute
	 * @see Attribute
	 */
	public synchronized void add(Attribute attr) {
		Attribute existing = get(attr.getName());
		if (existing != null) {
			existing.setValue(attr.getValue());
		} else {
			attributes.add(new Attribute(attr.getName(), attr.getValue()));
		}
	}

	/**
	 * Finds an attribute with given name and returns it.
	 * If none is found, returns null.
	 * @param name the name of the attribute to return
	 * @return the attribute whose key is equal to the provided key
	 * @see Attribute
	 */
	public synchronized Attribute get(String name) {
		Attribute attr;
		ListIterator<Attribute> iter = attributes.listIterator(0);
		while (iter.hasNext()) {
			attr = (Attribute) iter.next();
			if (attr.nameEquals(name)) {
				return attr;
			}
		}
		return null;
	}

	/**
	 * Returns the value of the attribute with given name.
	 * If the attribute is not present in the record, null is
	 * returned.
	 * @param name the name of the attribute to return the value of
	 * @return the value of the atrribute
	 * @see Attribute
	 */
	public synchronized String getValue(String name) {
		Attribute attr = get(name);
		if (attr != null) {
			return attr.getValue();
		} else {
			return null;
		}
	}

	/**
	 * Return's the <code>i</code>th attribute from the record.
	 * The index must be in range <code>0</code> - <code>count()-1</code>.
	 * This is usefull for outputing the list of all attributes of the record
	 * in some way.
	 * @param i the index of the attribute
	 * @return the attribute on the given position
	 * @see #get(String)
	 * @see Attribute
	 */
	public Attribute get(int i) {
		return (Attribute) attributes.get(i);
	}

	/**
	 * Returns the count of attributes actually present in the record.
	 * Useful in conjuction with <code>get(int)</code>.
	 * @return count of the attributes
	 * @see #get(int)
	 */
	public int count() {
		return attributes.size();
	}

	/**
	 * Removes an attribute with given name from the record.
	 * If the attribute isn't found, nothing will happen.
	 * @param name the name of attribute to remove
	 */
	public synchronized void remove(String name) {
		Attribute toRemove = get(name);
		if (toRemove != null) {
			attributes.remove(toRemove);
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 */