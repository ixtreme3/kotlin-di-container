package di

import javax.lang.model.type.TypeMirror

data class Provider(
    val type: TypeMirror,
    val argTypes: List<TypeMirror>,
    val returnType: TypeMirror,
)