# Informe de correccion

## Integrantes del grupo

| Nombre completo            | Codigo   | Correo electronico                    |
|----------------------------|----------|---------------------------------------|
| Kevin Alejandro Marulanda  | 2380697  | Kevin.marulanda@correounivalle.edu.co |

---

## 4.1.3. Informe de correccion

La correccion del proyecto se argumenta relacionando cada funcion con su
especificacion matematica. En este informe usamos tres ideas:

- equivalencia directa cuando la implementacion replica exactamente la
  definicion del problema;
- induccion estructural o sobre el indice de recorrido cuando la funcion es
  recursiva;
- descomposicion por mitades cuando la version paralela separa el mismo
  calculo en subproblemas independientes.

Las propiedades fueron contrastadas ademas con las pruebas unitarias del
proyecto, pero la correccion se justifica por el razonamiento formal que sigue.

### 1. `solapan`

La especificacion dice que dos cursos se traslapan si y solo si sus intervalos
de tiempo se intersectan. Para los intervalos semiabiertos
$[ini_1, fin_1)$ y $[ini_2, fin_2)$, eso equivale a:

$$
ini_1 < fin_2 \;\land\; ini_2 < fin_1
$$

La implementacion devuelve exactamente esa expresion, por lo tanto es correcta
por definicion.

### 2. `choques`

Sea $C(i,j)$ el valor devuelto por el auxiliar `contarDesde(i,j)`. Afirmamos
que $C(i,j)$ cuenta exactamente los pares de cursos $(p,q)$ que aun faltan por
revisar a partir de la posicion $(i,j)$, tales que:

- $p < q$;
- los cursos $p$ y $q$ se traslapan;
- $a(p) = a(q) \ge 0$.

La correccion se demuestra por recorrido lexicografico sobre los pares
$(i,j)$.

- Si $i \ge |cursos| - 1$, no existen pares pendientes y el resultado es `0`.
- Si $j \ge |cursos|$, la recursion avanza a la siguiente fila de pares, es
  decir, a `(i + 1, i + 2)`.
- En el caso general, el programa agrega `1` si el par actual cumple la
  condicion de choque y luego continua con el siguiente par `(i, j + 1)`.

Como cada par $(p,q)$ con $p < q$ se visita exactamente una vez y solo aporta
`1` cuando satisface la especificacion, `choques(cursos, a)` devuelve el numero
total de choques.

### 3. `capacidadFallida`

Sea $F(i)$ el valor devuelto por `aux(i)`. La propiedad a demostrar es:

$$
F(i) = \sum_{k=i}^{n-1} f(k)
$$

donde $f(k) = 1$ si el curso $k$ esta asignado a un aula con capacidad menor
que el numero de estudiantes, y $f(k) = 0$ en caso contrario.

La prueba es por induccion sobre $i$.

- Caso base: si $i \ge n$, no quedan cursos por revisar, entonces `aux(i) = 0`.
- Paso inductivo: para $i < n$, la funcion calcula si el curso $i$ falla por
  capacidad y suma ese valor con `aux(i + 1)`. Por hipotesis inductiva, el
  segundo termino ya coincide con la suma del resto de los cursos, asi que el
  resultado total coincide con la especificacion.

Por lo tanto, `capacidadFallida` cuenta correctamente los cursos mal
asignados por capacidad.

### 4. `desperdicio`

Sea $D(i)$ el valor devuelto por `aux(i)`. La propiedad a demostrar es:

$$
D(i) = \sum_{k=i}^{n-1} d(k)
$$

donde $a(k)$ es el indice del aula asignada al curso $k$ y:

$$
d(k) =
\begin{cases}
\operator{cap}(a(k)) - \operator{est}(c_k), & \text{si el curso } k \text{ esta asignado y la capacidad alcanza} \\
0, & \text{en otro caso}
\end{cases}
$$

La prueba es la misma estructura que en `capacidadFallida`.

- Caso base: si `i >= cursos.length`, no queda ningun curso, por lo que la suma
  es `0`.
- Paso inductivo: para `i < n`, el programa calcula la contribucion del curso
  `i` y la suma con `aux(i + 1)`. Por hipotesis inductiva, `aux(i + 1)` ya
  coincide con el desperdicio del sufijo restante, asi que el total coincide
  con la definicion.

