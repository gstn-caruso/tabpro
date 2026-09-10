package com.gstncaruso.tabpro.format.powertab;

import java.util.List;

/** A PowerTab staff: its string count and its two voices of positions. */
record PowerTabStaff(int stringCount, List<List<PowerTabPosition>> voices) {
}
