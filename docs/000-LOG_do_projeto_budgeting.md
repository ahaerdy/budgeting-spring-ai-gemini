# 📝 LOG DE EXECUÇÃO — DIA 01

**Projeto:** `budgeting-spring-ai-gemini`
**Data:** 12/08/2026
**Contexto:** Transição da stack original do curso DIO/NTT Data (Spring Boot + Spring AI + OpenAI) para uma versão adaptada 100% nativa em **Google Gemini**.

> **Nota sobre o campo "Marca de confiança" (introduzido em 17/08/2026):** a partir desta atualização, cada checkpoint de Parte passa a ter, além do ✅ de execução (criado/rodado/passando), uma segunda marca — **alta / média / baixa** — sobre o quanto Arthur sente que entendeu o *porquê* daquela Parte, não só o *que* foi feito. É palpite rápido, não teste formal; não deve travar o ritmo de execução. Checkpoints já fechados antes desta data (Partes 1 a 6) ficam marcados como **não avaliado retroativamente** — preencher de memória geraria dado artificial (ver `05-avaliacao_dev_java.md`, entrada de 20260816, 1ª sessão). A partir da Parte 7 em diante, preencher no momento em que cada checkpoint fechar.

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

**Marca de confiança:** Média (7/10). Ver `001-Detalhamento_Tecnico_do_Projeto.md` (Partes 1 e 2) — o detalhamento técnico linha a linha, produzido antes desta marca, é a razão registrada do valor: sem ele, a marca teria sido Baixa (abaixo de 5/10), segundo estimativa do próprio Arthur.

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

**Marca de confiança:** Média (7/10). Ver `001-Detalhamento_Tecnico_do_Projeto.md` (Parte 3) — mesma razão registrada da marca acima: sem o detalhamento produzido antes desta avaliação, a marca teria sido Baixa (abaixo de 5/10).

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

**Marca de confiança:** não avaliado retroativamente (checkpoint anterior à introdução do campo, 17/08/2026)

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

**Marca de confiança:** não avaliado retroativamente (checkpoint anterior à introdução do campo, 17/08/2026)

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

**Marca de confiança:** Média (7/10)

---

## 9. 📚 Atualização do material de estudo — Tutorial reescrito para a Parte 6 (formato de receita explícita)

Sem execução de código nesta sessão — trabalho de preparação do material para a próxima etapa prática.

**Alterações aplicadas ao `000-Tutorial_Budgeting_Spring_AI_COMPLETO.md`:**

1. **Parte 6 (Vídeo 06 — Transcrição) reescrita integralmente** no formato de receita explícita já usado nas Partes 1 a 5: tabela de 3 passos (gravar áudios → criar teste → criar controller), com **`📁 Arquivo`** e **`✅`** de confirmação em cada um, e as explicações conceituais (interface `TranscriptionModel` da OpenAI, motivo da lacuna no Gemini, `Media`/multimodalidade, `MultipartFile`) reorganizadas como blocos de leitura **antes** do código correspondente, em vez de misturadas com a criação de arquivos.
2. Incluídos, pela primeira vez de forma explícita nesta Parte: os arquivos completos com `import`s (o teste `GeminiTranscriptionModelIT.java` e o controller `TranscriptionController.java`, versão inicial), e um comando `curl -F` de teste manual para o endpoint `/api/transcribe` (sintaxe multipart, diferente dos `curl` de query string usados nas Partes 3 e 4).

**Próximo passo planejado:** executar a Parte 6 no projeto real — gravar os seis áudios de teste, criar `GeminiTranscriptionModelIT.java` (ajustando os valores esperados do `@CsvSource` às gravações reais) e `TranscriptionController.java`.

---

## 📝 LOG DE EXECUÇÃO — DIA 05

**Data:** 16/08/2026 (continuação da mesma sessão do DIA 04)
**Contexto:** Sessão de perguntas conceituais, sem execução de código, aprofundando o entendimento de mecanismos de linguagem Java que apareceram na Parte 6 e que são transversais a todo o projeto. Consolidada como atualização do tutorial, por serem lacunas genuínas de compreensão que "copiar e colar" não preenchia.

