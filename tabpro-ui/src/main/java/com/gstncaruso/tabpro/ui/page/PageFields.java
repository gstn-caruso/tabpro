package com.gstncaruso.tabpro.ui.page;

import com.gstncaruso.tabpro.core.model.ScoreInfo;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Los valores con los que se llenan los campos del encabezado y del pie de una hoja: los datos de
 * Informacion de la partitura mas en que hoja de cuantas estamos. Como dice el manual, las
 * palabras entre corchetes -[%title], [%artist], [%page]…- se reemplazan por su valor; lo que no
 * es un campo conocido queda tal cual lo escribio quien configuro la pagina.
 */
public record PageFields(ScoreInfo info, int pageNumber, int pageCount) {

    private static final Pattern FIELD = Pattern.compile("\\[%([a-zA-Z]+)]");

    public String fillIn(String template) {
        return FIELD.matcher(template).replaceAll(match -> Matcher.quoteReplacement(
                valueOf(match.group(1)).orElseGet(match::group)));
    }

    /**
     * Si vale la pena imprimir esa linea: un texto sin campos siempre dice algo, pero uno que solo
     * tiene campos que la partitura nunca lleno dejaria una linea vacia o un rotulo suelto.
     */
    public boolean hasAnythingToSay(String template) {
        Matcher fields = FIELD.matcher(template);
        boolean sawAField = false;
        while (fields.find()) {
            Optional<String> value = valueOf(fields.group(1));
            if (value.isEmpty()) {
                continue;
            }
            sawAField = true;
            if (!value.get().isBlank()) {
                return true;
            }
        }
        return !sawAField;
    }

    private Optional<String> valueOf(String field) {
        return switch (field.toLowerCase(Locale.ROOT)) {
            case "title" -> Optional.of(info.title());
            case "subtitle" -> Optional.of(info.subtitle());
            case "artist" -> Optional.of(info.artist());
            case "album" -> Optional.of(info.album());
            case "words" -> Optional.of(info.lyricsAuthor());
            case "music" -> Optional.of(info.musicAuthor());
            case "copyright" -> Optional.of(info.copyright());
            case "transcriber" -> Optional.of(info.transcriber());
            case "page" -> Optional.of(String.valueOf(pageNumber));
            case "pages" -> Optional.of(String.valueOf(pageCount));
            default -> Optional.empty();
        };
    }
}
