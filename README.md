[![Build Status][ci-img]][ci] [![Coverage Status][cov-img]][cov] [![Released Version][maven-img]][maven] [![Apache-2.0 license](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# OpenTracing Solr Client Instrumentation
OpenTracing instrumentation for Solr Client.

## Installation

pom.xml
```xml
<dependency>
    <groupId>io.opentracing.contrib</groupId>
    <artifactId>opentracing-solr-client</artifactId>
    <version>VERSION</version>
</dependency>
```

## Usage

```java
// Instantiate tracer
Tracer tracer = ...

// Optionally register it with GlobalTracer:
GlobalTracer.register(tracer);
``` 

### HttpSolrClient
```
// Instantiate HttpSolrClient using TracingHttpSolrClientBuilder
HttpSolrClient client = new TracingHttpSolrClientBuilder(solrUrl, tracer).build();

// If tracer is registered with GlobalTracer:
HttpSolrClient client = new TracingHttpSolrClientBuilder(solrUrl).build();
```

### SolrClient implementations

Usage of other `SolrClient` implementations requires providing `TracingHttpClient` as `HttpClient` e.g.:
```java
CloudSolrClient client = new CloudSolrClient.Builder(urls)
        .withHttpClient(new TracingHttpClient(HttpClientUtil.createClient(null), tracer))
        .build()
```

## License

[Apache 2.0 License](./LICENSE).

[ci-img]: https://travis-ci.org/opentracing-contrib/java-solr-client.svg?branch=master
[ci]: https://travis-ci.org/opentracing-contrib/java-solr-client
[cov-img]: https://coveralls.io/repos/github/opentracing-contrib/java-solr-client/badge.svg?branch=master
[cov]: https://coveralls.io/github/opentracing-contrib/java-solr-client?branch=master
[maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-solr-client.svg
[maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-solr-client

