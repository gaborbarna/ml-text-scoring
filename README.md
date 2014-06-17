# scoring #

Online logistic regression classification w/ Apache Mahout, Scalatra, Elasticsearch.

## Build & Run ##

create `documents` Elasticsearch index using es_index.json

```sh
$ cd scoring
$ ./sbt
> container:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.
