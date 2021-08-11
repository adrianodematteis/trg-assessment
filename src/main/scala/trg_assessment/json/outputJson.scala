package trg_assessment.json

package object outputJson {
  final case class Data(crimeId: String, districtName: String, latitude: Double,
    longitude: Double, crimeType: String, lastOutcome: String)
  final case class BaseData(data: List[Data])

  final case class DifferentCrimes(listOfCrimes: List[String])

  final case class CrimesByDistrict(districtName: String, count: BigInt)
  final case class ListCrimesByDistrict(crimesByDistrict: List[CrimesByDistrict])

  final case class CrimesByCrimeType(crimeType: String, count: BigInt)
  final case class ListCrimesByCrimeType(crimesByCrimeType: List[CrimesByCrimeType])

  final case class PathDoesNotExist(error: String)

}