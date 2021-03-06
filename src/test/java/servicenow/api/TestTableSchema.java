package servicenow.api;

import static org.junit.Assert.*;

import org.junit.*;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;

@RunWith(Parameterized.class)
public class TestTableSchema {

	@Parameters(name = "{index}:{0}")
	public static TestingProfile[] profiles() {
		// return new TestingProfile[] {TestingManager.getDefaultProfile()};
		return TestingManager.allProfiles();
	}

	TestingProfile profile;
	Session session;
	Logger logger = TestingManager.getLogger(this.getClass());

	final FieldNames testFields = new FieldNames(
			"sys_created_on,sys_created_by,sys_updated_on,sys_updated_by");
	
	public TestTableSchema(TestingProfile profile) throws Exception {
		TestingManager.setProfile(this.getClass(), profile);
		session = TestingManager.getProfile().getSession();
	}

	@AfterClass
	public static void clear() throws Exception {
		TestingManager.clearAll();
	}

	@Test
	public void testDetermineParent() throws Exception {
		Table table = session.table("sys_template");
		TableSchema schema = new TableSchema(table);
		String parent = schema.getParentName();
		logger.info(Log.TEST, "parent=" + parent);
		assertEquals("sys_metadata", parent);
	}
	
	@Test
	public void testSysTemplateSchema() throws Exception {
		Table table = session.table("sys_template");
		TableSchema schema = new TableSchema(table);
		FieldNames schemaFields = schema.getFieldNames();
		for (String name: testFields) {
			logger.info(Log.TEST, "field: " + name);
			assertTrue(schemaFields.contains(name));
		}		
	}
		
}
