package com.qhatcorp.qhat.interceptor

import io.grpc.Context
import io.grpc.Contexts
import io.grpc.Metadata
import io.grpc.Metadata.ASCII_STRING_MARSHALLER
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import io.jsonwebtoken.Jwts
import org.lognet.springboot.grpc.GRpcGlobalInterceptor

@GRpcGlobalInterceptor
class AuthorizationInterceptor : ServerInterceptor {
    class NoOperationListener<ReqT> : ServerCall.Listener<ReqT>()

    companion object {
        const val BEARER_TYPE = "Bearer"
        const val ACCESS_TOKEN_KEY = "C20F2F601BAA9B88945249CC9D499948C069B517A2278DB6C3307E596F125660"
        const val REFRESH_TOKEN_KEY = "249CC9D4B51601BAA9B9C20F2F32278DB6C6F125660307E59948C0698894597A"
        val AUTHORIZATION_METADATA_KEY: Metadata.Key<String> = Metadata.Key.of("Authorization", ASCII_STRING_MARSHALLER)
        val CLIENT_ID_CONTEXT_KEY: Context.Key<String> = Context.key("clientId")
    }

    private val accessTokenParser = Jwts.parserBuilder().setSigningKey(ACCESS_TOKEN_KEY).build()
    private val refreshTokenParser = Jwts.parserBuilder().setSigningKey(REFRESH_TOKEN_KEY).build()

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val status = headers.get(AUTHORIZATION_METADATA_KEY)?.let { value ->
            when (value.startsWith(BEARER_TYPE)) {
                false -> Status.UNAUTHENTICATED.withDescription("Unknown authorization type")
                true -> try {
                    val token = value.substring(BEARER_TYPE.length).trim { it <= ' ' }
                    val claims = accessTokenParser.parseClaimsJws(token)
                    val ctx = Context.current().withValue(CLIENT_ID_CONTEXT_KEY, claims.body.subject)
                    return Contexts.interceptCall(ctx, call, headers, next)
                } catch (e: Exception) {
                    Status.UNAUTHENTICATED.withDescription(e.message).withCause(e)
                }
            }
        } ?: run {
            Status.UNAUTHENTICATED.withDescription("Authorization token is missing")
        }
        call.close(status, headers)
        // no op
        return NoOperationListener()
    }
}