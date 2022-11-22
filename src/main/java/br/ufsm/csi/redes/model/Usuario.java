package br.ufsm.csi.redes.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@NoArgsConstructor
public class Usuario {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(endereco, usuario.endereco);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endereco);
    }

    private String nome;
    private Long timestamp;
    private StatusUsuario status;
    private InetAddress endereco;

    public Usuario(String nome, Long timestamp, StatusUsuario status, InetAddress endereco) {
        this.nome = nome;
        this.status = status;
        this.endereco = endereco;
        this.timestamp = timestamp;
    }

    public Usuario(String nome, StatusUsuario status, InetAddress endereco) {
        this.nome = nome;
        this.status = status;
        this.endereco = endereco;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public StatusUsuario getStatus() {
        return status;
    }

    public void setStatus(StatusUsuario status) {
        this.status = status;
    }

    public InetAddress getEndereco() {
        return endereco;
    }

    public void setEndereco(InetAddress endereco) {
        this.endereco = endereco;
    }

    public String toString() {
        return this.getNome() + " (" + getStatus().toString() + ")";
    }

    public enum StatusUsuario {
        DISPONIVEL, NAO_PERTURBE, VOLTO_LOGO
    }
}
