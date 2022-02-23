#!/bin/bash
docker run --name wxshop-nginx -d -p 5000:80 -v E:/java-project/westore-react-1/build:/static -v E:/java-project/wxshop/nginx/nginx.conf:/etc/nginx/nginx.conf nginx

