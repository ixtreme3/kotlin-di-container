package di

import com.squareup.kotlinpoet.TypeSpec

data class GeneratedProvider(
    val code: TypeSpec,
    val meta: Provider,
)