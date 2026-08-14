# Tutorial Final — Desenvolvendo sua API Inteligente com Reconhecimento de Fala e Spring Boot

**Projeto `budgeting` — do zero até o Vídeo 11, na versão adaptada para Google Gemini**

- Curso: NTT Data — Jornada Tech (DIO) · Módulo 4 — Curso 5: "Desenvolvendo sua API Inteligente com Reconhecimento de Fala e Spring Boot"
- Instrutor original do curso: Thiago Poiani (Principal Engineer at Skip) — a aula usa **OpenAI** como provedor de IA
- Autor deste projeto: você — adaptando, ao longo de todo o curso, o mesmo roteiro para usar a **API do Google Gemini** em vez da OpenAI
- Documento de referência de estudo — nível **iniciante em Java**, explicado "pegando na mão", passo a passo — escrito do zero a partir da leitura completa do código-fonte final (`budgeting_ate_o_video11.zip`) e do README atualizado (Vídeos 01 a 12)

---

## Sobre este tutorial (leia antes de começar)

Este documento é uma reescrita completa e definitiva, no nível de detalhe mais granular possível — o mesmo espírito "pegando na mão" dos tutoriais que você foi escrevendo comigo durante o curso (Vídeos 01 a 11), só que agora com a vantagem de eu já ter lido o projeto inteiro, do início ao fim, no seu estado real e final.

**O que significa "granular" aqui, na prática:**

- Cada bloco de código é quebrado **linha por linha, ou instrução por instrução** — não em parágrafos que resumem várias linhas de uma vez.
- Todo conceito novo (uma anotação, uma classe, um padrão de projeto, um termo técnico) ganha uma **caixa de explicação "do zero"**, escrita como se você nunca tivesse visto aquilo antes — mesmo que o conceito já tenha aparecido de forma correlata em outro contexto.
- Sempre que existe mais de uma forma de resolver o mesmo problema (por exemplo, duas formas de registrar um *bean* no Spring), o tutorial explica **as duas**, e por que a que está no código final foi a escolhida.
- Cada Parte termina revisando o que foi visto, e o início da Parte seguinte retoma o fio antes de avançar — para você nunca perder o contexto entre uma sessão de estudo e outra.
- Sempre que a narrativa do curso (documentada no README, que usa OpenAI) diverge do código que você efetivamente escreveu (que usa Gemini), este tutorial **segue o código real**, mas explica a divergência, para você entender exatamente o que mudou e por quê.

**Como este tutorial está organizado.** Cada vídeo do curso vira uma "Parte". Dentro de cada parte:

1. **Recapitulando** — uma ou duas frases retomando onde paramos (a partir da Parte 4 em diante).
2. **Objetivo** — o que aquele vídeo entrega, em uma frase.
3. **Conceitos novos** — explicados do zero, em caixas destacadas, antes de aparecerem em código.
4. **Construção passo a passo** — o código evolui em pequenos incrementos, cada linha explicada individualmente, do mesmo jeito que uma pessoa desenvolvendo o testaria: primeiro algo simples e verificável (um teste de integração, por exemplo), depois a funcionalidade "de verdade" (um endpoint).
5. **Checkpoint** — o estado final dos arquivos daquela etapa, **conferido diretamente contra o `.zip` enviado**, não apenas contra a narrativa do curso.

No final, você encontra um **glossário cumulativo em formato de tabela** (para consulta rápida), um **mapa geral da arquitetura**, um **guia de execução do projeto do zero** e os **próximos passos** para fechar a entrega do desafio (Vídeo 12 em diante).

> **Nota sobre a adaptação Gemini — leia com atenção, porque ela se repete em várias partes.** O curso original usa a OpenAI (`gpt-4o-mini` para chat, Whisper para transcrição, a Speech API da OpenAI para voz). O Spring AI dá suporte oficial a vários provedores através de *starters* — e o Google Gemini é um deles, através do artefato `spring-ai-starter-model-google-genai`. Isso significa que boa parte da troca de provedor é só trocar de dependência e de nome de classe (`OpenAiChatModel` → `GoogleGenAiChatModel`, por exemplo). Mas existem dois pontos em que o Spring AI **não** tem uma implementação pronta para o Gemini: a **Transcription API** (não existe `TranscriptionModel` para Gemini no Spring AI) e a **Text-to-Speech API** (não existe `TextToSpeechModel` para Gemini). Nesses dois pontos, em vez de usar as interfaces genéricas do Spring AI, o projeto teve que:
> - para transcrição: usar o **`ChatModel` multimodal** do Gemini (o mesmo `GoogleGenAiChatModel` do chat), enviando o áudio como uma mensagem com mídia anexada;
> - para síntese de voz: chamar diretamente o **SDK Java nativo do Google GenAI** (`com.google.genai.Client`), por fora do Spring AI, e converter manualmente o áudio bruto recebido (PCM) em um arquivo `.wav` reproduzível.
>
> Esses dois pontos — a razão da divergência e a solução escolhida — são explicados em detalhe nas Partes 6 e 7, com o mesmo cuidado "do zero" do resto do tutorial.

---

## Parte 0 — Antes de tocar em código: os conceitos que sustentam o projeto inteiro

Vale muito a pena não pular esta parte, mesmo com pressa de "ir para o código" — tudo o que vem depois é uma variação destes poucos conceitos, e voltar aqui sempre que uma dúvida de fundamento aparecer é mais rápido do que tentar entender tudo de uma vez lá na frente.

### 0.1. O que o projeto faz, em uma frase

O assistente de *budgeting* (que, em inglês, significa "orçamento" — o ato de planejar e controlar gastos) recebe um **áudio** de alguém falando um gasto ("gastei 50 reais no mercado"), **transcreve** esse áudio em texto, usa uma **IA (LLM)** para entender a intenção e **executar código Java de verdade** (salvar a transação no banco, ou consultar transações já salvas), e devolve uma **resposta em áudio** confirmando o que foi feito.

### 0.2. O que é um LLM, explicado do zero

Um **LLM** (*Large Language Model*, ou "Grande Modelo de Linguagem") é um tipo de modelo de inteligência artificial treinado, a partir de uma quantidade enorme de texto, para prever qual é a próxima palavra (mais precisamente, o próximo *token* — um pedaço de palavra, às vezes uma palavra inteira, às vezes só uma sílaba) mais provável em uma sequência. Na prática, isso permite que o modelo "converse": você fornece um texto de entrada (o *prompt*), e o modelo gera, palavra por palavra, uma continuação coerente com esse texto — o que, para quem está do outro lado, parece uma resposta inteligente.

Empresas como OpenAI, Google (dona do Gemini) e Anthropic mantêm e treinam esses modelos, e os disponibilizam como **APIs HTTP** — ou seja, você faz uma requisição pela internet, informando o texto de entrada, e recebe de volta a resposta gerada. Essas chamadas normalmente são cobradas por **token** processado (tanto os tokens da sua entrada quanto os da resposta gerada).

### 0.3. O papel do Spring AI, explicado do zero

Sem uma biblioteca intermediária, cada provedor de IA (OpenAI, Gemini, Anthropic, DeepSeek, etc.) exigiria que você aprendesse uma forma diferente de: montar a requisição HTTP, autenticar com a chave de API correta, interpretar o formato específico da resposta, e tratar os erros específicos daquele provedor.

O **Spring AI** é uma biblioteca do ecossistema Spring (o mesmo "mundo" do Spring Boot) que resolve esse problema **padronizando** o acesso a esses modelos. Ela define **interfaces comuns** — como `ChatModel` (que veremos na Parte 3) — que representam "o que é conversar com um LLM", independentemente de qual provedor está por trás. Cada provedor então tem um **starter**: uma dependência Gradle/Maven que, ao ser adicionada ao projeto, já implementa essas interfaces por baixo dos panos, conectando-se à API real daquele provedor específico. Trocar de provedor, na maioria dos casos, é trocar a dependência (o *starter*) e algumas propriedades de configuração — o código que **usa** a interface (`ChatModel`, por exemplo) muda pouco ou nada.

> **Por que isso importa para você, especificamente?** Porque é exatamente essa arquitetura que tornou possível você adaptar um curso inteiro pensado para OpenAI e fazê-lo funcionar com Gemini — na maior parte dos casos, "só" trocando o *starter* e ajustando nomes de classe e de propriedades. Os dois pontos em que isso **não** foi tão simples (Transcrição e Text-to-Speech, Partes 6 e 7) são justamente os pontos em que o Spring AI ainda não tinha uma interface pronta cobrindo o Gemini nessas funcionalidades específicas.

### 0.4. Os três pilares do pipeline: STT, Tool Calling, TTS

- **STT — Speech-to-Text** (fala para texto): o processo de transformar a onda sonora captada em um arquivo de áudio em uma *string* de texto que o resto do sistema consegue processar. É o primeiro passo do pipeline: sem transformar o áudio em texto, nenhuma IA de linguagem consegue "entender" o que a pessoa disse.
- **Tool Calling** (chamada de ferramentas, também chamado de *Function Calling*): um recurso em que o LLM, além de gerar texto, pode decidir **solicitar a execução de um método Java real** quando percebe, pelo contexto da conversa, que a intenção do usuário exige uma ação concreta no sistema (salvar algo em um banco de dados, consultar algo já salvo). É importante entender desde já: **o LLM não executa esse código sozinho**. Ele apenas decide *qual* método deveria ser chamado, e *com quais argumentos* — e é a aplicação Java (através do Spring AI) quem efetivamente invoca esse método e devolve o resultado de volta para o modelo continuar a conversa. Este conceito é explicado com muito mais detalhe, e com exemplos práticos, na Parte 5.
- **TTS — Text-to-Speech** (texto para fala): o processo inverso do STT — transforma a resposta final do assistente (que, internamente, é sempre texto) de volta em um áudio reproduzível, para que a interação com o usuário seja falada, humanizando a experiência.

### 0.5. Chave de API e variável de ambiente, explicado do zero

Toda chamada a um provedor de IA exige uma **chave de API** (*API key*) — uma sequência de caracteres secreta e única, associada à sua conta, que o provedor usa para: (1) confirmar que quem está fazendo a chamada tem permissão para usar o serviço; (2) contabilizar o uso, para fins de cobrança; (3) aplicar limites de uso (*rate limits*), evitando abuso.

Essa chave **nunca** deve ser escrita diretamente dentro de um arquivo que será versionado no Git (como o `application.properties`), porque, uma vez commitado, esse valor fica gravado permanentemente no histórico do repositório — mesmo que você o apague depois, ele continua acessível em commits antigos. Se o repositório for público (como o seu, que vai para o GitHub como portfólio), qualquer pessoa poderia copiar essa chave e usá-la por sua conta, gerando custos e riscos de segurança para você.

A prática padrão da indústria para evitar isso é armazenar a chave fora do código-fonte, em uma **variável de ambiente** — um valor definido no sistema operacional (ou na configuração de execução de uma IDE), acessível a programas em execução, mas que não fica gravado em nenhum arquivo do projeto. O Spring tem uma sintaxe própria para "ler o valor de uma variável de ambiente e usá-lo como valor de uma propriedade": `${NOME_DA_VARIAVEL}`. Você já viu isso em ação no `application.properties` do projeto: `spring.ai.google.genai.api-key=${GEMINI_API_KEY}` — o Spring, ao subir a aplicação, procura uma variável de ambiente chamada `GEMINI_API_KEY` e substitui esse valor no lugar de `${GEMINI_API_KEY}`.

Com todos esses conceitos de fundamento mapeados, vamos construir o projeto do zero, uma linha de cada vez.


---

## Parte 1 e 2 — Criando o projeto e conectando ao provedor de IA (Vídeos 01 e 02)

### Objetivo

Sair de "nenhum projeto" para uma aplicação Spring Boot mínima, capaz de subir sem erros, com a integração ao provedor de IA já configurada (chave de API lida do ambiente) — mesmo que ainda não exista nenhum código que efetivamente *use* essa integração.

### Visão geral desta etapa — os 4 passos, em ordem

| Passo | Ação | Arquivo |
|---|---|---|
| 1 | Gerar o projeto pelo Spring Initializr | (já feito por você, no IntelliJ) |
| 2 | Conferir `settings.gradle` | `budgeting/settings.gradle` |
| 3 | Editar `build.gradle` | `budgeting/build.gradle` |
| 4 | Editar `application.properties` | `budgeting/src/main/resources/application.properties` |

Cada passo abaixo segue sempre o mesmo formato: **o que fazer → o código a inserir → como o arquivo fica depois → por que cada linha está ali**.

### 1.1. Criando o esqueleto do projeto Spring Boot (Passo 1)

