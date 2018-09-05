package io.opentracing.contrib.solr;

import io.opentracing.mock.MockTracer;
import org.apache.solr.client.solrj.SolrClient;
import org.junit.Assert;
import org.junit.Test;

public class SolrTracingTest {
  private static final String solrUrl = "http://localhost:8983/solr";
  private static final MockTracer tracer = new MockTracer();

  @Test
  public void test() {
    SolrClient client = getHttpClient();
    Assert.assertNotNull(client);
  }

  private static SolrClient getHttpClient() {
    return new TracingHttpSolrClientBuilder(solrUrl, tracer).build();
  }
}
