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
import static io.opentracing.contrib.solr.SolrTracingUtils.onResponse;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import java.io.Closeable;
import java.io.IOException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

public class TracingHttpClient implements HttpClient, Closeable {

  static final String COMPONENT_NAME = "java-solr";

  private final HttpClient httpClient;
  private final Tracer tracer;

  public TracingHttpClient(HttpClient httpClient, Tracer tracer) {
    this.httpClient = httpClient;
    this.tracer = tracer;
  }

  @Override
  @Deprecated
  public HttpParams getParams() {
    return httpClient.getParams();
  }

  @Override
  @Deprecated
  public ClientConnectionManager getConnectionManager() {
    return httpClient.getConnectionManager();
  }

  @Override
  public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
    Span span = buildSpan(request);
    try {
      HttpResponse response = httpClient.execute(request);
      onResponse(span, response);
      return response;
    } catch (Exception e) {
      onError(span, e);
      throw e;
    } finally {
      span.finish();
    }
  }

  @Override
  public HttpResponse execute(HttpUriRequest request, HttpContext context)
      throws IOException, ClientProtocolException {
    Span span = buildSpan(request);
    try {
      HttpResponse response = httpClient.execute(request, context);
      onResponse(span, response);
      return response;
    } catch (Exception e) {
      onError(span, e);
      throw e;
    } finally {
      span.finish();
    }
  }

  @Override
  public HttpResponse execute(HttpHost target, HttpRequest request)
      throws IOException, ClientProtocolException {
    Span span = buildSpan(request);
    try {
      HttpResponse response = httpClient.execute(target, request);
      onResponse(span, response);
      return response;
    } catch (Exception e) {
      onError(span, e);
      throw e;
    } finally {
      span.finish();
    }
  }

  @Override
  public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context)
      throws IOException, ClientProtocolException {
    Span span = buildSpan(request);
    try {
      HttpResponse response = httpClient.execute(target, request, context);
      onResponse(span, response);
      return response;
    } catch (Exception e) {
      onError(span, e);
      throw e;
    } finally {
      span.finish();
    }
  }

  @Override
  public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler)
      throws IOException, ClientProtocolException {
    Span span = buildSpan(request);
    try {
      return httpClient
          .execute(request, new TracingResponseHandler<>(responseHandler, span, tracer));
    } finally {
      span.finish();
    }
  }

  @Override
  public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler,
      HttpContext context) throws IOException, ClientProtocolException {
    Span span = buildSpan(request);
    try {
      return httpClient
          .execute(request, new TracingResponseHandler<>(responseHandler, span, tracer), context);
    } finally {
      span.finish();
    }
  }

  @Override
  public <T> T execute(HttpHost target, HttpRequest request,
      ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
    Span span = buildSpan(request);
    try {
      return httpClient
          .execute(target, request, new TracingResponseHandler<>(responseHandler, span, tracer));
    } finally {
      span.finish();
    }
  }

  @Override
  public <T> T execute(HttpHost target, HttpRequest request,
      ResponseHandler<? extends T> responseHandler,
      HttpContext context) throws IOException, ClientProtocolException {
    Span span = buildSpan(request);
    try {
      return httpClient
          .execute(target, request, new TracingResponseHandler<>(responseHandler, span, tracer),
              context);
    } finally {
      span.finish();
    }
  }


  @Override
  public void close() throws IOException {
    if (httpClient instanceof Closeable) {
      ((Closeable) httpClient).close();
    }
  }

  private Span buildSpan(HttpRequest request) {
    Tracer.SpanBuilder spanBuilder = tracer.buildSpan(request.getRequestLine().getMethod())
        .withTag(Tags.COMPONENT.getKey(), COMPONENT_NAME)
        .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
        .withTag(Tags.HTTP_URL.getKey(), request.getRequestLine().getUri());

    Span span = spanBuilder.start();
    tracer
        .inject(span.context(), Format.Builtin.HTTP_HEADERS, new HttpHeadersInjectAdapter(request));
    return span;
  }


}
