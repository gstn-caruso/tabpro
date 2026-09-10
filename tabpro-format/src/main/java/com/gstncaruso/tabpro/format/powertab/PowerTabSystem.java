package com.gstncaruso.tabpro.format.powertab;

import java.util.List;

/**
 * A PowerTab system: a line of the score that can hold several measures inside,
 * delimited by their barlines. The final barline only carries its type and repeat
 * count (the file stores no key signature or time signature of its own for it: it
 * never opens a new measure, it only closes the last one).
 */
record PowerTabSystem(
        PowerTabBarline startBar,
        List<PowerTabBarline> internalBarlines,
        int endBarType,
        int endBarRepeatCount,
        List<PowerTabStaff> staves,
        int rhythmSlashCount) {
}
