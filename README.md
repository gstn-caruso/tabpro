# tabpro

Clon libre de Guitar Pro 5: editor de tablaturas para guitarra, minimal, en Java 25 con Swing.

## Estado

**v0.2.3 — MVP.** Editor de tablatura de una pista con edición por teclado al estilo
Guitar Pro 5, guardado y apertura en formato propio `.tabpro`, y reproducción MIDI
con cursor que sigue la música.

El modelo ya es multipista y cada pista tiene su mixer (instrumento General MIDI,
volumen, paneo, silenciar y solo), que se guarda en el archivo y se escucha en la
reproducción. La sesión de edición sabe agregar, seleccionar y quitar pistas y
mantiene todas alineadas compás a compás; la interfaz todavía muestra una sola pista.

Fuera de alcance por ahora: pentagrama (notación estándar), efectos (bends, slides,
ligados), dos voces por compás, tresillos e importación o exportación de archivos
`.gp5`.

## Instalación

**Debian / Ubuntu:** bajá el `.deb` de la última
[release](https://github.com/gstn-caruso/tabpro/releases) e instalalo:

```sh
sudo apt install ./tabpro_0.2.3_all.deb
tabpro
```

Queda también en el menú de aplicaciones. Necesita una JRE 25 con entorno gráfico
(`openjdk-25-jre`); la variante *headless* no alcanza.

**Cualquier sistema con Java 25:**

```sh
java -jar tabpro-app-0.2.3.jar
```

## Uso

El cursor marca una celda (compás, beat, cuerda). Todo se hace desde el teclado:

| Tecla | Acción |
|---|---|
| `0`–`9` | Escribir el traste en la cuerda del cursor. Dos dígitos seguidos (menos de 700 ms) forman trastes del 10 al 24. |
| `←` `→` | Beat anterior / siguiente. Al final de un compás incompleto agrega un silencio; al final de la partitura agrega un compás. |
| `↑` `↓` | Cuerda anterior / siguiente. |
| `Inicio` `Fin` | Primer / último beat del compás. |
| `+` `-` | Alargar / acortar la figura del beat (redonda … semifusa). |
| `.` | Puntillo. |
| `R` | Convertir el beat en silencio. |
| `Retroceso` | Borrar la nota bajo el cursor. |
| `Insert` `Supr` | Insertar un silencio antes del cursor / borrar el beat. |
| `Ctrl+Insert` `Ctrl+Supr` | Insertar un compás vacío antes del actual / borrar el compás. |
| `Ctrl+Z` `Ctrl+Y` | Deshacer / rehacer. |
| `Espacio` | Reproducir / detener. El tempo se cambia desde la barra de herramientas. |
| `Ctrl+N` `Ctrl+O` `Ctrl+S` | Nueva partitura / abrir / guardar. |

Un clic sobre la tablatura mueve el cursor a esa celda. El número de compás se pinta
en naranja cuando la suma de figuras no coincide con el compás.

## Formato `.tabpro`

JSON legible y versionado. Las afinaciones son números MIDI (cuerda 1 = la más aguda),
`value` es el denominador de la figura (4 = negra, 8 = corchea, …) y un beat sin notas
es un silencio. `volume` y `pan` van de 0 a 127 (paneo centrado en 64); los archivos de
la versión 1 se siguen abriendo y toman los valores por defecto:

```json
{
  "format": 2,
  "title": "Prueba",
  "tempo": 120,
  "tracks": [
    {
      "name": "Guitarra",
      "midiProgram": 25,
      "volume": 100,
      "pan": 64,
      "muted": false,
      "solo": false,
      "tuning": [64, 59, 55, 50, 45, 40],
      "measures": [
        {
          "timeSignature": { "beats": 4, "beatUnit": 4 },
          "beats": [
            { "value": 4, "dotted": false, "notes": [ { "string": 6, "fret": 0 } ] },
            { "value": 8, "dotted": true, "notes": [] }
          ]
        }
      ]
    }
  ]
}
```

## Stack

- Java 25
- Maven (multi-módulo)
- Swing + FlatLaf (look Darcula)
- Gson para el formato propio
- `javax.sound.midi` para la reproducción

## Módulos

Las dependencias van en una sola dirección hacia `tabpro-core`; la interfaz habla con
el formato y con MIDI solo a través de los puertos `ScoreFiles` y `Player` definidos en core.

- `tabpro-core` — modelo de dominio de la partitura (inmutable), sesión de edición con
  undo/redo, timeline de reproducción y puertos; sin dependencias de framework.
- `tabpro-format` — persistencia en el formato propio `.tabpro` (JSON con Gson).
- `tabpro-midi` — reproducción con `javax.sound.midi`, con un marcador por beat para
  mover el cursor sin polling.
- `tabpro-ui` — interfaz Swing: ventana, canvas de tablatura, teclado, documento y
  transporte (sin FlatLaf).
- `tabpro-app` — `main`, FlatLaf, cableado de módulos y empaquetado (jar ejecutable + `.deb`).

## Build

```sh
mvn verify
```

Genera `tabpro-app/target/tabpro-app-<versión>.jar` (ejecutable) y
`tabpro-app/target/tabpro_<versión>_all.deb`.

```sh
java -jar tabpro-app/target/tabpro-app-0.2.3.jar
```

## Desarrollo

Todo comportamiento entra por un test que falla primero (TDD). Cada cambio va en una
branch, se abre un PR contra `main` y el CI (`mvn -B verify`, headless) es el gate para
mergear. Un tag `vX.Y.Z` en `main` dispara el workflow de release, que construye el `.deb`
y lo publica en GitHub Releases.

## Licencia

[MIT](LICENSE)
