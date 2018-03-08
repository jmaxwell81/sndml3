package servicenow.api;

import java.io.IOException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TableAPI {

	final protected Table table;
	final protected Session session;
	final protected CloseableHttpClient client;
	
	final private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public TableAPI(Table table) {
		this.table = table;
		this.session = table.getSession();
		this.client = this.session.getClient();
	}

	public Table getTable() {
		return this.table;
	}

	public Session getSession() {
		return this.session;
	}

	public String getTableName() {
		return table.getName();
	}

//	@Deprecated
//	protected void setAPIContext(URI uri) {
//		Log.setSessionContext(session);
//		Log.setTableContext(table);
//		Log.setURIContext(uri);		
//	}
	
	/**
	 * Gets a record using the sys_id. 
	 * If the record is not found then null is returned.
	 * 
	 * @param sys_id
	 * @return Record if found otherwise null.
	 * @throws IOException
	 */
 	public abstract Record getRecord(Key sys_id) throws IOException;
 	
 	public abstract RecordList getRecords(EncodedQuery query, boolean displayValue) throws IOException;
 	
 	public abstract InsertResponse insertRecord(Parameters fields) throws IOException;
 	
 	public abstract void updateRecord(Key key, Parameters fields) throws IOException;
 	
 	public abstract boolean deleteRecord(Key key) throws IOException;

 	public abstract TableReader getDefaultReader() throws IOException;

 	public RecordList getRecords() throws IOException {
 		return getRecords(false);
 	}
 	
 	public RecordList getRecords(boolean displayValue) throws IOException {
 		return getRecords((EncodedQuery) null, displayValue);
 	}
 	
 	public RecordList getRecords(String fieldname, String fieldvalue) throws IOException {
 		return getRecords(fieldname, fieldvalue, false);
 	}
 	
 	public RecordList getRecords(EncodedQuery query) throws IOException {
 		return getRecords(query, false);
 	}
 	
	public RecordList getRecords(String fieldname, String fieldvalue, boolean displayValue) throws IOException {
		EncodedQuery query = new EncodedQuery(fieldname, fieldvalue);
		return getRecords(query, displayValue);
	}

	/**
	 * Retrieves a single record based on a unique field such as "name" or "number".  
	 * This method should be used in cases where the field value is known to be unique.
	 * If no qualifying records are found this function will return null.
	 * If one qualifying record is found it will be returned.
	 * If multiple qualifying records are found this method 
	 * will throw an RowCountExceededException.
	 * <pre>
	 * {@link Record} grouprec = session.table("sys_user_group").get("name", "Network Support");
	 * </pre>
	 * 
	 * @param fieldname Field name, e.g. "number" or "name"
	 * @param fieldvalue Field value
	 */
	public Record getRecord(String fieldname, String fieldvalue, boolean displayValues)
			throws IOException, SoapResponseException {
		RecordList result = getRecords(fieldname, fieldvalue, displayValues);
		int size = result.size();
		String msg = String.format("get %s=%s returned %d records", fieldname, fieldvalue, size);
		logger.info(Log.RESPONSE, msg);
		if (size == 0) return null;
		if (size > 1) throw new RowCountExceededException(getTable(), msg);
		return result.get(0);
	}
	
}
