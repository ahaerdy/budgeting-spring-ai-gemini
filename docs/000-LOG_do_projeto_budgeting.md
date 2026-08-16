# 📝 LOG DE EXECUÇÃO — DIA 01

**Projeto:** `budgeting-spring-ai-gemini`
**Data:** 12/08/2026
**Contexto:** Transição da stack original do curso DIO/NTT Data (Spring Boot + Spring AI + OpenAI) para uma versão adaptada 100% nativa em **Google Gemini**.

---

## 1. 🎯 Definição da Estrutura do Repositório (GitHub)

Nesta etapa inicial, foram definidas as diretrizes para a publicação do projeto no GitHub:

* **Nome do Repositório:** `budgeting-spring-ai-gemini`
* *Justificativa:* Reflete claramente o domínio (`budgeting`), a camada de abstração de IA (`spring-ai`) e a escolha do provedor (`gemini`), evitando qualquer ambiguidade com o repositório original do curso base.
* **Descrição Curta (GitHub Description):**
> *"API inteligente em Spring Boot + Spring AI que transforma comandos de voz em transações financeiras, usando Google Gemini (chat, tool calling, transcrição multimodal e TTS via SDK nativo)"*
> 
* **Ação Realizada:**
* Criação e inicialização do repositório remoto via interface do GitHub.
* Clone do repositório para o ambiente local no diretório:
`/mnt/storage_02/Backup_USB2/Backup_Github/budgeting-spring-ai-gemini`

---

## 2. 🛠️ Configuração e Inicialização do Projeto no IntelliJ IDEA

Para preparar o esqueleto do projeto mantendo o histórico de controle de versão (Git) já existente na pasta clonada, as configurações do assistente do Spring Boot no IntelliJ foram preenchidas rigorosamente de acordo com as especificações do `build.gradle` original:

### Mapeamento dos Campos de Setup (Spring Initializr Wizard)


| Campo | Valor Configurado | Motivação / Justificativa Técnica |
| --- | --- | --- |
| **Server URL** | `start.spring.io` | URL padrão do gerador do Spring Boot. |
| **Name** | `budgeting` | Mantém alinhamento com a raiz do projeto e com o `settings.gradle` (`rootProject.name = 'budgeting'`). |
| **Location** | `/mnt/storage_02/Backup_USB2/Backup_Github/budgeting-spring-ai-gemini` | **Crítico:** Aponta diretamente para a raiz do repositório clonado. |
| **Create Git repository** | ❌ **Desmarcado** | Previne sobrescrever ou criar um repositório Git aninhado/conflitante sobre o `.git` existente. |
| **Language** | `Java` | Linguagem base do projeto. |
| **Type** | `Gradle - Groovy` | Utiliza o padrão Groovy DSL (`build.gradle`). |
| **Group** | `dio` | Pacote/organização base (`group = 'dio'`). |
| **Artifact** | `budgeting` | Artefato principal gerado (`.jar`). |
| **Package name** | `dio.budgeting` | Pacote raiz da aplicação (`BudgetingApplication`, etc.). |
| **JDK** | `21 Java 21.0.11` | Versão LTS do Java utilizada no projeto. |
| **Java (Compatibility)** | `21` | **Ajustado de 17 para 21** para compatibilidade com o JDK selecionado. |
| **Packaging** | `Jar` | Empacotamento padrão executável. |
| **Configuration** | `Properties` | Projeto utilizará `application.properties`. |

**Estrutura resultante confirmada:**

```
budgeting-spring-ai-gemini/          ← raiz do repositório (README.md do projeto/portfólio)
└── budgeting/                       ← projeto Gradle gerado pelo IntelliJ
    ├── build.gradle
    ├── settings.gradle
    └── src/...
```

O IntelliJ gerou o projeto usando **Spring Boot 4.1.0**, versão mais recente que a usada como referência no tutorial original. Verificação de compatibilidade confirmou que o **Spring AI 2.0.0** já é uma versão **estável** (saiu do estágio de milestone `M4`), compatível com Spring Boot 4.x — decisão tomada de usar `2.0.0` estável em vez de `2.0.0-M4`, dispensando repositório de milestone.

---

