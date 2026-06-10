package proyecto

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import AsignacionAulas._
import AsignacionAulasPar._

@RunWith(classOf[JUnitRunner])
class AsignacionAulasParTest extends AnyFunSuite {

  val c1: Cursos    = Vector(("M01", 4, 8, 25), ("M02", 6, 10, 30), ("M03", 12, 16, 20))
  val a1: Aulas     = Vector(("E101", 30), ("E102", 40))
  val d1: Distancias = Vector(Vector(0, 3), Vector(3, 0))
  val w: Pesos      = (1000, 100, 1, 2)

  test("choquesPar: asignacion [0,0,1] tiene 1 choque") {
    assert(choquesPar(c1, Vector(0, 0, 1)) == 1)
  }

  test("choquesPar: asignacion [0,1,0] no tiene choques") {
    assert(choquesPar(c1, Vector(0, 1, 0)) == 0)
  }

  test("desperdicioPar: asignacion [0,0,1] tiene desperdicio 25") {
    assert(desperdicioPar(c1, a1, Vector(0, 0, 1)) == 25)
  }

  test("movilidadPar: asignacion [0,0,1] tiene movilidad 3") {
    assert(movilidadPar(c1, a1, d1, Vector(0, 0, 1)) == 3)
  }

  test("generarAsignacionesPar: 2 cursos y 2 aulas produce 4 asignaciones") {
    assert(generarAsignacionesPar(2, 2).length == 4)
  }
  test("n=0") {

    assert(
      generarAsignacionesPar(0,3) ==
        generarAsignaciones(0,3)
    )
  }

  test("n=1 m=2") {

    assert(
      generarAsignacionesPar(1,2) ==
        generarAsignaciones(1,2)
    )
  }

  test("n=2 m=2") {

    assert(
      generarAsignacionesPar(2,2) ==
        generarAsignaciones(2,2)
    )
  }

  test("Cantidad correcta") {

    assert(
      generarAsignacionesPar(3,2).length == 8
    )
  }

  test("Mismas asignaciones que la secuencial") {

    assert(
      generarAsignacionesPar(4,3).toSet ==
        generarAsignaciones(4,3).toSet
    )
  }

  test("asignacionOptimaPar: el costo de la optima no supera el de [0,1,0] (37)") {
    val (_, costo) = asignacionOptimaPar(c1, a1, d1, w)
    assert(costo <= 37)
  }

  test("asignacionOptimaPar: rechaza el caso sin aulas cuando hay cursos") {
    intercept[IllegalArgumentException] {
      asignacionOptimaPar(c1, Vector.empty, d1, w)
    }
  }

}
