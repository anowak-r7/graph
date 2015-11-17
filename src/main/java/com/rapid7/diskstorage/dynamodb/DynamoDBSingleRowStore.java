/*
 * Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.rapid7.diskstorage.dynamodb;

import com.rapid7.diskstorage.dynamodb.builder.EntryBuilder;
import com.rapid7.diskstorage.dynamodb.builder.ItemBuilder;
import com.rapid7.diskstorage.dynamodb.builder.SingleExpectedAttributeValueBuilder;
import com.rapid7.diskstorage.dynamodb.builder.SingleUpdateBuilder;
import com.rapid7.diskstorage.dynamodb.iterator.ScanBackedKeyIterator;
import com.rapid7.diskstorage.dynamodb.iterator.ScanContextInterpreter;
import com.rapid7.diskstorage.dynamodb.iterator.Scanner;
import com.rapid7.diskstorage.dynamodb.iterator.SequentialScanner;
import com.rapid7.diskstorage.dynamodb.iterator.SingleRowScanInterpreter;
import com.rapid7.diskstorage.dynamodb.mutation.MutateWorker;
import com.rapid7.diskstorage.dynamodb.mutation.SingleUpdateWithCleanupWorker;
import com.rapid7.diskstorage.dynamodb.mutation.UpdateItemWorker;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.google.common.collect.Lists;
import com.thinkaurelius.titan.diskstorage.BackendException;
import com.thinkaurelius.titan.diskstorage.Entry;
import com.thinkaurelius.titan.diskstorage.EntryList;
import com.thinkaurelius.titan.diskstorage.StaticBuffer;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.KCVMutation;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.KeyIterator;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.KeyRangeQuery;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.KeySliceQuery;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.SliceQuery;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.StoreTransaction;
import com.thinkaurelius.titan.diskstorage.util.StaticArrayEntryList;

/**
 * Acts as if DynamoDB were a Column Oriented Database by using key as the hash
 * key and each entry has their own column. Note that if you are likely to go
 * over the DynamoDB 400kb per item limit you should use DynamoDBStore.
 *
 * See configuration
 * storage.dynamodb.stores.***store_name***.data-model=SINGLE
 *
 * KCV Schema - actual table (Hash(S) only):
 * hk   |  0x02  |  0x04    <-Attribute Names
 * 0x01 |  0x03  |  0x05    <-Row Values
 *
 * @author Matthew Sowders
 * @author Alexander Patrikalakis
 *
 */
public class DynamoDBSingleRowStore extends AbstractDynamoDBStore {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public DynamoDBSingleRowStore(DynamoDBStoreManager manager, String prefix, String storeName) {
        super(manager, prefix, storeName);
    }

    @Override
    public CreateTableRequest getTableSchema() {
        return createTableRequest(tableName, client.readCapacity(getTableName()),
                                   client.writeCapacity(getTableName()));
    }

    public static final CreateTableRequest createTableRequest(String tableName, long rcu, long wcu) {
        return new CreateTableRequest()
             .withAttributeDefinitions(
                     new AttributeDefinition()
                             .withAttributeName(Constants.TITAN_HASH_KEY)
                             .withAttributeType(ScalarAttributeType.S))
             .withKeySchema(
                     new KeySchemaElement()
                             .withAttributeName(Constants.TITAN_HASH_KEY)
                             .withKeyType(KeyType.HASH))
             .withTableName(tableName)
             .withProvisionedThroughput(new ProvisionedThroughput()
                                                .withReadCapacityUnits(rcu)
                                                .withWriteCapacityUnits(wcu));
    }

    @Override
    public KeyIterator getKeys(KeyRangeQuery query, StoreTransaction txh) throws BackendException {
        throw new UnsupportedOperationException("Keys are not byte ordered.");
    }

