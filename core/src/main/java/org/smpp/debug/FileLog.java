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
package org.smpp.debug;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.1 $
 */

public class FileLog {
	private boolean active = false;
	// filesize defaults to 1 MB
	private long filesize = 1024000;
	private String fileExtension = "";
	private String endLine = ""; // the system-specific end-of-line character
	private String fileName = "";
	private String fileDir = "";

	private File logFile;
	private BufferedWriter fileOut;
	private char currentFileIndex = '0';

	@SuppressWarnings("unused")
	private static String WEEK_DAYS[] = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
	@SuppressWarnings("unused")
	private static String MONTHS[] =
		{ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	public FileLog() {
		endLine = System.getProperty("line.separator", "");
	}

	public FileLog(String dir, String name) {
		endLine = System.getProperty("line.separator", "");
		setCurrentFileIndex('0');
		setFileDir(dir);
		if (name != "") {
			setFileName(name.substring(0, name.length() - 4));
			setFileExtension(name.substring(name.length() - 4, name.length()));
			openFile(getFullName());
		} else {
			deactivate();
		}
	}

	public void finalize() {
		try {
			fileOut.flush();
			fileOut.close();
		} catch (IOException e) {
			// too late for exceptions
		}
	}

	protected void setCurrentFileIndex(char c) {
		currentFileIndex = c;
	}
	protected void setFileDir(String fd) {
		fileDir = fd;
	}
	protected void setFileName(String fnr) {
		fileName = fnr;
	}
	public void setFileSize(int size) {
		filesize = size;
	}
	protected void setFileExtension(String extension) {
		fileExtension = extension;
	}
	protected char getCurrentFileIndex() {
		return currentFileIndex;
	}
	protected String getFileDir() {
		return fileDir;
	}
	protected String getFileName() {
		return fileName;
	}
	protected String getFileExtension() {
		return (fileExtension);
	}
	protected String getFullName() {
		return new String(getFileDir() + getFileName() + getFileExtension() + getCurrentFileIndex());
	}

	public void activate() {
		active = true;
	}
	public void deactivate() {
		active = false;
	}
	public boolean isActive() {
		return active;
	}

	protected void openFile(String filename) {
		try {
			logFile = new File(filename);
			fileOut = new BufferedWriter(new FileWriter(filename, false));
		} catch (Exception e) {
			System.err.println("The file stream could not be opened in openFile() " + e);
		}
	}

	private void changeFile() {
		if (getCurrentFileIndex() < '2') {
			setCurrentFileIndex((char) ((int) (getCurrentFileIndex() + 1)));
		} else {
			setCurrentFileIndex('0');
		}

		try {
			fileOut = new BufferedWriter(new FileWriter(getFullName()));
		} catch (Exception e) {
			System.err.println("Error setting fileOut to next file in changeFile() " + e);
		}
		logFile = new File(getFullName());
	}

	synchronized protected void genericWrite(String msg) {
		long size = logFile.length();
		try {
			if (size >= filesize) {
				changeFile();
			}
			String tmStamp = getLineTimeStamp();
			fileOut.write(tmStamp + " " + msg + endLine);
			fileOut.flush();
		} catch (Exception e) {
			System.err.println("Event log failure in genericWrite() " + e);
		}
	}

	public static String getLineTimeStamp() {
		String retDate = "";
		String padString = "";
		Calendar fullCalendar = Calendar.getInstance();

		//for naming Validations, the array goes from 0-6 DAY_OF_WEEK is 1-7	
		padString = Integer.toString(fullCalendar.get(Calendar.HOUR));
		retDate += zeroPad(2, padString);
		padString = Integer.toString(fullCalendar.get(Calendar.MINUTE));
		retDate += ":" + zeroPad(2, padString);
		padString = Integer.toString(fullCalendar.get(Calendar.SECOND));
		retDate += ":" + zeroPad(2, padString);
		return retDate;
	}

	protected static String zeroPad(int length, String toPad) {
		int numberOfZeroes = length - toPad.length();
		for (int counter = 0; counter < numberOfZeroes; counter++) {
			toPad = "0" + toPad;
		}
		return toPad;
	}

	synchronized protected void blankLine() {
		try {
			if (logFile.length() < filesize) {
				fileOut.write(endLine);
				fileOut.flush();
			}
		} catch (Exception e) {
			System.err.println("Event log failure in blankLine() " + e);
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
