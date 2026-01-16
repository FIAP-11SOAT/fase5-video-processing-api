# 📹 Video Processing API

## Rodar api localmente


### 1. Subir as dependências locais
```bash
docker-compose up -d
```

### 2. Criar o bucket s3 com localstack
```bash
aws --endpoint-url=http://localhost:4566 s3 mb s3://fase5-infra-hacka-videos
```

### 3. Rodar a aplicação localmente
```bash
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