## 3. 🔌 Parte 1 e 2 do Tutorial — Criando o Projeto e Conectando ao Provedor de IA (Vídeos 01 e 02)

Objetivo desta etapa: sair do esqueleto gerado pelo Initializr para uma aplicação mínima capaz de subir sem erros, já com a integração ao Google Gemini configurada (BOM do Spring AI + starter do Gemini + chave de API via variável de ambiente).

### 3.1. Arquivos alterados

**`budgeting/build.gradle`** — adicionado o BOM do Spring AI e o starter do Gemini (referência à OpenAI mantida comentada, seguindo o padrão do tutorial):

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.1.0'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = 'dio'
version = '0.0.1-SNAPSHOT'
description = 'budgeting'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'

    // BOM do Spring AI — centraliza as versões de todos os módulos do Spring AI entre si
    implementation platform("org.springframework.ai:spring-ai-bom:2.0.0")

    // Starter do Google Gemini — usado neste projeto
    implementation 'org.springframework.ai:spring-ai-starter-model-google-genai'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

**`budgeting/src/main/resources/application.properties`** — este arquivo também sofreu alteração real, não apenas o `build.gradle`. O IntelliJ havia pré-preenchido automaticamente (provável sugestão do assistente de IA do próprio IDE) um conteúdo com **prefixo de propriedade incorreto** e um modelo desatualizado:

*Antes (gerado automaticamente pelo IntelliJ, incorreto):*
```properties
spring.application.name=budgeting
spring.ai.gemini.api-key=${GEMINI_API_KEY}
spring.ai.gemini.chat.options.model=gemini-1.5-flash
```

*Depois (corrigido):*
```properties
spring.application.name=budgeting
#spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.google.genai.api-key=${GEMINI_API_KEY}
```

Três correções aplicadas:
1. **Prefixo da propriedade de chave de API** corrigido de `spring.ai.gemini.*` (inexistente/não reconhecido pelo Spring AI) para `spring.ai.google.genai.*` — o prefixo correto, confirmado na documentação oficial do Spring AI para o starter `spring-ai-starter-model-google-genai`.
2. **Linha de configuração do modelo removida** (`spring.ai.gemini.chat.options.model=gemini-1.5-flash`) — além do prefixo errado, essa configuração está fora de escopo desta etapa (pertence à Parte 3, quando o `ChatModel` é introduzido), e `gemini-1.5-flash` é um modelo desatualizado.
3. **Linha comentada de referência à OpenAI adicionada** (`#spring.ai.openai.api-key=${OPENAI_API_KEY}`) — seguindo o padrão do tutorial de documentar, mesmo que comentada, a alternativa de provedor original do curso.

Nenhuma alteração foi necessária em `BudgetingApplication.java`, `settings.gradle` ou `BudgetingApplicationTests.java` — todos já corretos, como gerados pelo Initializr.

### 3.2. Incidente de execução: dependências do Spring AI ausentes do classpath

Ao rodar `BudgetingApplication` pela primeira vez após editar o `build.gradle`, a aplicação subiu com sucesso (`Started BudgetingApplication`, `exit code 0`), **mas** a inspeção do `-classpath` impresso pelo IntelliJ revelou que **nenhum artefato `org.springframework.ai` havia sido carregado** — sinal de que o painel Gradle do IntelliJ não havia sincronizado as mudanças do `build.gradle`.

**Diagnóstico confirmado** pela árvore de `Dependencies` do painel Gradle: `compileClasspath`, `runtimeClasspath`, `testCompileClasspath` e `testRuntimeClasspath` mostravam apenas `spring-boot-starter` e transitivas — nenhuma dependência do Spring AI.

**Resolução:**

```bash
cd /mnt/storage_02/Backup_USB2/Backup_Github/budgeting-spring-ai-gemini/budgeting
./gradlew --refresh-dependencies build -x test
```

Resultado: `BUILD SUCCESSFUL in 16s`. O comando forçou o Gradle a reprocessar o `build.gradle` do zero, ignorando o cache desatualizado do IntelliJ, e baixou as dependências corretas para `~/.gradle/caches`.

