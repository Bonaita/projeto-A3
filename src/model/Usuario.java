package model;

/**
 * Modelo simples para representar o usuário.
 */
public class Usuario {
    private int id;
    private String login;
    private String nomeCompleto;
    private String senhaHash;
    private String role;
    private boolean primeiroAcesso;
    private int tentativasLogin;
    private boolean bloqueado;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isPrimeiroAcesso() { return primeiroAcesso; }
    public void setPrimeiroAcesso(boolean primeiroAcesso) { this.primeiroAcesso = primeiroAcesso; }

    public int getTentativasLogin() { return tentativasLogin; }
    public void setTentativasLogin(int tentativasLogin) { this.tentativasLogin = tentativasLogin; }

    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }
}