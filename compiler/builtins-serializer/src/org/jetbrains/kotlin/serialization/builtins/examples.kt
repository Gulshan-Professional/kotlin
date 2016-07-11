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

package org.jetbrains.kotlin.serialization.builtins

/*
import org.jetbrains.kotlin.container.ComponentStorage
import org.jetbrains.kotlin.container.IterableDescriptor
import org.jetbrains.kotlin.container.ValueDescriptor
import org.jetbrains.kotlin.container.ValueResolveContext
import kotlin.reflect.*


// ***

interface SomeContext
interface KClassifier
sealed class KTypeProjection {
    val type: KType get() = null!!
    class Invariant : KTypeProjection()
    class Out : KTypeProjection()
    class In : KTypeProjection()
    object Star : KTypeProjection()
}
val KType.classifier: KClassifier get() = null!!
val KType.arguments: List<KTypeProjection> get() = null!!




// Invoke a constructor with a parameter of a certain type (KT-8998 comments)

fun <T : Any> create(klass: KClass<T>, context: SomeContext): T? {
    return klass.constructors.firstOrNull {
        it.parameters.singleOrNull()?.type?.classifier == SomeContext::class
    }?.call(context)
}

// Extract Iterable's type argument (StorageComponentContainer#resolveIterable)

private fun resolveIterable(request: KType, context: ValueResolveContext): ValueDescriptor? {
    if (request.classifier != Iterable::class) return null
    val typeArguments = request.arguments
    val iterableTypeProjection = typeArguments.singleOrNull() ?: return null

    val iterableType =
            when (iterableTypeProjection) {
                is KTypeProjection.In -> return null
                is KTypeProjection.Star -> Any::class.defaultType
                else -> iterableTypeProjection.type
            }

    return IterableDescriptor(componentStorage.resolveMultiple(iterableType, context))
}

*/