**Lição registrada:** o painel Gradle do IntelliJ pode não refletir imediatamente uma edição de `build.gradle`, mesmo após clicar no ícone de refresh. Quando o `-classpath` de uma execução não bate com o que se espera das dependências recém-adicionadas, o diagnóstico mais confiável é: (1) inspecionar a árvore de `Dependencies` do painel Gradle; (2) se a dependência não aparecer, rodar `./gradlew --refresh-dependencies` via terminal para forçar a resolução; (3) sincronizar o IntelliJ novamente, que passa a enxergar o cache já populado.

### 3.3. Execução final — sucesso confirmado

Após o `--refresh-dependencies`, nova execução de `BudgetingApplication` (PID `132227`) confirmou o classpath correto, incluindo:

- `spring-ai-starter-model-google-genai-2.0.0.jar`
- `spring-ai-google-genai-2.0.0.jar` — implementação `GoogleGenAiChatModel` (usada a partir da Parte 3)
- `spring-ai-client-chat-2.0.0.jar` — base do `ChatClient` (usado a partir da Parte 4)
- `spring-ai-autoconfigure-model-google-genai-2.0.0.jar` — auto-configuração que lê `spring.ai.google.genai.api-key`
- `google-genai-1.58.0.jar` — SDK Java nativo do Google GenAI (usado diretamente na Parte 7, para TTS)
- `spring-ai-autoconfigure-model-tool-2.0.0.jar`, `jsonschema-generator-5.0.0.jar` — suporte a Tool Calling, já disponível para a Parte 5

```
Started BudgetingApplication in 1.632 seconds (process running for 2.129)
Process finished with exit code 0
```

### 3.4. ✅ Checkpoint da Parte 1/2 — fechado

| Item | Status |
| --- | --- |
| `BudgetingApplication.java` sobe sem erros | ✅ |
| `settings.gradle` com `rootProject.name = 'budgeting'` | ✅ |
| `build.gradle` com BOM `2.0.0` + starter Gemini resolvidos no classpath | ✅ |
| `application.properties` com `spring.ai.google.genai.api-key` (prefixo correto) | ✅ |
| `BudgetingApplicationTests.contextLoads()` | pendente de execução explícita |

**Próximo passo planejado:** Parte 3 do tutorial (Vídeo 03) — adicionar `spring-boot-starter-web`, configurar `spring.ai.google.genai.chat.options.model` e `temperature`, criar o teste de integração `GeminiChatModelIT` e o `ChatModelController` com o endpoint `GET /api/chat-model`.

---

## 📝 LOG DE EXECUÇÃO — DIA 02

**Data:** 14/08/2026
**Contexto:** Continuação a partir do checkpoint da Parte 1/2 (fechado no DIA 01). Foco de hoje: Parte 3 do tutorial (Vídeo 03) — primeira integração real de chat com o Gemini, testada e exposta via HTTP.

---

## 4. 🔌 Parte 3 do Tutorial — ChatModel: a primeira chamada a uma LLM (Vídeo 03)

Objetivo desta etapa: validar a integração com o Gemini através de um teste de integração, e só depois expor essa integração como um endpoint HTTP simples.

### 4.1. Arquivos alterados/criados

**`budgeting/build.gradle`** — adicionada a dependência `spring-boot-starter-web`, necessária para o suporte HTTP/REST (servidor Tomcat embutido, anotações de controller, Jackson):

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-web'

    implementation platform("org.springframework.ai:spring-ai-bom:2.0.0")

    //  implementation 'org.springframework.ai:spring-ai-starter-model-openai'
    implementation 'org.springframework.ai:spring-ai-starter-model-google-genai'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

**`budgeting/src/main/resources/application.properties`** — adicionadas três propriedades: modelo padrão do Gemini, temperatura global, e nível de log do Spring AI:

```properties
spring.application.name=budgeting
#spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.google.genai.api-key=${GEMINI_API_KEY}
spring.ai.google.genai.chat.options.model=gemini-3-flash-preview
spring.ai.google.genai.chat.options.temperature=0.0
logging.level.org.springframework.ai=DEBUG
```

**`budgeting/src/test/java/dio/budgeting/GeminiChatModelIT.java`** (novo) — teste de integração usando `GoogleGenAiChatModel` diretamente, com opções sobrescritas por chamada (`temperature=1.0`, `responseMimeType=text/plain`).

