# Fixtures de Guitar Pro

## `tabpro-synthetic.*`

Los tres archivos tienen el mismo contenido musical escrito en
las tres generaciones del formato: una pista de guitarra en afinación estándar,
dos compases de 4/4 en negras, con una escala de Do mayor ascendente sobre la
quinta cuerda.

## `tabpro-features.*`

Los mismos tres formatos, con cuatro compases y tres pistas que ejercitan lo que
el lector tiene que entender: dinámica, bend, ligado, slide, armónico natural,
segunda voz (sólo en gp5, que es donde el formato la tiene), cambio de compás a
3/4, repetición con finales alternativos, diagrama de acorde, una pista de bajo
de cuatro cuerdas y una de percusión en el canal 10.

## `tabpro-effects2.*`

Tres compases con los efectos que el lector ramifica por versión: un par de notas
ligadas que arma un ligado real, vibrato, trino, trémolo de púa y los armónicos
artificial, tapped, pinch y semitono. GP3 no soporta el trino, el trémolo ni los
armónicos que no sean natural o artificial: ahí se pierden al grabar, y no es un
error del lector.

## `tabpro-features-v5.00.gp5`

El mismo contenido que `tabpro-features.gp5` pero grabado como v5.00, que guarda
las pistas de otra manera: el byte de banderas va delante de todas y no sólo de
la primera, y el instrumento de RSE ocupa un byte menos.

## Origen

Son **contenido original**, generado a propósito para probar el lector de tabpro.
No son material de Guitar Pro ni de Arobas Music, así que se pueden versionar y
redistribuir con el resto del proyecto bajo la licencia MIT.
