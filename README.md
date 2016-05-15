# Bip bip: the Script Runner

[![Circle CI](https://circleci.com/gh/gquintana/beepbeep.svg?style=svg)](https://circleci.com/gh/gquintana/beepbeep)
[![Coverage Status](https://coveralls.io/repos/github/gquintana/beepbeep/badge.svg?branch=master)](https://coveralls.io/github/gquintana/beepbeep?branch=master)


Bip bip is runs scripts on SQL and NoSQL databases and track already ran/new scripts.

It can be used to:
* Create tables/indices/collections and populate them with data
* Prepare a database for unit testing with JUnit
* Setup and configure database for production use

With the same tool, these scripts can be ran on:
* SQL scripts on relational databases using JDBC driver
* Sense scripts on Elasticsearch
* CQL scripts on Cassandra
* And so on...

## Usage

### Command line

Run all scripts in the `script` folder on Elasticsearch located on `localhost` and track their execution in the `.beepbeep` index.
```
$ bin/beepbeep.sh  -t elasticsearch -d http://localhost:9200 -s .beepbeep/script -f 'script/*.json'

START script/index_create.json
END_SUCCESS script/index_create.json:24
START script/index_data.json
END_SUCCESS script/index_data.json:15
```

### Java API

Run all SQL scripts from the classpath on a H2 embedded database and replace `${variable}` placeholders in scripts by value.
```
Consumer<ScriptStartEvent> input = new SqlPipelineBuilder()
    .withConnectionProvider(Driver.class.getName(), "jdbc:h2:mem:test", "sa", "")
    .withVariable("variable", "value")
    .build();
ResourceScriptScanner scriptScanner = ScriptScanners.resources(getClass().getClassLoader(),
    "com/github/gquintana/beepbeep/script/**/*.sql",
    input);
scriptScanner.scan();
```
