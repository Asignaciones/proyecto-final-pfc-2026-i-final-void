package proyecto

import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.util.Locale
import scala.util.Random
import AsignacionAulas._
import AsignacionAulasPar._

object MedicionParalelizacion {

  private case class Caso(n: Int, m: Int)
  private case class Instancia(
      cursos: Cursos,
      aulas: Aulas,
      distancias: Distancias,
      asignacion: Asignacion
  )
  private case class Config(
      warmup: Int = 1,
      reps: Int = 3,
      casos: Vector[Caso] = Vector(Caso(4, 3), Caso(6, 4), Caso(7, 5), Caso(8, 5)),
      output: Option[Path] = None
  )

  private val pesos: Pesos = (1000, 100, 1, 2)

  def main(args: Array[String]): Unit = {
    val config = parseArgs(args.toList, Config())
    val instancias = config.casos.map(caso => caso -> generarInstancia(caso)).toMap
    val reporte = generarReporte(config, instancias)

    config.output.foreach { path =>
      Files.writeString(
        path,
        reporte,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE
      )
    }

    println(reporte)
  }

  private def parseArgs(args: List[String], config: Config): Config = args match {
    case Nil => validar(config)
    case "--warmup" :: value :: tail =>
      parseArgs(tail, config.copy(warmup = value.toInt))
    case "--reps" :: value :: tail =>
      parseArgs(tail, config.copy(reps = value.toInt))
    case "--cases" :: value :: tail =>
      parseArgs(tail, config.copy(casos = parseCasos(value)))
    case "--out" :: value :: tail =>
      parseArgs(tail, config.copy(output = Some(Paths.get(value))))
    case "--help" :: _ =>
      throw new IllegalArgumentException(uso)
    case flag :: _ =>
      throw new IllegalArgumentException(s"Opcion no reconocida: $flag\n\n$uso")
  }

  private def validar(config: Config): Config = {
    require(config.warmup >= 0, "El calentamiento debe ser mayor o igual a 0")
    require(config.reps > 0, "La cantidad de repeticiones debe ser mayor a 0")
    require(config.casos.nonEmpty, "Debe haber al menos un caso de prueba")
    config
  }

  private def parseCasos(texto: String): Vector[Caso] =
    texto
      .split(",")
      .toVector
      .map(_.trim)
      .filter(_.nonEmpty)
      .map { caso =>
        caso.split("[xX]").toList match {
          case n :: m :: Nil => Caso(n.toInt, m.toInt)
          case _ => throw new IllegalArgumentException(s"Caso invalido: $caso")
        }
      }

  private def generarInstancia(caso: Caso): Instancia = {
    val random = new Random(caso.n.toLong * 1000L + caso.m.toLong)

    val cursos =
      Vector.tabulate(caso.n) { i =>
        val inicio = random.nextInt(24)
        val duracion = random.nextInt(4) + 2
        ("C" + i, inicio, inicio + duracion, random.nextInt(46) + 5)
      }

    val aulas =
      Vector.tabulate(caso.m)(j =>
        ("E" + j, random.nextInt(46) + 15)
      )

    val base =
      Vector.fill(caso.m, caso.m)(random.nextInt(caso.m * 2) + 1)

    val distancias =
      Vector.tabulate(caso.m, caso.m) { (i, j) =>
        if (i == j) 0
        else if (i < j) base(i)(j)
        else base(j)(i)
      }

    val asignacion =
      if (caso.m == 0)
        Vector.fill(caso.n)(-1)
      else
        Vector.tabulate(caso.n)(i => i % caso.m)

    Instancia(cursos, aulas, distancias, asignacion)
  }

  private def medirMs(reps: Int, warmup: Int)(body: => Any): Double = {
    def calentar(n: Int): Unit =
      if (n > 0) {
        body
        calentar(n - 1)
      }

    def medir(n: Int, acumulado: Long, firma: Int): (Long, Int) =
      if (n <= 0)
        (acumulado, firma)
      else {
        val inicio = System.nanoTime()
        val resultado = body
        val fin = System.nanoTime()
        medir(n - 1, acumulado + (fin - inicio), firma ^ resultado.hashCode())
      }

    calentar(warmup)
    val (totalNs, _) = medir(reps, 0L, 0)
    totalNs.toDouble / reps.toDouble / 1000000.0
  }

  private def comparar[A](warmup: Int, reps: Int)(seq: => A, par: => A): (Double, Double) =
    (
      medirMs(reps, warmup)(seq),
      medirMs(reps, warmup)(par)
    )

