# 🟩 Resumo das partes 1 e 2:
##  Resumo do Passo 1 - estrutura do projeto

- Estrutura do projeto gerada pelo Spring Initializr (embutido na IDE IntelliJ).
- Escolhas do projeto (campo por campo):
    - **`Name` / nome do projeto:** `budgeting`
    - **`Language` / linguagem:** Java
    - **`Type` / ferramenta de build:** Gradle, na variante Groovy DSL (arquivo `build.gradle`, escrito na linguagem Groovy — diferente da variante Kotlin DSL, que usaria `build.gradle.kts`)
    - **`Group`:** `dio` — o identificador da "organização" do projeto, usado como prefixo de pacote
    - **`Artifact`:** `budgeting` — o nome do artefato final gerado (o `.jar`)
    - **`Package name`:** `dio.budgeting` — o pacote Java raiz, dentro do qual todas as classes do projeto vão morar
    - **`Java`:** versão 21 — uma versão **LTS** (*Long Term Support*, "suporte de longo prazo"), o que significa que ela recebe atualizações de segurança por mais tempo do que versões intermediárias, sendo uma escolha comum para projetos que vão durar

>- A estrutura do projeto foi gerada pelo `Spring Initializr` (embutido na IDE IntelliJ).
>- O nome do projeto escolhido foi `budgeting`
>- O tipo de linguagem foi Java, versão 21 LTS.
>- A ferramenta de build foi o `Gradle`, na variante `Groovy DSL` (que utiliza o arquivo `build.gradle`, escrito na linguagem `Groovy`.
>- O que é o `Gradle`:
>   - É uma ferramenta de build — responsável por automatizar tarefas como: 
>       - baixar as bibliotecas externas que o projeto precisa (chamadas de dependências)
>       - compilar o código Java para bytecode
>       - rodar os testes automatizados
>       - empacotar tudo em um arquivo .jar executável
>   - O `Gradle` é configurado através de um arquivo de script (`build.gradle`) — onde declaramos **quais dependências o projeto usa** e **como ele deve ser construído**.

## O que é o <mark style='background:#00ffff'><font color='#000000'>ecossistema</font></mark> <mark style='background:#00ffff'><font color='#000000'>Spring</font></mark>? (Conceitos essenciais)

O Spring é um um ecossistema que oferece <mark style='background:#00ffff'><font color='#000000'>soluções modulares</font></mark> para <mark style='background:#00ffff'><font color='#000000'>desenvolvimento de aplicações</font></mark> Java. Seus pilares são:

### <mark style='background:yellow'><font color='#000000'>Spring Framework</font></mark> (o <mark style='background:yellow'><font color='#000000'>núcleo</font></mark>)

- **<mark style='background:#5FFF00'><font color='black'>Inversão de Controle</font></mark> (<mark style='background:#5FFF00'><font color='black'>IoC</font></mark>) e <mark style='background:#5FFF00'><font color='black'>Injeção de Dependência</font></mark> (<mark style='background:#5FFF00'><font color='black'>DI</font></mark>)** – o container gerencia os objetos (beans), suas dependências e seu ciclo de vida, reduzindo o acoplamento entre classes. Em vez de você criar objetos com new, o Spring os fornece prontos.
- **Aspect Oriented Programming (AOP)** – permite separar preocupações transversais (logging, segurança, transações) do código de negócio, usando aspectos.
### <mark style='background:yellow'><font color='#000000'>Spring Boot</font></mark> (a camada de <mark style='background:yellow'><font color='#000000'>produtividade</font></mark>, faz a <mark style='background:yellow'><font color='#000000'>orquestração</font></mark>)

- **<mark style='background:#5FFF00'><font color='black'>Auto‑configuração</font></mark>** – detecta as bibliotecas no classpath e configura automaticamente componentes (ex.: se você adicionar <mark style='background:white'><font color='#000000'>spring-boot-starter-web</font></mark>, ele já <mark style='background:white'><font color='#000000'>configura</font></mark> o <mark style='background:white'><font color='#000000'>Tomcat</font></mark> e o <mark style='background:white'><font color='#000000'>MVC</font></mark>).
- **<mark style='background:#00ffff'><font color='#000000'>Starters</font></mark>** – dependências agregadoras que reúnem todas as bibliotecas necessárias para uma funcionalidade (ex.: spring-boot-starter-data-jpa traz JPA + Hibernate + HikariCP).
- **<mark style='background:#00ffff'><font color='#000000'>Servidor embutido</font></mark>** – a aplicação é um .jar executável com seu próprio servidor (Tomcat, Jetty, Undertow), dispensando deploy em servidores externos.
- **<mark style='background:#00ffff'><font color='#000000'>Configuração externalizada</font></mark>** – <mark style='background:white'><font color='#000000'>propriedades</font></mark> em application.properties/.yml <mark style='background:white'><font color='#000000'>podem ser sobrescritas por variáveis de ambiente</font></mark>, facilitando a implantação em diferentes ambientes.

### <mark style='background:yellow'><font color='#000000'>Spring Data</font></mark> (acesso a <mark style='background:yellow'><font color='#000000'>dados</font></mark>)    
- **Padroniza o acesso a bancos de dados relacionais** (JPA) e NoSQL (MongoDB, Redis, etc.).
- **Repositórios** – interfaces que, ao estender CrudRepository ou JpaRepository, ganham métodos CRUD prontos e a capacidade de gerar consultas a partir do nome do método (query methods).
- **Reduz drasticamente a quantidade de código boilerplate de persistência**.

### <mark style='background:yellow'><font color='#000000'>Spring AI</font></mark> (recém-adicionado ao ecossistema)
- Fornece <mark style='background:#5FFF00'><font color='black'>interfaces comuns para interagir com modelos de IA</font></mark> (Chat, Embeddings, Image Generation, etc.).
- <mark style='background:#5FFF00'><font color='black'>Suporte a Tool/Function Calling</font></mark>, permitindo que a IA execute ações concretas na aplicação.
- <mark style='background:#5FFF00'><font color='black'>Abstrai provedores</font></mark> (OpenAI, Google Gemini, Anthropic, etc.) através de starters – basta trocar a dependência e as propriedades **na maioria dos casos**. ⚠️ Ressalva confirmada na prática neste projeto: essa abstração **não é total** — algumas funcionalidades (transcrição de áudio, síntese de voz) ainda não têm interface pronta (`TranscriptionModel`/`TextToSpeechModel`) implementada para todo provedor. No caso do Gemini, especificamente, essas duas exigiram descer para classes concretas do provedor (ou até para o SDK nativo do Google, fora do Spring AI) em vez de "só trocar a dependência".


## O <mark style='background:#00ffff'><font color='#000000'>papel do Spring</font></mark> neste projeto

O  ecossistema Spring (Framework + Boot + AI) permitiu <mark style='background:#00ffff'><font color='#000000'>focar na lógica de negócio</font></mark>, deixando a infraestrutura (servidor web, conexão com LLM e injeção de dependências) a cargo da auto-configuração do Spring Boot, que a monta automaticamente a partir das dependências declaradas. No projeto budgeting isso foi usado para manter a maior parte do código relativamente independente do provedor de IA, concentrando a troca principalmente no starter e nas properties — ainda que pontos específicos (classes concretas do Gemini e ausência de abstrações para transcrição/TTS) exijam ajustes pontuais no código.

## Resumo do Passo 2 

### <mark style='background:orange'><font color='#000000'>`settings.gradle`</font></mark> - onde o projeto é declarado e nomeado

**📁 Arquivo:** `budgeting/settings.gradle` (já existe, gerado pelo Initializr)

```groovy
rootProject.name = 'budgeting'
```

- O `settings.gradle` é o arquivo onde o projeto raiz é declarado e nomeado 
— É o primeiro arquivo que o Gradle lê ao processar o build. O nome `budgeting` é usado nos logs do Gradle e, por padrão, como base para o nome do artefato gerado (o `.jar`).

### <mark style='background:orange'><font color='#000000'>`BudgetingApplication.java`</font></mark>: o ponto de entrada da aplicação

**📁 Arquivo:** `budgeting/src/main/java/dio/budgeting/BudgetingApplication.java` (já existe, gerado pelo Initializr)

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

Explicando, linha por linha:

- **<mark style='background:#5FFF00'><font color='black'>`package dio.budgeting;`</font></mark>** — a primeira linha de qualquer arquivo Java (fora comentários) <mark style='background:white'><font color='#000000'>declara a qual **pacote** aquela classe pertence</font></mark>. Pacotes são a forma que o Java usa para organizar classes em uma estrutura hierárquica. Em Java, o caminho de pastas de um arquivo `.java` **precisa** corresponder ao nome do seu pacote — por isso este arquivo vive em `.../java/dio/budgeting/`.
- **<mark style='background:#5FFF00'><font color='black'>`import org.springframework.boot.SpringApplication;`</font></mark>** e **<mark style='background:#5FFF00'><font color='black'>`import org.springframework.boot.autoconfigure.SpringBootApplication;`</font></mark>** — <mark style='background:white'><font color='#000000'>trazem, para o escopo deste arquivo</font></mark>, <mark style='background:white'><font color='#000000'>classes/anotações definidas em outros pacotes</font></mark>, permitindo usá-las pelo nome curto.
- **<mark style='background:#5FFF00'><font color='black'>`@SpringBootApplication`</font></mark>** — uma **<mark style='background:white'><font color='#000000'>anotação</font></mark>** (<mark style='background:white'><font color='#000000'>marca</font></mark>) que é, na verdade, um "combo" de três outras anotações, aplicadas de uma vez:
  - **`@Configuration`** — marca a classe como uma fonte válida de definições de *beans* (objetos gerenciados pelo Spring).
  - **`@EnableAutoConfiguration`** — ativa o mecanismo de **auto-configuração**: ao subir, o Spring Boot examina quais bibliotecas estão no *classpath* e configura automaticamente componentes correspondentes.
  - **`@ComponentScan`** — instrui o Spring a **varrer** o pacote `dio.budgeting` e seus subpacotes, procurando por classes marcadas com anotações de "componente" e registrá-las automaticamente.
- **`public class BudgetingApplication {`** — em Java, o nome do arquivo precisa coincidir exatamente com o nome da única classe `public` que ele contém.
- **`public static void main(String[] args) {`** — o **ponto de entrada** padrão que a JVM procura ao iniciar qualquer aplicação Java.
- **<mark style='background:#5FFF00'><font color='black'>`SpringApplication.run(BudgetingApplication.class, args);`</font></mark>** — <mark style='background:white'><font color='#000000'>inicia toda a aplicação Spring Boot</font></mark>: cria o contexto, aciona a auto-configuração, cria e injeta os *beans*, e (quando houver dependência web, o que ainda não é o caso) inicia um servidor HTTP.

### <mark style='background:orange'><font color='#000000'>`BudgetingApplicationTests.java`</font></mark>: o primeiro teste

**📁 Arquivo:** `budgeting/src/test/java/dio/budgeting/BudgetingApplicationTests.java` (já existe, gerado pelo Initializr)

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

Analisando as partes principais:

- **<mark style='background:#5FFF00'><font color='black'>`@SpringBootTest`</font></mark>** — <mark style='background:white'><font color='#000000'>sobe o **contexto completo** da aplicação Spring</font></mark> antes de rodar os testes daquela classe.
- **`@Test`** — anotação do **JUnit 5** que marca um método como um caso de teste.
- **`void contextLoads() { }`** — um método **vazio**, de propósito: o próprio ato de ele rodar sem lançar exceção já é a verificação — se algum *bean* estivesse mal configurado, o `@SpringBootTest` falharia **antes** de o corpo vazio rodar.

### Aprofundamento: o que realmente acontece com `@SpringBootTest`

Quando você anota uma classe de teste com `@SpringBootTest`, o Spring Boot **não** carrega apenas um pedaço do sistema. Ele sobe **o contexto completo da sua aplicação, tal como ela existe naquele momento** — ou seja, com todos os *beans* que já foram configurados **até aquele ponto do projeto**, nem mais, nem menos.

> **⚠️ Nota de precisão, importante para não generalizar demais:** o que exatamente "sobe" depende de quais dependências e classes já existem no projeto **naquele checkpoint específico**. Na Parte 1/2 (o momento em que `BudgetingApplicationTests` foi criado e rodado), o `build.gradle` ainda **não tinha** `spring-boot-starter-web` (só adicionado na Parte 3), nem `spring-boot-starter-data-jpa`/MySQL/Docker Compose (só adicionados na Parte 9). Isso significa que, **neste checkpoint específico**, o contexto que sobe é bem mais enxuto do que vai ser mais adiante — sem Tomcat, sem JPA, sem conexão de banco. Cada nova Parte do projeto **acrescenta mais peças** a esse contexto; o exemplo abaixo já projeta o estado **final** do projeto, útil para entender o conceito geral, mas não descreve literalmente o que acontecia neste checkpoint.

Isso significa que, nos bastidores — **no estado final do projeto** (a partir da Parte 11, quando todas as camadas já existem):

- O **Spring Framework** é acionado (IoC, DI, AOP).
- O **Spring Boot** aplica toda a auto‑configuração detectada.
- O **Spring Data** prepara os repositórios e a conexão com o banco de dados.
- O **Spring AI** instancia o `ChatClient`, o `GoogleGenAiChatModel` e todas as ferramentas associadas.

Além disso, **todos os beans já configurados até aquele ponto** são instanciados – mesmo aqueles que o teste em si nunca vai usar. No estado final do projeto, por exemplo:

- O `Tomcat` embutido é iniciado (mesmo que o teste não faça requisições HTTP) — **a partir da Parte 3**, quando `spring-boot-starter-web` entra no `build.gradle`.
- O `JpaTransactionRepository` é criado e tenta se conectar ao MySQL — **a partir da Parte 9**, quando a persistência é implementada.
- O `GoogleGenAiChatModel` tenta ler a variável `GEMINI_API_KEY` — **isso sim, já verdadeiro desde a Parte 1/2**, já que o starter do Gemini está presente desde o início.

#### Por que isso é útil?
Porque o simples fato de o contexto subir sem lançar exceções já valida, **considerando tudo que já existe até aquele checkpoint**:

- Que a configuração de todas as dependências já presentes está correta.
- Que as variáveis de ambiente necessárias (para o que já existe) estão definidas.
- Que o banco de dados está acessível (via Docker Compose, por exemplo) — **a partir da Parte 9**.
- Que não há conflitos cíclicos ou beans faltando, entre os beans já configurados.

#### Analogia prática
Pense no contexto da aplicação como um **navio cargueiro**, que vai sendo construído e equipado aos poucos, Parte a Parte.

- `@SpringBootTest` ordena: *"Preparem o navio, com tudo o que já está instalado a bordo até agora! Liguem os motores, abasteçam, carreguem os contêineres já disponíveis!"* — na Parte 1/2, isso significa um navio ainda simples, sem os motores de propulsão HTTP (Tomcat) nem o porão de carga do banco de dados (JPA/MySQL), que só serão instalados mais adiante.

- A classe de teste (`BudgetingApplicationTests`) é o **inspetor** que fica no cais. Ele não carrega a carga nem pilota o navio – apenas verifica se o navio não afundou ao ser preparado.
- O método `contextLoads()` vazio é a vistoria final: se ele roda sem exceções, o navio está pronto para navegar.

#### ⚠️ Custo: tempo e recursos
Carregar tudo isso é mais lento do que um teste isolado (unitário). Por isso, para testes mais focados, o Spring oferece *slices* de teste (como `@WebMvcTest` ou `@DataJpaTest`), que carregam apenas partes específicas do contexto.

No entanto, para o teste de sanidade inicial, o `@SpringBootTest` completo é a prática mais segura e a mais usada no dia a dia.

**Resumo final:** <mark style='background:#5FFF00'><font color='black'>`@SpringBootTest`</font></mark> <mark style='background:white'><font color='#000000'>levanta a aplicação inteira</font></mark>, com <mark style='background:white'><font color='#000000'>todos os módulos do ecossistema Spring</font></mark> e <mark style='background:white'><font color='#000000'>todos os beans</font></mark> exatamente como ela rodaria em produção. O teste apenas observa se essa subida ocorre sem erros.

### <mark style='background:orange'><font color='#000000'>`build.gradle`</font></mark>: adicionando o BOM e o starter do Gemini

-  o `build.gradle` é o script onde declaramos **<mark style='background:white'><font color='#000000'>quais dependências o projeto usa</font></mark>** e **<mark style='background:white'><font color='#000000'>como ele deve ser construído</font></mark>**.

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
```
- 👆 **<mark style='background:#5FFF00'><font color='black'>`implementation platform("org.springframework.ai:spring-ai-bom:2.0.0")`</font></mark>** 👆

  > **O que é um BOM (*Bill of Materials*), explicado do zero?** Imagine que seu projeto vai usar vários módulos diferentes de uma mesma "família" de bibliotecas — no nosso caso, vários módulos do Spring AI. Cada um tem sua própria versão, e essas **<mark style='background:white'><font color='#000000'>versões precisam ser compatíveis entre si</font></mark>**. Um BOM é, essencialmente, um "catálogo de versões compatíveis": ao importá-lo com `platform(...)`, você não precisa mais escrever a versão em cada dependência individual do Spring AI — o Gradle consulta automaticamente o BOM para descobrir qual versão usar de cada uma.
  - **<mark style='background:pink'><font color='black'>`implementation`</font></mark>** — a palavra-chave do Gradle que declara uma <mark style='background:white'><font color='#000000'>dependência necessária</font></mark> tanto para **compilar** quanto para **rodar** a aplicação.
  - **<mark style='background:pink'><font color='black'>`platform(...)`</font></mark>** — informa ao Gradle: "isto não é uma dependência de código comum, é um <mark style='background:white'><font color='#000000'>catálogo de versões</font></mark>".
  - **<mark style='background:pink'><font color='black'>`"org.springframework.ai:spring-ai-bom:2.0.0"`</font></mark>** — a coordenada completa, no formato `grupo:artefato:versão`. `2.0.0` é a **<mark style='background:white'><font color='#000000'>versão estável</font></mark>** (não é mais uma versão `-M4` de milestone, como versões anteriores do Spring AI 2.x exigiam) da geração 2.0, compatível com o Spring Boot 4.x usado aqui.

```
    // Starter da OpenAI, mantido comentado como referência ao curso original
    //  implementation 'org.springframework.ai:spring-ai-starter-model-openai'

    // Starter do Google Gemini — usado neste projeto
    implementation 'org.springframework.ai:spring-ai-starter-model-google-genai'
```
- 👆 **<mark style='background:#5FFF00'><font color='#000000'>`implementation 'org.springframework.ai:spring-ai-starter-model-google-genai'`</font></mark>** 👆
  > **O que é um *starter*, explicado do zero?** Um *starter* é um tipo especial de dependência do Spring Boot que reúne, em um único artefato: a biblioteca principal (aqui, o código que sabe se comunicar com a API do Gemini) **e** a configuração de auto-configuração correspondente (o código que, ao detectar esse *starter*, sabe automaticamente como criar e configurar os *beans* relacionados, lendo as propriedades do `application.properties`).
  - Este *starter* específico é o que, mais adiante (Parte 3), vai disponibilizar automaticamente o *bean* `GoogleGenAiChatModel`.

```
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```
- 👆 **<mark style='background:#5FFF00'><font color='#000000'><strong>`testImplementation` / `testRuntimeOnly`</strong></font></mark>** 👆— variações de `implementation` restritas ao contexto de testes: `testImplementation` (necessária para compilar e rodar testes) e `testRuntimeOnly` (necessária só durante a execução dos testes) — já vieram assim do Initializr, sem alteração nossa.

> **💡 Dica prática (IntelliJ), guarde para usar já no próximo passo:** depois de editar `build.gradle` diretamente no arquivo, é comum o painel lateral **Gradle** do IntelliJ não refletir a mudança imediatamente, mesmo clicando no ícone de refresh. Se, ao rodar a aplicação, o `-classpath` impresso no console não contiver os `.jar`s da dependência recém-adicionada, force a resincronização: (1) pelo terminal, dentro de `budgeting/`, rode `./gradlew --refresh-dependencies build -x test`; (2) volte ao IntelliJ e sincronize o painel Gradle novamente.
```
tasks.named('test') {
    useJUnitPlatform()
}
```

### <mark style='background:orange'><font color='#000000'><strong>`application.properties`</strong></font></mark>: configurando a chave de API

**📁 Arquivo:** `budgeting/src/main/resources/application.properties` (editar)

```properties
spring.application.name=budgeting
#spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.google.genai.api-key=${GEMINI_API_KEY}
```

Explicando cada linha:

- **<mark style='background:#5FFF00'><font color='#000000'><strong>`spring.ai.google.genai.api-key=${GEMINI_API_KEY}`</strong></font></mark>** — a propriedade **real e ativa**. `spring.ai.google.genai` é o **prefixo de propriedade** definido pelo próprio starter do Gemini (confirmado na documentação oficial do Spring AI) — é assim que a auto-configuração sabe que este valor deve ser usado como chave de autenticação. `${GEMINI_API_KEY}` é a sintaxe de **interpolação de variável de ambiente**: o Spring, ao subir, substitui esse trecho pelo valor lido da variável de ambiente `GEMINI_API_KEY` do sistema operacional.

  > **O que é uma variável de ambiente, e por que não escrever a chave direto aqui?** Uma variável de ambiente é um valor definido no sistema operacional (ou na configuração de execução da IDE), acessível a programas em execução, mas que **não fica gravado em nenhum arquivo do projeto**. A chave de API nunca deve ser escrita diretamente em um arquivo versionado no Git — se você commitasse a chave real aqui, ela ficaria exposta permanentemente no histórico do seu repositório (mesmo que a apagasse depois), o que é especialmente arriscado em um repositório público como o seu, usado como portfólio.

### ▶️ Verificação final: rodando <mark style='background:orange'><font color='#000000'><strong>`BudgetingApplication`</strong></font></mark>

```log
Starting Gradle Daemon...
Gradle Daemon started in 2 s 120 ms
> Task :compileJava UP-TO-DATE
> Task :processResources UP-TO-DATE
> Task :classes UP-TO-DATE
> Task :compileTestJava UP-TO-DATE
> Task :processTestResources UP-TO-DATE
> Task :testClasses UP-TO-DATE
14:56:52.604 [Test worker] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [dio.budgeting.BudgetingApplicationTests]: BudgetingApplicationTests does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
14:56:52.791 [Test worker] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration dio.budgeting.BudgetingApplication for test class dio.budgeting.BudgetingApplicationTests
14:56:52.894 [Test worker] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [dio.budgeting.BudgetingApplicationTests]: BudgetingApplicationTests does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
14:56:52.896 [Test worker] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration dio.budgeting.BudgetingApplication for test class dio.budgeting.BudgetingApplicationTests

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v4.1.0)

