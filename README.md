# vsr

API REST para gestão de **vistorias de imóveis**: cadastro de pessoas e empresas, imóveis e seus proprietários, e o registro de vistorias de entrada e saída, organizadas em ambientes, itens e evidências fotográficas.

O módulo de **identidade e acesso** (cadastro, login, sessões por dispositivo, tokens e troca de senha) está implementado na API. O restante do domínio já está modelado no banco de dados (migrations `V1` a `V7`) e será exposto pela API nas próximas etapas.

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
| Persistência | PostgreSQL 17 com pg_cron, Hibernate 7, Flyway |
| Autenticação | JWT HS256 (`com.auth0:java-jwt`) e refresh token opaco com hash HMAC-SHA-256 |
| Senhas | Argon2id (Spring Security + BouncyCastle) |
| Mapeamento | MapStruct 1.6 e Lombok |
| Testes | JUnit, Mockito, MockMvc, Spring Security Test |
| Build | Maven (wrapper incluso) |

---

## Arquitetura

O projeto é organizado **por camada**, nunca por funcionalidade. Cada pacote contém uma única natureza de classe. As exceções são componentes de infraestrutura autocontidos: `exception/lib` e `ratelimit`, que reúne tudo do limite de requisições (algoritmo, configuração, rotas e resultado).

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
├── exception
│   ├── custom        Exceções de domínio
│   ├── enums         Catálogo de problemas (*Problem) e de constraints do banco
│   ├── handler       Um @RestControllerAdvice por exceção
│   └── lib           Infraestrutura de ProblemDetail e resolução de constraints
├── filter            Filtros servlet (exceção, rate limit, tamanho do corpo, autenticação)
├── lib               Algoritmos puros (Modulo11; ClientNetwork: IP de origem e rede para o rate limit)
├── mapper            Mappers MapStruct
├── principal         Identidade autenticada (Principal)
├── properties        @ConfigurationProperties
├── ratelimit         Componente autocontido de rate limit (RateLimit, RateLimitPolicy, Rules, Route, Limit, TokenBucket, Bucket, Quota)
├── service           Serviços de entidade, de caso de uso e de infraestrutura
├── token             JWT, token opaco (HMAC) e Claims
└── vo                Value objects (Email, Password, PasswordHash, Cpf, Cnpj, Name, Mail)
```

### Serviços

| Tipo | Serviços | Responsabilidade |
|---|---|---|
| Entidade | `IndividualService`, `UserService`, `LocalCredentialService`, `DeviceService`, `SessionService`, `RefreshTokenService` | Mexem apenas no próprio repositório e guardam as regras da própria entidade. Recebem entidades e value objects, não DTOs (exceção: `DeviceRequest`) |
| Caso de uso | `RegisterService`, `LoginService`, `RefreshService`, `LogoutService`, `ChangePasswordService`, `ProfileService` | Ponto de entrada de cada fluxo; apenas orquestram outros serviços |
| Composição | `AccessService`, `AccountService` | Etapas compartilhadas entre casos de uso. O `AccessService` concede o acesso (dispositivo, sessão, tokens) e é o único que monta a resposta de autenticação. O `AccountService` concentra as gravações da conta que precisam ser atômicas: abrir a conta e substituir a senha |
| Infraestrutura | `TokenService`, `LoginAttemptService`, `EmailService` | Isolam JWT, limite de falhas por conta e envio de e-mail. O `TokenService` é o único que emite e verifica o access token e o único que monta o par de tokens (`TokenResponse`) |

Cada regra tem um único dono: a validade e o dono da sessão são decididos só pelo `SessionService`; a contagem de falhas de login, só pelo `LoginAttemptService`; a detecção de reuso de refresh token, só pelo `RefreshTokenService`.

### Convenções

- O método público de um serviço lista os passos do caso de uso; cada passo é um método privado com uma única intenção, na ordem em que é chamado. Métodos públicos vêm primeiro; auxiliares `static` ficam no fim.
- Dependências externas (repositório, mapper, JWT, envio de e-mail) são chamadas em métodos privados. **Exceção:** um método público de um único passo chama a dependência direto, sem um privado só de repasse.
- Nomes: `create` monta sem persistir, `persist` grava, `find` busca e lança exceção quando não encontra, `is…`/`has…` retorna booleano. Uma classe nunca tem dois métodos com o mesmo nome. Fábricas: `of` cria a partir das partes e `from` converte de outro tipo.
- Nomes de variáveis e parâmetros: o UUID do usuário é `user`, o id da sessão é `session`, a entidade `RefreshToken` é `stored`, o refresh token em texto é `refreshToken` e o HMAC dele é `digest`. Quando aparecem juntos, `(user, session)` vêm primeiro.
- **Fail fast**: todo `if` fica no topo da função e o corpo só lança ou retorna. Quando a checagem depende de um valor buscado, o valor passa por uma função que começa pelos `if` e o devolve: `active(findOwned(user, session))`, `matched(find(user), password)`, `unmatched(credential, password)`, `present(mail)`. Uma recusa ou um efeito colateral condicional vira um passo próprio, nomeado `…If…`: `terminateIfReused`, `rejectIfExceeded`.
- **Transações** só envolvem gravações e leituras que precisam ficar juntas, e nunca o cálculo do Argon2 (que leva dezenas de milissegundos). Cadastro e troca de senha calculam o hash fora de transação e só então chamam o `AccountService`, que é `@Transactional`. Um teste de guarda falha se a anotação aparecer onde há Argon2 ou sumir onde há gravação.
- **Value objects** concentram a validação. Uma senha nunca trafega como `String`: DTOs e serviços recebem `Password`, e o valor só é extraído na borda com APIs externas. Nomes de pessoa usam `Name`.
- Escritas que vão além do CRUD são **procedures no banco**, chamadas com `@Procedure`.
- Violações de regra no banco usam o padrão `rn_<nome>` e são traduzidas pelo `ConstraintExceptionHandler`, pelo enum `RuleConstraint`, para o status HTTP de cada regra.

### Cadeia de filtros

Toda requisição passa, nesta ordem, por:

1. `ExceptionFilter`: converte exceções lançadas nos filtros em `ProblemDetail`.
2. `RateLimitFilter`: valida o IP de origem (`ClientNetwork`), aplica o limite da rota por rede do cliente e devolve os cabeçalhos `X-RateLimit-*`.
3. `RequestBodySizeLimitFilter`: recusa pelo `Content-Length` corpos acima de 16 KB (`413`) e corpos sem `Content-Length` (`411`), antes de qualquer leitura.
4. `AuthenticationFilter`: valida o JWT e confirma, numa única consulta, que a sessão do token está ativa e pertence ao usuário do token. O `Principal` guarda só o UUID do usuário e o id da sessão. É ignorado nas rotas públicas.

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
| `rotinas` | Procedures de manutenção executadas por jobs agendados no `pg_cron` |

As migrations ficam em `src/main/resources/db/migration`:

| Migration | Conteúdo |
|---|---|
| `V1__schema.sql` | Extensão `pg_cron` e schemas |
| `V2__functions.sql` | Funções de validação e de `updated_at` |
| `V3__ddl.sql` | Tipos, tabelas, constraints e índices |
| `V4__procedures.sql` | Procedures de dispositivo, sessão, refresh token, troca de senha e limpeza |
| `V5__triggers.sql` | Triggers de `updated_at` |
| `V6__initial_data.sql` | Estados e catálogo inicial de ambientes e itens |
| `V7__jobs.sql` | Agendamento dos jobs no `pg_cron` |

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
| `trocar_senha` | Grava o novo hash da senha só se o hash atual ainda for o lido pela aplicação (`rn_senha_alterada`), evitando que uma troca concorrente sobrescreva a outra |
| `rotinas.limpar_sessoes_expiradas` | Apaga sessões vencidas, junto com seus refresh tokens e histórico. Roda todo dia às 03:00 (horário do `pg_cron`, UTC por padrão) pelo job `limpar-sessoes-expiradas` |

---

## Regras de negócio

### Cadastro

- O cadastro calcula o hash da senha **antes** de abrir a transação. Depois, em uma única transação (`AccountService.open`), cria a **pessoa física**, o **usuário** e a **credencial local**, registra o dispositivo, abre a primeira sessão e emite os tokens. Se qualquer etapa falhar, nada fica gravado.
- E-mail e CPF são **únicos** no sistema. Cada pessoa física possui no máximo um usuário.
- A resposta já contém os tokens de acesso: o usuário sai do cadastro autenticado. Ainda não há confirmação de e-mail.
- Somente pessoas físicas podem ser usuários. Empresas se relacionam com usuários por meio de vínculos.
- Cada usuário recebe um **UUID público**, gerado pelo banco, que é o identificador exposto pela API.

### Login, dispositivos e sessões

- O login exige e-mail, senha e os dados do dispositivo.
- E-mail inexistente e senha incorreta produzem **a mesma resposta** (`E-mail ou senha incorretos.`).
- Antes de conferir a senha, a API verifica o **limite de falhas da conta**. Cada senha errada desconta uma tentativa do e-mail, de qualquer IP; logins corretos não descontam.
- A conferência da senha (Argon2id) roda **fora de transação**. A credencial é buscada numa consulta curta, e a conexão é devolvida antes do hash.
- O dispositivo é identificado pelo par **(usuário, identificador UUID)**. Se já existir, seus dados (plataforma, fabricante, modelo, versão do sistema) são atualizados.
- Cada dispositivo tem **no máximo uma sessão ativa**: um novo login revoga as sessões anteriores daquele dispositivo.
- Uma sessão dura **30 dias** a partir do login (`security.session.ttl`), sem renovação, e registra o IP de origem.
- Toda requisição autenticada confirma que a sessão **não foi revogada nem expirou** e que **pertence ao usuário do token**. Uma sessão revogada invalida imediatamente o access token correspondente. O último acesso é gravado no máximo a cada 5 minutos.

```mermaid
sequenceDiagram
    participant App
    participant API
    participant Banco

    App->>API: POST /auth/login (e-mail, senha, dispositivo)
    API->>API: confere o limite de falhas da conta
    API->>Banco: busca a credencial
    API->>API: confere a senha (Argon2id, sem conexão presa)
    API->>Banco: busca o usuário com a pessoa
    Note over API,Banco: transação
    API->>Banco: registrar_dispositivo
    API->>Banco: revogar_sessoes_dispositivo + cria sessão (30 dias)
    API->>Banco: emitir_refresh_token
    API-->>App: usuário + accessToken + refreshToken

    App->>API: GET /users/me (Bearer accessToken)
    API->>Banco: busca a sessão do token com o dono + registrar_acesso_sessao
    API-->>App: dados do usuário

    App->>API: POST /auth/refresh (refreshToken)
    API->>Banco: busca pelo HMAC (atual ou já usado)
    API->>Banco: retoma a sessão (ativa?) + registrar_acesso_sessao
    API->>Banco: renovar_refresh_token
    API-->>App: novo accessToken + novo refreshToken
