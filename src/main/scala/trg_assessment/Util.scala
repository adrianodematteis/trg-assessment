package trg_assessment

import org.apache.spark.sql.functions.{ col, udf }
import org.apache.spark.sql.types.{ DoubleType, StringType, StructField, StructType }

object Util {
  //street files
  val streetSchema = new StructType()
    .add("crimeId", StringType, nullable = true)
    .add("month", StringType, nullable = true)
    .add("reportedBy", StringType, nullable = true)
    .add("fallsWithin", StringType, nullable = true)
    .add("longitude", DoubleType, nullable = true)
    .add("latitude", DoubleType, nullable = true)
    .add("location", StringType, nullable = true)
    .add("LSOAcode", StringType, nullable = true)
    .add("LSOAname", StringType, nullable = true)
    .add("crimeType", StringType, nullable = true)
    .add("lastOutcomeCategory", StringType, nullable = true)
    .add("context", StringType, nullable = true)

  val extractDistrict = udf(
    (path: String) =>
      path.split('/').last
        .split('.').head
        .split('-')
        .dropRight(1)
        .drop(2)
        .mkString(" "))
  val streetColsOfInterest = "crimeId districtName latitude longitude crimeType lastOutcomeCategory"
    .split(" ").map(col)

  //outcomes files
  val outcomesHeader = "crimeId month reportedBy fallsWithin longitude latitude location LSOAcode	LSOAname outcomeType"

  val outcomesSchema = StructType(
    outcomesHeader
      .split(" ")
      .map {
        case field if field.equals("latitude") || field.equals("longitude") =>
          StructField(field, DoubleType, nullable = true)

        case field =>
          StructField(field, StringType, nullable = true)
      })
}
