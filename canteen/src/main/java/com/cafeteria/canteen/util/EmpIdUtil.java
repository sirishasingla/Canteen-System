package com.cafeteria.canteen.util;

/**
 * Employee IDs are 8-digit numeric strings starting with "7".
 * Users may enter either the full 8-digit ID, or a shorter numeric suffix — in which case
 * this helper pads with "7" + enough zeros to reach the canonical 8-digit form.
 *
 * Examples: "12345"  → "70012345", "1234" → "70001234", "9" → "70000009",
 *           "70012345" → "70012345" (unchanged), "abc" → "abc" (untouched; downstream rejects).
 */
public final class EmpIdUtil {

    public static final int LENGTH = 8;
    public static final char PREFIX_DIGIT = '7';

    private EmpIdUtil() {}

    public static String normalize(String empId) {
        if (empId == null) return null;
        String trimmed = empId.trim();
        // Only pad numeric inputs shorter than the canonical length.
        if (trimmed.matches("\\d{1," + (LENGTH - 1) + "}")) {
            int zeros = LENGTH - 1 - trimmed.length();
            StringBuilder sb = new StringBuilder(LENGTH);
            sb.append(PREFIX_DIGIT);
            for (int i = 0; i < zeros; i++) sb.append('0');
            sb.append(trimmed);
            return sb.toString();
        }
        return trimmed;
    }
}
