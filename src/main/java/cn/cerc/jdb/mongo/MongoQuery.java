package cn.cerc.jdb.mongo;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;

import cn.cerc.jdb.core.DataQuery;
import cn.cerc.jdb.core.DataSetState;
import cn.cerc.jdb.core.IDataOperator;
import cn.cerc.jdb.core.IHandle;
import cn.cerc.jdb.core.Record;
import cn.cerc.jdb.mysql.SqlOperator;

public class MongoQuery extends DataQuery {
	private static final long serialVersionUID = -1262005194419604476L;
	private MongoSession session = null;
	// 数据库保存操作执行对象
	private IDataOperator operator;
	// 仅当batchSave为true时，delList才有记录存在
	private List<Record> delList = new ArrayList<>();

	public MongoQuery(IHandle handle) {
		super(handle);
		session = (MongoSession) this.handle.getProperty(MongoSession.sessionId);
	}

	@Override
	public DataQuery open() {
		String table = SqlOperator.findTableName(this.getCommandText());
		// 查找业务ID对应的数据
		MongoCollection<Document> coll = session.getDatabase().getCollection(table);
		// Document res = null;
		BasicDBObject filter = new BasicDBObject();
		ArrayList<Document> list = coll.find(filter).into(new ArrayList<Document>());
		// 数据不存在,则状态不为更新,并返回一个空数据
		if (list == null || list.isEmpty())
			return this;

		for (Document doc : list) {
			Record record = append().getCurrent();
			for (String field : doc.keySet())
				record.setField(field, doc.get(field));
			record.setState(DataSetState.dsNone);
		}
		this.first();
		this.active = true;
		return this;
	}

	@Override
	public void post() {
		if (this.isBatchSave())
			return;
		Record record = this.getCurrent();
		if (record.getState() == DataSetState.dsInsert) {
			beforePost();
			getDefaultOperator().insert(record);
			super.post();
		} else if (record.getState() == DataSetState.dsEdit) {
			beforePost();
			getDefaultOperator().update(record);
			super.post();
		}
	}

	private IDataOperator getDefaultOperator() {
		if (operator == null) {
			MongoOperator obj = new MongoOperator(this.handle);
			obj.setTableName(SqlOperator.findTableName(this.getCommandText()));
			operator = obj;
		}
		return operator;
	}

	@Override
	public void delete() {
		Record record = this.getCurrent();
		super.delete();
		if (record.getState() == DataSetState.dsInsert)
			return;
		if (this.isBatchSave())
			delList.add(record);
		else {
			getDefaultOperator().delete(record);
		}
	}

	@Override
	public void save() {
		if (!this.isBatchSave())
			throw new RuntimeException("batchSave is false");
		IDataOperator operator = getDefaultOperator();
		// 先执行删除
		for (Record record : delList)
			operator.delete(record);
		delList.clear();
		// 再执行增加、修改
		this.first();
		while (this.fetch()) {
			if (this.getState().equals(DataSetState.dsInsert)) {
				beforePost();
				operator.insert(this.getCurrent());
				super.post();
			} else if (this.getState().equals(DataSetState.dsEdit)) {
				beforePost();
				operator.update(this.getCurrent());
				super.post();
			}
		}
	}

	public IDataOperator getOperator() {
		return operator;
	}

	public void setOperator(IDataOperator operator) {
		this.operator = operator;
	}

}
