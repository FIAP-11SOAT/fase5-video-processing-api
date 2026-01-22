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

### 3. Criar o dynamoDB
```bash
aws --endpoint-url=http://localhost:4566 dynamodb create-table \
  --table-name fase5-infra-hacka-video-processing \
  --attribute-definitions \
      AttributeName=videoKey,AttributeType=S \
      AttributeName=userId,AttributeType=S \
  --key-schema \
      AttributeName=videoKey,KeyType=HASH \
  --global-secondary-indexes '[
    {
      "IndexName": "userId-index",
      "KeySchema": [
        {
          "AttributeName": "userId",
          "KeyType": "HASH"
        }
      ],
      "Projection": {
        "ProjectionType": "ALL"
      }
    }
  ]' \
  --billing-mode PAY_PER_REQUEST
```

### 4. Rodar a aplicação localmente
```bash
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

### 5. Conferir o vídeo no s3
```bash
aws --endpoint-url=http://localhost:4566 s3 ls s3://fase5-infra-hacka-videos --recursive
```

### 6. Conferir a tabela populada
- Troque pela video key mostrada no bucket
```bash
aws --endpoint-url=http://localhost:4566 dynamodb get-item   --table-name fase5-infra-hacka-video-processing   --key '{
"videoKey": { "S": "123/e6fcd8ee-9f11-4459-89eb-f0c59fa1502d" }
}'
```


