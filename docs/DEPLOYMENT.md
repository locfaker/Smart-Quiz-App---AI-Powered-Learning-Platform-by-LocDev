# Smart Quiz App - Production Deployment Guide

## 🎯 Tổng quan

Hướng dẫn này cung cấp các bước chi tiết để deploy Smart Quiz App lên production environment với high availability, security và performance optimization.

## 🏗️ Infrastructure Requirements

### Minimum Production Requirements

| Component | Specification |
|-----------|---------------|
| **CPU** | 8 cores (16 vCPUs recommended) |
| **RAM** | 16GB (32GB recommended) |
| **Storage** | 200GB SSD (500GB recommended) |
| **Network** | 1Gbps bandwidth |
| **OS** | Ubuntu 22.04 LTS |

### Recommended Production Setup

```mermaid
graph TB
    subgraph "Load Balancer Tier"
        A[Nginx Load Balancer]
        B[SSL Termination]
    end
    
    subgraph "Application Tier"
        C[API Server 1]
        D[API Server 2]
        E[API Server 3]
    end
    
    subgraph "Database Tier"
        F[PostgreSQL Primary]
        G[PostgreSQL Replica 1]
        H[PostgreSQL Replica 2]
    end
    
    subgraph "Cache Tier"
        I[Redis Cluster]
        J[Redis Sentinel]
    end
    
    subgraph "Monitoring"
        K[Prometheus]
        L[Grafana]
        M[ELK Stack]
    end
    
    A --> C
    A --> D
    A --> E
    
    C --> F
    D --> G
    E --> H
    
    C --> I
    D --> I
    E --> I
```

## 🚀 Deployment Options

### Option 1: Docker Compose (Single Server)

Phù hợp cho: Small to medium applications, development staging

```bash
# 1. Clone repository
git clone https://github.com/username/smart-quiz-app.git
cd smart-quiz-app

# 2. Setup environment
cp .env.example .env
# Edit .env with production values

# 3. Deploy
docker-compose -f docker-compose.prod.yml up -d

# 4. Verify deployment
curl -f http://localhost/api/v1/health
```

### Option 2: Kubernetes (Recommended)

Phù hợp cho: Production environments, high availability

```bash
# 1. Setup Kubernetes cluster
kubectl apply -f k8s/namespace.yml

# 2. Create secrets
kubectl create secret generic smartquiz-secrets \
  --from-literal=database-password=<password> \
  --from-literal=jwt-secret=<jwt-secret> \
  --from-literal=openai-api-key=<openai-key>

# 3. Deploy infrastructure
kubectl apply -f k8s/infrastructure/

# 4. Deploy application
kubectl apply -f k8s/application/

# 5. Verify deployment
kubectl get pods -n smartquiz
```

### Option 3: Cloud Deployment (AWS/GCP/Azure)

Phù hợp cho: Enterprise applications, global scale

## 🔧 Environment Configuration

### Production Environment Variables

```bash
# Application Settings
FLASK_ENV=production
SECRET_KEY=<strong-secret-key-32-chars>
JWT_SECRET_KEY=<jwt-secret-key-32-chars>

# Database Configuration
DATABASE_URL=postgresql://user:pass@db-host:5432/smartquiz_prod
DATABASE_POOL_SIZE=20
DATABASE_MAX_OVERFLOW=10

# Redis Configuration
REDIS_HOST=redis-cluster.internal
REDIS_PORT=6379
REDIS_PASSWORD=<redis-password>
REDIS_CLUSTER_ENABLED=true

# AI Services
OPENAI_API_KEY=<openai-api-key>
GEMINI_API_KEY=<gemini-api-key>
AI_REQUEST_TIMEOUT=30
AI_MAX_RETRIES=3

# Security
CORS_ORIGINS=https://smartquiz.app,https://admin.smartquiz.app
SESSION_COOKIE_SECURE=true
SESSION_COOKIE_HTTPONLY=true

# Monitoring
SENTRY_DSN=<sentry-dsn>
PROMETHEUS_ENABLED=true
GRAFANA_ADMIN_PASSWORD=<grafana-password>

# Performance
GUNICORN_WORKERS=4
GUNICORN_TIMEOUT=120
CELERY_WORKERS=8
```

