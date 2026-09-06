package com.gstncaruso.tabpro.format.powertab;

import java.util.List;

/** Un pentagrama de PowerTab: su cantidad de cuerdas y sus dos voces de posiciones. */
record PowerTabStaff(int stringCount, List<List<PowerTabPosition>> voices) {
}
