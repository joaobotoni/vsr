# vsr

API REST para gestão de **vistorias de imóveis**: cadastro de pessoas e empresas, imóveis e seus proprietários, e o registro de vistorias de entrada e saída, organizadas em ambientes, itens e evidências fotográficas.

O módulo de **identidade e acesso** (cadastro, login, sessões por dispositivo, tokens e troca de senha) está implementado na API. O restante do domínio já está modelado no banco de dados (migrations `V1` a `V6`) e será exposto pela API nas próximas etapas.

---

## Sumário

- [Stack](#stack)
- [Arquitetura](#arquitetura)
- [Modelo de domínio](#modelo-de-domínio)
- [Regras de negócio](#regras-de-negócio)
  - [Cadastro](#cadastro)
  - [Login, dispositivos e sessões](#login-dispositivos-e-sessões)
  - [Tokens](#tokens)
  - [Logout e troca de senha](#logout-e-troca-de-senha)
  - [Validação de dados](#validação-de-dados)
  - [Limite de requisições](#limite-de-requisições)
  - [Regras do domínio de vistorias](#regras-do-domínio-de-vistorias)
- [Segurança](#segurança)
- [API](#api)
  - [Versionamento](#versionamento)
  - [Endpoints](#endpoints)
  - [Exemplos](#exemplos)
  - [Formato de erro](#formato-de-erro)
- [Configuração](#configuração)
- [Execução](#execução)
- [Logs](#logs)
- [Testes](#testes)

---

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 4.1 (Web MVC, Security, Data JPA, Validation, Mail) |
| Persistência | PostgreSQL 13+, Hibernate 7, Flyway |
| Autenticação | JWT HS256 (`com.auth0:java-jwt`) e refresh token opaco com hash HMAC-SHA-256 |
| Senhas | Argon2id (Spring Security + BouncyCastle) |
| Mapeamento | MapStruct 1.6 e Lombok |
| Testes | JUnit, Mockito, MockMvc, Spring Security Test |
| Build | Maven (wrapper incluso) |

---

## Arquitetura

O projeto é organizado **por camada**, nunca por funcionalidade. Cada pacote contém uma única natureza de classe.

```
com.botoni.vsr
├── configuration     Classes @Configuration (segurança, filtros, JWT, refresh token, senha, rate limit, web)
├── controller        Controllers REST
├── database
│   ├── converter     AttributeConverters dos value objects e enums
│   ├── entity        Entidades JPA
│   ├── enums         Enums persistidos (DevicePlatform, PersonType)
│   └── repository    Repositórios Spring Data, incluindo chamadas a procedures
├── dto
│   ├── request       Corpos de requisição
│   └── response      Corpos de resposta
├── email             Contratos de envio de e-mail
├── exception
│   ├── custom        Exceções de domínio
│   ├── enums         Catálogo de problemas (*Problem) e de constraints do banco
│   ├── handler       Um @RestControllerAdvice por exceção
│   └── lib           Infraestrutura de ProblemDetail e resolução de constraints
├── filter            Filtros servlet (exceção, rate limit, autenticação)
├── lib               Classes puras (Modulo11, TokenBucket)
├── mapper            Mappers MapStruct
├── properties        @ConfigurationProperties
├── ratelimit         Políticas, rotas e chave de rede do rate limit
├── security          JWT, token opaco (HMAC) e Principal
├── service           Serviços de entidade, de caso de uso e de infraestrutura
└── vo                Value objects (Email, Password, PasswordHash, Cpf, Cnpj)
```

### Serviços

| Tipo | Serviços | Responsabilidade |
|---|---|---|
| Entidade | `IndividualService`, `UserService`, `LocalCredentialService`, `DeviceService`, `SessionService`, `RefreshTokenService` | Mexem apenas no próprio repositório e guardam as regras da própria entidade |
| Caso de uso | `RegisterService`, `LoginService`, `AccessService`, `RefreshService`, `ChangePasswordService` | Apenas orquestram outros serviços |
| Infraestrutura | `TokenService`, `LoginAttemptService`, `EmailService` | Isolam JWT, limite de tentativas por conta e envio de e-mail |

### Convenções

- O método público de um serviço lista os passos do caso de uso; cada passo é um método privado com uma única intenção, na ordem em que é chamado.
- Dependências externas (repositório, mapper, JWT, envio de e-mail) só são chamadas em métodos privados.
- Nomes: `create` monta sem persistir, `persist` grava, `find` busca e lança exceção quando não encontra, `is…`/`has…` retorna booleano. Uma classe nunca tem dois métodos com o mesmo nome.
- **Fail fast**: todo `if` fica no topo da função e o corpo só lança ou retorna. Quando a checagem depende de um valor buscado, o valor passa por uma função que começa pelos `if` e o devolve, como `active(find(user, session))` e `matched(find(user), password)`. Efeitos colaterais condicionais viram um passo próprio (`if (!condição) return;`), como `revokeIfReused`.
- **Value objects** concentram a validação. Uma senha nunca trafega como `String`: DTOs e serviços recebem `Password`, e o valor só é extraído na borda com APIs externas.
- Escritas que vão além do CRUD são **procedures no banco**, chamadas com `@Procedure`.
- Violações de regra no banco usam o padrão `rn_<nome>` e são traduzidas pelo `ConstraintExceptionHandler`, pelo enum `RuleConstraint`, para o status HTTP de cada regra.

### Cadeia de filtros

Toda requisição passa, nesta ordem, por:

1. `ExceptionFilter`: converte exceções lançadas nos filtros em `ProblemDetail`.
2. `RateLimitFilter`: aplica o limite da rota por rede do cliente e devolve os cabeçalhos `X-RateLimit-*`.
3. `AuthenticationFilter`: valida o JWT, carrega o usuário e confirma que a sessão está ativa. É ignorado nas rotas públicas.

---

## Modelo de domínio

O banco é dividido em schemas por contexto:

| Schema | Conteúdo |
|---|---|
| `documento` | Funções de validação reutilizadas pelas constraints (CPF, CNPJ, CEP, UF, telefone, e-mail, texto) |
| `pessoas` | Pessoa (física ou jurídica), contato e titularidade |
| `usuarios` | Usuário, credencial local, credencial social, dispositivo, sessão, refresh token e histórico de refresh tokens usados |
| `vinculos` | Vínculo entre pessoa física e empresa |
| `enderecos` | Estado, cidade, bairro, logradouro e endereço |
| `imoveis` | Imóvel e seus proprietários |
| `catalogo` | Tipos de ambiente e de item, padrões do sistema ou personalizados |
| `vistorias` | Vistoria, pessoas vinculadas, ambiente, item e evidência |

As migrations ficam em `src/main/resources/db/migration`:

| Migration | Conteúdo |
|---|---|
| `V1__schema.sql` | Schemas |
| `V2__functions.sql` | Funções de validação e de `updated_at` |
| `V3__ddl.sql` | Tipos, tabelas, constraints e índices |
| `V4__procedures.sql` | Procedures de dispositivo, sessão e refresh token |
| `V5__triggers.sql` | Triggers de `updated_at` |
| `V6__initial_data.sql` | Estados e catálogo inicial de ambientes e itens |

### Procedures

| Procedure | Efeito |
|---|---|
| `registrar_dispositivo` | Cria ou atualiza o dispositivo pelo par (usuário, identificador) |
| `registrar_acesso_sessao` | Confere se a sessão está ativa (`rn_sessao_ativa`) e grava o último acesso no máximo a cada 5 minutos |
| `revogar_sessao` | Revoga uma sessão |
| `revogar_sessoes_dispositivo` | Revoga as sessões ativas de um dispositivo |
| `revogar_sessoes_usuario` | Revoga as sessões ativas de um usuário, exceto uma |
| `emitir_refresh_token` | Grava o hash do primeiro refresh token, com a validade da sessão |
| `renovar_refresh_token` | Troca o hash atual pelo novo, só se o atual ainda for o apresentado (`rn_refresh_token_renovado`), e guarda o antigo no histórico |
| `limpar_sessoes_expiradas` | Apaga sessões vencidas, junto com seus refresh tokens e histórico. **Ainda não é agendada.** |

---

## Regras de negócio

### Cadastro

- O cadastro cria, em uma única transação, a **pessoa física**, o **usuário** e a **credencial local**, registra o dispositivo e abre a primeira sessão.
- E-mail e CPF são **únicos** no sistema. Cada pessoa física possui no máximo um usuário.
- A resposta já contém os tokens de acesso: o usuário sai do cadastro autenticado. Ainda não há confirmação de e-mail.
- Somente pessoas físicas podem ser usuários. Empresas se relacionam com usuários por meio de vínculos.
- Cada usuário recebe um **UUID público**, gerado pelo banco, que é o identificador exposto pela API.

### Login, dispositivos e sessões

- O login exige e-mail, senha e os dados do dispositivo.
- E-mail inexistente e senha incorreta produzem **a mesma resposta** (`E-mail ou senha incorretos.`).
- Antes de conferir a senha, a API verifica o **limite de falhas da conta**. Cada senha errada desconta uma tentativa do e-mail, de qualquer IP; logins corretos não descontam.
- O dispositivo é identificado pelo par **(usuário, identificador UUID)**. Se já existir, seus dados (plataforma, fabricante, modelo, versão do sistema) são atualizados.
- Cada dispositivo tem **no máximo uma sessão ativa**: um novo login revoga as sessões anteriores daquele dispositivo.
- Uma sessão dura **30 dias** a partir do login (`security.session.ttl`), sem renovação, e registra o IP de origem.
- Toda requisição autenticada confirma que a sessão **não foi revogada nem expirou**. Uma sessão revogada invalida imediatamente o access token correspondente. O último acesso é gravado no máximo a cada 5 minutos.

```mermaid
sequenceDiagram
    participant App
    participant API
    participant Banco

    App->>API: POST /auth/login (e-mail, senha, dispositivo)
    API->>API: confere o limite de falhas da conta
    API->>Banco: valida credencial (Argon2id)
    API->>Banco: registrar_dispositivo
    API->>Banco: revogar_sessoes_dispositivo
    API->>Banco: cria sessão (30 dias)
    API->>Banco: emitir_refresh_token
    API-->>App: accessToken + refreshToken

    App->>API: GET /users/me (Bearer accessToken)
    API->>Banco: registrar_acesso_sessao
    API-->>App: dados do usuário

    App->>API: POST /auth/refresh (refreshToken)
    API->>Banco: renovar_refresh_token
    API-->>App: novo accessToken + novo refreshToken
```

### Tokens

| | Access token | Refresh token |
|---|---|---|
| Formato | JWT HS256 | 32 bytes aleatórios em Base64 URL |
| Conteúdo | `sub` (e-mail), `sid` (sessão), `iss`, `iat`, `exp` | Opaco |
| Validade | `JWT_EXPIRATION_TIME` | A mesma da sessão |
| Armazenamento | Não é armazenado | Somente o HMAC-SHA-256 com `REFRESH_TOKEN_SECRET_KEY` |
| Uso | Cabeçalho `Authorization: Bearer` | Corpo de `POST /auth/refresh` |

Regras do refresh token:

- **Rotação obrigatória**: cada renovação invalida o token usado e emite outro. O hash do token substituído vai para `usuarios.refresh_token_usado`.
- **Detecção de reuso**: apresentar **qualquer** token já substituído, de qualquer geração e mesmo vencido, indica vazamento. A sessão inteira é revogada.
- **Concorrência**: se duas renovações com o mesmo token chegarem ao mesmo tempo, apenas uma vence. A outra recebe `409` (`rn_refresh_token_renovado`).
- A renovação exige que a sessão continue ativa e que o token não tenha vencido.
- Recusas de sessão e de refresh token respondem o mesmo `401` genérico (`SecurityProblem.INVALID_SESSION`). O motivo real vai apenas para o log.

### Logout e troca de senha

- O **logout** revoga a sessão do token apresentado.
- A **troca de senha** exige a senha atual e recusa uma nova senha igual à atual. Em caso de sucesso, grava o novo hash, registra a data da alteração e **revoga todas as outras sessões** do usuário, mantendo a sessão atual.

### Validação de dados

As regras abaixo são aplicadas pelos value objects e DTOs na entrada da API e, quando aplicável, repetidas por constraints no banco.

| Campo | Regras |
|---|---|
| Senha | De 12 a 128 caracteres (acentos e emojis contam como um); qualquer combinação de caracteres, inclusive espaços; nunca é aparada |
| E-mail | Espaços nas pontas removidos e convertido para minúsculas; até 254 caracteres; parte local até 64 caracteres em `[a-z0-9._%+-]`; domínio em `[a-z0-9.-]` com extensão de 2 ou mais letras |
| CPF | Máscara (`.`, `-`, `/`, espaços) removida; 11 dígitos; dígitos não podem ser todos iguais; dígitos verificadores pelo módulo 11 |
| CNPJ | Formato alfanumérico: 12 caracteres `[0-9A-Z]` seguidos de 2 dígitos verificadores pelo módulo 11 |
| Nome | Espaços nas pontas removidos; obrigatório; até 200 caracteres |
| Dispositivo | Identificador UUID; plataforma `android` ou `ios`; fabricante, modelo e versão do sistema com espaços nas pontas removidos; fabricante e modelo até 64 caracteres; versão do sistema até 16 caracteres em `[0-9A-Za-z._- ]` |

No banco, textos livres não podem ter espaços nas extremidades e respeitam limites de tamanho por coluna.

### Limite de requisições

O limite usa o algoritmo **token bucket** e tem duas camadas.

**Por rede e rota** (`RateLimitFilter`). A chave é o IP do cliente; endereços IPv6 são agrupados pelo bloco `/64`.

| Rota | Variáveis |
|---|---|
| `POST /auth/login` | `RATE_LIMIT_LOGIN_*` |
| `POST /auth/register` | `RATE_LIMIT_REGISTER_*` |
| `POST /auth/refresh` | `RATE_LIMIT_REFRESH_*` |
| `PATCH /users/me/password` | `RATE_LIMIT_PASSWORD_RESET_*` |
| `POST /evidencias` | `RATE_LIMIT_UPLOAD_*` (rota reservada) |
| Demais rotas | `RATE_LIMIT_API_*` |

Toda resposta traz `X-RateLimit-Limit`, `X-RateLimit-Remaining` e `X-RateLimit-Reset`. Ao exceder o limite, a resposta é `429` com o cabeçalho `Retry-After`.

**Por conta** (`LoginAttemptService`). Conta apenas **senhas erradas** por e-mail, de qualquer IP (`RATE_LIMIT_ACCOUNT_*`). Ao exceder, o login responde `429` sem conferir a senha. Esse limite não envia os cabeçalhos `X-RateLimit-*`.

Os contadores ficam em memória: valem para uma única instância e são zerados quando a aplicação reinicia.

### Regras do domínio de vistorias

Regras já garantidas pelo schema, que valem para as próximas funcionalidades da API:

**Pessoas e empresas**

- Uma pessoa é física (`pf`) ou jurídica (`pj`), e o subtipo precisa corresponder ao tipo.
- O vínculo entre pessoa física e empresa pode ser `socio`, `representante`, `mei` ou `funcionario`, e é único por par.
- A titularidade de uma pessoa pertence a exatamente um titular: um usuário ou uma empresa.

**Endereços**

- Hierarquia: estado, cidade, bairro, logradouro (com CEP de 8 dígitos) e endereço (número e complemento).
- Nomes são únicos dentro do nível superior, sem diferenciar maiúsculas de minúsculas.
- Os 27 estados brasileiros são carregados pela migration.

**Imóveis**

- Tipo: `apartamento`, `casa`, `sala_comercial`, `galpao` ou `outro`. Categoria: `residencial`, `comercial` ou `alto_padrao`.
- Cada imóvel tem exatamente um titular (usuário ou empresa), e um titular não pode cadastrar dois imóveis no mesmo endereço.
- Um imóvel pode ter vários proprietários. Uma pessoa que é proprietária não pode ser excluída.

**Catálogo**

- Tipos de ambiente e de item podem ser padrões do sistema (`padrao`) ou personalizados por um usuário ou uma empresa.
- A migration carrega os ambientes Sala, Cozinha, Quarto, Banheiro e Área de serviço, cada um com seus itens típicos (piso, paredes, portas, tomadas, pia, chuveiro, entre outros).

**Vistorias**

- Tipo: `entrada` ou `saida`. Status: `em_andamento` ou `finalizada`.
- Uma vistoria de saída pode referenciar uma vistoria **do mesmo imóvel**. Vistorias de entrada não referenciam outras vistorias. O banco ainda não garante que a vistoria referenciada seja de entrada.
- Uma vistoria está finalizada se, e somente se, possui data de finalização, que não pode ser anterior ao início.
- Pessoas vinculadas à vistoria participam como `proprietario` ou `inquilino`.
- Ambientes e itens têm nomes únicos dentro da vistoria e do ambiente, respectivamente. Podem apontar para o ambiente ou item de origem (por exemplo, o correspondente da vistoria de entrada), mas nunca para si mesmos.
- Estado do item: `novo`, `excelente`, `bom`, `regular`, `ruim` ou `danificado`.
- Evidências são imagens **JPEG ou PNG de até 50 MB**, ligadas a um ambiente e, opcionalmente, a um item **desse mesmo ambiente**. O caminho do arquivo é único.

---

## Segurança

| Ameaça | Proteção |
|---|---|
| Força bruta e credential stuffing | Limite por rede e rota, com IPv6 agrupado por `/64`, e limite de falhas por conta |
| IP do cliente falsificado | `server.forward-headers-strategy: none`: o `X-Forwarded-For` é ignorado e o IP é sempre o da conexão |
| Endereço remoto que não é IP | Recusado com `400`, sem consulta DNS |
| Vazamento do banco | Senhas em Argon2id (19 MiB, 2 iterações); refresh tokens em HMAC-SHA-256 com chave fora do banco |
| Roubo de refresh token | Rotação a cada uso, detecção de reuso de qualquer geração e revogação da sessão |
| Renovação simultânea do mesmo token | Bloqueada no banco (`rn_refresh_token_renovado`) |
| Enumeração de contas no login | Mesma resposta para e-mail inexistente e senha errada |
| Exposição de estado de sessão | `401` genérico para sessão e refresh token recusados |
| Exposição de ids internos | API expõe o UUID público do usuário, não o id sequencial |
| Vazamento de detalhes internos | Erros `500` com mensagem genérica; detalhe só no log |
| Anexos de e-mail | Restritos ao diretório `MAIL_ATTACHMENTS_DIR`; assuntos com quebra de linha são recusados |

**Pendências conhecidas**

- Não há confirmação de e-mail no cadastro, e o cadastro revela se o e-mail ou o CPF já existe.
- Não há bloqueio persistente de conta; o limite de falhas fica em memória.
- A aplicação deve ser servida diretamente com **HTTPS** (não há proxy à frente).
- O agendamento de `limpar_sessoes_expiradas` e o limite de tamanho do corpo das requisições ainda não existem.

---

## API

### Versionamento

Todas as rotas ficam sob o prefixo `/api/{versao}`. A versão é negociada pelo cabeçalho definido em `API_VERSION_HEADER`; sem o cabeçalho, vale `API_VERSION_DEFAULT`. A versão atual é `1`.

### Endpoints

| Método | Rota | Autenticação | Descrição | Sucesso |
|---|---|---|---|---|
| `POST` | `/api/1/auth/register` | Pública | Cadastra o usuário e abre a sessão | `201` |
| `POST` | `/api/1/auth/login` | Pública | Autentica e abre a sessão no dispositivo | `200` |
| `POST` | `/api/1/auth/refresh` | Pública | Rotaciona o refresh token e emite um novo access token | `200` |
| `POST` | `/api/1/auth/logout` | Bearer | Revoga a sessão atual | `204` |
| `GET` | `/api/1/users/me` | Bearer | Retorna o usuário autenticado | `200` |
| `PATCH` | `/api/1/users/me/password` | Bearer | Troca a senha e revoga as outras sessões | `204` |

### Exemplos

**Cadastro**

```http
POST /api/1/auth/register
Content-Type: application/json

{
  "name": "Ana Souza",
  "cpf": "529.982.247-25",
  "email": "ana@exemplo.com",
  "password": "minha casa fica perto do rio",
  "device": {
    "identifier": "7f1c2a8e-3b4d-4c5e-8f90-112233445566",
    "platform": "android",
    "manufacturer": "Samsung",
    "model": "S23",
    "osVersion": "14"
  }
}
```

```json
{
  "user": {
    "id": "3f2b1c9e-8a47-4d1e-9b6a-5c0d2e7f8a91",
    "name": "Ana Souza",
    "email": "ana@exemplo.com"
  },
  "token": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "q3V0n6sB1o9...",
    "expiresIn": 900
  }
}
```

O login usa o mesmo formato, sem `name` e `cpf`, e devolve a mesma resposta.

**Renovação de token**

```http
POST /api/1/auth/refresh
Content-Type: application/json

{ "refreshToken": "q3V0n6sB1o9..." }
```

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "Zt8wLm2xP0c...",
  "expiresIn": 900
}
```

**Troca de senha**

```http
PATCH /api/1/users/me/password
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{ "currentPassword": "minha casa fica perto do rio", "newPassword": "outra frase bem longa 2026" }
```

### Formato de erro

Todos os erros seguem a RFC 9457 (`application/problem+json`), com o campo adicional `code` no formato `<Catálogo>.<Problema>`. Erros de validação de campos trazem a lista `errors`.

```json
{
  "type": "about:blank",
  "title": "Unauthorized",
  "status": 401,
  "detail": "A sessão é inválida. Faça login novamente.",
  "code": "SecurityProblem.INVALID_SESSION"
}
```

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Os dados informados são inválidos.",
  "code": "ValidationProblem.INVALID_DATA",
  "errors": [
    { "field": "device.model", "message": "O modelo é obrigatório." }
  ]
}
```

| Status | Situação |
|---|---|
| `400` | Corpo inválido, dado reprovado por um value object ou endereço remoto que não é IP |
| `401` | Credenciais inválidas, token ausente, inválido ou expirado, sessão ou refresh token recusados |
| `403` | Acesso negado |
| `404` | Recurso inexistente |
| `409` | Violação de unicidade ou renovação concorrente de refresh token |
| `422` | Violação de check ou de chave estrangeira no banco, senha atual incorreta ou nova senha igual à atual |
| `429` | Limite de requisições ou de falhas de login da conta excedido |
| `500` | Erro inesperado, com mensagem genérica |

---

## Configuração

A aplicação é configurada **por variáveis de ambiente**, referenciadas pelos arquivos em `src/main/resources`. As variáveis sem valor padrão são obrigatórias.

**Aplicação e API**

| Variável | Descrição | Exemplo |
|---|---|---|
| `APP_NAME` | Nome da aplicação | `vsr` |
| `API_VERSION_HEADER` | Cabeçalho de versão da API | `X-API-Version` |
| `API_VERSION_SUPPORTED` | Versões aceitas | `1` |
| `API_VERSION_DEFAULT` | Versão usada sem o cabeçalho | `1` |

**Banco de dados**

| Variável | Descrição | Exemplo |
|---|---|---|
| `DB_URL` | URL JDBC | `jdbc:postgresql://localhost:5432/vsr` |
| `DB_USERNAME` / `DB_PASSWORD` | Credenciais | |
| `DB_DRIVER` | Driver JDBC | `org.postgresql.Driver` |
| `DB_POOL_NAME` | Nome do pool Hikari | `vsr-pool` |
| `DB_POOL_MAX_SIZE` / `DB_POOL_MIN_IDLE` | Tamanho do pool | `10` / `2` |
| `DB_POOL_CONNECTION_TIMEOUT` / `DB_POOL_IDLE_TIMEOUT` / `DB_POOL_MAX_LIFETIME` | Tempos em milissegundos | `30000` / `600000` / `1800000` |
| `JPA_DIALECT` | Dialeto do Hibernate | `org.hibernate.dialect.PostgreSQLDialect` |
| `JPA_DDL_AUTO` | Estratégia de DDL; o schema é do Flyway | `validate` |
| `JPA_BATCH_SIZE` | Tamanho de lote JDBC | `50` |
| `JPA_SHOW_SQL` | Imprime o SQL no log (opcional, padrão `false`) | `false` |

**Segurança**

| Variável | Descrição | Exemplo |
|---|---|---|
| `JWT_SECRET_KEY` | Chave HMAC do JWT em Base64, com no mínimo 32 bytes | `openssl rand -base64 32` |
| `JWT_ISSUER` | Emissor dos tokens | `vsr` |
| `JWT_EXPIRATION_TIME` | Validade do access token | `15m` |
| `REFRESH_TOKEN_SECRET_KEY` | Chave HMAC dos refresh tokens em Base64, com no mínimo 32 bytes e diferente da chave do JWT. Trocá-la invalida todos os refresh tokens | `openssl rand -base64 32` |

**E-mail**

| Variável | Descrição |
|---|---|
| `MAIL_HOST` / `MAIL_PORT` | Servidor SMTP (STARTTLS obrigatório) |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Credenciais SMTP |
| `MAIL_FROM` | Remetente padrão |
| `MAIL_ATTACHMENTS_DIR` | Único diretório de onde anexos podem ser enviados (opcional, padrão `attachments`) |

**Limite de requisições**

Para cada grupo `API`, `LOGIN`, `REGISTER`, `REFRESH`, `ACCOUNT`, `PASSWORD_RESET` e `UPLOAD`, defina:

| Variável | Descrição | Exemplo |
|---|---|---|
| `RATE_LIMIT_<GRUPO>_CAPACITY` | Requisições permitidas em rajada (no grupo `ACCOUNT`, falhas de login) | `5` |
| `RATE_LIMIT_<GRUPO>_REFILL_RATE` | Fichas repostas a cada intervalo | `5` |
| `RATE_LIMIT_<GRUPO>_REFILL_INTERVAL` | Intervalo de reposição | `1m` |

---

## Execução

**Pré-requisitos:** JDK 17 e PostgreSQL 13 ou superior (o schema usa `gen_random_uuid()` nativo).

```bash
# compilar
./mvnw clean package -DskipTests

# executar com as variáveis de ambiente definidas
./mvnw spring-boot:run

# ou executar o artefato
java -jar target/vsr-0.0.1-SNAPSHOT.jar
```

Na primeira execução, o Flyway aplica as migrations `V1` a `V6` de `src/main/resources/db/migration`. Depois que um ambiente executou as migrations, os arquivos existentes não podem mais ser alterados; mudanças novas entram como `V7__...`.

A aplicação não confia em cabeçalhos de proxy (`X-Forwarded-*`). Se for colocada atrás de um proxy reverso, ajuste `server.forward-headers-strategy` e `server.tomcat.remoteip.internal-proxies` em `web.yaml`.

---

## Logs

Os logs vão para a saída padrão do processo, no formato padrão do Spring Boot. Não há gravação em arquivo.

| Origem | Nível | Evento |
|---|---|---|
| `UnexpectedExceptionHandler` | `ERROR` | Erro inesperado, com stack trace |
| `SecurityExceptionHandler` | `WARN` | Falha de login, com IP |
| `SessionExceptionHandler` | `WARN` | Sessão recusada, com motivo e IP |
| `RefreshTokenExceptionHandler` | `WARN` | Refresh token recusado, inclusive reuso, com motivo e IP |
| `RateLimitExceptionHandler` | `WARN` | Limite excedido, com rota e IP |

---

## Testes

```bash
./mvnw test
```

Os testes ficam em `src/test/java`, espelhando os pacotes de produção, e não precisam de banco de dados.

| Recurso | Função |
|---|---|
| `@WebSecurityTest` | Sobe a cadeia de segurança real (configuração, filtros, JWT, rate limit) com MockMvc e serviços mockados |
| `@Controle` | Marca testes que confirmam uma proteção (`-Dgroups=controle`) |
| `@Brecha` | Marca testes que reproduzem uma falha conhecida e ainda aberta (`-Dgroups=brecha`); quando a falha for corrigida, o teste passa a falhar e deve virar `@Controle` |
| `Users`, `Tokens`, `Requests`, `Ips` | Fixtures de usuário, tokens válidos e inválidos, requisições prontas e IPs exclusivos por requisição |
