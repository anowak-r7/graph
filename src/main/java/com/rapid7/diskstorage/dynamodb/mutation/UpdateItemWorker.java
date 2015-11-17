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
package com.rapid7.diskstorage.dynamodb.mutation;

import com.rapid7.diskstorage.dynamodb.DynamoDBDelegate;
import com.rapid7.diskstorage.dynamodb.ExponentialBackoff;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.thinkaurelius.titan.diskstorage.BackendException;

/**
 *
 * @author Alexander Patrikalakis
 *
 */
public class UpdateItemWorker implements MutateWorker {

    private UpdateItemRequest updateItemRequest;
    private DynamoDBDelegate dynamoDBDelegate;

    public UpdateItemWorker(UpdateItemRequest updateItemRequest, DynamoDBDelegate dynamoDBDelegate) {
        this.updateItemRequest = updateItemRequest;
        this.dynamoDBDelegate = dynamoDBDelegate;
    }

    @Override
    public Void call() throws BackendException {
        ExponentialBackoff.UpdateItem updateBackoff = new ExponentialBackoff.UpdateItem(updateItemRequest, dynamoDBDelegate);
        updateBackoff.runWithBackoff();

        // void
        return null;
    }
}
