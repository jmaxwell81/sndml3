package servicenow.api;

import org.junit.*;
import org.slf4j.Logger;

import servicenow.api.Session;
import servicenow.api.Table;
import servicenow.api.TableSchema;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Properties;

public class SessionVerificationTest {

	Logger logger = TestingManager.getLogger(this.getClass());
	
	@Test
	public void testValidate() throws Exception {
		Session session = TestingManager.getDefaultProfile().getSession();
		session.verify();
		Table user = session.table("sys_user");
		TableWSDL wsdl = user.getWSDL();
		int wsdlCount = wsdl.getReadFieldNames().size();
		logger.info("wsdl fields=" + wsdlCount);
		TableSchema schema = user.getSchema();
		int schemaCount = schema.getFieldNames().size();
		logger.info("schema fields=" + schemaCount);
		session.verify();
		assertEquals(wsdlCount, schemaCount);
	}

	@Test
	public void testAutoVerify() throws Exception {
		Properties props = new Properties();
		props.setProperty("servicenow.instance", "dev00000");
		props.setProperty("servicenow.username", "admin");
		props.setProperty("servicenow.password", "secret");
		Session session1 = new Session(props);
		assertNotNull(session1);
		props.setProperty("servicenow.verify_session", "true");
		Session session2 = null;
		try {
			session2 = new Session(props);
		}
		catch (IOException e) {
			logger.info(Log.TEST, e.getMessage());
		}
		assertNull(session2);
	}
}