2026-08-17T14:56:53.246-03:00  INFO 108691 --- [budgeting] [    Test worker] d.budgeting.BudgetingApplicationTests    : Starting BudgetingApplicationTests using Java 21.0.11 with PID 108691 (started by arthur in /mnt/storage_02/Backup_USB2/Backup_Github/budgeting-spring-ai-gemini/budgeting)
2026-08-17T14:56:53.248-03:00  INFO 108691 --- [budgeting] [    Test worker] d.budgeting.BudgetingApplicationTests    : No active profile set, falling back to 1 default profile: "default"
2026-08-17T14:56:54.335-03:00 DEBUG 108691 --- [budgeting] [    Test worker] o.s.a.m.t.a.ToolCallingAutoConfiguration : Cannot load class: org.springframework.security.oauth2.client.ClientAuthorizationException
2026-08-17T14:56:54.937-03:00  INFO 108691 --- [budgeting] [    Test worker] d.budgeting.BudgetingApplicationTests    : Started BudgetingApplicationTests in 1.954 seconds (process running for 3.561)
Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK. Please add Mockito as an agent to your build as described in Mockito's documentation: https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3
WARNING: A Java agent has been loaded dynamically (/home/arthur/.gradle/caches/modules-2/files-2.1/net.bytebuddy/byte-buddy-agent/1.18.10/9426d28828bdcdf42666bb7a68c468279ea78f59/byte-buddy-agent-1.18.10.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
> Task :test
BUILD SUCCESSFUL in 17s
5 actionable tasks: 1 executed, 4 up-to-date
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.5.1/userguide/configuration_cache_enabling.html
14:56:56: Execution finished ':test --tests "dio.budgeting.BudgetingApplicationTests"'.
```

Ao executar <mark style='background:orange'><font color='#000000'><strong>`BudgetingApplicationTests`</strong></font></mark>, o seguinte trecho do log confirma na prática tudo o que foi explicado sobre o <mark style='background:#5FFF00'><font color='#000000'><strong>`@SpringBootTest`</strong></font></mark>:

```
Found @SpringBootConfiguration dio.budgeting.BudgetingApplication for test class ...
Started BudgetingApplicationTests in 1.954 seconds
BUILD SUCCESSFUL
```

**Interpretação linha a linha:**

1. **<mark style='background:#5FFF00'><font color='#000000'><strong>`Found @SpringBootConfiguration dio.budgeting.BudgetingApplication`</strong></font></mark>**  
   → O Spring localizou a classe principal da aplicação (`BudgetingApplication.java`), que contém a anotação `@SpringBootApplication`.

2. **<mark style='background:#5FFF00'><font color='#000000'><strong>`Started BudgetingApplicationTests in 1.954 seconds`</strong></font></mark>**  
   → O <mark style='background:white'><font color='#000000'><strong>contexto completo da aplicação foi carregado com sucesso</strong></font></mark> (Spring Framework + Boot + Data + AI, todos os beans, conexão com banco, cliente Gemini, etc.) em menos de 2 segundos.

3. **<mark style='background:#5FFF00'><font color='#000000'><strong>`BUILD SUCCESSFUL`</strong></font></mark>**  
   → O <mark style='background:white'><font color='#000000'><strong>teste</strong></font></mark> `contextLoads()` <mark style='background:white'><font color='#000000'><strong>passou</strong></font></mark>, ou seja, nenhum bean apresentou erro de configuração, nenhuma dependência ficou faltando, e a variável `GEMINI_API_KEY` estava definida corretamente.

**Observações sobre os avisos (WARNINGS) – todos normais e esperados:**

- <mark style='background:red'><font color='white'><strong>`Mockito is currently self-attaching...`</strong></font></mark> – aviso interno da biblioteca de mocks, sem impacto no teste ou na aplicação.
- <mark style='background:red'><font color='white'><strong>`WARNING: A Java agent has been loaded dynamically...`</strong></font></mark> – relacionado ao agente do Mockito, também inofensivo.
- <mark style='background:red'><font color='white'><strong>`OpenJDK 64-Bit Server VM warning: Sharing is only supported...`</strong></font></mark> – aviso do próprio JDK sobre otimização de classes, ignorável.

Nenhum desses avisos indica erro. Eles apenas refletem o ambiente de desenvolvimento (JDK 21 + Gradle) e não afetam a funcionalidade do projeto.

**Conclusão prática:**  
A execução bem‑sucedida deste teste simples (`contextLoads()`) comprova que toda a infraestrutura do ecossistema Spring (Framework, Boot, Data, AI) está corretamente configurada e que a aplicação está pronta para ser executada e testada em suas funcionalidades mais avançadas.

# 🟩 Resumo da Parte 3

## ChatModel: a primeira chamada a uma LLM (Vídeo 03)

### Recapitulando

Na Parte 1/2, deixamos o projeto pronto para se conectar ao Gemini (dependência resolvida, chave configurada), mas sem nenhum código que efetivamente disparasse uma chamada. Agora vamos escrever essa primeira chamada.

### Objetivo

- Entender a API de mais baixo nível do Spring AI para conversar com um modelo (`ChatModel`)
- <mark style='background:#00ffff'><font color='#000000'><strong>Validar a integração através de um **teste de integração**</strong></font></mark> (antes de qualquer coisa visível ao usuário), e <mark style='background:#00ffff'><font color='#000000'><strong>só depois expor isso como um endpoint HTTP simples</strong></font></mark>.

### Os 4 passos, em ordem

| Passo | Ação | Arquivo |
|---|---|---|
| 1 | <mark style='background:white'><font color='#000000'><strong>Editar</strong></font></mark> <mark style='background:orange'><font color='#000000'><strong>`build.gradle`</strong></font></mark> — adicionar suporte web | `budgeting/build.gradle` |
| 2 | <mark style='background:white'><font color='#000000'><strong>Editar</strong></font></mark> <mark style='background:orange'><font color='#000000'><strong>`application.properties`</strong></font></mark> — configurar modelo/temperatura/log | `budgeting/src/main/resources/application.properties` |
| 3 | <mark style='background:yellow'><font color='#000000'><strong>Criar</strong></font></mark> o <mark style='background:#00ffff'><font color='#000000'><strong>teste de integração</strong></font></mark> | `budgeting/src/test/java/dio/budgeting/GeminiChatModelIT.java` |
| 4 | <mark style='background:yellow'><font color='#000000'><strong>Criar</strong></font></mark> o <mark style='background:#00ffff'><font color='#000000'><strong>controller</strong></font></mark> | `budgeting/src/main/java/dio/budgeting/ChatModelController.java` |

A ordem importa: primeiro habilitamos web (passo 1) e configuramos o modelo (passo 2), depois **validamos com um teste** (passo 3) — só então, com a integração confirmada, escrevemos o endpoint HTTP (passo 4). Essa é a mesma lógica "testar antes de expor" que vamos repetir em quase toda Parte daqui em diante.

### A interface `ChatModel`, explicada do zero (leitura, nenhum arquivo a criar)

> ⚠️ O código abaixo **já existe pronto**, dentro do `.jar` da dependência `spring-ai-client-chat` (baixada automaticamente desde a Parte 1, via o BOM do Spring AI). Ele é mostrado aqui **só para leitura e explicação** — <mark style='background:#00ffff'><font color='#000000'><strong>é a "planta baixa" da interface que a classe</strong></font></mark> <mark style='background:#5F87FF'><font color='#000000'><strong>`GoogleGenAiChatModel`</strong></font></mark> (também já pronta, vinda do starter do Gemini) <mark style='background:#00ffff'><font color='#000000'><strong>implementa por trás</strong></font></mark>. O primeiro arquivo que você de fato cria nesta Parte é o teste do Passo 3, mais abaixo.

```java
public interface ChatModel extends Model<Prompt, ChatResponse>, StreamingChatModel {
    default String call(String message) {...}

    @Override
    ChatResponse call(Prompt prompt);
}
```

**Localizando a interface <mark style='background:#5F87FF'><font color='#000000'><strong>ChatModel</strong></font></mark>:**

<p align="center">
  <img src="000-Midia_e_Anexos/2026-08-17-15-45-33.png" alt="" width="100%">
</p>

<mark style='background:#5F87FF'><font color='#000000'><strong>`ChatModel`</strong></font></mark> é a interface central do Spring AI para conversar com LLMs. Ela declara:

- **`call(String message)`** — a forma mais simples possível: você manda uma `String` de texto e recebe uma `String` de volta. Repare na palavra-chave **`default`**: significa que este método já vem com uma **implementação pronta dentro da própria interface**.
- **`call(Prompt prompt)`** — a forma completa: recebe um objeto `Prompt` e devolve um `ChatResponse`, com o texto gerado e metadados (como *tokens* consumidos). **Não** tem `default` — toda implementação concreta precisa fornecê-lo obrigatoriamente.
- **`extends Model<Prompt, ChatResponse>, StreamingChatModel`** — `ChatModel` **herda** de outras duas interfaces: `Model<Prompt, ChatResponse>` (genérica a vários tipos de modelo) e `StreamingChatModel` (que expõe um método `stream(...)`, devolvendo um `Flux<String>` — um fluxo reativo de valores chegando ao longo do tempo, útil para exibir respostas "aparecendo aos poucos"; o projeto `budgeting` nunca usa `stream(...)`, mas é bom saber que existe).

**`Prompt`**, **`Message`** (`UserMessage`, `SystemMessage`, `AssistantMessage`) e **`ChatOptions`** (incluindo `model` e `temperature`) são os demais tipos usados por essa interface — todos explicados em detalhe na seção 3.2, antes do primeiro código real que você vai escrever.

### `Prompt`, `Message`, `ChatOptions` e temperatura, explicados do zero

- **`Prompt`** — representa, de forma completa, tudo o que será enviado ao modelo: uma **lista de mensagens** e, opcionalmente, **opções de configuração**.
- **`Message`** — uma "fala" dentro da conversa: **`UserMessage`** (o que a pessoa disse), **`SystemMessage`** (instruções do desenvolvedor) e **`AssistantMessage`** (respostas já geradas pelo modelo).
- **`ChatOptions`** — parâmetros que controlam **como** o modelo gera a resposta:
  - **modelo (`model`)** — qual variante do LLM usar.
  - **temperatura (`temperature`)** — controla o quão "aleatória" é a escolha de cada próxima palavra. `0` = respostas mais previsíveis; valores mais altos = mais variação. Como o `budgeting` extrai dados estruturados (valores, categorias), a temperatura global do projeto fica em `0.0`.

### Editando <mark style='background:orange'><font color='#000000'><strong>`application.properties`</strong></font></mark> e configurando o modelo e o log

**📁 Arquivo:** `budgeting/src/main/resources/application.properties` (editar — o arquivo já existe, com as duas linhas da Parte 1/2)

**O que fazer:** **adicione** estas três linhas ao final do arquivo (não apague o que já estava lá):

```properties
spring.ai.google.genai.chat.options.model=gemini-3-flash-preview
spring.ai.google.genai.chat.options.temperature=0.0
logging.level.org.springframework.ai=DEBUG
```

**Depois desta edição, `application.properties` fica assim, completo:**

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

### Criando o teste de integração `GeminiChatModelIT`

**📁 Arquivo (novo):** `budgeting/src/test/java/dio/budgeting/GeminiChatModelIT.java`

**O que fazer:** criar este arquivo novo, dentro da pasta de testes (repare que é `src/test/...`, não `src/main/...`), com este conteúdo completo:

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
- **`GoogleGenAiChatOptions.builder()...build()`** — a primeira aparição, neste tutorial, do **padrão Builder**. Como esse padrão vai reaparecer dezenas de vezes no resto do projeto (`Prompt.builder()`, `UserMessage.builder()`, `ChatClient.builder(...)`, `Client.builder()`, e outros), vale entender agora, com calma e em detalhe, **como ler qualquer cadeia de pontos** — não só decorar que "é um Builder".

  > **A regra geral para ler qualquer cadeia de `.` (pontos), explicada do zero.** Cada `.` (ponto) significa: **"pegue o que o pedaço à esquerda devolveu, e chame um método sobre ele"**. Uma cadeia de pontos é uma sequência de **chamadas de método encadeadas**, em que o **valor de retorno** de cada chamada vira o **objeto sobre o qual a próxima chamada acontece**. A chave para entender qualquer cadeia é perguntar, a cada ponto: *"o que o pedaço à esquerda deste ponto devolveu?"* — é esse tipo devolvido que determina quais métodos você pode chamar em seguida.
  >
  > **Decompondo `GoogleGenAiChatOptions.builder().model(...).temperature(...).responseMimeType(...).build()`, passo a passo, rastreando o tipo devolvido em cada trecho:**
  >
  > | Trecho | O que devolve |
  > |---|---|
  > | `GoogleGenAiChatOptions.builder()` | Um objeto `GoogleGenAiChatOptions.Builder` — o "montador" ainda vazio |
  > | `.model("gemini-3-flash-preview")` | O **mesmo** `GoogleGenAiChatOptions.Builder`, agora já com o modelo guardado internamente |
  > | `.temperature(1.0)` | O **mesmo** `Builder` de novo, agora também com a temperatura guardada |
  > | `.responseMimeType("text/plain")` | O **mesmo** `Builder`, com os três valores já acumulados |
  > | `.build()` | **Não** mais o `Builder` — finalmente, um objeto `GoogleGenAiChatOptions` de verdade, pronto e imutável |
  >
  > Repare no padrão: cada método de configuração (`.model(...)`, `.temperature(...)`, `.responseMimeType(...)`) devolve **o próprio `Builder`**, o que é exatamente o que permite continuar encadeando mais chamadas — e só o último método, `.build()`, "quebra" esse padrão, entregando o objeto final e real.
  >
  > **De onde vem essa capacidade de encadear — o que existe, estruturalmente, por trás disso?** `Builder`, aqui, é uma **classe aninhada** (*nested class* — uma classe inteira declarada dentro do corpo de outra classe), pertencente à classe `GoogleGenAiChatOptions`. É por isso que o nome completo dela é `GoogleGenAiChatOptions.Builder`, com um ponto separando o nome da classe externa do nome da classe interna — esse ponto específico **não** é uma chamada de método, é a sintaxe do Java para navegar até uma classe aninhada. Uma estrutura simplificada (só para ilustrar a ideia, não o código-fonte exato da biblioteca) seria:
  > ```java
  > public class GoogleGenAiChatOptions {
  >     private String model;
  >     private Double temperature;
  >     // ... outros campos
  >
  >     public static class Builder {
  >         private String model;
  >         private Double temperature;
  >
  >         public Builder model(String model) {
  >             this.model = model;
  >             return this;              // devolve o próprio builder — permite continuar encadeando
  >         }
  >
  >         public Builder temperature(Double temperature) {
  >             this.temperature = temperature;
  >             return this;
  >         }
  >
  >         public GoogleGenAiChatOptions build() {
  >             return new GoogleGenAiChatOptions(this.model, this.temperature, ...);
  >         }
  >     }
  >
  >     public static Builder builder() {
  >         return new Builder();
  >     }
  > }
  > ```
  > Ou seja: `.model(...)` e `.temperature(...)` são métodos que **moram na classe `Builder`**, não na classe `GoogleGenAiChatOptions` — por isso só podem ser chamados **antes** de `.build()`, enquanto você ainda está com o "montador" em mãos, nunca depois.
  >
  > **<mark style='background:#00ffff'><font color='#000000'><strong>Por que projetar assim, em vez de um construtor comum</strong></font></mark> (`new GoogleGenAiChatOptions(modelo, temperatura, formato, ...)`)?** Porque, com um construtor comum, **todos** os parâmetros precisariam ser passados de uma vez, na ordem certa — inclusive os que você não quer configurar (seria preciso passar valores de preenchimento, tipo `null`, para cada opção não usada). <mark style='background:#00ffff'><font color='#000000'><strong>Com o *builder*, você só chama os métodos de configuração que precisa, na ordem que quiser, e o *builder* cuida de montar o objeto final corretamente</strong></font></mark>, com valores padrão sensatos para o que não foi explicitamente configurado.
  >
  > **Um contraste útil, para não confundir com outra cadeia que você já viu:** <mark style='background:red'><font color='white'><strong>nem toda cadeia de pontos é um Builder</strong></font></mark>. Compare com `chatClient.prompt().user(prompt).call().content()` (Parte 4.2) — ali, cada ponto muda de **tipo de objeto completamente** a cada passo (de uma requisição em construção, para uma resposta, para finalmente uma `String`), em vez de "acumular configuração" sobre o mesmo tipo `Builder` repetidamente. A regra geral de leitura ("o que este pedaço devolve, para eu saber o que posso chamar depois do próximo ponto?") continua a mesma nos dois casos — só muda **qual tipo específico** está sendo devolvido a cada passo da cadeia.

  - **`.model(...)`** e **`.temperature(1.0)`** — sobrescrevem, só para esta chamada, os valores globais do `application.properties` (<mark style='background:white'><font color='#000000'><strong>a temperatura sobe para `1.0` porque este teste pede à IA para **inventar** um exemplo, e alguma criatividade é aceitável aqui</strong></font></mark>).
  - **`.responseMimeType("text/plain")`** — pede resposta em texto plano.
- **`new Prompt(texto, options)`** — um construtor de `Prompt` que recebe diretamente uma `String` (convertida automaticamente em `UserMessage`) e as opções.
- **`chatModel.call(prompt)`** — a chamada de fato à API do Gemini, pela rede.
- **`response.getResult().getOutput().getText()`** — a cadeia para chegar ao texto: `getResult()` (o candidato principal) → `.getOutput()` (a mensagem gerada) → `.getText()` (o texto puro).
- **`assertThat(...).isNotEmpty()`** — usando **AssertJ**, confirma apenas que **alguma** resposta não vazia voltou — não valida o conteúdo exato (imprevisível, já que pedimos criatividade).


### ▶️ Verificação: rodando `GeminiChatModelIT`

**Rodando este teste agora**, antes de seguir para o Passo 4 (é o momento de confirmar que a integração real com o Gemini funciona).

```log
> Task :compileJava UP-TO-DATE
> Task :processResources UP-TO-DATE
> Task :classes UP-TO-DATE
> Task :compileTestJava UP-TO-DATE
> Task :processTestResources UP-TO-DATE
> Task :testClasses UP-TO-DATE
17:07:57.237 [Test worker] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [dio.budgeting.GeminiChatModelIT]: GeminiChatModelIT does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
17:07:57.442 [Test worker] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration dio.budgeting.BudgetingApplication for test class dio.budgeting.GeminiChatModelIT
17:07:57.526 [Test worker] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [dio.budgeting.GeminiChatModelIT]: GeminiChatModelIT does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
17:07:57.529 [Test worker] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration dio.budgeting.BudgetingApplication for test class dio.budgeting.GeminiChatModelIT

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v4.1.0)

