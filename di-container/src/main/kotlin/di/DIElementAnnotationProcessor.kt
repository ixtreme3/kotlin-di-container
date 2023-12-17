package di

import annotations.DIElement
import annotations.DIGraph
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements

@SupportedAnnotationTypes("annotations.DIElement", "annotations.DIGraph")
@AutoService(Processor::class)
@SupportedOptions("kapt.kotlin.generated")
class DIElementAnnotationProcessor : AbstractProcessor() {

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        println("start")
        val dependencies = roundEnv.getElementsAnnotatedWith(DIElement::class.java)
            .filter { it.kind == ElementKind.CONSTRUCTOR }
            .map { buildDependencyMeta(it) }
        println("process:" +dependencies.map { "${it.selfElement.simpleName} + ${it.dependenciesElements.forEach { a -> a.simpleName }}" })
        generateDependencyProviders(dependencies)

        return graphProcess(annotations, roundEnv)
    }
    private fun graphProcess(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment) : Boolean{
        println("startGraphProcessing")
        val graphs = roundEnv.getElementsAnnotatedWith(DIGraph::class.java)
            .filter { it.kind == ElementKind.INTERFACE }
            .map { buildGraphMeta(it) }
        println("graph process:" + graphs.map { "${it.selfElement.simpleName}" })
        generateGraphProviders(graphs)

        return true
    }

    private fun buildDependencyMeta(typeElement: Element): Dependency {
        val variableNames = typeElement.enclosedElements
            .filterIsInstance<VariableElement>()
            .map { it.simpleName.toString() }

        val dependencies = (typeElement as ExecutableElement).parameters

        val result = Dependency(typeElement, dependencies)

        println("dependency info:" + result)

        return result
    }

    private fun buildGraphMeta(typeElement: Element):Graph {

        val elements = typeElement.enclosedElements
        var functions = mutableListOf<Function>()
        for (i in elements.indices){
            functions.add(Function(elements[i],(elements[i] as ExecutableElement).returnType))
        }
        val result = Graph(typeElement, functions)

        println("graph info:"+result)

        return result
    }

    private fun generateDependencyProviders(dependencies: List<Dependency>) {
        if (dependencies.isEmpty()) {
            return
        }
        dependencies.forEach {
            val packageName = packageName(processingEnv.elementUtils, it.selfElement)
            val generatedClass = DependencyProviderGenerator.generateDependencyProviderClass(it)
            val javaFile = FileSpec.builder(packageName, generatedClass.name ?: "").addType(generatedClass).build()
            val options = processingEnv.options
            val generatedPath = options["kapt.kotlin.generated"]
            val path = generatedPath
                ?.replace("(.*)tmp(/kapt/debug/)kotlinGenerated".toRegex(), "$1generated/source$2")
            javaFile.writeTo(File(path, "${javaFile.name}.kt"))
        }
    }

    private fun generateGraphProviders(graphs: List<Graph>) {
        if (graphs.isEmpty()) {
            return
        }
        graphs.forEach {
            val packageName = packageName(processingEnv.elementUtils, it.selfElement)
            val generatedClass = GraphsGenerator.generateDependencyProviderClass(it)
            val javaFile = FileSpec.builder(packageName, generatedClass.name ?: "").addType(generatedClass).build()
            val options = processingEnv.options
            val generatedPath = options["kapt.kotlin.generated"]
            val path = generatedPath
                ?.replace("(.*)tmp(/kapt/debug/)kotlinGenerated".toRegex(), "$1generated/source$2")
            javaFile.writeTo(File(path, "${javaFile.name}.kt"))
        }
    }

    private fun packageName(elementUtils: Elements, typeElement: Element): String {
        val pkg = elementUtils.getPackageOf(typeElement)
        if (pkg.isUnnamed) {
            throw RuntimeException(typeElement.simpleName.toString())
        }
        return pkg.qualifiedName.toString()
    }
}