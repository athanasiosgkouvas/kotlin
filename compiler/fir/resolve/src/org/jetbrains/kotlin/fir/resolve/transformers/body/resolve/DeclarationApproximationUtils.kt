/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers.body.resolve

import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.resolve.substitution.AbstractConeSubstitutor
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.types.TypeApproximatorConfiguration
import org.jetbrains.kotlin.utils.addToStdlib.applyIf

fun FirResolvedTypeRef.approximateDeclarationType(
    session: FirSession,
    containingCallableVisibility: Visibility?,
    isLocal: Boolean,
    isInlineFunction: Boolean = false,
    stripEnhancedNullability: Boolean = true
): FirResolvedTypeRef {
    val approximatedType = coneType.approximateDeclarationType(
        session, containingCallableVisibility, isLocal, isInlineFunction
    )
    return this.withReplacedConeType(approximatedType).applyIf(stripEnhancedNullability) { withoutEnhancedNullability() }
}

fun ConeKotlinType.approximateDeclarationType(
    session: FirSession,
    containingCallableVisibility: Visibility?,
    isLocal: Boolean,
    isInlineFunction: Boolean = false
): ConeKotlinType {
    val configuration = when (isLocal) {
        true -> TypeApproximatorConfiguration.LocalDeclaration
        false -> when (shouldApproximateAnonymousTypesOfNonLocalDeclaration(containingCallableVisibility, isInlineFunction)) {
            true -> TypeApproximatorConfiguration.PublicDeclaration.ApproximateAnonymousTypes
            false -> TypeApproximatorConfiguration.PublicDeclaration.SaveAnonymousTypes
        }
    }

    var approximatedType = session.typeApproximator.approximateToSuperType(this, configuration) ?: this
    if (approximatedType.contains { type -> type.attributes.any { !it.keepInInferredDeclarationType } }) {
        approximatedType = UnnecessaryAttributesRemover(session).substituteOrSelf(approximatedType)
    }
    return approximatedType
}

private class UnnecessaryAttributesRemover(session: FirSession) : AbstractConeSubstitutor(session.typeContext) {
    override fun substituteType(type: ConeKotlinType): ConeKotlinType? {
        val filteredAttributes = type.attributes.filterNecessaryToKeep()
        return if (filteredAttributes === type.attributes) null
        else type.withAttributes(filteredAttributes)
    }
}

