package proyecto

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import AsignacionAulas._

@RunWith(classOf[JUnitRunner])
class AsignacionAulasTest extends AnyFunSuite {

  // Ejemplo 1 del enunciado
  val c1: Cursos    = Vector(("M01", 4, 8, 25), ("M02", 6, 10, 30), ("M03", 12, 16, 20))
  val a1: Aulas     = Vector(("E101", 30), ("E102", 40))
  val d1: Distancias = Vector(Vector(0, 3), Vector(3, 0))
  val w: Pesos      = (1000, 100, 1, 2)

  // solapan
  test("solapan: M01[4,8) y M02[6,10) se solapan") {
    assert(solapan(("M01", 4, 8, 25), ("M02", 6, 10, 30)))
  }

  test("solapan: M01[4,8) y M03[12,16) no se solapan") {
    assert(!solapan(("M01", 4, 8, 25), ("M03", 12, 16, 20)))
  }

  test("solapan: cursos adyacentes [0,4) y [4,8) no se solapan") {
    assert(!solapan(("A", 0, 4, 10), ("B", 4, 8, 10)))
  }

  test("solapan: Cursos que se traslapan parcialmente") {
    val c1 = ("M01", 4, 8, 25)
    val c2 = ("M02", 6, 10, 30)

    assert(solapan(c1, c2))
  }

  test("solapan: Cursos consecutivos no se traslapan") {
    val c1 = ("M01", 4, 8, 25)
    val c2 = ("M03", 8, 12, 20)

    assert(!solapan(c1, c2))
  }

  test("solapan: Un curso contenido completamente dentro de otro") {
    val c1 = ("M01", 4, 12, 25)
    val c2 = ("M02", 6, 8, 20)

    assert(solapan(c1, c2))
  }

  test("solapan: ursos idénticos se traslapan") {
    val c1 = ("M01", 4, 8, 25)
    val c2 = ("M02", 4, 8, 30)

    assert(solapan(c1, c2))
  }

  test("solapan: Cursos totalmente separados no se traslapan") {
    val c1 = ("M01", 0, 4, 25)
    val c2 = ("M02", 10, 14, 30)

    assert(!solapan(c1, c2))
  }
  // choques
  test("choques: asignacion [0,0,1] tiene 1 choque (M01 y M02 en E101)") {
    assert(choques(c1, Vector(0, 0, 1)) == 1)
  }

  test("choques: asignacion [0,1,0] no tiene choques") {
    assert(choques(c1, Vector(0, 1, 0)) == 0)
  }

  test("Un choque") {
    val cursos = Vector(
      ("M01",4,8,25),
      ("M02",6,10,30),
      ("M03",12,16,20)
    )

    val a = Vector(0,0,1)

    assert(choques(cursos,a) == 1)
  }

  test("Sin choques usando aulas diferentes") {
    val cursos = Vector(
      ("M01",4,8,25),
      ("M02",6,10,30)
    )

    val a = Vector(0,1)

    assert(choques(cursos,a) == 0)
  }

  test("Cursos no solapados en la misma aula") {
    val cursos = Vector(
      ("M01",0,4,25),
      ("M02",4,8,30)
    )

    val a = Vector(0,0)

    assert(choques(cursos,a) == 0)
  }

  test("Tres cursos solapados en la misma aula") {
    val cursos = Vector(
      ("C1",0,10,20),
      ("C2",2,8,20),
      ("C3",4,12,20)
    )

    val a = Vector(0,0,0)

    assert(choques(cursos,a) == 3)
  }

  test("Cursos sin asignar no generan choques") {
    val cursos = Vector(
      ("C1",0,10,20),
      ("C2",2,8,20)
    )

    val a = Vector(-1,-1)

    assert(choques(cursos,a) == 0)
  }

  // capacidadFallida
  test("capacidadFallida: asignacion [0,0,1] no falla capacidad") {
    assert(capacidadFallida(c1, a1, Vector(0, 0, 1)) == 0)
  }

  test("Ninguna falla") {
    val cursos = Vector(
      ("C1",0,4,20),
      ("C2",4,8,30)
    )

    val aulas = Vector(
      ("A1",40),
      ("A2",50)
    )

    val a = Vector(0,1)

    assert(capacidadFallida(cursos,aulas,a) == 0)
  }

