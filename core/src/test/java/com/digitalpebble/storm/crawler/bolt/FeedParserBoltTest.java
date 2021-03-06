/**
 * Licensed to DigitalPebble Ltd under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * DigitalPebble licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digitalpebble.storm.crawler.bolt;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.digitalpebble.storm.crawler.Constants;
import com.digitalpebble.storm.crawler.Metadata;
import com.digitalpebble.storm.crawler.TestUtil;
import com.digitalpebble.storm.crawler.parse.filter.ParsingTester;
import com.digitalpebble.storm.crawler.protocol.HttpHeaders;

import org.apache.storm.task.OutputCollector;

public class FeedParserBoltTest extends ParsingTester {

    @Before
    public void setupParserBolt() {
        bolt = new FeedParserBolt();
        setupParserBolt(bolt);
    }

    private void checkOutput() {
        Assert.assertEquals(170, output.getEmitted(Constants.StatusStreamName)
                .size());
        List<Object> fields = output.getEmitted(Constants.StatusStreamName)
                .get(0);
        Assert.assertEquals(3, fields.size());
    }

    @Test
    public void testFeedParsing() throws IOException {

        prepareParserBolt("test.parsefilters.json");

        Metadata metadata = new Metadata();
        // specify that it is a Feed file
        metadata.setValue(FeedParserBolt.isFeedKey, "true");
        parse("http://www.guardian.com/Feed.xml", "guardian.rss", metadata);
        checkOutput();
    }

    @Test
    public void testFeedParsingNoMT() throws IOException {

        Map parserConfig = new HashMap();
        parserConfig.put("feed.sniffContent", true);
        parserConfig.put("parsefilters.config.file", "test.parsefilters.json");
        bolt.prepare(parserConfig, TestUtil.getMockedTopologyContext(),
                new OutputCollector(output));

        Metadata metadata = new Metadata();

        // set mime-type
        metadata.setValue(HttpHeaders.CONTENT_TYPE, "application/rss+xml");

        parse("http://www.guardian.com/feed.xml", "guardian.rss", metadata);

        checkOutput();
    }

    @Test
    public void testNonFeedParsing() throws IOException {

        prepareParserBolt("test.parsefilters.json");
        // do not specify that it is a feed file
        parse("http://www.digitalpebble.com", "digitalpebble.com.html",
                new Metadata());

        Assert.assertEquals(1, output.getEmitted().size());
    }

}