    private GetItemWorker createGetItemWorker(StaticBuffer hashKey) {
        final GetItemRequest request = new GetItemRequest().withKey(new ItemBuilder().hashKey(hashKey).build())
                                                           .withTableName(tableName)
                                                           .withConsistentRead(forceConsistentRead)
                                                           .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);
        return new GetItemWorker(hashKey, request, client.delegate());
    }

    private EntryList extractEntriesFromGetItemResult(GetItemResult result, StaticBuffer sliceStart, StaticBuffer sliceEnd, int limit) {
        Map<String, AttributeValue> item = result.getItem();
        List<Entry> filteredEntries = Collections.emptyList();
        if (null != item) {
            item.remove(Constants.TITAN_HASH_KEY);
            filteredEntries = new EntryBuilder(item)
                    .slice(sliceStart, sliceEnd)
                    .limit(limit)
                    .buildAll();
        }
        return StaticArrayEntryList.of(filteredEntries);
    }

    @Override
    public KeyIterator getKeys(SliceQuery query, StoreTransaction txh) throws BackendException {
        log.debug("Entering getKeys table:{} query:{} txh:{}", getTableName(), encodeForLog(query), txh);

        final ScanRequest scanRequest = new ScanRequest().withTableName(tableName)
                                                         .withLimit(client.scanLimit(tableName))
                                                         .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);

        final Scanner scanner;
        if (client.enableParallelScan()) {
            scanner = client.delegate().getParallelScanCompletionService(scanRequest);
        } else {
            scanner = new SequentialScanner(client.delegate(), scanRequest);
        }
        // Because SINGLE records cannot be split across scan results, we can use the same interpreter for both
        // sequential and parallel scans.
        final ScanContextInterpreter interpreter = new SingleRowScanInterpreter(query);

        final KeyIterator result = new ScanBackedKeyIterator(scanner, interpreter);

        log.debug("Exiting getKeys table:{} query:{} txh:{} returning:{}", getTableName(), encodeForLog(query), txh, result);
        return result;
    }

    @Override
    public String getName() {
        return storeName;
    }

    @Override
    public EntryList getSlice(KeySliceQuery query, StoreTransaction txh) throws BackendException {
        log.debug("Entering getSliceKeySliceQuery table:{} query:{} txh:{}", getTableName(), encodeForLog(query), txh);
        final GetItemRequest request = new GetItemRequest()
                .withKey(new ItemBuilder().hashKey(query.getKey()).build())
                .withTableName(tableName)
                .withConsistentRead(forceConsistentRead)
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);
        final GetItemResult result = new ExponentialBackoff.GetItem(request, client.delegate()).runWithBackoff();

        final List<Entry> filteredEntries = extractEntriesFromGetItemResult(result, query.getSliceStart(), query.getSliceEnd(), query.getLimit());
        log.debug("Exiting getSliceKeySliceQuery table:{} query:{} txh:{} returning:{}", getTableName(), encodeForLog(query), txh,
                  filteredEntries.size());
        return StaticArrayEntryList.of(filteredEntries);
    }

    @Override
    public Map<StaticBuffer, EntryList> getSlice(List<StaticBuffer> keys, SliceQuery query, StoreTransaction txh) throws BackendException {
        log.debug("Entering getSliceMultiSliceQuery table:{} keys:{} query:{} txh:{}", getTableName(), encodeForLog(keys), encodeForLog(query),
                txh);
        final Map<StaticBuffer, EntryList> entries = new HashMap<>(keys.size());

        final List<GetItemWorker> getItemWorkers = Lists.newLinkedList();
        for (StaticBuffer hashKey : keys) {
            GetItemWorker worker = createGetItemWorker(hashKey);
            getItemWorkers.add(worker);
        }

        final Map<StaticBuffer, GetItemResult> resultMap = client.delegate().parallelGetItem(getItemWorkers);
        for (Map.Entry<StaticBuffer, GetItemResult> resultEntry : resultMap.entrySet()) {
            EntryList entryList = extractEntriesFromGetItemResult(resultEntry.getValue(), query.getSliceStart(),
                                                                  query.getSliceEnd(), query.getLimit());
            entries.put(resultEntry.getKey(), entryList);
        }

        log.debug("Exiting getSliceMultiSliceQuery table:{} keys:{} query:{} txh:{} returning:{}",
                getTableName(),
                encodeForLog(keys),
                encodeForLog(query),
                txh,
                entries.size());
        return entries;
    }

    @Override
    public void mutate(StaticBuffer hashKey, List<Entry> additions, List<StaticBuffer> deletions, StoreTransaction txh) throws BackendException {
        log.debug("Entering mutate table:{} keys:{} additions:{} deletions:{} txh:{}",
                  getTableName(),
                  encodeKeyForLog(hashKey),
                  encodeForLog(additions),
                  encodeForLog(deletions),
                  txh);
        Map<String, Map<StaticBuffer, KCVMutation>> mutations
                = Collections.singletonMap(storeName,
                                           Collections.singletonMap(hashKey,
                                                                    new KCVMutation(additions, deletions)));
        manager.mutateMany(mutations, txh);

        log.debug("Exiting mutate table:{} keys:{} additions:{} deletions:{} txh:{} returning:void",
                  getTableName(),
                  encodeKeyForLog(hashKey),
                  encodeForLog(additions),
                  encodeForLog(deletions),
                  txh);
    }

    @Override
    public int hashCode() {
        return getTableName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        DynamoDBSingleRowStore rhs = (DynamoDBSingleRowStore) obj;
        return new EqualsBuilder().append(getTableName(), rhs.getTableName()).isEquals();
    }

    @Override
    public String toString() {
        return "DynamoDBSingleRowStore:" + getTableName();
    }

    @Override
    public Collection<MutateWorker> createMutationWorkers(Map<StaticBuffer, KCVMutation> mutationMap, DynamoDBStoreTransaction txh) {

        List<MutateWorker> workers = Lists.newLinkedList();

        for (Map.Entry<StaticBuffer, KCVMutation> entry : mutationMap.entrySet()) {
            final StaticBuffer hashKey = entry.getKey();
            final KCVMutation mutation = entry.getValue();

            Map<String, AttributeValue> key = new ItemBuilder().hashKey(hashKey)
                                                               .build();

            // Using ExpectedAttributeValue map to handle large mutations in a single request
            // Large mutations would require multiple requests using expressions
            Map<String, ExpectedAttributeValue> expected = new SingleExpectedAttributeValueBuilder().key(hashKey)
                                                                                                    .transaction(txh)
                                                                                                    .build(mutation);

            Map<String, AttributeValueUpdate> attributeValueUpdates = new SingleUpdateBuilder().deletions(mutation.getDeletions())
                                                                                               .additions(mutation.getAdditions())
                                                                                               .build();

            UpdateItemRequest request = new UpdateItemRequest().withTableName(tableName)
                                                               .withKey(key)
                                                               .withReturnValues(ReturnValue.ALL_NEW)
                                                               .withAttributeUpdates(attributeValueUpdates)
                                                               .withExpected(expected)
                                                               .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);

            MutateWorker worker;
            if (mutation.hasDeletions() && !mutation.hasAdditions()) {
                worker = new SingleUpdateWithCleanupWorker(request, client.delegate());
            } else {
                worker = new UpdateItemWorker(request, client.delegate());
            }
            workers.add(worker);
        }
        return workers;
    }

}
