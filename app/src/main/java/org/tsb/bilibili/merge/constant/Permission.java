package org.tsb.bilibili.merge.constant;

public enum Permission {
    CODE_FOR_WRITE_PERMISSION(1);
    private int value = 0;

    private Permission(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
