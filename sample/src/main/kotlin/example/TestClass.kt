package example

import graphs.CarGraph_Graph

fun main() {
    val graph: CarGraph = CarGraph_Graph()
    val car: Car = graph.getCar()
    val garage: Garage = graph.getGarage()

    println(graph.getEngine())

    println(car)
    println(car.engine)
    println(car.wheel)

    car.type = "Supercar"
    println(car.type)
}
