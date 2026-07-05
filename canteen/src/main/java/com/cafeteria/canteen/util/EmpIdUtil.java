package com.cafeteria.canteen.util;

/**
 * Employee IDs are 8 digits starting with the prefix "700".
 * Users can enter either the full 8-digit ID or just the last 5 digits;
 * this helper normalizes both to the canonical 8-digit form.
 */
public final class EmpIdUtil {

    public static final String PREFIX = "700";

    private EmpIdUtil() {}

    public static String normalize(String empId) {
        if (empId == null) return null;
        String trimmed = empId.trim();
        if (trimmed.matches("\\d{5}")) {
            return PREFIX + trimmed;
        }
        return trimmed;
    }
}