O ponto de partida de praticamente qualquer projeto Spring Boot é o **Spring Initializr** — um gerador de projetos, acessível tanto pelo site [start.spring.io](https://start.spring.io) quanto embutido diretamente em IDEs como o IntelliJ (você já usou essa segunda forma, com os campos preenchidos como registrado no seu LOG de projeto). Ele monta, a partir de um formulário, um projeto vazio, mas já configurado com a estrutura básica que o Spring Boot espera.

As escolhas feitas neste projeto, campo por campo:

- **`Name` / nome do projeto:** `budgeting`
- **`Language` / linguagem:** Java
- **`Type` / ferramenta de build:** Gradle, na variante Groovy DSL (arquivo `build.gradle`, escrito na linguagem Groovy — diferente da variante Kotlin DSL, que usaria `build.gradle.kts`)
- **`Group`:** `dio` — o identificador da "organização" do projeto, usado como prefixo de pacote
- **`Artifact`:** `budgeting` — o nome do artefato final gerado (o `.jar`)
- **`Package name`:** `dio.budgeting` — o pacote Java raiz, dentro do qual todas as classes do projeto vão morar
- **`Java`:** versão 21 — uma versão **LTS** (*Long Term Support*, "suporte de longo prazo"), o que significa que ela recebe atualizações de segurança por mais tempo do que versões intermediárias, sendo uma escolha comum para projetos que vão durar

> **O que é o Gradle, explicado do zero?** Gradle é uma **ferramenta de build** — um programa responsável por automatizar tarefas como: baixar as bibliotecas externas que o projeto precisa (chamadas de *dependências*), compilar o código Java para *bytecode* (o formato que a JVM entende), rodar os testes automatizados, e empacotar tudo em um arquivo `.jar` executável. Ela é configurada através de um arquivo de script — no nosso caso, `build.gradle` — onde declaramos quais dependências o projeto usa e como ele deve ser construído.

O resultado desse passo, **que você já tem pronto**, é este esqueleto de projeto Gradle, com esta estrutura de pastas e arquivos:

```
budgeting-spring-ai-gemini/          ← raiz do seu repositório Git
└── budgeting/                       ← o projeto Gradle em si — é aqui que tudo abaixo vive
    ├── build.gradle
    ├── settings.gradle
    ├── gradlew, gradlew.bat        (o "Gradle Wrapper")
    └── src/
        ├── main/java/dio/budgeting/BudgetingApplication.java
        ├── main/resources/application.properties
        └── test/java/dio/budgeting/BudgetingApplicationTests.java
```

> **O que é o Gradle Wrapper (`gradlew`/`gradlew.bat`), explicado do zero?** São dois pequenos scripts (um para Linux/Mac, `gradlew`; outro para Windows, `gradlew.bat`) que permitem rodar comandos do Gradle **sem precisar ter o Gradle instalado manualmente** na máquina. Na primeira execução, eles mesmos baixam a versão correta do Gradle (definida em `gradle/wrapper/gradle-wrapper.properties`) e a usam a partir daí.

**Você não precisa criar nada neste passo** — só confirmar que sua estrutura de pastas bate com a mostrada acima. Se bater, siga para o Passo 2.

### 1.2. Passo 2 — Conferir `settings.gradle`

**📁 Arquivo:** `budgeting/settings.gradle` (já existe, gerado pelo Initializr — você só vai **conferir**, não editar)

Abra o arquivo e confirme que ele contém exatamente esta única linha:

```groovy
rootProject.name = 'budgeting'
```

**✅ Se o conteúdo bater, não mexa em nada — siga para o Passo 3.**

Explicando esta linha, mesmo sem precisar editá-la:

- **`rootProject`** — em um projeto Gradle, mesmo que ele seja composto de um único módulo (como é o nosso caso — não há "subprojetos"), esse único módulo é tratado internamente como o "projeto raiz" (*root project*). O `settings.gradle` é o arquivo onde esse projeto raiz é declarado e nomeado — é o primeiro arquivo que o Gradle lê ao processar o build.
- **`.name = 'budgeting'`** — atribui o nome `budgeting` a esse projeto raiz. Esse nome aparece, por exemplo, nos logs do Gradle e é usado, por padrão, como base para o nome do artefato gerado (o `.jar`).

### 1.3. `BudgetingApplication.java`: conferindo o ponto de entrada da aplicação

**📁 Arquivo:** `budgeting/src/main/java/dio/budgeting/BudgetingApplication.java` (já existe, gerado pelo Initializr — apenas **confira**, não precisa editar)

```java
package dio.budgeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BudgetingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BudgetingApplication.class, args);
    }

}
```

**✅ Se o conteúdo bater com o de cima, não mexa em nada.**

Explicando, linha por linha:

- **`package dio.budgeting;`** — a primeira linha de qualquer arquivo Java (fora comentários) declara a qual **pacote** aquela classe pertence. Pacotes são a forma que o Java usa para organizar classes em uma estrutura hierárquica. Em Java, o caminho de pastas de um arquivo `.java` **precisa** corresponder ao nome do seu pacote — por isso este arquivo vive em `.../java/dio/budgeting/`.
- **`import org.springframework.boot.SpringApplication;`** e **`import org.springframework.boot.autoconfigure.SpringBootApplication;`** — trazem, para o escopo deste arquivo, classes/anotações definidas em outros pacotes, permitindo usá-las pelo nome curto.
- **`@SpringBootApplication`** — uma **anotação** que é, na verdade, um "combo" de três outras anotações, aplicadas de uma vez:
  - **`@Configuration`** — marca a classe como uma fonte válida de definições de *beans* (objetos gerenciados pelo Spring).
  - **`@EnableAutoConfiguration`** — ativa o mecanismo de **auto-configuração**: ao subir, o Spring Boot examina quais bibliotecas estão no *classpath* e configura automaticamente componentes correspondentes.
  - **`@ComponentScan`** — instrui o Spring a **varrer** o pacote `dio.budgeting` e seus subpacotes, procurando por classes marcadas com anotações de "componente" e registrá-las automaticamente.
- **`public class BudgetingApplication {`** — em Java, o nome do arquivo precisa coincidir exatamente com o nome da única classe `public` que ele contém.
- **`public static void main(String[] args) {`** — o **ponto de entrada** padrão que a JVM procura ao iniciar qualquer aplicação Java.
- **`SpringApplication.run(BudgetingApplication.class, args);`** — inicia toda a aplicação Spring Boot: cria o contexto, aciona a auto-configuração, cria e injeta os *beans*, e (quando houver dependência web, o que ainda não é o caso) inicia um servidor HTTP.

### 1.4. `BudgetingApplicationTests.java`: conferindo o primeiro teste

**📁 Arquivo:** `budgeting/src/test/java/dio/budgeting/BudgetingApplicationTests.java` (já existe, gerado pelo Initializr — apenas **confira**)

```java
package dio.budgeting;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BudgetingApplicationTests {

    @Test
    void contextLoads() {
    }

}
```

**✅ Se bateu, siga em frente.**

- **`@SpringBootTest`** — sobe o **contexto completo** da aplicação Spring antes de rodar os testes daquela classe.
- **`@Test`** — anotação do **JUnit 5** que marca um método como um caso de teste.
- **`void contextLoads() { }`** — um método **vazio**, de propósito: o próprio ato de ele rodar sem lançar exceção já é a verificação — se algum *bean* estivesse mal configurado, o `@SpringBootTest` falharia **antes** de o corpo vazio rodar.

### 1.5. Passo 3 — Editar `build.gradle`: adicionando o BOM e o starter do Gemini

**📁 Arquivo:** `budgeting/build.gradle` (editar — arquivo já existe, gerado pelo Initializr, mas ainda **sem** nenhuma dependência de IA)

**O que fazer:** abra o arquivo e **substitua todo o conteúdo** pelo texto abaixo (ele já incorpora as duas dependências novas que vamos explicar em seguida):

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

    // Starter da OpenAI, mantido comentado como referência ao curso original
    //  implementation 'org.springframework.ai:spring-ai-starter-model-openai'

    // Starter do Google Gemini — usado neste projeto
    implementation 'org.springframework.ai:spring-ai-starter-model-google-genai'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

**✅ Depois desta edição, `budgeting/build.gradle` deve estar exatamente assim — igual ao bloco acima.** (Este é um arquivo pequeno o suficiente para ser mostrado por completo de uma vez, em vez de como um "trecho a inserir".)

Agora, explicando cada parte nova (o `plugins`, `group`, `java`, `repositories` e a estrutura geral já vieram prontos do Initializr; o que muda de verdade é dentro de `dependencies { }`):

- **`implementation platform("org.springframework.ai:spring-ai-bom:2.0.0")`**

  > **O que é um BOM (*Bill of Materials*), explicado do zero?** Imagine que seu projeto vai usar vários módulos diferentes de uma mesma "família" de bibliotecas — no nosso caso, vários módulos do Spring AI. Cada um tem sua própria versão, e essas versões precisam ser **compatíveis entre si**. Um BOM é, essencialmente, um "**catálogo de versões compatíveis**": ao importá-lo com `platform(...)`, você não precisa mais escrever a versão em cada dependência individual do Spring AI — o Gradle consulta automaticamente o BOM para descobrir qual versão usar de cada uma.
  - **`implementation`** — a palavra-chave do Gradle que declara uma dependência necessária tanto para **compilar** quanto para **rodar** a aplicação.
  - **`platform(...)`** — informa ao Gradle: "isto não é uma dependência de código comum, é um catálogo de versões".
  - **`"org.springframework.ai:spring-ai-bom:2.0.0"`** — a coordenada completa, no formato `grupo:artefato:versão`. `2.0.0` é a versão **estável** (não é mais uma versão `-M4` de milestone, como versões anteriores do Spring AI 2.x exigiam) da geração 2.0, compatível com o Spring Boot 4.x usado aqui.

- **`//  implementation 'org.springframework.ai:spring-ai-starter-model-openai'`**

  - **`//`** — em Groovy (a linguagem do `build.gradle`), duas barras iniciam um **comentário de linha**: tudo depois delas é ignorado. Esta linha específica **não tem efeito nenhum** na build — é só um registro histórico de que o curso original usa a OpenAI aqui, mantido como referência.

- **`implementation 'org.springframework.ai:spring-ai-starter-model-google-genai'`**

  > **O que é um *starter*, explicado do zero?** Um *starter* é um tipo especial de dependência do Spring Boot que reúne, em um único artefato: a biblioteca principal (aqui, o código que sabe se comunicar com a API do Gemini) **e** a configuração de auto-configuração correspondente (o código que, ao detectar esse *starter*, sabe automaticamente como criar e configurar os *beans* relacionados, lendo as propriedades do `application.properties`).
  - Este *starter* específico é o que, mais adiante (Parte 3), vai disponibilizar automaticamente o *bean* `GoogleGenAiChatModel`.

- **`testImplementation` / `testRuntimeOnly`** — variações de `implementation` restritas ao contexto de testes: `testImplementation` (necessária para compilar e rodar testes) e `testRuntimeOnly` (necessária só durante a execução dos testes) — já vieram assim do Initializr, sem alteração nossa.

> **💡 Dica prática (IntelliJ), guarde para usar já no próximo passo:** depois de editar `build.gradle` diretamente no arquivo, é comum o painel lateral **Gradle** do IntelliJ não refletir a mudança imediatamente, mesmo clicando no ícone de refresh. Se, ao rodar a aplicação, o `-classpath` impresso no console não contiver os `.jar`s da dependência recém-adicionada, force a resincronização: (1) pelo terminal, dentro de `budgeting/`, rode `./gradlew --refresh-dependencies build -x test`; (2) volte ao IntelliJ e sincronize o painel Gradle novamente.

### 1.6. Passo 4 — Editar `application.properties`: configurando a chave de API

**📁 Arquivo:** `budgeting/src/main/resources/application.properties` (editar)

**O que fazer:** substitua todo o conteúdo pelo texto abaixo:

```properties
spring.application.name=budgeting
#spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.google.genai.api-key=${GEMINI_API_KEY}
```

**✅ Depois desta edição, `application.properties` deve estar exatamente assim.**

Explicando cada linha:

- **`spring.application.name=budgeting`** — uma propriedade padrão do Spring Boot que define um **nome lógico** para a aplicação, usado em logs e ferramentas de observabilidade.
- **`#spring.ai.openai.api-key=${OPENAI_API_KEY}`** — em arquivos `.properties`, o caractere `#` no início de uma linha marca um **comentário**. Esta linha não tem efeito — é a referência histórica à configuração que seria usada com a OpenAI.
- **`spring.ai.google.genai.api-key=${GEMINI_API_KEY}`** — a propriedade **real e ativa**. `spring.ai.google.genai` é o **prefixo de propriedade** definido pelo próprio starter do Gemini (confirmado na documentação oficial do Spring AI) — é assim que a auto-configuração sabe que este valor deve ser usado como chave de autenticação. `${GEMINI_API_KEY}` é a sintaxe de **interpolação de variável de ambiente**: o Spring, ao subir, substitui esse trecho pelo valor lido da variável de ambiente `GEMINI_API_KEY` do sistema operacional.

  > **O que é uma variável de ambiente, e por que não escrever a chave direto aqui?** Uma variável de ambiente é um valor definido no sistema operacional (ou na configuração de execução da IDE), acessível a programas em execução, mas que **não fica gravado em nenhum arquivo do projeto**. A chave de API nunca deve ser escrita diretamente em um arquivo versionado no Git — se você commitasse a chave real aqui, ela ficaria exposta permanentemente no histórico do seu repositório (mesmo que a apagasse depois), o que é especialmente arriscado em um repositório público como o seu, usado como portfólio.

**Antes de rodar, configure a variável de ambiente** (fora do código, na sua máquina ou na sua IDE):

```bash
export GEMINI_API_KEY="sua-chave-aqui"
```

Ou, no IntelliJ: **Run → Edit Configurations...** → selecione `BudgetingApplication` → campo **Environment variables** → adicione `GEMINI_API_KEY=sua-chave-aqui`.

### 1.7. Verificação final: rodando `BudgetingApplication`

Com os 4 passos concluídos, rode `BudgetingApplication` (botão *Run* da IDE, ou `./gradlew bootRun` no terminal). O log deve terminar assim, sem erros:

```
:: Spring Boot ::

INFO ... dio.budgeting.BudgetingApplication : Starting BudgetingApplication
INFO ... dio.budgeting.BudgetingApplication : No active profile set, falling back to 1 default profile: "default"
INFO ... dio.budgeting.BudgetingApplication : Started BudgetingApplication in X.XXX seconds
```

E, no `-classpath` impresso pelo IntelliJ, confira que aparecem `.jar`s como `spring-ai-starter-model-google-genai-2.0.0.jar`, `spring-ai-google-genai-2.0.0.jar` e `google-genai-1.xx.0.jar` — a confirmação de que a dependência foi corretamente resolvida (se não aparecerem, veja a dica prática da seção 1.5).

### 1.8. Checkpoint da Parte 1/2 — o que você deve ter, ao final

| Arquivo | Ação nesta Parte | Estado final |
|---|---|---|
| `budgeting/settings.gradle` | Conferido, sem alteração | `rootProject.name = 'budgeting'` |
| `budgeting/build.gradle` | **Editado** (Passo 3) | plugins Spring Boot + BOM Spring AI (`2.0.0`) + starter Gemini (OpenAI comentado) |
| `budgeting/src/main/java/dio/budgeting/BudgetingApplication.java` | Conferido, sem alteração | classe de entrada padrão |
| `budgeting/src/main/resources/application.properties` | **Editado** (Passo 4) | `spring.application.name` + `spring.ai.google.genai.api-key` |
| `budgeting/src/test/java/dio/budgeting/BudgetingApplicationTests.java` | Conferido, sem alteração | teste de sanidade `contextLoads` |

**Recapitulando:** temos um projeto Spring Boot mínimo, capaz de subir sozinho, com a *dependência* de IA já resolvida e a *chave de autenticação* já configurada — mas ainda **nenhuma linha de código nossa** usa efetivamente o Gemini. É exatamente isso que a Parte 3 resolve.


---

## Parte 3 — ChatModel: a primeira chamada a uma LLM (Vídeo 03)

### Recapitulando

Na Parte 1/2, deixamos o projeto pronto para se conectar ao Gemini (dependência resolvida, chave configurada), mas sem nenhum código que efetivamente disparasse uma chamada. Agora vamos escrever essa primeira chamada.

### Objetivo

Entender a API de mais baixo nível do Spring AI para conversar com um modelo (`ChatModel`), validar a integração através de um **teste de integração** (antes de qualquer coisa visível ao usuário), e só depois expor isso como um endpoint HTTP simples.

### Visão geral desta etapa — os 4 passos, em ordem

| Passo | Ação | Arquivo |
|---|---|---|
| 1 | Editar `build.gradle` — adicionar suporte web | `budgeting/build.gradle` |
| 2 | Editar `application.properties` — configurar modelo/temperatura/log | `budgeting/src/main/resources/application.properties` |
| 3 | Criar o teste de integração | `budgeting/src/test/java/dio/budgeting/GeminiChatModelIT.java` |
| 4 | Criar o controller | `budgeting/src/main/java/dio/budgeting/ChatModelController.java` |

A ordem importa: primeiro habilitamos web (passo 1) e configuramos o modelo (passo 2), depois **validamos com um teste** (passo 3) — só então, com a integração confirmada, escrevemos o endpoint HTTP (passo 4). Essa é a mesma lógica "testar antes de expor" que vamos repetir em quase toda Parte daqui em diante.

### 3.1. A interface `ChatModel`, explicada do zero (leitura, nenhum arquivo a criar)

> **⚠️ Não crie nenhum arquivo para este bloco.** O código abaixo **já existe pronto**, dentro do `.jar` da dependência `spring-ai-client-chat` (baixada automaticamente desde a Parte 1, via o BOM do Spring AI). Ele é mostrado aqui **só para leitura e explicação** — é a "planta baixa" da interface que a classe `GoogleGenAiChatModel` (também já pronta, vinda do starter do Gemini) implementa por trás. O primeiro arquivo que você de fato cria nesta Parte é o teste do Passo 3, mais abaixo.

```java
public interface ChatModel extends Model<Prompt, ChatResponse>, StreamingChatModel {
    default String call(String message) {...}

    @Override
    ChatResponse call(Prompt prompt);
}
```

`ChatModel` é a interface central do Spring AI para conversar com LLMs. Ela declara:

- **`call(String message)`** — a forma mais simples possível: você manda uma `String` de texto e recebe uma `String` de volta. Repare na palavra-chave **`default`**: significa que este método já vem com uma **implementação pronta dentro da própria interface**.
- **`call(Prompt prompt)`** — a forma completa: recebe um objeto `Prompt` e devolve um `ChatResponse`, com o texto gerado e metadados (como *tokens* consumidos). **Não** tem `default` — toda implementação concreta precisa fornecê-lo obrigatoriamente.
- **`extends Model<Prompt, ChatResponse>, StreamingChatModel`** — `ChatModel` **herda** de outras duas interfaces: `Model<Prompt, ChatResponse>` (genérica a vários tipos de modelo) e `StreamingChatModel` (que expõe um método `stream(...)`, devolvendo um `Flux<String>` — um fluxo reativo de valores chegando ao longo do tempo, útil para exibir respostas "aparecendo aos poucos"; o projeto `budgeting` nunca usa `stream(...)`, mas é bom saber que existe).

**`Prompt`**, **`Message`** (`UserMessage`, `SystemMessage`, `AssistantMessage`) e **`ChatOptions`** (incluindo `model` e `temperature`) são os demais tipos usados por essa interface — todos explicados em detalhe na seção 3.2, antes do primeiro código real que você vai escrever.

### 3.2. `Prompt`, `Message`, `ChatOptions` e temperatura, explicados do zero

- **`Prompt`** — representa, de forma completa, tudo o que será enviado ao modelo: uma **lista de mensagens** e, opcionalmente, **opções de configuração**.
- **`Message`** — uma "fala" dentro da conversa: **`UserMessage`** (o que a pessoa disse), **`SystemMessage`** (instruções do desenvolvedor) e **`AssistantMessage`** (respostas já geradas pelo modelo).
- **`ChatOptions`** — parâmetros que controlam **como** o modelo gera a resposta:
  - **modelo (`model`)** — qual variante do LLM usar.
  - **temperatura (`temperature`)** — controla o quão "aleatória" é a escolha de cada próxima palavra. `0` = respostas mais previsíveis; valores mais altos = mais variação. Como o `budgeting` extrai dados estruturados (valores, categorias), a temperatura global do projeto fica em `0.0`.

### 3.3. Passo 2 — Editar `application.properties`: configurando o modelo e o log

**📁 Arquivo:** `budgeting/src/main/resources/application.properties` (editar — o arquivo já existe, com as duas linhas da Parte 1/2)

**O que fazer:** **adicione** estas três linhas ao final do arquivo (não apague o que já estava lá):

```properties
spring.ai.google.genai.chat.options.model=gemini-3-flash-preview
spring.ai.google.genai.chat.options.temperature=0.0
logging.level.org.springframework.ai=DEBUG
```

**✅ Depois desta edição, `application.properties` fica assim, completo:**

```properties
spring.application.name=budgeting
#spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.google.genai.api-key=${GEMINI_API_KEY}
spring.ai.google.genai.chat.options.model=gemini-3-flash-preview
spring.ai.google.genai.chat.options.temperature=0.0
logging.level.org.springframework.ai=DEBUG
```

Explicando as três linhas novas:

- **`spring.ai.google.genai.chat.options.model=gemini-3-flash-preview`** — define, globalmente, qual modelo Gemini é usado por padrão em toda chamada de chat, a menos que uma chamada específica sobrescreva isso. `gemini-3-flash-preview` é uma variante "Flash" — rápida e barata, com capacidade suficiente para extração de dados estruturados.
- **`spring.ai.google.genai.chat.options.temperature=0.0`** — a configuração global de temperatura, explicada na seção 3.2: `0.0` para maximizar consistência.
- **`logging.level.org.springframework.ai=DEBUG`** — eleva o nível de log especificamente do pacote `org.springframework.ai`, para `DEBUG`.

  > **O que são "níveis de log", explicado do zero?** Bibliotecas de log organizam mensagens por severidade/detalhe: `ERROR`, `WARN`, `INFO` (padrão), `DEBUG` (detalhes técnicos de depuração), `TRACE`. Configurar só um pacote para `DEBUG` faz esse pacote específico exibir mais detalhe, sem "poluir" o log com tudo o mais. Com isso ativo, cada requisição/resposta trocada com o Gemini passa a ser impressa no console — indispensável para depurar Tool Calling mais adiante (Parte 5).

### 3.4. Passo 3 — Criar o teste de integração `GeminiChatModelIT`

**📁 Arquivo (novo):** `budgeting/src/test/java/dio/budgeting/GeminiChatModelIT.java`

**O que fazer:** crie este arquivo novo, dentro da pasta de testes (repare que é `src/test/...`, não `src/main/...`), com este conteúdo completo:

```java
package dio.budgeting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
public class GeminiChatModelIT {

    @Autowired
    GoogleGenAiChatModel chatModel;

    @Test
    void should_receiveResponse_when_chatModelIsCalled() {
        var options = GoogleGenAiChatOptions.builder()
                .model("gemini-3-flash-preview")
                .temperature(1.0)
                .responseMimeType("text/plain")
                .build();

        ChatResponse response = chatModel.call(new Prompt("Gere um registro de budgeting, com descricao de gasto, valor em reais e local", options));
        System.out.println("Gemini response: " + response.getResult().getOutput().getText());

        assertThat(response.getResult().getOutput().getText()).isNotEmpty();
    }

}
```

**✅ Este é o arquivo completo — não há nada a acrescentar depois.**

Explicando cada peça, na ordem em que aparece no arquivo:

- **`import static org.assertj.core.api.Assertions.assertThat;`** — um `import static` traz um **método estático específico** (`assertThat`), permitindo chamá-lo diretamente pelo nome.
- **`@SpringBootTest`** — sobe o contexto completo do Spring, incluindo a auto-configuração do starter do Gemini, que cria automaticamente o *bean* `GoogleGenAiChatModel`.
- **`@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")`** — condiciona a execução do teste à existência da variável de ambiente `GEMINI_API_KEY` não vazia (`.+` = "um ou mais caracteres quaisquer"). Sem ela, o JUnit **pula** o teste (não falha).
- **`@Autowired GoogleGenAiChatModel chatModel;`** — injeção de dependência por campo: o Spring localiza, no contexto já montado, um *bean* do tipo `GoogleGenAiChatModel` e o atribui a este campo automaticamente.
- **`GoogleGenAiChatOptions.builder()...build()`** — o **padrão Builder**: em vez de um construtor gigante com muitos parâmetros, a classe expõe métodos encadeáveis (`.model(...)`, `.temperature(...)`, `.responseMimeType(...)`), finalizados por `.build()`.
  - **`.model(...)`** e **`.temperature(1.0)`** — sobrescrevem, só para esta chamada, os valores globais do `application.properties` (a temperatura sobe para `1.0` porque este teste pede à IA para **inventar** um exemplo, e alguma criatividade é aceitável aqui).
  - **`.responseMimeType("text/plain")`** — pede resposta em texto plano.
- **`new Prompt(texto, options)`** — um construtor de `Prompt` que recebe diretamente uma `String` (convertida automaticamente em `UserMessage`) e as opções.
- **`chatModel.call(prompt)`** — a chamada de fato à API do Gemini, pela rede.
- **`response.getResult().getOutput().getText()`** — a cadeia para chegar ao texto: `getResult()` (o candidato principal) → `.getOutput()` (a mensagem gerada) → `.getText()` (o texto puro).
- **`assertThat(...).isNotEmpty()`** — usando **AssertJ**, confirma apenas que **alguma** resposta não vazia voltou — não valida o conteúdo exato (imprevisível, já que pedimos criatividade).

**Rode este teste agora**, antes de seguir para o Passo 4 — é o momento de confirmar que a integração real com o Gemini funciona.

### 3.5. Passo 1 (contextualizando) — por que `spring-boot-starter-web` era necessário

Antes do controller do Passo 4, é preciso ter adicionado ao `build.gradle` a dependência que traz suporte a HTTP — o que você já deve ter feito no início desta Parte, no Passo 1:

**📁 Arquivo:** `budgeting/build.gradle` (editar)

**O que fazer:** dentro do bloco `dependencies { }`, **adicione** esta linha (mantendo tudo o que já estava lá, da Parte 1/2):

```groovy
implementation 'org.springframework.boot:spring-boot-starter-web'
```

**✅ Depois desta edição, `dependencies { }` fica assim, completo:**

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

- **`spring-boot-starter-web`** — o *starter* que traz tudo o necessário para construir uma API REST: um servidor HTTP embutido (por padrão, o **Tomcat**, rodando dentro do próprio processo da aplicação), as anotações de controller (`@RestController`, `@GetMapping`, etc.), e a biblioteca **Jackson**, usada automaticamente para converter objetos Java em JSON e vice-versa.

Depois de adicionar esta linha, lembre-se de sincronizar o Gradle (dica prática da Parte 1/2, seção 1.5) antes de seguir.

### 3.6. Passo 4 — Criar `ChatModelController`: o primeiro endpoint da API

**📁 Arquivo (novo):** `budgeting/src/main/java/dio/budgeting/ChatModelController.java`

**O que fazer:** crie este arquivo, dentro de `src/main/java/dio/budgeting/` (repare: `main`, não `test` — este é código de produção, não um teste), com este conteúdo:

```java
package dio.budgeting;

import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatModelController {
    private final GoogleGenAiChatModel chatModel;

    public ChatModelController(GoogleGenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/chat-model")
    String chat(String prompt) {
        return this.chatModel.call(prompt);
    }

}
```

**✅ Este é o arquivo completo.**

- **`@RestController`** — combina `@Controller` (marca a classe como componente web) com `@ResponseBody` (escreve o retorno de cada método diretamente no corpo da resposta HTTP, em vez de tratá-lo como nome de página).
- **`@RequestMapping("/api")`** — define um **prefixo de URL** comum a todos os métodos da classe: todo endpoint aqui começa em `/api`.
- **`private final GoogleGenAiChatModel chatModel;`** — campo privado (só a própria classe acessa) e `final` (não pode ser reatribuído depois de inicializado).
- **`public ChatModelController(GoogleGenAiChatModel chatModel) { this.chatModel = chatModel; }`** — **injeção de dependência via construtor**: o Spring identifica que a classe precisa de um `GoogleGenAiChatModel`, localiza esse *bean* (criado pela auto-configuração desde a Parte 1) e o passa automaticamente. `this.chatModel = chatModel;` — o `this.` é necessário porque o parâmetro e o campo têm o mesmo nome.

  > **Por que construtor, e não `@Autowired` em campo (como no teste)?** Torna as dependências explícitas e obrigatórias, e facilita testar a classe isoladamente. Este é o padrão usado em **todas** as classes de produção deste projeto, a partir daqui.
- **`@GetMapping("/chat-model")`** — mapeia requisições `GET` para `/api/chat-model` a este método.
- **`String chat(String prompt)`** — o parâmetro `prompt`, sem anotação, é preenchido automaticamente a partir de um **parâmetro de query string** de mesmo nome (`?prompt=...`).
- **`return this.chatModel.call(prompt);`** — usa a versão simplificada de `call`, vista na seção 3.1.

**Testando manualmente**, com a aplicação rodando:

```http
GET http://localhost:8080/api/chat-model?prompt=Oi
```

Deve devolver uma resposta de texto, como *"Oi! Como posso ajudar você hoje?"*.

### 3.7. Checkpoint da Parte 3

| Arquivo | Ação nesta Parte |
|---|---|
| `budgeting/build.gradle` | **Editado** — `spring-boot-starter-web` adicionado |
| `budgeting/src/main/resources/application.properties` | **Editado** — 3 linhas novas (modelo, temperatura, log) |
| `budgeting/src/test/java/dio/budgeting/GeminiChatModelIT.java` | **Criado** |
| `budgeting/src/main/java/dio/budgeting/ChatModelController.java` | **Criado** |

**Recapitulando:** agora temos a primeira integração real e funcionando com o Gemini, tanto testada (`GeminiChatModelIT`) quanto exposta via HTTP (`ChatModelController`). O próximo passo, na Parte 4, é trocar essa API de baixo nível (`ChatModel`) por uma API mais expressiva e fluente (`ChatClient`), que será a peça central do assistente a partir daqui.


---

## Parte 4 — ChatClient: a API fluente com contexto (Vídeo 04)

### Recapitulando

Até aqui, o projeto conversa com o Gemini usando diretamente o `ChatModel` — uma interface de baixo nível, específica de provedor, com um método `call(...)` simples. Vamos agora conhecer o `ChatClient`, uma API construída em cima dele.

### Objetivo

Trocar o `ChatModel` puro por `ChatClient` — uma API de mais alto nível e mais expressiva, que será a peça central do assistente a partir daqui, já que é ela que, mais adiante (Parte 5 em diante), ganhará *tools* e um prompt de sistema completo.

> **📁 Arquivos desta etapa:**
> 1. **Criar** `src/test/java/dio/budgeting/GeminiChatClientIT.java` — o teste primeiro, mesmo padrão da Parte 3 (seção 4.3).
> 2. **Criar** `src/main/java/dio/budgeting/ChatClientController.java` — o endpoint (seção 4.2).
>
> Nenhuma dependência nova no `build.gradle` e nenhuma propriedade nova no `application.properties` — o `ChatClient` reaproveita 100% da configuração já feita na Parte 3. Ambos os arquivos continuam soltos em `dio.budgeting`.

### 4.1. `ChatClient` vs. `ChatModel`: o que muda, exatamente

O `ChatClient` **não substitui** a auto-configuração vista na Parte 3 — ele é construído **em cima** de um `ChatModel` já existente, reaproveitando toda a configuração de conexão, autenticação e opções padrão já feita. A diferença central está na **expressividade** da API:

- Com o `ChatModel` puro, para configurar algo além do texto simples, era preciso montar manualmente objetos como `Prompt` e `GoogleGenAiChatOptions` (como fizemos no teste da Parte 3.4).
- Com o `ChatClient`, existe uma **API fluente** dedicada — métodos encadeados que leem quase como uma frase — especificamente pensada para compor uma conversa com IA, incluindo:
  - uma **mensagem de sistema** (*system message*) — instruções, definidas pelo desenvolvedor, que moldam o comportamento geral do assistente; não são visíveis ao usuário final, mas influenciam como o modelo interpreta e responde a cada mensagem;
  - uma ou mais **mensagens de usuário** (*user message*) — a entrada real de quem está conversando;
  - e, como veremos na Parte 5, **ferramentas** (*tools*) que o modelo pode decidir chamar.

> **Prompt de sistema × prompt de usuário, explicado do zero.** Tudo o que a pessoa usando a aplicação digita ou fala é um **prompt de usuário**. Um **prompt de sistema**, por outro lado, é definido pelo *desenvolvedor* (não pelo usuário final), dando contexto ao modelo sobre quem ele deve "ser" e o que se espera que ele faça — por exemplo, "você é um assistente financeiro" (frase que reaparecerá, quase literalmente, na Parte 11). Na prática, é uma forma de configurar o comportamento do modelo **antes mesmo** de qualquer mensagem do usuário chegar.

### 4.2. Criando o `ChatClient` a partir do `ChatClient.Builder`

Diferente do `ChatModel` (que já vinha pronto para injeção direta, graças à auto-configuração), o `ChatClient` **não é injetado diretamente** — ele precisa ser **construído** a partir de um `ChatClient.Builder`, que **esse sim** é auto-configurado e injetável.

```java
package dio.budgeting;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatClientController {

    private final ChatClient chatClient;

    public ChatClientController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(value = "prompt", defaultValue = "Olá!") String prompt) {
        return this.chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
```

- **`ChatClient.Builder`** — este é um *bean* de escopo **`prototype`** (diferente do que vimos até agora, que eram implicitamente *singleton* — uma única instância compartilhada por toda a aplicação). Escopo `prototype` significa que **uma nova instância é criada a cada injeção**. Isso importa aqui porque cada classe da aplicação que precisa de um `ChatClient` com configuração própria (um `defaultSystem` diferente, *tools* diferentes — como veremos na Parte 11, onde `TranscriptionController` monta um `ChatClient` bem mais elaborado do que este) recebe seu próprio *builder* "limpo" para configurar do zero, sem que uma classe interfira na configuração de outra.
- **`public ChatClientController(ChatClient.Builder chatClientBuilder) { this.chatClient = chatClientBuilder.build(); }`** — o Spring injeta o *builder* pronto (auto-configurado a partir do `GoogleGenAiChatModel` já existente no contexto), e o construtor imediatamente chama `.build()` sobre ele — sem nenhuma configuração adicional ainda — guardando o `ChatClient` resultante no campo `final` da classe. `.build()` finaliza a construção e devolve a instância pronta, do mesmo jeito que já vimos com `GoogleGenAiChatOptions.builder()...build()` na Parte 3.4.
- **`@RequestParam(value = "prompt", defaultValue = "Olá!") String prompt`** — diferente do parâmetro "cru", sem anotação, do `ChatModelController` (Parte 3.6), aqui o parâmetro de *query string* é declarado explicitamente com **`@RequestParam`**, o que permite configurar um **valor padrão**: `defaultValue = "Olá!"`. Isso significa que, se a requisição não informar `?prompt=...` na URL, o Spring usa `"Olá!"` automaticamente, em vez de devolver um erro ou um valor nulo.
- **`this.chatClient.prompt()`** — inicia a construção **fluente** de uma nova interação com o modelo — o ponto de entrada da API que dá nome ao conceito de "API fluente" explicado a seguir.

  > **O que é uma API fluente (*fluent API*), explicado do zero?** É um estilo de projeto de API em que os métodos são **encadeados** um após o outro (`objeto.metodoA().metodoB().metodoC()`), e cada método (exceto, tipicamente, o último da cadeia) devolve um novo objeto que permite continuar encadeando mais chamadas. Isso torna o código mais legível — quase como ler uma frase em linguagem natural — e evita a necessidade de criar várias variáveis intermediárias só para guardar resultados parciais.
- **`.user(prompt)`** — adiciona o texto recebido como uma mensagem do tipo **usuário** (`UserMessage`, já mencionada na Parte 3.2) a esta interação em construção.
- **`.call()`** — dispara, de fato, a chamada síncrona ao `ChatModel` que está por baixo deste `ChatClient` — o mesmo `GoogleGenAiChatModel` já configurado desde a Parte 3, só que acessado agora através da camada mais amigável do `ChatClient`.
- **`.content()`** — extrai apenas o **texto** da resposta, já pronto para uso como `String` — um atalho de conveniência equivalente, em uma única chamada, à cadeia `getResult().getOutput().getText()` que era necessária para extrair texto de um `ChatResponse` ao trabalhar diretamente com o `ChatModel` (Parte 3.4).

### 4.3. Teste de integração: `GeminiChatClientIT`, explicado linha por linha

```java
package dio.budgeting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
public class GeminiChatClientIT {
    @Autowired
    GoogleGenAiChatModel chatModel;

    @Test
    void should_executeSum_when_prompted() {
        var chatClient = ChatClient.builder(chatModel).defaultSystem("Voce é um matematico").build();

        var response = chatClient.prompt("Some 10 mais 20. Depois subtraia 30 do resultado anterior." +
                "Exiba o resultado final sem explicações")
                .call().content();

        assertThat(response).contains("0");
        System.out.println(response);
    }
}
```

- **`@Autowired GoogleGenAiChatModel chatModel;`** — repare que este teste injeta o **`ChatModel`**, não o `ChatClient.Builder` — a estratégia aqui é diferente da do controller: em vez de receber o *builder* já pronto, o teste vai construir o `ChatClient` manualmente a partir do `ChatModel` injetado, usando uma forma alternativa do método `builder`, explicada a seguir.
- **`ChatClient.builder(chatModel)`** — uma forma **estática alternativa** de obter um *builder*: em vez de `ChatClient.Builder` sendo injetado pronto pelo Spring (como no controller), aqui o método estático `ChatClient.builder(...)` recebe diretamente um `ChatModel` já em mãos e devolve um *builder* configurado a partir dele. É uma forma conveniente de usar em testes, onde já se tem o `ChatModel` disponível por outro motivo (a injeção via `@Autowired`).
- **`.defaultSystem("Voce é um matematico")`** — o método do *builder* que define a **mensagem de sistema padrão** (explicada na Parte 4.1): o texto passado aqui será enviado como prompt de sistema em **toda** chamada feita a partir deste `ChatClient` específico, sem precisar ser repetido a cada `.prompt(...)`. O prefixo **`default`** neste método (e em outros que veremos, como `defaultTools`, na Parte 5) sinaliza que a configuração vale para **todas** as chamadas feitas a partir deste `ChatClient`, a menos que uma chamada específica a sobrescreva explicitamente.
- **`chatClient.prompt("...")`** — uma forma **abreviada** de `chatClient.prompt().user("...")`: quando se passa uma `String` diretamente como argumento de `prompt(...)`, ela já é tratada automaticamente como a mensagem de usuário, sem precisar do `.user(...)` explícito visto no controller (Parte 4.2). Ambas as formas são equivalentes — a escolha de qual usar é apenas de estilo/conveniência.
- **`.call().content()`** — idêntico ao já explicado na Parte 4.2: dispara a chamada síncrona e extrai o texto puro da resposta.
- **`assertThat(response).contains("0");`** — repare no `import static` diferente do usado no teste da Parte 3.4: aqui é `org.assertj.core.api.AssertionsForClassTypes.assertThat`, em vez de `org.assertj.core.api.Assertions.assertThat`. Na prática, o efeito é o mesmo — `AssertionsForClassTypes` é uma classe interna do próprio AssertJ, focada em asserções para tipos "simples" como `String`, e a classe `Assertions` (usada no teste anterior) estende `AssertionsForClassTypes` por baixo dos panos, entre outras. A diferença de qual `import` foi escolhido em cada teste provavelmente reflete apenas uma sugestão automática diferente da IDE em momentos distintos do desenvolvimento — sem nenhum impacto prático no comportamento do teste.
- **`.contains("0")`**, em vez de `.isEqualTo("0")` — esta escolha **não** é acidental: o prompt pede a soma `10 + 20 − 30 = 0`, mas mesmo pedindo explicitamente "sem explicações", o modelo pode devolver um pouco de texto ao redor do número (por exemplo, "O resultado é 0"). Um `.isEqualTo("0")` falharia nesse cenário, mesmo com a resposta numérica correta — enquanto `.contains("0")` continua validando que o resultado certo está presente em algum lugar da resposta, sem exigir uma correspondência exata de formato. Este é um padrão que reaparece sempre que se testa a saída, em texto livre, de uma LLM.

Conta que o teste valida: `10 + 20 = 30`; `30 − 30 = 0`. **Ponto importante, que motiva a próxima Parte:** neste momento (antes do Tool Calling, Parte 5), é o **próprio modelo de linguagem** quem faz essa conta "de cabeça" — baseado em padrões estatísticos aprendidos durante o treinamento, não em uma operação matemática real e exata. Isso funciona razoavelmente bem para aritmética simples como esta, mas não é confiável nem verificável para operações mais complexas ou para regras de negócio precisas — como, por exemplo, garantir que um valor monetário seja registrado com exatidão. É exatamente esse problema que o **Tool Calling**, na Parte 5, resolve.

### 4.4. Checkpoint da Parte 4

Confirmado no `.zip`: `ChatClientController.java` existe com o endpoint `GET /api/chat`, injetando `ChatClient.Builder` diretamente no construtor (sem nenhuma classe de configuração `@Configuration` separada — o `ChatClient` é montado localmente, dentro do próprio construtor do controller). `GeminiChatClientIT.java` existe validando a construção fluente do `ChatClient` a partir do `GoogleGenAiChatModel` injetado.

**Recapitulando:** agora temos duas formas de conversar com o Gemini funcionando lado a lado — o `ChatModel` de baixo nível (Parte 3, endpoint `/api/chat-model`) e o `ChatClient` fluente (esta Parte, endpoint `/api/chat`). A partir daqui, é sempre o `ChatClient` que será usado, já que é ele que suporta os dois recursos que tornam o assistente realmente útil: prompt de sistema configurável e, a seguir, Tool Calling.


---

## Parte 5 — Tool Calling: quando a IA executa código de verdade (Vídeo 05)

### Recapitulando

Na Parte 4, vimos que o `ChatClient` conseguia "resolver" uma soma simples, mas apenas prevendo estatisticamente qual seria o resultado — sem executar nenhuma operação matemática real. Vamos agora corrigir isso com Tool Calling, o conceito mais importante de todo o projeto, já que é ele que permite ao assistente **agir de verdade** sobre o sistema (salvar e consultar transações), e não apenas conversar.

### Objetivo

Substituir a "matemática de cabeça" do modelo por chamadas reais a métodos Java, introduzindo o padrão de Tool Calling em um exemplo simples e controlado, antes de aplicá-lo aos casos de uso reais do domínio (o que só acontece na Parte 8 em diante).

> **📁 Arquivos desta etapa:**
> 1. **Criar** `src/test/java/dio/budgeting/ToolCallingIT.java` — único arquivo desta etapa (seção 5.4), contendo a classe interna `MathTools`.
>
> Não existe nenhum arquivo de produção nesta Parte — é intencionalmente **só um teste**, um "laboratório" isolado para aprender o mecanismo de Tool Calling antes de aplicá-lo a algo real. Nenhuma dependência nova é necessária (o suporte a `@Tool` já veio, de forma transitiva, junto do starter do Gemini, desde a Parte 1).

### 5.1. Tool Calling (Function Calling), explicado do zero, passo a passo

**Tool Calling** — também chamado de *Function Calling* na documentação de vários provedores — é um recurso em que um LLM, ao processar um prompt, pode decidir que a melhor forma de responder não é gerar texto diretamente, mas **solicitar a execução de uma função/método específico**, previamente disponibilizado pela aplicação, com argumentos que o próprio modelo extrai do contexto da conversa.

O fluxo completo, passo a passo:

1. **Declaração:** a aplicação informa ao modelo, junto com o prompt, quais *tools* (ferramentas) estão disponíveis — cada uma identificada por um **nome**, uma **descrição** (em linguagem natural, explicando o que a ferramenta faz e quando usá-la) e uma **assinatura de parâmetros** (quais argumentos ela espera, e de que tipo).
2. **Decisão do modelo:** o modelo recebe o prompt do usuário e, sozinho, decide se alguma das *tools* disponíveis deveria ser chamada para responder adequadamente — e, se sim, **com quais argumentos**, extraídos do contexto da conversa.
3. **Execução real:** este é o ponto mais importante de entender — **o modelo não executa nada por conta própria**. Ele apenas *solicita* a chamada (essa solicitação é, na prática, apenas mais uma estrutura de dados na resposta da API, dizendo "eu gostaria que a ferramenta X fosse chamada com estes argumentos"). É a **aplicação** — no nosso caso, o Spring AI, atuando por trás do `ChatClient` — quem efetivamente localiza o método Java correspondente e o invoca de verdade.
4. **Retorno e continuação:** o resultado dessa execução real volta para o modelo como uma nova mensagem, inserida automaticamente no histórico da conversa (uma `ToolResponseMessage`, mencionada brevemente na Parte 3.2 como um dos tipos de `Message` existentes). O modelo então usa esse resultado — que é um dado real e exato, não mais uma previsão estatística — para formular a resposta final ao usuário.

> **Por que isso resolve o problema visto na Parte 4?** Porque, em vez do modelo "adivinhar" o resultado de `10 + 20 − 30` com base em padrões de texto que viu durante o treinamento, ele passa a **delegar** o cálculo para um método Java real, que executa a operação matematicamente exata — e é esse valor exato, e não uma previsão, que retorna ao modelo para compor a resposta.

Os dois grandes casos de uso do Tool Calling, segundo a própria documentação do Spring AI, resumem bem por que este recurso existe: **Information Retrieval** ("busca de informação" — obter dados que o modelo não tem e não poderia saber, como o conteúdo atual de um banco de dados) e **Taking Action** ("realizar uma ação" — executar um efeito real no sistema, como salvar um novo registro). No projeto `budgeting`, ambos os casos aparecem: `ListTransactionsByCategoryUseCase` (Parte 10) é *Information Retrieval*; `PersistTransactionUseCase` (Parte 8) é *Taking Action*.

### 5.2. A anotação `@Tool`, explicada do zero

Uma *tool* é declarada simplesmente anotando um método Java comum com `@Tool`:

```java
static class MathTools {
    @Tool(description = "soma dois números inteiros, a e b")
    public int sum(int a, int b) {
        return a + b;
    }

    @Tool(description = "subtrai dois números inteiros, a e b")
    public int diff(int a, int b) {
        return a - b;
    }
}
```

- **`@Tool(description = "...")`** — a anotação, do pacote `org.springframework.ai.tool.annotation`, que transforma um método Java comum em uma ferramenta disponível ao modelo. O atributo **`description`** é, sem exagero, o elemento mais importante desta anotação: é o único texto que o modelo tem disponível para decidir **quando** e **por que** essa ferramenta deveria ser chamada. Quanto mais clara, específica e sem ambiguidade for essa descrição, melhor o modelo acerta a decisão de uso — ela funciona como uma "bula", escrita para a IA interpretar, não como um comentário de código para outro desenvolvedor humano ler.
- **Descoberta automática de parâmetros, via reflexão.** Repare que **não foi preciso** escrever manualmente, em nenhum lugar, "o parâmetro `a` é um inteiro chamado `a`". O Spring AI usa **reflexão** — a capacidade que a própria linguagem Java tem de examinar, em tempo de execução, a estrutura de uma classe (seus métodos, parâmetros, tipos) — para descobrir automaticamente o nome de cada parâmetro (`a`, `b`) e seu tipo (`int`), e a partir disso monta, sozinho, um **esquema** (uma descrição estruturada, no formato JSON Schema) que é enviado ao modelo junto da `description`, informando exatamente quais argumentos a ferramenta espera.

  > **O que é reflexão em Java, explicado do zero?** Normalmente, quando você escreve código Java, você sabe de antemão quais classes, métodos e campos vai usar — essa informação está fixa no próprio código-fonte. **Reflexão** é a capacidade da linguagem de **inspecionar essa estrutura em tempo de execução**, de forma dinâmica: por exemplo, dado um objeto qualquer, é possível perguntar "quais métodos esta classe tem?", "quais parâmetros este método específico espera, e de que tipos?", sem que essas perguntas tenham sido "programadas" com antecedência para aquela classe exata. É exatamente esse mecanismo que permite ao Spring AI (e a outras partes do próprio Spring, de forma geral) examinar qualquer classe anotada e gerar automaticamente metadados sobre ela, sem exigir configuração manual repetitiva.

### 5.3. Registrando as tools no `ChatClient`: `.defaultTools(...)`

```java
var chatClient = ChatClient.builder(chatModel)
        .defaultSystem("Você é um matemático")
        .defaultTools(new MathTools())
        .build();
```

- **`.defaultTools(new MathTools())`** — registra uma **instância** da classe de ferramentas (`new MathTools()` — criando um objeto novo dessa classe, na hora) como disponível para todas as chamadas feitas a partir deste `ChatClient` específico. É importante registrar corretamente onde essa chamada acontece: o registro precisa ser feito no momento da **construção** do `ChatClient` (encadeado junto de `.build()`), via `.defaultTools(...)` — registrar uma *tool* apenas em uma chamada específica de `.prompt(...)` (em vez de no *builder*) não garante o mesmo comportamento consistente ao longo da vida útil daquele `ChatClient`.

### 5.4. Teste de integração: `ToolCallingIT`, explicado linha por linha

```java
package dio.budgeting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
public class ToolCallingIT {
    @Autowired
    GoogleGenAiChatModel chatModel;

    static class MathTools {

        @Tool(description = "soma dois números inteiros, a e b")
        public int sum(int a, int b) {
            return a + b;
        }

        @Tool(description = "subtrai dois números inteiros, a e b")
        public int diff(int a, int b) {
            return a - b;
        }
    }

    @Test
    void should_executeSum_when_prompted() {
        var chatClient = ChatClient.builder(chatModel)
                .defaultSystem("Voce é um matematico")
                .defaultTools(new MathTools())
                .build();

        var response = chatClient.prompt("Some 10 mais 20. Depois subtraia 30 do resultado anterior." +
                "Exiba o resultado final sem explicações")
                .call().content();

        assertThat(response).contains("0");
        System.out.println(response);
    }
}
```

- **`static class MathTools { ... }`** — uma **classe interna estática** (*static nested class*), declarada dentro da própria classe de teste `ToolCallingIT`. A palavra-chave `static` aqui significa que essa classe interna **não precisa** de uma instância de `ToolCallingIT` para existir — ela pode ser instanciada diretamente com `new MathTools()`, independentemente de qualquer teste específico. Ela existe apenas para agrupar, localmente, as duas ferramentas de exemplo usadas neste teste — na Parte 8, veremos que as ferramentas "de verdade" do projeto (`PersistTransactionUseCase`, `ListTransactionsByCategoryUseCase`) não são classes internas de teste, mas classes de primeira classe do próprio pacote `application`.

Todo o restante deste teste é **estruturalmente idêntico** ao `GeminiChatClientIT` da Parte 4.3 (mesmo prompt, mesma asserção `.contains("0")`, mesmo padrão de `@SpringBootTest` + `@EnabledIfEnvironmentVariable`) — a única diferença de código é a linha `.defaultTools(new MathTools())` adicionada à construção do `ChatClient`.

**Ponto crucial: essa diferença não é visível "olhando o resultado".** O texto final devolvido pelo modelo pode até ser idêntico ao do teste da Parte 4 (`"0"`, ou uma frase contendo `"0"`) — a diferença real está no **comportamento interno**: em vez do modelo "adivinhar" a soma e a subtração a partir de padrões estatísticos de linguagem, ele agora **delega** ambas as operações para os métodos reais `sum` e `diff`, que executam a aritmética de forma exata em Java.

**Como confirmar, de fato, que a tool foi usada — e não o modelo "de cabeça"?** É aqui que a propriedade `logging.level.org.springframework.ai=DEBUG`, configurada lá na Parte 3.3, se torna útil: com ela ativa, os logs de execução deste teste mostram entradas de classes internas do Spring AI como `DefaultToolCallingManager` e `MethodToolCallback`, evidenciando as chamadas reais aos métodos `sum` e `diff` — inclusive a conversão do valor de retorno de cada um para um formato estruturado (JSON), antes de ser devolvido ao modelo, que então usa esses valores exatos (e não estimados) para compor a resposta final.

### 5.5. Checkpoint da Parte 5

Confirmado no `.zip`: `ToolCallingIT.java` existe em `dio.budgeting`, com a classe interna `MathTools` (contendo os métodos `sum` e `diff`, anotados com `@Tool`) e o método de teste `should_executeSum_when_prompted`, usando `GoogleGenAiChatModel` e `.defaultTools(...)`.

**Recapitulando:** este teste é, propositalmente, um "protótipo conceitual" simples e didático — soma e subtração de inteiros, sem nenhuma relação direta com o domínio de negócio do projeto (transações financeiras). É exatamente esse mesmo padrão — um método anotado com `@Tool`, registrado via `.defaultTools(...)` — que será aplicado, a partir da Parte 8, aos casos de uso **reais** do domínio: `PersistTransactionUseCase.execute(...)` e `ListTransactionsByCategoryUseCase.execute(...)`. Se o padrão desta Parte 5 ficou claro, o Tool Calling "de verdade" nas próximas partes será apenas uma aplicação do mesmo mecanismo a um problema mais interessante.


---

## Parte 6 — Transcrevendo áudio em texto: o primeiro ponto sem equivalente Gemini (Vídeo 06)

### Recapitulando

Já sabemos conversar com o Gemini (`ChatModel`, `ChatClient`) e já sabemos como delegar ações reais a métodos Java (Tool Calling). Falta ainda o primeiro passo do pipeline completo: transformar um áudio em texto processável.

### Objetivo

Transformar um arquivo de áudio (a fala do usuário) em texto — o primeiro elo real da cadeia **Áudio → STT → Tool Calling → TTS → Áudio**.

> **📁 Arquivos desta etapa:**
> 1. **Adicionar seus próprios áudios de teste** em `src/test/resources/audio/` — grave (ou peça para alguém gravar) seis áudios curtos, em português, cada um descrevendo um gasto com um valor diferente, e nomeie-os `recording-1.mp3` a `recording-6.mp3`. **Estes arquivos não vêm de lugar nenhum pronto — você precisa criá-los você mesmo** (celular, gravador do computador, etc.), já que são a "matéria-prima" real que o teste vai transcrever. Ajuste os valores esperados no `@CsvSource` do passo 2 para bater com o que você de fato falou em cada gravação.
> 2. **Criar** `src/test/java/dio/budgeting/GeminiTranscriptionModelIT.java` — o teste parametrizado (seção 6.5), usando os áudios do passo 1.
> 3. **Criar** `src/main/java/dio/budgeting/TranscriptionController.java` — **apenas o método `transcribe`**, por enquanto (seção 6.3/6.4). Este arquivo será **reaberto e expandido** na Parte 11, ganhando mais dois métodos e um construtor bem mais completo — não se preocupe em deixá-lo "definitivo" agora.
>
> Nenhuma dependência nova no `build.gradle` — a transcrição reaproveita o mesmo `GoogleGenAiChatModel` já configurado desde a Parte 3.

### 6.1. O caminho ensinado no curso: `TranscriptionModel` (OpenAI/Whisper)

O Spring AI define, para transcrição, uma interface dedicada:

> **⚠️ Não crie nenhum arquivo para este bloco — e, neste caso específico, você nunca vai usá-lo de fato.** Este código existe dentro do Spring AI, mas **não tem implementação para o Gemini** (é justamente o assunto desta Parte). Ele é mostrado apenas para você entender o que o curso ensina com OpenAI, antes de ver, na seção 6.3, a solução real que você vai implementar.

```java
public interface TranscriptionModel extends Model<AudioTranscriptionPrompt, AudioTranscriptionResponse> {
    AudioTranscriptionResponse call(AudioTranscriptionPrompt transcriptionPrompt);

    default String transcribe(Resource resource) {
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(resource);
        return this.call(prompt).getResult().getOutput();
    }
}
```

- **`Resource`** — uma abstração do próprio Spring (não específica de IA, usada em várias partes do framework) para representar "algo que pode ser lido como uma sequência de bytes", independentemente de onde esse conteúdo realmente mora: pode ser um arquivo no disco, um arquivo dentro do *classpath* (empacotado junto do `.jar` da aplicação), um array de bytes já em memória, ou até um arquivo recém-recebido em uma requisição HTTP. É o tipo usado, ao longo de todo este projeto, para representar áudio de entrada, não importa a origem.
- **`transcribe(Resource resource)`** — um método `default` (mesmo conceito da Parte 3.1: já vem implementado dentro da própria interface) de conveniência: você passa o áudio, recebe diretamente a `String` transcrita, sem precisar montar manualmente um `AudioTranscriptionPrompt`.

No momento em que o curso ensina este conteúdo, o **único provedor suportado** pelo Spring AI para `TranscriptionModel` é a **Whisper API da OpenAI** (e sua variante equivalente no Azure OpenAI). Whisper é o modelo de reconhecimento de fala de propósito geral e multilíngue desenvolvido pela própria OpenAI. A configuração ensinada usa propriedades como (⚠️ **não adicione isto ao seu `application.properties`** — é a configuração da rota OpenAI, que seu projeto não usa; mostrado só para contraste com a seção 6.3):

```properties
spring.ai.model.audio.transcription=openai
spring.ai.openai.audio.transcription.options.model=whisper-1
spring.ai.openai.audio.transcription.options.language=pt
spring.ai.openai.audio.transcription.options.temperature=0
spring.ai.openai.audio.transcription.options.response-format=text
```

### 6.2. Por que essa rota não existe no projeto Gemini — explicado com cuidado

Este é um dos dois pontos mais importantes de todo o tutorial para entender (o outro é a Parte 7). O `spring-ai-starter-model-google-genai` — o *starter* usado neste projeto desde a Parte 1 — **não implementa a interface `TranscriptionModel`**.

**Por quê?** O Gemini, ao contrário do Whisper, não é um modelo especializado *apenas* em transcrição — ele é um modelo **multimodal** de propósito geral, capaz de receber, em uma mesma conversa, combinações de **texto, imagem, áudio e vídeo**, e gerar uma resposta considerando tudo isso junto. Ou seja, dentro do ecossistema Gemini, "transcrever um áudio" não é uma API tecnicamente separada de "conversar por texto" — é apenas **uma conversa de chat comum, em que uma das mensagens contém um áudio anexado**, acompanhada de um prompt de texto pedindo explicitamente para transcrever esse áudio.

> **O que é "multimodalidade" em IA, explicado do zero?** Um modelo é dito **multimodal** quando consegue processar (ou gerar) mais de um tipo de mídia dentro da mesma interação — por exemplo, receber tanto texto quanto uma imagem, e responder considerando os dois juntos ("descreva o que há nesta foto"). Modelos "unimodais", por outro lado, são especializados em um único tipo de entrada/saída — Whisper, por exemplo, foi treinado especificamente para a tarefa de transcrição de áudio, e nada além disso.

### 6.3. A solução adotada: `GoogleGenAiChatModel` + `Media`

O projeto reaproveita o **mesmo** `GoogleGenAiChatModel` já usado desde a Parte 3 para conversas normais, mas monta uma mensagem de usuário com **conteúdo multimídia** anexado, usando a classe `Media` do Spring AI:

```java
private static final String TRANSCRIPTION_PROMPT = """
        Transcreva o áudio a seguir com fidelidade em português brasileiro.
        Contexto do áudio: contém descrição de gastos financeiros.
        Retorne APENAS a transcrição do áudio.
        """;

String transcribe(@RequestParam("file") MultipartFile file) {
    var audioMedia = new Media(MimeTypeUtils.parseMimeType("audio/mpeg"), file.getResource());

    var userMessage = UserMessage.builder()
            .text(TRANSCRIPTION_PROMPT)
            .media(List.of(audioMedia))
            .build();

    var prompt = Prompt.builder()
            .messages(List.of(userMessage))
            .build();

    return chatModel.call(prompt).getResult().getOutput().getText();
}
```

Explicando cada parte, na ordem em que aparece:

- **`private static final String TRANSCRIPTION_PROMPT = """..."""`** — declara uma constante de classe (`static final`, ou seja, um único valor compartilhado por todas as instâncias, que nunca muda depois de definido) contendo o texto de instrução enviado ao modelo. As três aspas duplas (`"""`) abrem um **text block** — um recurso do Java, disponível desde a versão 15, que permite escrever *strings* que ocupam várias linhas sem precisar concatenar manualmente cada uma delas com `"linha 1\n" + "linha 2\n" + ...` — o texto entre `"""` e `"""` é interpretado literalmente, preservando quebras de linha, tornando prompts longos muito mais legíveis de escrever e revisar.
- **`new Media(MimeTypeUtils.parseMimeType("audio/mpeg"), file.getResource())`** — cria um objeto `Media`, que empacota, juntos: (1) o **tipo MIME** do conteúdo anexado, e (2) o conteúdo em si, como um `Resource`.

  > **O que é um "tipo MIME", explicado do zero?** MIME (*Multipurpose Internet Mail Extensions*) é um padrão para identificar o **formato/tipo de um arquivo** através de uma string curta e padronizada, no formato `tipo/subtipo` — por exemplo, `text/plain` (texto puro, já visto na Parte 3.4), `application/json` (dados JSON), ou, aqui, `audio/mpeg` (áudio no formato MP3). Esse identificador é usado tanto em requisições/respostas HTTP quanto, como neste caso, para informar a uma IA multimodal **como interpretar** um bloco de bytes anexado — sem essa informação, o Gemini não saberia se aqueles bytes representam um áudio, uma imagem, ou outra coisa.
  - **`MimeTypeUtils.parseMimeType("audio/mpeg")`** — um método utilitário do Spring que converte a `String` `"audio/mpeg"` em um objeto `MimeType` estruturado, validando que o formato é reconhecível.
  - **`file.getResource()`** — converte o `MultipartFile` (explicado a seguir) — o arquivo recebido dentro da requisição HTTP — para um `Resource`, o tipo esperado pelo construtor de `Media`.
- **`UserMessage.builder().text(TRANSCRIPTION_PROMPT).media(List.of(audioMedia)).build()`** — usa, mais uma vez, o **padrão Builder** (Parte 3.4) para montar uma `UserMessage` que combina **dois tipos de conteúdo em uma única mensagem**: `.text(...)` adiciona o prompt de instrução (pedindo a transcrição), e `.media(List.of(audioMedia))` anexa o áudio — essa combinação, em uma única mensagem, é exatamente o que caracteriza a multimodalidade explicada na Parte 6.2.
  - **`List.of(audioMedia)`** — cria uma **lista imutável** (não pode ter itens adicionados ou removidos depois de criada) contendo um único elemento, `audioMedia`. `List.of(...)` é um método de fábrica introduzido no Java moderno para criar listas pequenas e fixas de forma concisa, sem precisar instanciar explicitamente uma `ArrayList` e chamar `.add(...)` em seguida.
- **`Prompt.builder().messages(List.of(userMessage)).build()`** — monta o `Prompt` final, contendo apenas essa única mensagem multimodal, usando o mesmo padrão Builder já visto (uma forma alternativa ao construtor `new Prompt(texto, options)` usado na Parte 3.4 — aqui, com uma lista de mensagens explícita, em vez de um texto simples).
- **`chatModel.call(prompt).getResult().getOutput().getText()`** — a mesma cadeia de extração de texto já vista, em detalhe, na Parte 3.4. Repare que, do ponto de vista do código, **não há absolutamente nenhuma diferença estrutural** entre "responder normalmente a uma pergunta de texto" (Parte 3) e "transcrever um áudio" (aqui) — ambos são, para o Spring AI e para o Gemini, apenas "gerar texto a partir de uma mensagem de entrada". A única diferença está no **conteúdo** dessa mensagem de entrada (com ou sem `Media` anexada) e na instrução dada no prompt.

### 6.4. `MultipartFile`: recebendo um arquivo de verdade por HTTP, explicado do zero

```java
@PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
String transcribe(@RequestParam("file") MultipartFile file) { ... }
```

- **`@PostMapping`**, em vez de `@GetMapping` (visto nas Partes 3 e 4) — usa o verbo HTTP **`POST`**, adequado aqui porque estamos **enviando dados relativamente grandes** (um arquivo de áudio) para o servidor processar, e não apenas pedindo para "buscar" algo através de parâmetros simples na URL (o que seria o uso típico de `GET`).
- **`consumes = MediaType.MULTIPART_FORM_DATA_VALUE`** — o atributo `consumes` declara qual **tipo de conteúdo** (`Content-Type`) este endpoint aceita receber no corpo da requisição. `MediaType.MULTIPART_FORM_DATA_VALUE` é uma constante que representa a *string* `"multipart/form-data"` — o formato padrão usado por navegadores e ferramentas HTTP para enviar **arquivos binários** dentro de uma requisição (diferente de `application/json`, adequado para dados textuais estruturados, mas não para arquivos brutos).

  > **O que é "multipart/form-data", explicado do zero?** É um formato de corpo de requisição HTTP desenhado especificamente para permitir o envio de **múltiplas partes** de dados diferentes em uma única requisição — cada parte pode ser um campo de texto simples, ou um arquivo binário completo, cada uma identificada por um nome. É o mesmo mecanismo usado, por exemplo, quando você anexa um arquivo em um formulário web tradicional.
- **`@RequestParam("file") MultipartFile file`** — diferente do `@RequestParam` visto na Parte 4.2 (que lia um parâmetro de *query string*), aqui ele associa este parâmetro à **parte** da requisição multipart cujo nome é `"file"` — ou seja, quem chama este endpoint precisa enviar um campo chamado exatamente `file` dentro do corpo `multipart/form-data`.
- **`MultipartFile`** — a abstração do Spring Web especificamente pensada para representar um arquivo recebido dentro de uma requisição multipart. Ela oferece métodos como `getBytes()` (o conteúdo bruto como array de bytes) ou `getInputStream()` (um fluxo de leitura) — e, como já vimos, também `getResource()` (Parte 6.3), que converte esse arquivo recebido diretamente em um `Resource` do Spring, pronto para ser usado em qualquer lugar que espere essa abstração mais genérica (como o construtor de `Media`).

### 6.5. Teste de integração: `GeminiTranscriptionModelIT`, explicado linha por linha

```java
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
public class GeminiTranscriptionModelIT {

    @Autowired
    private GoogleGenAiChatModel chatModel;

    @ParameterizedTest
    @CsvSource({
            "recording-1.mp3, 80 reais",
            "recording-2.mp3, 40 reais",
            "recording-3.mp3, 120 reais",
            "recording-4.mp3, 90 reais",
            "recording-5.mp3, 200 reais",
            "recording-6.mp3, 60 reais"
    })
    public void should_containExpectedKeywords_when_audioFilesAreProcessed(String fileName, String expectedKeyword) throws IOException {
        var recording = new ClassPathResource("audio/" + fileName);
        assertThat(recording.exists()).isTrue();

        var audioMedia = new Media(MimeTypeUtils.parseMimeType("audio/mpeg"), recording);

        String promptTexto = """
            Transcreva o áudio a seguir com fidelidade em português brasileiro.
            Contexto do áudio: contém descrição de gastos financeiros.
            Retorne APENAS a transcrição do áudio.
            """;

        var userMessage = UserMessage.builder().text(promptTexto).media(List.of(audioMedia)).build();
        var prompt = Prompt.builder().messages(List.of(userMessage)).build();

        var result = chatModel.call(prompt).getResult();
        assertThat(result).isNotNull();

        var output = result.getOutput();
        assertThat(output).isNotNull();

        var response = output.getText();
        assertThat(response).isNotNull().isNotEmpty();

        assertThat(response).containsIgnoringCase(expectedKeyword);
        System.out.println("Arquivo: " + fileName + " -> Transcrição: " + response);
    }
}
```

- **`@ParameterizedTest`** — em vez de escrever **seis testes** quase idênticos (um para cada arquivo de áudio de exemplo), esta anotação do JUnit 5 instrui o framework a executar o **mesmo método de teste várias vezes**, uma vez para cada linha de dados fornecida (explicada a seguir) — evitando duplicação de código de teste.
- **`@CsvSource({...})`** — a fonte de dados usada junto de `@ParameterizedTest`: cada `String` dentro das chaves representa uma **linha** de valores separados por vírgula (o formato **CSV**, *Comma-Separated Values*). Para cada linha, o JUnit injeta os valores, na ordem, como argumentos do método de teste — aqui, `fileName` recebe o nome do arquivo (por exemplo, `"recording-1.mp3"`) e `expectedKeyword` recebe a palavra-chave esperada na transcrição (`"80 reais"`).
- **`public void should_containExpectedKeywords_when_audioFilesAreProcessed(String fileName, String expectedKeyword) throws IOException`** — repare que a assinatura do método já recebe os dois parâmetros que o `@CsvSource` vai preencher a cada execução, na mesma ordem em que aparecem em cada linha do CSV. **`throws IOException`** declara que este método pode lançar essa exceção verificada (ligada a operações de entrada/saída, como ler um arquivo), sem tratá-la internamente — delegando esse tratamento para o próprio framework de testes, que sabe reportar a falha corretamente caso ela ocorra.
- **`new ClassPathResource("audio/" + fileName)`** — cria um `Resource` (mesma abstração da Parte 6.1) apontando para um arquivo dentro do **classpath** — neste caso, `src/test/resources/audio/`, onde os seis áudios de exemplo estão armazenados. `ClassPathResource` é uma das implementações concretas de `Resource`, especializada em localizar arquivos empacotados junto do próprio projeto (em oposição a, por exemplo, um arquivo em um caminho arbitrário do disco).
- **`assertThat(recording.exists()).isTrue();`** — uma verificação **defensiva**, feita **antes mesmo** de chamar a API do Gemini: confirma que o arquivo de áudio realmente existe no caminho esperado. Isso evita que uma falha por "arquivo não encontrado" (um problema de configuração do teste) seja confundida, na hora de investigar uma falha, com uma falha real de transcrição (um problema na integração com a IA) — são causas completamente diferentes, e separar essa verificação ajuda a diagnosticar rapidamente qual delas ocorreu.
- **A sequência de asserções encadeadas** (`assertThat(result).isNotNull()`, depois `assertThat(output).isNotNull()`, depois `assertThat(response).isNotNull().isNotEmpty()`) — em vez de extrair o texto final em uma única linha (como fizemos na Parte 3.4, com `.getResult().getOutput().getText()` tudo junto), este teste **quebra a cadeia em passos**, verificando a cada passo que o valor intermediário não é nulo. Essa é uma prática de teste mais robusta: se, por exemplo, `result` viesse nulo por algum motivo inesperado, o teste falharia exatamente **naquele ponto**, com uma mensagem de erro clara ("`result` era nulo"), em vez de lançar um `NullPointerException` genérico e menos informativo mais adiante, ao tentar chamar `.getOutput()` sobre um valor nulo.
- **`.containsIgnoringCase(expectedKeyword)`** — uma variante do `.contains(...)` já visto na Parte 4.3, que ignora diferenças entre maiúsculas e minúsculas ao comparar. Mais uma vez, uma asserção **flexível**, pelo mesmo motivo já discutido: a transcrição gerada por um LLM não é garantidamente idêntica, caractere por caractere, a cada execução — o modelo poderia escrever `"80 Reais"` em vez de `"80 reais"`, por exemplo, e ambas seriam transcrições corretas.

> **Observação sobre um comportamento real dos modelos de fala, útil para você saber de antemão:** tanto no curso (com Whisper) quanto na experiência prática com o Gemini, a transcrição de **números** é um ponto historicamente sensível — o modelo pode optar por escrever um valor **por extenso** ("duzentos reais") em vez de em algarismos ("200 reais"), e isso poderia fazer uma asserção mais rígida falhar mesmo diante de uma transcrição semanticamente correta. É exatamente por isso que o prompt de transcrição (Parte 6.3) é explícito ao dar contexto ao modelo sobre o domínio ("contém descrição de gastos financeiros") — essa informação extra ajuda a guiar o modelo para um formato de saída mais consistente e previsível.

### 6.6. Checkpoint da Parte 6

Confirmado no `.zip`: **não existe** nenhuma classe `TranscriptionModel`, nenhuma propriedade `spring.ai.*.audio.transcription.*` no projeto final — a rota da OpenAI/Whisper, ensinada na aula, não foi implementada, já que não existe equivalente para o Gemini. Em vez disso, `TranscriptionController.java` (cujo estado completo e final só será visto na Parte 11, já que esse arquivo acumula responsabilidades entre os Vídeos 06 e 11) já nasce usando o `GoogleGenAiChatModel` multimodal para transcrição, e os seis áudios de teste (`recording-1.mp3` a `recording-6.mp3`) estão em `src/test/resources/audio/`, validados por `GeminiTranscriptionModelIT`.

**Recapitulando:** conseguimos transformar áudio em texto sem depender de uma interface do Spring AI que simplesmente não existe para o Gemini — reaproveitando o mesmo `ChatModel` já usado desde a Parte 3, só que agora com uma mensagem multimodal. Esse mesmo raciocínio — "o Spring AI não tem uma abstração pronta para isto no Gemini, então vamos resolver de outra forma, entendendo *por que* a lacuna existe" — vai se repetir, de forma ainda mais acentuada, na Parte 7, para o problema inverso: transformar texto em áudio.


---

## Parte 7 — Sintetizando voz: o segundo ponto sem equivalente Gemini (Vídeo 07)

### Recapitulando

Já sabemos transformar áudio em texto (Parte 6). Agora vamos fechar o outro extremo do pipeline: transformar a resposta final do assistente, que é sempre texto internamente, de volta em áudio.

### Objetivo

Implementar a síntese de voz (*Text-to-Speech*, ou TTS), o passo final do pipeline completo de ponta a ponta.

> **📁 Arquivos desta etapa:**
> 1. **Criar** `src/test/java/dio/budgeting/GeminiSpeechModelIT.java` — o teste (seção 7.6), que salva o áudio gerado em um arquivo temporário para você ouvir manualmente.
> 2. **Criar** `src/main/java/dio/budgeting/TextToSpeechService.java` — o `@Service` com o SDK nativo do Gemini e a montagem do WAV (seção 7.3/7.4).
> 3. **Criar** `src/main/java/dio/budgeting/TextToSpeechController.java` — o endpoint `POST /api/synthesize` (seção 7.5), que já injeta o serviço do passo 2.
>
> Nenhuma dependência nova no `build.gradle` — o SDK `com.google.genai.Client` já veio, de forma transitiva, junto do starter do Gemini desde a Parte 1 (você já viu `google-genai-1.58.0.jar` no seu classpath, no checkpoint da Parte 1/2).

### 7.1. O caminho ensinado no curso: `TextToSpeechModel` (OpenAI)

Assim como para transcrição (Parte 6.1), o Spring AI define uma interface comum para síntese de voz:

> **⚠️ Não crie nenhum arquivo para este bloco — mesma situação da Parte 6.1: sem implementação para o Gemini.** Mostrado apenas para contexto; a solução real está na seção 7.3.

```java
public interface TextToSpeechModel extends Model<TextToSpeechPrompt, TextToSpeechResponse>, StreamingTextToSpeechModel {
    default byte[] call(String text) { ... }
    TextToSpeechResponse call(TextToSpeechPrompt prompt);
    default TextToSpeechOptions getDefaultOptions() { ... }
}
```

- **`default byte[] call(String text)`** — a forma simplificada: devolve diretamente um `byte[]` (um array de bytes) — o áudio já pronto, em algum formato binário. Diferente de `ChatModel.call(String)` (Parte 3.1), que devolvia uma `String`, aqui o retorno já precisa ser binário, já que áudio não é um tipo de dado representável diretamente como texto.
- A versão com `TextToSpeechPrompt` permite customizar detalhes como a voz utilizada, a velocidade da fala e o formato do arquivo de áudio gerado.

No momento em que essa parte do curso é gravada, os únicos provedores com suporte a esta interface no Spring AI são a **Speech API da OpenAI** e a **API da Eleven Labs** — mais uma vez, **sem suporte ao Gemini**.

### 7.2. Por que essa rota não existe no projeto Gemini

Pela mesma razão discutida em detalhe na Parte 6.2: o `spring-ai-starter-model-google-genai` não implementa `TextToSpeechModel`. O Gemini até oferece geração de áudio — mas através de um **modelo específico** dentro da própria API do Google GenAI (`gemini-2.5-flash-preview-tts`), acessado por meio de **configurações de geração de conteúdo** (a classe `GenerateContentConfig`, com uma opção `responseModalities("AUDIO")`) — um mecanismo que ainda não tem uma "porta de entrada" pronta dentro da abstração `TextToSpeechModel` do Spring AI.

### 7.3. A solução adotada: o SDK nativo `com.google.genai.Client`, explicado com cuidado

Em vez de depender de uma interface do Spring AI que simplesmente não cobre este caso, o projeto usa diretamente o **SDK Java oficial do Google GenAI** — a mesma biblioteca de baixo nível que, por baixo dos panos, o próprio `spring-ai-starter-model-google-genai` usa para implementar o `GoogleGenAiChatModel` que já conhecemos desde a Parte 3. Isso é feito de forma isolada em uma nova classe, `TextToSpeechService`.

> **Por que isso é possível — e por que não é "trapaça"?** Um *starter* do Spring AI, por baixo dos panos, sempre depende de alguma biblioteca de mais baixo nível que sabe conversar de fato com a API do provedor (autenticação, formato de requisição HTTP específico, etc.). No caso do Gemini, essa biblioteca de baixo nível é o `google-genai` (o mesmo `.jar` que apareceu no seu classpath já na Parte 1.8). O Spring AI **usa** essa biblioteca para implementar suas interfaces (`ChatModel`, e assim por diante), mas nada impede a própria aplicação de usar essa mesma biblioteca **diretamente**, para funcionalidades que o Spring AI ainda não "encapsulou" em uma interface própria — que é exatamente o caso aqui.

Vamos ver a classe inteira, e depois cada trecho em detalhe:

```java
package dio.budgeting;

import com.google.genai.Client;
import com.google.genai.types.*;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TextToSpeechService {

    private final Client geminiClient;

    public TextToSpeechService(@Value("${spring.ai.google.genai.api-key}") String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalArgumentException(
                    "A propriedade spring.ai.google.genai.api-key não foi resolvida. " +
                            "Verifique se a variável de ambiente GEMINI_API_KEY está definida.");
        }
        this.geminiClient = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    @PreDestroy
    public void close() {
        geminiClient.close();
    }

    public byte[] synthesize(String text) throws IOException {
        if (!StringUtils.hasText(text)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O texto a ser sintetizado não pode ser vazio.");
        }

        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseModalities("AUDIO")
                .speechConfig(SpeechConfig.builder()
                        .voiceConfig(VoiceConfig.builder()
                                .prebuiltVoiceConfig(PrebuiltVoiceConfig.builder()
                                        .voiceName("Kore")
                                        .build())
                                .build())
                        .build())
                .build();

        GenerateContentResponse response = geminiClient.models.generateContent(
                "gemini-2.5-flash-preview-tts",
                text,
                config
        );

        List<Part> parts = response.candidates()
                .flatMap(candidates -> candidates.stream().findFirst())
                .flatMap(Candidate::content)
                .flatMap(Content::parts)
                .orElse(new ArrayList<>());

        byte[] pcmAudio = parts.stream()
                .map(part -> part.inlineData().flatMap(Blob::data))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nenhum áudio retornado pelo Gemini"));

        return wrapPcmAsWav(pcmAudio, 24000, 1, 16);
    }
    // ... wrapPcmAsWav explicado na seção 7.4
}
```

Agora, linha por linha:

- **`@Service`** — uma anotação de **estereótipo** do Spring (assim como `@RestController`, mas com uma intenção semântica diferente: marca esta classe como um componente de **lógica de negócio/serviço**, e não de exposição web). Assim como `@RestController`, ela participa do `@ComponentScan` (Parte 1.3), tornando a classe elegível para injeção de dependência em outras classes — que é exatamente como o `TranscriptionController`, na Parte 11, vai obter uma instância dela.
- **`private final Client geminiClient;`** — o campo que vai guardar o cliente de baixo nível do SDK do Google GenAI, seguindo o mesmo padrão de campo `private final` já visto desde a Parte 3.6.
- **`public TextToSpeechService(@Value("${spring.ai.google.genai.api-key}") String apiKey) { ... }`** — o construtor, recebendo a chave de API. Repare na anotação **`@Value`**, aplicada diretamente sobre o **parâmetro** do construtor — algo diferente de tudo que vimos até agora (injeção de *beans* completos, via `@Autowired` ou via construtor sem anotação).

  > **O que é `@Value`, explicado do zero, e por que ela é diferente de `@Autowired`?** Enquanto `@Autowired` (ou a injeção implícita via construtor, já vista) serve para injetar um **objeto/*bean*** gerenciado pelo Spring, `@Value` serve para injetar o **valor de uma propriedade de configuração** — um dado simples (`String`, número, booleano), lido diretamente do `application.properties` (ou de uma variável de ambiente, através da mesma sintaxe `${...}` já vista na Parte 1.7). Aqui, `@Value("${spring.ai.google.genai.api-key}")` reaproveita a **mesma propriedade** já configurada para o `GoogleGenAiChatModel` (Parte 1.7) — evitando duplicar a leitura da variável de ambiente `GEMINI_API_KEY` em dois lugares diferentes do código.
- **A validação defensiva logo no início do construtor** (`if (!StringUtils.hasText(apiKey)) { throw new IllegalArgumentException(...); }`) — merece atenção especial:
  - **`StringUtils.hasText(apiKey)`** — um método utilitário do próprio Spring que verifica, de uma vez, três condições: que a *string* não é `null`, que não é vazia (`""`), e que não contém apenas espaços em branco. É mais completo do que simplesmente checar `apiKey != null`, que deixaria passar, por exemplo, uma *string* vazia.
  - **`throw new IllegalArgumentException("...")`** — se a validação falhar, a classe **lança uma exceção imediatamente**, ainda durante a construção do *bean* (ou seja, logo na inicialização da aplicação), com uma mensagem clara explicando exatamente qual configuração está faltando e o que fazer para corrigi-la.
  - **Por que fazer essa validação logo aqui, em vez de deixar o erro acontecer "naturalmente" mais tarde?** Este é um princípio de design chamado **"falhar rápido"** (*fail fast*): é preferível que um problema de configuração seja detectado o mais cedo possível (aqui, na inicialização da aplicação), com uma mensagem clara e específica, do que deixar a aplicação subir normalmente e só falhar, de forma confusa, na primeira vez que alguém tentasse de fato sintetizar um áudio — nesse cenário mais tardio, o erro provavelmente viria de dentro do SDK do Google, com uma mensagem técnica menos óbvia de relacionar à causa real (a variável de ambiente ausente).
- **`Client.builder().apiKey(apiKey).build();`** — cria o cliente de baixo nível do SDK do Google GenAI (mais uma vez, o padrão Builder já familiar), passando a chave de API validada. Este `Client` é o mesmo tipo de objeto que realiza, por baixo dos panos, a comunicação HTTP real com a API do Gemini — só que, aqui, instanciado e gerenciado **manualmente** pela nossa própria aplicação, em vez de ser criado automaticamente pela auto-configuração do Spring AI (como acontece com o `GoogleGenAiChatModel`).
- **`@PreDestroy public void close() { geminiClient.close(); }`** — a anotação **`@PreDestroy`** (do pacote padrão `jakarta.annotation`, não específica do Spring) marca um método para ser **chamado automaticamente pelo Spring** um pouco antes de este *bean* ser destruído — por exemplo, quando a aplicação está sendo encerrada de forma controlada. Aqui, garante que os recursos internos do `geminiClient` (conexões de rede abertas, *buffers*, etc.) sejam liberados corretamente ao encerrar, evitando o que se chama de **vazamento de recursos** (*resource leak* — recursos do sistema que ficam "presos", sem serem liberados, mesmo depois de não serem mais necessários).
- **`public byte[] synthesize(String text) throws IOException { ... }`** — o método principal da classe, que efetivamente converte um texto em áudio. Primeiro, a mesma validação defensiva já vista no construtor (agora checando o **texto** recebido, não mais a chave de API), mas desta vez lançando uma exceção diferente:
  - **`throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "...")`** — diferente do `IllegalArgumentException` do construtor (que ocorre na inicialização, sem relação direta com uma requisição HTTP específica), `ResponseStatusException` é uma exceção do próprio Spring Web, pensada para ser lançada dentro do fluxo de tratamento de uma requisição — quando capturada pelo mecanismo interno do Spring, ela automaticamente traduz-se em uma resposta HTTP com o código de status informado (`HttpStatus.BAD_REQUEST`, ou seja, `400`, o código padrão para "a requisição do cliente está malformada ou inválida") e a mensagem como corpo da resposta.
- **A cadeia de `GenerateContentConfig`, `SpeechConfig`, `VoiceConfig`, `PrebuiltVoiceConfig`** — quatro *builders* **aninhados** (uns dentro dos outros), todos do próprio SDK do Google (não do Spring AI), que juntos configuram exatamente como a chamada de TTS deve se comportar:
  - **`.responseModalities("AUDIO")`** — instrui explicitamente a API a devolver a resposta como **áudio**, em vez do comportamento padrão do Gemini (devolver texto).
  - **`.voiceName("Kore")`** — seleciona, entre as vozes pré-definidas (*prebuilt voices*) oferecidas pela API de TTS do Gemini, qual usar para gerar a fala. `"Kore"` é apenas uma dessas opções — o Gemini oferece outras vozes com timbres diferentes, que poderiam ser trocadas aqui livremente (um dos "próximos passos" possíveis, discutido ao final deste tutorial).
- **`geminiClient.models.generateContent("gemini-2.5-flash-preview-tts", text, config)`** — a chamada de fato à API, informando três coisas: o **modelo específico** de TTS do Gemini a usar, o **texto** a converter em fala, e a **configuração** montada acima.
- **A cadeia de extração do áudio, usando `Optional` encadeado** — este é o trecho mais denso da classe, e vale desmembrar com calma:

  > **O que é `Optional`, explicado do zero?** `Optional<T>` é um tipo do Java, introduzido para representar, de forma explícita e segura, "um valor que pode existir ou pode não existir" — em vez de simplesmente devolver `null` quando um valor está ausente (o que historicamente é a causa mais comum de `NullPointerException` em Java), um método pode devolver um `Optional<T>` vazio. Isso obriga quem recebe o valor a **lidar explicitamente** com a possibilidade de ausência, em vez de assumir (às vezes erroneamente) que o valor sempre existirá.

  ```java
  List<Part> parts = response.candidates()
          .flatMap(candidates -> candidates.stream().findFirst())
          .flatMap(Candidate::content)
          .flatMap(Content::parts)
          .orElse(new ArrayList<>());
  ```
  - `response.candidates()` devolve um `Optional` contendo, se presente, a lista de "candidatos" de resposta gerados pela API (o Gemini pode, em teoria, gerar mais de uma alternativa de resposta por chamada).
  - **`.flatMap(...)`** — um método de `Optional` que permite **encadear** uma nova operação que também devolve um `Optional`, "achatando" o resultado (em vez de terminar com um `Optional<Optional<X>>`, aninhado e incômodo de usar, `flatMap` produz diretamente um `Optional<X>`). Aqui, cada `.flatMap(...)` avança um nível na estrutura profundamente aninhada da resposta: da lista de candidatos, para o primeiro candidato (`candidates.stream().findFirst()`); do candidato, para seu conteúdo (`Candidate::content` — uma **referência a método**, uma forma abreviada de escrever `candidate -> candidate.content()`); do conteúdo, para suas partes (`Content::parts`).
  - **`.orElse(new ArrayList<>())`** — se, em **qualquer** ponto dessa cadeia, algum `Optional` intermediário estivesse vazio (por exemplo, se não houvesse nenhum candidato), o resultado final seria uma lista vazia, em vez de lançar uma exceção — um comportamento seguro e previsível diante de uma resposta inesperada da API.
  - **Por que essa navegação é tão "profunda"?** Porque é assim que a própria API do Google GenAI estrutura sua resposta: uma resposta (`GenerateContentResponse`) contém uma lista de candidatos (`Candidate`); cada candidato tem um conteúdo (`Content`); cada conteúdo tem uma lista de partes (`Part`) — e é dentro de uma dessas partes que o áudio efetivamente gerado está.

  ```java
  byte[] pcmAudio = parts.stream()
          .map(part -> part.inlineData().flatMap(Blob::data))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .findFirst()
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nenhum áudio retornado pelo Gemini"));
  ```
  - **`parts.stream()`** — converte a lista de partes em um `Stream`, a API funcional de processamento de coleções do Java (já mencionada de leve na Parte 9, e explicada com mais detalhe ali).
  - **`.map(part -> part.inlineData().flatMap(Blob::data))`** — para cada parte, tenta extrair seus dados binários embutidos (*inline data*, um `Blob` — "objeto binário grande", o termo genérico para um bloco de dados binários, como um áudio ou uma imagem, embutido diretamente em uma estrutura de dados). O resultado de cada item, neste ponto, ainda é um `Optional<byte[]>` — já que nem toda parte necessariamente contém dados binários (algumas poderiam ser texto, por exemplo).
  - **`.filter(Optional::isPresent)`** — mantém, no *stream*, apenas os itens cujo `Optional` **não** está vazio (ou seja, que de fato continham dados).
  - **`.map(Optional::get)`** — "desembrulha" cada `Optional` restante, extraindo o `byte[]` de dentro dele.
  - **`.findFirst()`** — pega o primeiro item da lista já filtrada — devolvendo, mais uma vez, um `Optional<byte[]>` (vazio, se a lista estivesse vazia).
  - **`.orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "..."))`** — se **nenhuma** parte continha áudio (um cenário inesperado, mas possível — por exemplo, se a API do Gemini tivesse uma falha momentânea), lança uma exceção HTTP `500` (`INTERNAL_SERVER_ERROR`, "erro interno do servidor" — diferente do `400` visto antes, já que este erro não é culpa de quem fez a requisição, mas de algo que deu errado do lado do servidor/provedor), com uma mensagem clara sobre a causa.
- **`return wrapPcmAsWav(pcmAudio, 24000, 1, 16);`** — finalmente, os bytes de áudio extraídos (ainda em formato bruto, chamado PCM) são passados para um método auxiliar que os transforma em um arquivo `.wav` de verdade — explicado a seguir, na seção 7.4.

### 7.4. O problema do PCM cru e a montagem manual do cabeçalho WAV, explicado do zero

O áudio devolvido pela API do Gemini não é um arquivo `.wav` ou `.mp3` já pronto e reproduzível — é **PCM cru**.

> **O que é PCM, explicado do zero?** PCM (*Pulse Code Modulation*, "modulação por código de pulso") é a representação digital **mais básica e direta** possível de uma onda sonora: uma sequência de números, cada um representando a **amplitude** (a "força" da onda sonora) medida em um instante específico do tempo, a intervalos regulares (a **taxa de amostragem**, explicada a seguir). É a forma "crua" de áudio digital, sem nenhuma compressão, sem nenhum metadado sobre como interpretar esses números — apenas a sequência de amostras em si. Formatos como `.wav` são, essencialmente, dados PCM **precedidos por um cabeçalho** que descreve como interpretá-los (quantos canais, qual taxa de amostragem, etc.); formatos como `.mp3` aplicam, além disso, algoritmos de compressão que reduzem o tamanho do arquivo às custas de alguma perda de qualidade.

Para que este áudio PCM possa ser salvo em um arquivo `.wav` reproduzível por qualquer player de áudio comum, é preciso **construir manualmente**, byte a byte, o cabeçalho que o formato WAV exige:

```java
private static byte[] wrapPcmAsWav(byte[] pcmData, int sampleRate, int channels, int bitsPerSample)
        throws IOException {
    int byteRate = sampleRate * channels * bitsPerSample / 8;
    int blockAlign = channels * bitsPerSample / 8;
    int dataSize = pcmData.length;

    ByteBuffer header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN);
    header.put("RIFF".getBytes());
    header.putInt(36 + dataSize);
    header.put("WAVE".getBytes());
    header.put("fmt ".getBytes());
    header.putInt(16);
    header.putShort((short) 1);
    header.putShort((short) channels);
    header.putInt(sampleRate);
    header.putInt(byteRate);
    header.putShort((short) blockAlign);
    header.putShort((short) bitsPerSample);
    header.put("data".getBytes());
    header.putInt(dataSize);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(header.array());
    out.write(pcmData);
    return out.toByteArray();
}
```

> **O formato WAV, explicado do zero.** WAV é, na essência, um cabeçalho fixo de **44 bytes**, seguido diretamente pelos dados de áudio PCM brutos. Esse cabeçalho segue a especificação **RIFF** (*Resource Interchange File Format*, "formato de intercâmbio de recursos") — um formato genérico de "contêiner" (uma estrutura que organiza diferentes blocos de dados dentro de um único arquivo) usado por vários tipos de arquivo multimídia, do qual o WAV é um dos exemplos mais conhecidos.

- **`private static byte[] wrapPcmAsWav(byte[] pcmData, int sampleRate, int channels, int bitsPerSample)`** — um método `private` (só usado internamente por esta classe) e `static` (não depende de nenhum estado de uma instância específica de `TextToSpeechService` — é uma função pura, que sempre produz o mesmo resultado para as mesmas entradas).
- **`int byteRate = sampleRate * channels * bitsPerSample / 8;`** e **`int blockAlign = channels * bitsPerSample / 8;`** — dois valores **derivados matematicamente** dos três parâmetros de entrada, exigidos pelo próprio cabeçalho WAV para que qualquer player consiga calcular corretamente a duração e a velocidade de reprodução do áudio (a divisão por `8` converte de **bits** para **bytes**, já que 1 byte equivale a 8 bits).
- **`int dataSize = pcmData.length;`** — o tamanho, em bytes, dos dados de áudio brutos recebidos — necessário para preencher um dos campos do cabeçalho, informando quantos bytes de áudio vêm depois dele.
- **`ByteBuffer header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN);`** — cria um `ByteBuffer` (uma classe do Java, do pacote `java.nio`, para manipular sequências de bytes de forma estruturada) com espaço reservado para exatamente **44 bytes** (o tamanho fixo do cabeçalho WAV), e define a **ordem de bytes** a ser usada ao escrever valores que ocupam mais de um byte (como inteiros de 32 bits).

  > **O que é `ByteOrder`/*endianness*, explicado do zero?** Quando um número ocupa mais de um byte (por exemplo, um inteiro de 32 bits ocupa 4 bytes), existem duas convenções possíveis para a **ordem** em que esses bytes são gravados em sequência: **big-endian** (o byte **mais** significativo primeiro — a convenção "intuitiva", como escrevemos números por extenso) e **little-endian** (o byte **menos** significativo primeiro). Diferentes formatos de arquivo e diferentes arquiteturas de processador escolhem convenções diferentes por razões históricas. A especificação do formato WAV **exige** little-endian — se a ordem estivesse errada (usando big-endian, por exemplo), o arquivo resultante seria interpretado de forma completamente incorreta por qualquer player, mesmo que os bytes individuais estivessem "certos".
- As chamadas **`header.put(...)`**, **`header.putInt(...)`** e **`header.putShort(...)`** — cada uma escreve, na posição atual do buffer (que avança automaticamente a cada escrita), um valor de um tamanho específico: `.put(byte[])` escreve uma sequência de bytes diretamente (usada aqui para escrever os identificadores de texto fixo do cabeçalho); `.putInt(int)` escreve um inteiro de 32 bits (4 bytes); `.putShort(short)` escreve um inteiro "curto" de 16 bits (2 bytes) — os *casts* `(short) 1` e similares convertem explicitamente um valor `int` (o tipo padrão de um literal numérico em Java) para `short`, já que o método espera esse tipo específico.
  - **`"RIFF"`, `"WAVE"`, `"fmt "`, `"data"`** — identificadores de texto fixo (chamados de *chunk IDs*, "identificadores de bloco"), exigidos literalmente pela especificação RIFF/WAV, marcando o início de cada seção estruturada dentro do cabeçalho. `.getBytes()`, chamado sobre cada uma dessas *strings*, converte o texto em sua representação de bytes (usando a codificação padrão do sistema, que para caracteres ASCII simples como estes é sempre consistente).
  - **`header.putInt(36 + dataSize);`** — o tamanho total do arquivo, **menos 8 bytes** (os dois primeiros campos do cabeçalho RIFF, por convenção da própria especificação, não entram nessa contagem específica). O valor `36` é o tamanho fixo do restante do cabeçalho (44 bytes totais, menos os 8 já excluídos).
  - **`header.putShort((short) 1);`** logo depois do bloco `"fmt "` — o **código de formato de áudio**: o valor `1` significa especificamente PCM **não comprimido** (o formato mais simples e universalmente suportado; outros códigos existiriam para formatos comprimidos, não usados aqui).
  - **`header.putShort((short) channels);`**, **`header.putInt(sampleRate);`**, **`header.putInt(byteRate);`**, **`header.putShort((short) blockAlign);`**, **`header.putShort((short) bitsPerSample);`** — cada um dos parâmetros técnicos já discutidos, gravados na ordem exata que a especificação WAV exige.
- **`ByteArrayOutputStream out = new ByteArrayOutputStream();`** — cria um fluxo de saída **em memória** (não escreve em um arquivo do disco, apenas acumula bytes em um *buffer* interno), útil para montar um array de bytes final a partir de múltiplas escritas.
- **`out.write(header.array());`** — escreve todo o cabeçalho de 44 bytes já montado.
- **`out.write(pcmData);`** — escreve, logo em seguida, todos os dados de áudio PCM brutos originalmente recebidos.
- **`return out.toByteArray();`** — converte tudo o que foi acumulado no fluxo (cabeçalho + dados) em um único array de bytes — este é, finalmente, o conteúdo completo e válido de um arquivo `.wav`.

Este cálculo específico (**`24000`** Hz de taxa de amostragem, **`1`** canal — mono, **`16`** bits por amostra) não é algo que a aplicação descobre dinamicamente a partir da resposta da API — são valores **fixos e documentados** pela própria API de TTS do Gemini, para este modelo específico (`gemini-2.5-flash-preview-tts`).

### 7.5. `TextToSpeechController`: expondo a síntese via HTTP

```java
package dio.budgeting;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class TextToSpeechController {

    private final TextToSpeechService textToSpeechService;

    public TextToSpeechController(TextToSpeechService textToSpeechService) {
        this.textToSpeechService = textToSpeechService;
    }

    @PostMapping(value = "/synthesize", produces = "audio/wav")
    public ResponseEntity<Resource> synthesize(@RequestBody SynthesizeRequest request) throws IOException {
        byte[] wavAudio = textToSpeechService.synthesize(request.text());
        var resource = new ByteArrayResource(wavAudio);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("audio.wav")
                                .build()
                                .toString())
                .body(resource);
    }

    public record SynthesizeRequest(String text) {
    }
}
```

- **`private final TextToSpeechService textToSpeechService;`** e injeção via construtor — o mesmo padrão de sempre (Parte 3.6), agora injetando o `@Service` construído na seção 7.3. Repare que toda a complexidade de configuração do Gemini, extração de áudio e montagem do WAV **não está aqui, no controller** — ela foi propositalmente **extraída** para o `TextToSpeechService`. Essa separação de responsabilidades tem um motivo concreto que só fica claro na Parte 11: **duas rotas diferentes** da aplicação (`/api/synthesize`, aqui, e `/api/ai`, o fluxo completo de voz para voz) precisam sintetizar áudio — se essa lógica estivesse duplicada em dois controllers, qualquer ajuste futuro (trocar a voz, mudar o modelo) precisaria ser replicado em dois lugares, um convite a inconsistências.
- **`@PostMapping(value = "/synthesize", produces = "audio/wav")`** — o atributo **`produces`** (diferente de `consumes`, visto na Parte 6.4) declara o tipo de conteúdo (`Content-Type`) que **este endpoint devolve** na resposta — aqui, `"audio/wav"`, coerente com o formato produzido por `wrapPcmAsWav(...)` no serviço.
- **`@RequestBody SynthesizeRequest request`** — assim como em outros endpoints `POST` que veremos (Parte 10), este parâmetro é desserializado a partir do corpo JSON da requisição.
- **`record SynthesizeRequest(String text) { }`** — um **record** (conceito que será formalmente explicado, com todos os detalhes, na Parte 8.3 — adiantando: uma forma compacta do Java para declarar uma classe imutável de dados) usado como o formato esperado do corpo da requisição: um JSON simples com um único campo, `{"text": "..."}`.
- **`ByteArrayResource resource = new ByteArrayResource(wavAudio);`** — uma implementação concreta de `Resource` (mesma abstração da Parte 6.1) construída diretamente a partir de um array de bytes já em memória, sem depender de nenhum arquivo físico salvo em disco.
- **`ContentDisposition.attachment().filename("audio.wav").build()`** — monta o valor do cabeçalho HTTP `Content-Disposition: attachment; filename="audio.wav"`, que instrui o cliente (navegador, ferramenta de requisição HTTP) a tratar a resposta recebida como um **arquivo para salvar/baixar**, sugerindo o nome `audio.wav`, em vez de tentar exibir o conteúdo diretamente na tela.
- **`ResponseEntity.ok().header(...).body(resource)`** — constrói a resposta HTTP completa de forma explícita: `.ok()` define o código de status `200`; `.header(...)` adiciona o cabeçalho de `Content-Disposition` recém-montado; `.body(resource)` define o `Resource` como corpo da resposta — o Spring, ao ver o tipo de retorno `ResponseEntity<Resource>` combinado com `produces = "audio/wav"`, sabe automaticamente como escrever esse recurso como bytes brutos no corpo da resposta.

### 7.6. Teste de integração: `GeminiSpeechModelIT`

O teste correspondente repete, de forma independente, a mesma lógica de configuração e o mesmo `wrapPcmAsWav` do `TextToSpeechService` (este teste antecede a extração do serviço, dentro da linha do tempo do desenvolvimento), mas salva o resultado em um **arquivo temporário**, para conferência manual e auditiva:

```java
Path tempFile = Files.createTempFile("AUDIO_", ".wav");
Files.write(tempFile, wavAudio);
System.out.println(tempFile.toAbsolutePath());
```

- **`Files.createTempFile("AUDIO_", ".wav")`** — um método utilitário do Java (`java.nio.file.Files`) que cria um arquivo temporário no diretório padrão do sistema operacional para esse fim (em Linux, tipicamente `/tmp`), com um nome único gerado automaticamente pelo sistema (combinando o prefixo `AUDIO_` e o sufixo `.wav` fornecidos com uma parte aleatória, garantindo que não colida com nenhum outro arquivo).
- **`Files.write(tempFile, wavAudio)`** — escreve o array de bytes do áudio já montado (com o cabeçalho WAV) nesse arquivo temporário recém-criado.
- Ao rodar este teste manualmente e **abrir o caminho impresso no console** (por exemplo, arrastando-o para um player de áudio, ou abrindo pelo gerenciador de arquivos), é possível **ouvir de fato** o áudio sintetizado, e conferir se a fala corresponde corretamente ao texto enviado — um passo de validação **manual e auditiva**, complementar às asserções automáticas do teste (como `assertThat(pcmAudio).hasSizeGreaterThan(1024)`, que apenas confirma que *algum* áudio de tamanho razoável foi recebido, sem validar seu conteúdo real).

### 7.7. Checkpoint da Parte 7

Confirmado no `.zip`: `TextToSpeechService.java` existe como `@Service`, encapsulando o SDK nativo do Gemini e o processo de *wrap* PCM → WAV; `TextToSpeechController.java` existe com o endpoint `POST /api/synthesize` (produzindo `audio/wav` — diferente de `audio/mp3`, como seria no protótipo original baseado em OpenAI); `GeminiSpeechModelIT.java` valida a geração de áudio de forma independente, salvando em arquivo temporário para audição manual.

**Recapitulando:** com esta parte concluída, temos os **três blocos individuais** do pipeline funcionando de forma isolada e testada: transcrever áudio (Parte 6), delegar ações reais a métodos Java a partir de uma conversa (Parte 5, ainda em um exemplo didático), e sintetizar voz (esta parte). Falta ainda uma peça central antes de juntar tudo: dar ao Tool Calling algo de verdade para fazer — o **domínio de negócio** do projeto (transações financeiras), construído a partir da Parte 8.


---

## Parte 8 — O domínio do negócio: `Transaction`, `Category` e o primeiro caso de uso (Vídeo 08)

### Recapitulando

Até aqui, o projeto sabia conversar com o Gemini, transcrever áudio e sintetizar voz — mas ainda não tinha nenhuma noção do que é, de fato, uma "transação financeira" dentro da aplicação. Esta parte constrói essa representação, junto da primeira operação real de negócio: persistir uma transação.

### Objetivo

Dar ao projeto uma representação própria do domínio de negócio (o que é uma transação, quais categorias existem), organizada em camadas bem definidas, e o primeiro **caso de uso** real, que também será a primeira *tool* de verdade (não mais um exemplo didático como `MathTools`, da Parte 5).

> **📁 Arquivos desta etapa — a partir daqui, os pacotes `domain`, `application` e `infrastructure` nascem pela primeira vez.** Crie os quatro subpacotes primeiro (pastas vazias, se sua IDE exigir), e depois os arquivos, **nesta ordem** (cada um depende apenas dos anteriores, nunca dos seguintes — por isso a ordem importa):
> 1. **Criar pacote** `src/main/java/dio/budgeting/domain/` (novo).
> 2. **Criar** `domain/TransactionId.java` (seção 8.2) — não depende de mais nada além da biblioteca padrão do Java.
> 3. **Criar** `domain/Category.java` (seção 8.3) — idem.
> 4. **Criar** `domain/Transaction.java` (seção 8.4) — depende de `TransactionId` e `Category`, já criados.
> 5. **Criar** `domain/TransactionRepository.java` (seção 8.6) — depende de `Transaction` e `Category`.
> 6. **Criar pacote** `src/main/java/dio/budgeting/application/` e os subpacotes `application/input/` e `application/output/` (novos).
> 7. **Criar** `application/input/PersistTransactionInput.java` (seção 8.8) — depende de `Category`.
> 8. **Criar** `application/output/TransactionOutput.java` (seção 8.8) — depende de `Transaction`.
> 9. **Criar** `application/PersistTransactionUseCase.java` (seção 8.7) — depende de todos os anteriores.
> 10. **Editar** `build.gradle` — adicionar o plugin `io.freefair.lombok` (seção 8.5), necessário para `Transaction` compilar (ela usa `@Getter`/`@AllArgsConstructor`). Faça isso **antes** de escrever `Transaction.java`, ou sua IDE vai acusar erro nessas anotações por não reconhecê-las.
> 11. **Criar pacote** `src/main/java/dio/budgeting/infrastructure/` — apenas o pacote vazio por enquanto, como marcador de que a próxima camada (persistência real) vem na Parte 9. Nenhum arquivo dentro dele ainda.
>
> Não existe teste de integração automatizado nesta Parte (nenhum sufixo `IT` novo) — a "prova" de que o domínio está correto, por enquanto, é o projeto compilar sem erros. A verificação funcional de verdade só acontece na Parte 9, quando `TransactionRepository` finalmente tiver uma implementação real para testar.

### 8.1. Domain-Driven Design e Clean Architecture, explicados do zero

A partir desta parte, o projeto passa a organizar o código Java em três pacotes dentro de `dio.budgeting`, cada um com uma responsabilidade clara e isolada:

- **`domain`** — as regras e entidades **centrais** do negócio: o que é uma transação, quais categorias existem, e o **contrato** de como uma transação deve poder ser persistida — sem absolutamente nenhum detalhe técnico de "como" isso é feito na prática (nada de SQL, nada de anotações de banco de dados).
- **`application`** — os **casos de uso** (*use cases*, explicados a seguir): as ações concretas que a aplicação sabe realizar, como "persistir uma transação" ou "listar transações de uma categoria".
- **`infrastructure`** — as implementações técnicas concretas — como, de fato, acessar um banco de dados (Parte 9), ou expor um endpoint HTTP tradicional (Parte 10).

> **O que é um "caso de uso" (*use case*), explicado do zero?** Um caso de uso representa **uma ação específica e completa** que a aplicação sabe executar, do ponto de vista de quem a utiliza — por exemplo, "persistir uma nova transação" é um caso de uso; "listar transações de uma categoria" é outro. Organizar o código em torno de casos de uso (em vez de, por exemplo, apenas em torno de entidades de banco de dados) deixa mais evidente **o que a aplicação faz de fato**, e facilita testar e entender cada ação isoladamente.

Essa separação em três pacotes é a aplicação prática de dois conceitos de arquitetura de software bastante conhecidos, que vale a pena conhecer pelo nome:

- **Domain-Driven Design (DDD)**, ou "Design Orientado a Domínio" — uma abordagem de projeto de software em que o código é organizado **em torno do domínio do negócio** (aqui, "transações financeiras e suas categorias"), mantendo essas regras isoladas de detalhes técnicos de infraestrutura (banco de dados, frameworks web, etc.), para que mudanças em um lado não obriguem mudanças no outro.
- **Clean Architecture** ("Arquitetura Limpa") — um estilo de arquitetura, popularizado pelo autor Robert C. Martin, organizado em **camadas concêntricas**, em que camadas mais internas (o domínio) **nunca dependem** de camadas mais externas (a infraestrutura) — é sempre o contrário: o domínio define **apenas o quê** precisa ser feito (através de uma interface, como veremos na seção 8.7), e é a infraestrutura quem fornece **o como** (a implementação concreta, na Parte 9). É exatamente essa "inversão" que torna possível, em teoria, trocar completamente o banco de dados usado sem alterar **nenhuma** linha de regra de negócio.

  > **Por que essa inversão importa na prática, e não é só "elegância acadêmica"?** Porque ela reduz o **acoplamento** — a dependência de uma parte do sistema em relação aos detalhes internos de outra parte. Se `PersistTransactionUseCase` (que veremos na seção 8.8) dependesse diretamente de uma classe JPA/Hibernate específica, qualquer mudança na forma de persistência (trocar de MySQL para outro banco, por exemplo, ou até para um armazenamento totalmente diferente) exigiria alterar o caso de uso também. Como ele depende apenas de uma **interface** de domínio (`TransactionRepository`, seção 8.7), essa troca fica isolada inteiramente dentro da camada `infrastructure`.

### 8.2. `TransactionId`: um identificador fortemente tipado, explicado do zero

```java
package dio.budgeting.domain;

import java.util.UUID;

public record TransactionId(UUID uuid) {
    public TransactionId() {
        this(UUID.randomUUID());
    }
}
```

- **`record TransactionId(UUID uuid)`** — a primeira aparição, neste tutorial, da palavra-chave **`record`**, que já foi mencionada de leve na Parte 7.5, mas merece agora sua explicação completa.

  > **O que é um `record`, explicado do zero, passo a passo?** Um `record` é um recurso da linguagem Java (disponível de forma estável desde a versão 16) para declarar, de forma extremamente compacta, uma classe **imutável** focada em apenas carregar dados — sem a necessidade de escrever manualmente todo o código repetitivo (chamado de *boilerplate*, "modelo padrão"/"código repetitivo") que uma classe assim normalmente exigiria. Ao escrever `record TransactionId(UUID uuid)`, o compilador Java gera **automaticamente**, nos bastidores:
  > - um **construtor** que recebe exatamente os componentes declarados (aqui, um `UUID`);
  > - um **método de acesso** para cada componente, chamado pelo **mesmo nome** do componente, mas **sem** o prefixo `get` tradicional (por exemplo, `uuid()`, não `getUuid()`) — uma diferença de convenção importante em relação a classes Java "tradicionais";
  > - implementações corretas e automáticas de `equals()` (compara se dois objetos representam o mesmo valor), `hashCode()` (gera um código numérico consistente com `equals()`, usado internamente por estruturas como `HashMap`) e `toString()` (uma representação textual legível do objeto, útil para depuração/logs);
  > - e, crucialmente, os campos internos de um `record` são **sempre `final`** — ou seja, **imutáveis**: uma vez criado um `TransactionId`, o valor de `uuid` dentro dele nunca pode ser alterado.
  >
  > Tudo isso, sem escrever uma única linha a mais do que `record TransactionId(UUID uuid) { }`.
- **`UUID`** (do pacote `java.util`, parte da biblioteca padrão do Java) — sigla para *Universally Unique Identifier* ("Identificador Universalmente Único"). É um valor de **128 bits**, gerado (tipicamente) a partir de uma combinação de aleatoriedade e/ou informações do sistema, com uma probabilidade de colisão (dois `UUID`s diferentes acabarem sendo iguais por acaso) tão baixa que, na prática, é considerada desprezível — mesmo gerando bilhões deles. Isso o torna ideal para identificar registros de forma única **sem depender de um contador central** (como um número sequencial gerenciado por um único banco de dados, chamado de `AUTO_INCREMENT`), o que seria um ponto único de falha e de contenção em sistemas mais distribuídos.
- **Identificador fortemente tipado (*strongly-typed ID*), explicado do zero.** Em vez de representar o identificador de uma transação simplesmente como uma `String` ou um `UUID` "solto" circulando pelo código (o que também funcionaria, tecnicamente), o projeto cria um **tipo próprio** só para esse propósito: `TransactionId`. A vantagem: o **compilador Java passa a impedir**, automaticamente, que o identificador de uma transação seja confundido, por engano, com o identificador de qualquer outra entidade que o projeto viesse a ter no futuro (o id de um usuário, por exemplo) — mesmo que, por baixo dos panos, ambos fossem representados apenas como um `UUID`. Um método que espera um parâmetro `TransactionId` simplesmente **não compila** se você tentar passar, por engano, o `UUID` de outra coisa qualquer.
- **`public TransactionId() { this(UUID.randomUUID()); }`** — um **segundo construtor**, escrito manualmente, sem nenhum parâmetro. Isso é necessário porque o construtor **gerado automaticamente** pelo `record` (o que recebe um `UUID` já pronto) é obrigatório informar um valor — não haveria, por padrão, uma forma de criar um `TransactionId` **novo**, com um identificador recém-gerado, sem esse construtor extra.
  - **`this(UUID.randomUUID())`** — a sintaxe `this(...)`, quando é a **primeira instrução** dentro de um construtor, chama **outro construtor da mesma classe** (aqui, o construtor gerado automaticamente pelo `record`, que recebe um `UUID`), repassando o valor calculado — neste caso, `UUID.randomUUID()`, um método estático da própria classe `UUID` que gera um novo identificador aleatório, a cada chamada.

### 8.3. `Category`: um `enum` para valores fixos e conhecidos, explicado do zero

```java
package dio.budgeting.domain;

public enum Category {
    GROCERIES,
    PHARMA,
    AUTO
}
```

> **O que é um `enum`, explicado do zero?** `enum` (abreviação de *enumeration*, "enumeração") é um tipo especial do Java para representar um conjunto **fixo, pequeno e conhecido de antemão** de valores possíveis. A diferença crucial em relação a uma `String` livre: se `Category` fosse simplesmente uma `String`, qualquer texto seria tecnicamente aceito onde uma categoria é esperada — incluindo erros de digitação (`"Groceriess"`, por exemplo) ou valores completamente sem sentido para o domínio (`"BananaDePijamas"`). Com um `enum`, o **próprio compilador Java** garante, já durante a compilação (antes mesmo de rodar o programa), que apenas um dos valores explicitamente declarados (`GROCERIES`, `PHARMA`, `AUTO`) pode ser usado em qualquer lugar do código que espere um valor do tipo `Category` — eliminando uma categoria inteira de erros possíveis, de forma automática.

`GROCERIES`, `PHARMA`, `AUTO` são as três categorias suportadas nesta versão do projeto — respectivamente, mercado/compras do dia a dia, farmácia, e gastos relacionados a veículo/automóvel. Essa lista fixa é o primeiro candidato natural de expansão futura do projeto (assunto retomado na seção de Próximos Passos, ao final deste tutorial).

### 8.4. `Transaction`: a entidade de domínio, explicada do zero

```java
package dio.budgeting.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Transaction {
    private TransactionId id;
    private String description;
    private double amount;
    private Category category;

    public Transaction(String description, double amount, Category category) {
        this.id = new TransactionId();
        this.description = description;
        this.amount = amount;
        this.category = category;
    }
}
```

- **`private TransactionId id;`**, **`private String description;`**, **`private double amount;`**, **`private Category category;`** — quatro campos privados, representando o estado completo de uma transação. Repare que `Transaction`, diferente de `TransactionId` e `Category`, **não é um `record`** — é uma classe Java tradicional. Isso será explicado a seguir, junto do Lombok.

  > **Por que `private`, explicado do zero?** O modificador de acesso `private` restringe a visibilidade de um campo (ou método) **exclusivamente** ao código dentro da própria classe onde ele foi declarado — nenhuma outra classe, nem mesmo uma que "herde" desta, consegue acessar esse campo diretamente pelo nome (`transaction.description`, por exemplo, não compilaria de fora da classe). Esta é a base do princípio de **encapsulamento** da orientação a objetos: os dados internos de um objeto ficam protegidos contra alteração ou leitura descontrolada vinda de fora, sendo acessados apenas através de métodos explicitamente disponibilizados para isso (os chamados *getters*, explicados na próxima seção).
- **`private double amount;`** — vale destacar uma decisão de projeto importante aqui: diferente de armazenar um valor monetário como um número inteiro em **centavos** (`long`), o domínio armazena diretamente o valor em **reais**, como número decimal.

  > **Por que isso é uma decisão que vale a pena notar?** Em software financeiro "de livro-texto", é mais comum guardar valores monetários como um inteiro representando centavos, justamente para evitar os pequenos erros de arredondamento característicos de números de ponto flutuante (`double`) em cálculos repetidos — por exemplo, somar `0.1 + 0.2` em `double` não resulta exatamente em `0.3`, por limitações de como esse tipo representa números decimais internamente. O projeto opta, de forma consciente, por manter o domínio já em `double`/reais — e concentra toda a **conversão** de centavos-para-reais na **borda** do sistema, especificamente dentro de `PersistTransactionUseCase` (seção 8.9), mantendo a entidade de domínio sempre já no formato "pronto para exibir e usar internamente".
- **Dois construtores, e por que ambos existem.** Repare que a classe tem **dois** construtores diferentes:
  - O primeiro, **gerado automaticamente pelo Lombok** (explicado na próxima seção, via `@AllArgsConstructor`), aceita **todos** os quatro campos, incluindo o `id` já pronto — usado quando uma transação **já existente** (por exemplo, vinda de volta do banco de dados, como veremos na Parte 9) precisa ser reconstruída em memória, com o identificador que ela já tinha.
  - O segundo, **escrito manualmente** (`public Transaction(String description, double amount, Category category) { ... }`), aceita apenas os três dados que fazem sentido vir "de fora" ao **criar uma transação nova** — e **gera o `id` internamente** (`this.id = new TransactionId();`, usando o construtor sem argumentos visto na seção 8.2). Isso reflete uma regra de negócio importante: não faz sentido pedir para quem está criando uma transação nova também "inventar" um identificador único para ela — essa responsabilidade pertence à própria classe.
  - Ter dois construtores com **assinaturas diferentes** (quantidade e/ou tipos de parâmetros diferentes) na mesma classe é permitido em Java, e chama-se **sobrecarga de construtores** (*constructor overloading*) — o compilador decide automaticamente qual dos dois usar, com base em quantos e quais argumentos são passados em cada chamada específica (`new Transaction(id, desc, valor, cat)` usa o primeiro; `new Transaction(desc, valor, cat)` usa o segundo).

### 8.5. Lombok: eliminando código repetitivo, explicado do zero

**Lombok** é uma biblioteca Java que **gera código repetitivo automaticamente**, em tempo de compilação, a partir de anotações simples aplicadas às classes — evitando que o programador precise escrever (e, pior ainda, **manter atualizado**) esse tipo de código manualmente toda vez que um campo é adicionado ou removido.

> **Por que "código repetitivo" é um problema, mesmo sendo simples de escrever?** Escrever um método `getDescription()` que só devolve `this.description` é trivial — mas em uma classe com vários campos, isso significa vários métodos quase idênticos, só mudando o nome do campo. Além do tempo gasto digitando, existe o risco de **inconsistência**: se um campo novo é adicionado à classe, é fácil esquecer de adicionar o *getter* correspondente, e esse tipo de esquecimento só costuma aparecer como um bug mais tarde, quando algum código tenta chamar um método que "deveria existir" mas não existe.

O Lombok é adicionado ao projeto através de um **plugin** no `build.gradle`:

```groovy
plugins {
    id 'io.freefair.lombok' version '9.2.0'
}
```

- **`io.freefair.lombok`** — um plugin Gradle de terceiros (mantido pela comunidade, não pela própria equipe do Lombok, mas amplamente usado e confiável) que integra o Lombok ao processo de compilação do Gradle automaticamente, sem exigir configuração manual adicional do chamado *annotation processor* (o mecanismo interno do compilador Java que permite a bibliotecas como o Lombok "interceptar" a compilação e gerar código extra).

As duas anotações do Lombok usadas em `Transaction`:

- **`@Getter`** — gera automaticamente, para **cada campo privado** da classe, um método público de acesso no formato `getNomeDoCampo()` — aqui, `getId()`, `getDescription()`, `getAmount()`, `getCategory()` — sem que o programador precise escrever nenhum deles manualmente. Repare que **não** existe `@Setter` aplicado a esta classe: isso é intencional, e reforça que a única forma de alterar o estado de uma `Transaction` já criada é... não existir tal forma — ela é, na prática, tratada como um objeto que, uma vez montado, não é mais modificado diretamente (embora tecnicamente os campos não sejam declarados `final`, a ausência de *setters* já impede a maior parte das alterações acidentais).
- **`@AllArgsConstructor`** — gera automaticamente um construtor que recebe **todos** os campos da classe como parâmetros, exatamente na ordem em que foram declarados no código — no caso de `Transaction`, um construtor equivalente a `Transaction(TransactionId id, String description, double amount, Category category)`. É exatamente este o construtor mencionado na seção anterior, que complementa o construtor de três argumentos escrito manualmente.

### 8.6. `TransactionRepository`: o contrato de persistência, vivendo no domínio

```java
package dio.budgeting.domain;

import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    List<Transaction> findAllByCategory(Category category);
}
```

Esta interface vive dentro do pacote `domain` — o pacote mais **interno** da arquitetura em camadas (seção 8.1) — e, propositalmente, **não sabe absolutamente nada** sobre bancos de dados, SQL, JPA, ou qualquer outro detalhe técnico de persistência. Ela apenas declara **o que** a aplicação precisa poder fazer com uma transação: salvá-la (`save`) e buscá-la por categoria (`findAllByCategory`). É exatamente o "contrato" mencionado na explicação de Clean Architecture (seção 8.1): quem quer que implemente esta interface (a camada de `infrastructure`, na Parte 9) é livre para decidir **como**, na prática, essas duas operações são realizadas — MySQL, outro banco, ou até um armazenamento em arquivo — sem que o domínio precise mudar uma única linha.

- **`Transaction save(Transaction transaction);`** — recebe uma transação (nova ou existente) e devolve a transação persistida — note que devolver a transação (em vez de `void`, "não devolve nada") é útil, por exemplo, para o chamador ter acesso ao identificador gerado, caso ainda não o tivesse.
- **`List<Transaction> findAllByCategory(Category category);`** — recebe uma categoria e devolve todas as transações associadas a ela, como uma `List<Transaction>` (uma lista, potencialmente vazia se não houver nenhuma transação naquela categoria).

### 8.7. `PersistTransactionUseCase`: o primeiro caso de uso real, e a primeira *tool* de verdade

```java
package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class PersistTransactionUseCase {
    private final TransactionRepository transactionRepository;

    public PersistTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "persistTransaction", description = "Persiste uma nova transação financeira")
    public TransactionOutput execute(PersistTransactionInput input) {
        var amountInReais = input.amount() / 100.0;

        var transaction = transactionRepository.save(
                new Transaction(input.description(), amountInReais, input.category()));

        return TransactionOutput.from(transaction);
    }
}
```

- **`@Service` + injeção via construtor de `TransactionRepository`** — o mesmo padrão familiar desde a Parte 3.6. O ponto crucial a notar aqui: `PersistTransactionUseCase` depende apenas da **interface** de domínio `TransactionRepository`, e **não** de nenhuma implementação concreta específica. Isso significa que, mesmo **antes** de a Parte 9 implementar a persistência real em banco de dados, este código já está completo e correto — basta que **alguma** implementação de `TransactionRepository` esteja disponível no contexto do Spring para que tudo funcione, seja ela qual for.
- **`@Tool(name = "persistTransaction", description = "Persiste uma nova transação financeira")`** — este é o momento em que o padrão de Tool Calling, estudado em detalhe e com um exemplo didático na Parte 5, é finalmente aplicado a um caso de uso **real** do negócio — o próprio método `execute` de um caso de uso vira, diretamente, uma ferramenta que o LLM pode decidir chamar. Repare no atributo **`name`**, explicitamente definido aqui (diferente do exemplo `MathTools` da Parte 5.2, que não precisou dele) — o motivo específico é explicado com detalhe na Parte 11.2 (adiantando: evita uma colisão de nomes entre duas *tools* diferentes que, coincidentemente, têm métodos Java chamados da mesma forma).
- **`var amountInReais = input.amount() / 100.0;`** — a conversão de **centavos** (a unidade em que o valor chega até este método, tanto vindo da API REST tradicional quanto vindo da extração feita pela própria IA a partir da fala do usuário) para **reais** (a unidade em que o domínio armazena o valor, como decidido na seção 8.4).

  > **Por que dividir por `100.0`, e não por `100`?** Em Java, dividir dois valores do tipo `long` (ou `int`) entre si, usando o operador `/`, realiza uma **divisão inteira** — o resultado é sempre truncado, descartando qualquer parte decimal (`7 / 2` resulta em `3`, não `3.5`). Ao dividir por `100.0` (um literal do tipo `double`, por causa do ponto decimal escrito), a expressão inteira é automaticamente promovida para uma divisão de ponto flutuante, preservando os centavos como casas decimais corretas no resultado (`8000 / 100.0` resulta em `80.0`, corretamente, em vez de `80` sem parte decimal — que, tecnicamente, até funcionaria aqui, mas o princípio geral é importante para outros casos onde o resultado não seria um número redondo).
- **`var`** — usado ao longo de todo o código do projeto (você já viu isso em vários testes anteriores) — é a palavra-chave do Java (desde a versão 10) para **inferência de tipo local**: em vez de escrever explicitamente o tipo de uma variável local (`double amountInReais = ...`), `var` permite que o **compilador** deduza o tipo automaticamente, a partir do valor atribuído. Isso não torna Java uma linguagem "dinamicamente tipada" (o tipo continua fixo e verificado em tempo de compilação, exatamente como seria com o tipo explícito) — é apenas uma conveniência de escrita, reduzindo repetição visual quando o tipo já é óbvio a partir do lado direito da atribuição.
- **`new Transaction(input.description(), amountInReais, input.category())`** — usa o construtor de **três argumentos** de `Transaction` (seção 8.4), o que automaticamente gera um novo `TransactionId` internamente — reforçando, mais uma vez, que criar uma transação nova nunca exige que o chamador escolha um identificador para ela.
- **`TransactionOutput.from(transaction)`** — converte o objeto de domínio (`Transaction`) para um objeto de saída específico deste caso de uso (`TransactionOutput`), explicado na próxima seção.

### 8.8. `PersistTransactionInput` e `TransactionOutput`: DTOs de entrada e saída, explicados do zero

```java
package dio.budgeting.application.input;

import dio.budgeting.domain.Category;
import org.springframework.ai.tool.annotation.ToolParam;

public record PersistTransactionInput(@ToolParam(description = "Descrição do gasto") String description,
                                      @ToolParam(description = "Valor do gasto (em centavos)") long amount,
                                      Category category) {
}
```

```java
package dio.budgeting.application.output;
import dio.budgeting.domain.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record TransactionOutput(String id, String description, String category, double value) {
    public static TransactionOutput from(Transaction transaction) {
        return new TransactionOutput(
                transaction.getId().uuid().toString(),
                transaction.getDescription(),
                transaction.getCategory().name(),
                BigDecimal.valueOf(transaction.getAmount()).setScale(2, RoundingMode.HALF_UP).doubleValue()
        );
    }
}
```

> **O que é um DTO (*Data Transfer Object*), explicado do zero, e por que não usar `Transaction` diretamente em todo lugar?** Um DTO é um objeto cuja **única** responsabilidade é **carregar dados** entre diferentes camadas ou processos de um sistema — sem nenhuma lógica de negócio própria (comparado com `Transaction`, que representa um conceito de domínio "de verdade"). `PersistTransactionInput` (o que o caso de uso *recebe*) e `TransactionOutput` (o que ele *devolve*) são exatamente isso: eles isolam o caso de uso do **formato exato** usado pelas camadas que o cercam — a IA, ao extrair dados da fala do usuário (Parte 11), ou a camada HTTP tradicional (Parte 10), que reaproveita esses mesmos DTOs. Se o formato de um request HTTP mudasse no futuro, por exemplo, isso não precisaria, necessariamente, alterar a estrutura interna de `Transaction`.

- **`@ToolParam(description = "...")`** — o equivalente, aplicado a um **parâmetro individual** de uma *tool*, da `description` já vista em `@Tool` (Parte 5.2, ao nível do método inteiro). Dá ao modelo um contexto específico sobre o **significado** de cada campo — especialmente útil quando os parâmetros formam um objeto mais complexo (como este `record`, com três campos), em vez de um único parâmetro primitivo simples (como era o caso de `sum(int a, int b)`, na Parte 5.2). Aqui, é explicitado que `amount` é esperado **em centavos** — uma informação essencial para que o modelo formate corretamente o valor extraído de uma frase falada (por exemplo, "R$ 80" precisa se tornar `8000`, e não `80`).
- **Uma observação honesta sobre uma inconsistência real do código, conferida diretamente no `.zip`:** repare que apenas `description` e `amount` têm `@ToolParam`; **`category` não tem**. Isso não impede o funcionamento — o Spring AI ainda expõe `category` ao modelo (usando o nome do campo, e o fato de ser um `enum`, cujos valores possíveis já ficam implícitos na descoberta automática via reflexão, explicada na Parte 5.2), mas uma descrição explícita como *"a categoria do gasto, escolhida entre as opções disponíveis"* tornaria a instrução ainda mais clara para o modelo. Este é um pequeno e legítimo candidato de melhoria, listado na seção de Próximos Passos ao final deste tutorial.
- **`BigDecimal` e `RoundingMode`, explicados do zero.** Ao converter o `double` interno de `Transaction` de volta para um valor "apresentável" em `TransactionOutput`, o código usa `BigDecimal` apenas para o passo de **arredondamento**:
  - **`BigDecimal`** — uma classe do Java (pacote `java.math`) para representar números decimais com **precisão arbitrária**, sem os erros de arredondamento inerentes ao tipo `double` (mencionados na seção 8.4) — usada aqui **apenas** como uma ferramenta pontual de arredondamento controlado, e não como o tipo de armazenamento do domínio em si.
  - **`BigDecimal.valueOf(transaction.getAmount())`** — converte o `double` já existente para um `BigDecimal` temporário.
  - **`.setScale(2, RoundingMode.HALF_UP)`** — ajusta o número para exatamente **duas casas decimais**, usando a regra de arredondamento `HALF_UP` — "para cima a partir do dígito 5" (por exemplo, `125.335` arredondaria para `125.34`), a convenção de arredondamento mais familiar e usada no dia a dia.
  - **`.doubleValue()`** — converte o `BigDecimal` já arredondado de volta para `double`, o tipo esperado pelo campo `value` de `TransactionOutput`.
  - **Por que esse passo extra é necessário?** Porque um valor como `125.335000001` (um artefato comum de imprecisão de ponto flutuante, que pode surgir de operações anteriores em `double`) precisa ser "limpo" antes de ser exibido ao usuário final, evitando que apareçam casas decimais estranhas e sem sentido em uma resposta.

### 8.9. Checkpoint da Parte 8

Confirmado no `.zip`: o pacote `domain` contém `Transaction`, `TransactionId`, `Category` e `TransactionRepository` exatamente como descrito; o pacote `application` contém `PersistTransactionUseCase`, `application/input/PersistTransactionInput` e `application/output/TransactionOutput`. O `build.gradle` já inclui o plugin `io.freefair.lombok`. O pacote `infrastructure` já existe fisicamente neste ponto do projeto, mas ainda **vazio de implementação real** — isso só acontece na Parte 9.

**Recapitulando:** agora existe, pela primeira vez, uma representação de negócio de verdade (`Transaction`, `Category`) e um caso de uso completo (`PersistTransactionUseCase`) já pronto para ser usado como *tool* de IA — mas ele ainda **não tem onde persistir de fato** os dados, já que `TransactionRepository` é, até aqui, apenas um contrato sem implementação. A Parte 9 resolve exatamente isso.


---

## Parte 9 — Persistência de verdade: MySQL via Docker Compose e Spring Data JPA (Vídeo 09)

### Recapitulando

Na Parte 8, criamos `TransactionRepository` como uma interface (um "contrato") de domínio, sem nenhuma implementação real. Chegou a hora de implementá-la de verdade, persistindo transações em um banco de dados relacional.

### Objetivo

Implementar, de fato, a interface `TransactionRepository`, persistindo transações em um banco de dados MySQL real, rodando em um container Docker — sem que a camada de domínio precise saber absolutamente nada sobre esse detalhe técnico.

> **📁 Arquivos desta etapa:**
> 1. **Criar** `compose.yml` **na raiz do projeto** (ao lado de `build.gradle`, **fora** de `src/`) — seção 9.2.
> 2. **Editar** `build.gradle` — adicionar três dependências: `spring-boot-docker-compose` (`developmentOnly`), `spring-boot-starter-data-jpa` (`implementation`) e `mysql-connector-j` (`runtimeOnly`) — seção 9.3/9.4.
> 3. **Editar** `application.properties` — adicionar `spring.jpa.hibernate.ddl-auto=update` e `spring.jpa.show-sql=true` (seção 9.8).
> 4. **Criar pacotes** `infrastructure/persistence/entity/` e `infrastructure/persistence/repository/` dentro de `src/main/java/dio/budgeting/infrastructure/` (o pacote `infrastructure` já existia, vazio, desde a Parte 8).
> 5. **Criar** `infrastructure/persistence/entity/TransactionEntity.java` (seção 9.5) — depende de `Transaction`, `TransactionId`, `Category` (já existentes desde a Parte 8).
> 6. **Criar** `infrastructure/persistence/repository/TransactionEntityRepository.java` (seção 9.6) — depende de `TransactionEntity`.
> 7. **Criar** `infrastructure/persistence/repository/JpaTransactionRepository.java` (seção 9.7) — depende de todos os anteriores, e é esta classe que finalmente **implementa** `TransactionRepository`.
>
> Depois do passo 7, o projeto inteiro passa a compilar **e funcionar de ponta a ponta pela primeira vez** com persistência real — é um bom momento para rodar `BudgetingApplication` e confirmar, nos logs, que o Docker sobe o container do MySQL automaticamente (ver seção 9.3).

### 9.1. Docker e Docker Compose, explicados do zero

> **O que é Docker, explicado do zero?** Docker é uma tecnologia de **containers** — uma forma de empacotar e rodar um programa (como um servidor de banco de dados) de forma **isolada** do restante do sistema operacional, incluindo suas próprias dependências e configurações, sem interferir (nem sofrer interferência) de outros programas instalados na mesma máquina. Em vez de instalar o MySQL diretamente no seu sistema operacional (com todos os riscos de conflito de versão, configuração manual trabalhosa, e o clássico problema de "funciona na minha máquina, mas não na do colega"), o banco roda dentro desse ambiente isolado e reprodutível — o **container** — construído a partir de uma **imagem** pré-configurada (no nosso caso, `mysql:9.6`, a imagem oficial e mantida do MySQL, na versão `9.6`).

> **O que é Docker Compose, explicado do zero?** É uma ferramenta, que acompanha o Docker, para **descrever, em um único arquivo YAML**, um ou mais serviços de container e como eles devem ser configurados e conectados entre si — extremamente conveniente para ambientes de desenvolvimento com múltiplas dependências externas (mesmo que, no nosso caso, seja apenas uma: o banco de dados).

### 9.2. `compose.yml`: descrevendo o banco de dados, linha por linha

```yaml
services:
  database:
    image: mysql:9.6
    environment:
      MYSQL_DATABASE: transaction
      MYSQL_ROOT_PASSWORD: root
      MYSQL_USER: app
      MYSQL_PASSWORD: app
    ports:
      - "3307:3306"
    volumes:
      - transaction_data:/var/lib/mysql
    healthcheck:
      test: [ "CMD", "mysqladmin", "ping", "-h", "localhost", "-uapp", "-papp" ]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  transaction_data:
```

- **`services:`** — a seção raiz onde todos os "serviços" (cada um correspondendo, tipicamente, a um container) são declarados.
- **`database:`** — o **nome escolhido** para este serviço específico dentro do arquivo — pode ser qualquer identificador, usado internamente pelo Docker Compose para se referir a este container.
- **`image: mysql:9.6`** — declara a partir de qual **imagem** este container deve ser criado: `mysql`, na **tag** (versão específica) `9.6`, publicada oficialmente no Docker Hub (o repositório público de imagens Docker mais usado).
- **`environment:`** — variáveis de ambiente **específicas da imagem do MySQL**, que ela usa internamente, na primeira inicialização do container, para se autoconfigurar:
  - **`MYSQL_DATABASE: transaction`** — o nome do banco de dados a ser criado automaticamente.
  - **`MYSQL_ROOT_PASSWORD: root`** — a senha do usuário administrativo padrão do MySQL (`root`).
  - **`MYSQL_USER: app`** e **`MYSQL_PASSWORD: app`** — criam um usuário de aplicação adicional (`app`, com senha `app`), com permissões sobre o banco criado — este é o usuário que a própria aplicação Spring Boot usará para se conectar, em vez de usar o usuário administrativo diretamente.
- **`ports: - "3307:3306"`** — mapeia portas entre a máquina hospedeira (a sua máquina real) e o container. O formato é `"porta_da_maquina:porta_do_container"`. `3306` é a porta padrão em que o MySQL escuta **dentro** do container; `3307` é a porta escolhida, na sua máquina real, para acessar esse mesmo serviço de fora.

  > **Por que `3307` em vez de `3306` diretamente?** Se você já tivesse uma instalação de MySQL rodando localmente (fora do Docker) na porta padrão `3306`, mapear o container também para `3306` geraria um **conflito de porta** — o sistema operacional não permite que dois processos escutem na mesma porta simultaneamente. Usar `3307` na máquina hospedeira evita esse conflito preventivamente, mesmo que você não tenha, de fato, outra instalação de MySQL — é uma escolha defensiva.
- **`volumes: - transaction_data:/var/lib/mysql`** — associa um **volume nomeado** (`transaction_data`, declarado na seção `volumes:` ao final do arquivo) ao diretório interno do container onde o MySQL efetivamente guarda os dados do banco (`/var/lib/mysql`).

  > **O que é um "volume" no Docker, explicado do zero, e por que ele é necessário?** Por padrão, tudo o que é escrito **dentro** de um container (incluindo os dados de um banco de dados) é perdido quando esse container é removido ou recriado — o container é, por design, uma unidade "descartável". Um **volume** é um mecanismo do Docker para persistir dados **fora** do ciclo de vida do container, no sistema hospedeiro real, mas ainda gerenciado pelo próprio Docker. Ao associar um volume ao diretório de dados do MySQL, os dados sobrevivem a reinicializações e até a recriações completas do container — sem o volume, cada vez que você reiniciasse a aplicação (e, com ela, o container do banco), todas as transações salvas anteriormente desapareceriam.
- **`healthcheck:`** — define um comando periódico (`test: [ "CMD", "mysqladmin", "ping", ... ]`) que o Docker executa **dentro** do container, para verificar se o serviço já está de fato pronto para aceitar conexões (`interval: 5s` — a cada 5 segundos; `retries: 5` — até 5 tentativas antes de considerar o serviço "não saudável").

  > **Por que isso é necessário — o processo do MySQL não fica pronto instantaneamente?** Não. Mesmo depois de o container "subir" (o processo do MySQL começar a rodar), ele ainda precisa de alguns segundos para inicializar completamente (criar o banco, aplicar as configurações iniciais). Sem um `healthcheck`, outros serviços que dependem do banco (como a própria aplicação Spring Boot) poderiam tentar se conectar **antes** dessa inicialização terminar, e falhar. O `healthcheck` permite que ferramentas externas (incluindo o próprio Spring Boot, através do mecanismo visto na próxima seção) esperem, de forma inteligente, até que o banco esteja realmente pronto.

### 9.3. Integrando o Spring Boot ao Docker Compose automaticamente

```groovy
developmentOnly 'org.springframework.boot:spring-boot-docker-compose'
```

- **`developmentOnly`** — uma configuração especial do Gradle, equivalente, no vocabulário do Spring Boot, a dizer "inclua esta dependência **apenas** ao rodar a aplicação localmente, durante o desenvolvimento" — ela garante que esse artefato **não** seja empacotado no `.jar` final de produção, já que, em um ambiente real de produção, o banco provavelmente não seria gerenciado dessa forma automática (haveria um banco já existente, gerenciado separadamente).
- Com essa dependência presente, o Spring Boot **detecta automaticamente** o arquivo `compose.yml` na raiz do projeto e **sobe o(s) container(s) sozinho**, ao iniciar a aplicação (e os derruba, automaticamente também, ao encerrá-la) — sem exigir que você rode manualmente nenhum comando `docker compose up` em um terminal separado. É esse comportamento automático que os logs de inicialização confirmam ao mostrar o Spring Boot criando rede, volume e container, extraindo essas informações diretamente da definição do `compose.yml`.

### 9.4. Dependências de JPA e do driver MySQL, explicadas do zero

```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
runtimeOnly 'com.mysql:mysql-connector-j'
```

> **O que é JPA, explicado do zero?** JPA (*Jakarta Persistence API*) é uma **especificação padrão** do ecossistema Java para **mapeamento objeto-relacional** — ou seja, para representar, de forma automática e consistente, **tabelas** de um banco de dados relacional como **classes** Java, e **linhas** dessas tabelas como **objetos** dessas classes. Sendo apenas uma especificação (um conjunto de regras e interfaces), o JPA precisa de uma **implementação** de fato para funcionar — a mais usada, de longe, é o **Hibernate**, trazido automaticamente por este *starter*.

- **`spring-boot-starter-data-jpa`** — o *starter* (mesmo conceito das Partes 1.6 e 3.5) que traz, de uma vez: o **Spring Data JPA** (uma camada de conveniência do Spring construída em cima do JPA cru, que veremos na seção 9.6) e o **Hibernate** (a implementação de referência do JPA).
- **`mysql-connector-j`** — o **driver JDBC** específico do MySQL.

  > **O que é um driver JDBC, explicado do zero?** JDBC (*Java Database Connectivity*) é a API padrão do Java para conectar-se a bancos de dados relacionais. Ela define **interfaces genéricas** (como "abrir uma conexão", "executar uma consulta SQL"), mas cada banco de dados específico (MySQL, PostgreSQL, Oracle, etc.) precisa de um **driver** — uma implementação concreta dessas interfaces, que sabe efetivamente como se comunicar, pela rede, no protocolo específico daquele banco. `mysql-connector-j` é o driver oficial para o MySQL.
- **`runtimeOnly`** — diferente de `implementation` (usado nas dependências vistas até agora), esta palavra-chave declara que a dependência é necessária **apenas durante a execução** da aplicação, e **não durante a compilação**. Isso faz sentido aqui porque nenhuma linha do código Java que escrevemos referencia diretamente nenhuma classe do driver MySQL — é o próprio Hibernate/JPA quem o usa internamente, "por baixo dos panos", em tempo de execução.

### 9.5. `TransactionEntity`: a entidade JPA, explicada linha por linha

```java
package dio.budgeting.infrastructure.persistence.entity;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEntity {
    @Id
    private UUID id;
    private String description;
    private double amount;

    @Enumerated(EnumType.STRING)
    private Category category;

    public static TransactionEntity from(Transaction transaction) {
        return new TransactionEntity(transaction.getId().uuid(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getCategory());
    }

    public Transaction toDomain() {
        return new Transaction(
                new TransactionId(this.id),
                this.description,
                this.amount,
                this.category
        );
    }
}
```

Esta classe vive no pacote `infrastructure.persistence.entity` — propositalmente **separada** da entidade de domínio `Transaction` (Parte 8.4), mesmo representando, conceitualmente, "a mesma coisa". Essa separação é uma decisão de arquitetura deliberada, alinhada com o princípio de Clean Architecture visto na Parte 8.1: `TransactionEntity` carrega anotações e preocupações específicas do JPA (como veremos a seguir), e sua estrutura poderia, um dia, divergir da estrutura pura do domínio (por exemplo, para acomodar colunas de auditoria — data de criação, usuário responsável — sem "sujar" a classe `Transaction`, que deve continuar representando apenas o conceito de negócio puro).

- **`@Entity`** — a anotação do JPA que marca esta classe como representando uma **tabela** do banco de dados. Por convenção padrão (sem nenhuma customização adicional, que poderia ser feita através de outra anotação, `@Table`), o nome da tabela gerada é derivado automaticamente do nome da classe: `transaction_entity`.
- **`@Data`** (do Lombok) — uma anotação "combo", mais abrangente do que o `@Getter` isolado visto na Parte 8.5: gera, de uma vez, *getters* **e** *setters* para todos os campos, além de `toString()`, `equals()` e `hashCode()`. Entidades JPA tipicamente precisam de *setters* (diferente de `Transaction`, que não tinha nenhum), porque o **Hibernate os usa internamente**, via reflexão, ao reconstruir um objeto a partir dos dados lidos do banco.
- **`@AllArgsConstructor` + `@NoArgsConstructor`** — a combinação exigida, na prática, pelo próprio JPA:
  - Um construtor **sem argumentos** (`@NoArgsConstructor`) é **obrigatório** para que o Hibernate consiga instanciar a entidade via reflexão (Parte 5.2) — ele primeiro cria um objeto "vazio", e só depois preenche cada campo individualmente, usando os *setters* gerados por `@Data`.
  - Um construtor **com todos os argumentos** (`@AllArgsConstructor`) é o que o método `from` (explicado a seguir) usa, de forma mais direta e legível, para construir a entidade a partir de uma `Transaction` já completa.
- **`@Id`** — marca o campo `id` como a **chave primária** da tabela — o valor que identifica, de forma única, cada linha.
- **`@Enumerated(EnumType.STRING)`** — instrui o JPA sobre **como** persistir o valor de um campo do tipo `enum` (aqui, `Category`).

  > **Por que essa anotação é necessária, e por que `STRING` e não o padrão?** Sem essa anotação, o comportamento padrão do JPA seria `EnumType.ORDINAL` — persistir apenas a **posição numérica** do valor dentro da declaração do `enum` (`0` para `GROCERIES`, `1` para `PHARMA`, `2` para `AUTO`, considerando a ordem declarada na Parte 8.3). Isso é **frágil**: se, no futuro, a ordem dos valores do `enum` mudasse (por exemplo, inserindo uma nova categoria **no meio** da lista já existente), os dados **já salvos** no banco ficariam com o significado completamente corrompido — uma linha que antes representava `PHARMA` (posição `1`) passaria a ser interpretada como qualquer categoria que acabasse ocupando essa mesma posição depois da mudança. `EnumType.STRING`, em vez disso, persiste o **nome literal** do valor (a coluna guarda, de fato, o texto `"GROCERIES"`) — mais legível ao inspecionar o banco diretamente, e imune a esse problema de reordenação futura.
- **`from(Transaction transaction)`**, o **mapper de ida** — um método `static` que recebe um objeto de domínio e devolve a entidade JPA correspondente, extraindo cada valor através dos *getters* de `Transaction` (gerados pelo `@Getter` da Parte 8.5): `transaction.getId().uuid()` (o `UUID` de dentro do `TransactionId`), `getDescription()`, `getAmount()`, `getCategory()`.
- **`toDomain()`**, o **mapper de volta** — reconstrói um objeto `Transaction` puro a partir dos dados armazenados na entidade, usando o construtor de `Transaction` que aceita um `TransactionId` já pronto (o gerado pelo `@AllArgsConstructor` da Parte 8.5) — essencial aqui, já que uma transação **lida de volta do banco** já possui um identificador definido (diferente de uma transação recém-criada, cujo id é gerado internamente, como vimos na Parte 8.4).

  > **O que é um "mapper", explicado do zero?** Um mapper é, simplesmente, um método (ou classe) cuja única responsabilidade é **converter** entre duas representações diferentes de um mesmo conceito — aqui, entre `Transaction` (domínio) e `TransactionEntity` (persistência). Manter essa conversão isolada, em métodos claramente nomeados (`from`/`toDomain`), evita espalhar essa lógica de "tradução" por vários lugares do código, e deixa explícito, em qualquer ponto do sistema, quando uma conversão de camada está acontecendo.

### 9.6. `TransactionEntityRepository`: o repositório Spring Data, explicado do zero

```java
package dio.budgeting.infrastructure.persistence.repository;

import dio.budgeting.domain.Category;
import dio.budgeting.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionEntityRepository extends CrudRepository<TransactionEntity, UUID> {
    List<TransactionEntity> findAllByCategory(Category category);
}
```

- **`CrudRepository<TransactionEntity, UUID>`** — uma interface do **Spring Data** (não confundir com o JPA "cru" — Spring Data é a camada de conveniência mencionada na seção 9.4). Apenas por **estender** esta interface (usando `extends`, o mesmo mecanismo de herança de interfaces já visto na Parte 3.1, com `ChatModel extends Model<...>`), `TransactionEntityRepository` já ganha, **automaticamente e sem nenhuma linha de implementação escrita**, um conjunto completo de operações básicas de CRUD (*Create, Read, Update, Delete* — "Criar, Ler, Atualizar, Apagar", as quatro operações fundamentais sobre dados persistidos): métodos como `save(...)`, `findById(...)`, `findAll()`, `deleteById(...)` já existem, prontos para uso, assim que a aplicação sobe. Os dois parâmetros genéricos entre `< >` informam ao Spring Data qual é o **tipo da entidade** gerenciada (`TransactionEntity`) e qual é o **tipo da chave primária** dela (`UUID`).

  > **Como o Spring Data consegue "implementar" uma interface sem nenhum código escrito?** Em tempo de execução, o Spring Data gera **dinamicamente** (usando, mais uma vez, o mecanismo de reflexão já mencionado na Parte 5.2, combinado com geração de código em memória) uma classe concreta que implementa `TransactionEntityRepository`, com toda a lógica de acesso ao banco já embutida — o programador nunca vê nem escreve essa classe gerada; ele só declara a interface e usa.
- **`List<TransactionEntity> findAllByCategory(Category category);`** — este método **não existe** em `CrudRepository` — é uma adição específica desta interface. Ele segue a convenção de nomenclatura de **query methods** ("métodos de consulta") do Spring Data:

  > **O que são "query methods", explicado do zero?** É um recurso do Spring Data em que, a partir do **nome do método** (seguindo um padrão específico), a consulta SQL correspondente é **inferida e gerada automaticamente** — sem que uma única linha de SQL precise ser escrita manualmente. Aqui, o nome `findAllByCategory` é interpretado, palavra por palavra, como: `findAll` ("buscar todos") + `By` (separador) + `Category` (o nome do campo a filtrar, que precisa corresponder exatamente a um campo existente na entidade `TransactionEntity`) — resultando, internamente, em uma consulta equivalente a `SELECT * FROM transaction_entity WHERE category = ?`.

### 9.7. `JpaTransactionRepository`: a implementação concreta do contrato de domínio, finalmente

```java
package dio.budgeting.infrastructure.persistence.repository;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaTransactionRepository implements TransactionRepository {
    private final TransactionEntityRepository transactionEntityRepository;

    public JpaTransactionRepository(TransactionEntityRepository transactionEntityRepository) {
        this.transactionEntityRepository = transactionEntityRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        var entity = TransactionEntity.from(transaction);
        return transactionEntityRepository.save(entity).toDomain();
    }

    @Override
    public List<Transaction> findAllByCategory(Category category) {
        return transactionEntityRepository.findAllByCategory(category)
                .stream()
                .map(TransactionEntity::toDomain)
                .toList();
    }

}
```

Esta é a classe que **fecha o ciclo** iniciado com a declaração do contrato na Parte 8.6: ela `implements TransactionRepository` — a palavra-chave `implements`, diferente de `extends`, usada aqui porque `JpaTransactionRepository` é uma **classe concreta** fornecendo a implementação real de uma **interface**.

- **`@Repository`** — uma anotação de estereótipo do Spring (assim como `@Service` e `@RestController`, já vistas), especificamente pensada para marcar componentes de **acesso a dados**. É graças a esta anotação, combinada com `implements TransactionRepository`, que o Spring — ao ver `PersistTransactionUseCase` pedindo, no construtor (Parte 8.7), um objeto do tipo `TransactionRepository` — sabe exatamente **qual** implementação injetar: esta.
- **`private final TransactionEntityRepository transactionEntityRepository;`** e injeção via construtor — o padrão já muito familiar a esta altura.
- **`save(Transaction transaction)`** — implementa o método exigido pela interface de domínio, combinando três passos, cada um já explicado individualmente: (1) `TransactionEntity.from(transaction)` — o **mapper de ida** (seção 9.5), convertendo o objeto de domínio para a entidade JPA; (2) `transactionEntityRepository.save(entity)` — a persistência de fato, usando o método herdado de `CrudRepository` (seção 9.6); (3) `.toDomain()` — o **mapper de volta**, convertendo o resultado salvo de volta para o tipo de domínio, **antes** de devolvê-lo ao chamador. O ponto central a notar: quem chama `JpaTransactionRepository.save(...)` (como `PersistTransactionUseCase`) **nunca vê** o tipo `TransactionEntity` — ele entra como `Transaction` e sai como `Transaction`, com toda a conversão intermediária escondida dentro desta classe.
- **`@Override`** — uma anotação (não estritamente obrigatória para o código funcionar, mas fortemente recomendada como boa prática) que informa ao compilador: "este método deve estar, de fato, sobrescrevendo/implementando um método herdado de uma interface ou superclasse". Se, por engano, o nome do método fosse digitado errado (por exemplo, `saves` em vez de `save`), o compilador **rejeitaria a compilação imediatamente**, apontando o erro — em vez de, silenciosamente, criar um método novo e desconectado que nunca seria de fato chamado pelo mecanismo de interface esperado.
- **`findAllByCategory(Category category)`** — busca as entidades daquela categoria através do *query method* já visto (seção 9.6), e então:
  - **`.stream()`** — converte a `List<TransactionEntity>` retornada em um **`Stream`**, a API funcional de processamento de coleções do Java moderno.

    > **O que é um `Stream`, explicado do zero?** Um `Stream` representa uma **sequência de elementos** sobre a qual é possível aplicar operações **encadeadas**, no estilo de API fluente já visto na Parte 4.1 — como transformar cada elemento (`.map(...)`), filtrar apenas alguns (`.filter(...)`, já visto na Parte 7.3), e finalmente **coletar** o resultado em alguma estrutura concreta (como uma nova lista). É uma forma mais declarativa (descrevendo *o quê* fazer com os dados) de processar coleções, em contraste com um laço `for` tradicional (que descreve *como*, passo a passo, iterar manualmente).
  - **`.map(TransactionEntity::toDomain)`** — transforma cada `TransactionEntity` do *stream* em uma `Transaction`, usando uma **referência a método** (`TransactionEntity::toDomain`) — uma forma abreviada de escrever `entity -> entity.toDomain()`, aproveitando que o método `toDomain()` já existe exatamente com a assinatura que `.map(...)` espera (recebe um item, devolve outro).
  - **`.toList()`** — finaliza o *stream*, **coletando** todos os elementos já transformados de volta em uma `List<Transaction>` concreta, imutável.

### 9.8. `application.properties`: criação automática do schema, explicado do zero

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

- **`spring.jpa.hibernate.ddl-auto`** — controla como o **Hibernate** gerencia o **schema** (a estrutura de tabelas, colunas e tipos do banco de dados) a partir das entidades JPA mapeadas no código Java.

  > **O que é "DDL", explicado do zero?** DDL (*Data Definition Language*, "Linguagem de Definição de Dados") é o subconjunto de comandos SQL responsáveis por **criar e alterar a estrutura** do banco (`CREATE TABLE`, `ALTER TABLE`, e assim por diante) — em oposição a comandos que manipulam os **dados em si** (`INSERT`, `SELECT`, chamados de DML). `ddl-auto` controla, especificamente, se e como o Hibernate deve gerar e executar automaticamente esses comandos estruturais.
  - O valor **`update`**, usado no projeto, instrui o Hibernate a **criar ou ajustar** automaticamente as tabelas necessárias (com base nas anotações `@Entity`, `@Id`, etc. já vistas) toda vez que a aplicação sobe, **preservando os dados já existentes** entre uma execução e outra. Isso é diferente de outra opção comum, `create` (às vezes usada temporariamente durante um desenvolvimento muito inicial), que **recria** o schema inteiro do zero a cada subida, **apagando** qualquer dado já salvo — inadequada para um projeto em uso contínuo, como o `budgeting`.
  - **Uma ressalva importante para o futuro:** `update` é adequado para desenvolvimento contínuo, como neste tutorial — mas, em um ambiente de **produção** de verdade, a prática recomendada pela indústria é usar ferramentas de **migração de schema dedicadas** (como Flyway ou Liquibase, que registram, de forma controlada e versionada, cada alteração estrutural feita ao longo do tempo), em vez de deixar o Hibernate alterar o schema automaticamente e sem rastro — algo a se ter em mente caso este projeto evolua além do escopo do desafio.
- **`spring.jpa.show-sql=true`** — faz o Hibernate **imprimir no console**, para cada operação realizada, o comando SQL efetivamente executado — uma ferramenta valiosa de depuração, permitindo, por exemplo, conferir visualmente que a consulta gerada a partir de `findAllByCategory` (seção 9.6) realmente filtra pela coluna esperada, sem precisar "confiar cegamente" no mecanismo automático de geração de query methods.

### 9.9. Checkpoint da Parte 9

Confirmado no `.zip`: `compose.yml` na raiz do projeto define o serviço `database` (MySQL `9.6`); `build.gradle` inclui `spring-boot-docker-compose` (`developmentOnly`), `spring-boot-starter-data-jpa` e `mysql-connector-j` (`runtimeOnly`); os pacotes `infrastructure.persistence.entity` (`TransactionEntity`) e `infrastructure.persistence.repository` (`TransactionEntityRepository`, `JpaTransactionRepository`) existem exatamente como descrito; `application.properties` tem `spring.jpa.hibernate.ddl-auto=update` e `spring.jpa.show-sql=true`.

**Para rodar você mesmo:** é necessário ter o **Docker** (ou Docker Desktop) instalado e **em execução** na sua máquina antes de subir a aplicação — é ele quem efetivamente executa o container do MySQL que o Spring Boot orquestra automaticamente, através do `compose.yml`, graças à dependência `spring-boot-docker-compose`.

**Recapitulando:** agora o domínio construído na Parte 8 tem, finalmente, um lugar real para persistir suas transações — o contrato (`TransactionRepository`) permanece intocado no pacote `domain`, e toda a complexidade técnica de fato (Docker, MySQL, JPA, mapeamento) ficou isolada dentro de `infrastructure`. A Parte 10 vai finalmente expor tudo isso via uma API REST tradicional, e a Parte 11 vai conectar o pipeline de IA a este mesmo domínio.

---

## Parte 10 — Expondo transações via REST: criação e listagem (Vídeo 10)

### Recapitulando

O domínio (Parte 8) e a persistência real (Parte 9) já existem e funcionam. Falta apenas uma porta de entrada tradicional, via HTTP/JSON, para criar e consultar transações — independente, por enquanto, de qualquer envolvimento da IA.

### Objetivo

Dar aos casos de uso já implementados uma porta de entrada REST convencional, permitindo criar e consultar transações diretamente por JSON, sem passar pelo pipeline de IA.

> **📁 Arquivos desta etapa:**
> 1. **Criar** `application/ListTransactionsByCategoryUseCase.java` (seção 10.4) — ao lado de `PersistTransactionUseCase`, já existente desde a Parte 8.
> 2. **Criar pacotes** `infrastructure/http/request/` e `infrastructure/http/response/` dentro de `infrastructure/http/` (novo).
> 3. **Criar** `infrastructure/http/request/TransactionRequest.java` (seção 10.2) — depende de `PersistTransactionInput` e `Category`.
> 4. **Criar** `infrastructure/http/response/TransactionResponse.java` (seção 10.2) — depende de `TransactionOutput`.
> 5. **Criar** `infrastructure/http/TransactionController.java` (seções 10.1 e 10.5) — depende de todos os anteriores, mais os dois casos de uso.
> 6. **Criar** `infrastructure/config/UseCaseConfig.java` (seção 10.6) — opcional na prática (seção 10.6 explica por que ela é redundante com o `@Service` já existente em `PersistTransactionUseCase`), mas incluído aqui para o projeto ficar fiel ao `.zip` final. Se preferir, pode pular este arquivo sem quebrar nada.
>
> Nenhuma dependência nova no `build.gradle`.

### 10.1. `TransactionController`: criação de transações, explicado linha por linha

```java
package dio.budgeting.infrastructure.http;

import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.infrastructure.http.request.TransactionRequest;
import dio.budgeting.infrastructure.http.response.TransactionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final PersistTransactionUseCase persistTransactionUseCase;

    public TransactionController(PersistTransactionUseCase persistTransactionUseCase) {
        this.persistTransactionUseCase = persistTransactionUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(@RequestBody TransactionRequest request) {
        var transactionOutput = persistTransactionUseCase.execute(request.toInput());
        return TransactionResponse.from(transactionOutput);
    }
}
```

Repare que este controller vive em `infrastructure.http` — um pacote **novo**, diferente de onde os controllers de IA moram (`ChatModelController`, `ChatClientController`, `TranscriptionController`, `TextToSpeechController` ficam soltos diretamente em `dio.budgeting`, um pacote mais "raso"). Essa separação reflete diretamente a organização em camadas explicada na Parte 8.1: qualquer controller que expõe o domínio de forma **REST tradicional** fica isolado dentro de `infrastructure`, como uma das possíveis "formas de entrada" do sistema.

- **`@RequestMapping("/transactions")`** — o prefixo de URL deste controller, notoriamente **sem** o `/api` usado pelos controllers de IA (Partes 3, 4, 6, 7) — mais um indício visual de que este é o caminho REST convencional, para o recurso "transações", tratado de forma independente da camada de IA.
- **`@RequestBody TransactionRequest request`** — a anotação **`@RequestBody`** instrui o Spring a **desserializar** o corpo da requisição HTTP (esperado como JSON) diretamente em um objeto Java, aqui `TransactionRequest` (explicado na próxima seção). Esse processo de conversão JSON → objeto Java (e vice-versa) é feito automaticamente pela biblioteca **Jackson**, incluída por padrão junto do `spring-boot-starter-web` (Parte 3.5).
- **`request.toInput()`** — converte o DTO de entrada específico da camada HTTP (`TransactionRequest`) para o DTO de entrada esperado pelo caso de uso (`PersistTransactionInput`, Parte 8.8) — mantendo os dois DTOs **desacoplados**: uma mudança futura no formato do request JSON não obrigaria a alterar o caso de uso, e vice-versa.
- **`@ResponseStatus(HttpStatus.CREATED)`** — como este endpoint específico **cria** um novo recurso (uma nova transação), ele retorna o código HTTP **`201 Created`** — em vez do `200 OK` padrão que o Spring usaria automaticamente — seguindo a convenção REST de sinalizar explicitamente, através do código de status, que uma operação de criação foi bem-sucedida.
- **`TransactionResponse.from(transactionOutput)`** — converte a saída do caso de uso (`TransactionOutput`, Parte 8.8) para o DTO de resposta específico da camada HTTP (`TransactionResponse`), pela mesma razão de desacoplamento já explicada.

### 10.2. `TransactionRequest` e `TransactionResponse`: DTOs da camada HTTP, explicados do zero

```java
package dio.budgeting.infrastructure.http.request;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;

public record TransactionRequest(String description, Category category, double amount) {
    public PersistTransactionInput toInput() {
        return new PersistTransactionInput(description, Math.round(amount * 100), category);
    }
}
```

```java
package dio.budgeting.infrastructure.http.response;
import dio.budgeting.application.output.TransactionOutput;

public record TransactionResponse(String id, String category, String description, double amount) {
    public static TransactionResponse from(TransactionOutput output) {
        return new TransactionResponse(output.id(), output.category(), output.description(), output.value());
    }
}
```

- **`TransactionRequest(String description, Category category, double amount)`** — repare, com atenção, que aqui `amount` é do tipo **`double`** — o valor **em reais**, exatamente como uma pessoa integrando com esta API via JSON esperaria escrever naturalmente (por exemplo, `125.33`). Isso é diferente da unidade usada internamente por `PersistTransactionInput` (centavos, `long` — Parte 8.8) — e é exatamente essa diferença de unidade que o método `toInput()` reconcilia.
- **`Math.round(amount * 100)`** — dentro de `toInput()`, esta é a conversão de reais (a unidade "amigável" da API REST) para **centavos** (a unidade interna esperada por `PersistTransactionInput`): multiplica-se por `100`, e o resultado é arredondado para o **inteiro mais próximo**.

  > **Por que `Math.round`, em vez de simplesmente truncar ou fazer a conversão direta?** `Math.round(double)` devolve um `long`, arredondando para o inteiro mais próximo — necessário aqui porque a multiplicação `amount * 100` (uma operação em `double`) pode, por imprecisão inerente ao ponto flutuante (o mesmo fenômeno já discutido na Parte 8.4), resultar em um valor como `12532.999999999998` em vez de exatamente `12533.0`, mesmo partindo de um valor de entrada "redondo" como `125.33`. Sem o arredondamento explícito, uma conversão ingênua desse valor para `long` (que simplesmente **trunca** a parte decimal, descartando-a) resultaria incorretamente em `12532`, um erro de um centavo — pequeno, mas inaceitável em um sistema que lida com dinheiro. `Math.round` corrige exatamente esse tipo de imprecisão.
- **Records como DTOs, revisitando o conceito.** Tanto `TransactionRequest` quanto `TransactionResponse` são `record`s (conceito completo na Parte 8.2) — a escolha natural para objetos de transferência de dados imutáveis: eles não carregam nenhuma lógica de negócio própria, apenas valores (e, no caso de `TransactionRequest`, um pequeno método auxiliar de conversão, `toInput()`, que não é "regra de negócio" propriamente dita, mas apenas adaptação de formato entre camadas).

### 10.3. Um bug encontrado (e corrigido depois): a conversão de centavos, contado com transparência

Ao testar manualmente este endpoint pela primeira vez — enviando uma transação com `amount: 125.33` — o valor retornado pela API aparecia **incorretamente** como `12533.0`, e não `125.33`, como seria de se esperar.

**A causa:** em uma versão anterior deste fluxo de código (antes do ajuste final, já corrigido e documentado neste tutorial, nas seções 8.7 e 10.2), o valor era tratado, em algum ponto da cadeia entre o request HTTP e a persistência, como já estando na unidade errada, sem a devida conversão de volta.

Este tipo de "bug encontrado ao testar manualmente" é **absolutamente normal** durante o desenvolvimento incremental de qualquer sistema — e é exatamente o motivo de sempre **testar cada endpoint assim que ele é implementado**, em vez de deixar toda a validação para o final do projeto, quando encontrar a causa exata de um problema como este, em meio a muito mais código já escrito, seria bem mais trabalhoso.

A correção definitiva desse fluxo de conversão (reais ↔ centavos, sempre concentrada nas **bordas** de cada camada, nunca "no meio" do domínio) é exatamente o que está documentado, já funcionando corretamente, nas Partes 8.7 (`input.amount() / 100.0`, centavos → reais, ao persistir) e 10.2 (`Math.round(amount * 100)`, reais → centavos, ao receber via REST) deste tutorial.

### 10.4. `ListTransactionsByCategoryUseCase`: o segundo caso de uso, explicado linha por linha

```java
package dio.budgeting.application;

import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListTransactionsByCategoryUseCase {
    private final TransactionRepository transactionRepository;

    public ListTransactionsByCategoryUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "listTransactionsByCategory", description = "Lista transações financeiras por categoria")
    public List<TransactionOutput> execute(@ToolParam(description = "Categoria de uma transação") Category category) {
        return transactionRepository.findAllByCategory(category).stream().map(TransactionOutput::from).toList();
    }
}
```

Este caso de uso é estruturalmente **análogo** a `PersistTransactionUseCase` (Parte 8.7) — mesmo padrão de injeção do `TransactionRepository`, mesma anotação `@Tool` diretamente no método `execute` desde sua criação (reforçando, mais uma vez, que **todo** caso de uso relevante para a IA neste projeto já nasce, desde o início, também como uma *tool* de Tool Calling — não é um passo separado, adicionado depois). A única diferença notável de padrão: aqui, `@ToolParam` **está** presente, explicando o único parâmetro (`category`) — diferente da inconsistência pontual já observada em `PersistTransactionInput.category` (Parte 8.8, sem `@ToolParam`).

- **`execute(...)` devolve `List<TransactionOutput>`** — diferente de `PersistTransactionUseCase.execute(...)`, que devolvia um único `TransactionOutput` (Parte 8.7), este método devolve uma **lista** — coerente com a natureza da operação: "listar" implica, naturalmente, em múltiplos resultados possíveis (incluindo, no caso extremo, nenhum resultado, se não houver transações naquela categoria).
- **`transactionRepository.findAllByCategory(category).stream().map(TransactionOutput::from).toList()`** — a mesma sequência de `.stream().map(...).toList()` já vista em detalhe na Parte 9.7, agora aplicando o mapper `TransactionOutput::from` (referência a método, Parte 9.7) sobre cada `Transaction` encontrada, convertendo-a para o DTO de saída apropriado.

### 10.5. Completando o `TransactionController`: o endpoint de listagem

```java
private final PersistTransactionUseCase persistTransactionUseCase;
private final ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;

public TransactionController(PersistTransactionUseCase persistTransactionUseCase,
                              ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase) {
    this.persistTransactionUseCase = persistTransactionUseCase;
    this.listTransactionsByCategoryUseCase = listTransactionsByCategoryUseCase;
}

@GetMapping("/{category}")
public List<TransactionResponse> readTransactions(@PathVariable Category category) {
    return listTransactionsByCategoryUseCase.execute(category).stream()
            .map(TransactionResponse::from)
            .toList();
}
```

- **Duas dependências agora injetadas no mesmo construtor** — o padrão de injeção por construtor (Parte 3.6) escala naturalmente para mais de uma dependência: basta adicionar mais um parâmetro à assinatura do construtor e atribuí-lo a um novo campo, sem nenhuma configuração adicional — o Spring resolve automaticamente **ambos** os *beans* necessários.
- **`@GetMapping("/{category}")`** — repare na sintaxe `{category}`, com chaves — isso declara uma **variável de caminho** (*path variable*) dentro da própria URL, diferente do parâmetro de *query string* (`?prompt=...`) usado nas Partes 3 e 4. Aqui, a requisição seria feita, por exemplo, para `GET /transactions/GROCERIES`, com o valor da categoria embutido diretamente no caminho da URL.
- **`@PathVariable Category category`** — associa esse parâmetro ao valor capturado da variável de caminho `{category}`. O Spring converte **automaticamente** o texto recebido na URL para o `enum Category` correspondente (Parte 8.3) — e, crucialmente, se o texto não corresponder a **nenhum** valor válido daquele `enum` (por exemplo, `GET /transactions/BANANA`), o Spring já responde, sozinho, com um erro HTTP apropriado, sem que nenhuma linha de código de validação manual precise ser escrita para esse caso.

### 10.6. `UseCaseConfig`: uma configuração explícita adicional — e uma observação honesta sobre redundância

```java
package dio.budgeting.infrastructure.config;

import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public PersistTransactionUseCase persistTransactionUseCase(TransactionRepository transactionRepository) {
        return new PersistTransactionUseCase(transactionRepository);
    }
}
```

- **`@Configuration`** — a mesma anotação já vista de leve na Parte 4.1 (na explicação teórica sobre resolver o erro de bean do `ChatClient`): marca a classe como uma fonte adicional e explícita de definições de *beans* para o Spring — um lugar centralizado onde componentes podem ser construídos manualmente, complementando (ou, como veremos, às vezes duplicando) a auto-configuração e o `@ComponentScan` automático.
- **`@Bean`** — aplicada sobre um **método** (e não sobre uma classe, como `@Service` ou `@RestController`), esta anotação, dentro de uma classe `@Configuration`, informa ao Spring que o valor devolvido por este método específico deve ser registrado como um *bean* gerenciado, disponível para injeção em qualquer outro ponto da aplicação que peça um objeto daquele tipo.
- **`persistTransactionUseCase(TransactionRepository transactionRepository)`** — o **parâmetro** deste método (`TransactionRepository`) também é resolvido automaticamente pelo Spring, da mesma forma que qualquer parâmetro de construtor já visto — o Spring localiza o *bean* de `TransactionRepository` já disponível (a implementação `JpaTransactionRepository`, da Parte 9.7) e o passa aqui.

> **Uma nota de leitura cuidadosa, honesta e didaticamente importante: esta classe é, na prática, redundante.** Como `PersistTransactionUseCase` **já é** anotada com `@Service` diretamente em sua própria declaração (Parte 8.7) — o que, sozinho, já registraria um *bean* dela automaticamente via `@ComponentScan` (Parte 1.3) — esta classe de configuração explícita acaba **duplicando** o mecanismo de registro do mesmo *bean*, por dois caminhos diferentes. Isso **não chega a causar um erro** de execução (o Spring só reclamaria de uma colisão real de *beans* se dois mecanismos tentassem registrar *beans* com nomes conflitantes de forma incompatível — o que não é exatamente o caso aqui, já que o método `@Bean` tem o mesmo nome do *bean* que já seria gerado por `@Service`, resultando, na prática, em apenas uma definição efetivamente prevalecendo), mas é um ponto interessante para observar durante os estudos: **nem sempre um projeto real chega absolutamente "enxuto" em cada etapa do seu desenvolvimento** — e saber identificar esse tipo de configuração redundante, sem se assustar com ela, é parte real de aprender a ler criticamente uma base de código já existente, inclusive a sua própria.

### 10.7. Checkpoint da Parte 10

Confirmado no `.zip`: `infrastructure/http/TransactionController.java`, `infrastructure/http/request/TransactionRequest.java`, `infrastructure/http/response/TransactionResponse.java`, `application/ListTransactionsByCategoryUseCase.java` e `infrastructure/config/UseCaseConfig.java` existem exatamente como descrito. O `TransactionController` expõe `POST /transactions` (criação) e `GET /transactions/{category}` (listagem).

**Testando manualmente:**

```http
POST http://localhost:8080/transactions
Content-Type: application/json

{
  "description": "Compras do mês",
  "category": "GROCERIES",
  "amount": 125.33
}
```

```http
GET http://localhost:8080/transactions/GROCERIES
```

**Recapitulando:** o projeto agora tem uma API REST **completa e funcional**, independente de qualquer envolvimento da IA — é possível criar e consultar transações puramente por JSON. Falta apenas **uma última peça**: conectar o pipeline de voz (Partes 6 e 7) a estes mesmos casos de uso (Partes 8 e 10), fechando o ciclo completo de ponta a ponta. É isso que a Parte 11 — a mais longa e a que reúne tudo o que foi construído até aqui — faz.


---

## Parte 11 — Integrando tudo: do áudio à resposta falada (Vídeo 11)

### Recapitulando

Vamos revisar tudo o que já temos pronto, separadamente, antes de juntar as peças:

- **STT** (Parte 6): transformar áudio em texto, usando o `GoogleGenAiChatModel` de forma multimodal.
- **Tool Calling** (Parte 5): o mecanismo geral, testado com um exemplo didático (`MathTools`).
- **Domínio e casos de uso reais** (Partes 8 e 10): `PersistTransactionUseCase` e `ListTransactionsByCategoryUseCase`, ambos já anotados com `@Tool` desde que foram criados.
- **Persistência real** (Parte 9): as transações realmente são salvas em um banco MySQL.
- **TTS** (Parte 7): transformar texto de volta em áudio, usando o SDK nativo do Gemini.

### Objetivo

Esta é a Parte que finalmente **conecta todas as peças** construídas até aqui em um único fluxo de ponta a ponta: **um áudio entra, uma transação é criada ou consultada de verdade, um áudio de resposta sai**.

> **Nota importante sobre uma divergência de organização entre o curso e o seu projeto real, explicada com cuidado.** O curso, seguindo a rota da OpenAI, implementa este fluxo final **dentro do próprio `TransactionController`** (o controller REST construído na Parte 10), adicionando um endpoint `/transactions/ai`. No projeto final que você efetivamente entregou (Gemini), a decisão foi **diferente**: o fluxo completo de voz-para-voz continua vivendo dentro do **`TranscriptionController`** — o mesmo controller que já hospedava a transcrição pura desde a Parte 6 — mantendo `TransactionController` (Parte 10) dedicado **exclusivamente** à API REST tradicional em JSON, e `TranscriptionController` dedicado a **tudo** que envolve áudio (transcrição pura, o fluxo completo de IA por voz, e até uma segunda rota de consulta de transações via `/api`). Esta é uma escolha de organização perfeitamente legítima — ambas cumprem exatamente o mesmo objetivo funcional — e é o motivo pelo qual, a partir daqui, o código apresentado usa nomes de endpoint e de classe diferentes dos mostrados na narrativa original do curso.

> **📁 Arquivos desta etapa:**
> 1. **Criar pacote** `src/main/resources/prompts/` e, dentro dele, **criar** `system-message.st` (seção 11.2) — texto puro, não é código Java.
> 2. **Reabrir e editar** `src/main/java/dio/budgeting/TranscriptionController.java` — o arquivo já existia desde a Parte 6, com apenas o método `transcribe`. Agora ele ganha: os campos e o construtor completo (5 dependências), o método `readTransactions`, e o método `processAudio` (seção 11.3). Substitua o conteúdo inteiro do arquivo pelo código completo mostrado na seção 11.3, já que ele é cumulativo em relação ao que você escreveu na Parte 6.
> 3. **Conferir** (sem precisar editar, se você seguiu a Parte 8 e 10 à risca) que `PersistTransactionUseCase` e `ListTransactionsByCategoryUseCase` já têm o atributo `name=` explícito em `@Tool` — seção 11.1 explica por que isso é obrigatório a partir de agora, já que as duas *tools* passam a ser registradas **juntas**, pela primeira vez, no mesmo `ChatClient`.
>
> Nenhuma dependência nova no `build.gradle`. Depois do passo 2, o projeto está **funcionalmente completo** — este é o momento de testar o endpoint `POST /api/ai` de ponta a ponta (seção 11.5).

### 11.1. Preparando as *tools*: por que nomes explícitos evitam uma colisão real

Até a Parte 10, tanto `PersistTransactionUseCase.execute(...)` (Parte 8.7) quanto `ListTransactionsByCategoryUseCase.execute(...)` (Parte 10.4) já estavam anotados com `@Tool`. Existe um problema em potencial aqui, que vale entender com precisão: **os dois métodos têm o mesmo nome Java** — `execute` — só que declarados em classes diferentes.

```java
@Tool(name = "persistTransaction", description = "Persiste uma nova transação financeira")
public TransactionOutput execute(PersistTransactionInput input) { ... }

@Tool(name = "listTransactionsByCategory", description = "Lista transações financeiras por categoria")
public List<TransactionOutput> execute(@ToolParam(description = "Categoria de uma transação") Category category) { ... }
```

Ao registrar **as duas classes** como *tools* disponíveis para o mesmo `ChatClient` (o que acontece nesta Parte 11), o Spring AI precisa de alguma forma de **diferenciá-las** de maneira inequívoca para o modelo — e é exatamente para isso que serve o atributo **`name`** de `@Tool`, já usado, desde o início, nas Partes 8.7 e 10.4.

**Sem esse `name` explícito**, o Spring AI usaria, por padrão, o próprio nome do método Java (`execute`) como o nome da ferramenta exposta ao modelo — para **ambas** as *tools* simultaneamente. Isso criaria uma **colisão de nomes**, impedindo o modelo de diferenciar, de forma confiável, qual das duas ferramentas ele de fato quer chamar em cada situação. Ao dar nomes de negócio explícitos e **únicos** (`persistTransaction`, `listTransactionsByCategory`), essa ambiguidade desaparece completamente — mesmo que os métodos Java por trás continuem, coincidentemente, chamando-se `execute` os dois.

### 11.2. O prompt de sistema definitivo: `system-message.st`

```
Você é um assistente financeiro.
Sua tarefa é extrair dados de transações e usar as ferramentas disponíveis para manipular transações.
Ao registrar uma transação, escolha a categoria que melhor se adapta ao contexto.
```

- **Onde este arquivo vive:** `src/main/resources/prompts/system-message.st` — dentro de `resources`, para que seja **empacotado no *classpath*** da aplicação (junto do `.jar` final) e possa ser carregado como um `Resource` (o mesmo conceito da Parte 6.1), independentemente de onde a aplicação for executada.
- **A extensão `.st`** — faz referência ao **StringTemplate**, uma biblioteca/formato para templates de texto usada em outras partes do ecossistema Spring para prompts parametrizáveis (com marcadores de posição que poderiam ser substituídos dinamicamente por valores, em tempo de execução). Neste arquivo específico, o conteúdo é usado como **texto fixo**, sem nenhum marcador — mas a extensão `.st` já sinaliza a intenção de que, no futuro, este prompt **poderia** ser parametrizado (por exemplo, incluindo dinamicamente a lista completa de categorias válidas, em vez de o modelo precisar inferi-las apenas a partir do `enum`).
- **O conteúdo do prompt, frase por frase:**
  - *"Você é um assistente financeiro."* — define o **papel** (a *persona*) do assistente, o mesmo padrão já visto de forma bem mais simples na Parte 4.3 (`"Você é um matemático"`).
  - *"Sua tarefa é extrair dados de transações e usar as ferramentas disponíveis para manipular transações."* — instrui **explicitamente** o modelo a preferir o uso das *tools* registradas, em vez de apenas responder em texto livre — reforçando o comportamento de Tool Calling desejado.
  - *"Ao registrar uma transação, escolha a categoria que melhor se adapta ao contexto."* — uma orientação de negócio bastante específica: o usuário, na prática, **raramente** vai dizer explicitamente "categoria: PHARMA" — ele vai dizer algo natural como *"passei na farmácia rapidinho"*, cabendo ao **próprio modelo** inferir corretamente qual das três categorias disponíveis (Parte 8.3) melhor se encaixa nessa frase.
- Comparado com o prompt de sistema mostrado na narrativa original do curso (mais extenso, cobrindo explicitamente mais casos e exemplos), o `system-message.st` final do seu projeto é mais **enxuto** — outro ponto de possível evolução futura, listado na seção de Próximos Passos.

### 11.3. `TranscriptionController`: o estado final e completo, explicado por inteiro

Este é, sem dúvida, o arquivo mais denso de todo o projeto — porque ele acumula responsabilidades ao longo de dois vídeos diferentes (06 e 11). Vamos ver a classe inteira primeiro, e depois dissecar cada parte nova em relação ao que já conhecemos da Parte 6.

```java
package dio.budgeting;

import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.domain.Category;
import dio.budgeting.infrastructure.http.response.TransactionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TranscriptionController {

    private static final String TRANSCRIPTION_PROMPT = """
            Transcreva o áudio a seguir com fidelidade em português brasileiro.
            Contexto do áudio: contém descrição de gastos financeiros.
            Retorne APENAS a transcrição do áudio.
            """;

    private final GoogleGenAiChatModel chatModel;
    private final PersistTransactionUseCase persistTransactionUseCase;
    private final ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;
    private final ChatClient chatClient;
    private final TextToSpeechService textToSpeechService;

    public TranscriptionController(GoogleGenAiChatModel chatModel,
                                   PersistTransactionUseCase persistTransactionUseCase,
                                   ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase,
                                   ChatClient.Builder chatClientBuilder,
                                   @Value("classpath:/prompts/system-message.st") Resource systemPrompt,
                                   TextToSpeechService textToSpeechService) throws IOException {
        this.chatModel = chatModel;
        this.persistTransactionUseCase = persistTransactionUseCase;
        this.listTransactionsByCategoryUseCase = listTransactionsByCategoryUseCase;
        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt.getContentAsString(StandardCharsets.UTF_8))
                .defaultTools(persistTransactionUseCase, listTransactionsByCategoryUseCase)
                .build();
        this.textToSpeechService = textToSpeechService;
    }

    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String transcribe(@RequestParam("file") MultipartFile file) {
        var audioMedia = new Media(MimeTypeUtils.parseMimeType("audio/mpeg"), file.getResource());

        var userMessage = UserMessage.builder()
                .text(TRANSCRIPTION_PROMPT)
                .media(List.of(audioMedia))
                .build();

        var prompt = Prompt.builder()
                .messages(List.of(userMessage))
                .build();

        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    @GetMapping("/{category}")
    public List<TransactionResponse> readTransactions(@PathVariable Category category) {
        return listTransactionsByCategoryUseCase.execute(category).stream().map(TransactionResponse::from).toList();
    }

    @PostMapping(value = "/ai", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "audio/wav")
    ResponseEntity<Resource> processAudio(@RequestParam("file") MultipartFile file) throws IOException {
        var transcript = transcribe(file);
        var answer = chatClient.prompt().user(transcript).call().content();

        byte[] wavAudio = textToSpeechService.synthesize(answer);
        var resource = new ByteArrayResource(wavAudio);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("audio.wav")
                                .build()
                                .toString())
                .body(resource);
    }

}
```

Analisando, com calma, apenas as partes **novas** em relação ao que já havia sido construído na Parte 6 (o método `transcribe`, em si, é literalmente idêntico ao já explicado — não vamos repeti-lo):

- **Cinco dependências injetadas de uma vez no construtor** — cada uma correspondendo a uma responsabilidade específica de um dos três endpoints desta classe: `GoogleGenAiChatModel` (para a transcrição multimodal, Parte 6.3), os dois casos de uso reais (`PersistTransactionUseCase`, `ListTransactionsByCategoryUseCase` — para expô-los como *tools* **e** para o endpoint de listagem direta), um `ChatClient.Builder` (mesmo mecanismo da Parte 4.2), e um `TextToSpeechService` (Parte 7.3).
- **`@Value("classpath:/prompts/system-message.st") Resource systemPrompt`** — repare que esta é uma forma **diferente** de usar `@Value` em relação à que vimos no `TextToSpeechService` (Parte 7.3, onde `@Value("${...}")` injetava o **valor de uma propriedade**). Aqui, o prefixo especial **`classpath:`** (em vez de `${...}`) instrui o Spring a interpretar o texto como um **caminho de recurso**, e a injetar diretamente um `Resource` apontando para esse arquivo dentro do *classpath* — o padrão idiomático usado para carregar arquivos de texto/template como este prompt de sistema, em vez de uma propriedade simples de configuração.
- **`systemPrompt.getContentAsString(StandardCharsets.UTF_8)`** — lê **todo** o conteúdo do `Resource` de uma vez, como uma única `String`, decodificada especificamente em **UTF-8**.

  > **O que é UTF-8, explicado do zero, e por que especificá-lo explicitamente aqui?** UTF-8 é um padrão de **codificação de caracteres** — a forma como um texto é convertido em uma sequência de bytes (e vice-versa) para ser armazenado ou transmitido. Ele é amplamente usado por ser compatível com ASCII (o padrão mais básico, cobrindo letras sem acento e símbolos comuns) e, ao mesmo tempo, suportar corretamente caracteres de praticamente qualquer idioma do mundo — incluindo acentuação e caracteres especiais do português, essenciais para o prompt de sistema (que contém "não", "está", "é", entre outros). Especificar explicitamente `StandardCharsets.UTF_8`, em vez de deixar o sistema usar uma codificação "padrão" (que pode variar dependendo do sistema operacional/configuração de quem roda o código), garante que o texto seja lido **corretamente**, com os acentos intactos, independentemente de onde a aplicação for executada.
- **`.defaultTools(persistTransactionUseCase, listTransactionsByCategoryUseCase)`** — repare em uma diferença importante em relação ao exemplo apresentado na narrativa do curso original, que passa **classes** (`.defaultTools(PersistTransactionUseCase.class, ListTransactionsByCategoryUseCase.class)`). O código final do seu projeto passa, em vez disso, **instâncias já injetadas** dos dois casos de uso.

  > **Por que essa diferença importa de verdade, e não é só estilo?** Como os dois casos de uso são *beans* gerenciados pelo Spring (com suas próprias dependências já resolvidas — cada um deles, internamente, já tem seu `TransactionRepository` real injetado, apontando para o `JpaTransactionRepository` da Parte 9.7), registrar a **instância gerenciada** garante que a *tool* efetivamente chamada pelo modelo execute com o **mesmo objeto** já configurado e pronto pelo contexto do Spring — com seu repositório de verdade conectado ao banco. Registrar apenas a **classe** exigiria que o próprio Spring AI soubesse instanciar `PersistTransactionUseCase` sozinho, do zero, sem saber de onde viria o `TransactionRepository` necessário — algo que, na prática, não funcionaria corretamente sem configuração adicional. Usar as instâncias já injetadas é, portanto, a forma correta e coerente com o resto da arquitetura do projeto.
- **`throws IOException` na assinatura do construtor** — declarado porque `getContentAsString(...)` pode, em teoria, lançar essa exceção (a leitura de um arquivo, mesmo do *classpath*, é uma operação de entrada/saída, sujeita a falhas). Java exige que exceções **verificadas** (*checked exceptions*, como `IOException` — categorizadas assim porque o compilador **obriga** a tratá-las ou declará-las explicitamente, diferente de exceções "não verificadas" como `IllegalArgumentException`, já vista na Parte 7.3) sejam tratadas com um `try/catch`, ou explicitamente declaradas na assinatura do método com `throws`, repassando a responsabilidade de tratamento para quem chama esse método.
- **`readTransactions(...)`** — o endpoint `GET /api/{category}`, **estruturalmente idêntico** ao `GET /transactions/{category}` do `TransactionController` (Parte 10.5), mas exposto sob o prefixo `/api` em vez de `/transactions` — uma **segunda porta de entrada**, funcionalmente equivalente, para a mesma consulta, agora vivendo ao lado dos endpoints de IA, em vez de junto dos endpoints REST tradicionais.
- **`processAudio(...)` — o endpoint `POST /api/ai`, o fluxo completo de ponta a ponta, passo a passo:**
  1. **`var transcript = transcribe(file);`** — reaproveita, por chamada direta, o **mesmo método** `transcribe` já existente nesta própria classe (explicado por completo na Parte 6.3), convertendo o áudio recebido em texto.
  2. **`var answer = chatClient.prompt().user(transcript).call().content();`** — envia esse texto transcrito como mensagem de usuário ao `ChatClient` **já configurado** no construtor (com o prompt de sistema de `system-message.st` e as duas *tools* de negócio registradas). É exatamente **neste passo** que todo o mecanismo de Tool Calling, explicado com detalhe na Parte 5, entra em ação de verdade: o modelo decide, sozinho, com base na fala transcrita, se deve chamar `persistTransaction` (se a fala descreve um **novo** gasto a registrar) ou `listTransactionsByCategory` (se a fala pede, em vez disso, uma **consulta** de gastos já feitos), executa de fato a *tool* correspondente através do Spring AI, e formula uma **resposta textual final** (`answer`), já incorporando o resultado real dessa execução (o valor exato salvo, ou a lista real de transações encontradas).
  3. **`byte[] wavAudio = textToSpeechService.synthesize(answer);`** — converte essa resposta textual final de volta em áudio (todo o processo explicado em detalhe na Parte 7), fechando definitivamente o ciclo completo: **Áudio → Texto → Ação real → Texto → Áudio**.
  4. A resposta HTTP final é montada exatamente da mesma forma já vista no `TextToSpeechController` (Parte 7.5): `ByteArrayResource`, cabeçalho `Content-Disposition: attachment`, nome de arquivo sugerido `audio.wav`.

### 11.4. Verificando o fluxo com um breakpoint de depuração, passo a passo

Uma forma particularmente eficaz de **confirmar visualmente** que o Tool Calling está de fato acontecendo (e não apenas confiar cegamente na resposta final devolvida) é colocar um **breakpoint** dentro do método `execute` de `PersistTransactionUseCase` (Parte 8.7) e rodar a aplicação em **modo debug** pela IDE.

> **O que é um breakpoint, explicado do zero, para quem nunca depurou código dessa forma?** Um *breakpoint* ("ponto de interrupção") é um marcador que você posiciona em uma linha específica do código, através da sua IDE. Ao rodar a aplicação em **modo debug** (em vez do modo de execução normal), a execução do programa **pausa automaticamente**, exatamente naquela linha, sempre que ela é alcançada — permitindo que você **inspecione**, em tempo real, o valor de todas as variáveis disponíveis naquele ponto exato, antes de decidir continuar a execução (linha por linha, se desejar) ou deixá-la seguir normalmente até o próximo breakpoint.

Ao enviar um áudio como *"Passei na farmácia rapidinho e deixei R$ 80 em três itens"* para `/api/ai`, a execução para exatamente nesse breakpoint, e o painel de variáveis da IDE permite inspecionar o objeto `input` (`PersistTransactionInput`) já **completamente preenchido pelo modelo** a partir da fala transcrita: uma **descrição** gerada automaticamente pela IA (por exemplo, *"Compra de três itens na farmácia"*), o **valor** já em centavos (`8000`, correspondente a R$ 80,00) e a **categoria** corretamente inferida (`PHARMA`) — tudo isso extraído puramente da linguagem natural falada, sem que o usuário tenha dito, em nenhum momento, algo tão explícito quanto "categoria PHARMA" ou "valor 8000".

### 11.5. Testando manualmente o fluxo completo

```http
POST http://localhost:8080/api/ai
Content-Type: multipart/form-data; boundary=boundary

--boundary
Content-Disposition: form-data; name="file"; filename="recording-1.mp3"

< ./src/test/resources/audio/recording-1.mp3
--boundary
```

A resposta é um arquivo `audio.wav`, que, ao ser reproduzido, deve confirmar **em voz** a transação registrada — algo como *"Registrei sua transação de R$ 80 para farmácia na categoria pharma."*. É possível conferir a persistência real diretamente, consultando a tabela `transaction_entity` no banco (via qualquer cliente MySQL, ou pelo painel de banco de dados integrado da própria IDE), confirmando que o registro foi de fato salvo com os valores corretamente extraídos.

### 11.6. Checkpoint da Parte 11 — estado final do projeto inteiro

Conferido diretamente contra `budgeting_ate_o_video11.zip`, este é o estado completo e final do código-fonte de todo o projeto:

```
budgeting/
├── build.gradle
├── compose.yml
├── settings.gradle
├── src/main/java/dio/budgeting/
│   ├── BudgetingApplication.java
│   ├── ChatModelController.java              (Parte 3 — GET /api/chat-model)
│   ├── ChatClientController.java              (Parte 4 — GET /api/chat)
│   ├── TranscriptionController.java           (Partes 6 e 11 — /api/transcribe, /api/{category}, /api/ai)
│   ├── TextToSpeechController.java            (Parte 7 — POST /api/synthesize)
│   ├── TextToSpeechService.java               (Parte 7 — SDK nativo do Gemini + WAV)
│   ├── application/
│   │   ├── PersistTransactionUseCase.java     (Parte 8 — @Tool "persistTransaction")
│   │   ├── ListTransactionsByCategoryUseCase.java (Parte 10 — @Tool "listTransactionsByCategory")
│   │   ├── input/PersistTransactionInput.java
│   │   └── output/TransactionOutput.java
│   ├── domain/
│   │   ├── Transaction.java
│   │   ├── TransactionId.java
│   │   ├── Category.java
│   │   └── TransactionRepository.java          (contrato)
│   └── infrastructure/
│       ├── config/UseCaseConfig.java
│       ├── http/
│       │   ├── TransactionController.java      (Parte 10 — POST/GET /transactions)
│       │   ├── request/TransactionRequest.java
│       │   └── response/TransactionResponse.java
│       └── persistence/
│           ├── entity/TransactionEntity.java
│           └── repository/
│               ├── TransactionEntityRepository.java
│               └── JpaTransactionRepository.java (implementação de TransactionRepository)
├── src/main/resources/
│   ├── application.properties
│   └── prompts/system-message.st
└── src/test/java/dio/budgeting/
    ├── BudgetingApplicationTests.java
    ├── GeminiChatModelIT.java
    ├── GeminiChatClientIT.java
    ├── ToolCallingIT.java
    ├── GeminiTranscriptionModelIT.java
    └── GeminiSpeechModelIT.java
    (+ src/test/resources/audio/recording-1.mp3 a recording-6.mp3)
```

**Recapitulando o projeto inteiro, de ponta a ponta:** começamos com um esqueleto Spring Boot vazio (Parte 1/2); aprendemos a conversar com o Gemini, primeiro de forma crua (Parte 3), depois fluente (Parte 4); aprendemos o mecanismo de Tool Calling em um exemplo didático e seguro (Parte 5); resolvemos, com soluções próprias e bem fundamentadas, os dois pontos em que o Spring AI ainda não cobre o Gemini — transcrição (Parte 6) e síntese de voz (Parte 7); construímos um domínio de negócio limpo e testável, seguindo princípios reais de arquitetura de software (Parte 8); demos a esse domínio uma persistência real, em banco de dados, orquestrada via Docker (Parte 9); expusemos tudo isso via uma API REST tradicional (Parte 10); e, finalmente, conectamos absolutamente tudo em um único fluxo de voz-para-voz (esta Parte 11). Cada uma dessas partes, isoladamente, é relativamente simples — a complexidade real do projeto está em como elas se **encaixam** umas nas outras, e é exatamente esse encaixe que este tutorial tentou deixar o mais explícito e rastreável possível.


---

## Mapa geral da arquitetura

```
                         ┌─────────────────────────────────────────┐
                         │        infrastructure.http (Parte 10)     │
                         │   TransactionController — /transactions   │
                         └───────────────┬───────────────────────────┘
                                          │
   ┌────────────────────────┐            │            ┌──────────────────────────┐
   │  dio.budgeting (raiz)   │            │            │   application (Parte 8)   │
   │  TranscriptionController│◄───────────┼───────────►│ PersistTransactionUseCase │ (@Tool)
   │  /api/transcribe        │            │            │ ListTransactionsByCategory│ (@Tool)
   │  /api/{category}        │            │            │ UseCase                   │
   │  /api/ai  (voz-a-voz)   │            │            └─────────────┬─────────────┘
   │                         │            │                          │
   │  ChatModelController    │            │                          ▼
   │  ChatClientController   │            │            ┌──────────────────────────┐
   │  TextToSpeechController │            │            │      domain (Parte 8)     │
   │  TextToSpeechService    │            │            │  Transaction, Category,   │
   └───────────┬─────────────┘            │            │  TransactionId,           │
               │                          │            │  TransactionRepository    │ (interface)
               ▼                          │            └─────────────┬─────────────┘
   ┌─────────────────────────┐            │                          │
   │  GoogleGenAiChatModel    │            │                          ▼
   │  (Spring AI + Gemini)    │            │            ┌──────────────────────────┐
   │  — chat, tool calling,   │            │            │ infrastructure.persistence│
   │    transcrição multimodal│            │            │ JpaTransactionRepository   │
   │                          │            │            │ TransactionEntity          │
   │  com.google.genai.Client │            │            │ TransactionEntityRepository│
   │  (SDK nativo — TTS)      │            │            └─────────────┬─────────────┘
   └─────────────────────────┘            │                          │
                                           │                          ▼
                                           │            ┌──────────────────────────┐
                                           └───────────►│  MySQL (via Docker Compose)│
                                                         └──────────────────────────┘
```

O fluxo de ponta a ponta (`POST /api/ai`), passo a passo, revisitando a Parte 11 de forma resumida:

1. **Áudio** chega ao endpoint como `MultipartFile`.
2. **STT (Parte 6):** `GoogleGenAiChatModel`, usado de forma multimodal, transcreve o áudio em texto.
3. **Tool Calling (Parte 5 e 11):** o texto transcrito vira uma mensagem de usuário para o `ChatClient`, que — guiado pelo *system prompt* de `system-message.st` (Parte 11.2) — decide chamar `persistTransaction` ou `listTransactionsByCategory`.
4. **Domínio + Persistência (Partes 8 e 9):** o caso de uso escolhido opera sobre `Transaction`/`Category` através de `TransactionRepository`, cuja implementação real (`JpaTransactionRepository`) grava/lê diretamente no MySQL.
5. O modelo formula uma **resposta textual final**, a partir do resultado real devolvido pela ferramenta executada.
6. **TTS (Parte 7):** `TextToSpeechService`, usando o SDK nativo do Gemini, converte essa resposta de volta em áudio `.wav`.
7. O áudio de resposta volta ao cliente HTTP que fez a chamada original.

---

## Guia rápido: rodando o projeto do zero

1. **Pré-requisitos:** JDK 21 instalado (ou um *toolchain* compatível, que o próprio Gradle pode baixar automaticamente); Docker (ou Docker Desktop) em execução; uma chave de API do Google Gemini, obtida gratuitamente em [aistudio.google.com](https://aistudio.google.com/).
2. **Configurar a chave de API** como variável de ambiente:
   ```bash
   export GEMINI_API_KEY="sua-chave-aqui"
   ```
   (ou configurá-la na *Run Configuration* da sua IDE — ver Parte 1.7)
3. **Subir a aplicação:**
   ```bash
   ./gradlew bootRun
   ```
   O Spring Boot sobe automaticamente o container do MySQL (via `spring-boot-docker-compose` e `compose.yml`, Parte 9.3) antes mesmo de a aplicação terminar de iniciar.
4. **Rodar os testes de integração** (que exigem `GEMINI_API_KEY` configurada — sem ela, são automaticamente pulados, graças a `@EnabledIfEnvironmentVariable`, Parte 3.4):
   ```bash
   ./gradlew test
   ```
5. **Testar manualmente** os endpoints principais, com uma ferramenta como o HTTP Client do IntelliJ, Insomnia, ou `curl`:
   - `GET /api/chat?prompt=Oi` — chat simples (Parte 4).
   - `POST /transactions` — criação de transação via JSON puro (Parte 10).
   - `GET /transactions/{category}` ou `GET /api/{category}` — listagem por categoria (Partes 10 e 11).
   - `POST /api/transcribe` (multipart, campo `file`) — transcrição pura (Parte 6).
   - `POST /api/synthesize` (JSON `{"text": "..."}`) — síntese de voz pura (Parte 7).
   - `POST /api/ai` (multipart, campo `file`) — o fluxo completo: áudio de gasto financeiro entra, áudio de confirmação sai (Parte 11).

---

## Glossário cumulativo — todos os conceitos, em ordem de primeira aparição

| # | Termo | Onde apareceu pela 1ª vez | Significado |
|---|---|---|---|
| 1 | LLM (*Large Language Model*) | Parte 0.2 | Modelo de IA treinado para prever a continuação mais provável de um texto, permitindo gerar respostas coerentes a partir de um prompt de entrada. |
| 2 | Token | Parte 0.2 | A menor unidade de texto processada por um LLM — um pedaço de palavra, uma palavra inteira, ou uma sílaba, dependendo do modelo. |
| 3 | Spring AI | Parte 0.3 | Biblioteca do ecossistema Spring que padroniza o acesso a diferentes provedores de IA através de interfaces comuns. |
| 4 | Starter | Parte 0.3 / 1.6 | Dependência "tudo-em-um" do Spring Boot, que traz a biblioteca principal de uma funcionalidade e sua auto-configuração correspondente. |
| 5 | STT (*Speech-to-Text*) | Parte 0.4 | Processo de transformar áudio falado em texto processável. |
| 6 | Tool Calling / Function Calling | Parte 0.4 / 5.1 | Recurso em que um LLM solicita a execução de um método real da aplicação, com argumentos extraídos do contexto — a aplicação, não o modelo, executa de fato. |
| 7 | TTS (*Text-to-Speech*) | Parte 0.4 | Processo de transformar texto de volta em áudio falado. |
| 8 | Chave de API / variável de ambiente | Parte 0.5 / 1.7 | Credencial secreta de autenticação junto a um provedor externo, mantida fora do código-fonte através de uma variável de ambiente. |
| 9 | Gradle / `build.gradle` | Parte 1.1 | Ferramenta de build responsável por baixar dependências, compilar e empacotar o projeto. |
| 10 | Gradle Wrapper (`gradlew`) | Parte 1.1 | Scripts que permitem rodar comandos Gradle sem instalar o Gradle globalmente na máquina. |
| 11 | `settings.gradle` / `rootProject` | Parte 1.2 | Arquivo que nomeia o projeto Gradle raiz. |
| 12 | Pacote (Java) | Parte 1.3 | Mecanismo de organização hierárquica de classes Java, evitando conflitos de nome. |
| 13 | Anotação (Java) | Parte 1.3 | Metadado, iniciado por `@`, que adiciona comportamento especial a uma classe/método/campo, interpretado por frameworks como o Spring. |
| 14 | `@SpringBootApplication` | Parte 1.3 | Anotação combinada que ativa `@Configuration`, `@EnableAutoConfiguration` e `@ComponentScan`. |
| 15 | Auto-configuração | Parte 1.3 | Mecanismo do Spring Boot que configura automaticamente componentes com base nas dependências presentes no classpath. |
| 16 | `@ComponentScan` | Parte 1.3 | Varredura automática de classes anotadas como componentes, registrando-as como beans. |
| 17 | Método `main` | Parte 1.3 | Ponto de entrada padrão de qualquer aplicação Java. |
| 18 | JUnit | Parte 1.4 | Framework de testes automatizados mais usado no ecossistema Java. |
| 19 | `@SpringBootTest` | Parte 1.4 | Anotação de teste que sobe o contexto completo da aplicação Spring. |
| 20 | BOM (*Bill of Materials*) | Parte 1.5 | Artefato que centraliza versões compatíveis de um conjunto de dependências relacionadas. |
| 21 | `implementation` / `runtimeOnly` / `developmentOnly` / `testImplementation` | Parte 1.5 / 3.5 / 9.3 | Palavras-chave do Gradle que declaram em qual etapa (compilação, execução, apenas desenvolvimento, apenas testes) uma dependência é necessária. |
| 22 | `${VARIAVEL}` (interpolação) | Parte 1.7 | Sintaxe do Spring para ler o valor de uma variável de ambiente dentro de um arquivo de propriedades. |
| 23 | Interface (Java) | Parte 3.1 | "Contrato" que declara métodos que um tipo precisa ter, sem necessariamente dizer como eles funcionam. |
| 24 | `ChatModel` | Parte 3.1 | Interface de baixo nível do Spring AI para chamadas simples a um LLM. |
| 25 | Método `default` (interface) | Parte 3.1 | Método de uma interface que já vem com implementação pronta, dispensando reimplementação obrigatória. |
| 26 | `StreamingChatModel` / `Flux` | Parte 3.1 | Interface para respostas em fluxo contínuo (streaming), usando o tipo reativo `Flux` do Project Reactor. |
| 27 | `Prompt` | Parte 3.2 | Estrutura que representa tudo o que é enviado a um LLM: mensagens e opções. |
| 28 | `Message` (`SystemMessage`, `UserMessage`, `AssistantMessage`) | Parte 3.2 | Representação de uma "fala" dentro de uma conversa com IA, categorizada por origem. |
| 29 | Temperatura (`temperature`) | Parte 3.2 | Parâmetro que controla a aleatoriedade/criatividade das respostas de um LLM. |
| 30 | Padrão Builder | Parte 3.4 | Padrão de projeto em que um objeto "montador" permite configurar um objeto complexo através de métodos encadeados, finalizados por `.build()`. |
| 31 | Injeção de dependência | Parte 3.4 | Padrão em que as dependências de um objeto são fornecidas de fora, em vez de criadas por ele mesmo. |
| 32 | `@Autowired` | Parte 3.4 | Anotação que injeta um bean diretamente em um campo. |
| 33 | AssertJ / `assertThat` | Parte 3.4 | Biblioteca de asserções fluentes para testes Java. |
| 34 | Teste de integração (sufixo `IT`) | Parte 3.4 | Teste que depende de recursos externos reais, em oposição a um teste unitário isolado. |
| 35 | `@EnabledIfEnvironmentVariable` | Parte 3.4 | Condiciona a execução de um teste à presença de uma variável de ambiente. |
| 36 | Nível de log (`DEBUG`, `INFO`, etc.) | Parte 3.3 | Classificação da severidade/detalhe de uma mensagem registrada em log. |
| 37 | `@RestController` | Parte 3.6 | Combinação de `@Controller` + `@ResponseBody`, expondo métodos diretamente como resposta HTTP. |
| 38 | `@RequestMapping` / `@GetMapping` / `@PostMapping` | Parte 3.6 / 6.4 | Anotações que mapeiam métodos de um controller a caminhos e verbos HTTP específicos. |
| 39 | Injeção via construtor | Parte 3.6 | Padrão preferido de injeção de dependência, tornando-a explícita e obrigatória. |
| 40 | `final` (campo) | Parte 3.6 | Modificador que impede a reatribuição de um campo após sua inicialização. |
| 41 | Encapsulamento | Parte 3.6 / 8.4 | Princípio de proteger o estado interno de um objeto, expondo-o apenas através de métodos controlados. |
| 42 | `@RequestParam` | Parte 4.2 | Associa um parâmetro de método a um parâmetro de query string da URL. |
| 43 | API fluente (*fluent API*) | Parte 4.2 | Estilo de API com métodos encadeados, legível como uma frase. |
| 44 | `ChatClient` | Parte 4.1 | API de alto nível do Spring AI, construída sobre um `ChatModel`, com suporte a prompt de sistema e tools. |
| 45 | `ChatClient.Builder` | Parte 4.2 | Bean auto-configurado, de escopo *prototype*, usado para construir um `ChatClient`. |
| 46 | Escopo *prototype* × *singleton* | Parte 4.2 | Prototype: uma nova instância a cada injeção; singleton: uma única instância compartilhada. |
| 47 | Prompt de sistema × prompt de usuário | Parte 4.1 | Sistema: instrução do desenvolvedor sobre o comportamento do modelo; usuário: a entrada real de quem conversa. |
| 48 | `.defaultSystem(...)` | Parte 4.3 | Define a mensagem de sistema padrão para todas as chamadas de um `ChatClient`. |
| 49 | Tool Calling / Function Calling (fluxo completo) | Parte 5.1 | Declaração → decisão do modelo → execução real pela aplicação → retorno ao modelo. |
| 50 | `@Tool` / `description` | Parte 5.2 | Anotação que expõe um método como ferramenta disponível a um LLM. |
| 51 | Reflexão (Java) | Parte 5.2 | Capacidade de examinar, em tempo de execução, a estrutura de uma classe (métodos, parâmetros, tipos). |
| 52 | `.defaultTools(...)` | Parte 5.3 | Registra instâncias de classes de ferramentas como disponíveis para todas as chamadas de um `ChatClient`. |
| 53 | `@ParameterizedTest` / `@CsvSource` | Parte 6.5 | Executa o mesmo teste várias vezes, uma por linha de dados fornecida. |
| 54 | `Resource` | Parte 6.1 | Abstração do Spring para "algo que pode ser lido como bytes", independente da origem. |
| 55 | `TranscriptionModel` | Parte 6.1 | Interface do Spring AI para transcrição de áudio — sem implementação para Gemini. |
| 56 | Multimodalidade | Parte 6.2 | Capacidade de um modelo de IA processar mais de um tipo de mídia (texto, áudio, imagem) na mesma interação. |
| 57 | `Media` | Parte 6.3 | Classe do Spring AI para anexar conteúdo não-textual a uma mensagem. |
| 58 | Tipo MIME | Parte 6.3 | Identificador padronizado do formato de um arquivo (ex: `audio/mpeg`). |
| 59 | Text block (`"""..."""`) | Parte 6.3 | Sintaxe do Java para strings multilinha legíveis. |
| 60 | `MultipartFile` / `multipart/form-data` | Parte 6.4 | Abstração e formato para receber arquivos binários em uma requisição HTTP. |
| 61 | `ClassPathResource` | Parte 6.5 | Implementação de `Resource` que localiza arquivos dentro do classpath. |
| 62 | `TextToSpeechModel` | Parte 7.1 | Interface do Spring AI para síntese de voz — sem implementação para Gemini. |
| 63 | SDK nativo do Google GenAI (`com.google.genai.Client`) | Parte 7.3 | Biblioteca de baixo nível usada diretamente quando uma funcionalidade não tem abstração pronta no Spring AI. |
| 64 | `@Service` | Parte 7.3 | Anotação de estereótipo que marca uma classe como componente de lógica de negócio/serviço. |
| 65 | `@Value` | Parte 7.3 / 11.3 | Injeta o valor de uma propriedade de configuração ou um `Resource` de arquivo, em vez de um bean completo. |
| 66 | Fail fast ("falhar rápido") | Parte 7.3 | Princípio de detectar e sinalizar um erro de configuração o mais cedo possível. |
| 67 | `@PreDestroy` | Parte 7.3 | Marca um método a ser chamado automaticamente antes da destruição de um bean. |
| 68 | `Optional` / `.flatMap(...)` | Parte 7.3 | Tipo que representa um valor que pode ou não existir, evitando `NullPointerException`. |
| 69 | PCM (*Pulse Code Modulation*) | Parte 7.4 | Representação digital crua de uma onda sonora, sem compressão nem metadados. |
| 70 | Formato WAV / RIFF | Parte 7.4 | Formato de áudio composto por um cabeçalho de 44 bytes seguido dos dados PCM brutos. |
| 71 | `ByteBuffer` / `ByteOrder` (*endianness*) | Parte 7.4 | Classe para manipular bytes estruturadamente; ordem de escrita de bytes multi-byte (little/big-endian). |
| 72 | `ResponseEntity` / `ContentDisposition` | Parte 7.5 | Tipo para controle explícito de resposta HTTP; cabeçalho que sugere tratamento como arquivo para download. |
| 73 | `record` | Parte 8.2 | Tipo compacto do Java para classes imutáveis de dados, com construtor/getters/equals/hashCode/toString gerados automaticamente. |
| 74 | UUID | Parte 8.2 | Identificador de 128 bits com probabilidade de colisão desprezível. |
| 75 | Identificador fortemente tipado | Parte 8.2 | Uso de um tipo próprio (em vez de String/UUID solto) para prevenir confusão entre ids de entidades diferentes. |
| 76 | `enum` | Parte 8.3 | Tipo que representa um conjunto fixo e conhecido de valores possíveis. |
| 77 | Domain-Driven Design (DDD) | Parte 8.1 | Abordagem que organiza o código em torno do domínio de negócio, isolado de detalhes técnicos. |
| 78 | Clean Architecture | Parte 8.1 | Arquitetura em camadas concêntricas, em que camadas internas definem contratos implementados pelas externas. |
| 79 | Caso de uso (*use case*) | Parte 8.1 | Classe/método que representa uma ação específica e completa que a aplicação sabe executar. |
| 80 | Lombok (`@Getter`, `@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`) | Parte 8.5 / 9.5 | Biblioteca que gera código repetitivo (getters/setters/construtores) via anotações. |
| 81 | Repositório (padrão *Repository*) | Parte 8.6 | Interface que abstrai o acesso a dados, isolando o domínio de detalhes de persistência. |
| 82 | DTO (*Data Transfer Object*) | Parte 8.8 | Objeto cuja única responsabilidade é carregar dados entre camadas, sem lógica de negócio própria. |
| 83 | `@ToolParam` | Parte 8.8 | Anotação que descreve, para o modelo, o significado de um parâmetro individual de uma tool. |
| 84 | `BigDecimal` / `RoundingMode` | Parte 8.8 | Classe para arredondamento decimal controlado, sem os erros de imprecisão do `double`. |
| 85 | Docker / container | Parte 9.1 | Tecnologia de empacotamento e execução isolada de programas, a partir de imagens pré-configuradas. |
| 86 | Docker Compose | Parte 9.1 | Ferramenta para descrever e orquestrar múltiplos serviços de container em um único arquivo YAML. |
| 87 | Volume (Docker) | Parte 9.2 | Mecanismo para persistir dados fora do ciclo de vida de um container. |
| 88 | Healthcheck (Docker) | Parte 9.2 | Comando periódico que verifica se um serviço em container está pronto para uso. |
| 89 | JPA (*Jakarta Persistence API*) / Hibernate | Parte 9.4 | Especificação padrão para mapeamento objeto-relacional; Hibernate é sua implementação de referência. |
| 90 | Driver JDBC | Parte 9.4 | Implementação concreta, específica de um banco, das interfaces JDBC de conexão. |
| 91 | `@Entity` / `@Id` / `@Enumerated` | Parte 9.5 | Anotações JPA que mapeiam uma classe e seus campos a uma tabela e colunas de banco de dados. |
| 92 | Mapper (`from`/`toDomain`) | Parte 9.5 | Método cuja única responsabilidade é converter entre duas representações de um mesmo conceito. |
| 93 | `CrudRepository` | Parte 9.6 | Interface do Spring Data que já fornece operações básicas de CRUD sem implementação manual. |
| 94 | Query methods | Parte 9.6 | Métodos de repositório cujo nome é interpretado para gerar automaticamente a consulta SQL correspondente. |
| 95 | `Stream` / `.map(...)` / `.toList()` | Parte 9.7 | API funcional de processamento de coleções do Java. |
| 96 | Referência a método (`Classe::metodo`) | Parte 9.7 | Forma abreviada de escrever uma lambda que apenas chama um método existente. |
| 97 | `@Override` | Parte 9.7 | Anotação que confirma, em tempo de compilação, que um método está de fato sobrescrevendo/implementando um método herdado. |
| 98 | DDL (*Data Definition Language*) / `ddl-auto` | Parte 9.8 | Subconjunto de SQL para definir estrutura de banco; propriedade que controla a geração automática de schema pelo Hibernate. |
| 99 | `@PathVariable` | Parte 10.5 | Associa um parâmetro de método a uma variável de caminho da URL. |
| 100 | `@ResponseStatus` | Parte 10.1 | Define explicitamente o código de status HTTP de uma resposta. |
| 101 | `@Configuration` / `@Bean` | Parte 4.1 / 10.6 | Marca uma classe como fonte de definições de beans; marca um método cujo retorno deve ser registrado como bean. |
| 102 | Breakpoint / modo debug | Parte 11.4 | Marcador que pausa a execução em uma linha específica, permitindo inspecionar variáveis em tempo real. |

---

## Próximos passos: fechando a entrega do desafio (a partir do Vídeo 12)

O Vídeo 12 do curso não adiciona código — é um vídeo de **roadmap**, propondo evoluções para transformar esta prova de conceito em algo mais robusto. Isso marca a transição entre "seguir o curso" e "entregar o desafio proposto pela DIO", cujos requisitos (conferidos no próprio README) são:

### O que o roadmap do curso sugere (Vídeo 12)

- **Persistência e auditoria mais completas** — evoluir a persistência (por exemplo, adicionando informações de auditoria: horário, autor e origem de cada transação).
- **Segurança com Spring Security e JWT** — proteger os endpoints e permitir múltiplos usuários com identidade própria (hoje, qualquer pessoa com acesso à API pode criar ou consultar qualquer transação).
- **Conectividade externa (Spring OpenFeign)** — integrar com serviços externos, como antifraude ou conversão de moeda.
- **Desacoplamento com MCP Server** — isolar a lógica de negócio (os casos de uso) atrás de um *Model Context Protocol Server*, permitindo que a "tool" de IA seja consumida por qualquer aplicação, independente da linguagem em que foi construída.
- **Microserviços de áudio especializados** — delegar transcrição e TTS a serviços isolados (por exemplo, em Python), comunicando-se via HTTP/JSON.
- **Domínio puro** — reforçar a independência do domínio em relação a frameworks específicos.

### O que o desafio da DIO pede como entrega (seção "Entendendo o Desafio" do README)

1. Fazer um fork do repositório do instrutor, **ou** publicar seu próprio repositório com esta versão do projeto (já adaptada para Gemini — como você já fez, com `budgeting-spring-ai-gemini`).
2. No `README.md` do repositório entregue, explicar de forma simples:
   - O que o projeto faz;
   - Como executar a aplicação (o "Guia rápido" acima é um bom ponto de partida);
   - **Qual melhoria você implementou** — a própria adaptação de OpenAI para Gemini, documentada neste tutorial, já é, em si, uma evolução significativa e defensável como a "melhoria" pedida pelo desafio, mas nada impede de somar uma segunda melhoria menor e pontual;
   - Quais tecnologias foram usadas (Spring Boot, Spring AI, Google Gemini, MySQL, Docker, Lombok, JUnit 5, AssertJ);
   - Como testar o fluxo principal (o endpoint `/api/ai`);
   - O que você aprendeu durante o desafio.
3. Incluir, se possível, prints, exemplos de requisições, testes realizados ou anotações pessoais — o **LOG de Projeto** que você já mantém em paralelo a este tutorial é material perfeito para essa seção.

### Ideias de melhorias pequenas e viáveis, coerentes com o estado atual do projeto

Pensando em algo "pequeno, bem explicado e funcionando" (a recomendação explícita do próprio README, em vez de "uma ideia grande incompleta"), alguns candidatos concretos, ordenados do mais simples ao mais trabalhoso:

1. **Completar o `@ToolParam` faltante** em `PersistTransactionInput.category` (Parte 8.8) — um ajuste de poucos minutos, com potencial de melhorar a precisão da categorização feita pela IA.
2. **Adicionar novas categorias** ao `enum Category` (Parte 8.3) — hoje limitado a `GROCERIES`, `PHARMA`, `AUTO` — e testar se o modelo já as categoriza corretamente a partir de novos áudios de exemplo.
3. **Validação de entrada** no `TransactionRequest` (Parte 10.2) — por exemplo, usando Bean Validation (`@NotBlank`, `@Positive`) para rejeitar requisições com descrição vazia ou valor negativo antes mesmo de chegar ao caso de uso.
4. **Testes automatizados para os casos de uso e controllers** — o projeto tem bons testes de integração para a camada de IA (Partes 3 a 7), mas não há testes unitários dedicados a `PersistTransactionUseCase`, `ListTransactionsByCategoryUseCase` ou aos controllers REST da Parte 10 — um bom uso de *mocks* (por exemplo, com Mockito) para isolar essas classes de suas dependências reais.
5. **Resolver a redundância do `UseCaseConfig`** (Parte 10.6) — decidir conscientemente entre manter o `@Service` na própria classe do caso de uso **ou** a configuração explícita via `@Bean`, documentando a escolha.
6. **Um endpoint de consulta mais flexível** — por exemplo, listar todas as transações (sem filtro de categoria), ou permitir filtrar por período de datas — exigiria adicionar um novo método ao `TransactionRepository` (Parte 8.6) e sua implementação em `JpaTransactionRepository` (Parte 9.7).
7. **Melhorar as respostas faladas** — ajustar o `system-message.st` (Parte 11.2) para produzir respostas mais naturais e variadas, ou trocar a voz do TTS (`voiceName("Kore")`, Parte 7.3) por outra das vozes pré-definidas do Gemini.

Qualquer uma dessas é suficiente, sozinha, como a "melhoria implementada" pedida na entrega — o importante, reforça o próprio README, é entender o fluxo, testar a solução, e documentar claramente o que foi construído.

