#!/usr/bin/env bash
mvn clean package  -am -pl rrs-mall-web  -Dmaven.test.skip=true -U
