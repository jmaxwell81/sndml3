package servicenow.datamart;

import servicenow.api.*;

import static org.junit.Assert.*;
import org.junit.*;

import java.io.File;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class TimestampTest {

	@Parameters(name = "{index}:{0}")
	public static String[] profiles() {
//		return new String[] {"awsmysql","awspg", "awsora"};
		return new String[] {"awsmysql"};
	}

	final Logger logger = LoggerFactory.getLogger(this.getClass());
	final Session session;
	final Database database;
	
	public TimestampTest(String profile) throws Exception {
		TestingManager.loadProfile(profile);
		session = ResourceManager.getSession();
		database = ResourceManager.getDatabase();
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {		
	}

	static boolean tableLoaded = false;
	
	@Before
	public void setUp() throws Exception {
		if (!tableLoaded) {
			loadTable();
			tableLoaded = true;
		}
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ResourceManager.getDatabase().close();
	}
	
	void loadTable() throws Exception {
		LoaderConfig config = new LoaderConfig(new File("src/test/resources/yaml/load_incident_truncate.yaml"));
		Loader loader = new Loader(config);
		TableLoader tableLoader = loader.jobs.get(0);
		loader.loadTables();
		WriterMetrics metrics = tableLoader.getMetrics();
		assertTrue(metrics.getProcessed() > 0);		
	}
	
	@Test
	public void testIncidentTimestamp() throws Exception {
		Table inc = session.table("incident");
		database.createMissingTable(inc, inc.getName());
		String sys_id = TestingManager.getProperty("some_incident_sys_id");
		String created = TestingManager.getProperty("some_incident_created");
		DateTimeRange emptyRange = new DateTimeRange(null, null);
		Record rec = inc.getRecord(new Key(sys_id));
		assertEquals(created, rec.getCreatedTimestamp().toString());
		TableConfig config = new TableConfig(inc);
		config.setFilter(new EncodedQuery("sys_id=" + sys_id));
		config.setCreated(emptyRange);
		TableLoader loader = new TableLoader(config);
		loader.call();
		DatabaseTimestampReader reader = new DatabaseTimestampReader(database);
		DateTime dbcreated = reader.getTimestampCreated(inc.getName(), new Key(sys_id));
		assertNotNull(dbcreated);
		assertEquals(created, dbcreated.toString());		
	}
	
	@Test
	public void testGetTimestamps() throws Exception {
		DatabaseTimestampReader reader = new DatabaseTimestampReader(database);
		TimestampLookup timestamps = reader.getTimestamps("incident");
		logger.debug(Log.TEST, String.format("Hash size = %d", timestamps.size()));
		assertTrue(timestamps.size() > 0);
		KeySet keys = timestamps.getKeys();
		assertEquals(timestamps.size(), keys.size());
		Key firstKey = keys.get(0);
		DateTime firstTimestamp = timestamps.get(firstKey);
		logger.info(Log.TEST, String.format("%s=%s", firstKey, firstTimestamp));
		Record firstRec = session.table("incident").api().getRecord(firstKey);
		DateTime firstRecUpdated = firstRec.getUpdatedTimestamp();
		assertEquals(firstRecUpdated, firstTimestamp);
	}

}