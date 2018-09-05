/*
 * Copyright 2018 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.contrib.solr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.tag.Tags;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.MapSolrParams;
import org.junit.Before;
import org.junit.Test;

public class SolrTracingTest {
  private static final String solrUrl = "http://localhost:8983/solr";
  private static final MockTracer tracer = new MockTracer();

  @Before
  public void before() {
    tracer.reset();
  }

  @Test
  public void testHttpClient() throws Exception {
    SolrClient client = getHttpClient();
    test(client, true);
  }

  @Test
  public void testCloudClient() throws Exception {
    SolrClient client = getCloudClient(false);
    test(client, false);
  }

  @Test
  public void testCloudClientSkipStatusAction() throws Exception {
    SolrClient client = getCloudClient(true);
    test(client, true);
  }

  private void test(SolrClient client, boolean skipStatusAction) throws Exception {
    final Map<String, String> queryParamMap = new HashMap<>();
    queryParamMap.put("q", "*:*");
    queryParamMap.put("fl", "id, name");
    queryParamMap.put("sort", "id asc");
    MapSolrParams queryParams = new MapSolrParams(queryParamMap);

    final QueryResponse response = client.query("gettingstarted", queryParams);
    final SolrDocumentList documents = response.getResults();
    assertNotNull(documents);

    List<MockSpan> spans = tracer.finishedSpans();
    assertFalse(spans.isEmpty());
    if (skipStatusAction) {
      assertEquals(1, spans.size());
    }

    for (MockSpan span : spans) {
      assertEquals(span.tags().get(Tags.SPAN_KIND.getKey()), Tags.SPAN_KIND_CLIENT);
      assertEquals(TracingHttpClient.COMPONENT_NAME, span.tags().get(Tags.COMPONENT.getKey()));
      assertEquals(200, span.tags().get(Tags.HTTP_STATUS.getKey()));
      assertEquals("GET", span.operationName());
      assertEquals(0, span.generatedErrors().size());
      assertNotNull(span.tags().get(Tags.HTTP_URL.getKey()));
    }
  }

  private static SolrClient getHttpClient() {
    return new TracingHttpSolrClientBuilder(solrUrl, tracer).build();
  }

  private static SolrClient getCloudClient(boolean skipStatusAction) {
    List<String> urls = new ArrayList<>();
    urls.add(solrUrl);

    HttpClient httpClient;
    if (skipStatusAction) {
      httpClient = new TracingHttpClient(HttpClientUtil.createClient(null), tracer, true);
    } else {
      httpClient = new TracingHttpClient(HttpClientUtil.createClient(null), tracer);
    }

    return new CloudSolrClient.Builder(urls)
        .withHttpClient(httpClient)
        .build();
  }
}
