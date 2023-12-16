package example

import annotations.DIElement

class Car @DIElement constructor(
    engine: Engine,
    wheel: Wheel,
) {
    val type = "Type 1"
}

class Engine()

class Wheel()