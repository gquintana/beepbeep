# Bip bip: the Script Runner

[![Circle CI](https://circleci.com/gh/gquintana/beepbeep.svg?style=shield)](https://circleci.com/gh/gquintana/beepbeep)
[![AppVeyor](https://ci.appveyor.com/api/projects/status/1cx4rg3ysqodcc8l?svg=true)](https://ci.appveyor.com/project/gquintana/beepbeep)
[![Coverage Status](https://coveralls.io/repos/github/gquintana/beepbeep/badge.svg?branch=master)](https://coveralls.io/github/gquintana/beepbeep?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.gquintana/beepbeep/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.gquintana/beepbeep)

Beep beep can run scripts on SQL and NoSQL databases,
track ran scripts and not run them again.

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
new SqlPipelineBuilder()
    .withConnectionProvider(Driver.class.getName(), "jdbc:h2:mem:test", "sa", "")
    .withVariable("variable", "value")
    .withResourcesScriptScanner(getClass().getClassLoader(),
      "com/github/gquintana/beepbeep/script/**/*.sql")
    .scan();
```

## Installation

### Build from source

1. Install Java DK 8 and Apache Maven 3
2. Run Maven `mvn install`

### Command line tool

1. Grab the `target\beepbeep-x.y-bin.zip` and unzip it anywhere
2. If necessary, add your JDBC driver Jar in the `lib` folder
3. If necessary, edit the configuration files in the `config` 

## Documentation

Documentation is in GitHub [Wiki](https://github.com/gquintana/beepbeep/wiki)
