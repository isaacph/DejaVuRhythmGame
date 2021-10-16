package game;

import org.joml.Vector2f;

public class MathUtil {

    public static boolean pointInside(float point, float min, float max) {
        return min <= point && point <= max;
    }

    public static boolean pointInside(Vector2f point, Vector2f center, Vector2f scale) {
        return pointInside(point.x, center.x - scale.x / 2, center.x + scale.x / 2) &&
                pointInside(point.y, center.y - scale.y / 2, center.y + scale.y / 2);
    }
}
