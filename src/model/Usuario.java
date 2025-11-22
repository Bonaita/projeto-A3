package model;

import java.time.LocalDateTime;

public class Usuario {

    private int id;
    private String nomeCompleto;
    private String login;
    private String senhaHash;
    private boolean bloqueado;
    private String role;

    // Campos necessários para AuthService
    private int tentativasLogin;
    private LocalDateTime ultimoLogin;

    public Usuario() {}

    // ===== ID =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // ===== Nome =====
    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    // ===== Login =====
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    // ===== Senha =====
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    // ===== Bloqueado =====
    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }

    // ===== Role =====
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }

    // ===== Tentativas de Login =====
    public int getTentativasLogin() { return tentativasLogin; }
    public void setTentativasLogin(int tentativasLogin) { this.tentativasLogin = tentativasLogin; }

    // ===== Último login =====
    public LocalDateTime getUltimoLogin() { return ultimoLogin; }
    public void setUltimoLogin(LocalDateTime ultimoLogin) { this.ultimoLogin = ultimoLogin; }
}
