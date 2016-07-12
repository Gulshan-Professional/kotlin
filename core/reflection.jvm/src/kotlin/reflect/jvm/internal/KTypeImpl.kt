/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package kotlin.reflect.jvm.internal

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.TypeAliasDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.load.java.structure.reflect.createArrayType
import org.jetbrains.kotlin.load.java.structure.reflect.primitiveByWrapper
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.Variance
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KotlinReflectionInternalError

internal class KTypeImpl(
        val type: KotlinType,
        computeJavaType: () -> Type
) : KType {
    internal val javaType: Type by ReflectProperties.lazySoft(computeJavaType)

    override val classifier: KClassifier?
        get() = convert(type)

    private fun convert(type: KotlinType): KClassifier? {
        val descriptor = type.constructor.declarationDescriptor
        when (descriptor) {
            is ClassDescriptor -> {
                val jClass = descriptor.toJavaClass() ?: return null
                if (jClass.isArray) {
                    // There may be no argument if it's a primitive array (such as IntArray)
                    val argument = type.arguments.singleOrNull()?.type
                    return if (argument == null) KClassImpl(jClass)
                    else {
                        val elementType = convert(argument) as? KClassImpl<*>
                                          ?: TODO("Unsupported array element type: $argument")
                        KClassImpl(elementType.java.createArrayType())
                    }
                }

                if (!type.isMarkedNullable) {
                    return KClassImpl(jClass.primitiveByWrapper ?: jClass)
                }

                return KClassImpl(jClass)
            }
            is TypeParameterDescriptor -> TODO("Type parameter classifiers are not yet supported")
            is TypeAliasDescriptor -> TODO("Type alias classifiers are not yet supported")
            else -> return null
        }
    }

    override val arguments: List<KTypeProjection>
        get() = type.arguments.mapIndexed { i, typeProjection ->
            if (typeProjection.isStarProjection) {
                KTypeProjection.Star
            }
            else {
                val type = KTypeImpl(typeProjection.type) {
                    val javaType = javaType
                    when (javaType) {
                        is GenericArrayType -> {
                            if (i != 0) throw KotlinReflectionInternalError("Array type has been queried for a non-0th argument: $this")
                            javaType.genericComponentType
                        }
                        is ParameterizedType -> {
                            // TODO: this is cloned every time
                            javaType.actualTypeArguments[i]
                        }
                        else -> throw KotlinReflectionInternalError("Non-generic type has been queried for arguments: $this")
                    }
                }
                when (typeProjection.projectionKind) {
                    Variance.INVARIANT -> KTypeProjection.Invariant(type)
                    Variance.IN_VARIANCE -> KTypeProjection.In(type)
                    Variance.OUT_VARIANCE -> KTypeProjection.Out(type)
                }
            }
        }

    override val isMarkedNullable: Boolean
        get() = type.isMarkedNullable

    override fun equals(other: Any?) =
            other is KTypeImpl && type == other.type

    override fun hashCode() =
            type.hashCode()

    override fun toString() =
            ReflectionObjectRenderer.renderType(type)
}
