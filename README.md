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