---

## 10. 🧠 Discussão conceitual — encadeamento de métodos (`.`) e o padrão Builder, em profundidade

A partir de dúvidas sobre o trecho `UserMessage.builder().text(TRANSCRIPTION_PROMPT).media(List.of(audioMedia)).build()` (Parte 6), foram esclarecidos, em sequência:

1. **`@Test` (JUnit)** — o que a anotação faz mecanicamente: marca um método para ser localizado, instanciado (uma instância nova por teste) e executado automaticamente pelo JUnit, sem chamada manual; o resultado (passou/falhou) depende de nenhuma exceção ter sido lançada, incluindo as lançadas por asserções do AssertJ quando não satisfeitas.
2. **Escopo de `.defaultTools(...)`** — confirmado que a *tool* fica disponível apenas para a instância local de `ChatClient` construída naquele método específico, não para "a classe `ChatClient`" de forma geral — cada `ChatClient.Builder` é uma instância nova e isolada (reforçando o conceito de escopo `prototype`, já visto na Parte 4).
3. **Se `UserMessage` já vem preparada para receber `.media(...)` opcionalmente** — confirmado que sim: o método já existe pronto no `.jar` do Spring AI, é opcional, e seu uso é o que viabiliza a multimodalidade (Parte 6.2).
4. **Como entender uma cadeia de `.` (pontos), de forma geral** — o núcleo da discussão. Estabelecida a regra geral ("cada ponto opera sobre o que o pedaço anterior devolveu") e decomposto, passo a passo, o tipo devolvido em cada trecho de `GoogleGenAiChatOptions.builder().model(...).temperature(...).responseMimeType(...).build()`.
5. **A quem pertence um método como `.text(...)`** — esclarecido que métodos de configuração do Builder (`.text(...)`, `.model(...)`, `.temperature(...)`) pertencem à classe `X.Builder`, não à classe `X` final — e por quê (só é possível chamá-los **antes** de `.build()`).
6. **`UserMessage.Builder` como classe aninhada** — confirmado que `Builder` é uma classe declarada **dentro** do corpo da classe `UserMessage` (uma *nested class*), com uma estrutura interna ilustrada (campos, métodos que devolvem `this`, e o método `build()` que finalmente monta o objeto real).

### 10.1. 📚 Atualização aplicada ao tutorial

A explicação completa foi incorporada ao `000-Tutorial_Budgeting_Spring_AI_COMPLETO.md`, na **Parte 3.4** (primeira aparição do padrão Builder no tutorial, com `GoogleGenAiChatOptions.builder()`), incluindo:

- A regra geral de leitura de cadeias de método (`.`).
- Uma tabela rastreando o tipo devolvido em cada trecho da cadeia `GoogleGenAiChatOptions.builder()...build()`.
- Uma estrutura de código ilustrativa mostrando `Builder` como classe aninhada, com métodos devolvendo `this` e `.build()` finalizando a construção.
- O motivo de projetar assim em vez de um construtor comum (evitar passar todos os parâmetros de uma vez, inclusive os não usados).
- Um contraste explícito com uma cadeia **não-Builder** já vista (`chatClient.prompt().user(prompt).call().content()`, Parte 4.2), reforçando que a regra de leitura é a mesma, mas o tipo devolvido a cada passo muda de forma diferente.

Na **Parte 6.4**, o trecho sobre `UserMessage.builder()...build()` foi ajustado para referenciar essa explicação completa (em vez de duplicá-la), com uma nota adicional específica sobre `.text(...)`/`.media(...)` pertencerem à classe `UserMessage.Builder`.

**Lição registrada:** esta foi uma boa demonstração prática do próprio princípio discutido no primeiro dia do tutorial (ver "Discussão sobre nível de profundidade x caixas-pretas") — o padrão Builder havia sido tratado, até aqui, como algo a "aceitar e seguir em frente" (Nível 2/3 daquela conversa). Ao ser questionado com atenção, revelou-se um dos poucos conceitos verdadeiramente **transversais** ao projeto inteiro (reaparece em praticamente toda Parte restante do tutorial), justificando o investimento de reclassificá-lo para Nível 1 e documentá-lo em profundidade, de uma vez, no ponto de primeira aparição.

