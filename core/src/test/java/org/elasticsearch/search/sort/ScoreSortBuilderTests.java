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

package org.elasticsearch.search.sort;


import org.elasticsearch.common.ParseFieldMatcher;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.QueryParseContext;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.io.IOException;

public class ScoreSortBuilderTests extends AbstractSortTestCase<ScoreSortBuilder> {

    @Override
    protected ScoreSortBuilder createTestItem() {
        return new ScoreSortBuilder().order(randomBoolean() ? SortOrder.ASC : SortOrder.DESC);
    }

    @Override
    protected ScoreSortBuilder mutate(ScoreSortBuilder original) throws IOException {
        ScoreSortBuilder result = new ScoreSortBuilder();
        if (original.order() == SortOrder.ASC) {
            result.order(SortOrder.DESC);
        } else {
            result.order(SortOrder.ASC);
        }
        return result;
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    /**
     * test passing null to {@link ScoreSortBuilder#order(SortOrder)} is illegal
     */
    public void testIllegalOrder() {
            exceptionRule.expect(NullPointerException.class);
            exceptionRule.expectMessage("sort order cannot be null.");
            new ScoreSortBuilder().order(null);
    }

    /**
     * test parsing order parameter if specified as `order` field in the json
     * instead of the `reverse` field that we render in toXContent
     */
    public void testParseOrder() throws IOException {
        QueryParseContext context = new QueryParseContext(indicesQueriesRegistry);
        context.parseFieldMatcher(new ParseFieldMatcher(Settings.EMPTY));
        SortOrder order = randomBoolean() ? SortOrder.ASC : SortOrder.DESC;
        String scoreSortString = "{ \"_score\": { \"order\": \""+ order.toString() +"\" }}";
        XContentParser parser = XContentFactory.xContent(scoreSortString).createParser(scoreSortString);
        // need to skip until parser is located on second START_OBJECT
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();

        context.reset(parser);
        ScoreSortBuilder scoreSort = ScoreSortBuilder.PROTOTYPE.fromXContent(context, "_score");
        assertEquals(order, scoreSort.order());
    }
}
