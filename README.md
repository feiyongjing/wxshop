# wxshop
一个电商项目
生产环境 
docker run --name wxshop -d -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=wxshop -p 3306:3306 mysql:8.0.19
docker run -d --name redis -p 6379:6379 redis
docker run -d --name zookeeper -p 2181:2181 zookeeper

测试环境
docker run --name wxshop-test -d -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=wxshop -p 3307:3306 mysql:8.0.19
docker run -d --name redis-test -p 6380:6379 redis