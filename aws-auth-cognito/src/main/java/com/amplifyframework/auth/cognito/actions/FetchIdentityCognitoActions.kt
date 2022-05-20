/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amplifyframework.auth.cognito.actions

import aws.sdk.kotlin.services.cognitoidentity.model.GetIdRequest
import com.amplifyframework.auth.cognito.AuthEnvironment
import com.amplifyframework.statemachine.Action
import com.amplifyframework.statemachine.codegen.actions.FetchIdentityActions
import com.amplifyframework.statemachine.codegen.data.AmplifyCredential
import com.amplifyframework.statemachine.codegen.events.FetchAuthSessionEvent
import com.amplifyframework.statemachine.codegen.events.FetchIdentityEvent

object FetchIdentityCognitoActions : FetchIdentityActions {
    override fun initFetchIdentityAction(amplifyCredential: AmplifyCredential?): Action =
        Action<AuthEnvironment>("InitFetchIdentity") { id, dispatcher ->
            logger?.verbose("$id Starting execution")
            val evt = try {
                val idToken = amplifyCredential?.cognitoUserPoolTokens?.idToken
                val loginsMap: Map<String, String>? = configuration.userPool?.identityProviderName?.let { provider ->
                    idToken?.let { mapOf(provider to idToken) }
                }

                val getIdRequest = GetIdRequest {
                    identityPoolId = configuration.identityPool?.poolId
                    loginsMap?.apply { logins = loginsMap }
                }

                val getIDResponse = cognitoAuthService.cognitoIdentityClient?.getId(getIdRequest)

                val updatedAmplifyCredential = AmplifyCredential(
                    cognitoUserPoolTokens = amplifyCredential?.cognitoUserPoolTokens,
                    identityId = getIDResponse?.identityId,
                    awsCredentials = amplifyCredential?.awsCredentials
                )

                val fetchedEvent = FetchIdentityEvent(FetchIdentityEvent.EventType.Fetched())
                logger?.verbose("$id Sending event ${fetchedEvent.type}")
                dispatcher.send(fetchedEvent)

                FetchAuthSessionEvent(FetchAuthSessionEvent.EventType.FetchAwsCredentials(updatedAmplifyCredential))
            } catch (e: Exception) {
                val errorEvent = FetchIdentityEvent(FetchIdentityEvent.EventType.ThrowError(e))
                logger?.verbose("$id Sending event ${errorEvent.type}")
                dispatcher.send(errorEvent)

                FetchAuthSessionEvent(FetchAuthSessionEvent.EventType.FetchAwsCredentials(amplifyCredential))
            }
            logger?.verbose("$id Sending event ${evt.type}")
            dispatcher.send(evt)
        }
}