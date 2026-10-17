package dev.towkie.morph;

/** Per-player visual model parameters. All values are multipliers. */
public final class Scale {
    public static final String[] KEYS = {"overall", "height", "width", "head", "body", "arms", "legs"};
    public static final String[] LABELS = {"Общий масштаб", "Высота", "Ширина", "Голова", "Туловище", "Руки", "Ноги"};
    public float overall = 1f;
    public float height = 1f;
    public float width = 1f;
    public float head = 1f;
    public float body = 1f;
    public float arms = 1f;
    public float legs = 1f;

    public float get(int i) {
        return switch (i) {
            case 0 -> overall;
            case 1 -> height;
            case 2 -> width;
            case 3 -> head;
            case 4 -> body;
            case 5 -> arms;
            case 6 -> legs;
            default -> 1f;
        };
    }
    public void set(int i, float value) {
        // Prevent invisible models / huge geometry and bad matrix operations.
        float min = i == 0 ? 0.25f : 0.5f;
        float max = i == 0 ? 4f : 3f;
        value = Math.max(min, Math.min(max, value));
        switch (i) {
            case 0 -> overall = value;
            case 1 -> height = value;
            case 2 -> width = value;
            case 3 -> head = value;
            case 4 -> body = value;
            case 5 -> arms = value;
            case 6 -> legs = value;
            default -> { }
        }
    }
}
