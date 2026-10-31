package com.Cesde.BankingSystem.Dto;

// Transferencia desde TransferPanel.jsx:
//  - destinationType: "phone" (celular), "AH" (cuenta de ahorros) o "CC" (cuenta corriente)
//  - bank: "MiPlata", otro banco (Bancolombia...) o billetera (Nequi, Daviplata)
//  - destination: el celular o el numero de cuenta de destino
public record TransferenciaRequest(
        String username,
        String fromAccountNumber,
        Double amount,
        String destinationType,
        String bank,
        String destination
) {
}
