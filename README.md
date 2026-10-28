# Atividade — Injeção de Dependências (JUnit 5)

Projeto baseado na atividade dos slides sobre a classe `Usuario` e nos exemplos anteriores de `TestInfo`, `TestReporter`, `RepetitionInfo` e `@RepeatedTest`.

## O que foi implementado

- Getters e setters para nome e e-mail.
- Validação de campos nulos ou vazios.
- Nome com letras e espaços entre palavras, sem números ou símbolos.
- E-mail com usuário formado por letras sem acento, números, ponto ou sublinhado; domínio com letras, números e ponto. O slide não deixa explícito o caractere após “ou” no item do domínio; este projeto considera o ponto como permitido.
- Senha guardada em hash SHA-256 e verificação de autenticação.
- No máximo três falhas em uma janela de 30 segundos. A quarta falha lança `ExceededAttemptsException` e bloqueia novas tentativas por um minuto.
- Testes com `TestInfo`, `TestReporter`, `RepetitionInfo` e `@RepeatedTest`.
- Um `Clock` injetável permite testar os tempos sem aguardar 30 ou 60 segundos reais.

## Como executar no VS Code

1. Instale o JDK 17 ou superior e o Apache Maven.
2. Instale a extensão **Extension Pack for Java**.
3. Abra esta pasta no VS Code.
4. Abra **Terminal > Novo Terminal**.
5. Execute `mvn test`.

O resultado esperado ao final é `BUILD SUCCESS`. Os relatórios ficam em `target/surefire-reports/`.

Também é possível abrir `UsuarioTest.java` e usar o botão **Run Test** que aparece acima dos métodos de teste.

## Print do Log

O Print do Log retornado pelos testes está na pasta ./assets.
