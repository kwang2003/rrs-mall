#!/usr/bin/env bash
mvn clean package  -am -pl rrs-admin  -Dmaven.test.skip=true -Pprod