```

### Tokens

| | Access token | Refresh token |
|---|---|---|
| Formato | JWT HS256 | 32 bytes aleatórios em Base64 URL |
| Conteúdo | `sub` (UUID público do usuário), `sid` (sessão), `iss`, `iat`, `exp` | Opaco |
| Validade | `JWT_EXPIRATION_TIME` | A mesma da sessão |
| Armazenamento | Não é armazenado | Somente o HMAC-SHA-256 com `REFRESH_TOKEN_SECRET_KEY` |
| Uso | Cabeçalho `Authorization: Bearer` | Corpo de `POST /auth/refresh` |

Regras do refresh token:

- **Rotação obrigatória**: cada renovação invalida o token usado e emite outro. O hash do token substituído vai para `usuarios.refresh_token_usado`.
- O HMAC do token apresentado é calculado **uma vez** e reaproveitado na busca e na detecção de reuso.
- **Detecção de reuso**: apresentar **qualquer** token já substituído, de qualquer geração e mesmo vencido, indica vazamento. A sessão inteira é revogada direto pelo id, sem nova consulta.
- **Concorrência**: se duas renovações com o mesmo token chegarem ao mesmo tempo, apenas uma vence. A outra recebe `409` (`rn_refresh_token_renovado`).
- A renovação exige que a **sessão** continue ativa. A validade é decidida só pela sessão: o refresh token recebe a mesma data de expiração da sessão na emissão.
- Recusas de sessão e de refresh token respondem o mesmo `401` genérico (`SecurityProblem.INVALID_SESSION`). O motivo real vai apenas para o log.

### Logout e troca de senha

- O **logout** revoga a sessão do token apresentado.
- A **troca de senha** exige a senha atual e recusa uma nova senha igual à atual. As conferências e o novo hash (três operações Argon2) são calculados **fora de transação**. Depois, numa transação curta (`AccountService.replace`), a procedure `trocar_senha` grava o novo hash e as **outras sessões do usuário são revogadas**, mantendo a sessão atual. Se outra troca de senha da mesma conta tiver acontecido no meio, a requisição recebe `409` (`rn_senha_alterada`) e nada é alterado.

### Validação de dados

As regras abaixo são aplicadas pelos value objects e DTOs na entrada da API e, quando aplicável, repetidas por constraints no banco.

| Campo | Regras |
|---|---|
| Senha | De 12 a 128 caracteres (acentos e emojis contam como um); qualquer combinação de caracteres, inclusive espaços; nunca é aparada |
| E-mail | Espaços nas pontas removidos e convertido para minúsculas; até 254 caracteres; parte local até 64 caracteres em `[a-z0-9._%+-]`; domínio em `[a-z0-9.-]` com extensão de 2 ou mais letras |
| CPF | Máscara (`.`, `-`, `/`, espaços) removida; 11 dígitos; dígitos não podem ser todos iguais; dígitos verificadores pelo módulo 11 |
| CNPJ | Formato alfanumérico: 12 caracteres `[0-9A-Z]` seguidos de 2 dígitos verificadores pelo módulo 11 |
| Nome | VO `Name`: espaços nas pontas removidos; obrigatório; até 200 caracteres (acentos contam como um) |
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
| `PATCH /users/me/password` | `RATE_LIMIT_PASSWORD_RESET_*` (limita a **troca** de senha; o nome é histórico, não há reset) |
| `POST /evidencias` | `RATE_LIMIT_UPLOAD_*` (rota reservada) |
| Demais rotas | `RATE_LIMIT_API_*` |

Toda resposta traz `X-RateLimit-Limit`, `X-RateLimit-Remaining` e `X-RateLimit-Reset`. Ao exceder o limite, a resposta é `429` com o cabeçalho `Retry-After`.

**Por conta** (`LoginAttemptService`). Conta apenas **senhas erradas** por e-mail, de qualquer IP (`RATE_LIMIT_ACCOUNT_*`). Ao exceder, o login responde `429` sem conferir a senha. Esse limite não envia os cabeçalhos `X-RateLimit-*`.

Os contadores ficam em memória: valem para uma única instância e são zerados quando a aplicação reinicia.

As rotas com limite próprio são declaradas num só lugar (`ratelimit.Rules`). Cada limite (`ratelimit.Limit`) se valida na subida: capacidade, taxa e intervalo precisam ser maiores que zero, senão a aplicação não inicia (`LimitProblem`).

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
| Enumeração de CPF e e-mail no cadastro | Mesma recusa genérica para os dois (`RegisterProblem.UNAVAILABLE`, 409) |
| Corpo de requisição gigante | Recusado pelo `Content-Length` antes de ser lido: acima de 16 KB (`vsr.request.max-body-size`) responde `413`; sem `Content-Length` (chunked) responde `411` |
| Dados pessoais no access token | O `sub` do JWT é o UUID público, não o e-mail |
| Token apontando para a sessão de outro usuário | A sessão é buscada pelo id **e** pelo UUID do token; se não pertencer ao usuário, a resposta é o `401` genérico |
| Troca de senha simultânea | Bloqueada no banco (`rn_senha_alterada`): só grava se o hash ainda for o lido |
| Conexões do banco esgotadas por hash de senha | O Argon2 roda fora de transação e o `open-in-view` está desligado, então nenhum hash prende conexão do pool |

**Pendências conhecidas**

- Não há confirmação de e-mail no cadastro, e o `409` ainda revela que o CPF ou o e-mail já existe (sem dizer qual).
- O limite de falhas por conta fica em memória.
- A aplicação deve ser servida diretamente com **HTTPS** (não há proxy à frente).

**Riscos aceitos**

| Risco | Consequência | Por que foi aceito |
|---|---|---|
| Não há bloqueio de conta | Uma conta comprometida ou abusiva só pode ser contida revogando as sessões; o dono da senha consegue entrar de novo | Revogar sessões e trocar a senha cobre os casos atuais |
| Não há modelo de autorização | `getAuthorities()` é vazio; cada endpoint futuro precisa filtrar pelo dono vindo do `Principal`, nunca por um id recebido na requisição, e expor só UUID | Os endpoints de imóvel e vistoria ainda não existem |
| Resposta perdida na renovação | Se a resposta do `/auth/refresh` se perde na rede e o app reenvia o token antigo, isso conta como reuso e a sessão é revogada; o usuário precisa fazer login de novo | Preferido a abrir uma janela de tolerância para o token anterior |

---

## API

### Versionamento

Todas as rotas ficam sob o prefixo `/api/{versao}`. A versão é negociada pelo cabeçalho definido em `VERSION_HEADER`; sem o cabeçalho, vale `VERSION_DEFAULT`. A versão atual é `1`.

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
| `400` | Corpo inválido, dado reprovado por um value object ou endereço remoto que não é IP (`RequestProblem.INVALID_ADDRESS`) |
| `401` | Credenciais inválidas, token ausente, inválido ou expirado, sessão ou refresh token recusados |
| `403` | Acesso negado |
| `404` | Recurso inexistente |
| `409` | Cadastro com CPF ou e-mail já existente (`RegisterProblem.UNAVAILABLE`, a mesma resposta para os dois), outra violação de unicidade, renovação concorrente de refresh token ou troca de senha concorrente |
| `411` | Corpo enviado sem `Content-Length` |
| `413` | Corpo acima de 16 KB |
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
| `VERSION_HEADER` | Cabeçalho de versão da API | `X-API-Version` |
| `VERSION_SUPPORTED` | Versões aceitas | `1` |
| `VERSION_DEFAULT` | Versão usada sem o cabeçalho | `1` |

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
| `JPA_BATCH_SIZE` | Tamanho de lote JDBC | `50` |

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
| `MAIL_ATTACHMENTS_DIR` | Único diretório de onde anexos podem ser enviados |

**Limite de requisições**

Para cada grupo `API`, `LOGIN`, `REGISTER`, `REFRESH`, `ACCOUNT`, `PASSWORD_RESET` e `UPLOAD`, defina:

| Variável | Descrição | Exemplo |
|---|---|---|
| `RATE_LIMIT_<GRUPO>_CAPACITY` | Requisições permitidas em rajada (no grupo `ACCOUNT`, falhas de login) | `5` |
| `RATE_LIMIT_<GRUPO>_REFILL_RATE` | Fichas repostas a cada intervalo | `5` |
| `RATE_LIMIT_<GRUPO>_REFILL_INTERVAL` | Intervalo de reposição | `1m` |

Os três valores precisam ser maiores que zero, senão a aplicação não inicia.

**Valores fixos (não são variáveis de ambiente)**

| Propriedade | Arquivo | Valor | Motivo |
|---|---|---|---|
| `spring.jpa.hibernate.ddl-auto` | `database.yaml` | `validate` | O schema pertence ao Flyway |
| `spring.jpa.show-sql` | `database.yaml` | `false` | |
| `spring.jpa.open-in-view` | `database.yaml` | `false` | A conexão fica presa só durante as transações |
| `security.session.ttl` | `security.yaml` | `30d` | Validade da sessão e do refresh token |
| `vsr.request.max-body-size` | `web.yaml` | `16KB` | Limite do corpo das requisições |
| `server.forward-headers-strategy` | `web.yaml` | `none` | O IP do cliente é sempre o da conexão |

---

## Execução

**Pré-requisitos:** JDK 17 e PostgreSQL 17 com a extensão `pg_cron` disponível. O `pg_cron` exige, no `postgresql.conf`, `shared_preload_libraries = 'pg_cron'` e `cron.database_name` apontando para o banco da aplicação (reinicie o Postgres depois). O usuário do Flyway precisa de permissão para criar a extensão.

```bash
# compilar
./mvnw clean package -DskipTests

