# Golden Raspberry Awards API

API RESTful para leitura da lista de indicados e vencedores da categoria **Pior Filme** do
Golden Raspberry Awards. Ao iniciar, a aplicação lê um CSV, grava os dados em um banco H2 em
memória e expõe, entre outros recursos, o produtor com **maior** intervalo entre dois prêmios
consecutivos e o que obteve dois prêmios **mais rápido**.

## Requisitos

- **Java 21** (JDK)
- Nada mais: o Maven é baixado pelo wrapper (`mvnw`) e o banco é embarcado (H2 em memória).

## Executando

```bash
# Linux / macOS / Git Bash
./mvnw spring-boot:run

# Windows (cmd / PowerShell)
mvnw.cmd spring-boot:run
```

Ou gerando o jar:

```bash
./mvnw package
java -jar target/razzies-api-1.0.0-SNAPSHOT.jar
```

A API sobe em `http://localhost:8080`.

### Interface web

Abra **http://localhost:8080/** para uma interface simples, servida pela própria aplicação
(`src/main/resources/static/index.html`, HTML e JavaScript puros, sem build). O menu lateral lista os
resources (**Producers**, **Movies**, **Studios**) e, como submenus, as ações de cada um (listar com
filtros e paginação, buscar por ID, criar, atualizar, excluir). A tela inicial é o
**Award intervals**, o endpoint da avaliação, com os produtores de menor e maior intervalo. Cada tela
mostra a requisição HTTP feita, o status, o resultado em tabela e o JSON da resposta.

### Usando outro CSV

Por padrão é carregado `src/main/resources/data/movielist.csv` (cópia do `Movielist.csv` da proposta).
Para usar outro arquivo, sem recompilar:

```bash
java -jar target/razzies-api-1.0.0-SNAPSHOT.jar --razzies.data.csv-location=file:/caminho/movielist.csv
# ou
./mvnw spring-boot:run -Dspring-boot.run.arguments=--razzies.data.csv-location=file:/caminho/movielist.csv
```

Formato esperado (cabeçalho obrigatório; ordem das colunas indiferente):

```
year;title;studios;producers;winner
1980;Can't Stop the Music;Associated Film Distribution;Allan Carr;yes
```

- Produtores separados por `,` e/ou `and` (`A, B and C`, `A, B, and C`); estúdios por `,`.
- Valores podem vir entre aspas duplas (`"Producer A"`, `"Studio 1, Studio 2"`, `""` escapa uma aspa).
- `winner`: `yes`, `y` ou `true` (sem diferenciar maiúsculas) para vencedor; qualquer outro valor
  (vazio, `no`...) é indicado. Valores inesperados geram um aviso no log, sem impedir a carga.
- Linhas do mesmo filme (mesmo ano e título, ignorando maiúsculas e espaços extras) são **mescladas**:
  produtores e estúdios são unidos e o filme é vencedor se alguma linha disser que é. Assim uma linha
  repetida não vira uma segunda vitória no mesmo ano, e nenhum produtor é perdido.
- Espaços extras, linhas em branco, BOM UTF-8 e quebras de linha `CRLF` são tolerados.
- Conteúdo inválido (ano não numérico, título vazio, coluna ausente, aspas não fechadas, arquivo
  inexistente) **interrompe a inicialização** com a linha do erro, para nunca servir resultados incorretos.

## Executando os testes

O projeto contém **somente testes de integração** (`*IT.java`): cada um sobe a aplicação inteira
(carga real do CSV, JPA, H2 e camada HTTP) e valida as respostas da API.

```bash
./mvnw verify        # Windows: mvnw.cmd verify
```

| Classe | O que garante |
|---|---|
| `AwardIntervalsIT` | Resposta exata de `/award-intervals` para o CSV da proposta |
| `AwardIntervalScenariosIT` | Outros conjuntos de dados (`src/test/resources/csv`): empates em min/max, mesmo produtor em min e max, apenas vitórias **consecutivas**, indicações ignoradas, vitórias no mesmo ano, nenhum intervalo, variações de formato do CSV, valores entre aspas, filmes duplicados e valores de `winner` |
| `MovieDataLoadIT` | Carga completa (206 filmes, 42 vencedores), separação de produtores e falha de inicialização com CSV inválido |
| `MovieApiIT` | CRUD de filmes, paginação, filtros, status HTTP e corpo de erro |
| `ProducerAndStudioApiIT` | Consulta de produtores e estúdios |
| `WebUiIT` | A interface web é servida pela aplicação |

## API

Documentação interativa (OpenAPI): **http://localhost:8080/swagger-ui.html**

### Intervalo de prêmios

```http
GET /api/v1/producers/award-intervals
```

```json
{
  "min": [
    { "producer": "Joel Silver", "interval": 1, "previousWin": 1990, "followingWin": 1991 }
  ],
  "max": [
    { "producer": "Matthew Vaughn", "interval": 13, "previousWin": 2002, "followingWin": 2015 }
  ]
}
```

### Recursos

