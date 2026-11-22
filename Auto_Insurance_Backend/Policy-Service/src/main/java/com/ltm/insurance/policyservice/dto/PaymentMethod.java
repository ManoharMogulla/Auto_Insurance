package com.ltm.insurance.policyservice.dto;

public enum PaymentMethod {
    CARD,
    UPI,
    NET_BANKING,
    WALLET,
    CASH,
    BANK_TRANSFER,
    CHEQUE
}




//package com.ltm.insurance.paymentservice.model;
//
//public enum PaymentMethod {
//    CARD(false),
//    UPI(false),
//    NET_BANKING(false),
//    WALLET(false),
//    CASH(true),
//    BANK_TRANSFER(true),
//    CHEQUE(true);
//
//    private final boolean manualSettlement;
//
//    PaymentMethod(boolean manualSettlement) {
//        this.manualSettlement = manualSettlement;
//    }
//
//    public boolean requiresManualSettlement() {
//        return manualSettlement;
//    }
//}
