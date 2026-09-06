# Fixtures de PowerTab

Los 18 archivos `.ptb` (y el `.pt2` que los acompaña) son **contenido de
terceros**, a diferencia de los fixtures de Guitar Pro: son archivos reales
tomados tal cual del propio repositorio del proyecto
[powertabeditor](https://github.com/powertab/powertabeditor) (licencia
GPLv3), del directorio `test/formats/powertab_old/data/`, donde forman parte
de su propia suite de tests (`test/formats/powertab_old/test_powertabold.cpp`).
Cada uno ejercita una sola sección del formato:

- `song_header.ptb` — los datos de la canción (título, artista, autor, etc.).
- `guitars.ptb` — guitarras y afinaciones, en las dos "score" del archivo.
- `barlines.ptb` — tipo de barra, armadura y medida.
- `staves.ptb` — pentagramas, clave y cantidad de cuerdas.
- `positions.ptb` — figura, silencio, doble puntillo y silencio de varios
  compases comprimido (multibar rest).
- `notes.ptb` — cuerda, traste y efectos de nota (armónico natural, tapped,
  trino, ligado, nota fantasma, octava).
- `alternate_endings.ptb` — finales alternativos (1ª, 2ª vuelta) y D.C./D.S.
- `tempo_markers.ptb` — marcador de tempo estándar.
- `guitar_ins.ptb` — reasignación de guitarras a pentagramas a mitad de la
  pieza (el "guitar in" del formato).
- `chordtext.ptb`, `chord_diagrams.ptb` — nombre y diagrama de acorde.
- `directions.ptb` — símbolos de navegación (Coda, Segno).
- `floating_text.ptb` — texto suelto sobre la partitura.
- `bends.ptb`, `tremolo_bars.ptb`, `volume_swells.ptb` — bend, palanca de
  trémolo y reguladores de volumen.
- `merge_multibar_rests.ptb` (+ `merge_multibar_rests_correct.pt2`) — otro
  caso de silencio de varios compases comprimido.

## Qué lee tabpro de cada uno

El lector de tabpro (`tabpro-format/.../powertab/`) no cubre todo lo que
prueba la suite original de powertabeditor: estructura (compases, pistas,
afinación), notas y sus duraciones, y un grupo razonable de efectos por nota
están soportados; lo que no, se declara con una excepción clara en vez de
adivinarse — ver el javadoc de `PowerTabFile`. En particular:

- `positions.ptb` y `merge_multibar_rests.ptb` **tienen que fallar** al
  importarse: usan un silencio de varios compases comprimido, que no se
  soporta.
- `guitar_ins.ptb` **tiene que fallar**: reasigna un pentagrama a otra
  guitarra a mitad de la pieza.
- `chord_diagrams.ptb`, `chordtext.ptb`, `floating_text.ptb`,
  `directions.ptb`, `bends.ptb`, `tremolo_bars.ptb` y `volume_swells.ptb` se
  importan sin error, pero esas secciones puntuales se descartan a
  propósito (no cambian el tono, la duración ni la cuerda de una nota: son
  anotación y decoración).

## Licencia

Estos archivos siguen bajo la licencia del proyecto de origen (GPLv3), no
bajo la licencia MIT de tabpro — y **eso es a propósito, no un descuido**:
la decisión de dejarlos así, en vez de reemplazarlos por sintéticos, se
tomó y quedó documentada acá para que quede clara la próxima vez que
alguien la lea. Son datos de prueba, no código que se distribuya como
parte del programa: la misma lógica con la que cualquier suite de tests
versiona fixtures de terceros para probar un lector de formato ajeno. El
código del lector en sí (`tabpro-format/.../powertab/`) es de tabpro, MIT
como el resto del proyecto; ver su javadoc para la procedencia de la
especificación que sí usa.
