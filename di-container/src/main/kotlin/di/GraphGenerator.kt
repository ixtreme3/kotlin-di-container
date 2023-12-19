package di

import com.squareup.kotlinpoet.*
import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror

object GraphGenerator {

    fun generateGraphClass(graph: Graph): TypeSpec {
        println("graph type = ${graph.selfElement.enclosingElement.simpleName}")
//        return TypeSpec.interfaceBuilder("${graph.name}_Generated")
//            .addSuperinterface(graph.type.asTypeName())
//            .build()
        val graphClassBuilder = TypeSpec.classBuilder("${graph.name}_Graph")
            .addSuperinterface(graph.selfElement.asType().asTypeName())
            .introduceProviderParameters(graph)

        graph.functionsElements.forEach { function ->
            val implProvider = graph.getDependencyForType(function.outputElement)

            graphClassBuilder.addFunction(
                FunSpec.builder(function.selfElement.simpleName.toString())
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(function.outputElement.asTypeName())
                    .addStatement("return ${implProvider.providerLocalName}.get()")
                    .build()
            )
        }

        return graphClassBuilder.build()
    }

    private fun TypeSpec.Builder.introduceProviderParameters(graph: Graph): TypeSpec.Builder {
        val noArgsDeps = graph.dependencies.filter { it.dependenciesElements.isEmpty() }
        val argsDeps = graph.dependencies - noArgsDeps.toSet()

        //TODO: продумать алгоритм инициализации провайдеров

        noArgsDeps.forEach { this.introduceProviderParameter(it, graph) }

        argsDeps.forEach { this.introduceProviderParameter(it, graph) }

        return this
    }

    private fun TypeSpec.Builder.introduceProviderParameter(dependency: Dependency, graph: Graph): TypeSpec.Builder {
        val providerLocalName = dependency.providerLocalName
        val providerClassName = ClassName(PROVIDERS_PACKAGE_NAME, dependency.providerClassName)
        val argNames = dependency.dependenciesElements.map { graph.getDependencyForType(it.asType()) }.map { it.providerLocalName }

        val propertySpec = PropertySpec.builder(providerLocalName, providerClassName)
            .addModifiers(KModifier.PRIVATE)
            .initializer("%T(${argNames.joinToString(separator = ", ") { "${it}.get()" } })", providerClassName)
            .build()
        return this.addProperty(propertySpec)
    }

    private fun createArgsString(dependencies: List<Element>): String {
        return dependencies.joinToString(separator = ", ") { it.simpleName }
    }

    private fun Graph.getDependencyForType(type: TypeMirror): Dependency {
        return dependencies
            .map { it.type.asTypeName() to it }
            .first { it.first == type.asTypeName() }
            .second
    }
}