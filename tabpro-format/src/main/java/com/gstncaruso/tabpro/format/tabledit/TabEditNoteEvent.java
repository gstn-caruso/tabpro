package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.VoicePart;

/** Una nota ya ubicada: su posicion, su figura y la nota tal como suena. */
record TabEditNoteEvent(
        TabEditPosition position, Duration duration, VoicePart voice, Note note, boolean tapping, boolean slapping,
        boolean fadeIn) implements TabEditEvent {
}
