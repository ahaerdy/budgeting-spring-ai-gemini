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