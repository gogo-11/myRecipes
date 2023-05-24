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
    MEAT("Месни"),
    MEATLESS("Безмесни"),
    DESSERTS("Десерти"),
    ALAMINUTES("Аламинути"),
    SOUPS("Супи"),
    SALADS("Салати"),
    DOUGH("Тестени");

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