## 🗄️ Database Setup

### PostgreSQL Production Configuration

```sql
-- Create production database
CREATE DATABASE smartquiz_prod;
CREATE USER smartquiz WITH PASSWORD '<strong-password>';
GRANT ALL PRIVILEGES ON DATABASE smartquiz_prod TO smartquiz;

-- Performance tuning
ALTER SYSTEM SET shared_buffers = '4GB';
ALTER SYSTEM SET effective_cache_size = '12GB';
ALTER SYSTEM SET maintenance_work_mem = '1GB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;
ALTER SYSTEM SET random_page_cost = 1.1;
ALTER SYSTEM SET effective_io_concurrency = 200;

-- Reload configuration
SELECT pg_reload_conf();
```

### Database Migration

```bash
# Run migrations
cd server
export FLASK_APP=app.py
export DATABASE_URL=<production-database-url>

flask db upgrade

# Verify migration
flask db current
```

### Backup Strategy

```bash
#!/bin/bash
# backup-db.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups/postgresql"
DB_NAME="smartquiz_prod"

# Create backup
pg_dump -h localhost -U smartquiz -d $DB_NAME | gzip > $BACKUP_DIR/smartquiz_$DATE.sql.gz

# Keep only last 30 days
find $BACKUP_DIR -name "smartquiz_*.sql.gz" -mtime +30 -delete

# Upload to S3 (optional)
aws s3 cp $BACKUP_DIR/smartquiz_$DATE.sql.gz s3://smartquiz-backups/
```

## 🔄 Redis Configuration

### Redis Cluster Setup

```bash
# redis.conf
port 7000
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 5000
appendonly yes
appendfsync everysec
save 900 1
save 300 10
save 60 10000
maxmemory 2gb
maxmemory-policy allkeys-lru
```

### Redis Monitoring

```bash
# Monitor Redis performance
redis-cli --latency-history -i 1
redis-cli info memory
redis-cli info stats
```

## 🌐 Load Balancer Configuration

### Nginx Configuration

```nginx
# /etc/nginx/sites-available/smartquiz
upstream backend {
    least_conn;
    server 10.0.1.10:5000 max_fails=3 fail_timeout=30s;
    server 10.0.1.11:5000 max_fails=3 fail_timeout=30s;
    server 10.0.1.12:5000 max_fails=3 fail_timeout=30s;
}

server {
    listen 80;
    server_name smartquiz.app www.smartquiz.app;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name smartquiz.app www.smartquiz.app;

    # SSL Configuration
    ssl_certificate /etc/letsencrypt/live/smartquiz.app/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/smartquiz.app/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    # Security Headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options DENY always;
    add_header X-Content-Type-Options nosniff always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;

    # Rate Limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    limit_req_zone $binary_remote_addr zone=login:10m rate=1r/s;

    # API Routes
    location /api/ {
        limit_req zone=api burst=20 nodelay;
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
    }

    # Login endpoint with stricter rate limiting
    location /api/v1/auth/login {
        limit_req zone=login burst=5 nodelay;
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Static files
    location /static/ {
        alias /var/www/smartquiz/static/;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Health check
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
```

## 🔒 SSL/TLS Setup

### Let's Encrypt Certificate

```bash
# Install Certbot
sudo apt update
sudo apt install certbot python3-certbot-nginx

# Obtain certificate
sudo certbot --nginx -d smartquiz.app -d www.smartquiz.app

# Auto-renewal
sudo crontab -e
# Add: 0 12 * * * /usr/bin/certbot renew --quiet
```

### Certificate Monitoring

```bash
#!/bin/bash
# check-ssl.sh

DOMAIN="smartquiz.app"
EXPIRY_DATE=$(echo | openssl s_client -servername $DOMAIN -connect $DOMAIN:443 2>/dev/null | openssl x509 -noout -dates | grep notAfter | cut -d= -f2)
EXPIRY_EPOCH=$(date -d "$EXPIRY_DATE" +%s)
CURRENT_EPOCH=$(date +%s)
DAYS_UNTIL_EXPIRY=$(( ($EXPIRY_EPOCH - $CURRENT_EPOCH) / 86400 ))

if [ $DAYS_UNTIL_EXPIRY -lt 30 ]; then
    echo "SSL certificate expires in $DAYS_UNTIL_EXPIRY days!"
    # Send alert
fi
```

