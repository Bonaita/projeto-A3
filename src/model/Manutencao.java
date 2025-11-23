package model;

import java.time.LocalDate;

public class Manutencao {

    private int id;
    private int idMaquina;
    private LocalDate dataAgendada;
    private String tipo;
    private String status;
    private String observacoes;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdMaquina() { return idMaquina; }
    public void setIdMaquina(int idMaquina) { this.idMaquina = idMaquina; }

    public LocalDate getDataAgendada() { return dataAgendada; }
    public void setDataAgendada(LocalDate dataAgendada) { this.dataAgendada = dataAgendada; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