  test("Una falla") {
    val cursos = Vector(
      ("C1",0,4,60)
    )

    val aulas = Vector(
      ("A1",40)
    )

    val a = Vector(0)

    assert(capacidadFallida(cursos,aulas,a) == 1)
  }

  test("Dos fallas") {
    val cursos = Vector(
      ("C1",0,4,60),
      ("C2",4,8,70)
    )

    val aulas = Vector(
      ("A1",40)
    )

    val a = Vector(0,0)

    assert(capacidadFallida(cursos,aulas,a) == 2)
  }
  // desperdicio
  test("desperdicio: asignacion [0,0,1] tiene desperdicio 25") {
    // E101(30)-M01(25)=5, E101(30)-M02(30)=0, E102(40)-M03(20)=20 → 25
    assert(desperdicio(c1, a1, Vector(0, 0, 1)) == 25)
  }

  test("desperdicio: asignacion [0,1,0] tiene desperdicio 25") {
    // E101(30)-M01(25)=5, E102(40)-M02(30)=10, E101(30)-M03(20)=10 → 25
    assert(desperdicio(c1, a1, Vector(0, 1, 0)) == 25)
  }

  test("Desperdicio simple") {
    val cursos = Vector(
      ("C1",0,4,20)
    )

    val aulas = Vector(
      ("A1",30)
    )

    val a = Vector(0)

    assert(desperdicio(cursos,aulas,a) == 10)
  }

  test("Desperdicio cero cuando no cabe") {
    val cursos = Vector(
      ("C1",0,4,40)
    )

    val aulas = Vector(
      ("A1",30)
    )

    val a = Vector(0)

    assert(desperdicio(cursos,aulas,a) == 0)
  }

  test("Desperdicio acumulado") {
    val cursos = Vector(
      ("C1",0,4,20),
      ("C2",4,8,25),
      ("C3",8,12,15)
    )

    val aulas = Vector(
      ("A1",30),
      ("A2",40)
    )

    val a = Vector(0,1,0)

    assert(desperdicio(cursos,aulas,a) == 10 + 15 + 15)
  }
  // costoAsignacion
  test("costoAsignacion: asignacion [0,0,1] cuesta 1031") {
    assert(costoAsignacion(c1, a1, d1, Vector(0, 0, 1), w) == 1031)
  }

  test("costoAsignacion: asignacion [0,1,0] cuesta 37") {
    assert(costoAsignacion(c1, a1, d1, Vector(0, 1, 0), w) == 37)
  }

  // Movilidad
  test("Movilidad ejemplo 1") {

    val cursos = Vector(
      ("M01",4,8,25),
      ("M02",6,10,30),
      ("M03",12,16,20)
    )

    val aulas = Vector(
      ("E101",30),
      ("E102",40)
    )

    val d = Vector(
      Vector(0,3),
      Vector(3,0)
    )

    val a = Vector(0,1,0)

    assert(movilidad(cursos,aulas,d,a) == 6)
  }

  test("Un curso tiene movilidad cero") {

    val cursos = Vector(
      ("C1",0,4,20)
    )

    val aulas = Vector(
      ("A1",30)
    )

    val d = Vector(
      Vector(0)
    )

    val a = Vector(0)

    assert(movilidad(cursos,aulas,d,a) == 0)
  }

  test("Misma aula genera movilidad cero") {

    val cursos = Vector(
      ("C1",0,4,20),
      ("C2",4,8,20),
      ("C3",8,12,20)
    )

    val aulas = Vector(
      ("A1",40)
    )

    val d = Vector(
      Vector(0)
    )

    val a = Vector(0,0,0)

    assert(movilidad(cursos,aulas,d,a) == 0)
  }

  test("Dos desplazamientos") {

    val cursos = Vector(
      ("C1",0,4,20),
      ("C2",4,8,20),
      ("C3",8,12,20)
    )

    val aulas = Vector(
      ("A1",40),
      ("A2",40)
    )

    val d = Vector(
      Vector(0,5),
      Vector(5,0)
    )

    val a = Vector(0,1,0)

    assert(movilidad(cursos,aulas,d,a) == 10)
  }

