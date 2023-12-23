package di

import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror

data class Dependency (
    val selfElement: Element,
    val dependenciesElements: List<Element>,
) {
    val type: TypeMirror
        get() = selfElement.enclosingElement.asType()

    val name: String
        get() = selfElement.enclosingElement.simpleName.toString()

    val providerClassName: String
        get() = "${name}_DependencyProvider"

    val providerLocalName: String = "${name.lowercase()}DependencyProvider"
}