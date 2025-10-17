package edu.java3projectpetmatchapp.enums;

public enum Sociability {

    LIKES_CATS("Good with cats"),
    DISLIKES_CATS("Not good with cats"),
    LIKES_DOGS("Good with dogs"),
    DISLIKES_DOGS("Not good with dogs"),
    LIKES_KIDS("Good with kids"),
    DISLIKES_KIDS("Not Good with kids"),
    UNKNOWN("Unknown");

    private final String label;

    Sociability(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}