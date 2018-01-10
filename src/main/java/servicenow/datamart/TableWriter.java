package servicenow.datamart;

import java.io.IOException;
import java.sql.SQLException;

import org.slf4j.Logger;

import servicenow.core.*;

public abstract class TableWriter extends Writer {

	final private Database db;
	final private Table table;
	final private String sqlTableName;
	
	private ColumnDefinitions columns;
	protected InsertStatement insertStmt;
	protected UpdateStatement updateStmt;
	
	final private Logger logger = Log.logger(this.getClass());
	
	public TableWriter(Database db, Table table, String sqlTableName) throws IOException, SQLException {
		this.db = db;
		this.table = table;
		this.sqlTableName = sqlTableName == null ? table.getName() : sqlTableName;
		Log.setTableContext(this.table);
	}
	
	@Override
	public void open() throws SQLException, IOException {
		columns = new ColumnDefinitions(this.db, this.table, this.sqlTableName);
		insertStmt = new InsertStatement(this.db, this.sqlTableName, columns);
		updateStmt = new UpdateStatement(this.db, this.sqlTableName, columns);		
		metrics.start();
	}
	
	@Override
	public synchronized void processRecords(RecordList recs) throws IOException, SQLException {
		metrics.start();
		for (Record rec : recs) {
			writeRecord(rec);
			logger.debug(Log.PROCESS, String.format("processing %s", rec.getKey().toString()));
		}
		metrics.finish();
		logProgress("loaded");
		db.commit();
	}
	
	private synchronized void logProgress(String status) {
		assert getReader() != null;
		assert getReader().getMetrics() != null;
		getReader().setLogContext();
		ReaderMetrics readerMetrics = getReader().getMetrics();
		if (readerMetrics.getParent() == null) 
			logger.info(Log.PROCESS, String.format("%s %s", status, readerMetrics.getProgress()));
		else
			logger.info(Log.PROCESS, String.format("%s %s (%s)", status, 
					readerMetrics.getProgress(), readerMetrics.getParent().getProgress())); 
	}

	abstract void writeRecord(Record rec) throws SQLException;
	
//	private synchronized void writeRecord(Record rec) throws SQLException {
//		if (updateStmt.update(rec)) {
//			metrics.incrementUpdated();
//		}
//		else {
//			insertStmt.insert(rec);
//			metrics.incrementInserted();
//		}
//	}
				
}