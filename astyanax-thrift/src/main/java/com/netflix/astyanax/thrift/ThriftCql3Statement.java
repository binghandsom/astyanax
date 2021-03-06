/**
 * Copyright 2013 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.astyanax.thrift;

import java.nio.ByteBuffer;

import org.apache.cassandra.thrift.Compression;
import org.apache.cassandra.thrift.Cassandra.Client;

import com.google.common.util.concurrent.ListenableFuture;
import com.netflix.astyanax.CassandraOperationType;
import com.netflix.astyanax.connectionpool.ConnectionContext;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.cql.CqlPreparedStatement;
import com.netflix.astyanax.cql.CqlStatement;
import com.netflix.astyanax.cql.CqlStatementResult;
import com.netflix.astyanax.model.ConsistencyLevel;
import com.netflix.astyanax.retry.RetryPolicy;
import com.netflix.astyanax.serializers.StringSerializer;

public class ThriftCql3Statement implements CqlStatement {
    
    private ThriftKeyspaceImpl keyspace;
    private ByteBuffer  query;
    private Compression compression = Compression.NONE;
    private RetryPolicy retry;
    private ConsistencyLevel cl = ConsistencyLevel.CL_ONE;
    
    public ThriftCql3Statement(ThriftKeyspaceImpl keyspace) {
        this.keyspace = keyspace;
        this.retry = keyspace.getConfig().getRetryPolicy().duplicate();
    }

    @Override
    public OperationResult<CqlStatementResult> execute() throws ConnectionException {
        return keyspace.connectionPool.executeWithFailover(
                new AbstractKeyspaceOperationImpl<CqlStatementResult>(keyspace.tracerFactory.newTracer(
                        CassandraOperationType.CQL, null), null, keyspace.getKeyspaceName()) {
                    @Override
                    public CqlStatementResult internalExecute(Client client, ConnectionContext context) throws Exception {
                        return new ThriftCqlStatementResult(client.execute_cql3_query(query, compression, ThriftConverter.ToThriftConsistencyLevel(cl)));
                    }
                }, retry);
    }

    @Override
    public ListenableFuture<OperationResult<CqlStatementResult>> executeAsync() throws ConnectionException {
        // TODO:
        throw new RuntimeException("Not supported yet");
    }

    @Override
    public CqlStatement withCql(String cql) {
        query = StringSerializer.get().toByteBuffer(cql);
        return this;
    }
    
    public CqlStatement withCompression(Boolean flag) {
        if (flag)
            compression = Compression.GZIP;
        else
            compression = Compression.NONE;
        return this;
    }

    @Override
    public CqlStatement withConsistencyLevel(ConsistencyLevel cl) {
        this.cl = cl;
        return this;
    }

    @Override
    public CqlPreparedStatement asPreparedStatement() {
        // TODO:
        throw new RuntimeException("Not supported yet");
    }
}
