package br.edu.ufu.atividade;

public class ExceededAttemptsException extends RuntimeException {
    public ExceededAttemptsException(String mensagem) {
        super(mensagem);
    }
}
