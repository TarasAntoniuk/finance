# Docker Deployment Guide

## 🚀 Quick Start

### Daily Deployment (Automated via CI/CD)

The application is automatically deployed when pushing to `dev` branch:

```bash
git push origin dev
```

GitHub Actions will:
1. Run all tests
2. Build JAR file
3. Deploy to production server
4. Verify health check

---

## 🔐 Initial SSL Setup (One-Time Only)

**Required only when deploying to a new server or domain.**

### On the server:

```bash
cd /srv/apps/finance/docker
bash init-letsencrypt.sh
```

When prompted:
- **If certificates exist**: Answer `N` to keep them
- **For new setup**: Answer `Y` to create certificates

### What it does:
1. Creates temporary self-signed certificates
2. Starts nginx with temporary certs
3. Obtains real Let's Encrypt certificates
4. Restarts nginx with real certificates

---

## 🔄 SSL Certificate Auto-Renewal

SSL certificates are **automatically renewed** by the certbot container.

The certbot service runs every 12 hours and checks for certificate expiration:

```yaml
certbot:
  image: certbot/certbot
  entrypoint: "/bin/sh -c 'trap exit TERM; while :; do certbot renew; sleep 12h & wait $${!}; done;'"
```

**No manual intervention required!** ✅

---

## 🛠️ Manual Operations

### View running containers:
```bash
docker ps
```

### View logs:
```bash
# Application logs
docker logs finance-app

# Nginx logs
docker logs finance-nginx

# Database logs
docker logs finance-db

# Follow logs in real-time
docker logs -f finance-app
```

### Restart specific service:
```bash
docker restart finance-app
# or
docker restart finance-nginx
```

### Stop all services:
```bash
cd /srv/apps/finance/docker
docker compose down
```

### Start all services:
```bash
cd /srv/apps/finance/docker
docker compose up -d
```

### Rebuild and restart:
```bash
cd /srv/apps/finance/docker
docker compose up -d --build
```

---

## 🔧 Manual SSL Certificate Renewal

If automatic renewal fails or you need to force renewal:

```bash
cd /srv/apps/finance/docker

# Check certificate status
docker compose run --rm certbot certificates

# Force renewal
docker compose run --rm certbot renew --force-renewal

# Restart nginx to apply new certificates
docker compose restart nginx
```

---

## 🗄️ Database Operations

### Backup database:
```bash
docker exec finance-db pg_dump -U test_user finance_db | gzip > backup_$(date +%Y%m%d).sql.gz
```

### Restore database:
```bash
gunzip < backup_20250121.sql.gz | docker exec -i finance-db psql -U test_user finance_db
```

### Connect to database:
```bash
docker exec -it finance-db psql -U test_user finance_db
```

---

## 📊 Health Check

### Application health:
```bash
curl http://localhost:8080/actuator/health
```

### Public endpoint:
```bash
curl https://api.tarasantoniuk.com/actuator/health
```

---

## 🐛 Troubleshooting

### Application won't start:
```bash
# Check logs
docker logs finance-app --tail 100

# Check if port is already in use
sudo lsof -i :8080

# Restart container
docker restart finance-app
```

### Nginx shows 502 Bad Gateway:
```bash
# Check if app is running
docker ps | grep finance-app

# Check app logs
docker logs finance-app

# Restart both
docker restart finance-app finance-nginx
```

### SSL certificate issues:
```bash
# Check certificate validity
docker compose run --rm certbot certificates

# Check nginx configuration
docker exec finance-nginx nginx -t

# View nginx error logs
docker logs finance-nginx --tail 50
```

### Database connection issues:
```bash
# Check if database is running
docker ps | grep finance-db

# Check database logs
docker logs finance-db --tail 50

# Verify connection from app container
docker exec finance-app ping db
```

---

## 📁 Directory Structure

```
/srv/apps/finance/docker/
├── app/                    # Application JAR
│   ├── Dockerfile
│   └── app.jar
├── nginx/                  # Nginx configuration
│   ├── conf.d/
│   │   └── default.conf
│   └── nginx.conf
├── certs/                  # SSL certificates (Let's Encrypt)
│   ├── live/
│   ├── archive/
│   └── renewal/
├── logs/                   # Application logs
│   └── nginx/
│       ├── access.log
│       └── error.log
├── data/                   # PostgreSQL data
│   └── postgres/
├── webroot/               # Certbot webroot for challenges
├── docker-compose.yml     # Main compose file
└── init-letsencrypt.sh    # SSL setup script
```

---

## 🔒 Security Notes

1. **SSL/TLS**: All traffic is encrypted via Let's Encrypt certificates
2. **Database**: PostgreSQL is not exposed externally (bound to 127.0.0.1)
3. **Application**: Only accessible through nginx reverse proxy
4. **Firewall**: UFW should be configured to allow only ports 22, 80, 443

---

## 📞 Support

For issues or questions:
- Check logs: `docker logs [container-name]`
- Review GitHub Actions: https://github.com/TarasAntoniuk/finance/actions
- API Documentation: https://api.tarasantoniuk.com/swagger-ui/index.html

---

**Last Updated**: November 2025