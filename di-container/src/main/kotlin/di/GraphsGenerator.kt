package di

import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

object GraphsGenerator {
    fun generateDependencyProviderClass(graph: Graph): TypeSpec {
        println("graph type = ${graph.selfElement.enclosingElement.simpleName}")
        return TypeSpec.interfaceBuilder("${graph.name}_Generated")
            .addSuperinterface(graph.type.asTypeName())
            .build()
    }
}