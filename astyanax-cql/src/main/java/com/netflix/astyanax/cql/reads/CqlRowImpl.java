package com.netflix.astyanax.cql.reads;

import java.nio.ByteBuffer;
import java.util.List;

import com.netflix.astyanax.cql.util.CqlTypeMapping;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.model.Row;

@SuppressWarnings("unchecked")
public class CqlRowImpl<K, C> implements Row<K, C> {

	private final K rowKey;
	private final CqlColumnListImpl<C> cqlColumnList;
	private final ColumnFamily<K, C> cf;
	
	public CqlRowImpl(com.datastax.driver.core.Row resultRow, ColumnFamily<K, C> cf) {
		this.rowKey = (K) getRowKey(resultRow, cf);
		this.cqlColumnList = new CqlColumnListImpl<C>(resultRow);
		this.cf = cf;
	}
	
	public CqlRowImpl(List<com.datastax.driver.core.Row> rows, ColumnFamily<K, C> cf) {
		this.rowKey = (K) getRowKey(rows.get(0), cf);
		this.cqlColumnList = new CqlColumnListImpl<C>(rows, cf);
		this.cf = cf;
	}
	
	@Override
	public K getKey() {
		return rowKey;
	}

	@Override
	public ByteBuffer getRawKey() {
		return cf.getKeySerializer().toByteBuffer(rowKey);
	}

	@Override
	public ColumnList<C> getColumns() {
		return cqlColumnList;
	}
	
	private Object getRowKey(com.datastax.driver.core.Row row, ColumnFamily<K, C> cf) {
		return CqlTypeMapping.getDynamicColumn(row, cf.getKeySerializer(), 0);
	}
}