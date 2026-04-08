#!/bin/bash

set -e

AWS_REGION="ap-northeast-2"
ECR_REGISTRY="909176971481.dkr.ecr.ap-northeast-2.amazonaws.com"
ECR_REPOSITORY="linktrip"
IMAGE="${ECR_REGISTRY}/${ECR_REPOSITORY}:latest"

echo ">>> ECR 로그인"
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}

echo ">>> 최신 이미지 pull"
docker pull ${IMAGE}

echo ">>> 기존 컨테이너 중지 및 제거"
docker stop linktrip-app 2>/dev/null || true
docker rm linktrip-app 2>/dev/null || true

echo ">>> 새 컨테이너 실행"
docker run -d \
  --name linktrip-app \
  --restart unless-stopped \
  --network linktrip-network \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e MYSQL_ROOT_PASSWORD=12345678 \
  -e TZ=Asia/Seoul \
  -v /etc/localtime:/etc/localtime:ro \
  ${IMAGE}

echo ">>> 미사용 이미지 정리"
docker image prune -f

echo ">>> 배포 완료"
docker ps | grep linktrip-app