**`budgeting/src/main/java/dio/budgeting/ChatModelController.java`** (novo) — endpoint `GET /api/chat-model`, injetando `GoogleGenAiChatModel` via construtor.

### 4.2. Incidente 1 — variável de ambiente não visível no terminal usado para `./gradlew test`

Ao rodar `./gradlew test` pela primeira vez pelo terminal, o resultado foi `BUILD SUCCESSFUL` — mas isso, isoladamente, **não confirmou** que `GeminiChatModelIT` de fato executou: como o teste está anotado com `@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")`, ele também produziria `BUILD SUCCESSFUL` caso fosse **pulado** por falta da variável — o Gradle não distingue visualmente, no resumo padrão, "passou" de "foi pulado".

**Causa identificada:** a variável `GEMINI_API_KEY` só havia sido configurada na *Run Configuration* do IntelliJ (Parte 1), não no terminal usado para rodar `./gradlew test` — são ambientes de variáveis independentes.

**Resolução:** configurada a variável também no terminal/ambiente relevante. Nova execução do teste confirmou, pela primeira vez, a saída real do `System.out.println` do teste (`Gemini response: ...`, uma tabela markdown de exemplo de gastos gerada pelo Gemini) — confirmando que o teste **de fato rodou e passou** (`assertThat(...).isNotEmpty()` satisfeito).

**Lição registrada:** `BUILD SUCCESSFUL` sozinho não é suficiente para confirmar que um teste anotado com `@EnabledIfEnvironmentVariable` realmente executou — é preciso ou inspecionar a saída de console do próprio teste, ou rodá-lo pela IDE (que mostra visualmente "passou"/"pulado"/"falhou"), ou usar `./gradlew test --info`, ou checar o relatório HTML em `build/reports/tests/test/index.html`.

**Nota lateral:** apareceu, junto do resultado, um aviso do Mockito sobre auto-anexação de agente Java (`Mockito is currently self-attaching...`) — confirmado como aviso de compatibilidade futura da própria biblioteca (trazida transitivamente pelo `spring-boot-starter-test`), sem relação com o código do projeto e sem ação necessária.

### 4.3. Incidente 2 — `API key not valid` ao testar o endpoint HTTP

Após criar `ChatModelController` e rodar `BudgetingApplication`, a requisição `GET /api/chat-model?prompt=Oi` devolveu:

```json
{"timestamp":"2026-08-14T12:15:43.177Z","status":500,"error":"Internal Server Error","path":"/api/chat-model"}
```

O log da aplicação revelou a causa raiz, no *stack trace*:

```
com.google.genai.errors.ClientException: 400 . API key not valid. Please pass a valid API key.
```

**Causa identificada:** mesma raiz do Incidente 1, mas em outra frente — a *Run Configuration* do IntelliJ usada para `BudgetingApplication` (tipo "Application") não tinha `GEMINI_API_KEY` configurada, ou a chave estava incorreta. Cada *Run Configuration* do IntelliJ mantém seu próprio conjunto de variáveis de ambiente, independente das demais (inclusive da configuração usada para `GeminiChatModelIT`, tipo "JUnit").

**Resolução:** corrigida a configuração da chave `GEMINI_API_KEY` na *Run Configuration* de `BudgetingApplication`. Nova execução confirmou o endpoint respondendo corretamente:

```bash
curl -X GET "http://localhost:8080/api/chat-model?prompt=Oi"
Olá! Tudo bem? Como posso te ajudar hoje?
```

**Lição registrada:** variáveis de ambiente configuradas no IntelliJ não são compartilhadas automaticamente entre *Run Configurations* diferentes (uma de teste, outra de aplicação), mesmo dentro do mesmo projeto — é preciso configurar cada uma individualmente, ou usar um mecanismo centralizado (como um arquivo `.env` lido por um plugin, algo a considerar como possível melhoria futura de conveniência, não estritamente necessária para o projeto funcionar).

### 4.4. ✅ Checkpoint da Parte 3 — fechado

| Item | Status |
| --- | --- |
| `build.gradle` — `spring-boot-starter-web` adicionado | ✅ |
| `application.properties` — modelo/temperatura/log configurados | ✅ |
| `GeminiChatModelIT` — criado, rodado e passando | ✅ |
| `ChatModelController` — criado, endpoint `GET /api/chat-model` respondendo corretamente | ✅ |