  private def aceleracion(seqMs: Double, parMs: Double): Double =
    if (seqMs == 0.0) 0.0
    else ((seqMs - parMs) / seqMs) * 100.0

  private def fila(caso: Caso, seqMs: Double, parMs: Double): String =
    String.format(
      Locale.US,
      "| %d | %d | %.3f | %.3f | %.2f |",
      Int.box(caso.n),
      Int.box(caso.m),
      Double.box(seqMs),
      Double.box(parMs),
      Double.box(aceleracion(seqMs, parMs))
    )

  private def seccion(
      titulo: String,
      filas: Vector[(Caso, Double, Double)]
  ): String = {
    val lineas =
      Vector(
        s"## $titulo",
        "",
        "| n | m | Secuencial (ms) | Paralela (ms) | Aceleracion (%) |",
        "|---:|---:|---:|---:|---:|"
      ) ++ filas.map { case (caso, seqMs, parMs) =>
        fila(caso, seqMs, parMs)
      } ++ Vector("")

    lineas.mkString("\n")
  }

  private def generarReporte(config: Config, instancias: Map[Caso, Instancia]): String = {
    val casosTexto = config.casos.map(caso => s"${caso.n}x${caso.m}").mkString(", ")

    val medicionesChoques = config.casos.map { caso =>
      val instancia = instancias(caso)
      val (seqMs, parMs) =
        comparar(config.warmup, config.reps)(
          choques(instancia.cursos, instancia.asignacion),
          choquesPar(instancia.cursos, instancia.asignacion)
        )
      (caso, seqMs, parMs)
    }

    val medicionesDesperdicio = config.casos.map { caso =>
      val instancia = instancias(caso)
      val (seqMs, parMs) =
        comparar(config.warmup, config.reps)(
          desperdicio(instancia.cursos, instancia.aulas, instancia.asignacion),
          desperdicioPar(instancia.cursos, instancia.aulas, instancia.asignacion)
        )
      (caso, seqMs, parMs)
    }

    val medicionesMovilidad = config.casos.map { caso =>
      val instancia = instancias(caso)
      val (seqMs, parMs) =
        comparar(config.warmup, config.reps)(
          movilidad(instancia.cursos, instancia.aulas, instancia.distancias, instancia.asignacion),
          movilidadPar(instancia.cursos, instancia.aulas, instancia.distancias, instancia.asignacion)
        )
      (caso, seqMs, parMs)
    }

    val medicionesGenerar = config.casos.map { caso =>
      val (seqMs, parMs) =
        comparar(config.warmup, config.reps)(
          generarAsignaciones(caso.n, caso.m),
          generarAsignacionesPar(caso.n, caso.m)
        )
      (caso, seqMs, parMs)
    }

    val medicionesOptima = config.casos.map { caso =>
      val instancia = instancias(caso)
      val (seqMs, parMs) =
        comparar(config.warmup, config.reps)(
          asignacionOptima(instancia.cursos, instancia.aulas, instancia.distancias, pesos),
          asignacionOptimaPar(instancia.cursos, instancia.aulas, instancia.distancias, pesos)
        )
      (caso, seqMs, parMs)
    }

    Vector(
      "# Mediciones de paralelizacion",
      "",
      s"Instancias evaluadas: $casosTexto.",
      s"Calentamiento: ${config.warmup} | Repeticiones: ${config.reps}.",
      if (config.output.isDefined) s"Salida: ${config.output.get}" else "Salida: stdout.",
      "",
      seccion("choques vs choquesPar", medicionesChoques),
      seccion("desperdicio vs desperdicioPar", medicionesDesperdicio),
      seccion("movilidad vs movilidadPar", medicionesMovilidad),
      seccion("generarAsignaciones vs generarAsignacionesPar", medicionesGenerar),
      seccion("asignacionOptima vs asignacionOptimaPar", medicionesOptima)
    ).mkString("\n")
  }

  private val uso: String =
    """Uso:
      |  ./gradlew run --args="bench [opciones]"
      |
      |Opciones:
      |  --warmup N   cantidad de ejecuciones de calentamiento (default: 1)
      |  --reps N     cantidad de mediciones por caso (default: 3)
      |  --cases LIST lista de casos nXm separados por coma (default: 4x3,6x4,7x5,8x5)
      |  --out PATH   escribe el reporte Markdown en PATH
      |""".stripMargin
}
