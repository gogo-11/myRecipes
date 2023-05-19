package com.myrecipe.entities;

/**
 * Възможните категории за рецептите
 *
 * {@link #MEAT}
 * {@link #MEATLESS}
 * {@link #DESSERTS}
 * {@link #ALAMINUTES}
 * {@link #SOUPS}
 * {@link #SALADS}
 * {@link #DOUGH}
 */
public enum Categories {
    MEAT("месни"),
    MEATLESS("безмесни"),
    DESSERTS("десерти"),
    ALAMINUTES("аламинути"),
    SOUPS("супи"),
    SALADS("салати"),
    DOUGH("тестени");

    private final String explanation;

    Categories(String explanation) {
        this.explanation = explanation;
    }

    public static boolean categoryExists(String name) {
        boolean result = false;
        for (Categories category : values()) {
            if (category.name().equalsIgnoreCase(name)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public String getExplanation() {
        return explanation;
    }
}
