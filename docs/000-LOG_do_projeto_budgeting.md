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
