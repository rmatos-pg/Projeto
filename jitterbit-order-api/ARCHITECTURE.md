# Arquitetura - Jitterbit Order API

## Visão geral

API REST em Node.js com arquitetura em camadas: rotas → controllers → clients → models. Persistência em PostgreSQL. Lógica centralizada no OrderClient.

## Diagrama de componentes

```
                    +------------------+
                    |   Cliente HTTP   |
                    +--------+---------+
                             |
         POST/GET/PUT/DELETE /order
                             |
                             v
+------------------------------------------------------------------+
|                         Express (porta 3000)                      |
|  +----------------+  +----------------+  +------------------------+|
|  | idempotency    |  | /order/*       |  | / (static)     |  | public/index.html (UI) ||
|  | middleware     |  | orderRoutes    |  | express.static |  |                        ||
|  +---------------+-+-------┬--------+  +----------------+  +------------------------+|
+------------------|---------|--------------------------------------------------------+
                   |         |
                   v         v
+------------------+  +------------------+
| idempotency      |  | orderController  |  Timeout, validação, status HTTP
| (Idempotency-Key)|  +--------+---------+
+------------------+           |
                                v
+------------------+
| orderClient      |  Toda a lógica: retry, timeout, cache, Transactional Outbox
+--------+---------+
         |
    +----+----+----+----+
    v         v    v    v
+--------+ +--------+ +------------+ +----------------+
| Order  | | Item   | |orderMapper | | database       |
| model  | | model  | |            | | connection     |
+--------+ +--------+ +------------+ +----------------+
    |         |           |              |
    +---------+-----------+--------------+
                         |
                         v
                  +--------------+
                  | PostgreSQL   |
                  | orders/items |
                  +--------------+
```

## Camadas

| Camada | Responsabilidade |
|--------|------------------|
| **routes** | Definição de rotas e mapeamento para controllers |
| **controllers** | Thin: validação, chama client, monta resposta HTTP (handleAsync) |
| **clients** | orderClient: toda a lógica, retry, timeout, cache, outbox |
| **services** | orderMapper: mapeamento input/output |
| **models** | Acesso a dados (Order, Item, Outbox) |
| **database** | Pool PostgreSQL, criação de tabelas, retry de conexão |
| **utils** | logAction, cache, configLoader, async, httpHandler |

## Estrutura de pastas

```
jitterbit-order-api/
├── src/
│   ├── controllers/    # Lógica HTTP, validação, status codes
│   ├── services/       # Regras de negócio, orderMapper
│   ├── routes/         # Definição de rotas Express
│   ├── clients/        # orderClient (toda a lógica, retry, timeout, outbox)
│   ├── models/         # Order, Item, Outbox
│   ├── middleware/     # idempotency
│   ├── outbox/         # publisher
│   ├── database/       # connection.js (Pool PostgreSQL)
│   └── utils/          # logAction, cache, configLoader, async, httpHandler
├── public/             # Interface web (index.html)
├── .devcontainer/      # Configuração DevContainer
├── app.js              # Configuração Express
├── index.js            # Chama server.start()
├── src/server.js       # initDb, outbox, listen
├── Dockerfile
├── docker-compose.yml
└── package.json
```

## Fluxo de dados (exemplo: criar pedido)

1. **Request** → `POST /order` com JSON (numeroPedido, valorTotal, dataCriacao, items)
2. **orderController** → Valida `numeroPedido`, chama `orderClient.createOrder()`
3. **orderClient** → `execute()` (retry+timeout) → `createOrderWithOutbox()` → `mapOrderToOutput()`
4. **Response** → `201` com JSON (orderId, value, creationDate, items)

## Modelo de dados (PostgreSQL)

| Tabela | Colunas |
|--------|---------|
| **orders** | orderId (PK), value, creationDate |
| **items** | orderId (FK), productId, quantity, price |
| **outbox_events** | id, aggregate_type, aggregate_id, event_type, payload, status |

## Padrões

| Padrão | Onde |
|--------|------|
| **Retry** | orderClient.execute() usa withRetry, async.js |
| **Timeout** | orderClient.execute() usa withTimeout, async.js |
| **Idempotência** | middleware idempotency (header Idempotency-Key) |
| **Transactional Outbox** | createOrderWithOutbox (Order + Items + Outbox na mesma transação), publisher.js |

## Docker

- **api**: Node 20 Alpine, porta 3000, depende do PostgreSQL
- **postgres**: PostgreSQL 15, porta 5432, healthcheck antes de subir a API
