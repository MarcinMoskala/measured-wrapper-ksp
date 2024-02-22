@file:Suppress("UnnecessaryVariable")

package academy.kt

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class MeasuredWrapperGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    private val annotationName = Measured::class.qualifiedName!!

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver
            .getSymbolsWithAnnotation(
                annotationName
            )
            .filterIsInstance<KSFunctionDeclaration>()
            .groupBy { it.closestClassDeclaration() }
            .forEach { (classDeclaration, _) ->
                if (classDeclaration != null) {
                    generateMeasuredClass(classDeclaration)
                }
            }

        return emptyList()
    }

    private fun generateMeasuredClass(
        classElement: KSClassDeclaration
    ) {
        val className = classElement.simpleName.getShortName()
        val measuredName = "Measured$className"
        val measuredPackage = classElement.packageName.asString()
        val publicMethods = classElement
            .getDeclaredFunctions()
            .filter { !it.isConstructor() && it.isPublic() }
            .toList()

        val fileSpec = FileSpec.builder(
            measuredPackage,
            "$measuredName.kt"
        )
            .addType(
                TypeSpec.classBuilder(measuredName)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter(
                                "wrapper",
                                classElement.asType(emptyList())
                                    .toTypeName()
                            )
                            .build()
                    )
                    .addFunction(
                        FunSpec.constructorBuilder()
                            .addParameters(
                                classElement.primaryConstructor!!.parameters
                                    .map { buildInterfaceMethodParameter(it) }
                            )
                            .callThisConstructor(
                                "$className(${
                                    classElement.primaryConstructor!!.parameters.joinToString {
                                        it.name?.getShortName().orEmpty()
                                    }
                                })"
                            )
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder(
                            "wrapper",
                            classElement.asType(emptyList())
                                .toTypeName()
                        ).initializer("wrapper")
                            .build()
                    )
                    .addFunctions(
                        publicMethods
                            .map { buildMethod(className, it) }
                            .toList()
                    )
                    .build()
            )
            .build()

        val dependencies = Dependencies(
            aggregating = false,
            classElement.containingFile!!
        )
        val file = codeGenerator.createNewFile(
            dependencies, measuredPackage, measuredName
        )
        OutputStreamWriter(file, StandardCharsets.UTF_8)
            .use(fileSpec::writeTo)
    }

    private fun buildMethod(
        className: String,
        method: KSFunctionDeclaration
    ): FunSpec {
        val methodName = method.simpleName.getShortName()
        return FunSpec.builder(methodName)
            .addModifiers(
                method.modifiers
                    .mapNotNull { it.toKModifier() }.toList()
            )
            .addParameters(
                method.parameters
                    .map { buildInterfaceMethodParameter(it) }
            )
            .returns(method.returnType!!.toTypeName())
            .addAnnotations(method
                .annotations
                .filter { !isMeasured(it) }
                .map { it.toAnnotationSpec() }
                .toList())
            .addCode(
                if (method.annotations.none { isMeasured(it) })
                    "return wrapper.$methodName(${
                        method.parameters.joinToString {
                            it.name?.getShortName().orEmpty()
                        }
                    })"
                else
                    """
                    val before = System.currentTimeMillis()
                    val value = wrapper.$methodName(${
                        method.parameters.joinToString {
                            it.name?.getShortName().orEmpty()
                        }
                    })
                    val after = System.currentTimeMillis()
                    println("$methodName from $className took ${'$'}{after-before} ms")
                    return value
                    """.trimIndent()
            )
            .build()
    }

    private fun isMeasured(annotation: KSAnnotation) = annotation
        .annotationType
        .resolve()
        .declaration
        .qualifiedName
        ?.asString() == annotationName

    private fun buildInterfaceMethodParameter(
        variableElement: KSValueParameter,
    ): ParameterSpec = ParameterSpec
        .builder(
            variableElement.name!!.getShortName(),
            variableElement.type.toTypeName(),
        )
        .addAnnotations(
            variableElement.annotations
                .map { it.toAnnotationSpec() }.toList()
        )
        .build()
}
