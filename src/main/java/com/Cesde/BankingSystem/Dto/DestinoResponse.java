package com.Cesde.BankingSystem.Dto;

// Cuenta MiPlata de destino de una transferencia: lo que se muestra en la
// confirmacion de TransferPanel.jsx antes de enviar el dinero
public record DestinoResponse(String fullName, String username, String accountType, String accountNumber) {
}
