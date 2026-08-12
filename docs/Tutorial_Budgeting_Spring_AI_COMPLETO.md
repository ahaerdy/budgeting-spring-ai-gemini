# Tutorial Final — Desenvolvendo sua API Inteligente com Reconhecimento de Fala e Spring Boot

**Projeto `budgeting` — do zero até o Vídeo 11, na versão adaptada para Google Gemini**

- Curso: NTT Data — Jornada Tech (DIO) · Módulo 4 — Curso 5: "Desenvolvendo sua API Inteligente com Reconhecimento de Fala e Spring Boot"
- Instrutor original do curso: Thiago Poiani (Principal Engineer at Skip) — a aula usa **OpenAI** como provedor de IA
- Autor deste projeto: você — adaptando, ao longo de todo o curso, o mesmo roteiro para usar a **API do Google Gemini** em vez da OpenAI
- Documento de referência de estudo — nível iniciante em Java — escrito do zero a partir da leitura completa do código-fonte final (`budgeting_ate_o_video11.zip`) e do README atualizado (Vídeos 01 a 12)

---

## Sobre este tutorial

Este documento **não é uma concatenação** dos tutoriais anteriores (`001` a `010`). É uma reescrita completa, pensada agora que o projeto inteiro — do "Hello World" do Spring Boot até o assistente de voz de ponta a ponta — já existe e pôde ser lido, arquivo por arquivo, no estado real entregue em `budgeting_ate_o_video11.zip`. Sempre que a narrativa do curso (documentada no README, que usa OpenAI) diverge do código que você efetivamente escreveu (que usa Gemini), este tutorial **segue o código real**, explicando a divergência em vez de escondê-la — porque essa divergência é, precisamente, a parte do projeto que representa a sua própria adaptação e aprendizado.

**Como este tutorial está organizado.** Cada vídeo do curso vira uma "Parte". Dentro de cada parte:

1. **Objetivo** — o que aquele vídeo entrega, em uma frase.
2. **Conceitos novos** — explicados do zero, antes de aparecerem em código.
3. **Construção passo a passo** — o código evolui em pequenos incrementos, do mesmo jeito que uma pessoa desenvolvendo o testaria: primeiro algo simples e verificável (um teste de integração, por exemplo), depois a funcionalidade "de verdade" (um endpoint).
4. **Checkpoint** — o estado final dos arquivos daquela etapa, **conferido diretamente contra o `.zip` enviado**, não apenas contra a narrativa do curso.

No final, você encontra um **glossário cumulativo**, um **mapa geral da arquitetura**, um **guia de execução do projeto do zero** e os **próximos passos** para fechar a entrega do desafio (Vídeo 12 em diante).

> **Nota sobre a adaptação Gemini.** O curso original usa a OpenAI (`gpt-4o-mini` para chat, Whisper para transcrição, a Speech API da OpenAI para voz). O Spring AI dá suporte oficial a vários provedores through de *starters* — e o Google Gemini é um deles, através do artefato `spring-ai-starter-model-google-genai`. Isso significa que boa parte da troca de provedor é só trocar de dependência e de nome de classe (`OpenAiChatModel` → `GoogleGenAiChatModel`, por exemplo). Mas existem dois pontos em que o Spring AI **não** tem uma implementação pronta para o Gemini: a **Transcription API** (não existe `TranscriptionModel` para Gemini no Spring AI) e a **Text-to-Speech API** (não existe `TextToSpeechModel` para Gemini). Nesses dois pontos, em vez de usar as interfaces genéricas do Spring AI, o projeto teve que:
> - para transcrição: usar o **`ChatModel` multimodal** do Gemini (o mesmo `GoogleGenAiChatModel` do chat), enviando o áudio como uma mensagem com mídia anexada;
> - para síntese de voz: chamar diretamente o **SDK Java nativo do Google GenAI** (`com.google.genai.Client`), por fora do Spring AI, e converter manualmente o áudio bruto recebido (PCM) em um arquivo `.wav` reproduzível.
>
> Esses dois pontos — a razão da divergência e a solução escolhida — são explicados em detalhe nas Partes 6 e 7.

---

## Parte 0 — Antes de tocar em código: os conceitos que sustentam o projeto inteiro

Vale investir alguns minutos aqui, porque tudo o que vem depois é uma variação desses poucos conceitos.

### 0.1. O que o projeto faz, em uma frase

O assistente de *budgeting* recebe um **áudio** de alguém falando um gasto (“gastei 50 reais no mercado”), **transcreve** esse áudio em texto, usa uma **IA (LLM)** para entender a intenção e **executar código Java de verdade** (salvar a transação no banco, ou consultar transações já salvas), e devolve uma **resposta em áudio** confirmando o que foi feito.

### 0.2. Modelo de linguagem (LLM) e o papel do Spring AI

Um **LLM** (*Large Language Model* — Grande Modelo de Linguagem) é um modelo de IA treinado para prever e gerar texto a partir de um texto de entrada (o *prompt*). Empresas como OpenAI, Google e Anthropic expõem esses modelos como APIs HTTP: você manda um texto, paga por *tokens* (pedaços de palavras processados) e recebe uma resposta.

O **Spring AI** é uma biblioteca do ecossistema Spring que padroniza o acesso a esses modelos. Em vez de cada provedor (OpenAI, Gemini, Anthropic, DeepSeek...) ter uma forma diferente de chamar a API, montar autenticação e tratar erros, o Spring AI oferece **interfaces comuns** (`ChatModel`, `TranscriptionModel`, `TextToSpeechModel`, etc.) e cada provedor tem um *starter* (uma dependência Gradle/Maven) que implementa essas interfaces por baixo dos panos. Trocar de provedor, na maioria dos casos, é trocar a dependência e as propriedades de configuração — o código que usa a interface muda pouco ou nada.

### 0.3. Os três pilares do pipeline: STT, Tool Calling, TTS

- **STT — Speech-to-Text** (fala para texto): transforma a onda sonora do áudio em uma string de texto processável.
- **Tool Calling** (chamada de ferramentas): o LLM, além de gerar texto, pode decidir **chamar um método Java real** quando percebe que a intenção do usuário exige uma ação concreta (salvar algo, consultar algo). O LLM não executa o código — ele decide *qual* método chamar e *com quais argumentos*, e é a aplicação Java quem efetivamente executa.
- **TTS — Text-to-Speech** (texto para fala): transforma a resposta final (texto) de volta em áudio, para humanizar a resposta ao usuário.

### 0.4. Chave de API e variável de ambiente

Toda chamada a um provedor de IA exige uma **chave de API** — uma *string* secreta que identifica e autentica a sua conta, permitindo que o provedor cobre o uso e bloqueie acesso não autorizado. Essa chave **nunca** deve ser escrita diretamente em um arquivo versionado no Git (como `application.properties`), porque isso a exporia publicamente em caso de commit. A prática padrão é referenciá-la a partir de uma **variável de ambiente** do sistema operacional — no projeto final, `GEMINI_API_KEY` — e usar a sintaxe do Spring, `${NOME_DA_VARIAVEL}`, para que o Spring resolva o valor em tempo de execução, lendo o ambiente onde a aplicação roda.

Com isso mapeado, vamos construir o projeto do zero.

---

## Parte 1 e 2 — Criando o projeto e conectando ao provedor de IA (Vídeos 01 e 02)

### 1.1. Objetivo

Sair de "nenhum projeto" para uma aplicação Spring Boot mínima, capaz de subir sem erros com a integração ao provedor de IA já configurada (chave de API lida do ambiente).

### 1.2. Criando o projeto Spring Boot

