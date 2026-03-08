# Jitterbit Order API

API REST em Node.js para gerenciamento de pedidos. CRUD completo com mapeamento de campos entre o formato de entrada e o modelo interno. Desenvolvida para o desafio Jitterbit.

## Tecnologias

- **Node.js** (>= 18) + **Express**
- **PostgreSQL**
- **Docker** + **DevContainer**

## Padrões aplicados

- **Retry** – operações de banco com retentativa automática
- **Timeout** – limite de tempo por operação (configurável)
- **Idempotência** – header `Idempotency-Key` em POST/PUT para evitar duplicatas
- **Transactional Outbox** – eventos OrderCreated gravados em outbox na mesma transação

## Início rápido

### Com Docker (recomendado)

```bash
docker-compose up --build
```

Acesse:
- **API:** http://localhost:3000/order
- **Interface web:** http://localhost:3000

### Com DevContainer

1. Abra o projeto no VS Code
2. Command Palette → **Reopen in Container**
3. Aguarde o build e `npm install`
4. A API inicia com `npm run dev` (hot-reload)

### Local (PostgreSQL em localhost)

```bash
npm install
npm start
```

Variáveis de ambiente: `DB_HOST`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`, `DB_PORT`, `PORT`, `RETRY_ATTEMPTS`, `RETRY_DELAY_MS`, `OPERATION_TIMEOUT_MS`, `IDEMPOTENCY_TTL_MS`, `OUTBOX_POLL_MS`

## Endpoints

| Method | URL | Descrição |
|--------|-----|-----------|
| POST | /order | Criar pedido |
| GET | /order/:orderId | Obter pedido |
| GET | /order/list | Listar todos os pedidos |
| PUT | /order/:orderId | Atualizar pedido |
| DELETE | /order/:orderId | Deletar pedido |

## Exemplo de request

```bash
curl -X POST http://localhost:3000/order \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: uuid-unico-por-requisicao" \
  -d '{
    "numeroPedido": "v10089015vdb-01",
    "valorTotal": 10000,
    "dataCriacao": "2023-07-19T12:24:11.529Z",
    "items": [
      {"idItem": "2434", "quantidadeItem": 1, "valorItem": 1000}
    ]
  }'
```

## Mapping de campos

| Entrada | Saída |
|---------|-------|
| numeroPedido | orderId (remove sufixo -01) |
| valorTotal | value |
| dataCriacao | creationDate (ISO) |
| items[].idItem | items[].productId |
| items[].quantidadeItem | items[].quantity |
| items[].valorItem | items[].price |

## Interface web

Em http://localhost:3000 você pode:
- Criar pedidos (formulário)
- Listar pedidos (tabela)
- Remover pedidos (botão por linha)

## Testes

```bash
npm test
```

## Estrutura do projeto

```
jitterbit-order-api/
├── src/
│   ├── server.js       # initDb, outbox, listen
│   ├── clients/        # orderClient (lógica centralizada)
│   ├── controllers/    # thin, só chama client
│   ├── services/       # orderMapper
│   ├── routes/
│   ├── models/
│   ├── database/
│   └── utils/          # async, httpHandler, cache, etc.
├── public/             # Interface web
├── .devcontainer/
├── app.js
├── index.js            # Chama server.start()
├── Dockerfile
├── docker-compose.yml
└── package.json
```

Ver [ARCHITECTURE.md](./ARCHITECTURE.md) para detalhes da arquitetura.

## Licença

MIT
