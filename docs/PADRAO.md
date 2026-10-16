# Padrão de código

Regras que valem para todo o código de produção. As regras de forma dos serviços são garantidas pelo `ServicePatternTest`; o glossário é conferido na revisão.

## Princípios

1. **Um nível de indentação por método.**
2. **Um nível de abstração por método:** o público lista passos; cada passo é um método com uma intenção.
3. **Um verbo, um sentido:** o mesmo verbo nunca significa coisas diferentes no projeto (ver [Glossário](#glossário)).

## Métodos

- O público só chama passos. Todo desvio de fluxo (`if`, `try`, `throw`, `orElseThrow`, laço que valida) fica num auxiliar próprio.
- Cada passo fica no seu método, inclusive o que só repassa para outro serviço.
- **O privado que repassa para outro serviço usa o mesmo verbo do método chamado** (`issue` → `refreshTokenService.issue`). Se esse nome já existir na classe, ele recebe o substantivo do que devolve (`tokens` → `tokenService.issue`).
- O privado que chama repositório, mapper ou framework traduz o nome técnico para o domínio (`clear` → `sessionRepository.revokeDevice`).
- Guardas são `void`, têm o nome da condição exigida (`present`, `fresh`, `unexpired`) e o corpo do `if` só sai cedo (`throw` ou `return`).
- O `TokenBucket` é exceção declarada: segue o formato do commit b9f9420 (ver [Rate limit](#rate-limit)).

## Nomes

- **Uma palavra** por padrão. **Duas palavras** só quando nenhuma palavra sozinha diz a coisa sem ambiguidade. Vale também para campos de record, que viram métodos.
- Booleanos sem prefixo `is`/`has`/`can`, sempre na **forma positiva**. A negação fica no `if` (`if (!current(...))`), sem criar um booleano gêmeo negado (`wrong`, `unused`).
- Passo que devolve valor pode ter nome de substantivo (`person`, `user`, `token`); ação tem nome de verbo.
- Nunca dois métodos com o mesmo nome na mesma classe.

## Ordem no arquivo

Públicos → passos na ordem em que são chamados → auxiliares de desvio de fluxo → booleanos.

## Glossário

### Acesso e conta

| Verbo | Sentido | Onde |
|---|---|---|
| `register` | cadastrar um usuário | `RegisterService` |
| `open` | criar algo com ciclo de vida (conta, sessão) | `AccountService`, `SessionService` |
| `authenticate` | conferir e-mail e senha pelo Spring Security | `LoginAttemptService` |
| `verify` | conferir um segredo (senha, credenciais, JWT) e lançar se não bater | `LocalCredentialService`, `*AttemptService`, `TokenService` |
| `grant` | dar acesso a um usuário já conferido: dispositivo, sessão e tokens | `AccessService` |
| `enroll` | registrar ou atualizar o dispositivo do usuário | `DeviceService` |

### Senha

| Verbo | Sentido | Onde |
|---|---|---|
| `change` | caso de uso de troca de senha | `ChangePasswordService` |
| `commit` | efetivar, numa transação, uma troca já validada (revoga as sessões, grava, emite tokens) | `PasswordService` |
| `replace` | gravar a nova senha no lugar da atual | `LocalCredentialService` |
| `hash` | calcular o hash de um segredo | `LocalCredentialService`, `RefreshTokenService` |
| `rehash` | conferir que a nova senha é diferente da atual e calcular o hash dela | `LocalCredentialService` |
| `upgrade` | gravar o hash recalculado com os parâmetros atuais do Argon2, depois de um login correto | `LocalCredentialService`, `AuthenticationConfig` |
| `swap` | `replace` que desiste em silêncio se outra troca de senha gravou primeiro | `LocalCredentialService` |

### Tokens

| Verbo | Sentido | Onde |
|---|---|---|
| `issue` | gerar um token novo | `RefreshTokenService`, `TokenService`, `JwtToken` |
| `sign` | gerar o access token assinado (JWT) | `TokenService` |
| `renew` | gerar o próximo refresh token no lugar do atual | `RefreshTokenService`, `RefreshService` |
| `rotate` | trocar as credenciais em uso por novas | `AccessService`, `RefreshService`, `RefreshTokenService` |
| `reused` | o refresh token apresentado já foi trocado | `RefreshTokenService` |
| `revokeReused` | revogar a sessão quando um refresh token reutilizado aparece | `RefreshService` |
| `tokens` | o par access + refresh token (passo que devolve `TokenResponse`) | `AccessService` |

### Sessão

| Verbo | Sentido | Onde |
|---|---|---|
| `access` | registrar o uso de uma sessão do dono (valida e toca) | `SessionService` |
| `resume` | retomar a sessão do refresh token e devolver o dono | `SessionService` |
| `touch` | atualizar o último acesso | `SessionService` |
| `active` | exigir sessão não revogada, não expirada e não ociosa | `SessionPolicyService` |
| `revoke` | revogar uma sessão do dono (logout); o sufixo diz o alcance (`revokeAll`, `revokeReused`) | `SessionService` |
| `invalidate` | revogar uma sessão pelo id, sem conferir o dono | `SessionService`, `RefreshService` |
| `close` | revogar uma sessão já carregada | `SessionService` |
| `revokeAll` | conferir a sessão do dono e revogar todas as sessões dele | `SessionService`, `PasswordService` |
| `dismiss` | revogar todas as sessões do usuário | `SessionService` |
| `clear` | revogar as sessões de um dispositivo | `SessionService` |
| `cap` | manter o usuário dentro do máximo de sessões | `SessionService` |

### Rate limit

O algoritmo fica inteiro no `TokenBucket`, a mesma conta do script Lua (formato do commit b9f9420), com uma chave genérica e intocado; em volta dele ficam só o limite de chaves e a limpeza; com os records `Limit`, `Allowance` e `Bucket` dentro dele. Os records só carregam dados: a validação do `Limit` e o cálculo do `retry` ficam no `TokenBucket`. Este bloco segue esse formato em vez das regras de passos acima.

| Nome | Sentido | Onde |
|---|---|---|
| `allow` | recarregar e gastar uma ficha da chave dentro do `compute`; `allow(key, now)` recebe o instante (gancho de teste) | `TokenBucket` |
| `evict` | remover os saldos ociosos (`findIdleKeys` acha, `removeKeys` remove só se ainda estiver ocioso) | `TokenBucket` |
| `refillInterval` | o `period` do `Limit` em segundos, calculado uma vez | `TokenBucket` |
| `ttl` | tempo para encher do zero: `ceil(capacity / refillRate)` intervalos; depois dele o saldo é `idle` | `TokenBucket` |
| `lastRefill` | instante da última recarga | `TokenBucket.Bucket` |
| `reset` | segundos até a próxima recarga | `TokenBucket.Allowance` |
| `retry` | o `reset` arredondado para cima, com mínimo de 1 (`TokenBucket.retry(allowance)`) | `TokenBucket` |
| `validate` | exigir capacidade, taxa e período maiores que zero ao montar o `TokenBucket` | `TokenBucket` |
| `cleaner` / `schedule` | a thread de limpeza compartilhada por todos os `TokenBucket` / agendar o `evict` periódico | `TokenBucket` |
| `crowded` | o limite de chaves (200 mil) está cheio e a chave é nova; antes de recusar, chama o `evict` no máximo uma vez por segundo (`due`) | `TokenBucket` |
| `capacity` / `rate` / `period` | fichas máximas / fichas por recarga / tempo entre recargas (`Duration`), como no `rate-limit.yaml` | `TokenBucket.Limit` |
| `pattern` | o caminho da rota em qualquer versão (`/api/*` + caminho) | `Route` |
| `resolve` | escolher o `TokenBucket` da requisição, ou o fallback `api` | `RateLimitPolicy` |
| `account` / `ceiling` | o `TokenBucket` da conta (conta e rede no login; usuário na troca de senha) / o do teto da conta | `RateLimitPolicy` |
| `from` | montar as rotas da API com o limite de cada uma | `Routes` |
| `deadline` | instante (epoch) da próxima recarga | `RateLimitFilter` |
| `advise` | informar no `Retry-After` quando tentar de novo | `RateLimitFilter` |
| `within` | exigir que o consumo esteja dentro do limite, senão `429` | `LoginAttemptService`, `PasswordAttemptService` |
| `network` / `account` | no login, gastar da conta naquela rede / do teto da conta (só se a rede ainda tinha saldo) | `LoginAttemptService` |

Classes do rate limit:

| Classe | Responsabilidade |
|---|---|
| `TokenBucket` | o algoritmo e os saldos de um limite, a validação do limite e o `retry`, com `Limit` (configuração), `Allowance` (resultado) e `Bucket` (saldo) aninhados como records só de dados |
| `Route` | record no estilo dos VOs: método e caminho validados |
| `RateLimitPolicy` | dona de todos os `TokenBucket`: o de cada rota, o fallback, o da conta e o do teto; fecha todos no `close()` |
| `Routes` | as rotas da API e o limite de cada uma |

### Rede

| Verbo | Sentido | Onde |
|---|---|---|
| `address` | ler o endereço do cliente como literal IP, sem DNS | `ClientNetwork` |
| `literal` | exigir que o texto seja um literal IPv4 ou IPv6 | `ClientNetwork` |
| `ip` | o texto tem forma de literal IP | `ClientNetwork` |
| `parse` | converter o literal em `InetAddress` | `ClientNetwork` |
| `invalid` | a exceção de endereço inválido | `ClientNetwork` |
| `network` | a rede (`Network`) do endereço já lido | `ClientNetwork` |
| `from` | escolher a família (`Ipv4`, `Ipv6`) pelo endereço | `Network` |
| `key` | chave da rede do cliente (ex.: `203.0.113.7`, `2001:db8::/56`), parte da chave do saldo | `Network`, `Mask`, `ClientNetwork` |
| `prefix` | bits que identificam a rede na família | `Ipv4`, `Ipv6` |
| `format` | formato da chave da família (`%s`, `%s/%d`) | `Ipv4`, `Ipv6` |
| `valid` | o texto tem a sintaxe de um endereço da família | `Ipv4`, `Ipv6` |
| `syntax` | a regex que define a sintaxe da família (no IPv6, montada a partir das formas abaixo) | `Ipv4`, `Ipv6` |
| `expanded` | a forma do IPv6 com os 8 hextets escritos | `Ipv6` |
| `compressed` | a forma do IPv6 com `::` depois de um número de hextets | `Ipv6` |
| `head` / `tail` | os hextets antes / depois do `::` | `Ipv6` |
| `apply` | aplicar o prefixo e devolver o endereço da rede | `Mask` |
| `truncate` | cortar o endereço no prefixo, zerando os bits depois dele | `Mask` |

### Gerais

| Verbo | Sentido |
|---|---|
| `create` | montar uma entidade sem gravar |
| `save` | gravar |
| `find` | buscar e lançar se não existir (nunca devolve `Optional`) |
| `show` | converter para o DTO de resposta |
| `send` / `sendAttachments` | enviar e-mail sem anexos / com anexos |
| `load` | conferir e ler os anexos uma vez, pelo caminho real e sem seguir link | `MailService` |
| `plain` / `multipart` | montar a mensagem de texto / com partes |
| `view` | consultar o perfil do usuário (`ProfileService`) |
