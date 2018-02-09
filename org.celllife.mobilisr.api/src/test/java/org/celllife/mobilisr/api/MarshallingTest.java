package org.celllife.mobilisr.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.Assert;

import org.celllife.mobilisr.api.mock.DtoMockFactory;
import org.junit.Before;
import org.xml.sax.SAXException;

public abstract class MarshallingTest {

	private static final String UTF_8 = "UTF-8";
	protected static final File TEST_FILE = new File("target/test.xml");

	public MarshallingTest() {
		super();
	}

	@Before
	public void setUp() throws Exception {
		if (TEST_FILE.exists()) {
			if (!TEST_FILE.delete()) {
				Assert.fail("impossible to delete the test file, please release it and run the test again");
			}
		}
	}

	public void testMarshalling(Class<?> clazz) {
		Object get = DtoMockFactory._().on(clazz)
				.withMode(DtoMockFactory.MODE_GET).create();
		testMarshalling(get);
		
		Object post = DtoMockFactory._().on(clazz)
			.withMode(DtoMockFactory.MODE_POST).create();
		testMarshalling(post);
	}

	public void testMarshalling(Object dto) {
		try {
			writeXml(dto, TEST_FILE);
			Object result = read(dto, TEST_FILE);
			Assert.assertEquals(dto.toString(), result.toString());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	protected void writeXml(Object sample, File file)
			throws JAXBException, IOException {
		FileWriter writer = new FileWriter(file);
		try {
			String name = sample.getClass().getPackage().getName();
			JAXBContext jc = JAXBContext.newInstance(name, Thread
					.currentThread().getContextClassLoader());
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_ENCODING, UTF_8);
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(sample, writer);
		} finally {
			writer.close();
		}
	}

	public Object read(Object sample, File file) throws JAXBException, SAXException,
			IOException {
		InputStreamReader reader = new InputStreamReader(new FileInputStream(
				file));
		try {
			String name = sample.getClass().getPackage().getName();
			JAXBContext jc = JAXBContext.newInstance(name, Thread.currentThread()
					.getContextClassLoader());

			Unmarshaller unmarshaller = jc.createUnmarshaller();

			Object element = unmarshaller.unmarshal(reader);
			return element;
		} finally {
			reader.close();
		}
	}
	
}