package br.edu.ufu.atividade;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

public class Usuario {
    private String nome;
    private String email;
    private String senhaHash;

    private final Clock relogio;
    private Instant primeiraFalha;
    private Instant bloqueadoAte;
    private int tentativasFalhas;

    private static final Duration JANELA_TENTATIVAS = Duration.ofSeconds(30);
    private static final Duration TEMPO_BLOQUEIO = Duration.ofMinutes(1);

    public Usuario() {
        this(Clock.systemUTC());
    }

    // O relógio pode ser injetado para testar os tempos sem esperar de verdade.
    public Usuario(Clock relogio) {
        this.relogio = Objects.requireNonNull(relogio, "O relógio não pode ser nulo.");
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (!validarNome(nome)) {
            throw new IllegalArgumentException("Nome inválido.");
        }
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (!validarEmail(email)) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
        this.email = email;
    }

    public void setSenha(String senha) {
        if (senha == null || senha.isEmpty()) {
            throw new IllegalArgumentException("A senha não pode ser nula ou vazia.");
        }
        this.senhaHash = gerarHash(senha);
    }

    public boolean validarNome(String nome) {
        if (nome == null || nome.isEmpty()) {
            return false;
        }

        return nome.matches("[\\p{L}]+(?: [\\p{L}]+)*");
    }

    public boolean validarEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        // Para o domínio, considera letras, números e ponto.
        return email.matches("[A-Za-z0-9._]+@[A-Za-z0-9.]+");
    }

    public boolean autenticar(String senha) {
        if (senha == null || senha.isEmpty()) {
            throw new IllegalArgumentException("A senha não pode ser nula ou vazia.");
        }

        Instant agora = relogio.instant();
        verificarBloqueio(agora);

        if (senhaHash != null && senhaHash.equals(gerarHash(senha))) {
            limparTentativas();
            return true;
        }

        registrarFalha(agora);
        return false;
    }

    private void registrarFalha(Instant agora) {
        if (primeiraFalha == null
                || Duration.between(primeiraFalha, agora).compareTo(JANELA_TENTATIVAS) >= 0) {
            primeiraFalha = agora;
            tentativasFalhas = 1;
        } else {
            tentativasFalhas++;
        }

        if (tentativasFalhas >= 4) {
            bloqueadoAte = agora.plus(TEMPO_BLOQUEIO);
            throw new ExceededAttemptsException(
                    "Limite de tentativas excedido. Tente novamente após um minuto.");
        }
    }

    private void verificarBloqueio(Instant agora) {
        if (bloqueadoAte == null) {
            return;
        }

        if (agora.isBefore(bloqueadoAte)) {
            throw new ExceededAttemptsException(
                    "Usuário bloqueado. Tente novamente após um minuto.");
        }

        // O minuto passou. O usuário pode tentar novamente.
        bloqueadoAte = null;
        limparTentativas();
    }

    private void limparTentativas() {
        primeiraFalha = null;
        tentativasFalhas = 0;
    }

    private String gerarHash(String senha) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(senha.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Não foi possível gerar o hash da senha.", e);
        }
    }
}
