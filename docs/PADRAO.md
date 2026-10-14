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
- Classes de cálculo (`Bucket`, `Limit`, `TokenBucket`) seguem o mesmo estilo de passos.

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
| `known` | o dispositivo já pertence ao usuário | `DeviceService` |

### Senha

| Verbo | Sentido | Onde |
|---|---|---|
| `change` | caso de uso de troca de senha | `ChangePasswordService` |
| `commit` | efetivar, numa transação, uma troca já validada (revoga as sessões, grava, emite tokens) | `PasswordService` |
| `replace` | gravar a nova senha no lugar da atual | `LocalCredentialService` |
| `hash` | calcular o hash de um segredo | `LocalCredentialService`, `RefreshTokenService` |
| `rehash` | conferir que a nova senha é diferente da atual e calcular o hash dela | `LocalCredentialService` |

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

| Verbo | Sentido | Onde |
|---|---|---|
| `consume` | gastar uma ficha | `Rule`, `TokenBucket`, `Bucket` |
| `take` | `consume` com o instante explícito (gancho de teste) | `TokenBucket` |
| `refund` | devolver uma ficha | `Rule`, `TokenBucket`, `Bucket` |
| `refill` | recarregar as fichas pelo tempo decorrido | `Bucket` |
| `periods` | quantos intervalos completos se passaram desde a última recarga | `Bucket` |
| `advance` / `next` | avançar o bucket para a próxima recarga, se houver intervalo completo | `Bucket` |
| `level` / `later` | saldo e instante depois da recarga | `Bucket` |
| `moment` | instante da última recarga | `Bucket` |
| `rate` / `period` | fichas por recarga / tempo entre recargas (`Duration`); `interval()` é o mesmo período em segundos | `Limit` |
| `reset` | segundos até a próxima recarga | `Bucket`, `Verdict` |
| `deadline` | instante (epoch) da próxima recarga | `RateLimitFilter` |
| `capped` | limitado à capacidade | `Bucket` |
| `key` | chave do saldo (ex.: `ip:...`, `login:...`) | `Attempt`, `TokenBucket`, `RateLimitFilter` |
| `pattern` | casar método e caminho da rota | `Matcher` |
| `delegate` | o `RequestMatcher` do Spring que o `Matcher` envolve | `Matcher` |
| `within` | exigir que o consumo esteja dentro do limite | `AttemptLimiter` |
| `advise` | informar no `Retry-After` quando tentar de novo | `RateLimitFilter` |
| `retry` | segundos até poder tentar de novo | `Verdict` |
| `allowed` | resultado do último consumo, guardado no `Bucket` porque o `compute` do mapa só devolve o novo valor | `Bucket`, `Verdict` |

Tipos do rate limit: `Limit` é a configuração (capacidade, taxa, período); `Rule` é uma regra (rota + limite + saldos); `Verdict` é o resultado de um consumo (permitido, restante, reset); `RateLimitPolicy` escolhe a regra da requisição.

### Gerais

| Verbo | Sentido |
|---|---|
| `create` | montar uma entidade sem gravar |
| `save` | gravar |
| `find` | buscar e lançar se não existir (nunca devolve `Optional`) |
| `show` | converter para o DTO de resposta |
| `send` / `sendAttachments` | enviar e-mail sem anexos / com anexos |
| `plain` / `multipart` | montar a mensagem de texto / com partes |
| `view` | consultar o perfil do usuário (`ProfileService`) |
