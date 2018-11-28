#!/usr/bin/env bash

mvn clean deploy -Dmaven.test.skip -DaltDeploymentRepository=acupt-repository::default::file:../repository/