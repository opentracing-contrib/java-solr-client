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

import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracerFactory;
import io.opentracing.util.GlobalTracer;
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

public class TracingHttpSolrClientBuilder extends HttpSolrClient.Builder {

  private final Tracer tracer;

  public TracingHttpSolrClientBuilder() {
    super();
    this.tracer = GlobalTracer.get();
  }

  public TracingHttpSolrClientBuilder(Tracer tracer) {
    super();
    this.tracer = tracer;
  }

  public TracingHttpSolrClientBuilder(String baseSolrUrl, Tracer tracer) {
    super(baseSolrUrl);
    this.tracer = tracer;
  }

  @Override
  public HttpSolrClient build() {

    HttpSolrClient client = super.build();
    HttpClient httpClient = client.getHttpClient();
    super.withHttpClient(
        new TracingHttpClient(httpClient, tracer == null ? NoopTracerFactory.create() : tracer));

    return super.build();
  }
}