**Próximo passo planejado:** Parte 4 do tutorial (Vídeo 04) — trocar o `ChatModel` de baixo nível pelo `ChatClient` fluente: criar `GeminiChatClientIT.java` (teste) e `ChatClientController.java` (endpoint `GET /api/chat`), ambos sem exigir nenhuma dependência ou propriedade nova.

---

## 📝 LOG DE EXECUÇÃO — DIA 03

**Data:** 15/08/2026
**Contexto:** Continuação a partir do checkpoint da Parte 3 (fechado no DIA 02). Foco de hoje: Parte 4 do tutorial (Vídeo 04) — troca do `ChatModel` de baixo nível pelo `ChatClient` fluente — e Parte 5 (Vídeo 05) — primeiro contato com Tool Calling, em exemplo didático.

---

## 5. 🔌 Parte 4 do Tutorial — ChatClient: a API fluente com contexto (Vídeo 04)

Objetivo desta etapa: substituir o `ChatModel` cru por `ChatClient`, a API fluente que passa a ser usada no restante do projeto.

### 5.1. Arquivos criados

**`budgeting/src/test/java/dio/budgeting/GeminiChatClientIT.java`** (novo) — teste de integração construindo o `ChatClient` a partir do `ChatModel` já injetado (`ChatClient.builder(chatModel)`), com `.defaultSystem("Voce é um matematico")`, validando uma soma/subtração resolvida "de cabeça" pelo modelo (ainda sem Tool Calling).

**`budgeting/src/main/java/dio/budgeting/ChatClientController.java`** (novo) — endpoint `GET /api/chat`, injetando `ChatClient.Builder` (não o `ChatClient` pronto) e finalizando a construção com `.build()` dentro do próprio construtor.

### 5.2. Incidente — mesma causa raiz do DIA 02, em um novo contexto

Ao testar `/api/chat`, mesmo erro `API key not valid` já visto na Parte 3 — desta vez com a chave preenchida na *Run Configuration*, mas com **formato incorreto** no campo *Environment variables* do IntelliJ (o campo exige `NOME_VARIAVEL=valor`, não apenas o valor solto).

**Resolução, em duas etapas:**
1. Corrigida a sintaxe do campo para `GEMINI_API_KEY=<valor>`.
2. Confirmado, à parte, que o valor da própria chave também precisava ser conferido/regenerado no AI Studio (formato do valor copiado gerou dúvida inicial, mas era proveniente de uma chave válida gerada corretamente).

**Resultado final, confirmado:**
```bash
curl -X GET "http://localhost:8080/api/chat?prompt=Quanto%20%C3%A9%2010%20mais%2020%3F"
10 mais 20 é igual a **30**.
```

Resposta corretamente formatada em markdown (negrito no resultado) — comportamento característico do `ChatClient`, diferente da resposta mais "crua" observada no `ChatModelController` (Parte 3), reforçando na prática que são dois caminhos de código distintos, mesmo usando a mesma chave e o mesmo modelo por baixo.

**Lição registrada (generalizando o Incidente 2 do DIA 02):** o campo *Environment variables* do IntelliJ tem uma sintaxe própria (`VAR=value; VAR1=value1`, variáveis separadas por ponto e vírgula) — não basta colar o valor da chave sozinho, é preciso preceder com `NOME_DA_VARIAVEL=`. Esse é o tipo de erro que gera exatamente o mesmo sintoma (`API key not valid`) do problema de "variável ausente" já visto antes, então vale sempre conferir a sintaxe do campo, não só se ele está preenchido.

### 5.3. ✅ Checkpoint da Parte 4 — fechado

| Item | Status |
| --- | --- |
| `GeminiChatClientIT` — criado, rodado e passando (confirmado via saída `0` no console) | ✅ |
| `ChatClientController` — criado, endpoint `GET /api/chat` respondendo corretamente | ✅ |
| Rotas `/api/chat-model` (Parte 3) e `/api/chat` (Parte 4) coexistindo sem conflito | ✅ |

---