## 📊 Monitoring Setup

### Prometheus Configuration

```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "alert_rules.yml"

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

scrape_configs:
  - job_name: 'smartquiz-api'
    static_configs:
      - targets: ['api-server-1:5000', 'api-server-2:5000']
    metrics_path: '/metrics'
    scrape_interval: 30s

  - job_name: 'postgres'
    static_configs:
      - targets: ['postgres-exporter:9187']

  - job_name: 'redis'
    static_configs:
      - targets: ['redis-exporter:9121']

  - job_name: 'nginx'
    static_configs:
      - targets: ['nginx-exporter:9113']
```

### Alert Rules

```yaml
# alert_rules.yml
groups:
  - name: smartquiz_alerts
    rules:
      - alert: HighErrorRate
        expr: rate(flask_http_request_exceptions_total[5m]) > 0.1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }} errors per second"

      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(flask_http_request_duration_seconds_bucket[5m])) > 0.5
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time detected"
          description: "95th percentile response time is {{ $value }} seconds"

      - alert: DatabaseConnectionFailure
        expr: up{job="postgres"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Database connection failure"
          description: "PostgreSQL database is down"
```

## 🔄 CI/CD Pipeline

### GitHub Actions Deployment

```yaml
# .github/workflows/deploy-production.yml
name: Deploy to Production

on:
  push:
    branches: [main]
    tags: ['v*']

jobs:
  deploy:
    runs-on: ubuntu-latest
    environment: production
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Deploy to production
        uses: appleboy/ssh-action@v1.0.0
        with:
          host: ${{ secrets.PRODUCTION_HOST }}
          username: ${{ secrets.PRODUCTION_USER }}
          key: ${{ secrets.PRODUCTION_SSH_KEY }}
          script: |
            cd /opt/smartquiz-production
            git pull origin main
            docker-compose pull
            docker-compose up -d --no-deps backend
            docker system prune -f
            
      - name: Health check
        run: |
          sleep 60
          curl -f https://smartquiz.app/api/v1/health
          
      - name: Rollback on failure
        if: failure()
        uses: appleboy/ssh-action@v1.0.0
        with:
          host: ${{ secrets.PRODUCTION_HOST }}
          username: ${{ secrets.PRODUCTION_USER }}
          key: ${{ secrets.PRODUCTION_SSH_KEY }}
          script: |
            cd /opt/smartquiz-production
            docker-compose down
            git checkout HEAD~1
            docker-compose up -d
```

## 🛡️ Security Hardening

### Server Security

```bash
#!/bin/bash
# security-hardening.sh

# Update system
apt update && apt upgrade -y

# Configure firewall
ufw default deny incoming
ufw default allow outgoing
ufw allow ssh
ufw allow 80/tcp
ufw allow 443/tcp
ufw enable

# Disable root login
sed -i 's/PermitRootLogin yes/PermitRootLogin no/' /etc/ssh/sshd_config
systemctl restart ssh

# Install fail2ban
apt install fail2ban -y
systemctl enable fail2ban
systemctl start fail2ban

# Setup automatic security updates
apt install unattended-upgrades -y
dpkg-reconfigure -plow unattended-upgrades
```

### Application Security

```python
# security_middleware.py
from flask import request, abort
import time
from collections import defaultdict

class SecurityMiddleware:
    def __init__(self, app):
        self.app = app
        self.rate_limits = defaultdict(list)
        
    def __call__(self, environ, start_response):
        # Rate limiting
        client_ip = environ.get('REMOTE_ADDR')
        current_time = time.time()
        
        # Clean old requests
        self.rate_limits[client_ip] = [
            req_time for req_time in self.rate_limits[client_ip]
            if current_time - req_time < 3600  # 1 hour window
        ]
        
        # Check rate limit
        if len(self.rate_limits[client_ip]) > 1000:  # 1000 requests per hour
            start_response('429 Too Many Requests', [])
            return [b'Rate limit exceeded']
        
        self.rate_limits[client_ip].append(current_time)
        
        return self.app(environ, start_response)
```

## 📈 Performance Optimization

### Database Optimization