  test("Debe ordenar por hora de inicio") {

    val cursos = Vector(
      ("C1",8,12,20),
      ("C2",0,4,20),
      ("C3",4,8,20)
    )

    val aulas = Vector(
      ("A1",40),
      ("A2",40)
    )

    val d = Vector(
      Vector(0,2),
      Vector(2,0)
    )

    val a = Vector(0,0,1)

    // Orden real:
    // C2 -> C3 -> C1
    // 0 -> 1 -> 0
    // 2 + 2 = 4

    assert(movilidad(cursos,aulas,d,a) == 4)
  }

  //CostoAsignacion

  test("Costo ejemplo 1") {

    val cursos = Vector(
      ("M01",4,8,25),
      ("M02",6,10,30),
      ("M03",12,16,20)
    )

    val aulas = Vector(
      ("E101",30),
      ("E102",40)
    )

    val d = Vector(
      Vector(0,3),
      Vector(3,0)
    )

    val pesos = (1000,100,1,2)

    val a = Vector(0,0,1)

    assert(
      costoAsignacion(cursos,aulas,d,a,pesos) == 1031
    )
  }

  test("Costo ejemplo 2") {

    val cursos = Vector(
      ("F01",0,4,40),
      ("F02",4,8,25),
      ("F03",8,12,50),
      ("F04",12,16,15)
    )

    val aulas = Vector(
      ("S201",45),
      ("S202",30)
    )

    val d = Vector(
      Vector(0,5),
      Vector(5,0)
    )

    val pesos = (1000,100,1,2)

    val a = Vector(0,1,0,1)

    assert(
      costoAsignacion(cursos,aulas,d,a,pesos) == 155
    )
  }

  test("Asignacion sin penalizaciones") {

    val cursos = Vector(
      ("C1",0,4,20)
    )

    val aulas = Vector(
      ("A1",20)
    )

    val d = Vector(
      Vector(0)
    )

    val pesos = (1000,100,1,2)

    val a = Vector(0)

    assert(
      costoAsignacion(cursos,aulas,d,a,pesos) == 0
    )
  }

  test("Solo penalizacion por capacidad") {

    val cursos = Vector(
      ("C1",0,4,50)
    )

    val aulas = Vector(
      ("A1",30)
    )

    val d = Vector(
      Vector(0)
    )

    val pesos = (1000,100,1,2)

    val a = Vector(0)

    assert(
      costoAsignacion(cursos,aulas,d,a,pesos) == 100
    )
  }

  test("Solo desperdicio") {

    val cursos = Vector(
      ("C1",0,4,20)
    )

    val aulas = Vector(
      ("A1",30)
    )

    val d = Vector(
      Vector(0)
    )

    val pesos = (1000,100,1,2)

    val a = Vector(0)

    assert(
      costoAsignacion(cursos,aulas,d,a,pesos) == 10
    )
  }
  // generarAsignaciones
  test("generarAsignaciones: 2 cursos y 2 aulas produce 4 asignaciones") {
    assert(generarAsignaciones(2, 2).length == 4)
  }

  test("generarAsignaciones: 3 cursos y 3 aulas produce 27 asignaciones") {
    assert(generarAsignaciones(3, 3).length == 27)
  }
  test("Generar asignaciones sin cursos") {

    val resultado =
      generarAsignaciones(0,3)

    assert(
      resultado == Vector(Vector())
    )
  }

  test("Un curso dos aulas") {

    val resultado =
      generarAsignaciones(1,2)

    assert(
      resultado ==
        Vector(
          Vector(0),
          Vector(1)
        )
    )
  }

  test("Dos cursos dos aulas") {

    val resultado =
      generarAsignaciones(2,2)

    assert(
      resultado.length == 4
    )
  }

  test("Tres cursos dos aulas genera ocho asignaciones") {

    assert(
      generarAsignaciones(3,2).length == 8
    )
  }

  test("Asignaciones correctas para n=2 m=2") {

    val esperado =
      Vector(
        Vector(0,0),
        Vector(0,1),
        Vector(1,0),
        Vector(1,1)
      )

    assert(
      generarAsignaciones(2,2) == esperado
    )
  }
  // asignacionOptima
  test("asignacionOptima: el costo de la optima no supera el de [0,1,0] (37)") {
    val (_, costo) = asignacionOptima(c1, a1, d1, w)
    assert(costo <= 37)
  }
}
