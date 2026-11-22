package com.ltm.insurance.customermicroservices.DTO;


public class MemberNumber {

    private static long sequence = 123401234L;

    public synchronized Long generateMemberId() {
        return sequence++;
    }
}