**Próximo passo, à época:** execução da Parte 6 no projeto real — registrado a seguir, ainda dentro do mesmo DIA 05.

---

## 11. 🔌 Parte 6 do Tutorial — Transcrevendo áudio em texto (Vídeo 06) — executada e concluída

Objetivo desta etapa: transformar áudio em texto usando o `GoogleGenAiChatModel` de forma multimodal (sem `TranscriptionModel`, que não existe para Gemini), validado por teste e exposto via HTTP.

### 11.1. Arquivos criados

**`budgeting/src/test/resources/audio/recording-1.mp3` a `recording-6.mp3`** — seis áudios próprios, gravados descrevendo gastos financeiros com valores distintos (Passo 1).

**`budgeting/src/test/java/dio/budgeting/GeminiTranscriptionModelIT.java`** (novo) — teste parametrizado (`@ParameterizedTest` + `@CsvSource`), uma execução por áudio, validando a transcrição de cada um contra uma palavra-chave esperada.

**`budgeting/src/main/java/dio/budgeting/TranscriptionController.java`** (novo, versão inicial) — endpoint `POST /api/transcribe`, injetando `GoogleGenAiChatModel`, montando uma `Media` a partir do arquivo recebido e devolvendo a transcrição como texto puro. Será expandido na Parte 11 do tutorial.

### 11.2. Incidente 1 — "No matching tests found" ao rodar o teste recém-criado

Primeira tentativa de `./gradlew test --tests "dio.budgeting.GeminiTranscriptionModelIT"` falhou com:
```
No matching tests found in any candidate test task.
```
mesmo com o código do arquivo já conferido, linha por linha, como correto (`package`, nome da classe, imports).

**Investigação:** hipótese inicial de que faltaria a anotação `@Test` — descartada, já que `@ParameterizedTest` já cumpre esse papel sozinha, sendo incompatível com `@Test` no mesmo método. Segunda hipótese, a dependência `junit-jupiter-params` (necessária para `@ParameterizedTest`/`@CsvSource`) poder estar ausente do classpath de testes.

**Resolução:** `./gradlew clean compileTestJava` → `BUILD SUCCESSFUL`, confirmando de uma vez: (a) o arquivo está no caminho certo; (b) todos os imports resolvem, incluindo os de `junit-jupiter-params` (já presente transitivamente via `spring-boot-starter-test`, sem necessidade de alteração no `build.gradle`); (c) a causa real era um **cache de build desatualizado**, que não refletia a criação do arquivo novo — o mesmo tipo de dessincronia já visto com o IntelliJ nas Partes 1 e 3.

**Confirmação da correção:** nova execução de `./gradlew test --tests "dio.budgeting.GeminiTranscriptionModelIT"` rodou as seis execuções parametrizadas (`6 tests completed`), confirmando o arquivo plenamente reconhecido e funcional.

**Lição registrada:** ao criar um arquivo de teste novo e receber "No matching tests found" mesmo com o código correto, `./gradlew clean compileTestJava` é o primeiro passo de diagnóstico — mais confiável do que assumir um erro de sintaxe ou de anotação ausente.

### 11.3. Incidente 2 — 2 de 6 casos falharam por formato de número (comportamento esperado, não bug)

Resultado: `6 tests completed, 2 failed` — `recording-5.mp3` (esperado `"200 reais"`) e `recording-6.mp3` (esperado `"60 reais"`).

**Investigação:** como a asserção que falhou é a última do método (linha 60, `assertThat(response).containsIgnoringCase(expectedKeyword)`), o `System.out.println` da transcrição não chegou a rodar para os casos com falha. A transcrição real foi localizada navegando pelo relatório HTML do Gradle (`build/reports/tests/test/index.html` → classe → método → caso específico → "Failure details"), revelando:

```
Expecting actual:
  "Saí para jantar ontem e a conta ficou duzentos reais por pessoa."
to contain:
  "200 reais"
 (ignoring case)
```

