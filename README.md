# tabpro

Clon libre de Guitar Pro 5: editor de tablaturas para guitarra.

## Stack

- Java 25
- Maven (multi-módulo)
- Swing + FlatLaf (look Darcula)

## Módulos

- `tabpro-core` — modelo de dominio de la partitura, sesión de edición y puertos; sin dependencias de framework.
- `tabpro-format` — persistencia en el formato propio `.tabpro` (JSON con Gson).
- `tabpro-midi` — reproducción con `javax.sound.midi`.
- `tabpro-ui` — interfaz Swing: ventana, canvas de tablatura, teclado y transporte (sin FlatLaf).
- `tabpro-app` — `main`, FlatLaf, cableado de módulos y empaquetado (jar ejecutable + `.deb`).

## Build

```sh
mvn verify
```

Ejecutar:

```sh
java -jar tabpro-app/target/tabpro-app-0.1.0-SNAPSHOT.jar
```

## Estado

Fase 0: cinco módulos en el reactor y ventana vacía. El desarrollo sigue TDD
(test que falla → mínimo código para pasarlo → refactor).

## Licencia

[MIT](LICENSE)
