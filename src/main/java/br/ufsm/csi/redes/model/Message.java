package br.ufsm.csi.redes.model;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String text;
    private Usuario sender;
}
