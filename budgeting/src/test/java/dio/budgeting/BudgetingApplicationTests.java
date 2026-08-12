// Declaração do pacote de testes
package dio.budgeting;

// Importação das anotações de teste do JUnit 5 e Spring Boot Test

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Carrega o contexto completo da aplicação Spring Boot para validações e testes de integração
@SpringBootTest
class BudgetingApplicationTests {

    // Método de teste básico para validar se o contexto do Spring carrega sem erros
    @Test
    void contextLoads() {
    }

}