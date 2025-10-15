#!/bin/bash

set -e

DOMAIN="api.tarasantoniuk.com"
EMAIL="bronya2004@gmail.com"
STAGING=0

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}=== Ініціалізація Let's Encrypt SSL для $DOMAIN ===${NC}"

if [ -d "./certs/live/$DOMAIN" ]; then
  echo -e "${YELLOW}Сертифікати вже існують. Видалити та створити нові? (y/N)${NC}"
  read -r response
  if [[ ! "$response" =~ ^[Yy]$ ]]; then
    echo "Вихід без змін."
    exit 0
  fi
  echo "Видалення існуючих сертифікатів..."
  rm -rf "./certs/live/$DOMAIN"
  rm -rf "./certs/archive/$DOMAIN"
  rm -rf "./certs/renewal/$DOMAIN.conf"
fi

mkdir -p ./certs/live/$DOMAIN
mkdir -p ./webroot

echo -e "${GREEN}Створення тимчасових сертифікатів...${NC}"

openssl req -x509 -nodes -newkey rsa:4096 -days 1 \
  -keyout "./certs/live/$DOMAIN/privkey.pem" \
  -out "./certs/live/$DOMAIN/fullchain.pem" \
  -subj "/CN=$DOMAIN"

echo -e "${GREEN}Запуск nginx...${NC}"
docker-compose up -d nginx

echo -e "${GREEN}Видалення тимчасових сертифікатів...${NC}"
rm -rf "./certs/live/$DOMAIN"

echo -e "${GREEN}Отримання справжніх SSL сертифікатів...${NC}"

STAGING_ARG=""
if [ $STAGING != "0" ]; then
  STAGING_ARG="--staging"
  echo -e "${YELLOW}Використовується STAGING режим${NC}"
fi

docker-compose run --rm certbot certonly \
  --webroot \
  --webroot-path=/var/www/certbot \
  --email "$EMAIL" \
  --agree-tos \
  --no-eff-email \
  $STAGING_ARG \
  -d "$DOMAIN"

echo -e "${GREEN}Перезапуск nginx...${NC}"
docker-compose restart nginx

echo -e "${GREEN}=== Готово! ===${NC}"