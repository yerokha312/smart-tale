package dev.yerokha.smarttale.util;


import lombok.Getter;

@Getter
public enum Authorities {

    CREATE_ORDER(1 << 0), // 1
    CREATE_POSITION(1 << 1), // 2
    UPDATE_POSITION(1 << 2), // 4
    INVITE_EMPLOYEE(1 << 3), // 8
    ASSIGN_EMPLOYEES(1 << 4), //16
    UPDATE_STATUS_TO_CHECKING(1 << 5), // 32
    UPDATE_STATUS_FROM_CHECKING(1 << 6), // 64
    DELETE_ORDER(1 << 7), // 128
    DELETE_EMPLOYEE(1 << 8), // 256
    DELETE_POSITION(1 << 9); // 512

    private final int bitmask;

    Authorities(int bitmask) {
        this.bitmask = bitmask;
    }

    public static int allAuthorities() {
        int allBitmask = 0;
        for (Authorities authority : Authorities.values()) {
            allBitmask |= authority.getBitmask();
        }
        return allBitmask; // 1023
    }
}
