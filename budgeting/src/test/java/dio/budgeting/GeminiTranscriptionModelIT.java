package dio.budgeting;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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