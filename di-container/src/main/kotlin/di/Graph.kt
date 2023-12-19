package di

import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror

data class Graph(
    val selfElement: Element,
    val functionsElements: List<Function>,
    val dependencies: List<Dependency>,
) {

    val type: TypeMirror
        get() = selfElement.asType()

    val name: String
        get() = selfElement.simpleName.toString()
}

data class Function(
    val selfElement: Element,
    val outputElement: TypeMirror,
) {
}