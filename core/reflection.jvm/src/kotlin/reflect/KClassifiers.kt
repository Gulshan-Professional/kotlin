/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("KClassifiers")
package kotlin.reflect

import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.types.*
import kotlin.reflect.jvm.internal.KClassImpl
import kotlin.reflect.jvm.internal.KClassifierImpl
import kotlin.reflect.jvm.internal.KTypeImpl
import kotlin.reflect.jvm.internal.KTypeParameterImpl

/**
 * TODO
 */
fun KClassifier.createType(
        arguments: List<KTypeProjection> = listOf(),
        nullable: Boolean = false,
        annotations: List<Annotation> = listOf()
): KType {
    val descriptor = (this as? KClassifierImpl)?.descriptor
                     ?: throw KotlinReflectionInternalError("Cannot create type for an unsupported classifier: $this (${this.javaClass})")

    val typeConstructor = descriptor.typeConstructor
    val parameters = typeConstructor.parameters
    if (parameters.size != arguments.size) {
        throw IllegalArgumentException("Class declares ${parameters.size} type parameters, but ${arguments.size} were provided.")
    }

    val typeAnnotations =
            if (annotations.isEmpty()) Annotations.EMPTY
            else Annotations.EMPTY // TODO: support type annotations

    val kotlinType = createKotlinType(typeAnnotations, typeConstructor, arguments, nullable)

    return KTypeImpl(kotlinType) {
        when (this) {
            is KClassImpl<*> -> jClass
            is KTypeParameterImpl -> TODO("Java type is not yet supported for type variables: $this")
            else -> TODO("Java type is not yet supported for non-class classifiers: $this")
        }
    }
}

private fun createKotlinType(
        typeAnnotations: Annotations, typeConstructor: TypeConstructor, arguments: List<KTypeProjection>, nullable: Boolean
): SimpleType {
    val parameters = typeConstructor.parameters
    return KotlinTypeFactory.simpleType(typeAnnotations, typeConstructor, arguments.mapIndexed { index, typeProjection ->
        if (typeProjection is KTypeProjection.Star) {
            StarProjectionImpl(parameters[index])
        }
        else {
            val type = (typeProjection.type as KTypeImpl).type
            when (typeProjection) {
                is KTypeProjection.Invariant -> TypeProjectionImpl(Variance.INVARIANT, type)
                is KTypeProjection.In -> TypeProjectionImpl(Variance.IN_VARIANCE, type)
                is KTypeProjection.Out -> TypeProjectionImpl(Variance.OUT_VARIANCE, type)
                is KTypeProjection.Star -> error("Unreachable")
            }
        }
    }, nullable)
}

/**
 * TODO
 */
val KClassifier.starProjectedType: KType
    get() {
        val descriptor = (this as? KClassifierImpl)?.descriptor
                         ?: return createType()

        val typeParameters = descriptor.typeConstructor.parameters
        if (typeParameters.isEmpty()) return createType() // TODO: optimize, get defaultType from ClassDescriptor

        return createType(typeParameters.map { KTypeProjection.Star })
    }
