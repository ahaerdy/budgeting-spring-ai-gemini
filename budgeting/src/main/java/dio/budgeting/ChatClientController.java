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