package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.VoicePart;

/** Un hecho musical con posicion propia: una nota o un silencio. */
sealed interface TabEditEvent permits TabEditNoteEvent, TabEditRestEvent {

    TabEditPosition position();

    Duration duration();

    VoicePart voice();
}