```sql
-- Create indexes for better performance
CREATE INDEX CONCURRENTLY idx_quizzes_user_completed 
ON quizzes(user_id, completed_at) WHERE is_completed = true;

CREATE INDEX CONCURRENTLY idx_questions_subject_difficulty 
ON questions(subject, difficulty) WHERE is_active = true;

-- Analyze tables
ANALYZE quizzes;
ANALYZE questions;
ANALYZE users;

-- Monitor slow queries
ALTER SYSTEM SET log_min_duration_statement = 1000;
SELECT pg_reload_conf();
```

### Application Performance

```python
# performance_monitoring.py
import time
import functools
from flask import g
import logging

def monitor_performance(func):
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        start_time = time.time()
        try:
            result = func(*args, **kwargs)
            return result
        finally:
            duration = time.time() - start_time
            logging.info(f"{func.__name__} took {duration:.3f} seconds")
            
            # Alert on slow operations
            if duration > 5.0:
                logging.warning(f"Slow operation detected: {func.__name__} took {duration:.3f}s")
    
    return wrapper
```

## 🔄 Backup and Recovery

### Automated Backup Script

```bash
#!/bin/bash
# backup-system.sh

BACKUP_DIR="/backups"
DATE=$(date +%Y%m%d_%H%M%S)
S3_BUCKET="smartquiz-backups"

# Database backup
pg_dump -h localhost -U smartquiz smartquiz_prod | gzip > $BACKUP_DIR/db_$DATE.sql.gz

# Redis backup
redis-cli --rdb $BACKUP_DIR/redis_$DATE.rdb

# Application files backup
tar -czf $BACKUP_DIR/app_$DATE.tar.gz /opt/smartquiz-production

# Upload to S3
aws s3 sync $BACKUP_DIR s3://$S3_BUCKET/$(date +%Y/%m/%d)/

# Cleanup local backups (keep 7 days)
find $BACKUP_DIR -name "*_*.gz" -mtime +7 -delete
find $BACKUP_DIR -name "*_*.rdb" -mtime +7 -delete
find $BACKUP_DIR -name "*_*.tar.gz" -mtime +7 -delete
```

### Disaster Recovery Plan

1. **RTO (Recovery Time Objective)**: 4 hours
2. **RPO (Recovery Point Objective)**: 1 hour
3. **Backup Frequency**: Every 6 hours
4. **Testing**: Monthly disaster recovery drills

## 📋 Deployment Checklist

### Pre-Deployment

- [ ] Environment variables configured
- [ ] SSL certificates installed
- [ ] Database migrations tested
- [ ] Load balancer configured
- [ ] Monitoring setup complete
- [ ] Backup strategy implemented
- [ ] Security hardening applied

### Deployment

- [ ] Application deployed
- [ ] Health checks passing
- [ ] Database connectivity verified
- [ ] Redis connectivity verified
- [ ] API endpoints responding
- [ ] SSL certificate valid

### Post-Deployment

- [ ] Performance metrics normal
- [ ] Error rates acceptable
- [ ] User authentication working
- [ ] AI services responding
- [ ] Monitoring alerts configured
- [ ] Backup verification complete

## 🚨 Troubleshooting

### Common Issues

1. **High Memory Usage**
   ```bash
   # Check memory usage
   free -h
   docker stats
   
   # Restart services if needed
   docker-compose restart backend
   ```

2. **Database Connection Issues**
   ```bash
   # Check PostgreSQL status
   systemctl status postgresql
   
   # Check connections
   sudo -u postgres psql -c "SELECT * FROM pg_stat_activity;"
   ```

3. **Redis Connection Issues**
   ```bash
   # Check Redis status
   redis-cli ping
   
   # Check memory usage
   redis-cli info memory
   ```

### Log Analysis

```bash
# Application logs
docker-compose logs -f backend

# Nginx logs
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log

# System logs
journalctl -u smartquiz-backend -f
```

## 📞 Support

Để được hỗ trợ deployment:
- **Email**: devops@smartquiz.app
- **Slack**: #deployment-support
- **Documentation**: https://docs.smartquiz.app/deployment

---

**⚠️ Lưu ý**: Luôn test deployment trên staging environment trước khi deploy lên production!