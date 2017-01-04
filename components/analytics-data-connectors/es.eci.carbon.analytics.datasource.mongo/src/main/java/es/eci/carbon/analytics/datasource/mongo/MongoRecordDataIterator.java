package es.eci.carbon.analytics.datasource.mongo;

import java.io.IOException;
import java.util.List;

import org.bson.Document;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.Record;

import com.mongodb.client.MongoCursor;

public class MongoRecordDataIterator implements AnalyticsIterator<Record> {

	private MongoCursor<Document> iterable;

	private List<String> columns;

	private String tableName;

	private Integer tenantId;

	public MongoRecordDataIterator(MongoCursor<Document> mongoIterable, List<String> columns, String tableName,
			Integer tenantId) {
		this.iterable = mongoIterable;
		this.columns = columns;
		this.tableName = tableName;
		this.tenantId = tenantId;
	}

	@Override
	public boolean hasNext() {
		return iterable.hasNext();
	}

	@Override
	public Record next() {
		Document document = iterable.next();
		return AnalyticsRecord.toRecord(AnalyticsRecord.fromDocument(document, tableName, tenantId), columns);
	}

	@Override
	public void remove() {
		/* this is a read-only iterator, nothing will be removed */
	}

	@Override
	public void close() throws IOException {
		iterable.close();
	}

}
