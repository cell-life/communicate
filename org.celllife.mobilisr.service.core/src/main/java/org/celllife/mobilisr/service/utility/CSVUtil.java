package org.celllife.mobilisr.service.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import liquibase.csv.opencsv.CSVReader;

import org.apache.commons.io.IOUtils;
import org.celllife.mobilisr.exception.ImportException;

public class CSVUtil {
	
	public static List<List<String>> readCSVFixedRecordLength(String filePath, int maxRead) {
		int recordsRead = 0;
		List<List<String>> csvDataList = new ArrayList<List<String>>();
		try {

			File file = new File(filePath);
			FileInputStream inputStream = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

			BufferedReader buffRead = new BufferedReader(inputStreamReader);
			CSVReader reader = new CSVReader(buffRead);
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				List<String> line = new ArrayList<String>();
				recordsRead++;
				for (int i = 0; i < nextLine.length; i++) {
					if (nextLine[i].equals("")) {
						line.add("  ");
					}
					else {
						line.add(nextLine[i]);
					}
				}
				csvDataList.add(line);

				if (maxRead > 0 && recordsRead >= maxRead) {
					break;
				}
			}
			
			reader.close();
			IOUtils.closeQuietly(buffRead);
			IOUtils.closeQuietly(inputStreamReader);
			
		}
		catch (IOException e) {
			csvDataList = null;
			throw new ImportException("Unable to read data from CSV file.", e);
		}

		return csvDataList;
	}
	
	public static int countLines(String filename) {
		try {
			LineNumberReader reader = new LineNumberReader(new FileReader(filename));			
			reader.setLineNumber(1);
			IOUtils.readLines(reader);
			IOUtils.closeQuietly(reader);
			return reader.getLineNumber();
		} catch (Exception e) {
			throw new ImportException("Unable to read import file",e);
		}
	}

	public static int asInteger(String string) {
		return Integer.parseInt(string);
	}

	public static Date asTime(String string) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		return sdf.parse(string);
	}
}