| Método | Recurso | Descrição | Sucesso |
|---|---|---|---|
| GET | `/api/v1/movies` | Lista paginada. Filtros: `year`, `winner`, `title`, `producer`, `studio` | 200 |
| GET | `/api/v1/movies/{id}` | Um filme | 200 |
| POST | `/api/v1/movies` | Cria um filme (`Location` aponta para o novo recurso) | 201 |
| PUT | `/api/v1/movies/{id}` | Substitui um filme | 200 |
| DELETE | `/api/v1/movies/{id}` | Remove um filme | 204 |
| GET | `/api/v1/producers` | Lista paginada. Filtro: `name` | 200 |
| GET | `/api/v1/producers/{id}` | Um produtor | 200 |
| GET | `/api/v1/producers/award-intervals` | Menor e maior intervalo entre vitórias consecutivas | 200 |
| GET | `/api/v1/studios` | Lista paginada. Filtro: `name` | 200 |
| GET | `/api/v1/studios/{id}` | Um estúdio | 200 |

Paginação: `?page=0&size=20&sort=year,desc` (máximo de 100 por página).

Erros seguem o padrão **RFC 9457 (Problem Details)**: `400` (validação, JSON malformado, parâmetro
ou ordenação inválidos), `404`, `405`, `409` e `415`.

```bash
curl "http://localhost:8080/api/v1/movies?winner=true&producer=silver"

curl -i -X POST http://localhost:8080/api/v1/movies \
  -H "Content-Type: application/json" \
  -d '{"year": 2025, "title": "New Movie", "winner": true, "producers": ["Joel Silver"], "studios": ["Warner Bros."]}'
```

Como os filmes podem ser alterados pela API, o intervalo de prêmios é sempre calculado sobre o
estado atual da base.

### Outros endpoints

- Health check: `http://localhost:8080/actuator/health`
- Console do H2: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:razzies`, usuário `sa`, sem senha)

## Arquitetura

Arquitetura **hexagonal** (portas e adaptadores), organizada **por feature** (`movie`, `producer`,
`studio`, `dataload`, `shared`). Cada feature tem as mesmas camadas:

```
<feature>/
├── domain/                     Modelo e regras de negócio, Java puro (sem Spring nem JPA)
├── application/
│   ├── port/in/                Portas de entrada: casos de uso (interfaces) e seus comandos
│   ├── port/out/               Portas de saída: o que o núcleo precisa do mundo externo (repositórios)
│   └── *Service                Implementação dos casos de uso, orquestrando domínio e portas de saída
└── adapter/
    ├── in/web/                 Adaptador de entrada HTTP: controllers e DTOs de request/response
    ├── in/csv/                 (dataload) Adaptador de entrada que lê o CSV na inicialização
    └── out/persistence/        Adaptador de saída JPA: entidades, Spring Data e implementação das portas
```

- As dependências apontam sempre para dentro: `adapter → application → domain`. Nem o domínio nem a
  aplicação importam classes de adaptadores, de JPA ou de Spring MVC.
- Exemplo: `GET /award-intervals` entra por `ProducerController` (adaptador web), chama a porta
  `GetAwardIntervalsUseCase`, implementada por `AwardIntervalService`, que lê as vitórias pela porta
  `ProducerWinRepository` (implementada por `ProducerWinPersistenceAdapter`, uma única consulta JPQL)
  e aplica a regra de domínio `AwardIntervalCalculator`.
- A paginação usa tipos próprios (`PageQuery`, `PageResult`), traduzidos de/para Spring Data nos adaptadores.
- Concessão pragmática: os serviços de aplicação usam `@Service` e `@Transactional` do Spring, para não
  duplicar a gestão de transações em uma camada extra.

## Decisões de projeto

- **Stack:** Java 21, Spring Boot 4.1 (Web MVC, Data JPA, Validation, Actuator), H2, Commons CSV, springdoc-openapi.
- **Modelo normalizado:** `movie`, `producer` e `studio` com tabelas de associação N:N. O cálculo
  por produtor fica correto mesmo quando um filme tem vários produtores, e um produtor é a mesma
  entidade em qualquer filme (nomes normalizados quanto a espaços).
- **Regra do intervalo** (`AwardIntervalCalculator`): agrupa os anos de vitória por produtor,
  ordena e compara apenas vitórias **consecutivas**. Retorna **todos** os empates do mínimo e do
  máximo, com ordenação determinística (produtor, ano). Duas vitórias no mesmo ano resultam em
  intervalo `0`. O custo é O(n log n), sobre uma única consulta que traz apenas `(produtor, ano)`
  dos vencedores.
- **Carga antes de aceitar requisições:** o CSV é importado antes de o servidor web subir, numa
  única transação (tudo ou nada), com inserções em lote e resolução de produtores e estúdios em
  uma consulta. Linhas duplicadas (mesmo ano e título) são mescladas com um aviso no log, porque
  contariam como uma segunda vitória no mesmo ano.
- **REST nível 2 de Richardson:** recursos nomeados por substantivos, verbos HTTP com semântica
  correta, status codes adequados (`201` + `Location`, `204`, `404`, `405`...), DTOs desacoplados
  das entidades e versionamento por URI (`/api/v1`).
- **Testes:** só de integração, como pede a proposta. Cada contexto de teste usa um banco H2
  próprio (`@IntegrationTest`), para que cenários com CSVs diferentes não interfiram entre si.
  Testes que alteram dados fazem rollback.

## Uso de IA

O projeto foi desenvolvido com o **Claude Code** (agente de IA da Anthropic). O registro completo
das interações está em [`docs/ai-log/`](docs/ai-log/README.md): todos os prompts, as respostas
do agente e cada ferramenta executada (comandos, edições de arquivos e seus resultados).

O registro é gerado a partir dos transcripts locais do Claude Code:

```bash
python scripts/export-ai-log.py
```
