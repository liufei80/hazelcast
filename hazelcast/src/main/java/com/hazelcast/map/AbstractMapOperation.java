/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.map;

import com.hazelcast.nio.IOUtil;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.spi.KeyBasedOperation;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public abstract class AbstractMapOperation extends Operation implements KeyBasedOperation {

    protected String name;
    protected Data dataKey;
    protected int threadId = -1;
    protected Data dataValue = null;
    protected long ttl = -1; // how long should this item live? -1 means forever
    protected String txnId = null;

    public AbstractMapOperation() {
    }

    public AbstractMapOperation(String name, Data dataKey) {
        super();
        this.dataKey = dataKey;
        this.name = name;
    }

    protected AbstractMapOperation(String name, Data dataKey, Data dataValue) {
        this.name = name;
        this.dataKey = dataKey;
        this.dataValue = dataValue;
    }

    protected AbstractMapOperation(String name, Data dataKey, long ttl) {
        this.name = name;
        this.dataKey = dataKey;
        this.ttl = ttl;
    }

    protected AbstractMapOperation(String name, Data dataKey, Data dataValue, long ttl) {
        this.name = name;
        this.dataKey = dataKey;
        this.dataValue = dataValue;
        this.ttl = ttl;
    }

    public final String getName() {
        return name;
    }

    public final Data getKey() {
        return dataKey;
    }

    public final int getThreadId() {
        return threadId;
    }

    public final void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public final Data getValue() {
        return dataValue;
    }

    public final long getTtl() {
        return ttl;
    }

    public final String getTxnId() {
        return txnId;
    }

    public final void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public final int getKeyHash() {
        return dataKey != null ? dataKey.getPartitionHash() : 0;
    }

    @Override
    public void beforeRun() throws Exception {
    }

    @Override
    public void afterRun() throws Exception {
    }

    @Override
    public final boolean returnsResponse() {
        return true;
    }

    protected final void invalidateNearCaches() {
        final MapService mapService = getService();
        final MapContainer mapContainer = mapService.getMapContainer(name);
        if (mapContainer.isNearCacheEnabled()
                && mapContainer.getMapConfig().getNearCacheConfig().isInvalidateOnChange()) {
            mapService.invalidateAllNearCaches(name, dataKey);
        }
    }

    protected void writeInternal(ObjectDataOutput out) throws IOException {
        out.writeUTF(name);
        dataKey.writeData(out);
        out.writeInt(threadId);
        IOUtil.writeNullableData(out, dataValue);
        out.writeLong(ttl);
        IOUtil.writeNullableString(out, txnId);
    }

    protected void readInternal(ObjectDataInput in) throws IOException {
        name = in.readUTF();
        dataKey = new Data();
        dataKey.readData(in);
        threadId = in.readInt();
        dataValue = IOUtil.readNullableData(in);
        ttl = in.readLong();
        txnId = IOUtil.readNullableString(in);
    }
}