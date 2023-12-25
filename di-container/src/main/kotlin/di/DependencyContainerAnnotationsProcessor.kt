package di

import annotations.DIElement
import annotations.DIGraph
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

const val PROVIDERS_PACKAGE_NAME = "providers"
const val GRAPHS_PACKAGE_NAME = "graphs"

@SupportedAnnotationTypes("annotations.DIElement","annotations.DIGraph")
@AutoService(Processor::class)
@SupportedOptions("kapt.kotlin.generated")
class DependencyContainerAnnotationsProcessor : AbstractProcessor() {

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        println("start")
        val dependencies = roundEnv.getElementsAnnotatedWith(DIElement::class.java)
            .filter { it.kind == ElementKind.CONSTRUCTOR }
            .map { buildDependencyMeta(it) }

        println("process:" + dependencies.map { "${it.selfElement.simpleName} + ${it.dependenciesElements.forEach { a -> a.simpleName }}" })

        generateDependencyProviders(dependencies)

        return graphProcess(roundEnv, dependencies)
    }

    private fun graphProcess(roundEnv: RoundEnvironment, dependencies: List<Dependency>) : Boolean{
        println("startGraphProcessing")
        val graphs = roundEnv.getElementsAnnotatedWith(DIGraph::class.java)
            .filter { it.kind == ElementKind.INTERFACE }
            .map { buildGraphMeta(it, dependencies) }
        println("graph process:" + graphs.map { "${it.selfElement.simpleName}" })
        generateGraphs(graphs)
        return true
    }

    private fun buildDependencyMeta(typeElement: Element): Dependency {
        val dependencies = (typeElement as ExecutableElement).parameters

        return Dependency(typeElement, dependencies)
    }

    private fun buildGraphMeta(typeElement: Element, dependencies: List<Dependency>):Graph {
        val elements = typeElement.enclosedElements
        val functions = mutableListOf<Function>()
        for (i in elements.indices){
            functions.add(Function(elements[i],(elements[i] as ExecutableElement).returnType))
        }
        val result = Graph(typeElement, functions, dependencies)
        println("graph info:$result")

        return result
    }

    private fun generateDependencyProviders(dependencies: List<Dependency>) {
        if (dependencies.isEmpty()) {
            return
        }
        dependencies.forEach {
            val generatedClass = DependencyProviderGenerator.generateDependencyProviderClass(it)
            writeCodeFile(PROVIDERS_PACKAGE_NAME, generatedClass)
        }
    }

    private fun generateGraphs(graphs: List<Graph>) {
        if (graphs.isEmpty()) {
            return
        }
        graphs.forEach {
            val generatedClass = GraphGenerator.generateGraphClass(it)
            writeCodeFile(GRAPHS_PACKAGE_NAME, generatedClass)
        }
    }

    private fun writeCodeFile(packageName: String, generatedClass: TypeSpec) {
        val javaFile = FileSpec.builder(packageName, generatedClass.name ?: "").addType(generatedClass).build()
        val options = processingEnv.options
        val generatedPath = options["kapt.kotlin.generated"]
        val path = generatedPath
            ?.replace("(.*)tmp(/kapt/debug/)kotlinGenerated".toRegex(), "$1generated/source$2")

        path?.let{ p -> javaFile.writeTo(File(p)) }
    }
}