O projeto é criado como um novo projeto Spring Boot (via [start.spring.io](https://start.spring.io) ou diretamente pela IDE), com:

- **Nome do projeto / `artifactId`:** `budgeting`
- **Linguagem:** Java
- **Build tool:** Gradle (usando a *Groovy DSL*, arquivo `build.gradle`)
- **Java:** versão 21 (a versão efetivamente usada no `build.gradle` final — o curso começa com uma versão mais recente, mas o projeto convergiu para o toolchain 21, uma versão LTS — *Long Term Support* — do Java, amplamente compatível com bibliotecas do ecossistema Spring)

O resultado inicial é um esqueleto de projeto Gradle:

```
budgeting/
├── build.gradle
├── settings.gradle
├── gradlew, gradlew.bat        (o "Gradle Wrapper" — permite rodar o build sem instalar o Gradle manualmente)
└── src/
    ├── main/java/dio/budgeting/BudgetingApplication.java
    ├── main/resources/application.properties
    └── test/java/dio/budgeting/BudgetingApplicationTests.java
```

**`settings.gradle`** — arquivo que dá nome ao projeto Gradle como um todo (o *root project*):

```groovy
rootProject.name = 'budgeting'
```

**`BudgetingApplication.java`** — a classe de entrada (*entry point*) de qualquer aplicação Spring Boot:

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

- **`@SpringBootApplication`** — uma anotação "combo" que ativa três comportamentos de uma vez: `@Configuration` (a classe pode declarar *beans*, isto é, objetos gerenciados pelo Spring), `@EnableAutoConfiguration` (o Spring Boot tenta configurar automaticamente tudo que detecta no *classpath* — por exemplo, se ele encontra a dependência de um driver de banco, ele já prepara um `DataSource`) e `@ComponentScan` (o Spring varre o pacote atual e os subpacotes em busca de classes anotadas, como `@RestController` ou `@Service`, e as registra automaticamente).
- **`public static void main(String[] args)`** — o método padrão que a JVM (*Java Virtual Machine*) procura para iniciar qualquer aplicação Java.
- **`SpringApplication.run(...)`** — sobe todo o contexto do Spring (cria os *beans*, inicia o servidor HTTP embutido, conecta a integrações externas) e mantém a aplicação rodando até ser encerrada.

**`BudgetingApplicationTests.java`** — um teste de "sanidade" gerado automaticamente, cujo único objetivo é garantir que o contexto do Spring sobe sem erros:

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

- **`@SpringBootTest`** — anotação de teste que sobe o contexto completo da aplicação Spring (todos os *beans*, todas as auto-configurações), simulando um ambiente real, ao contrário de um teste unitário isolado.
- **`contextLoads()`** — um teste vazio. Se ele passa, significa que nada travou ao montar o contexto (nenhum *bean* faltando, nenhuma configuração inválida).

### 1.3. O BOM do Spring AI

Antes de adicionar o provedor de IA, o `build.gradle` recebe uma dependência especial:

```groovy
implementation platform("org.springframework.ai:spring-ai-bom:2.0.0")
```

- **BOM (*Bill of Materials*)** — em vez de uma dependência de código, um BOM é um "índice de versões". Ele declara, para um conjunto de artefatos relacionados (todos os módulos do Spring AI, por exemplo), qual versão de cada um é compatível entre si. Ao importar o BOM com `platform(...)`, você não precisa mais escrever a versão em cada dependência individual do Spring AI (`spring-ai-starter-model-openai`, `spring-ai-starter-model-google-genai`, etc.) — o Gradle resolve automaticamente a versão certa de cada uma a partir do BOM. Isso evita o problema clássico de duas dependências do mesmo "família" ficarem em versões incompatíveis entre si.
- **`2.0.0`** — a versão **estável** da geração 2.0 do Spring AI (a série `2.x` é a que traz suporte nativo ao Gemini através do starter `spring-ai-starter-model-google-genai`, e é compatível com Spring Boot 4.x). Em ciclos de desenvolvimento anteriores desta mesma geração, o Spring AI passou por versões de pré-lançamento identificadas com o sufixo `-M` (*milestone*, "versão de testes") — por exemplo, `2.0.0-M4` — que exigiam adicionar um repositório de milestones separado ao projeto, além do Maven Central padrão. Ao montar o projeto, vale sempre conferir na documentação oficial (`docs.spring.io/spring-ai`) qual é a versão estável mais recente disponível — usar uma versão estável, quando existir, evita essa configuração extra e garante uma API mais previsível.

> **💡 Dica prática (IntelliJ):** depois de editar `build.gradle` diretamente no arquivo (em vez de usar o assistente visual do IntelliJ), é comum o painel lateral **Gradle** não refletir a mudança imediatamente, mesmo clicando no ícone de refresh. Se, ao rodar a aplicação, o `-classpath` impresso no console não contiver os `.jar`s da dependência recém-adicionada, force a resincronização em duas etapas: (1) pelo terminal, dentro da pasta do projeto Gradle, rode `./gradlew --refresh-dependencies build -x test`; (2) volte ao IntelliJ e sincronize o painel Gradle novamente — ele passará a enxergar o cache já populado pelo comando do terminal. Esse é um dos poucos pontos de atrito puramente "de ferramenta" (não de código) que costuma aparecer ao seguir este tutorial.

### 1.4. O starter do modelo — a decisão OpenAI vs. Gemini

O curso, neste ponto, adiciona o starter da OpenAI:

```groovy
implementation 'org.springframework.ai:spring-ai-starter-model-openai'
```

No projeto final (o que está no `.zip`), essa linha existe **comentada**, e no lugar dela está o starter do Gemini:

```groovy
//  implementation 'org.springframework.ai:spring-ai-starter-model-openai'
implementation 'org.springframework.ai:spring-ai-starter-model-google-genai'
```

- **`spring-ai-starter-model-google-genai`** — o *starter* (dependência "tudo-em-um", que já traz a biblioteca principal e a configuração automática) responsável por conectar o Spring AI à API do **Google Gemini** através do SDK oficial `google-genai`. É esse starter que disponibiliza, entre outros, o *bean* `GoogleGenAiChatModel` usado em todo o projeto a partir daqui.
- A linha da OpenAI foi **mantida comentada** (não apagada) — uma prática comum para deixar rastro de que a aplicação já funcionou com outro provedor, e que a troca é, de fato, apenas de dependência.

### 1.5. Configurando a chave de API do Gemini

No `application.properties`, a chave é referenciada via variável de ambiente, exatamente como explicado na Parte 0.4:

```properties
spring.application.name=budgeting
#spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.google.genai.api-key=${GEMINI_API_KEY}
```

- **`spring.application.name`** — nome lógico da aplicação, usado em logs e métricas.
- **`spring.ai.google.genai.api-key`** — a propriedade específica (definida pelo starter do Gemini) que o Spring AI usa para autenticar todas as chamadas ao Gemini. O valor `${GEMINI_API_KEY}` instrui o Spring a procurar essa chave numa variável de ambiente chamada `GEMINI_API_KEY` no sistema operacional (ou na configuração de execução da IDE) — exatamente o mesmo raciocínio do `OPENAI_API_KEY` comentado logo acima, só que apontando para outro provedor.

**Para rodar você mesmo:** antes de subir a aplicação, é preciso ter uma chave de API do Gemini (obtida em [aistudio.google.com](https://aistudio.google.com/)) e exportá-la como variável de ambiente, por exemplo:

```bash
export GEMINI_API_KEY="sua-chave-aqui"
```

Ou, no IntelliJ: **Run/Debug Configurations → Environment variables**, adicionando `GEMINI_API_KEY=sua-chave-aqui`.

### 1.6. Verificação: a aplicação sobe sem erros

Neste ponto, rodar `./gradlew bootRun` (ou o botão de *Run* da IDE apontando para `BudgetingApplication`) deve produzir um log parecido com:

```
:: Spring Boot ::

INFO ... dio.budgeting.BudgetingApplication : Starting BudgetingApplication
INFO ... dio.budgeting.BudgetingApplication : Started BudgetingApplication
```

Se a variável `GEMINI_API_KEY` não estiver definida, a aplicação ainda sobe (a validação da chave só acontece na primeira chamada real ao modelo) — mas qualquer tentativa de usar o chat ou os endpoints de IA vai falhar. Esse é exatamente o comportamento observado no curso com a OpenAI: sem a chave, aparece um `IllegalArgumentException` explicando qual propriedade não foi resolvida.

### 1.7. Checkpoint — estado dos arquivos após a Parte 1/2

Conferido no `.zip` final, o estado desta etapa corresponde a:

- `build.gradle` — plugins Spring Boot + BOM do Spring AI + starter Gemini (OpenAI comentado)
- `settings.gradle` — `rootProject.name = 'budgeting'`
- `src/main/java/dio/budgeting/BudgetingApplication.java` — classe de entrada padrão
- `src/main/resources/application.properties` — `spring.application.name` + `spring.ai.google.genai.api-key`
- `src/test/java/dio/budgeting/BudgetingApplicationTests.java` — teste de sanidade `contextLoads`


---

## Parte 3 — ChatModel: a primeira chamada a uma LLM (Vídeo 03)

### 3.1. Objetivo

Entender a API de mais baixo nível do Spring AI para conversar com um modelo (`ChatModel`), validar a integração através de um **teste de integração**, e só depois expor isso como um endpoint HTTP.

### 3.2. Conceito: a interface `ChatModel`

`ChatModel` é a interface central do Spring AI para chat com LLMs. Ela expõe, de forma simplificada, um método `call`:

```java
public interface ChatModel extends Model<Prompt, ChatResponse>, StreamingChatModel {
    default String call(String message) {...}

    @Override
    ChatResponse call(Prompt prompt);
}
```

- **`call(String message)`** — a forma mais simples: você manda uma `String` e recebe uma `String` de volta. É um método `default` (tem implementação pronta na própria interface), útil para protótipos e testes rápidos.
- **`call(Prompt prompt)`** — a forma completa, usada em aplicações reais: recebe um objeto `Prompt` (que pode incluir várias mensagens, opções de configuração, etc.) e devolve um `ChatResponse` (que carrega, além do texto, metadados como *tokens* consumidos).
- **`StreamingChatModel`** — uma interface irmã, com um método `stream(...)` que devolve um `Flux<String>` (um fluxo reativo — do projeto Reactor) em vez de aguardar a resposta inteira, útil para exibir a resposta "digitando" em tempo real, como em um chat.
- **`Prompt`** — a classe que representa, de forma completa, o que será enviado ao modelo: uma lista de `Message` (mensagens — que podem ser do sistema, do usuário, do assistente, ou de retorno de uma *tool*) e, opcionalmente, `ChatOptions` (configurações como modelo específico, temperatura, formato de resposta).
- **Temperatura (`temperature`)** — um parâmetro numérico (tipicamente entre 0 e 1, ou 0 e 2 dependendo do provedor) que controla a aleatoriedade das respostas do modelo. `0` torna a saída mais determinística e repetível (o modelo tende a escolher sempre a palavra mais provável); valores mais altos aumentam a criatividade e a variação entre respostas para o mesmo prompt.

### 3.3. `GoogleGenAiChatModel` — a implementação usada no projeto

No projeto final, a implementação concreta injetada é `GoogleGenAiChatModel`, do pacote `org.springframework.ai.google.genai`. Ela é criada automaticamente pelo Spring Boot (auto-configuração) a partir das propriedades já vistas na Parte 1.5, complementadas por:

```properties
spring.ai.google.genai.chat.options.model=gemini-3-flash-preview
spring.ai.google.genai.chat.options.temperature=0.0
```

- **`spring.ai.google.genai.chat.options.model`** — define qual variante do Gemini será usada por padrão em todas as chamadas de chat: `gemini-3-flash-preview`, uma versão da família "Flash" (mais rápida e barata, com boa relação custo/capacidade para tarefas de extração de dados como as deste projeto, em oposição às variantes "Pro", mais lentas e caras, otimizadas para tarefas de raciocínio mais complexo).
- **`temperature=0.0`** — equivalente global à opção `temperature = 0` já explicada: como o objetivo do assistente é extrair dados estruturados de forma confiável (valor, categoria, descrição), e não ser criativo, faz sentido reduzir a variabilidade das respostas.
- **`logging.level.org.springframework.ai=DEBUG`** — eleva o nível de log do pacote do Spring AI, permitindo acompanhar nos logs, com detalhe, cada requisição e resposta trocada com o provedor — muito útil para depurar *tool calling* mais adiante.

### 3.4. Teste de integração: `GeminiChatModelIT`

Antes de escrever qualquer controller, o projeto valida a integração através de um teste. O sufixo **`IT`** (*Integration Test*) é uma convenção: diferente de um teste unitário (`...Test`), que roda isolado e rápido, um teste `IT` normalmente depende de recursos externos (aqui, a própria API do Gemini pela rede) e é mais lento e sujeito a falhas de rede ou de cota.

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

Explicando cada peça, na ordem em que aparece:

- **`@SpringBootTest`** — já visto: sobe o contexto completo do Spring, incluindo a auto-configuração que cria o *bean* `GoogleGenAiChatModel`.
- **`@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")`** — anotação do JUnit 5 que **condiciona a execução do teste** à existência de uma variável de ambiente. `matches = ".+"` é uma expressão regular exigindo "um ou mais caracteres quaisquer" — ou seja, "a variável existe e não está vazia". Sem a chave configurada, o teste é **pulado** (não falha, apenas não roda), o que permite que o restante da suíte de testes continue passando em ambientes sem a chave configurada (como um pipeline de CI sem segredos, por exemplo).
- **`@Autowired GoogleGenAiChatModel chatModel`** — injeção de dependência por campo: o Spring identifica que existe um *bean* do tipo `GoogleGenAiChatModel` no contexto (criado pela auto-configuração) e o atribui automaticamente a este campo, sem que o teste precise instanciá-lo manualmente.
- **`GoogleGenAiChatOptions.builder()...build()`** — o padrão de projeto **Builder**: em vez de um construtor com muitos parâmetros (o que ficaria confuso e propenso a erro de ordem), a classe expõe métodos encadeáveis (`.model(...)`, `.temperature(...)`, `.responseMimeType(...)`) que vão configurando um objeto internamente, finalizado por `.build()`. Aqui, essas opções **sobrescrevem**, apenas para esta chamada, o modelo e a temperatura definidos globalmente no `application.properties` (usando `temperature(1.0)` — mais criativo, já que o teste pede para a IA *inventar* um exemplo de gasto).
- **`responseMimeType("text/plain")`** — instrui o Gemini a devolver texto plano, e não, por exemplo, JSON estruturado (outro modo que o Gemini suporta nativamente).
- **`new Prompt(texto, options)`** — construção direta de um `Prompt` a partir de uma `String` (que vira automaticamente uma `UserMessage`) e das opções.
- **`chatModel.call(prompt)`** — a chamada de fato à API do Gemini pela rede.
- **`response.getResult().getOutput().getText()`** — a cadeia de acesso ao texto da resposta: um `ChatResponse` tem um `Result` "principal" (`getResult()` — o Gemini pode, em teoria, devolver múltiplos candidatos, mas por padrão o Spring AI expõe o melhor), esse resultado tem uma mensagem de saída (`getOutput()`, um `AssistantMessage`), e essa mensagem tem seu conteúdo textual (`getText()`).
- **`assertThat(...).isNotEmpty()`** — a asserção da biblioteca **AssertJ**, mais fluente que o `assertEquals` tradicional do JUnit; aqui, apenas confirma que alguma resposta não vazia voltou — o teste não valida o *conteúdo* exato (que é não-determinístico), só que o pipeline técnico funciona de ponta a ponta.

### 3.5. Expondo via HTTP: `ChatModelController`

Com a integração validada pelo teste, o próximo passo é a dependência `spring-boot-starter-web` (já presente no `build.gradle` final) e o controller que expõe o `ChatModel` via HTTP:

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

- **`@RestController`** — anotação que combina `@Controller` (marca a classe como um componente web gerenciado pelo Spring) com `@ResponseBody` (indica que o valor retornado pelos métodos deve ser escrito diretamente no corpo da resposta HTTP — normalmente serializado como texto ou JSON — em vez de ser interpretado como o nome de uma página a renderizar).
- **`@RequestMapping("/api")`** — define um prefixo de URL comum a todos os métodos da classe: todo endpoint aqui começa em `/api`.
- **Injeção via construtor** — o padrão adotado em todo o projeto (em vez de `@Autowired` em campo, como no teste): o Spring identifica que `ChatModelController` precisa de um `GoogleGenAiChatModel` no construtor e injeta o *bean* correspondente automaticamente. Injeção via construtor é considerada uma boa prática por tornar as dependências explícitas, obrigatórias (o objeto não pode existir sem elas) e a classe mais fácil de testar isoladamente (sem exigir o contexto do Spring).
- **`@GetMapping("/chat-model")`** — mapeia requisições HTTP `GET` para `/api/chat-model` a este método.
- **`String chat(String prompt)`** — o Spring, por convenção, associa automaticamente um parâmetro de método sem anotação explícita a um **parâmetro de query string** de mesmo nome (`?prompt=...`) quando o tipo é simples (como `String`).
- **`this.chatModel.call(prompt)`** — usa a versão simplificada do método `call` (a que recebe e devolve `String`).

**Testando manualmente:**

```http
GET http://localhost:8080/api/chat-model?prompt=Oi
```

Deve devolver uma resposta de texto simples gerada pelo Gemini, algo como *"Oi! Como posso ajudar você hoje?"*.

### 3.6. Checkpoint da Parte 3

Confirmado no `.zip`: `ChatModelController.java` existe em `dio.budgeting`, injetando `GoogleGenAiChatModel`, com o único endpoint `GET /api/chat-model`. O teste `GeminiChatModelIT` existe em `src/test/java/dio/budgeting`, validando a chamada crua ao `GoogleGenAiChatModel`.

---

## Parte 4 — ChatClient: a API fluente com contexto (Vídeo 04)

### 4.1. Objetivo

Trocar o `ChatModel` (baixo nível) por `ChatClient` (API fluente, de mais alto nível), que será a peça central do assistente a partir daqui — é ela que, mais adiante, ganhará *tools* e um prompt de sistema.

### 4.2. Conceito: `ChatClient` vs. `ChatModel`

O `ChatClient` é construído **em cima** de um `ChatModel` (ele não substitui a auto-configuração; reaproveita-a). A diferença central é de **expressividade**: enquanto o `ChatModel` trabalha com objetos como `Prompt` e `ChatResponse` montados manualmente, o `ChatClient` oferece uma **API fluente** (métodos encadeados que leem quase como uma frase) para compor a conversa, incluindo:

- uma **mensagem de sistema** (*system message*) — instruções que definem o comportamento geral do assistente, não visíveis ao usuário final, mas que moldam como o modelo interpreta e responde a cada mensagem de usuário;
- uma ou mais **mensagens de usuário** (*user message*) — a entrada direta da pessoa;
- e, como será visto na Parte 5, **ferramentas** (*tools*) que o modelo pode chamar.

### 4.3. Criando o `ChatClient` a partir do `ChatClient.Builder`

O `ChatClient` não é injetado diretamente — ele é **construído** a partir de um `ChatClient.Builder`, que **esse sim** é auto-configurado e injetável pelo Spring Boot:

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

    // Recebe o Builder injetado automaticamente pelo Spring AI
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

- **`ChatClient.Builder`** — um *bean* de escopo `prototype` (uma nova instância é gerada a cada injeção), auto-configurado a partir do `ChatModel` já disponível no contexto (o mesmo `GoogleGenAiChatModel` da Parte 3). Ser `prototype` — e não *singleton* — importa porque cada classe que precisa de um `ChatClient` com uma configuração diferente (um prompt de sistema diferente, tools diferentes) recebe seu próprio `Builder` "limpo" para configurar, sem interferir nos demais.
- **`chatClientBuilder.build()`** — finaliza a configuração (aqui, sem nenhuma customização ainda) e produz o `ChatClient` de fato, guardado como campo `final` da classe.
- **`@RequestParam(value = "prompt", defaultValue = "Olá!")`** — diferente do parâmetro "cru" do `ChatModelController` (Parte 3.5), aqui o parâmetro de query string é declarado explicitamente com `@RequestParam`, permitindo definir um **valor padrão** (`"Olá!"`) caso a requisição não informe `?prompt=...`.
- **`this.chatClient.prompt()`** — inicia a construção fluente de uma nova interação.
- **`.user(prompt)`** — adiciona o texto recebido como uma mensagem do tipo **usuário** (`UserMessage`) a essa interação.
- **`.call()`** — executa a chamada (síncrona) ao modelo.
- **`.content()`** — extrai apenas o texto da resposta, já pronto para uso — o equivalente, em uma linha, à cadeia `getResult().getOutput().getText()` vista com o `ChatModel` puro na Parte 3.

### 4.4. Teste de integração: `GeminiChatClientIT`

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

- **`ChatClient.builder(chatModel)`** — uma forma alternativa (estática) de obter um `Builder`, a partir de um `ChatModel` já em mãos (em vez de injetar o `ChatClient.Builder` pronto), útil em testes onde já se tem o `ChatModel` injetado por outro motivo.
- **`.defaultSystem("Voce é um matematico")`** — define a **mensagem de sistema padrão** para todas as interações feitas por este `ChatClient`: instrui o modelo a se comportar como um matemático antes mesmo de receber a pergunta do usuário. O prefixo `default` nesse método (e nos que virão, como `defaultTools`) indica que essa configuração vale para **todas** as chamadas feitas a partir deste `ChatClient`, a menos que seja explicitamente sobrescrita em uma chamada específica.
- **`chatClient.prompt("...")`** — uma forma abreviada de `chatClient.prompt().user("...")`: quando se passa a `String` diretamente para `prompt(...)`, ela já é tratada como a mensagem do usuário.
- **`assertThat(response).contains("0")`** — em vez de `isEqualTo`, o teste usa `contains` porque o LLM pode devolver texto adicional ao redor do número (“O resultado é 0” em vez de apenas “0”) — uma asserção exata falharia por qualquer variação de fraseado, mesmo com a resposta numérica correta.

Conta que o teste valida: 10 + 20 = 30; 30 − 30 = 0. Neste ponto (antes do Tool Calling, Parte 5), é o **próprio modelo** quem faz essa conta "de cabeça" — o que funciona para aritmética simples, mas não é confiável nem verificável para operações mais complexas, motivando a próxima etapa.

### 4.5. Checkpoint da Parte 4

Confirmado no `.zip`: `ChatClientController.java` existe com o endpoint `GET /api/chat`, e `GeminiChatClientIT.java` existe validando a construção fluente do `ChatClient` a partir do `GoogleGenAiChatModel`.


---

## Parte 5 — Tool Calling: quando a IA executa código de verdade (Vídeo 05)

### 5.1. Objetivo

Substituir a "matemática de cabeça" do modelo por chamadas reais a métodos Java, introduzindo o conceito central que sustenta o assistente inteiro: **Tool Calling**.

### 5.2. Conceito: Tool Calling (Function Calling)

**Tool Calling** — também chamado de *Function Calling* — é um padrão em que um LLM, ao processar um prompt, pode decidir que a melhor forma de responder não é gerar texto diretamente, mas **solicitar a execução de uma função/método específico**, com argumentos que ele mesmo extrai do contexto da conversa. O fluxo típico é:

1. A aplicação informa ao modelo quais *tools* (ferramentas) estão disponíveis, cada uma com um nome, uma descrição e uma assinatura de parâmetros.
2. O modelo recebe o prompt do usuário e decide, sozinho, se alguma *tool* deveria ser chamada — e, se sim, com quais argumentos.
3. **O modelo não executa nada**: ele apenas *pede* a chamada. É a aplicação (o Spring AI, neste caso) quem efetivamente invoca o método Java correspondente.
4. O resultado dessa chamada volta para o modelo como uma nova mensagem no histórico da conversa, e o modelo usa esse resultado para formular a resposta final ao usuário.

Os dois usos citados na documentação do Spring AI resumem bem o motivo de existir esse recurso: **Information Retrieval** (buscar dados que o modelo não tem — como o conteúdo atual de um banco de dados) e **Taking Action** (executar uma ação real no sistema — como salvar um registro).

### 5.3. A anotação `@Tool`

Uma *tool* é declarada anotando um método Java comum com `@Tool`:

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

- **`@Tool(description = "...")`** — a `description` é o texto que o modelo lê para decidir *quando* e *por que* chamar esse método; quanto mais claro e específico, melhor o modelo acerta a decisão. Ela funciona como uma "bula" da ferramenta, escrita para a IA, não para outro desenvolvedor.
- O Spring AI usa **reflexão** (a capacidade do Java de inspecionar classes, métodos e parâmetros em tempo de execução) para descobrir automaticamente o nome dos parâmetros (`a`, `b`) e seus tipos (`int`), gerando um "esquema" que é enviado ao modelo junto da descrição.

### 5.4. Registrando as tools no `ChatClient`: `defaultTools`

```java
var chatClient = ChatClient.builder(openAiChatModel)
        .defaultSystem("Você é um matemático")
        .defaultTools(new MathTools())
        .build();
```

- **`.defaultTools(new MathTools())`** — registra uma **instância** da classe de ferramentas como disponível para todas as chamadas deste `ChatClient`. É importante notar (o curso original passa por esse erro e o corrige) que registrar a tool via `.tools(...)` **na hora do `prompt()`** não funciona da mesma forma — o registro precisa acontecer na **construção** do `ChatClient` via `.defaultTools(...)`, para que ele fique disponível de forma consistente em toda a vida útil daquele cliente.

### 5.5. Teste de integração: `ToolCallingIT`

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

Esse teste é estruturalmente idêntico ao da Parte 4.4, mas agora com `.defaultTools(new MathTools())` adicionado. A diferença **não é visível no código do teste** — está no *comportamento* interno: em vez do modelo "adivinhar" a soma e a subtração a partir do seu conhecimento estatístico de linguagem, ele agora **delega** o cálculo para os métodos `sum` e `diff`, que executam a operação matematicamente exata em Java.

**Como confirmar que a tool foi realmente usada** (e não o modelo "de cabeça"): com `logging.level.org.springframework.ai=DEBUG` ativo (Parte 3.3), os logs de execução do teste mostram entradas do `DefaultToolCallingManager` e do `MethodToolCallback`, evidenciando as chamadas reais a `sum` e a `diff` — e a conversão de cada retorno para JSON antes de ser devolvido ao modelo, que então usa esses valores exatos para compor a resposta final.

### 5.6. Checkpoint da Parte 5

Confirmado no `.zip`: `ToolCallingIT.java` existe em `dio.budgeting`, com a classe interna `MathTools` e o teste `should_executeSum_when_prompted`, usando `GoogleGenAiChatModel` e `defaultTools`. Este teste é o "protótipo" conceitual do padrão que, a partir da Parte 8, será aplicado aos casos de uso reais do domínio (`PersistTransactionUseCase`, `ListTransactionsByCategoryUseCase`).

---

## Parte 6 — Transcrevendo áudio em texto: o primeiro ponto sem equivalente Gemini (Vídeo 06)

### 6.1. Objetivo

Transformar um arquivo de áudio (a fala do usuário) em texto processável — o primeiro passo real do pipeline **Áudio → STT → Tool Calling → TTS → Áudio**.

### 6.2. O caminho ensinado no curso: `TranscriptionModel` (OpenAI/Whisper)

O Spring AI define, para transcrição, uma interface dedicada:

```java
public interface TranscriptionModel extends Model<AudioTranscriptionPrompt, AudioTranscriptionResponse> {
    AudioTranscriptionResponse call(AudioTranscriptionPrompt transcriptionPrompt);

    default String transcribe(Resource resource) {
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(resource);
        return this.call(prompt).getResult().getOutput();
    }
}
```

- **`Resource`** — uma abstração do Spring (não específica de IA) para representar "algo que pode ser lido como bytes", seja um arquivo no disco, um arquivo no *classpath*, um array de bytes em memória, ou um arquivo recebido em uma requisição HTTP. É o tipo usado para representar o áudio de entrada, independentemente de sua origem.
- **`transcribe(Resource resource)`** — método de conveniência: você passa o áudio, recebe direto a `String` transcrita, sem precisar montar manualmente um `AudioTranscriptionPrompt`.

No momento em que o curso ensina esse conteúdo, o **único provedor suportado** pelo Spring AI para `TranscriptionModel` é a **Whisper API da OpenAI** (e sua variante no Azure OpenAI) — Whisper sendo o modelo de reconhecimento de fala de propósito geral, multilíngue, desenvolvido pela própria OpenAI. A configuração ensinada usa propriedades como:

```properties
spring.ai.model.audio.transcription=openai
spring.ai.openai.audio.transcription.options.model=whisper-1
spring.ai.openai.audio.transcription.options.language=pt
spring.ai.openai.audio.transcription.options.temperature=0
spring.ai.openai.audio.transcription.options.response-format=text
```

### 6.3. Por que essa rota não existe no projeto Gemini

O `spring-ai-starter-model-google-genai` — o *starter* usado neste projeto — **não implementa a interface `TranscriptionModel`**. O Gemini, ao contrário do Whisper, não é um modelo especializado só em transcrição: ele é um modelo **multimodal** de propósito geral, capaz de receber texto, imagem, áudio e vídeo *dentro da mesma conversa de chat*, e responder com base em tudo isso combinado. Ou seja, no ecossistema Gemini, "transcrever um áudio" não é uma API separada — é apenas **uma conversa de chat em que uma das mensagens contém áudio anexado**, com um prompt de texto pedindo para transcrever esse áudio.

### 6.4. A solução adotada: `GoogleGenAiChatModel` + `Media`

O projeto reaproveita o mesmo `GoogleGenAiChatModel` já usado desde a Parte 3, mas monta uma mensagem de usuário com **conteúdo multimídia** anexado, usando a classe `Media` do Spring AI:

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

- **`"""..."""`** — um **text block** do Java (recurso disponível desde o Java 15), uma forma de declarar *strings* multilinha sem precisar concatenar `"linha 1\n" + "linha 2\n"` manualmente. É ideal para prompts longos e legíveis.
- **`Media`** — classe do Spring AI que empacota um conteúdo não-textual (aqui, áudio) junto do seu tipo MIME, para ser anexado a uma mensagem.
- **`MimeTypeUtils.parseMimeType("audio/mpeg")`** — declara explicitamente que o conteúdo anexado é um áudio no formato MPEG (MP3), informação que o Gemini usa para saber como interpretar os bytes recebidos.
- **`file.getResource()`** — converte o `MultipartFile` (o arquivo recebido na requisição HTTP, explicado na Parte 6.5) para um `Resource`, que é o tipo esperado por `Media`.
- **`UserMessage.builder().text(...).media(List.of(audioMedia)).build()`** — constrói uma mensagem de usuário que combina **texto** (o prompt de instrução, pedindo a transcrição) **e** o **áudio anexado** — a essência da multimodalidade: uma única mensagem carregando dois tipos de conteúdo simultaneamente.
- **`Prompt.builder().messages(List.of(userMessage)).build()`** — monta o `Prompt` final a partir dessa única mensagem multimodal.
- **`chatModel.call(prompt).getResult().getOutput().getText()`** — a mesma cadeia de acesso já vista na Parte 3.4, agora devolvendo o texto transcrito em vez de uma resposta de chat comum — porque, do ponto de vista do modelo, não há distinção estrutural entre "responder a uma pergunta" e "transcrever um áudio": ambos são apenas "gerar texto a partir de uma mensagem de entrada".

### 6.5. `MultipartFile` — recebendo um arquivo por HTTP

```java
@PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
String transcribe(@RequestParam("file") MultipartFile file) { ... }
```

- **`consumes = MediaType.MULTIPART_FORM_DATA_VALUE`** — declara que este endpoint só aceita requisições cujo corpo é `multipart/form-data`, o formato padrão usado por navegadores e clientes HTTP para enviar arquivos binários (em vez de JSON puro).
- **`MultipartFile`** — a abstração do Spring Web para um arquivo recebido dentro de uma requisição multipart, com métodos para acessar seu conteúdo (`getBytes()`, `getInputStream()`) ou, como usado aqui, convertê-lo diretamente em um `Resource` do Spring.
- **`@RequestParam("file")`** — associa este parâmetro à parte da requisição multipart nomeada `"file"`.

### 6.6. Teste de integração: `GeminiTranscriptionModelIT`

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

- **`@ParameterizedTest` + `@CsvSource({...})`** — em vez de escrever seis testes quase idênticos (um por arquivo de áudio), o JUnit executa o **mesmo método de teste várias vezes**, uma por linha do `@CsvSource`, injetando cada valor separado por vírgula como argumento do método (`fileName`, `expectedKeyword`). É a forma padrão de testar a mesma lógica contra múltiplos casos de entrada sem duplicar código.
- **`ClassPathResource("audio/" + fileName)`** — carrega um arquivo a partir do *classpath* (aqui, `src/test/resources/audio/`), retornando um `Resource` pronto para ser usado como `Media`.
- **`assertThat(recording.exists()).isTrue()`** — uma verificação defensiva antes mesmo de chamar a API: garante que o arquivo de teste realmente existe no lugar esperado, evitando que uma falha de "arquivo não encontrado" seja confundida com uma falha real de transcrição.
- **`containsIgnoringCase(expectedKeyword)`** — variante do `contains` que ignora maiúsculas/minúsculas — mais uma vez, uma asserção **flexível**, porque a transcrição de um LLM não é garantidamente idêntica byte a byte a cada execução (o modelo pode escrever "80 Reais" ou "80 reais", por exemplo).
- **Observação sobre o comportamento real dos modelos de fala:** tanto o curso (com Whisper) quanto a experiência com Gemini mostram que transcrição de números é um ponto sensível — o modelo pode escrever um valor por extenso ("duzentos reais") em vez de numérico ("200 reais"), o que pode fazer uma asserção mais rígida falhar mesmo com uma transcrição semanticamente correta. É por isso que o prompt de transcrição (Parte 6.4) é explícito ao contextualizar o modelo sobre o domínio (gastos financeiros), ajudando a guiar o formato da saída.

### 6.7. Checkpoint da Parte 6

Confirmado no `.zip`: **não existe** nenhuma classe `TranscriptionModel`, `TranscriptionController` vazio, nem propriedades `spring.ai.*.audio.transcription.*` no projeto final — a rota da OpenAI/Whisper não foi implementada. Em vez disso, o arquivo `TranscriptionController.java` (cujo estado final completo será visto na Parte 11, pois ele acumula várias responsabilidades ao longo dos Vídeos 06 e 11) já nasce usando o `GoogleGenAiChatModel` multimodal para transcrição, e os seis áudios de teste (`recording-1.mp3` a `recording-6.mp3`) estão em `src/test/resources/audio/`, validados por `GeminiTranscriptionModelIT`.


---

## Parte 7 — Sintetizando voz: o segundo ponto sem equivalente Gemini (Vídeo 07)

### 7.1. Objetivo

Fechar o outro extremo do pipeline: transformar a resposta textual do assistente de volta em áudio, para que a interação seja falada, e não apenas escrita.

### 7.2. O caminho ensinado no curso: `TextToSpeechModel` (OpenAI)

Assim como para transcrição, o Spring AI define uma interface comum para síntese de voz:

```java
public interface TextToSpeechModel extends Model<TextToSpeechPrompt, TextToSpeechResponse>, StreamingTextToSpeechModel {
    default byte[] call(String text) { ... }
    TextToSpeechResponse call(TextToSpeechPrompt prompt);
    default TextToSpeechOptions getDefaultOptions() { ... }
}
```

Nesta interface, `call(String text)` devolve diretamente um `byte[]` (o áudio já pronto), e a versão com `TextToSpeechPrompt` permite customizar voz, velocidade e formato. No momento em que essa parte do curso é gravada, os únicos provedores suportados pelo Spring AI para essa interface são a **Speech API da OpenAI** e a **API da Eleven Labs** — novamente, **sem suporte ao Gemini**.

### 7.3. Por que essa rota não existe no projeto Gemini

Pela mesma razão da Parte 6: o `spring-ai-starter-model-google-genai` não implementa `TextToSpeechModel`. O Gemini oferece geração de áudio (*text-to-speech*), mas através de um modelo específico dentro da própria API do Google GenAI (`gemini-2.5-flash-preview-tts`), acessado via **configurações de geração de conteúdo** (`GenerateContentConfig` com `responseModalities("AUDIO")`) — um mecanismo que ainda não tem um "encaixe" pronto dentro da abstração `TextToSpeechModel` do Spring AI.

### 7.4. A solução adotada: o SDK nativo `com.google.genai.Client`

Em vez de depender de uma interface do Spring AI que não existe para este caso, o projeto usa diretamente o **SDK Java oficial do Google GenAI** — a mesma biblioteca de baixo nível que o próprio `spring-ai-starter-model-google-genai` usa por baixo dos panos para implementar o `GoogleGenAiChatModel`. Isso é feito na classe `TextToSpeechService`:

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
    // ... wrapPcmAsWav explicado a seguir
}
```

Explicando peça por peça:

- **`@Service`** — anotação de estereótipo do Spring que marca a classe como um componente de **lógica de negócio/serviço**, tornando-a elegível para injeção de dependência em outras classes (como acontece na Parte 11, quando `TranscriptionController` passa a depender dela).
- **`@Value("${spring.ai.google.genai.api-key}") String apiKey`** — em vez de injetar um *bean* pronto (como `@Autowired`), esta anotação injeta o **valor de uma propriedade** de configuração diretamente como parâmetro do construtor — aqui, reaproveitando a mesma chave já configurada para o `GoogleGenAiChatModel` (Parte 1.5), em vez de duplicar a leitura da variável de ambiente.
- **Validação defensiva no construtor** — se a propriedade não resolver para um valor útil (`StringUtils.hasText` verifica que a *string* não é nula, vazia, ou só espaços em branco), a classe lança um `IllegalArgumentException` **imediatamente na inicialização**, com uma mensagem clara sobre a causa — falhar cedo e com uma mensagem útil é preferível a deixar o erro aparecer, confuso, apenas na primeira tentativa de uso.
- **`Client.builder().apiKey(apiKey).build()`** — cria o cliente de baixo nível do SDK do Google GenAI, o mesmo tipo de objeto que faz a comunicação HTTP com a API do Gemini por baixo do `GoogleGenAiChatModel` do Spring AI — só que, aqui, instanciado e gerenciado manualmente pela aplicação.
- **`@PreDestroy`** — anotação padrão do Java (pacote `jakarta.annotation`) que marca um método para ser chamado automaticamente pelo Spring **antes de o bean ser destruído** (por exemplo, ao encerrar a aplicação). Aqui, garante que a conexão/recursos do `geminiClient` sejam liberados corretamente (`geminiClient.close()`), evitando vazamento de recursos (*resource leak*).
- **`GenerateContentConfig`, `SpeechConfig`, `VoiceConfig`, `PrebuiltVoiceConfig`** — uma cadeia de *builders* aninhados (do próprio SDK do Google, não do Spring AI) que configura a chamada: `responseModalities("AUDIO")` pede explicitamente uma resposta em áudio (em vez de texto, o padrão); `voiceName("Kore")` seleciona uma das vozes pré-definidas oferecidas pelo Gemini para TTS.
- **`geminiClient.models.generateContent("gemini-2.5-flash-preview-tts", text, config)`** — a chamada de fato à API, informando o modelo específico de TTS do Gemini, o texto a converter e a configuração montada.
- **A cadeia de extração do áudio** (`response.candidates()...flatMap(...)...orElse(...)`) — usa o tipo `Optional` do Java de forma encadeada (`flatMap`) para navegar com segurança por uma estrutura de resposta profundamente aninhada (resposta → lista de candidatos → primeiro candidato → seu conteúdo → as partes desse conteúdo), sem lançar `NullPointerException` caso algum nível esteja ausente — se qualquer etapa da cadeia estiver vazia, o resultado final é uma lista vazia (`orElse(new ArrayList<>())`), tratada com segurança no passo seguinte.
- **`part.inlineData().flatMap(Blob::data)`** — dentro de cada "parte" da resposta, o áudio efetivamente gerado vem como dado binário embutido (*inline data*, um `Blob`); a cadeia extrai esses bytes brutos.
- **`.findFirst().orElseThrow(...)`** — pega o primeiro pedaço de áudio disponível ou lança uma exceção HTTP 500 (`INTERNAL_SERVER_ERROR`) com uma mensagem clara, caso o Gemini não tenha retornado áudio algum — outro exemplo de tratamento defensivo de um cenário inesperado.

### 7.5. O problema do PCM cru e a montagem manual do cabeçalho WAV

O áudio devolvido pelo Gemini não é um arquivo `.wav` ou `.mp3` pronto — é **PCM cru** (*Pulse Code Modulation* — a representação digital mais básica de uma onda sonora, apenas amostras de amplitude, sem nenhum cabeçalho ou metadado). Para que esse áudio possa ser salvo em um arquivo `.wav` reproduzível por qualquer player padrão, é preciso **construir manualmente o cabeçalho WAV** na frente dos dados brutos:

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

- **O formato WAV** é, na essência, um cabeçalho fixo de **44 bytes** seguido diretamente pelos dados de áudio brutos (PCM). Esse cabeçalho segue a especificação **RIFF** (*Resource Interchange File Format*), um formato genérico de "contêiner" usado por vários tipos de arquivo multimídia (WAV entre eles).
- **`ByteBuffer`** — classe do Java (pacote `java.nio`) para manipular sequências de bytes de forma estruturada, permitindo escrever valores de diferentes tamanhos (inteiros de 32 bits, inteiros curtos de 16 bits, texto) em posições sequenciais de um buffer, sem precisar calcular manualmente os deslocamentos de cada campo.
- **`ByteOrder.LITTLE_ENDIAN`** — define a **ordem dos bytes** (*endianness*) usada ao escrever números com mais de um byte. Em *little-endian*, o byte menos significativo vem primeiro; é a ordem exigida pela especificação do formato WAV — se a ordem estivesse errada (*big-endian*, por exemplo), o arquivo resultante seria interpretado incorretamente por qualquer player.
- **`"RIFF"`, `"WAVE"`, `"fmt "`, `"data"`** — identificadores de texto fixo (*chunk IDs*) exigidos pela especificação RIFF/WAV, marcando o início de cada seção do cabeçalho.
- **`putInt(36 + dataSize)`** — o tamanho total do arquivo menos 8 bytes (os dois primeiros campos do cabeçalho RIFF não entram nessa contagem); `36` é o tamanho fixo do restante do cabeçalho.
- **`putShort((short) 1)`** — o código de formato de áudio: `1` significa PCM não comprimido (o formato mais simples e universal).
- **`sampleRate`, `channels`, `bitsPerSample`** — os três parâmetros que descrevem tecnicamente como interpretar os bytes de áudio: **taxa de amostragem** (quantas amostras de som por segundo — aqui, `24000`, ou seja, 24kHz, o valor fixo que o modelo de TTS do Gemini usa), **número de canais** (`1` = mono, um único canal de áudio — em oposição a `2`, estéreo) e **profundidade de bits** (`16` bits por amostra, controlando a resolução/precisão de cada amostra de som).
- **`byteRate`** e **`blockAlign`** — valores derivados matematicamente dos três parâmetros acima, exigidos pelo cabeçalho para que qualquer player consiga calcular corretamente a duração e a taxa de reprodução do áudio.

Esse mesmo cálculo (`24000` Hz, `1` canal, `16` bits) é o valor **fixo e conhecido** documentado pela API de TTS do Gemini para esse modelo específico — não é algo que a aplicação descobre dinamicamente a partir da resposta, é uma constante da própria API.

### 7.6. `TextToSpeechController`: expondo a síntese via HTTP

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

- Note que a lógica de chamada ao Gemini (a construção do `GenerateContentConfig`, a extração do áudio, o *wrap* em WAV) **não está no controller** — ela foi extraída para o `TextToSpeechService` (Parte 7.4), um `@Service` reutilizável. Essa separação existe porque, como será visto na Parte 11, **duas rotas diferentes** da aplicação (`/api/synthesize`, aqui, e `/api/ai`, o fluxo completo de voz para voz) precisam sintetizar áudio — extrair essa lógica para um serviço evita duplicar o código de configuração do Gemini e da montagem do WAV em dois lugares.
- **`produces = "audio/wav"`** — declara o tipo de conteúdo (`Content-Type`) da resposta HTTP, informando ao cliente que o corpo da resposta é um áudio no formato WAV — coerente com o `wrapPcmAsWav(...)` feito no serviço.
- **`ByteArrayResource`** — uma implementação de `Resource` (a mesma abstração vista na Parte 6.2) construída diretamente a partir de um array de bytes em memória, sem precisar de um arquivo físico no disco.
- **`ContentDisposition.attachment().filename("audio.wav").build()`** — monta o cabeçalho HTTP `Content-Disposition: attachment; filename="audio.wav"`, que instrui o cliente (navegador, ferramenta HTTP) a tratar a resposta como um **arquivo para download/salvar**, sugerindo o nome `audio.wav`, em vez de tentar exibi-la inline.
- **`record SynthesizeRequest(String text)`** — um `record` (Parte 8.3 explica esse recurso em detalhe) usado como corpo esperado da requisição `POST`: um JSON simples com um único campo `text`.

### 7.7. Teste de integração: `GeminiSpeechModelIT`

O teste correspondente (`GeminiSpeechModelIT`) usa o SDK diretamente (sem passar pelo `TextToSpeechService`, já que este teste antecede a extração do serviço), repetindo a mesma lógica de configuração e o mesmo `wrapPcmAsWav`, mas salvando o resultado em um arquivo temporário para conferência manual:

```java
Path tempFile = Files.createTempFile("AUDIO_", ".wav");
Files.write(tempFile, wavAudio);
System.out.println(tempFile.toAbsolutePath());
```

- **`Files.createTempFile("AUDIO_", ".wav")`** — cria um arquivo temporário no diretório padrão do sistema operacional (em Linux, geralmente `/tmp`), com um nome único gerado automaticamente (prefixo `AUDIO_`, sufixo `.wav`).
- Ao rodar o teste manualmente e abrir o caminho impresso no console, é possível **ouvir** o áudio sintetizado e conferir de fato se a fala corresponde ao texto enviado — um passo de validação manual, complementar às asserções automáticas (`assertThat(pcmAudio).hasSizeGreaterThan(1024)`, que só confirma que *algum* áudio de tamanho razoável foi recebido, sem validar seu conteúdo).

### 7.8. Checkpoint da Parte 7

Confirmado no `.zip`: `TextToSpeechService.java` existe como `@Service`, encapsulando o SDK nativo do Gemini e o *wrap* PCM → WAV; `TextToSpeechController.java` existe com o endpoint `POST /api/synthesize` (produzindo `audio/wav`, e não `audio/mp3` como no protótipo original da OpenAI); `GeminiSpeechModelIT.java` valida a geração de áudio de forma independente, salvando em arquivo temporário para audição manual.


---

## Parte 8 — O domínio do negócio: `Transaction`, `Category` e o primeiro caso de uso (Vídeo 08)

### 8.1. Objetivo

Até aqui, o projeto sabia conversar com o Gemini, transcrever áudio e sintetizar voz — mas ainda não tinha nenhuma noção do que é, de fato, uma "transação financeira" dentro da aplicação. Esta parte constrói essa representação, junto da primeira operação real de negócio: persistir uma transação.

### 8.2. Conceito: Domain-Driven Design e Clean Architecture

O projeto passa a organizar o código Java em três pacotes, dentro de `dio.budgeting`, cada um com uma responsabilidade clara:

- **`domain`** — as regras e entidades centrais do negócio: o que é uma transação, quais categorias existem, e o **contrato** (interface) de como ela deve poder ser persistida — sem nenhum detalhe de "como" isso é feito na prática.
- **`application`** — os **casos de uso** (*use cases*): as ações que a aplicação sabe realizar, como "persistir uma transação" ou "listar transações de uma categoria".
- **`infrastructure`** — as implementações técnicas concretas dessas regras — como, de fato, acessar um banco de dados (Parte 9) ou expor um endpoint HTTP (Parte 10).

Essa separação é a aplicação prática de dois conceitos de arquitetura de software:

- **Domain-Driven Design (DDD)** — uma abordagem em que o código é organizado em torno do domínio do negócio (aqui, "transações financeiras"), mantendo essas regras isoladas de detalhes técnicos de infraestrutura (banco de dados, frameworks web).
- **Clean Architecture** — um estilo de arquitetura (popularizado por Robert C. Martin) em que camadas mais internas (o domínio) **não dependem** das mais externas (a infraestrutura) — é o contrário: o domínio define apenas *o quê* precisa ser feito (uma interface), e é a infraestrutura quem fornece o *como* (a implementação concreta). Essa "inversão" é o que permite, por exemplo, trocar o banco de dados usado sem alterar nenhuma regra de negócio.

### 8.3. `TransactionId`: um identificador fortemente tipado

```java
package dio.budgeting.domain;

import java.util.UUID;

public record TransactionId(UUID uuid) {
    public TransactionId() {
        this(UUID.randomUUID());
    }
}
```

- **`record`** — um recurso do Java (desde a versão 16) para declarar, de forma compacta, uma classe imutável focada em carregar dados. Ao escrever `record TransactionId(UUID uuid)`, o compilador gera automaticamente: um construtor que recebe um `UUID`; um método de acesso `uuid()` (sem o prefixo `get`, diferente de uma classe tradicional); e implementações corretas de `equals()`, `hashCode()` e `toString()` — tudo sem que o programador precise escrever esse código repetitivo manualmente.
- **`UUID`** (*Universally Unique Identifier*, do pacote `java.util`) — um valor de 128 bits praticamente impossível de colidir com outro gerado em qualquer lugar do mundo, ideal para gerar identificadores únicos sem depender de um contador central (como um `AUTO_INCREMENT` de banco de dados).
- **Identificador fortemente tipado** (*strongly-typed ID*) — em vez de representar o id de uma transação como uma `String` ou `UUID` "solto" circulando pelo código, cria-se um tipo próprio (`TransactionId`). Isso permite que o compilador impeça, por exemplo, que o id de uma transação seja confundido com o id de outra entidade qualquer — mesmo que ambos sejam, por baixo, apenas um `UUID`, eles nunca serão intercambiáveis por engano.
- **Construtor auxiliar `public TransactionId()`** — como o construtor gerado automaticamente pelo `record` **exige** um `UUID` já pronto, este construtor extra, sem parâmetros, resolve esse problema chamando `this(UUID.randomUUID())` — gerando um novo identificador aleatório sempre que uma transação nova (ainda sem id vindo de fora) é criada.

### 8.4. `Category`: um `enum` para valores fixos e conhecidos

```java
package dio.budgeting.domain;

public enum Category {
    GROCERIES,
    PHARMA,
    AUTO
}
```

- **`enum`** (*enumeration*) — um tipo especial do Java para representar um conjunto **fixo e conhecido** de valores possíveis. Em vez de uma `String` livre (que aceitaria qualquer texto, incluindo erros de digitação como `"Groceriess"`), um `enum` garante, já em tempo de compilação, que apenas um desses valores exatos pode ser usado onde uma `Category` é esperada.
- **`GROCERIES`, `PHARMA`, `AUTO`** — as três categorias suportadas nesta versão do projeto: mercado/compras, farmácia e gastos automotivos. Essa lista é o primeiro candidato natural de expansão do projeto (ver a seção de Próximos Passos, ao final deste documento).

### 8.5. `Transaction`: a entidade de domínio

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

- **Campos `private`** — o modificador `private` restringe a visibilidade de um campo apenas à própria classe, a base do princípio de **encapsulamento** da orientação a objetos: os dados internos ficam protegidos de alteração direta vinda de fora, sendo acessados apenas através de métodos controlados (os *getters*, explicados a seguir).
- **`double amount`** — diferente de um valor guardado em centavos (`long`), aqui o valor já é armazenado como número decimal, representando **reais** diretamente. Essa é uma decisão que vale a pena destacar: em software financeiro, é mais comum guardar valores monetários como um inteiro em centavos, justamente para evitar os pequenos erros de arredondamento característicos de números de ponto flutuante (`double`) em operações matemáticas repetidas. O projeto opta por manter o domínio em `double`/reais, e concentra a conversão de centavos-para-reais **na borda** — no `PersistTransactionUseCase` (Parte 8.7) — mantendo a entidade de domínio já no formato "pronto para exibir".
- **Dois construtores**: o primeiro, mais completo (gerado por `@AllArgsConstructor`, explicado a seguir), aceita todos os campos incluindo o `id` — usado quando uma transação já existente (vinda do banco de dados) precisa ser reconstruída em memória. O segundo, escrito manualmente, aceita apenas os dados que vêm "de fora" (`description`, `amount`, `category`) e **gera o id internamente** (`new TransactionId()`) — usado ao criar uma transação nova, já que não faz sentido pedir para quem cria uma transação também inventar um identificador único para ela.

### 8.6. Lombok: eliminando código repetitivo

**Lombok** é uma biblioteca Java que gera, em tempo de compilação, código repetitivo (*boilerplate*) — como *getters*, *setters* e construtores — a partir de anotações simples, evitando que o programador precise escrever (e manter) esse código manualmente. Ela é adicionada ao projeto através de um plugin no `build.gradle`:

```groovy
plugins {
    id 'io.freefair.lombok' version '9.2.0'
}
```

- **`io.freefair.lombok`** — um plugin Gradle de terceiros (não oficial do Lombok, mas amplamente usado) que integra o Lombok ao processo de compilação do Gradle, cuidando automaticamente de configurar o *annotation processor* necessário.

As duas anotações usadas em `Transaction`:

- **`@Getter`** — gera automaticamente um método `getXxx()` público para cada campo privado da classe (`getId()`, `getDescription()`, `getAmount()`, `getCategory()`), sem que o programador precise escrevê-los manualmente.
- **`@AllArgsConstructor`** — gera automaticamente um construtor que recebe **todos** os campos da classe, na ordem em que foram declarados — no caso de `Transaction`, um construtor `Transaction(TransactionId id, String description, double amount, Category category)`, que complementa o construtor manual de três argumentos já existente (Java permite múltiplos construtores, desde que suas assinaturas — tipos e quantidade de parâmetros — sejam diferentes, um recurso chamado **sobrecarga de construtores**, *constructor overloading*).

### 8.7. `TransactionRepository`: o contrato de persistência (no domínio)

```java
package dio.budgeting.domain;

import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    List<Transaction> findAllByCategory(Category category);
}
```

Esta interface vive dentro de `domain` — o pacote mais interno da arquitetura — e **não sabe nada** sobre bancos de dados, SQL, ou JPA. Ela apenas declara *o que* a aplicação precisa poder fazer com uma transação: salvá-la e buscá-la por categoria. É exatamente o "contrato" mencionado na Parte 8.2: quem implementa essa interface (a camada de `infrastructure`, na Parte 9) decide *como* isso é feito de verdade.

### 8.8. `PersistTransactionUseCase`: o primeiro caso de uso

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

- **`@Service` + injeção via construtor de `TransactionRepository`** — o mesmo padrão já visto em outros controllers: o caso de uso depende apenas da **interface** de domínio, não de uma implementação concreta. Isso significa que, mesmo antes de a Parte 9 implementar a persistência real em banco, esse código já está pronto e correto — basta que exista alguma implementação de `TransactionRepository` disponível no contexto do Spring.
- **`@Tool(name = "persistTransaction", description = "...")`** — este é o momento em que o padrão de Tool Calling estudado na Parte 5 é aplicado a um caso de uso **real** do negócio, e não mais a um exemplo didático de soma e subtração: o próprio método `execute` de um caso de uso vira uma ferramenta que o LLM pode chamar diretamente. O parâmetro `name` explicita o identificador da ferramenta para o modelo (necessário, como será visto na Parte 11, para evitar ambiguidade quando várias *tools* diferentes têm métodos chamados `execute`).
- **`input.amount() / 100.0`** — a conversão de **centavos** (a unidade em que o valor chega, tanto vindo da API REST quanto vindo da extração feita pela IA a partir da fala) para **reais** (a unidade em que o domínio guarda o valor, como decidido na Parte 8.5). Note o `100.0` (e não `100`): dividir um `long` por um número inteiro em Java faz uma **divisão inteira** (descartando a parte decimal); ao dividir por `100.0` (um `double`), a divisão é forçada a ser de ponto flutuante, preservando os centavos como casas decimais.
- **`new Transaction(input.description(), amountInReais, input.category())`** — usa o construtor de três argumentos de `Transaction` (Parte 8.5), que gera o `TransactionId` internamente — reforçando que, do ponto de vista deste caso de uso, criar uma transação nova nunca exige que quem a cria também escolha seu identificador.
- **`TransactionOutput.from(transaction)`** — converte o objeto de domínio (`Transaction`) para um objeto de saída específico do caso de uso (`TransactionOutput`), explicado a seguir.

### 8.9. `PersistTransactionInput` e `TransactionOutput`: DTOs de entrada e saída

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

- **DTO** (*Data Transfer Object*) — um objeto cuja única responsabilidade é carregar dados entre camadas ou processos, sem lógica de negócio própria. `PersistTransactionInput` (o que o caso de uso recebe) e `TransactionOutput` (o que ele devolve) são exemplos disso — eles isolam o caso de uso do formato exato usado pela camada de domínio (`Transaction`) e pelas camadas externas (o request/response HTTP da Parte 10, ou a extração feita pela IA na Parte 11).
- **`@ToolParam(description = "...")`** — o equivalente, para **parâmetros** de uma *tool*, da `description` já vista em `@Tool` (Parte 5.3): dá contexto ao modelo sobre o significado de cada campo, especialmente útil quando os parâmetros formam um objeto mais complexo (como este `record`, com três campos), em vez de tipos primitivos simples. Aqui, é explicitado que `amount` é esperado **em centavos** — informação essencial para que o modelo formate corretamente o valor extraído da fala do usuário (por exemplo, "R$ 80" deve virar `8000`, não `80`).
- **Por que `PersistTransactionInput.category` não tem `@ToolParam`?** Conferido diretamente no código: apenas `description` e `amount` estão anotados; `category` não recebeu uma descrição própria neste ponto do desenvolvimento. Isso não impede o funcionamento — o Spring AI ainda expõe `category` ao modelo (usando o nome do campo e o fato de ser um `enum`, cujos valores possíveis já ficam implícitos), mas um `@ToolParam(description = "...")` explicando, por exemplo, "a categoria do gasto, escolhida entre as opções disponíveis" tornaria a instrução ainda mais clara para o modelo — um ajuste pequeno e um bom candidato de melhoria (ver Próximos Passos).
- **`BigDecimal` e `RoundingMode`** — ao converter o `double` interno de volta para uma saída "apresentável", o código usa `BigDecimal` (uma classe do Java para representar números decimais com precisão arbitrária, sem os erros de arredondamento de `double`) apenas para **arredondar** o valor para duas casas decimais (`setScale(2, RoundingMode.HALF_UP)` — arredondamento "para cima" a partir do dígito 5, a regra de arredondamento mais comum no dia a dia) antes de convertê-lo de volta para `double` — uma forma de garantir que um valor como `125.335000001` (um artefato comum de imprecisão de ponto flutuante) seja exibido de forma limpa, como `125.34`.

### 8.10. Checkpoint da Parte 8

Confirmado no `.zip`: os pacotes `domain` (`Transaction`, `TransactionId`, `Category`, `TransactionRepository`) e `application` (`PersistTransactionUseCase`, `application/input/PersistTransactionInput`, `application/output/TransactionOutput`) existem exatamente como descrito acima. O pacote `infrastructure` já existe neste ponto do projeto, mas sua implementação real da persistência só é construída na Parte 9. O `build.gradle` já inclui o plugin `io.freefair.lombok`.


---

## Parte 9 — Persistência de verdade: MySQL via Docker Compose e Spring Data JPA (Vídeo 09)

### 9.1. Objetivo

Implementar, de fato, a interface `TransactionRepository` definida na Parte 8.7, persistindo transações em um banco de dados relacional real — sem que o domínio precise saber nada sobre isso.

### 9.2. Conceito: containers e Docker Compose

**Docker** é uma tecnologia de **containers**: em vez de instalar um banco de dados diretamente no sistema operacional (com todos os riscos de conflito de versão, configuração manual e "funciona na minha máquina"), o banco roda dentro de um ambiente isolado e reprodutível (o *container*), a partir de uma **imagem** pré-configurada (aqui, `mysql:9.6`, a imagem oficial do MySQL na versão 9.6). **Docker Compose** é uma ferramenta para descrever, em um único arquivo YAML, um ou mais serviços de container e como eles devem ser configurados e conectados entre si — ideal para ambientes de desenvolvimento com múltiplas dependências externas.

### 9.3. `compose.yml`: descrevendo o banco de dados

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

- **`services.database`** — declara um serviço chamado `database`, a partir da imagem `mysql:9.6`.
- **`environment`** — variáveis de ambiente específicas da imagem oficial do MySQL, usadas por ela para se auto-configurar na primeira inicialização: o nome do banco a criar (`transaction`), a senha do usuário administrativo (`root`) e as credenciais de um usuário de aplicação (`app`/`app`) com permissões sobre esse banco.
- **`ports: "3307:3306"`** — mapeia a porta `3307` da máquina hospedeira para a porta padrão do MySQL (`3306`) **dentro** do container. Usar `3307` (e não `3306` diretamente) evita conflito caso já exista uma instalação de MySQL rodando localmente na porta padrão.
- **`volumes: transaction_data:/var/lib/mysql`** — associa um **volume nomeado** (`transaction_data`, declarado ao final do arquivo) ao diretório onde o MySQL guarda seus dados dentro do container. Sem isso, os dados existiriam apenas *dentro* do container e seriam perdidos ao removê-lo; com o volume, os dados persistem no sistema hospedeiro, sobrevivendo a reinicializações e recriações do container.
- **`healthcheck`** — um comando (`mysqladmin ping`) que o Docker executa periodicamente (a cada `5s`, com até `5` tentativas) para verificar se o banco já está pronto para aceitar conexões — importante porque o processo do MySQL pode levar alguns segundos para inicializar completamente após o container "subir", e outros serviços (como a própria aplicação Spring Boot) precisam esperar esse estado antes de tentar se conectar.

### 9.4. Integrando o Spring Boot ao Docker Compose

```groovy
developmentOnly 'org.springframework.boot:spring-boot-docker-compose'
```

- **`developmentOnly`** — uma configuração especial do Gradle (equivalente, no ecossistema Spring Boot, a dizer "só inclua isso ao rodar localmente em desenvolvimento") que garante que essa dependência **não** seja empacotada no artefato final da aplicação (o `.jar` de produção) — ela é útil apenas durante o desenvolvimento.
- Com essa dependência presente, o Spring Boot detecta automaticamente o arquivo `compose.yml` na raiz do projeto e **sobe o(s) container(s) automaticamente** ao iniciar a aplicação (e os derruba ao encerrá-la), sem exigir nenhum comando manual de `docker compose up`. É esse comportamento que os logs de inicialização confirmam, mostrando o Spring Boot criando rede, volumes e container a partir da definição do `compose.yml`.

### 9.5. Dependências de JPA e do driver MySQL

```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
runtimeOnly 'com.mysql:mysql-connector-j'
```

- **`spring-boot-starter-data-jpa`** — traz o **Spring Data JPA**, uma camada de abstração sobre o **JPA** (*Jakarta Persistence API* — a especificação padrão do Java para mapeamento objeto-relacional, isto é, para representar tabelas de banco como classes Java e linhas como objetos), junto de sua implementação de referência, o **Hibernate**.
- **`mysql-connector-j`** — o **driver JDBC** específico do MySQL: o componente de baixo nível que efetivamente sabe como abrir uma conexão de rede com um servidor MySQL e traduzir comandos SQL em requisições no protocolo do banco. `runtimeOnly` indica que essa dependência só é necessária durante a **execução** da aplicação (não durante a compilação), já que o código Java escrito não referencia diretamente nenhuma classe do driver — o Spring Data JPA/Hibernate a usa internamente.

### 9.6. `TransactionEntity`: a entidade JPA

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

Esta classe é, propositalmente, **separada** da entidade de domínio `Transaction` (Parte 8.5) — uma decisão de arquitetura importante: `TransactionEntity` pertence à camada de `infrastructure`, carrega anotações específicas do JPA, e sua estrutura pode um dia divergir da estrutura do domínio (por exemplo, para acomodar colunas de auditoria, como data de criação, sem "sujar" a classe `Transaction`). A conversão entre as duas é feita por métodos **mapeadores** (*mappers*): `from` (domínio → entidade) e `toDomain` (entidade → domínio).

- **`@Entity`** — anotação do JPA que marca a classe como representando uma tabela do banco de dados. Por convenção (sem customização adicional), o nome da tabela gerada é derivado do nome da classe: `transaction_entity`.
- **`@Data`** (do Lombok) — uma anotação "combo" que gera, de uma vez, *getters* e *setters* para todos os campos, além de `toString()`, `equals()` e `hashCode()` — mais abrangente que o `@Getter` isolado usado em `Transaction` (Parte 8.6), já que entidades JPA tipicamente precisam de *setters* (o Hibernate os usa internamente ao reconstruir objetos a partir do banco).
- **`@AllArgsConstructor` + `@NoArgsConstructor`** — a combinação exigida pelo JPA: um construtor **sem** argumentos (`@NoArgsConstructor`) é obrigatório para o Hibernate poder instanciar a entidade via reflexão antes de preencher seus campos a partir dos dados lidos do banco; um construtor **com todos** os argumentos (`@AllArgsConstructor`) é o que o método `from` usa para construir a entidade a partir de uma `Transaction` já pronta.
- **`@Id`** — marca o campo `id` como a **chave primária** da tabela.
- **`@Enumerated(EnumType.STRING)`** — instrui o JPA a persistir o valor do `enum Category` como **texto** (por exemplo, a coluna guarda literalmente `"GROCERIES"`), em vez do comportamento padrão do JPA (`EnumType.ORDINAL`), que persistiria apenas a **posição numérica** do valor no `enum` (`0`, `1`, `2`...). Persistir como `STRING` é considerado mais seguro e legível: se a ordem dos valores do `enum` mudar no futuro (por exemplo, adicionar uma nova categoria no meio da lista), os dados já salvos não ficam com o significado corrompido, como aconteceria com `ORDINAL`.
- **`from(Transaction transaction)`** — o mapeador de ida: extrai cada campo do objeto de domínio (`transaction.getId().uuid()`, `getDescription()`, `getAmount()`, `getCategory()` — todos métodos gerados pelo `@Getter` de `Transaction`) e monta uma nova `TransactionEntity`.
- **`toDomain()`** — o mapeador de volta: reconstrói um objeto `Transaction` a partir dos dados armazenados na entidade, usando o construtor de `Transaction` que aceita um `TransactionId` já existente (`@AllArgsConstructor`, Parte 8.6) — essencial aqui, já que uma transação lida do banco **já tem** um identificador definido, ao contrário de uma transação recém-criada.

### 9.7. `TransactionEntityRepository`: o repositório Spring Data

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

- **`CrudRepository<TransactionEntity, UUID>`** — uma interface do Spring Data que, apenas por ser **estendida**, já fornece automaticamente uma implementação completa de operações básicas de CRUD (*Create, Read, Update, Delete*) para o tipo `TransactionEntity`, cuja chave primária é do tipo `UUID` — métodos como `save(...)`, `findById(...)`, `findAll()`, `deleteById(...)` já existem, sem uma linha de implementação escrita manualmente.
- **`findAllByCategory(Category category)`** — este método **não existe** em `CrudRepository`; ele é declarado aqui seguindo a convenção de nomenclatura de **query methods** do Spring Data: a partir do nome do método (`findAllBy` + o nome de um campo, `Category`), o Hibernate consegue **inferir e gerar automaticamente** a consulta SQL equivalente (algo como `SELECT * FROM transaction_entity WHERE category = ?`), sem que uma única linha de SQL precise ser escrita.

### 9.8. `JpaTransactionRepository`: a implementação concreta do contrato de domínio

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

Esta classe é a peça que **fecha o ciclo** iniciado na Parte 8.7: ela `implements TransactionRepository` (a interface de domínio), sendo a implementação real que estava "faltando" até aqui.

- **`@Repository`** — anotação de estereótipo do Spring (semelhante a `@Service`) que marca a classe como um componente de acesso a dados, tornando-a candidata à injeção de dependência. É graças a esta anotação (e a `implements TransactionRepository`) que o Spring, ao ver `PersistTransactionUseCase` pedindo um `TransactionRepository` no construtor (Parte 8.8), sabe exatamente qual objeto injetar: esta implementação.
- **`save(Transaction transaction)`** — o **mapper de ida** (`TransactionEntity.from(transaction)`) converte o objeto de domínio para a entidade JPA; `transactionEntityRepository.save(entity)` persiste de fato (herdado de `CrudRepository`); e o resultado é convertido de volta para o domínio (`.toDomain()`) antes de ser devolvido — o chamador desta classe (o caso de uso) nunca "vê" o tipo `TransactionEntity`, apenas `Transaction`.
- **`findAllByCategory(Category category)`** — busca as entidades da categoria via o *query method* (Parte 9.7), transforma a `List<TransactionEntity>` em um `Stream` (a API funcional de processamento de coleções do Java), converte cada item para o domínio com uma **referência a método** (`TransactionEntity::toDomain` — uma forma abreviada de escrever `entity -> entity.toDomain()`), e coleta o resultado de volta em uma lista com `.toList()`.

### 9.9. `application.properties`: criação automática do schema

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

- **`spring.jpa.hibernate.ddl-auto`** — controla como o Hibernate gerencia o **schema** (a estrutura de tabelas) do banco a partir das entidades mapeadas. O valor `update` instrui o Hibernate a **criar ou ajustar** as tabelas necessárias automaticamente ao iniciar a aplicação, **preservando os dados já existentes** entre execuções — diferente da opção `create` (usada temporariamente durante o desenvolvimento inicial), que recria o schema do zero a cada subida, **apagando** qualquer dado salvo anteriormente. `update` é adequado para desenvolvimento contínuo; em produção, a prática recomendada é usar ferramentas de migração dedicadas (como Flyway ou Liquibase) em vez de deixar o Hibernate alterar o schema automaticamente.
- **`spring.jpa.show-sql=true`** — faz o Hibernate imprimir, no console, cada comando SQL efetivamente executado — uma ferramenta valiosa de depuração, permitindo conferir visualmente que a query gerada a partir de `findAllByCategory` (Parte 9.7) realmente filtra pela coluna esperada, por exemplo.

### 9.10. Checkpoint da Parte 9

Confirmado no `.zip`: `compose.yml` na raiz do projeto define o serviço `database` (MySQL 9.6); `build.gradle` inclui `spring-boot-docker-compose` (`developmentOnly`), `spring-boot-starter-data-jpa` e `mysql-connector-j` (`runtimeOnly`); os pacotes `infrastructure.persistence.entity` (`TransactionEntity`) e `infrastructure.persistence.repository` (`TransactionEntityRepository`, `JpaTransactionRepository`) existem como descrito; `application.properties` tem `spring.jpa.hibernate.ddl-auto=update` e `spring.jpa.show-sql=true`.

**Para rodar você mesmo:** é necessário ter o **Docker** (ou Docker Desktop) instalado e em execução na máquina antes de subir a aplicação — é ele quem efetivamente executa o container do MySQL que o Spring Boot orquestra automaticamente através do `compose.yml`.


---

## Parte 10 — Expondo transações via REST: criação e listagem (Vídeo 10)

### 10.1. Objetivo

Dar aos casos de uso já implementados (Parte 8) uma porta de entrada HTTP tradicional, no padrão REST, independente da IA — permitindo criar e consultar transações diretamente, por JSON.

### 10.2. `TransactionController`: criação de transações

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

Note que este controller vive em `infrastructure.http` — um pacote **novo**, separado dos controllers de IA já vistos (`ChatModelController`, `ChatClientController`, `TranscriptionController`, `TextToSpeechController`, que ficam soltos diretamente em `dio.budgeting`). Essa separação reflete a organização em camadas adotada a partir da Parte 8: controllers relacionados à exposição REST "tradicional" do domínio ficam isolados dentro de `infrastructure`.

- **`@RequestMapping("/transactions")`** — o prefixo de URL deste controller, sem o `/api` usado pelos controllers de IA — outra pista de que esta é uma API REST convencional, para o recurso "transações", independente da camada de IA.
- **`@RequestBody TransactionRequest request`** — instrui o Spring a **desserializar** o corpo da requisição HTTP (esperado em JSON) diretamente em um objeto `TransactionRequest` (explicado a seguir), usando a biblioteca Jackson (incluída por padrão no `spring-boot-starter-web`).
- **`request.toInput()`** — converte o DTO de entrada da camada HTTP (`TransactionRequest`) para o DTO de entrada esperado pelo caso de uso (`PersistTransactionInput`, Parte 8.9) — mantendo os dois DTOs desacoplados: uma mudança no formato do request JSON não obriga a alterar o caso de uso, e vice-versa.
- **`@ResponseStatus(HttpStatus.CREATED)`** — como este endpoint **cria** um novo recurso, ele retorna o código HTTP `201 Created` (em vez do `200 OK` padrão), seguindo a convenção REST para operações de criação bem-sucedida.
- **`TransactionResponse.from(transactionOutput)`** — converte a saída do caso de uso (`TransactionOutput`, Parte 8.9) para o DTO de resposta HTTP (`TransactionResponse`), pela mesma razão de desacoplamento.

### 10.3. `TransactionRequest` e `TransactionResponse`: DTOs da camada HTTP

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

- **`TransactionRequest(String description, Category category, double amount)`** — repare que, aqui, `amount` é `double` (o valor **em reais**, como uma pessoa integrando com a API via JSON esperaria escrever, por exemplo `125.33`), e não `long` em centavos.
- **`Math.round(amount * 100)`** — dentro de `toInput()`, é aqui que acontece a conversão de reais (a unidade "amigável" da API REST) para **centavos** (a unidade interna esperada por `PersistTransactionInput`, Parte 8.9): multiplica-se por `100` e arredonda-se para o inteiro mais próximo (`Math.round`, que devolve um `long` quando o argumento é `double`), evitando problemas de imprecisão de ponto flutuante (por exemplo, `125.33 * 100` pode, por imprecisão de `double`, resultar em algo como `12532.999999...`; `Math.round` corrige isso para `12533`).
- **Records como DTOs** — tanto `TransactionRequest` quanto `TransactionResponse` são `record`s (Parte 8.3), a escolha natural para objetos de transferência de dados imutáveis: eles não têm nenhuma lógica de negócio própria, apenas carregam valores (e, no caso de `TransactionRequest`, um pequeno método de conversão, `toInput()`).

### 10.4. Um bug encontrado (e corrigido depois): a conversão de centavos

Ao testar manualmente este endpoint pela primeira vez — enviando uma transação com `amount: 125.33` — o valor retornado pela API aparecia incorretamente como `12533.0`, e não `125.33`. A causa: em uma versão anterior deste código (antes do ajuste final, documentado aqui já corrigido), o valor era tratado como já estando em centavos em algum ponto da cadeia, sem a devida conversão de volta para reais. Esse tipo de "bug encontrado ao testar manualmente" é normal durante o desenvolvimento incremental — e é exatamente o motivo de sempre **testar cada endpoint assim que ele é implementado**, e não deixar a validação para o final. A correção definitiva desse fluxo de conversão (reais ↔ centavos, na borda de cada camada) é o que está documentado, já funcionando corretamente, nas Partes 8.8 (centavos → reais, ao persistir) e 10.3 (reais → centavos, ao receber via REST) deste tutorial.

### 10.5. `ListTransactionsByCategoryUseCase`: o segundo caso de uso

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

Estruturalmente análogo a `PersistTransactionUseCase` (Parte 8.8): injeta o `TransactionRepository` de domínio, expõe um `execute` que já nasce anotado com `@Tool` — reafirmando que, no projeto final, **todo caso de uso relevante à IA é, desde sua criação, também uma ferramenta de Tool Calling** — e usa `@ToolParam` para explicar ao modelo o que representa o único parâmetro (`category`).

### 10.6. Completando o `TransactionController`: o endpoint de listagem

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

- **`@GetMapping("/{category}")` + `@PathVariable Category category`** — a categoria é recebida diretamente **na URL** (por exemplo, `GET /transactions/GROCERIES`), e não como parâmetro de query string. O Spring converte automaticamente o texto recebido no segmento `{category}` da URL para o `enum Category` correspondente — e, se o texto não corresponder a nenhum valor válido do `enum`, o Spring já responde com um erro HTTP apropriado automaticamente, sem código adicional.
- Múltiplas dependências injetadas no mesmo construtor — o padrão de injeção por construtor (usado desde a Parte 3) escala naturalmente: basta adicionar mais um parâmetro e atribuí-lo, e o Spring resolve ambos os *beans* automaticamente.

### 10.7. `UseCaseConfig`: uma configuração explícita adicional

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

- **`@Configuration`** — marca a classe como uma fonte de definições de *beans* para o Spring (o mesmo papel, em essência, desempenhado implicitamente por `@Service` em outras classes, mas aqui de forma explícita).
- **`@Bean`** — em vez de deixar o Spring criar o *bean* automaticamente a partir de `@Service` na própria classe `PersistTransactionUseCase` (que também está anotada com `@Service`, Parte 8.8), esta configuração declara explicitamente **como** construir esse *bean*, recebendo o `TransactionRepository` como parâmetro do método (que o Spring também resolve automaticamente) e retornando uma nova instância.
- **Nota de leitura cuidadosa:** como `PersistTransactionUseCase` já é `@Service` (o que, por si só, já registraria um *bean* dela automaticamente via `@ComponentScan`), esta classe de configuração é, na prática, **redundante** com a anotação `@Service` já presente na própria classe do caso de uso — ambas resultariam em um *bean* de `PersistTransactionUseCase` disponível no contexto. Não chega a causar erro (o Spring detectaria uma colisão de *beans* apenas se os dois mecanismos tentassem registrar *beans* com o mesmo nome de forma conflitante, o que não é o caso aqui, já que o método `@Bean` tem o mesmo nome do *bean* gerado por `@Service`, resultando em apenas uma definição efetiva sendo usada), mas é um ponto interessante para observar durante os estudos: nem sempre um projeto real chega absolutamente "enxuto" em cada etapa, e identificar esse tipo de configuração redundante é parte de aprender a ler criticamente uma base de código.

### 10.8. Checkpoint da Parte 10

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

---

## Parte 11 — Integrando tudo: do áudio à resposta falada (Vídeo 11)

### 11.1. Objetivo

Este é o vídeo que finalmente conecta todas as peças construídas até aqui — STT (Parte 6), Tool Calling aplicado aos casos de uso reais (Partes 5, 8 e 10), e TTS (Parte 7) — em um único fluxo de ponta a ponta: **um áudio entra, uma transação é criada ou consultada, um áudio de resposta sai**.

> **Nota sobre a divergência com o README/curso original.** O curso, seguindo a rota da OpenAI, implementa esse fluxo final **dentro do próprio `TransactionController`** (o controller REST da Parte 10), adicionando um endpoint `/transactions/ai`. No projeto final entregue (Gemini), a decisão foi diferente: o fluxo de voz-para-voz continua no **`TranscriptionController`** — o mesmo controller que já hospedava a transcrição desde a Parte 6 — mantendo `TransactionController` (Parte 10) dedicado apenas à API REST tradicional em JSON, e `TranscriptionController` dedicado a tudo que envolve áudio (transcrição pura, o fluxo completo de IA por voz, e a leitura de transações por categoria via um segundo endpoint). Esta é uma escolha de organização legítima — ambas cumprem o mesmo objetivo funcional — e é o motivo pelo qual, a partir daqui, o código apresentado usa nomes de endpoint e de classe diferentes dos mostrados na narrativa do curso.

### 11.2. Preparando as *tools*: nomes explícitos para evitar colisão

Até a Parte 10, tanto `PersistTransactionUseCase.execute(...)` quanto `ListTransactionsByCategoryUseCase.execute(...)` já estavam anotados com `@Tool`. O problema: os dois métodos têm o **mesmo nome Java** (`execute`), apenas em classes diferentes. Ao registrar as duas classes como *tools* no mesmo `ChatClient`, o Spring AI precisa de um jeito de diferenciá-las para o modelo — e é exatamente para isso que serve o atributo `name` de `@Tool`, já usado desde a Parte 8.8 e a Parte 10.5:

```java
@Tool(name = "persistTransaction", description = "Persiste uma nova transação financeira")
public TransactionOutput execute(PersistTransactionInput input) { ... }

@Tool(name = "listTransactionsByCategory", description = "Lista transações financeiras por categoria")
public List<TransactionOutput> execute(@ToolParam(description = "Categoria de uma transação") Category category) { ... }
```

Sem o `name` explícito, o Spring AI usaria o nome do método Java (`execute`) como nome da ferramenta para ambas — criando uma colisão que impediria o modelo de diferenciar qual ferramenta chamar. Ao dar nomes de negócio explícitos e únicos (`persistTransaction`, `listTransactionsByCategory`), essa ambiguidade desaparece.

### 11.3. O prompt de sistema: `system-message.st`

```
Você é um assistente financeiro.
Sua tarefa é extrair dados de transações e usar as ferramentas disponíveis para manipular transações.
Ao registrar uma transação, escolha a categoria que melhor se adapta ao contexto.
```

- **Onde o arquivo vive:** `src/main/resources/prompts/system-message.st` — dentro de `resources`, para que seja empacotado no *classpath* da aplicação e possa ser carregado como um `Resource` (o mesmo conceito já visto na Parte 6.2).
- **A extensão `.st`** — faz referência ao **StringTemplate**, uma biblioteca/formato para templates de texto (usada em outras partes do ecossistema Spring para prompts parametrizáveis, com placeholders que podem ser substituídos dinamicamente). Neste arquivo específico, o conteúdo é usado como texto fixo, sem placeholders — mas a extensão `.st` sinaliza a intenção de que, no futuro, esse prompt poderia ser parametrizado (por exemplo, incluindo a lista de categorias válidas dinamicamente).
- **O conteúdo do prompt** — três instruções diretas: define o papel do assistente ("assistente financeiro"), sua tarefa (extrair dados e usar as ferramentas disponíveis) e uma orientação específica de negócio (escolher a categoria mais adequada ao contexto, uma vez que o usuário raramente vai dizer explicitamente "categoria: PHARMA" — ele vai dizer algo como "passei na farmácia", cabendo ao modelo inferir a categoria certa).
- Comparado com o `system.st` mostrado na narrativa original do curso (mais extenso, cobrindo explicitamente mais casos), o `system-message.st` final é mais enxuto — outro ponto de possível evolução (ver Próximos Passos).

### 11.4. `TranscriptionController`: o estado final, explicado por completo

Este é o arquivo mais denso do projeto, porque acumula responsabilidades desde o Vídeo 06 até o Vídeo 11. Seu estado final completo:

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

Analisando as partes novas em relação ao que já havia sido construído (Parte 6):

- **Cinco dependências injetadas no construtor:** `GoogleGenAiChatModel` (para transcrição multimodal, Parte 6.4), os dois casos de uso (para expô-los como *tools* e para o endpoint de listagem), um `ChatClient.Builder` (Parte 4.3) e um `TextToSpeechService` (Parte 7.4) — cada dependência corresponde a uma responsabilidade específica de um dos três endpoints desta classe.
- **`@Value("classpath:/prompts/system-message.st") Resource systemPrompt`** — diferente da injeção de valor **de propriedade** vista no `TextToSpeechService` (`@Value("${...}")`, Parte 7.4), aqui o prefixo `classpath:` faz o Spring injetar um **`Resource` apontando para um arquivo do classpath** — o padrão usado para carregar arquivos de texto/template como o prompt de sistema, em vez de uma propriedade simples.
- **`systemPrompt.getContentAsString(StandardCharsets.UTF_8)`** — lê todo o conteúdo do `Resource` como uma `String`, decodificada em **UTF-8** (o padrão de codificação de caracteres que suporta acentuação e caracteres especiais do português corretamente).
- **`.defaultTools(persistTransactionUseCase, listTransactionsByCategoryUseCase)`** — diferente do exemplo do curso original (que passa **classes**, `.defaultTools(PersistTransactionUseCase.class, ListTransactionsByCategoryUseCase.class)`), o código final passa **instâncias já injetadas** dos dois casos de uso. Essa é, na prática, a forma mais correta neste contexto: como os casos de uso são *beans* gerenciados pelo Spring (com suas próprias dependências já resolvidas, como o `TransactionRepository`), registrar a **instância gerenciada** garante que a *tool* chamada pelo modelo execute com o mesmo objeto configurado pelo contexto do Spring — passar apenas a classe exigiria que o Spring AI soubesse instanciá-la sozinho, sem suas dependências.
- **`throws IOException` no construtor** — declarado porque `getContentAsString(...)` pode lançar essa exceção (a leitura de um arquivo pode falhar por I/O), e o Java exige que exceções verificadas (*checked exceptions*, como `IOException`) sejam tratadas ou declaradas explicitamente na assinatura do método.
- **O método `transcribe(...)`** é idêntico ao já explicado na Parte 6.4 — reutilizado, sem alteração, pelos outros dois endpoints da classe.
- **`readTransactions(...)`** — o endpoint `GET /api/{category}`, estruturalmente idêntico ao `GET /transactions/{category}` do `TransactionController` (Parte 10.6), mas exposto sob o prefixo `/api` em vez de `/transactions` — uma segunda porta de entrada para a mesma funcionalidade de consulta, agora ao lado dos endpoints de IA.
- **`processAudio(...)` — o endpoint `POST /api/ai`, o fluxo completo de ponta a ponta:**
  1. `transcribe(file)` — reaproveita o método já existente para converter o áudio recebido em texto (Parte 6).
  2. `chatClient.prompt().user(transcript).call().content()` — envia o texto transcrito como mensagem de usuário ao `ChatClient` já configurado (no construtor) com o prompt de sistema e as duas *tools* de negócio. É neste passo que o modelo decide, sozinho, se deve chamar `persistTransaction` (se a fala descreve um novo gasto) ou `listTransactionsByCategory` (se a fala pede uma consulta), executa a *tool* correspondente através do mecanismo de Tool Calling (Parte 5), e formula uma resposta textual final (`answer`) incorporando o resultado dessa execução.
  3. `textToSpeechService.synthesize(answer)` — converte essa resposta textual final de volta em áudio (Parte 7), fechando o ciclo Áudio → Texto → Ação → Texto → Áudio.
  4. A resposta HTTP é montada exatamente como no `TextToSpeechController` (Parte 7.6): `ByteArrayResource`, cabeçalho `Content-Disposition: attachment`, nome de arquivo `audio.wav`.

### 11.5. Verificando o fluxo com depuração (debug)

Uma forma eficaz de confirmar visualmente que o Tool Calling está realmente acontecendo (e não apenas confiar na resposta final) é colocar um **breakpoint** dentro do método `execute` de `PersistTransactionUseCase` e rodar a aplicação em modo debug. Ao enviar um áudio como "Passei na farmácia rapidinho e deixei R$ 80 em três itens" para `/api/ai`, a execução para nesse breakpoint, e o painel de variáveis da IDE permite inspecionar o objeto `input` (`PersistTransactionInput`) já preenchido pelo modelo a partir da fala: uma descrição gerada automaticamente ("Compra de três itens na farmácia"), o valor em centavos (`8000`, equivalente a R$ 80) e a categoria inferida (`PHARMA`) — tudo extraído da linguagem natural, sem que o usuário tenha dito literalmente "categoria PHARMA" ou "valor 8000".

### 11.6. Testando manualmente o fluxo completo

```http
POST http://localhost:8080/api/ai
Content-Type: multipart/form-data; boundary=boundary

--boundary
Content-Disposition: form-data; name="file"; filename="recording-1.mp3"

< ./src/test/resources/audio/recording-1.mp3
--boundary
```

A resposta é um arquivo `audio.wav`, que, ao ser reproduzido, deve confirmar em voz a transação registrada (por exemplo, algo como "Registrei sua transação de R$ 80 para farmácia na categoria pharma."). É possível conferir a persistência real consultando a tabela `transaction_entity` no banco (via qualquer cliente MySQL, ou pelo painel de banco de dados da IDE), confirmando que o registro foi de fato salvo com os valores extraídos.

### 11.7. Checkpoint da Parte 11 — estado final do projeto inteiro

Conferido diretamente contra `budgeting_ate_o_video11.zip`, este é o estado completo e final do código-fonte:

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

O fluxo de ponta a ponta (`POST /api/ai`), especificamente:

1. **Áudio** chega como `MultipartFile`.
2. **STT**: `GoogleGenAiChatModel` (multimodal) transcreve o áudio em texto.
3. **Tool Calling**: o texto vira uma mensagem de usuário para o `ChatClient`, que (guiado pelo *system prompt* de `system-message.st`) decide chamar `persistTransaction` ou `listTransactionsByCategory`.
4. **Domínio + Persistência**: o caso de uso escolhido opera sobre `Transaction`/`Category` via `TransactionRepository`, cuja implementação real (`JpaTransactionRepository`) grava/lê no MySQL.
5. O modelo formula uma **resposta textual** a partir do resultado da ferramenta.
6. **TTS**: `TextToSpeechService` (SDK nativo do Gemini) converte essa resposta em áudio `.wav`.
7. O áudio de resposta volta ao cliente HTTP.

---

## Guia rápido: rodando o projeto do zero

1. **Pré-requisitos:** JDK 21 instalado (ou um *toolchain* compatível, que o Gradle pode baixar automaticamente); Docker (ou Docker Desktop) em execução; uma chave de API do Google Gemini, obtida em [aistudio.google.com](https://aistudio.google.com/).
2. **Configurar a chave de API:**
   ```bash
   export GEMINI_API_KEY="sua-chave-aqui"
   ```
   (ou configurá-la como variável de ambiente na *Run Configuration* da sua IDE)
3. **Subir a aplicação:**
   ```bash
   ./gradlew bootRun
   ```
   O Spring Boot sobe automaticamente o container MySQL (via `spring-boot-docker-compose` e `compose.yml`) antes de a aplicação terminar de iniciar.
4. **Rodar os testes de integração** (que exigem `GEMINI_API_KEY` configurada, senão são pulados):
   ```bash
   ./gradlew test
   ```
5. **Testar manualmente** os endpoints principais, com uma ferramenta como o HTTP Client do IntelliJ, Insomnia, ou `curl`:
   - `GET /api/chat?prompt=Oi` — chat simples.
   - `POST /transactions` — criação de transação via JSON puro.
   - `GET /transactions/{category}` ou `GET /api/{category}` — listagem por categoria.
   - `POST /api/transcribe` (multipart, campo `file`) — transcrição pura.
   - `POST /api/synthesize` (JSON `{"text": "..."}`) — síntese de voz pura.
   - `POST /api/ai` (multipart, campo `file`) — o fluxo completo: áudio de gasto financeiro entra, áudio de confirmação sai.

---

## Glossário — conceitos de Java, Spring e arquitetura usados no projeto

**Fundamentos de linguagem e build**
- **JDK / toolchain** — o *Java Development Kit*, o conjunto de ferramentas para compilar e rodar Java; um *toolchain* no Gradle define qual versão específica usar.
- **Gradle / `build.gradle` / `settings.gradle`** — a ferramenta de build usada no projeto; `build.gradle` declara plugins e dependências, `settings.gradle` nomeia o projeto.
- **BOM (*Bill of Materials*)** — um artefato que centraliza as versões compatíveis de um conjunto de dependências relacionadas, evitando declarar cada versão manualmente.
- **Starter** — uma dependência "tudo-em-um" do Spring Boot, que já traz a biblioteca principal e sua auto-configuração.
- **`record`** — um tipo compacto do Java (desde a versão 16) para classes imutáveis de dados, com construtor, *getters* (sem prefixo `get`), `equals`, `hashCode` e `toString` gerados automaticamente.
- **`enum`** — um tipo que representa um conjunto fixo e conhecido de valores possíveis.
- **Text block (`"""..."""`)** — sintaxe do Java para *strings* multilinha legíveis.
- **`Optional`** — tipo do Java para representar um valor que pode ou não estar presente, evitando `NullPointerException` em cadeias de acesso.
- **`ByteBuffer` / `ByteOrder`** — classes do Java para manipular bytes de forma estruturada; `ByteOrder` define a ordem de bytes (*endianness*) usada ao escrever números multi-byte.
- **Lombok** — biblioteca que gera código repetitivo (*getters*, *setters*, construtores) via anotações (`@Getter`, `@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`).

**Spring / Spring Boot — núcleo**
- **`@SpringBootApplication`** — anotação combinada que ativa configuração automática, varredura de componentes e a possibilidade de declarar *beans*.
- **Bean** — um objeto cuja criação e ciclo de vida são gerenciados pelo Spring (Inversão de Controle).
- **Injeção de dependência via construtor** — padrão em que as dependências de uma classe são recebidas como parâmetros do construtor, tornando-as explícitas e obrigatórias.
- **`@Autowired`** — anotação para injeção de dependência (usada em campos de teste no projeto); a injeção via construtor, usada nas classes de produção, dispensa essa anotação.
- **`@Service`, `@Repository`, `@Configuration`, `@Bean`** — anotações de estereótipo/configuração que registram classes ou métodos como *beans* gerenciados pelo Spring, com diferentes intenções semânticas (lógica de negócio, acesso a dados, configuração explícita).
- **`@Value`** — injeta o valor de uma propriedade de configuração (`${...}`) ou um `Resource` de um arquivo (`classpath:...`) diretamente em um campo ou parâmetro.
- **`@PreDestroy`** — marca um método a ser executado automaticamente antes de um *bean* ser destruído, útil para liberar recursos.
- **`Resource`** — abstração do Spring para "algo que pode ser lido como bytes", independente de sua origem (arquivo, classpath, memória, upload HTTP).

**Spring Web (REST)**
- **`@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`** — anotações que definem uma classe/método como um endpoint HTTP e o mapeiam a uma URL/verbo específico.
- **`@RequestParam`, `@PathVariable`, `@RequestBody`** — formas de receber dados de uma requisição HTTP: por query string, por segmento de URL, ou pelo corpo (desserializado, tipicamente de JSON).
- **`@ResponseStatus`** — define explicitamente o código HTTP de uma resposta (por exemplo, `201 Created`).
- **`MultipartFile`** — abstração para um arquivo recebido em uma requisição `multipart/form-data`.
- **`ResponseEntity<Resource>`** — tipo de retorno que permite controlar cabeçalhos e corpo de uma resposta HTTP de forma explícita, usado para devolver arquivos (como áudio).
- **`ContentDisposition` / `Content-Disposition`** — cabeçalho HTTP que instrui o cliente a tratar a resposta como um arquivo para download.
- **DTO (*Data Transfer Object*)** — objeto cuja única responsabilidade é carregar dados entre camadas ou processos, sem lógica de negócio.

**Spring AI**
- **`ChatModel`** — interface de baixo nível do Spring AI para chamadas simples a um LLM.
- **`ChatClient`** — API fluente construída sobre um `ChatModel`, com suporte a *system prompt*, *tools* e composição de mensagens.
- **`Prompt` / `Message` (`SystemMessage`, `UserMessage`, `AssistantMessage`, `ToolResponseMessage`)** — a estrutura de dados que representa uma conversa enviada ao modelo.
- **Temperatura (`temperature`)** — parâmetro que controla a aleatoriedade/criatividade das respostas de um LLM.
- **Tool Calling / Function Calling** — padrão em que o LLM decide chamar um método real da aplicação, informando os argumentos extraídos do contexto; a aplicação (não o modelo) executa o método.
- **`@Tool` / `@ToolParam`** — anotações que expõem um método (e descrevem seus parâmetros) como uma ferramenta disponível ao modelo.
- **`Media`** — classe do Spring AI para anexar conteúdo não-textual (áudio, imagem) a uma mensagem, habilitando entrada multimodal.
- **`TranscriptionModel` / `TextToSpeechModel`** — interfaces do Spring AI para STT e TTS, respectivamente, com suporte OpenAI/Azure OpenAI (e Eleven Labs, para TTS) no momento estudado — sem implementação para Gemini, motivando as soluções alternativas das Partes 6 e 7.
- **SDK nativo do Google GenAI (`com.google.genai.Client`)** — biblioteca de baixo nível usada diretamente quando uma funcionalidade (aqui, TTS) não tem uma abstração pronta no Spring AI para o provedor escolhido.

**Arquitetura e domínio**
- **Domain-Driven Design (DDD)** — abordagem que organiza o código em torno das regras do domínio de negócio, isolando-as de detalhes técnicos.
- **Clean Architecture** — estilo de arquitetura em camadas concêntricas, em que camadas internas (domínio) definem contratos que camadas externas (infraestrutura) implementam.
- **Identificador fortemente tipado (*strongly-typed ID*)** — usar um tipo próprio (como `TransactionId`) em vez de um `String`/`UUID` solto, para prevenir confusão entre ids de entidades diferentes.
- **Caso de uso (*use case*)** — uma classe/método que representa uma ação específica que a aplicação sabe realizar.
- **Repositório (padrão *Repository*)** — uma interface que abstrai o acesso a dados, permitindo que o domínio dependa apenas do "o quê" (buscar, salvar), não do "como" (SQL, JPA).
- **Mapper** — um método (tipicamente `from`/`toDomain`) que converte entre representações de um mesmo conceito em camadas diferentes (por exemplo, `Transaction` de domínio ↔ `TransactionEntity` de persistência).

**Persistência**
- **JPA (*Jakarta Persistence API*) / Hibernate** — a especificação e a implementação padrão do Java para mapeamento objeto-relacional.
- **`@Entity`, `@Id`, `@Enumerated`** — anotações que mapeiam uma classe Java e seus campos a uma tabela e colunas de banco de dados.
- **`CrudRepository`** — interface do Spring Data que já fornece operações básicas de CRUD sem implementação manual.
- **Query methods** — método de repositório cujo nome (seguindo uma convenção) é interpretado pelo Spring Data para gerar automaticamente a consulta SQL correspondente.
- **`ddl-auto`** — propriedade que controla como o Hibernate gerencia (cria/atualiza/recria) o schema do banco a partir das entidades mapeadas.
- **Docker / Docker Compose** — tecnologia de containers e a ferramenta para descrever e orquestrar múltiplos serviços de container (como o banco de dados de desenvolvimento) em um único arquivo.

**Testes**
- **`@SpringBootTest`** — sobe o contexto completo da aplicação para o teste.
- **Teste de integração (sufixo `IT`)** — teste que depende de recursos externos reais (como uma API de IA pela rede), em oposição a um teste unitário isolado.
- **`@EnabledIfEnvironmentVariable`** — condiciona a execução de um teste à presença de uma variável de ambiente.
- **`@ParameterizedTest` / `@CsvSource`** — executa o mesmo teste várias vezes, uma por conjunto de valores fornecido.
- **AssertJ (`assertThat(...)`)** — biblioteca de asserções fluentes, usada no lugar do `assertEquals` tradicional do JUnit.

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

1. Fazer um fork do repositório do instrutor, **ou** publicar seu próprio repositório com esta versão do projeto (já adaptada para Gemini).
2. No `README.md` do repositório entregue, explicar de forma simples:
   - O que o projeto faz;
   - Como executar a aplicação (o "Guia rápido" acima é um bom ponto de partida);
   - **Qual melhoria você implementou** — a própria adaptação de OpenAI para Gemini, documentada neste tutorial, já é, em si, uma evolução significativa e defensável como a "melhoria" pedida pelo desafio, mas nada impede de somar uma segunda melhoria menor e pontual;
   - Quais tecnologias foram usadas (Spring Boot, Spring AI, Google Gemini, MySQL, Docker, Lombok, JUnit 5, AssertJ);
   - Como testar o fluxo principal (o endpoint `/api/ai`);
   - O que você aprendeu durante o desafio.
3. Incluir, se possível, prints, exemplos de requisições, testes realizados ou anotações pessoais — o **LOG de Projeto** que você já pretende manter em paralelo a este tutorial é material perfeito para essa seção.

### Ideias de melhorias pequenas e viáveis, coerentes com o estado atual do projeto

Pensando em algo "pequeno, bem explicado e funcionando" (a recomendação explícita do próprio README, em vez de "uma ideia grande incompleta"), alguns candidatos concretos, ordenados do mais simples ao mais trabalhoso:

1. **Completar o `@ToolParam` faltante** em `PersistTransactionInput.category` (Parte 8.9) — um ajuste de poucos minutos, com potencial de melhorar a precisão da categorização feita pela IA.
2. **Adicionar novas categorias** ao `enum Category` (Parte 8.4) — hoje limitado a `GROCERIES`, `PHARMA`, `AUTO` — e testar se o modelo já as categoriza corretamente a partir de novos áudios de exemplo.
3. **Validação de entrada** no `TransactionRequest` (Parte 10.3) — por exemplo, usando Bean Validation (`@NotBlank`, `@Positive`) para rejeitar requisições com descrição vazia ou valor negativo antes mesmo de chegar ao caso de uso.
4. **Testes automatizados para os casos de uso e controllers** — o projeto tem bons testes de integração para a camada de IA (Partes 3 a 7), mas não há testes unitários dedicados a `PersistTransactionUseCase`, `ListTransactionsByCategoryUseCase` ou aos controllers REST da Parte 10 — um bom uso de *mocks* (por exemplo, com Mockito) para isolar essas classes de suas dependências reais.
5. **Resolver a redundância do `UseCaseConfig`** (Parte 10.7) — decidir conscientemente entre manter o `@Service` na própria classe do caso de uso **ou** a configuração explícita via `@Bean`, documentando a escolha.
6. **Um endpoint de consulta mais flexível** — por exemplo, listar todas as transações (sem filtro de categoria), ou permitir filtrar por período de datas — exigiria adicionar um novo método ao `TransactionRepository` (Parte 8.7) e sua implementação em `JpaTransactionRepository` (Parte 9.8).
7. **Melhorar as respostas faladas** — ajustar o `system-message.st` (Parte 11.3) para produzir respostas mais naturais e variadas, ou trocar a voz do TTS (`voiceName("Kore")`, Parte 7.4) por outra das vozes pré-definidas do Gemini.

Qualquer uma dessas é suficiente, sozinha, como a "melhoria implementada" pedida na entrega — o importante, reforça o próprio README, é entender o fluxo, testar a solução, e documentar claramente o que foi construído.