**Causa confirmada:** o Gemini transcreveu o valor **por extenso** ("duzentos reais"), não em algarismos ("200 reais") — uma transcrição correta e fiel ao áudio, apenas em formato diferente do esperado no `@CsvSource`. Comportamento antecipado teoricamente pelo próprio tutorial (seção 6.5), agora confirmado na prática.

**Resolução:** ajustado o valor esperado na linha correspondente do `@CsvSource`, de `"200 reais"` para `"duzentos reais"` (e, presumivelmente, ajuste equivalente para `recording-6.mp3`, seguindo o mesmo padrão).

**Lição registrada:** esta não foi tratada como falha de implementação — é uma demonstração real e documentada da natureza não-determinística da saída de um LLM, e da razão de ser das asserções flexíveis (`.containsIgnoringCase`) usadas em todo teste de IA deste projeto. Passou a ser um dos melhores exemplos concretos para a seção "o que você aprendeu" da entrega do desafio.

### 11.4. Incidente 3 — `curl` do endpoint falhando por diretório de execução incorreto

Ao testar `POST /api/transcribe` manualmente, primeira tentativa retornou:
```
curl: (26) Failed to open/read local data from file/application
```

**Causa identificada:** o `curl` usa um caminho relativo (`src/test/resources/audio/recording-1.mp3`), válido apenas quando executado a partir da pasta `budgeting/`. O terminal, na primeira tentativa, não estava posicionado ali.

**Resolução:** confirmado o diretório de execução (`cd` até `budgeting/`) antes de rodar o `curl` novamente.

