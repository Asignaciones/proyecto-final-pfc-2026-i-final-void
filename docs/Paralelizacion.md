# Informe de paralelización

## Integrantes del grupo

| Nombre completo            | Código   | Correo electrónico                       |
|----------------------------|----------|------------------------------------------|
| Kevin Alejandro Marulanda  | 2380697  | Kevin.marulanda@correounivalle.edu.co    |

## Estrategia de paralelización

La paralelización del proyecto se apoyó en dividir el trabajo en partes
independientes y combinar los resultados al final. La idea general fue conservar
la semántica de las funciones secuenciales y, al mismo tiempo, reducir el tiempo
de ejecución en instancias donde el costo de cómputo justifica el uso de hilos.

Ejemplo de estructura esperada:

- **`choquesPar`** divide el vector de cursos en dos mitades. Cada mitad cuenta
  los choques parciales y luego se suman los resultados.
- **`desperdicioPar`** aplica el mismo criterio de división y suma el desperdicio
  calculado en cada subrango.
- **`movilidadPar`** primero ordena los cursos asignados por hora de inicio y
  luego divide la lista de pares consecutivos para sumar distancias en paralelo.
- **`generarAsignacionesPar`** construye el espacio de asignaciones a partir de
  la elección del primer curso, paralelizando las mitades del conjunto de aulas
  posibles.
- **`asignacionOptimaPar`** evalúa las asignaciones candidatas en dos mitades,
  calcula el mínimo de cada una en paralelo y compara ambos resultados al final.

En la versión paralela se cuidó que el resultado fuera equivalente al secuencial
para los mismos datos de entrada. En particular, `generarAsignacionesPar` produce
el mismo conjunto de asignaciones que `generarAsignaciones`, y `asignacionOptimaPar`
devuelve el mismo costo óptimo que `asignacionOptima` cuando se evalúan sobre la
misma instancia.

---

## Resultados experimentales

### Generación de datos

Para comparar de forma justa las versiones secuencial y paralela, cada instancia
debe generarse una sola vez y reutilizarse en ambas mediciones. Eso evita comparar
dos problemas distintos.

Se recomienda medir, para cada par $(n,m)$, las siguientes funciones:

- `choques` vs `choquesPar`
- `desperdicio` vs `desperdicioPar`
- `movilidad` vs `movilidadPar`
- `generarAsignaciones` vs `generarAsignacionesPar`
- `asignacionOptima` vs `asignacionOptimaPar`

Para reducir el ruido de la JVM, conviene hacer calentamiento previo y luego tomar
varias mediciones por instancia. La biblioteca `org.scalameter` ya está incluida en
el proyecto y es la opción recomendada para obtener tiempos comparables.

Para el caso de `choques`, `desperdicio` y `movilidad`, además de `cursos`, `aulas`
y `distancias`, se debe usar una asignación fija `a` para medir ambas versiones con
exactamente la misma entrada.

### Tamaño de las instancias

El espacio de búsqueda crece muy rápido porque `generarAsignaciones(n,m)` produce
$m^n$ asignaciones. Por eso, para mantener el análisis tratable, se recomienda usar
instancias pequeñas y medianas como las sugeridas en la plantilla del informe.
Las siguientes combinaciones son razonables para el análisis comparativo:

| Cursos $n$ | Aulas $m$ | Secuencial (ms) | Paralela (ms) | Aceleración (%) |
|:----------:|:---------:|:--------------:|:-------------:|:---------------:|
| 4          | 3         | 1.878          | 0.687         | 63.42           |
| 6          | 4         | 17.453         | 6.113         | 64.97           |
| 7          | 5         | 78.442         | 42.180        | 46.23           |
| 8          | 5         | 433.994        | 255.349       | 41.16           |

La aceleración puede calcularse como:

$$
\text{Aceleración}(\%) = \frac{T_{seq} - T_{par}}{T_{seq}} \times 100
$$

### Presentación de resultados

En cada fila de la tabla se debe reportar el tiempo promedio o mediano de varias
ejecuciones. Si una versión paralela resulta más lenta, la aceleración será negativa,
lo cual también es un resultado válido y útil para el análisis.

En general, los resultados deben presentarse por función y por tamaño de instancia,
de modo que sea posible distinguir:

- cuándo el costo de coordinación supera el beneficio del paralelismo;
- cuándo el paralelismo empieza a compensar;
- qué función obtiene el mejor aprovechamiento de varios núcleos.

### Lectura esperada de los resultados

- En instancias pequeñas, el paralelismo suele perder frente a la versión secuencial
  por la sobrecarga de crear tareas, sincronizar resultados y mover datos.
- En instancias más grandes, `asignacionOptimaPar` es la que más oportunidad tiene
  de mejorar, porque el costo de evaluar muchas asignaciones domina el tiempo total.
- `choquesPar`, `desperdicioPar` y `movilidadPar` paralelizan cómputos lineales;
  su beneficio existe, pero suele ser más moderado.
- `generarAsignacionesPar` puede ganar tiempo cuando el espacio de búsqueda crece,
  pero también paga un costo alto por la construcción de vectores intermedios.

---

## Análisis con la ley de Amdahl

La ley de Amdahl establece que la aceleración teórica máxima con $p$ procesadores es:

$$ S(p) = \frac{1}{(1-\alpha) + \frac{\alpha}{p}} $$

donde $\alpha$ es la fracción del programa que se puede paralelizar.

En este proyecto, la fracción paralelizable es alta en las funciones de conteo y
en la búsqueda de candidatos, pero nunca llega al 100% porque siempre existe una
parte secuencial: generar la estructura de datos, dividir el trabajo, combinar
resultados y materializar colecciones.

Interpretación por función:

- **`choquesPar`, `desperdicioPar` y `movilidadPar`**: su parte paralela es grande,
  pero cada tarea es relativamente liviana. Por eso, el beneficio aparece sobre todo
  cuando el número de cursos crece.
- **`generarAsignacionesPar`**: el trabajo crece exponencialmente con $m^n$, así que
  el paralelismo ayuda cuando el espacio de búsqueda ya es costoso, pero sigue
  existiendo una sobrecarga importante por la creación de vectores.
- **`asignacionOptimaPar`**: es la mejor candidata para paralelizar porque evalúa
  muchas asignaciones independientes. Aun así, su aceleración real queda limitada por
  la parte secuencial de generar candidatos y por los costos de memoria.

La conclusión esperada es que el paralelismo sí aporta valor, pero solo cuando el
tamaño de la instancia hace que el trabajo útil domine ampliamente la sobrecarga de
coordinación.

---

## Conclusiones de paralelización

La versión paralela mejora el comportamiento del proyecto cuando se trabaja con
instancias suficientemente grandes y cuando hay trabajo independiente para repartir.
En cambio, en instancias pequeñas la sobrecarga de paralelizar puede anular la ganancia.

Por eso, la evaluación comparativa debe hacerse con datos reales, sobre las mismas
entradas y con un criterio de medición consistente. El resultado final no solo debe
mostrar tiempos, sino también explicar por qué una versión gana o pierde frente a la
otra.
