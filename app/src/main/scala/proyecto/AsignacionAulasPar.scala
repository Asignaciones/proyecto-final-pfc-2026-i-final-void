package proyecto

import common._
import AsignacionAulas._

object AsignacionAulasPar {

  def choquesPar(cursos: Cursos, a: Asignacion): Int = {

    def contarRango(
                     desde: Int,
                     hasta: Int
                   ): Int = {

      if (desde >= hasta)
        0
      else {

        val choquesActuales =
          ((desde + 1) until cursos.length)
            .count { j =>
              a(desde) >= 0 &&
                a(desde) == a(j) &&
                solapan(cursos(desde), cursos(j))
            }

        choquesActuales +
          contarRango(desde + 1, hasta)
      }
    }

    val mitad = cursos.length / 2

    val (izq, der) =
      parallel(
        contarRango(0, mitad),
        contarRango(mitad, cursos.length)
      )

    izq + der
  }

  def desperdicioPar(cursos: Cursos, aulas: Aulas, a: Asignacion): Int = {

    def desperdicioRango(
                          desde: Int,
                          hasta: Int
                        ): Int = {

      if (desde >= hasta)
        0
      else {

        val aulaAsignada = a(desde)

        val valor =
          if (
            aulaAsignada >= 0 &&
              capAula(aulas(aulaAsignada))
                >= estCurso(cursos(desde))
          )
            capAula(aulas(aulaAsignada)) - estCurso(cursos(desde))
          else
            0

        valor +
          desperdicioRango(desde + 1, hasta)
      }
    }

    val mitad = cursos.length / 2

    val (izq, der) =
      parallel(
        desperdicioRango(0, mitad),
        desperdicioRango(mitad, cursos.length)
      )

    izq + der
  }

  def movilidadPar(cursos: Cursos, aulas: Aulas, d: Distancias,
                   a: Asignacion): Int = {

    val ordenados =
      cursos.indices
        .filter(i => a(i) >= 0)
        .sortBy(i => iniCurso(cursos(i)))

    val pares =
      ordenados.sliding(2).toVector

    def sumarRango(
                    desde: Int,
                    hasta: Int
                  ): Int = {

      if (desde >= hasta)
        0
      else {

        val Vector(i, j) = pares(desde)

        d(a(i))(a(j)) +
          sumarRango(desde + 1, hasta)
      }
    }

    val mitad = pares.length / 2

    val (izq, der) =
      parallel(
        sumarRango(0, mitad),
        sumarRango(mitad, pares.length)
      )

    izq + der
  }

  def generarAsignacionesPar(n: Int, m: Int): Vector[Asignacion] = {

    if (n == 0)
      Vector(Vector())

    else if (m == 1)
      generarAsignaciones(n, m)

    else {

      val mitad = m / 2

      def construir(
                     inicio: Int,
                     fin: Int
                   ): Vector[Asignacion] = {

        (inicio until fin).toVector.flatMap { aula =>

          generarAsignaciones(n - 1, m)
            .map(resto => aula +: resto)

        }
      }

      val (izq, der) =
        parallel(
          construir(0, mitad),
          construir(mitad, m)
        )

      izq ++ der
    }
  }

  def asignacionOptimaPar(cursos: Cursos, aulas: Aulas, d: Distancias,
                          w: Pesos): (Asignacion, Int) = {
    require(
      cursos.isEmpty || aulas.nonEmpty,
      "asignacionOptimaPar requiere al menos un aula cuando hay cursos"
    )

    val asignaciones =
      generarAsignacionesPar(cursos.length, aulas.length)

    def mejorDe(candidatas: Vector[Asignacion]): (Asignacion, Int) = {
      candidatas
        .map(a => (a, costoAsignacion(cursos, aulas, d, a, w)))
        .minBy(_._2)
    }

    if (asignaciones.length <= 1)
      mejorDe(asignaciones)
    else {
      val mitad = asignaciones.length / 2
      val (izq, der) =
        parallel(
          mejorDe(asignaciones.take(mitad)),
          mejorDe(asignaciones.drop(mitad))
        )

      if (izq._2 <= der._2) izq else der
    }
  }
}
