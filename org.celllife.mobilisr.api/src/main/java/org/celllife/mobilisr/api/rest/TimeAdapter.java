package org.celllife.mobilisr.api.rest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class TimeAdapter extends XmlAdapter<String, Date> {

	DateFormat df = new SimpleDateFormat("HH:mm:ssZ");

	public Date unmarshal(String date) throws Exception {
		return df.parse(date);
	}

	public String marshal(Date date) throws Exception {
		return df.format(date);
	}
}