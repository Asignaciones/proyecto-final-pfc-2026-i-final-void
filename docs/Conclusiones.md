# Conclusiones

## Integrantes del grupo

| Nombre completo            | Codigo   | Correo electronico                    |
|----------------------------|----------|---------------------------------------|
| Kevin Alejandro Marulanda  | 2380697  | Kevin.marulanda@correounivalle.edu.co |

## Conclusiones del proyecto

### 1. Programacion funcional

La principal ventaja de resolver el problema con recursion, funciones de alto
orden e inmutabilidad es que la solucion queda muy cerca de la especificacion.
En `AsignacionAulas.scala` y `AsignacionAulasPar.scala` cada funcion expresa de
forma directa el calculo que debe hacer: contar choques, acumular desperdicio,
construir asignaciones o seleccionar el costo minimo.

La dificultad principal fue controlar el crecimiento del espacio de busqueda y
manejar correctamente los casos base. En especial, `generarAsignaciones` y
`asignacionOptima` obligan a pensar en terminos de construccion recursiva y no
en terminos de iteracion. Eso hace la correccion mas clara, pero exige mas
cuidado para no introducir errores con colecciones vacias o con estados
intermedios mal definidos.

### 2. Correccion

La correccion se argumento en `docs/Correccion.md` usando razonamiento formal y
no solo pruebas. Para las funciones recursivas se aplico induccion estructural
o induccion sobre el indice de recorrido. Para las funciones que solo
componen resultados, como `costoAsignacion`, la correccion se obtuvo por
composicion directa con las propiedades ya demostradas.

En las versiones paralelas, la idea clave fue que dividir el trabajo en mitades
no cambia el resultado matematico. Si la operacion de combinacion es una suma,
la asociatividad garantiza el mismo total. Si la operacion es un minimo,
comparar los minimos parciales produce el mismo minimo global. Las pruebas
unitarias ayudaron a validar la implementacion, pero el soporte principal de la
correccion fue el argumento formal.

### 3. Paralelismo

Paralelizar fue util cuando el costo de trabajo supero claramente el costo de
coordinar tareas. Eso se ve mejor en `asignacionOptimaPar`, porque evaluar
todas las asignaciones candidatas es costoso y cada candidata es independiente.
Tambien puede verse beneficio en `generarAsignacionesPar`, aunque ahi el costo
de construir colecciones intermedias sigue siendo alto.

En cambio, `choquesPar`, `desperdicioPar` y `movilidadPar` tienen un trabajo
por particion relativamente pequeno, asi que en instancias pequenas la
sobrecarga del paralelismo puede superar la ganancia. La conclusion practica es
que la version paralela no siempre es mas rapida; solo empieza a compensar
cuando el tamano del problema hace que haya suficiente trabajo util por
repartir.

### 4. Aprendizajes

Los conceptos mas utiles fueron recursion, funciones de alto orden, patrones de
induccion, colecciones inmutables y descomposicion de problemas en
subproblemas. Tambien fue importante entender que una implementacion correcta
no necesariamente es la mas rapida, por lo que el diseno debio separarse en dos
frentes: correccion formal y medicion de rendimiento.

Si volviera a empezar, dejaria desde el principio una estrategia de evaluacion
deterministica y un harness de benchmarking separado de la logica del problema.
Tambien intentaria aislar mas pronto la generacion de candidatos, la evaluacion
del costo y la busqueda del minimo, para facilitar tanto la demostracion de
correctitud como la version paralela.

## Cierre

El proyecto cumplio el objetivo de resolver el problema usando programacion
funcional y concurrente en Scala. La solucion secuencial sirvio como base de
correccion, y la version paralela se construyo sin cambiar la semantica del
resultado. El aprendizaje mas relevante es que la claridad matematica de la
solucion facilita tanto la verificacion como la extension hacia paralelismo y
pruebas.
