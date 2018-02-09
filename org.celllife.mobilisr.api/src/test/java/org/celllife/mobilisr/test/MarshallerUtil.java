package org.celllife.mobilisr.test;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.celllife.mobilisr.api.MobilisrDto;

public class MarshallerUtil {

	public static final String UTF_8 = "UTF-8";


	public static String marshallToString(MobilisrDto dto)
			throws PropertyException, JAXBException {
		return marshallToString(dto, true);
	}

	public static String marshallToString(MobilisrDto dto, boolean format)
			throws PropertyException, JAXBException {
		String name = dto.getClass().getPackage().getName();
		Marshaller m = getMarshaller(name, format);
		StringWriter writer = new StringWriter();
		m.marshal(dto, writer);
		return writer.toString();
	}

	public static Marshaller getMarshaller(String packageName, boolean format)
			throws JAXBException, PropertyException {
		JAXBContext jc = JAXBContext.newInstance(packageName, Thread
				.currentThread().getContextClassLoader());
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_ENCODING, UTF_8);
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, format);
		return m;
	}

}
