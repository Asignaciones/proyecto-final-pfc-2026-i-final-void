package proyecto

object App {
  def main(args: Array[String]): Unit = {
    args.toList match {
      case "bench" :: resto =>
        MedicionParalelizacion.main(resto.toArray)

      case _ =>
        println("Proyecto Final - Asignacion Optima de Aulas")
        println("Implemente AsignacionAulas y AsignacionAulasPar para comenzar.")
        println("Use 'bench' para generar mediciones en formato Markdown.")
    }
  }
}
