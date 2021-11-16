package com.study;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum State {
    IDLE(1),
    RUNNING(2),
    INTERRUPTED(0);

    private int value;
}