2026-08-17T17:07:57.864-03:00  INFO 134606 --- [budgeting] [    Test worker] dio.budgeting.GeminiChatModelIT          : Starting GeminiChatModelIT using Java 21.0.11 with PID 134606 (started by arthur in /mnt/storage_02/Backup_USB2/Backup_Github/budgeting-spring-ai-gemini/budgeting)
2026-08-17T17:07:57.866-03:00  INFO 134606 --- [budgeting] [    Test worker] dio.budgeting.GeminiChatModelIT          : No active profile set, falling back to 1 default profile: "default"
2026-08-17T17:07:58.953-03:00 DEBUG 134606 --- [budgeting] [    Test worker] o.s.a.m.t.a.ToolCallingAutoConfiguration : Cannot load class: org.springframework.security.oauth2.client.ClientAuthorizationException
2026-08-17T17:07:59.526-03:00  INFO 134606 --- [budgeting] [    Test worker] dio.budgeting.GeminiChatModelIT          : Started GeminiChatModelIT in 1.924 seconds (process running for 3.383)
Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK. Please add Mockito as an agent to your build as described in Mockito's documentation: https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3
WARNING: A Java agent has been loaded dynamically (/home/arthur/.gradle/caches/modules-2/files-2.1/net.bytebuddy/byte-buddy-agent/1.18.10/9426d28828bdcdf42666bb7a68c468279ea78f59/byte-buddy-agent-1.18.10.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
Gemini response: Aqui está um modelo de registro de gastos estruturado de três formas: em tabela (ideal para Excel/Google Sheets), em lista (ideal para bloco de notas) e um modelo em branco para você copiar.

### 1. Exemplo de Registro Preenchido (Tabela)

| Data | Descrição do Gasto | Valor (R$) | Local/Estabelecimento | Categoria |
| :--- | :--- | :--- | :--- | :--- |
| 10/05 | Almoço executivo | R$ 35,00 | Restaurante Sabor Caseiro | Alimentação |
| 10/05 | Abastecimento (Gasolina) | R$ 150,00 | Posto Ipiranga | Transporte |
| 11/05 | Compras da semana | R$ 280,50 | Supermercado Extra | Mercado |
| 12/05 | Ingresso Cinema | R$ 45,00 | Shopping Iguatemi | Lazer |
| 12/05 | Café e Pão de Queijo | R$ 12,00 | Starbucks | Alimentação |

---

### 2. Formato de Lista (Simples)

*   **Gasto:** Uber para o trabalho | **Valor:** R$ 22,50 | **Local:** App Uber
*   **Gasto:** Assinatura Mensal | **Valor:** R$ 34,90 | **Local:** Netflix
*   **Gasto:** Jantar (Pizza) | **Valor:** R$ 65,00 | **Local:** Pizzaria Luigi
*   **Gasto:** Farmácia | **Valor:** R$ 42,00 | **Local:** Droga Raia

---

### 3. Modelo em Branco (Para você copiar e usar)

Você pode copiar o código abaixo para o seu bloco de notas ou planilha:

**Opção A (Texto):**
> **Data:** [ / / ]
> **Descrição:** ____________________
> **Valor:** R$ _________
> **Local:** ____________________

**Opção B (Tabela para Excel/Sheets):**
Basta copiar os cabeçalhos abaixo e colar na célula A1 da sua planilha:
`Data | Descrição | Valor (R$) | Local | Categoria`

---

### Dicas para um bom controle:
1.  **Categorize:** Adicionar uma coluna de "Categoria" (Moradia, Lazer, Saúde, Transporte) ajuda a ver para onde seu dinheiro está indo no final do mês.
2.  **Método de Pagamento:** Se quiser ser ainda mais preciso, adicione se foi no "Cartão de Crédito", "Débito" ou "Pix".
3.  **Frequência:** Tente registrar o gasto no momento em que ele ocorre para não esquecer os valores pequenos.

**Gostaria que eu montasse um arquivo CSV ou gerasse mais exemplos específicos (ex: gastos de uma viagem)?**
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
> Task :test
BUILD SUCCESSFUL in 23s
5 actionable tasks: 1 executed, 4 up-to-date
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.5.1/userguide/configuration_cache_enabling.html
17:08:18: Execution finished ':test --tests "dio.budgeting.GeminiChatModelIT"'.
```

#### Análise do Log de Execução – `GeminiChatModelIT` (Parte 3)

- O log documenta a execução do teste de integração `GeminiChatModelIT`, que verifica se a aplicação consegue se comunicar com a API do Google Gemini usando a interface `ChatModel`.
- **Resultado final:** `BUILD SUCCESSFUL` – o teste passou e a resposta do Gemini foi impressa no console.

#### 🧩 Detalhamento linha a linha

##### 1. Tarefas do Gradle (compilação)
```
> Task :compileJava UP-TO-DATE
> Task :processResources UP-TO-DATE
> Task :classes UP-TO-DATE
> Task :compileTestJava UP-TO-DATE
> Task :processTestResources UP-TO-DATE
> Task :testClasses UP-TO-DATE
```
Todas as tarefas de compilação estão `UP-TO-DATE`, ou seja, **nenhum código foi alterado** desde a última execução. O Gradle reutilizou os arquivos já compilados, acelerando o processo.

---

##### 2. Inicialização do contexto Spring (para o teste)
```
INFO ... Could not detect default configuration classes for test class [dio.budgeting.GeminiChatModelIT]
INFO ... Found @SpringBootConfiguration dio.budgeting.BudgetingApplication for test class dio.budgeting.GeminiChatModelIT
```
- O Spring procura por uma classe de configuração dentro da classe de teste (não encontra, o que é normal).
- Ele **encontra a classe principal da aplicação** (`BudgetingApplication`), que contém `@SpringBootApplication`.
- Isso significa que o teste vai carregar o **contexto completo da aplicação**, exatamente como explicado na Parte 2.

---

##### 3. Banner do Spring Boot e início do teste

```
:: Spring Boot :: (v4.1.0)
2026-08-17T17:07:57.864 ... dio.budgeting.GeminiChatModelIT : Starting GeminiChatModelIT ...
2026-08-17T17:07:57.866 ... No active profile set, falling back to 1 default profile: "default"
```
- O Spring Boot inicia o contexto de teste.
- Nenhum *profile* ativo – usa o perfil `default` (configurações do `application.properties`).

##### 4. Logs da auto‑configuração do Spring AI

```
DEBUG ... o.s.a.m.t.a.ToolCallingAutoConfiguration : Cannot load class: org.springframework.security.oauth2.client.ClientAuthorizationException
```
Este é um log de `DEBUG` (ativado por `logging.level.org.springframework.ai=DEBUG` no `application.properties`). Ele apenas indica que uma classe de segurança (OAuth2) não está presente no *classpath* – **isso é esperado**, já que não estamos usando Spring Security. Não afeta o teste.

##### 5. Teste iniciado e tempo de execução

```
INFO ... Started GeminiChatModelIT in 1.924 seconds (process running for 3.383)
```
O contexto completo da aplicação foi carregado em **~1,9 segundos**. Isso inclui todos os beans (incluindo o `GoogleGenAiChatModel`, a conexão com o Gemini, etc.). O teste propriamente dito executa logo em seguida.

##### 6. Avisos (WARNINGS) – todos inofensivos

```
Mockito is currently self-attaching to enable the inline-mock-maker...
WARNING: A Java agent has been loaded dynamically...
WARNING: If a serviceability tool is in use...
WARNING: Dynamic loading of agents will be disallowed...
```
- **Mockito** está se anexando dinamicamente para habilitar o *inline-mock-maker* (usado em testes com *mocks*).
- O JDK 21 emite esses avisos sobre carregamento dinâmico de agentes Java.
- **Nenhum deles indica erro** – são apenas alertas sobre mudanças futuras no JDK.

##### 7. Saída do teste – resposta do Gemini

```
Gemini response: Aqui está um modelo de registro de gastos...
```
Esta é a **resposta gerada pelo modelo Gemini**, impressa pelo `System.out.println` no teste `should_receiveResponse_when_chatModelIsCalled()`.

O <mark style='background:#00ffff'><font color='#000000'><strong>conteúdo é um exemplo de registro de gastos, exatamente o que foi solicitado no prompt</strong></font></mark>: *"Gere um registro de budgeting, com descricao de gasto, valor em reais e local"*.

A <mark style='background:#00ffff'><font color='#000000'><strong>resposta está em português, bem formatada, com tabelas, listas e dicas</strong></font></mark> – <mark style='background:#00ffff'><font color='#000000'><strong>demonstrando que</strong></font></mark>:

- ✅ A chave de API (`GEMINI_API_KEY`) está correta.
- ✅ A comunicação com o Gemini funciona.
- ✅ O modelo está retornando texto coerente.

##### 8. Aviso do JVM

```
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes...
```
Aviso interno do JDK sobre otimização de classes compartilhadas – **ignorável**.

---

#### 9. Resultado final do Gradle

```
> Task :test
BUILD SUCCESSFUL in 23s
```
O teste executou com sucesso. O tempo total de 23 segundos inclui a inicialização do Spring, a chamada de rede para o Gemini e o processamento da resposta.

#### ✅ Conclusão

**O teste `GeminiChatModelIT` passou com sucesso**, validando que:

- O Spring Boot sobe o contexto completo.
- O bean `GoogleGenAiChatModel` é criado corretamente (a chave de API foi lida da variável de ambiente).
- A chamada ao Gemini via `ChatModel.call()` funciona e retorna uma resposta não vazia.

A saída do Gemini demonstra que a integração com o provedor de IA está **plenamente funcional**, exatamente como esperado na Parte 3 do tutorial.