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


import static io.opentracing.contrib.solr.SolrTracingUtils.onError;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

public class TracingResponseHandler<T> implements ResponseHandler<T> {

  private final ResponseHandler<T> responseHandler;
  private final Span span;
  private final Tracer tracer;

  public TracingResponseHandler(ResponseHandler<T> responseHandler, Span span,
      Tracer tracer) {
    this.responseHandler = responseHandler;
    this.span = span;
    this.tracer = tracer;
  }

  @Override
  public T handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
    Tags.HTTP_STATUS.set(span, response.getStatusLine().getStatusCode());
    try (Scope ignored = tracer.scopeManager().activate(span, false)) {
      try {
        return responseHandler.handleResponse(response);
      } catch (Exception e) {
        onError(span, e);
        throw e;
      }
    }
  }
}
