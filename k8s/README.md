# Kubernetes Deployment

This directory contains Kubernetes manifests for deploying JavaBase.

## Structure

```
k8s/
├── base/                    # Base manifests
│   ├── kustomization.yaml   # Kustomize configuration
│   ├── namespace.yaml       # Namespace definition
│   ├── serviceaccount.yaml  # Service account
│   ├── configmap.yaml       # Application configuration
│   ├── secret.yaml          # Secrets (template)
│   ├── deployment.yaml      # Deployment specification
│   ├── service.yaml         # ClusterIP service
│   ├── ingress.yaml         # Ingress configuration
│   ├── hpa.yaml             # Horizontal Pod Autoscaler
│   └── pdb.yaml             # Pod Disruption Budget
└── README.md
```

## Prerequisites

- Kubernetes cluster (1.25+)
- kubectl configured
- PostgreSQL database
- RabbitMQ message broker
- NGINX Ingress Controller (for Ingress)

## Quick Start

### 1. Build and Push Docker Image

```bash
# Build the image
docker build -t ghcr.io/justinwells85/javabase:latest .

# Push to registry
docker push ghcr.io/justinwells85/javabase:latest
```

### 2. Update Secrets

Edit `base/secret.yaml` with your actual credentials:

```yaml
stringData:
  DB_USERNAME: "your-db-user"
  DB_PASSWORD: "your-secure-password"
  RABBITMQ_USERNAME: "your-rabbitmq-user"
  RABBITMQ_PASSWORD: "your-secure-password"
```

### 3. Update Ingress

Edit `base/ingress.yaml` with your domain:

```yaml
spec:
  tls:
    - hosts:
        - your-domain.com
  rules:
    - host: your-domain.com
```

### 4. Deploy

```bash
# Preview the manifests
kubectl kustomize k8s/base

# Apply to cluster
kubectl apply -k k8s/base

# Verify deployment
kubectl -n javabase get all
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | `postgres-service` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `javabase` |
| `RABBITMQ_HOST` | RabbitMQ host | `rabbitmq-service` |
| `RABBITMQ_PORT` | RabbitMQ port | `5672` |

### Resource Limits

Default resource configuration:
- **Requests**: 512Mi memory, 250m CPU
- **Limits**: 1Gi memory, 1000m CPU

Adjust in `deployment.yaml` based on your workload.

### Scaling

HPA is configured to scale between 2-10 replicas based on:
- CPU utilization > 70%
- Memory utilization > 80%

## Production Considerations

1. **Secrets Management**: Use External Secrets Operator, Sealed Secrets, or Vault
2. **TLS Certificates**: Use cert-manager for automatic certificate management
3. **Network Policies**: Add network policies to restrict pod communication
4. **Resource Quotas**: Set namespace resource quotas
5. **Monitoring**: Deploy Prometheus and Grafana for observability

## Monitoring

The deployment exposes Prometheus metrics at `/actuator/prometheus`.

To scrape metrics, ensure your Prometheus is configured to discover pods with:
```yaml
prometheus.io/scrape: "true"
prometheus.io/port: "8080"
prometheus.io/path: "/actuator/prometheus"
```

## Troubleshooting

```bash
# Check pod status
kubectl -n javabase get pods

# View logs
kubectl -n javabase logs -l app.kubernetes.io/name=javabase

# Describe deployment
kubectl -n javabase describe deployment javabase

# Check events
kubectl -n javabase get events --sort-by='.lastTimestamp'
```
