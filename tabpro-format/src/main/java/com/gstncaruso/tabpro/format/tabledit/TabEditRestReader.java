package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.model.VoicePart;

/**
 * Lee los siete bytes de carga util de un silencio. A diferencia de la nota,
 * la duracion ocupa el byte entero: no se mezcla con ninguna otra bandera.
 */
final class TabEditRestReader {

    TabEditRestFields read(TabEditByteReader input) {
        int durationCode = input.readUnsignedByte();

        int flags = input.readUnsignedByte();
        boolean bit4 = (flags & 0x10) != 0;
        boolean bit5 = (flags & 0x20) != 0;
        VoicePart voice = bit5 && bit4 ? VoicePart.BASS : VoicePart.LEAD;

        input.skip(5); // posicion vertical, corte de corchete secundario y banderas de dibujo: sin uso.

        return new TabEditRestFields(TabEditDurationMapper.toDuration(durationCode), voice);
    }
}
