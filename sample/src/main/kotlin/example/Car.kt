package example

import annotations.DIElement
import annotations.DIGraph

class Car @DIElement constructor(
    engine: Engine,
    wheel: Wheel,
) {
    val type = "Type 1"
}

class Engine @DIElement constructor()

class Wheel @DIElement constructor()

@DIGraph
interface CarGraph{
    fun getcar():Car
}