**Ruído adicional observado (sem relação com o erro acima):** ao colar o comando de duas linhas (com `\` de continuação), uma execução subsequente produziu uma saída confusa, misturando o erro anterior, a transcrição correta, e um erro de sintaxe do zsh (`unknown file attribute`) — resolvido rodando `clear` e reexecutando o comando em uma única linha.

**Resultado final, confirmado, limpo:**
```bash
curl -X POST "http://localhost:8080/api/transcribe" \
  -F "file=@src/test/resources/audio/recording-1.mp3;type=audio/mpeg"
Fui na farmácia rapidinho e deixei 80 reais em três itens.
```

Transcrição correta e fiel ao áudio original.

### 11.5. ✅ Checkpoint da Parte 6 — fechado

| Item | Status |
| --- | --- |
| Seis áudios de teste gravados e posicionados em `src/test/resources/audio/` | ✅ |
| `GeminiTranscriptionModelIT` — criado, rodado; 2 falhas iniciais diagnosticadas como diferença de formato (não bug), `@CsvSource` corrigido | ✅ |
| `TranscriptionController` (versão inicial) — criado, endpoint `POST /api/transcribe` confirmado funcionando via `curl`, transcrição correta | ✅ |

**Marca de confiança:** não avaliado retroativamente (checkpoint anterior à introdução do campo, 17/08/2026) — nota de contexto, não de valor: esta Parte foi executada na sequência imediata da discussão conceitual sobre o padrão Builder (seção 10, mesmo dia), o que pode ter elevado a confiança real no momento, mas isso não é preenchido aqui como suposição

### 11.6. 📚 Atualizações aplicadas ao tutorial, a partir desta execução

Incorporadas ao `000-Tutorial_Budgeting_Spring_AI_COMPLETO.md`, Parte 6:

- **Seção 6.5:** adicionado um quadro **"Caso real confirmado"**, documentando o exemplo de `recording-5.mp3`/"duzentos reais" como ilustração concreta do comportamento já teoricamente descrito.
- **Seção 6.5:** adicionada uma dica prática sobre o erro "No matching tests found" e o fluxo de diagnóstico (`./gradlew clean compileTestJava`).
- **Seção 6.5:** adicionado um guia passo a passo de como investigar a transcrição real de um caso que falhou (relatório HTML em cascata, ou o arquivo XML alternativo, mais direto).
- **Seção 6.7 (Passo 3):** expandida a nota sobre o `curl`, incluindo o erro `curl: (26)` (causa: diretório de execução incorreto) e a dica sobre colagem de comandos multi-linha em alguns shells.

**Próximo passo planejado:** Parte 7 do tutorial (Vídeo 07) — o segundo e último ponto sem equivalente Gemini no Spring AI (síntese de voz/TTS), usando o SDK nativo do Google GenAI diretamente. Ainda não reescrita no formato de receita explícita.

---

## 📝 LOG DE EXECUÇÃO — DIA 06

**Data:** 22/08/2026
**Contexto:** Execução completa da Parte 7 do tutorial (Vídeo 07) — síntese de voz (TTS), o segundo e último ponto sem equivalente Gemini no Spring AI. Nesta mesma sessão, a Parte 7 do tutorial foi previamente reescrita no formato de receita explícita (3 passos, com o código completo de `GeminiSpeechModelIT.java` — antes ausente do documento — e instruções de teste manual via `curl` para o `TextToSpeechController`, também ausentes anteriormente).

---

## 12. 🔌 Parte 7 do Tutorial — Sintetizando voz (Vídeo 07) — executada e concluída

Objetivo desta etapa: transformar texto em áudio usando o SDK nativo `com.google.genai.Client` (sem `TextToSpeechModel`, que não existe para Gemini), validado por teste com audição manual e exposto via HTTP.

### 12.1. Arquivos criados

**`budgeting/src/test/java/dio/budgeting/GeminiSpeechModelIT.java`** (novo) — teste de integração usando o SDK nativo do Gemini diretamente, gerando um arquivo `.wav` temporário para audição manual, além da asserção automática de tamanho mínimo do áudio (`hasSizeGreaterThan(1024)`).

**`budgeting/src/main/java/dio/budgeting/TextToSpeechService.java`** (novo) — `@Service` encapsulando a configuração do SDK do Gemini (`Client`, `GenerateContentConfig`, voz `"Kore"`) e o processo de conversão de PCM cru para um arquivo WAV válido (`wrapPcmAsWav`).

**`budgeting/src/main/java/dio/budgeting/TextToSpeechController.java`** (novo) — endpoint `POST /api/synthesize`, recebendo um corpo JSON (`{"text": "..."}`) e devolvendo um arquivo `audio/wav` como anexo para download.

### 12.2. Passo 1 — `GeminiSpeechModelIT`: execução e validação

**Comando executado:**
```bash
./gradlew test --tests "dio.budgeting.GeminiSpeechModelIT"
```

**Resultado do console:**
```
Starting a Gradle Daemon (subsequent builds will be faster)
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
BUILD SUCCESSFUL in 28s
5 actionable tasks: 3 executed, 2 up-to-date
```

**Arquivo gerado:** `AUDIO_3994516759941284544.wav` (nome único gerado automaticamente por `Files.createTempFile(...)`, conforme seção 7.4 do tutorial).

**Validação auditiva manual:** confirmada — a frase reproduzida foi *"Sua transação de oitenta reais na farmácia foi registrada com sucesso."*, exatamente o texto enviado ao Gemini dentro do teste.

**Observação sobre a saída do console, mais enxuta que em execuções anteriores:** esta foi a primeira execução do Gradle nesta sessão/reinicialização — daí a linha `"Starting a Gradle Daemon"`. Diferente das execuções anteriores (que mostravam o banner do Spring Boot e os logs `INFO`/`DEBUG` completos no terminal), aqui o console ficou mais resumido; parte da saída detalhada do teste passa a ficar apenas no relatório HTML (`build/reports/tests/test/index.html`), sem prejuízo à validação, já que `BUILD SUCCESSFUL` já confirma que a asserção `hasSizeGreaterThan(1024)` foi satisfeita, e a audição manual complementa a confirmação de que o SDK do Gemini realmente devolveu um áudio compreensível — validação dupla, exatamente como pedido pelo tutorial (automática + auditiva).

### 12.3. Passo 3 — `TextToSpeechController`: execução e validação via `curl`

**Comando executado:**
```bash
curl -X POST "http://localhost:8080/api/synthesize" \
  -H "Content-Type: application/json" \
  -d '{"text": "Sua transação de oitenta reais na farmácia foi registrada com sucesso."}' \
  --output audio.wav
```

**Saída do `curl`:**
```
% Total    % Received % Xferd  Average Speed  Time    Time    Time   Current
                                 Dload  Upload  Total   Spent   Left   Speed
100 251.8k 100 251.8k 100     85  51082     16   00:05   00:05
```

**Arquivo gerado:** `audio.wav`, **251.8 KB**.

**Validação auditiva manual:** confirmada — mesma frase reproduzida corretamente.

**Confirmação cruzada via log de `BudgetingApplication`:** o log da aplicação, anexado à execução, confirma o fluxo HTTP completo sem nenhuma linha `ERROR`: Tomcat inicializado na porta `8080` às `18:16:19`; `DispatcherServlet` inicializado sob demanda, na primeira requisição recebida, às `18:17:22` (o intervalo de mais de um minuto corresponde ao tempo entre subir a aplicação e efetivamente disparar o `curl`, não a nenhuma lentidão do sistema).

**Verificação de sanidade sobre o tamanho do arquivo:** `251.8 KB` é plausível e coerente com os parâmetros de áudio documentados no tutorial (`24000` Hz, mono, `16` bits): `24000 amostras/s × 2 bytes/amostra × ~5s de fala ≈ 240.000 bytes`, muito próximo do valor observado — confirmando que `wrapPcmAsWav(...)` produziu um cabeçalho e dados consistentes com a especificação.

### 12.4. ✅ Checkpoint da Parte 7 — fechado

| Item | Status |
| --- | --- |
| `GeminiSpeechModelIT` — criado, rodado (`BUILD SUCCESSFUL`), validado por asserção automática **e** audição manual | ✅ |
| `TextToSpeechService` — criado, encapsulando SDK nativo do Gemini e conversão PCM → WAV | ✅ |
| `TextToSpeechController` — criado, endpoint `POST /api/synthesize` testado via `curl`, arquivo `audio.wav` gerado e validado por audição manual, log da aplicação sem erros | ✅ |

**Marca de confiança:** *(a preencher por Arthur, conforme critério definido em 17/08/2026 — ver nota no início deste documento)*

### 12.5. 📚 Atualização aplicada ao tutorial, nesta mesma sessão

Incorporada ao `000-Tutorial_Budgeting_Spring_AI_COMPLETO.md`, Parte 7, **antes** da execução registrada acima — corrigindo duas lacunas identificadas: (1) o código completo de `GeminiSpeechModelIT.java`, com todos os `import`s, estava ausente do documento (só um fragmento de 3 linhas era mostrado anteriormente); (2) não havia nenhuma instrução de como testar o `TextToSpeechController` manualmente. A Parte 7 foi reescrita integralmente no formato de receita explícita (3 passos, tabela "Visão geral", `📁 Arquivo`/`✅` em cada seção), incluindo:

- O arquivo completo de `GeminiSpeechModelIT.java`, com uma nota explicando por que ele "duplica" a lógica de `wrapPcmAsWav` do serviço (o teste antecede a extração do serviço, na ordem de construção do tutorial).
- Um passo a passo de validação para o teste, incluindo a etapa de audição manual do arquivo temporário gerado.
- Um `curl -X POST` completo para o `TextToSpeechController`, com `-H "Content-Type: application/json"`, `-d` com o corpo JSON, e `--output audio.wav` (com nota explicando por que este `curl` precisa de `--output`, diferente dos anteriores, por se tratar de uma resposta binária).
- Um passo a passo de validação equivalente para o controller.

**Próximo passo planejado:** Parte 8 do tutorial (Vídeo 08) — o domínio de negócio do projeto (`Transaction`, `Category`, `TransactionRepository`) e o primeiro caso de uso real (`PersistTransactionUseCase`), onde o padrão de Tool Calling (já validado na Parte 5) passa a ser aplicado pela primeira vez a uma operação de negócio de verdade, não mais a um exemplo didático.

---

## 📝 LOG DE EXECUÇÃO — DIA 07

**Data:** 23/08/2026
**Contexto:** Execução completa da Parte 8 do tutorial (Vídeo 08) — o domínio de negócio do projeto (`Transaction`, `Category`, `TransactionId`, `TransactionRepository`) e o primeiro caso de uso real (`PersistTransactionUseCase`), com Tool Calling aplicado pela primeira vez a uma operação de negócio de verdade. Nesta mesma sessão, a Parte 8 do tutorial foi previamente reescrita no formato de receita explícita (11 passos, com `📁 Arquivo`/`✅` em cada seção, correção de uma inconsistência de ordem entre a edição do `build.gradle` e a criação de `Transaction.java`, e uma nova seção explicando por que esta Parte não tem teste de integração).

---

## 13. 🔌 Parte 8 do Tutorial — O domínio do negócio (Vídeo 08) — executada e concluída

Objetivo desta etapa: dar ao projeto uma representação própria do domínio (`Transaction`, `Category`), organizada segundo os princípios de Domain-Driven Design e Clean Architecture, e o primeiro caso de uso real (`PersistTransactionUseCase`), já registrado como *tool* de IA desde sua criação.

### 13.1. Arquivos criados/editados

**`budgeting/src/main/java/dio/budgeting/domain/TransactionId.java`** (novo) — identificador fortemente tipado, `record` envolvendo um `UUID`, com construtor auxiliar sem argumentos para gerar um novo identificador aleatório.

**`budgeting/src/main/java/dio/budgeting/domain/Category.java`** (novo) — `enum` com as três categorias suportadas (`GROCERIES`, `PHARMA`, `AUTO`).

**`budgeting/build.gradle`** (editado) — adicionado o plugin `io.freefair.lombok`, versão `9.2.0`, ao bloco `plugins { }`.

**`budgeting/src/main/java/dio/budgeting/domain/Transaction.java`** (novo) — a entidade de domínio, usando `@Getter`/`@AllArgsConstructor` do Lombok, com dois construtores (um gerado, um manual, gerando o `TransactionId` internamente para transações novas).

**`budgeting/src/main/java/dio/budgeting/domain/TransactionRepository.java`** (novo) — a interface de domínio (o "contrato" de persistência), com `save(...)` e `findAllByCategory(...)`, sem nenhuma implementação nesta Parte.

**`budgeting/src/main/java/dio/budgeting/application/input/PersistTransactionInput.java`** (novo) — DTO de entrada do caso de uso, com `@ToolParam` em `description` e `amount` (não em `category`, inconsistência já documentada no tutorial como candidato de melhoria).

**`budgeting/src/main/java/dio/budgeting/application/output/TransactionOutput.java`** (novo) — DTO de saída, com o mapeamento `from(Transaction)` incluindo arredondamento via `BigDecimal`/`RoundingMode.HALF_UP`.

**`budgeting/src/main/java/dio/budgeting/application/PersistTransactionUseCase.java`** (novo) — o primeiro caso de uso real, já anotado com `@Tool(name = "persistTransaction", ...)` desde sua criação, injetando `TransactionRepository` (a interface, ainda sem implementação concreta nesta Parte).

**`budgeting/src/main/java/dio/budgeting/infrastructure/`** (novo, pacote vazio) — marcador para a camada de persistência real, implementada na Parte 9.

### 13.2. Execução e validação

Diferente de todas as Partes anteriores (3 a 7), esta Parte não tem nenhum teste de integração (`...IT.java`) — `TransactionRepository` ainda é só uma interface, sem implementação concreta para exercitar. A verificação, conforme orientado na seção 8.10 do tutorial (reescrita nesta mesma sessão), é apenas de **compilação**.

**Comando executado:**
```bash
./gradlew clean compileJava
```

**Resultado do console:**
```
Starting a Gradle Daemon (subsequent builds will be faster)
BUILD SUCCESSFUL in 12s
3 actionable tasks: 3 executed
```

**Análise do resultado:**

- **`BUILD SUCCESSFUL`, sem nenhuma linha de erro** — confirma que todo o código novo desta Parte (`TransactionId`, `Category`, `Transaction`, `TransactionRepository`, `PersistTransactionInput`, `TransactionOutput`, `PersistTransactionUseCase`) compilou corretamente: sintaxe válida, todos os tipos referenciados existentes e corretamente importados, e — ponto de atenção específico desta Parte — os métodos gerados pelo Lombok (`@Getter`, `@AllArgsConstructor` em `Transaction`) foram corretamente reconhecidos pelo compilador, confirmando que o plugin `io.freefair.lombok` foi adicionado e sincronizado com sucesso **antes** da criação de `Transaction.java`, na ordem correta indicada pelo tutorial.
- **`"Starting a Gradle Daemon"`** — mesmo comportamento já observado no DIA 06 (Parte 7): primeira execução do Gradle nesta sessão/reinicialização, iniciando um novo processo *daemon* residente em memória.
- **`3 actionable tasks: 3 executed`** — número de tarefas pequeno e coerente com o escopo do comando (`compileJava` foca apenas na compilação do código de produção, sem processar recursos de teste nem rodar testes) — diferente do `./gradlew test`, que envolveria mais tarefas (compilação de testes, execução, etc.).
- **Ausência de qualquer menção a `cannot find symbol` ou `constructor ... cannot be applied`** — os dois erros mais prováveis, antecipados na seção 8.10 do tutorial, caso o Lombok não tivesse sido reconhecido corretamente. A ausência deles confirma que a ordem de execução (editar `build.gradle` antes de criar `Transaction.java`) foi seguida corretamente.

### 13.3. ✅ Checkpoint da Parte 8 — fechado

| Item | Status |
| --- | --- |
| `domain` — `TransactionId`, `Category`, `Transaction`, `TransactionRepository` criados | ✅ |
| `build.gradle` — plugin Lombok adicionado, na ordem correta (antes de `Transaction.java`) | ✅ |
| `application` — `PersistTransactionInput`, `TransactionOutput`, `PersistTransactionUseCase` criados, `@Tool` já registrado | ✅ |
| `infrastructure` — pacote criado, vazio (aguardando Parte 9) | ✅ |
| Verificação de compilação (`./gradlew clean compileJava`) — `BUILD SUCCESSFUL`, sem erros | ✅ |

**Marca de confiança:** *(a preencher por Arthur, conforme critério definido em 17/08/2026 — ver nota no início deste documento)*

### 13.4. 📚 Atualização aplicada ao tutorial, nesta mesma sessão

Incorporada ao `000-Tutorial_Budgeting_Spring_AI_COMPLETO.md`, Parte 8, **antes** da execução registrada acima:

- Reescrita integral no formato de receita explícita (tabela "Visão geral" com os 11 passos, `📁 Arquivo`/`✅` em cada uma das seções 8.2 a 8.9, antes ausentes — o documento só listava os arquivos na caixa do topo, sem repetir a instrução em cada seção específica).
- **Correção de uma inconsistência de ordem identificada durante a reescrita:** a versão anterior instruía editar `build.gradle` (adicionar Lombok) como último passo da lista, mas o próprio texto já avisava "faça isso antes de escrever `Transaction.java`" — uma contradição na sequência numerada. A edição do `build.gradle` foi reposicionada para o Passo 4, imediatamente antes da criação de `Transaction.java` (Passo 5), eliminando a inconsistência.
- **Nova seção 8.10**, explicando por que esta Parte não tem teste de integração (a ausência de uma implementação concreta de `TransactionRepository` até a Parte 9) e orientando a verificação por compilação (`./gradlew clean compileJava`), incluindo os erros mais prováveis caso o Lombok não tenha sido corretamente configurado.
- Checkpoint renumerado para 8.11, com tabela listando cada arquivo e sua ação, no mesmo padrão das Partes 4 a 7.

**Próximo passo planejado:** Parte 9 do tutorial (Vídeo 09) — implementação real da persistência (`JpaTransactionRepository`, `TransactionEntity`, Docker Compose com MySQL, Spring Data JPA), finalmente implementando a interface `TransactionRepository` criada nesta Parte e permitindo, pela primeira vez, testar `PersistTransactionUseCase` de ponta a ponta.

---