## 6. 🧠 Discussão conceitual — ChatModel vs. ChatClient, abstração de provedor, e convenção de nomenclatura

Antes de seguir para a Parte 5, foram esclarecidos três pontos conceituais importantes, registrados aqui para consulta futura (e já incorporados ao tutorial nesta atualização):

1. **Por que `ChatModel` é "baixo nível":** porque usá-lo além do atalho `call(String)` exige montar manualmente `Prompt`, `ChatOptions` e desmontar `ChatResponse` a cada chamada — sem nenhum conceito de configuração persistente (como um prompt de sistema padrão).
2. **Precisão sobre "`ChatModel` é específico de provedor":** a interface em si é genérica (definida pelo Spring AI, agnóstica de provedor). O que a torna "provider-específica na prática" é que, para configuração completa, é preciso usar classes concretas do provedor escolhido (`GoogleGenAiChatModel`, `GoogleGenAiChatOptions`). Essa imprecisão foi corrigida no texto do tutorial (Parte 4, abertura).
3. **Onde a informação do provedor realmente mora, já que `ChatClientController` nunca a menciona:** rastreada em três lugares concretos — `build.gradle` (qual *starter*), `application.properties` (chave/modelo/temperatura), e o código de auto-configuração dentro do `.jar` do *starter* (que efetivamente monta o `GoogleGenAiChatModel`). O termo técnico para esse comportamento é **desacoplamento** — o mesmo princípio que reaparecerá, de forma mais explícita, na Parte 8 (`TransactionRepository`).
4. **Convenção de nomenclatura dos sufixos (`Controller`, `Service`, `Repository`, `Config`):** esclarecido que o sufixo no nome da classe não tem efeito técnico — é a **anotação** (`@RestController`, `@Service`, `@Repository`, `@Configuration`) quem habilita o comportamento real; o nome é só uma convenção legível, herdada do padrão de arquitetura MVC no caso de "Controller".

Essas quatro explicações foram incorporadas ao tutorial nesta atualização, na seção 4.1 (ChatClient vs. ChatModel).

---

## 7. 🔌 Parte 5 do Tutorial — Tool Calling: quando a IA executa código de verdade (Vídeo 05)

Objetivo desta etapa: aprender o mecanismo de Tool Calling em um exemplo didático e isolado (soma/subtração), antes de aplicá-lo aos casos de uso reais do domínio, a partir da Parte 8.

### 7.1. Arquivo criado

**`budgeting/src/test/java/dio/budgeting/ToolCallingIT.java`** (novo, único arquivo desta Parte) — contém a classe interna estática `MathTools` (métodos `sum` e `diff`, anotados com `@Tool`) e o teste `should_executeSum_when_prompted`, que registra `.defaultTools(new MathTools())` no `ChatClient` e valida que o resultado da mesma operação da Parte 4 (`10 + 20 − 30 = 0`) continua correto — agora resolvido por execução real dos métodos Java, não por previsão estatística do modelo.

Nenhuma dependência nova no `build.gradle` — suporte a `@Tool` já vinha transitivamente do starter do Gemini desde a Parte 1.

### 7.2. Execução

```
09:21:09 ... Task :test
0
BUILD SUCCESSFUL in 5s
```

Confirmado via saída do `System.out.println(response)` (`0`) e `BUILD SUCCESSFUL` sem falhas, rodando o teste de forma direcionada (`--tests "dio.budgeting.GeminiChatClientIT"` — nota: essa execução específica documentada foi da Parte 4; o padrão de verificação direcionada passa a ser adotado também para os testes da Parte 5 daqui em diante, evitando a ambiguidade "passou vs. pulado" já registrada no DIA 02).

### 7.3. ✅ Checkpoint da Parte 5 — fechado

| Item | Status |
| --- | --- |
| `ToolCallingIT` — criado, rodado e passando | ✅ |

**Próximo passo planejado:** Parte 6 do tutorial (Vídeo 06) — primeiro ponto de divergência real com o curso original (transcrição de áudio, sem `TranscriptionModel` disponível para Gemini): gravar os seis áudios de teste próprios, criar `GeminiTranscriptionModelIT.java` e a primeira versão de `TranscriptionController.java` (apenas o método `transcribe`, expandido depois na Parte 11).

