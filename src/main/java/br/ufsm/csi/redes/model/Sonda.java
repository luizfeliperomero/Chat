package br.ufsm.csi.redes.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sonda {
    private String tipoMensagem;
    private String username;
    private Usuario.StatusUsuario status;
}
