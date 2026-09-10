package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.VoicePart;

record TabEditRestEvent(TabEditPosition position, Duration duration, VoicePart voice) implements TabEditEvent {
}
