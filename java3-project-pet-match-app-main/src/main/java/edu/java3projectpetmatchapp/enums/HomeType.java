package edu.java3projectpetmatchapp.enums;

public enum HomeType {
    CONDO_APARTMENT("One unit among many within a building"),
    SINGLE_DETACHED("A house or duplex type building");

    private final String label;

    HomeType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;}

}