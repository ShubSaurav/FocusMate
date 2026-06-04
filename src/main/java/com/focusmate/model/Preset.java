package com.focusmate.model;

public class Preset {
    public Integer id;
    public String name;
    public int focusMin;
    public int shortBreakMin;
    public int longBreakMin;
    public int cyclesBeforeLong;

    @Override
    public String toString() {
        return name + " (" + focusMin + "m)";
    }
}