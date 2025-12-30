# JavaBase Helm Chart

A Helm chart for deploying JavaBase - a production-ready Spring Boot microservice template.

## Prerequisites

- Kubernetes 1.25+
- Helm 3.0+
- PostgreSQL database
- RabbitMQ message broker

## Installation

### Add the repository (if published)

```bash
helm repo add javabase https://justinwells85.github.io/JavaBase
helm repo update
```

### Install from local chart

```bash
helm install my-javabase ./charts/javabase
```

### Install with custom values

```bash
helm install my-javabase ./charts/javabase \
  --set image.tag=v0.3.0 \
  --set database.host=my-postgres \
  --set rabbitmq.host=my-rabbitmq
```

## Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `replicaCount` | Number of replicas | `2` |
| `image.repository` | Image repository | `ghcr.io/justinwells85/javabase` |
| `image.tag` | Image tag | Chart appVersion |
| `image.pullPolicy` | Image pull policy | `IfNotPresent` |
| `service.type` | Kubernetes service type | `ClusterIP` |
| `service.port` | Service port | `80` |
| `ingress.enabled` | Enable ingress | `false` |
| `ingress.hosts` | Ingress hosts | `[]` |
| `resources.requests.memory` | Memory request | `512Mi` |
| `resources.requests.cpu` | CPU request | `250m` |
| `resources.limits.memory` | Memory limit | `1Gi` |
| `resources.limits.cpu` | CPU limit | `1000m` |
| `autoscaling.enabled` | Enable HPA | `true` |
| `autoscaling.minReplicas` | Minimum replicas | `2` |
| `autoscaling.maxReplicas` | Maximum replicas | `10` |
| `database.host` | PostgreSQL host | `postgres-service` |
| `database.port` | PostgreSQL port | `5432` |
| `database.name` | Database name | `javabase` |
| `rabbitmq.host` | RabbitMQ host | `rabbitmq-service` |
| `rabbitmq.port` | RabbitMQ port | `5672` |

## Secrets

The chart expects secrets for database and RabbitMQ credentials:

```bash
# Create database secret
kubectl create secret generic my-javabase-db \
  --from-literal=DB_USERNAME=postgres \
  --from-literal=DB_PASSWORD=your-password

# Create RabbitMQ secret
kubectl create secret generic my-javabase-rabbitmq \
  --from-literal=RABBITMQ_USERNAME=guest \
  --from-literal=RABBITMQ_PASSWORD=your-password
```

Or reference existing secrets:

```yaml
database:
  existingSecret: my-existing-db-secret
rabbitmq:
  existingSecret: my-existing-rabbitmq-secret
```

## Upgrading

```bash
helm upgrade my-javabase ./charts/javabase
```

## Uninstalling

```bash
helm uninstall my-javabase
```

## Development

### Lint the chart

```bash
helm lint ./charts/javabase
```

### Render templates locally

```bash
helm template my-javabase ./charts/javabase
```

### Package the chart

```bash
helm package ./charts/javabase
```
