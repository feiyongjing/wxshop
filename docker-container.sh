#!/bin/bash
if [ $1 == start ]
then
  docker start wxshop ||
  docker run --name wxshop -d -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=wxshop -p 3306:3306 mysql:8.0.19 :
  docker start redis ||
  docker run --name wxshop -d -p 6379:6379 redis :
  docker start zookeeper ||
  docker run --name zookeeper -d -p 2181:2181 zookeeper :
  docker start wxshop-test ||
  docker run --name wxshop-test -d -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=wxshop -p 3307:3306 mysql:8.0.19 :
  docker start redis-test ||
  docker run --name redis-test -d -p 6380:6379 redis
elif [ $1 == stop ]
  then
      docker stop wxshop
      docker stop wxshop-test
      docker stop redis
      docker stop redis-test
      docker stop zookeeper
fi