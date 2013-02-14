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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents table of <code>Record</code>s. Users can add,
 * search, replace and remove records as well as read table from
 * file and write it to a file. Different records in the table can have
 * different attributes, however if the search for record with certain
 * value of given attribute is required, then the attribute must be present
 * in all the records. Single attribute search is supported, i.e. if
 * the key is naturally represented by more than one attribute,
 * there must be an attribute which contains bothe the attributes in some way.
 * <p>
 * The table can be read and written from and to input and output stream using
 * an implementation of <code>TableParser</code> class.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 * @see Record
 * @see Attribute
 * @see TableParser
 * @see BasicTableParser
 */
public class Table {
	/**
	 * Holds all records currently present in the table.
	 */
	private List<Record> records;

	/**
	 * The name (path) of the file to load the records from.
	 * Re-user in <code>reload</code> function.
	 * @see #read(String)
	 * @see #reload()
	 */
	private String fileName;

	/**
	 * Constructs an empty table.
	 */
	public Table() {
		fileName = null;
		records = new LinkedList<Record>();
	}

	/**
	 * Constructs a table by reading it from the disk file.
	 * For parsin of the table data read from the file uses parser
	 * returned by method <code>getParser</code>.
	 * @see #getParser()
	 * @see TableParser
	 * @see BasicTableParser
	 */
	public Table(String fileName) throws FileNotFoundException, IOException {
		this.fileName = fileName;
		read(fileName);
	}

	/**
	 * Adds one record to the table. No checking on duplicates is
	 * performed as the name of the key attribute is not provided.
	 * @param record the record to add
	 */
	public synchronized void add(Record record) {
		records.add(record);
	}

	/**
	 * Adds one record to the table. The checking on duplicates is
	 * performed; if a record with the same key is already present in the
	 * table, it's replaced with this record. Order of the records isn't
	 * ensured.
	 * @param record the record to add
	 * @param key the key attribute for checking the uniquenes
	 * @see #replace(Record,Attribute)
	 */
	public synchronized void add(Record record, Attribute key) {
		replace(record, key);
	}

	/**
	 * Replaces a record with the given attribute with the provided record.
	 * If no record with the same attribute is present in the table,
	 * the provided record is added to the table.
	 * @param record the record which replaces the existing record
	 * @param oldKey the key attribute for finding the record in the table
	 */
	public synchronized void replace(Record record, Attribute oldKey) {
		Record old = find(oldKey);
		if (old != null) {
			remove(oldKey);
		}
		add(record);
	}

	/**
	 * Returns a record whose one of the attributes matches to
	 * the provided attribute. If none found, returns null.
	 * @param key the attribute used for matching
	 * @return the found record
	 */
	public synchronized Record find(Attribute key) {
		if (key != null) {
			return find(key.getName(), key.getValue());
		} else {
			return null;
		}
	}

	/**
	 * Returns record which contains an attribute with the same name
	 * as provided equal to the value as provided. If none found, returns null.
	 * The comparison of the value is case sensitive.
	 * @param name the name of attribute to check
	 * @param value the required value of the attribute
	 * @return the found record
	 */
	public synchronized Record find(String name, String value) {
		Record current;
		String currKeyValue;
		ListIterator<Record> iter = records.listIterator(0);
		while (iter.hasNext()) {
			current = (Record) iter.next();
			currKeyValue = current.getValue(name);
			if ((currKeyValue != null) && (currKeyValue.equals(value))) {
				return current;
			}
		}
		return null;
	}

	/**
	 * Removes a record whose one attribute matches to the given attribute.
	 * Nothing will happen if none is found.
	 * @param key the attribute used for matching
	 */
	public synchronized void remove(Attribute key) {
		remove(key.getName(), key.getValue());
	}

	/**
	 * Removes a record whose attribute with the same key as provided
	 * is equal to the provided value. Nothing will happen if none is found.
	 * @param key the name of the key attribute
	 * @param value the required value of the key attribute
	 */
	public synchronized void remove(String key, String value) {
		Record toRemove = find(key, value);
		if (toRemove != null) {
			records.remove(toRemove);
		}
	}

	/**
	 * Returns count of records currently present in the table.
	 * @return the count of the records
	 * @see #get(int)
	 */
	public int count() {
		return records.size();
	}

	/**
	 * Returns a record on the given position. Useful for listing of all
	 * records from the table. Records are numbered from <code>0</code>
	 * to <code>count()-1</code>.
	 * @param i the index of the record to return
	 * @return the record on the given position
	 * @see #count()
	 */
	public Record get(int i) {
		return (Record) records.get(i);
	}

	/**
	 * Loads the table from the disk file. For parsing the file
	 * uses <code>TableParser</code> implementation returned by
	 * <code>getParser</code>.
	 * @param fileName the name of the file with the table data
	 * @see #read(InputStream)
	 * @see #write(String)
	 * @see #getParser()
	 * @see BasicTableParser
	 * @see TableParser
	 */
	public synchronized void read(String fileName) throws FileNotFoundException, IOException {
		FileInputStream is = new FileInputStream(fileName);
		records = new LinkedList<Record>(); // clear current list of records
		read(is);
		is.close();
	}

	/**
	 * Loads the table from the input stream. For parsing the file
	 * uses <code>TableParser</code> implementation returned by
	 * <code>getParser</code>.
	 * @param is the input stream with the data
	 * @see #write(OutputStream)
	 * @see #getParser()
	 * @see BasicTableParser
	 * @see TableParser
	 */
	public synchronized void read(InputStream is) throws IOException {
		TableParser parser = getParser();
		parser.parse(is);
	}

	/**
	 * Re-reads the table from disk file as set up by constructor.
	 * @see #Table(String)
	 * @see #read(String)
	 */
	public synchronized void reload() throws IOException {
		read(fileName);
	}
	/**
	 * Writes table data to the disk file. For composing the data
	 * uses <code>TableParser</code> implementation returned by
	 * <code>getParser</code>. The contents of the file is deleted
	 * before writing.
	 * @param fileName the name of file to write the data to
	 * @see #write(OutputStream)
	 * @see #read(String)
	 * @see #getParser()
	 * @see BasicTableParser
	 * @see TableParser
	 */
	public synchronized void write(String fileName) throws FileNotFoundException, IOException {
		FileOutputStream os = new FileOutputStream(fileName);
		write(os);
		os.close();
	}

	/**
	 * Writes table data to the oputput stream. For composing the data
	 * uses <code>TableParser</code> implementation returned by
	 * <code>getParser</code>.
	 * @param os the output stream to write the data to
	 * @see #write(String)
	 * @see #read(InputStream)
	 * @see #getParser()
	 * @see BasicTableParser
	 * @see TableParser
	 */
	public synchronized void write(OutputStream os) throws IOException {
		TableParser parser = getParser();
		parser.compose(os);
	}

	/**
	 * Returns parser to use for parsing and composing table data form
	 * and to input and output stream. By default returns instance
	 * of <code>BasicTableParser</code>. If necessary, another implemantation
	 * of <code>TableParser</code> can be created and used.
	 * @return the parser to use for reading and writting of the table data
	 * @see TableParser
	 * @see BasicTableParser
	 */
	public TableParser getParser() {
		return new BasicTableParser(this);
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 */
