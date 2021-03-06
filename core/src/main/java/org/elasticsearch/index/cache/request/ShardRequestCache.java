/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.cache.request;

import org.apache.lucene.util.Accountable;
import org.elasticsearch.common.cache.RemovalListener;
import org.elasticsearch.common.cache.RemovalNotification;
import org.elasticsearch.common.metrics.CounterMetric;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.shard.AbstractIndexShardComponent;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.IndicesRequestCache;

/**
 */
public final class ShardRequestCache {

    final CounterMetric evictionsMetric = new CounterMetric();
    final CounterMetric totalMetric = new CounterMetric();
    final CounterMetric hitCount = new CounterMetric();
    final CounterMetric missCount = new CounterMetric();

    public RequestCacheStats stats() {
        return new RequestCacheStats(totalMetric.count(), evictionsMetric.count(), hitCount.count(), missCount.count());
    }

    public void onHit() {
        hitCount.inc();
    }

    public void onMiss() {
        missCount.inc();
    }

    public void onCached(Accountable key, Accountable value) {
        totalMetric.inc(key.ramBytesUsed() + value.ramBytesUsed());
    }

    public void onRemoval(Accountable key, Accountable value, boolean evicted) {
        if (evicted) {
            evictionsMetric.inc();
        }
        long dec = 0;
        if (key != null) {
            dec += key.ramBytesUsed();
        }
        if (value != null) {
            dec += value.ramBytesUsed();
        }
        totalMetric.dec(dec);
    }
}
