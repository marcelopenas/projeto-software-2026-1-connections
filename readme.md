# Instalação

### Instalação do Mongo

Criação da rede

```
docker network create -d bridge rede
```

Executar o mongo na rede

```
docker run -d --network=rede --name mongo-connections -p 27017:27017 mongo:7
```

### Executar o Redis

```
docker run -p 6379:6379 -d --network=rede --name redis-2 redis
```

### Execução da Aplicação no Docker

```
docker build -t conexoes .
```


```
docker run --name conexoes -p 8080:8080 conexoes 
```

### Docker Compose

```
docker compose up
```


```
docker compose down
```