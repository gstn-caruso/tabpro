# tabpro

Clon libre de Guitar Pro 5: editor de tablaturas y partituras para guitarra, en
Java 25 con Swing.

## Estado

**v0.7.1 — El clon.** tabpro hace lo que describe el manual de Guitar Pro 5, con
la misma forma de pantalla pero con una estética moderna y plana en vez de la de
Windows XP.

La partitura se ve como un cancionero publicado: en **modo página** hay una hoja
con márgenes, encabezado con título, artista, álbum y créditos, y pie con el
copyright. También están el modo pergamino y las dos pantallas, con zoom del 30%
al 200%.

Cada pista lleva su **pentagrama** arriba y su **tablatura** abajo, con clave,
armadura, figuras, plicas, barras de unión, silencios, puntillos, alteraciones,
ligaduras, grupos irregulares y **dos voces**. Sobre eso van todos los símbolos
del capítulo *Add Symbols*: palm mute, let ring, tapping, slap y pop, armónicos,
vibrato y vibrato amplio, trino, trémolo de púa, rasgueos, púa, notas fantasma y
muertas, acentos, staccato, ligados, slides, bends con su curva, notas de adorno,
digitación de las dos manos, texto libre y diagramas de acordes. Y la estructura
del compás: repeticiones con su conteo, finales alternativos, doble barra,
direcciones musicales (Coda, Segno, Fine y los catorce saltos) y marcadores.

Arriba, el **diapasón** y el **teclado** marcan las notas del beat y escriben al
clic, con los modos de visualización del manual, nombres de notas, tipos de
diapasón, zurdo o diestro y la nota que está bajo el mouse. Abajo, la **mesa de
mezcla** al estilo de Guitar Pro (puerto, canal, instrumento General MIDI,
volumen, paneo, chorus, reverb, phaser, trémolo, silenciar y solo) y la **vista
global** con la zona de marcadores y un cuadradito por compás.

Suena en MIDI respetando el orden real de los compases —repeticiones, finales
alternativos y saltos— y los efectos: bends y palanca con pitch bend, slides,
ligados, trinos, trémolos, armónicos, rasgueos demorados, notas de adorno, fade
in y swing. Con metrónomo, cuenta regresiva, loop con entrenador de velocidad,
tempo relativo de x0.25 a x2 y modo paso a paso.

Trae además las dos herramientas del guitarrista: la **ventana de acordes**, que
genera los diagramas para cualquier afinación, los nombra y los digita, y la
**ventana de escalas**, con su biblioteca y el buscador de la escala que usa un
rango de compases. Más el afinador, el asistente de percusión y los seis
asistentes del menú Herramientas.

La partitura se puede mirar de a una pista o **multipista**, apagando las que
molestan desde la mesa de mezcla, y el menú Ver puede esconder el pentagrama o
la tablatura en toda la partitura: lo que se esconde no deja hueco, la notación
que queda sube a su lugar.

**Lo que todavía no está:** el motor de sonido realista (RSE), que es
propietario, y con él la exportación a WAVE; la exportación al formato de Guitar
Pro; y la importación de PowerTab y de TablEdit.

## Instalación

**Debian / Ubuntu:** bajá el `.deb` de la última
[release](https://github.com/gstn-caruso/tabpro/releases) e instalalo:

```sh
sudo apt install ./tabpro_0.7.1_all.deb
tabpro
```

Queda en el menú de aplicaciones y se asocia a los archivos `.tabpro` y a los de
Guitar Pro: abrir cualquiera de los dos desde el escritorio abre tabpro con esa
partitura. Necesita una JRE 25 con entorno gráfico (`openjdk-25-jre`); la
variante *headless* no alcanza.

**Cualquier sistema con Java 25:**

```sh
java -jar tabpro-app-0.7.1.jar [archivo]
```

## Uso

El cursor marca una celda (pista, compás, voz, beat, cuerda). Se escribe con el
teclado de la computadora, clickeando el diapasón o el teclado de arriba, o
tocando un instrumento MIDI conectado.

Los atajos son los del manual: los dígitos escriben el traste, las flechas mueven
el cursor, `+` acorta la figura y `-` la alarga, `R` pone un silencio, `L` liga, `/` hace
un tresillo, `H` un ligado, `S` un slide, `B` un bend, `V` vibrato, `P` palm
mute, `I` let ring, `X` nota muerta, `O` nota fantasma, `G` nota de adorno, `T`
texto, `A` acorde, `F` fade in, `Espacio` reproduce. `F5` abre la información de
la partitura, `F6` las propiedades de la pista, `F7` el instrumento, `F8` la
configuración de página, `F9` el loop, `F12` las preferencias. `Ctrl+Tab` y
`Shift+Tab` saltan entre marcadores y `F1` abre la lista completa de atajos.

Los doce menús —Archivo, Editar, Compás, Pista, Nota, Efectos, Marcadores,
Herramientas, Sonido, Ver, Opciones y Ayuda— llevan todo lo demás.

## Formato `.tabpro`

JSON legible y versionado, en su versión 3. Guarda efectos, ligaduras, grupos
irregulares, las dos voces, los atributos de cada compás, las propiedades de
pista, los diez datos del encabezado y la letra. Sigue abriendo los archivos de
las versiones 1 y 2.

También importa y exporta MIDI, tablatura ASCII y MusicXML, exporta la partitura
como imagen y como PDF, la imprime, y abre archivos `.gp3`, `.gp4`, `.gp5` y
`.gtp`.

## Stack

- Java 25
- Maven (multi-módulo)
- Swing + FlatLaf (tema propio, claro y oscuro)
- Gson para el formato propio
- `javax.sound.midi` para la reproducción y la captura

## Módulos

Las dependencias van en una sola dirección hacia `tabpro-core`; la interfaz habla
con el formato y con MIDI sólo a través de los puertos `ScoreFiles`,
`ScoreExchange` y `Player`, definidos en core.

- `tabpro-core` — el modelo de la partitura (inmutable), la sesión de edición con
  deshacer y rehacer, la notación, la armonía (acordes y escalas), los asistentes
  y la reproducción.
- `tabpro-format` — el formato propio `.tabpro`, los formatos de intercambio
  (MIDI, ASCII, MusicXML) y el lector de archivos de Guitar Pro.
- `tabpro-midi` — reproducción y captura con `javax.sound.midi`.
- `tabpro-ui` — la interfaz Swing: partitura, diapasón, teclado, percusión, mesa
  de mezcla, vista global, barra de estado, menús, barras de herramientas y las
  ventanas de diálogo.
- `tabpro-app` — `main`, el tema, el cableado de módulos y el empaquetado (jar
  ejecutable + `.deb`).

## Build

```sh
mvn verify
```

Genera `tabpro-app/target/tabpro-app-<versión>.jar` (ejecutable) y
`tabpro-app/target/tabpro_<versión>_all.deb`.

## Desarrollo

Todo comportamiento entra por un test que falla primero (TDD). Cada cambio va en
una branch, se abre un PR contra `main` y el CI (`mvn -B verify`, headless) es el
gate para mergear. Un tag `vX.Y.Z` en `main` dispara el workflow de release, que
construye el `.deb` y lo publica en GitHub Releases.

## Licencia

[MIT](LICENSE)