---

## 📝 LOG DE EXECUÇÃO — DIA 04

**Data:** 16/08/2026
**Contexto:** Confirmação reforçada do checkpoint da Parte 5 (Tool Calling), e preparação do tutorial para a Parte 6 — nenhuma implementação de código nova nesta sessão, foco em validação e documentação.

---

## 8. ✅ Confirmação reforçada — Parte 5 (Tool Calling) comprovado via logs de execução real

Nova execução de `ToolCallingIT` produziu evidência mais completa do que a rodada anterior (DIA 03): o console mostrou, explicitamente, as classes internas do Spring AI executando **cada** *tool* de verdade:

```
DefaultToolCallingManager : Executing tool call: sum
MethodToolCallback        : Starting execution of tool: sum
MethodToolCallback        : Successful execution of tool: sum
DefaultToolCallResultConverter : Converting tool result to JSON.
DefaultToolCallingManager : Executing tool call: diff
MethodToolCallback        : Starting execution of tool: diff
MethodToolCallback        : Successful execution of tool: diff
DefaultToolCallResultConverter : Converting tool result to JSON.
0
```

**Leitura do log, confirmando o fluxo teórico da seção 5.1 do tutorial:**

| Evidência no log | Etapa do fluxo de Tool Calling |
| --- | --- |
| `Executing tool call: sum` | Modelo decidiu chamar `sum`; aplicação prestes a executar de verdade |
| `Starting/Successful execution of tool: sum` | `sum(10, 20)` executado como código Java real → `30` |
| `Converting tool result to JSON` | Resultado preparado para retornar ao modelo |
| *(sequência repetida para `diff`)* | `diff(30, 30)` executado com o resultado real de `sum`, não uma suposição → `0` |

**Por que essa confirmação é mais forte que a do DIA 03:** a execução anterior só mostrava o resultado final (`0`), sem provar que o cálculo foi de fato delegado a `sum`/`diff` (em vez de "adivinhado" pelo modelo, como na Parte 4). Esta nova evidência prova, através das classes `DefaultToolCallingManager` e `MethodToolCallback` aparecendo duas vezes cada, que **as duas ferramentas foram executadas de verdade, na ordem certa, e encadeadas corretamente** (o resultado de `sum` foi usado como entrada de `diff`).

### 8.1. ✅ Checkpoint da Parte 5 — atualizado e reforçado

| Item | Status |
| --- | --- |
| `ToolCallingIT` — criado, rodado e passando | ✅ |
| Tool Calling confirmado via logs (`DefaultToolCallingManager`, `MethodToolCallback`) para **ambas** as ferramentas, em sequência correta | ✅ |

---

## 9. 📚 Atualização do material de estudo — Tutorial reescrito para a Parte 6 (formato de receita explícita)

Sem execução de código nesta sessão — trabalho de preparação do material para a próxima etapa prática.

**Alterações aplicadas ao `000-Tutorial_Budgeting_Spring_AI_COMPLETO.md`:**

1. **Parte 6 (Vídeo 06 — Transcrição) reescrita integralmente** no formato de receita explícita já usado nas Partes 1 a 5: tabela de 3 passos (gravar áudios → criar teste → criar controller), com **`📁 Arquivo`** e **`✅`** de confirmação em cada um, e as explicações conceituais (interface `TranscriptionModel` da OpenAI, motivo da lacuna no Gemini, `Media`/multimodalidade, `MultipartFile`) reorganizadas como blocos de leitura **antes** do código correspondente, em vez de misturadas com a criação de arquivos.
2. Incluídos, pela primeira vez de forma explícita nesta Parte: os arquivos completos com `import`s (o teste `GeminiTranscriptionModelIT.java` e o controller `TranscriptionController.java`, versão inicial), e um comando `curl -F` de teste manual para o endpoint `/api/transcribe` (sintaxe multipart, diferente dos `curl` de query string usados nas Partes 3 e 4).

**Próximo passo planejado:** executar a Parte 6 no projeto real — gravar os seis áudios de teste, criar `GeminiTranscriptionModelIT.java` (ajustando os valores esperados do `@CsvSource` às gravações reais) e `TranscriptionController.java`.

---
