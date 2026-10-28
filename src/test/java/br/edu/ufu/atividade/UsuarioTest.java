package br.edu.ufu.atividade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.RepetitionInfo;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {
    private Usuario usuario;
    private RelogioFalso relogio;

    @BeforeEach
    void prepararTeste(TestInfo info, TestReporter reporter) {
        relogio = new RelogioFalso();
        usuario = new Usuario(relogio);
        usuario.setNome("Matheus Rodrigues Rocha");
        usuario.setEmail("matheus.rocha@ufu.br");
        usuario.setSenha("123456");

        // TestInfo informa o nome do teste; TestReporter registra essa informação no relatório.
        reporter.publishEntry("Teste executado", info.getDisplayName());
    }

    @Test
    void deveAceitarNomeValido() {
        usuario.setNome("Ana Maria");

        assertEquals("Ana Maria", usuario.getNome());
    }

    @Test
    void naoDeveAceitarNomeComNumeroOuSimbolo() {
        assertFalse(usuario.validarNome("Ana123"));
        assertFalse(usuario.validarNome("Ana-Maria"));
        assertThrows(IllegalArgumentException.class, () -> usuario.setNome("Ana123"));
    }

    @Test
    void naoDeveAceitarNomeNuloOuVazio() {
        assertFalse(usuario.validarNome(null));
        assertFalse(usuario.validarNome(""));
        assertThrows(IllegalArgumentException.class, () -> usuario.setNome(null));
        assertThrows(IllegalArgumentException.class, () -> usuario.setNome(""));
    }

    @Test
    void deveAceitarEmailValido() {
        usuario.setEmail("ana.maria_1@dominio.com");

        assertEquals("ana.maria_1@dominio.com", usuario.getEmail());
    }

    @Test
    void naoDeveAceitarEmailForaDoPadrao() {
        assertFalse(usuario.validarEmail("ana maria@dominio.com"));
        assertFalse(usuario.validarEmail("ana@domínio.com"));
        assertFalse(usuario.validarEmail("email-sem-arroba"));
        assertThrows(IllegalArgumentException.class, () -> usuario.setEmail("email-sem-arroba"));
    }

    @Test
    void naoDeveAceitarEmailNuloOuVazio() {
        assertFalse(usuario.validarEmail(null));
        assertFalse(usuario.validarEmail(""));
        assertThrows(IllegalArgumentException.class, () -> usuario.setEmail(null));
        assertThrows(IllegalArgumentException.class, () -> usuario.setEmail(""));
    }

    @Test
    void naoDeveAceitarSenhaNulaOuVazia() {
        assertThrows(IllegalArgumentException.class, () -> usuario.setSenha(null));
        assertThrows(IllegalArgumentException.class, () -> usuario.setSenha(""));
    }

    @Test
    void deveAutenticarComSenhaCorreta() {
        assertTrue(usuario.autenticar("123456"));
    }

    @RepeatedTest(value = 3, name = "Senha errada - repetição {currentRepetition}/{totalRepetitions}")
    void deveRecusarSenhaErrada(RepetitionInfo repeticao, TestReporter reporter) {
        assertFalse(usuario.autenticar("senhaErrada"));

        reporter.publishEntry("Repetição", repeticao.getCurrentRepetition()
                + " de " + repeticao.getTotalRepetitions());
    }

    @Test
    void deveBloquearNaQuartaFalha() {
        assertFalse(usuario.autenticar("errada1"));
        assertFalse(usuario.autenticar("errada2"));
        assertFalse(usuario.autenticar("errada3"));

        assertThrows(ExceededAttemptsException.class,
                () -> usuario.autenticar("errada4"));
    }

    @Test
    void deveLiberarDepoisDeUmMinuto() {
        usuario.autenticar("errada1");
        usuario.autenticar("errada2");
        usuario.autenticar("errada3");
        assertThrows(ExceededAttemptsException.class, () -> usuario.autenticar("errada4"));

        // Antes de completar um minuto, até a senha correta fica bloqueada.
        assertThrows(ExceededAttemptsException.class, () -> usuario.autenticar("123456"));
        relogio.avancar(Duration.ofSeconds(59));
        assertThrows(ExceededAttemptsException.class, () -> usuario.autenticar("123456"));

        relogio.avancar(Duration.ofSeconds(1));
        assertTrue(usuario.autenticar("123456"));
    }

    @Test
    void deveReiniciarContagemDepoisDeTrintaSegundos() {
        assertFalse(usuario.autenticar("errada1"));
        assertFalse(usuario.autenticar("errada2"));
        assertFalse(usuario.autenticar("errada3"));

        relogio.avancar(Duration.ofSeconds(30));

        // A janela anterior terminou, então esta falha inicia uma nova contagem.
        assertFalse(usuario.autenticar("errada4"));
    }

    private static class RelogioFalso extends Clock {
        private Instant agora = Instant.parse("2026-01-01T10:00:00Z");

        void avancar(Duration tempo) {
            agora = agora.plus(tempo);
        }

        @Override
        public Instant instant() {
            return agora;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