# executar com as variáveis de ambiente definidas
./mvnw spring-boot:run

# ou executar o artefato
java -jar target/vsr-0.0.1-SNAPSHOT.jar
```

Na primeira execução, o Flyway aplica as migrations `V1` a `V7` de `src/main/resources/db/migration`. Depois que um ambiente executou as migrations, os arquivos existentes não podem mais ser alterados; mudanças novas entram como `V8__...`.

O `spring.jpa.open-in-view` está desligado em `database.yaml`: a conexão do banco só fica presa durante as transações, e não durante toda a requisição. Por isso, nenhum dado carregado preguiçosamente pode ser lido fora da transação em que foi buscado; quando um fluxo precisa de uma associação depois, a consulta já a traz (`join fetch`, como em `findWithPerson`).

A aplicação não confia em cabeçalhos de proxy (`X-Forwarded-*`). Se for colocada atrás de um proxy reverso, ajuste `server.forward-headers-strategy` e `server.tomcat.remoteip.internal-proxies` em `web.yaml`. O `ClientNetwork` continua recusando, sem consulta DNS, qualquer endereço de origem que não seja um IP literal.

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

Os testes ficam em `src/test/java`, espelhando os pacotes de produção, e não precisam de banco de dados. São 218 testes, e todo serviço, value object e componente de rate limit tem o seu.

| Recurso | Função |
|---|---|
| `@WebSecurityTest` | Sobe a cadeia de segurança real (configuração, filtros, JWT, rate limit, limite de corpo) com MockMvc e serviços mockados |
| `@Controle` | Marca testes que confirmam uma proteção (`-Dgroups=controle`) |
| `@Brecha` | Marca testes que reproduzem uma falha conhecida e ainda aberta (`-Dgroups=brecha`); quando a falha for corrigida, o teste passa a falhar e deve virar `@Controle` |
| `Users`, `Tokens`, `Requests`, `Ips` | Fixtures de usuário, tokens válidos e inválidos, requisições prontas e IPs exclusivos por requisição |

Além do comportamento, os testes conferem:

- a **ordem** das etapas: o Argon2 acontece antes de qualquer transação ou gravação, e o dispositivo antes da sessão e dos tokens;
- **quantas consultas** cada fluxo faz: o refresh busca a sessão uma vez, e o reuso revoga sem consultar;
- **onde há transação**: um teste de guarda falha se `@Transactional` aparecer onde há Argon2 ou sumir onde há gravação.

**Limitação:** os repositórios são simulados. Procedures, `rn_*`, consultas com `join fetch`, transações do `AccountService` e o `open-in-view` desligado ainda não são verificados contra um PostgreSQL real. Um teste de integração com Testcontainers (PostgreSQL 17 com `pg_cron`) cobriria isso.
