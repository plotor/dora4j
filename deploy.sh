#!/usr/bin/env bash
mvn clean deploy -Dregistry=https://maven.pkg.github.com/plotor -Dmaven.test.skip
