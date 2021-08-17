#!/bin/bash
docker run -d -p 5000:80 -v E:/java-project/westore-react-1/build:/static -v E:/java-project/wxshop/nginx/:/etc/nginx/ nginx

