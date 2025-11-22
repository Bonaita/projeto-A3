package utils;

import services.AuditoriaService;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorHandler {

    private static final AuditoriaService auditoria = new AuditoriaService();

    /**
     * Trata uma exceção globalmente.
     *
     * @param e        Exceção capturada
     * @param acao     Descrição do que o usuário estava tentando fazer
     * @param usuarioId ID do usuário logado (0 se desconhecido)
     */
    public static void tratar(Exception e, String acao, int usuarioId) {

        // 1 — Converter stacktrace para texto
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stacktrace = sw.toString();

        // 2 — Registrar auditoria
        try {
            auditoria.registrar(usuarioId, "ERROR", acao + " — " + e.getMessage());
        } catch (Exception ignored) {}

        // 3 — Registrar no console (dev mode)
        System.err.println("==== ERRO NO SISTEMA ====");
        System.err.println("Ação: " + acao);
        System.err.println(stacktrace);

        // 4 — Mostrar mensagem moderna ao usuário
        JOptionPane.showMessageDialog(
                null,
                "Ocorreu um erro ao executar esta ação:\n\n" + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Exibe uma mensagem amigável ao usuário (sem crash).
     */
    public static void mensagem(String texto) {
        JOptionPane.showMessageDialog(
                null,
                texto,
                "Aviso",
                JOptionPane.WARNING_MESSAGE
        );
    }

    /**
     * Exibe uma mensagem de sucesso.
     */
    public static void sucesso(String texto) {
        JOptionPane.showMessageDialog(
                null,
                texto,
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
