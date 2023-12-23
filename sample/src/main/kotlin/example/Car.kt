package example

import annotations.DIElement
import annotations.DIGraph

class Car @DIElement constructor(
    val engine: Engine,
    val wheel: Wheel,
) {
    var type = "Type 1"
}

class Engine @DIElement constructor()

class Wheel @DIElement constructor()

class Garage @DIElement constructor(
    val car: Car
)


@DIGraph
interface CarGraph {
    fun getCar(): Car
    fun getGarage(): Garage
    fun getEngine(): Engine
}