Por lo tanto, `desperdicio` suma exactamente el desperdicio de capacidad de
los cursos que tienen aula suficiente.

### 5. `movilidad`

Sea $i_0, i_1, \dots, i_{k-1}$ la secuencia de cursos asignados ordenados por
hora de inicio. La especificacion pide:

$$
\sum_{t=0}^{k-2} d(a(i_t))(a(i_{t+1}))
$$

La implementacion construye precisamente esa secuencia:

- filtra solo los cursos asignados;
- los ordena por `iniCurso`;
- aplica `sliding(2)` para obtener los pares consecutivos;
- suma las distancias entre aulas de cada par.

Si hay `0` o `1` cursos asignados, la coleccion de pares consecutivos es vacia
y la suma vale `0`, que coincide con la especificacion. Por eso `movilidad`
es correcta.

### 6. `generarAsignaciones`

Sea $G(n,m)$ el resultado de `generarAsignaciones(n,m)`. Queremos demostrar
que $G(n,m)$ contiene exactamente todas las asignaciones completas de $n$
cursos en $m$ aulas, es decir, todos los vectores en $\{0,\dots,m-1\}^n$.

La prueba es por induccion estructural sobre $n$.

- Caso base: si $n = 0$, el unico vector posible es el vacio. La funcion
  devuelve `Vector(Vector())`, que es exactamente la unica asignacion de
  longitud cero.
- Paso inductivo: supongamos correcto `generarAsignaciones(n - 1, m)`. El
  programa toma cada asignacion parcial de longitud $n-1$ y la extiende con
  cada aula $0,\dots,m-1$. Por lo tanto, produce todas y solo las asignaciones
  de longitud $n$.

Ademas, el numero de resultados es $m \cdot m^{n-1} = m^n$. Si $m = 0$ y
$n > 0$, el resultado es vacio, lo cual tambien es correcto porque no existe
ninguna asignacion completa.

### 7. `costoAsignacion`

Esta funcion no introduce una logica nueva: aplica exactamente la formula del
enunciado.

$$
CT = w_{CH} \cdot CH + w_{CF} \cdot CF + w_{DE} \cdot DE + w_{MV} \cdot MV
$$

Como cada uno de los terminos ya fue definido y argumentado correctamente,
`costoAsignacion` tambien es correcto por composicion.

### 8. `asignacionOptima`

Sea $A = generarAsignaciones(n,m)$. Por el punto anterior, $A$ es exactamente
el conjunto de todas las asignaciones posibles. La funcion aplica
`costoAsignacion` a cada elemento de $A$ y luego usa `minBy` para elegir el de
menor costo.

Por lo tanto, si $a^\*$ es el resultado devuelto, entonces:

$$
\forall a \in A,\quad costoAsignacion(a^\*) \le costoAsignacion(a)
$$

Eso coincide con la especificacion de asignacion optima.

El `require(cursos.isEmpty || aulas.nonEmpty, ...)` evita el caso sin aulas
cuando hay cursos, que no tiene solucion valida y haria fallar la busqueda.

### 9. Versiones paralelas

Las funciones paralelas preservan la correccion porque solo cambian la forma en
que se organiza el calculo, no el resultado matematico.

- `choquesPar`, `desperdicioPar` y `movilidadPar` dividen la suma en dos
  mitades disjuntas y luego agregan los resultados. Como la suma es asociativa,
  el valor final no cambia.
- `generarAsignacionesPar` separa las opciones de la primera aula en dos
  rangos disjuntos cuya union es el conjunto completo de aulas. Cada rama usa
  la misma construccion recursiva de la version secuencial, por lo que el
  conjunto generado es el mismo.
- `asignacionOptimaPar` calcula el minimo de cada mitad y luego compara esos
  dos minimos. Como el minimo del conjunto unido es el minimo entre los minimos
  parciales, el resultado final coincide con `asignacionOptima`. La comparacion
  `<=` conserva la preferencia por la mitad izquierda cuando hay empate, que es
  compatible con la enumeracion de candidatos.

### 10. Conclusion

La correccion del proyecto se sostiene porque cada funcion implementa
directamente su especificacion o la preserva por induccion y composicion. Las
versiones paralelas no alteran la semantica: solo reorganizan el trabajo para
ejecutarlo concurrentemente. Con esto, el comportamiento observado en las
pruebas unitarias queda respaldado por argumentos formales.
