# tabpro

Clon libre de Guitar Pro 5: editor de tablaturas para guitarra.

## Stack

- Java 25
- Maven (multi-módulo)

## Módulos

- `tabpro-core` — modelo de dominio y parsing de tablaturas, sin dependencias de UI.
- `tabpro-app` — aplicación de escritorio (interfaz gráfica).

## Build

```sh
mvn verify
```

## Estado

Setup inicial del proyecto. Sin funcionalidad todavía — el desarrollo sigue TDD
(test que falla → mínimo código para pasarlo → refactor) a partir de acá.

## Licencia

[MIT](LICENSE)
