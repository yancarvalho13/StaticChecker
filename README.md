# StaticChecker

Checker léxico da linguagem Ali2026-2, implementado em Java com Gradle.
Compile com `./gradlew build` e execute com `java -jar build/libs/StaticChecker-1.0-SNAPSHOT.jar caminho/MeuTeste`.

## Módulos

### Main
Recebe o caminho ou nome base do fonte e acrescenta `.262` quando necessário.
Inicia o controlador e apresenta erros de entrada no console.

### CheckerController
Coordena a leitura, o Lexer, o ScopeController e a tabela de símbolos.
Exibe tokens e a tabela no console; ainda não gera `.LEX` nem `.TAB`.

### SourceReader e CharStatus
SourceReader lê o fonte ASCII e registra linha e coluna de cada caractere.
CharStatus representa um caractere junto à sua posição no arquivo.

### Lexer, Token e TokenType
Lexer produz um token por chamada, filtrando comentários e aplicando os padrões de Ali2026-2.
Token guarda os dados do átomo; TokenType define sua categoria, lexema reservado e código oficial.

### ScopeController
Acompanha declarações para classificar nomes e atribuir tipos a variáveis, vetores, funções e parâmetros.
Não valida a gramática completa nem implementa análise sintática.

### SymbolTable e SymbolEntry
SymbolTable indexa identificadores sem diferenciar maiúsculas e minúsculas e preserva a ordem.
SymbolEntry guarda código, tipo, lexema, tamanhos, tipo do símbolo e as cinco primeiras ocorrências.
