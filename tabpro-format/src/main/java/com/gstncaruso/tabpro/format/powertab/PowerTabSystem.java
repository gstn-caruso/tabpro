package com.gstncaruso.tabpro.format.powertab;

import java.util.List;

/**
 * Un sistema de PowerTab: una linea de la partitura que puede tener varios
 * compases adentro, delimitados por sus barras. La barra final solo trae su
 * tipo y su cantidad de repeticion (el archivo no le guarda armadura ni
 * medida propias: nunca abre un compas nuevo, solo cierra el ultimo).
 */
record PowerTabSystem(
        PowerTabBarline startBar,
        List<PowerTabBarline> internalBarlines,
        int endBarType,
        int endBarRepeatCount,
        List<PowerTabStaff> staves,
        int rhythmSlashCount) {
}
