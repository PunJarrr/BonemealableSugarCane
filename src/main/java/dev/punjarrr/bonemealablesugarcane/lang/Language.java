package dev.punjarrr.bonemealablesugarcane.lang;

public enum Language {

    EN_US("en_US", "English"),
    DE_DE("de_DE", "Deutsch"),
    FR_FR("fr_FR", "Français"),
    FI_FI("fi_FI", "Suomi"),
    PL_PL("pl_PL", "Polski"),
    NL_NL("nl_NL", "Nederlands");

    private final String code;
    private final String displayName;

    Language(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Language fromCode(String code) {
        if (code == null) return EN_US;
        for (Language lang : values()) {
            if (lang.code.equalsIgnoreCase(code)) return lang;
        }
        return EN_US;
    }

    @Override
    public String toString() {
        return code;
    }
}
