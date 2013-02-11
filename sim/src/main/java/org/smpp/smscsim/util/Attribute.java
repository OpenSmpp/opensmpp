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

/**
 * Represents an attribute (field) of a <code>Record</code>. Each attribute
 * has name and a value. Values are textual, i.e. if you want to use
 * this class to work with integral values, you have to cast the values
 * explicitly.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 * @see Record
 * @see Table
 */
public class Attribute {
	/**
	 * The name of the attribute
	 */
	private String name = null;

	/**
	 * The value of the attribute.
	 */
	private String value = null;

	/**
	 * Default constructor initialises <code>name</code> and <code>value</code>
	 * of the attribute to empty (null) values.
	 * @see #setName(String)
	 * @see #setValue(String)
	 */
	public Attribute() {
	}

	/**
	 * If you know the name but not the value yet, use this constructor.
	 * @see #setValue(String)
	 */
	public Attribute(String name) {
		this.name = name;
	}

	/**
	 * Initialises the attribute's both <code>name</code> and <code>value</code>
	 */
	public Attribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Sets the name of the attribute.
	 * @param name the new value for the name of the attribute.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the value of the attribute.
	 * @param value the new vlaue of the attribute.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Returns the name of the attribute.
	 * @return the name of the attribute
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the current value of the attribute.
	 * @return the current value of the attribute
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Compares the name of the attribute to the provided value.
	 * The comparison is case sensitive, i.e. 'Password' and 'password' are
	 * different names!
	 * @param name the name to compare the attribute's name to
	 * @return if the name of the attribute is equal to the provided name
	 */
	public boolean nameEquals(String name) {
		if (this.name != null) {
			return this.name.equals(name);
		} else {
			return name == null; // nulls are equal
		}
	}

	/**
	 * Compares two attributes if their names and values are equal.
	 * @param attr the attribute to compare this attribute to
	 * @return if the attribute's name &amp; value are equal to those of
	 *         the provided
	 */
	public boolean equals(Attribute attr) {
		if (attr != null) {
			if (nameEquals(attr.getName())) {
				if (this.value != null) {
					return this.value.equals(value);
				} else {
					return value == null; // nulls are equal
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
