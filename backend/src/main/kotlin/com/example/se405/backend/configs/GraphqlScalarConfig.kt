package com.example.se405.backend.configs

import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.RuntimeWiringConfigurer
import java.time.LocalDate
import java.util.UUID

@Configuration
class GraphqlScalarConfig {
    @Bean
    fun runtimeWiringConfigurer(): RuntimeWiringConfigurer {
        return RuntimeWiringConfigurer { builder ->
            builder.scalar(uuidScalar())
            builder.scalar(localDateScalar())
        }
    }

    private fun uuidScalar(): GraphQLScalarType {
        return GraphQLScalarType.newScalar()
            .name("UUID")
            .coercing(object : Coercing<UUID, String> {
                override fun serialize(dataFetcherResult: Any): String {
                    return when (dataFetcherResult) {
                        is UUID -> dataFetcherResult.toString()
                        is String -> runCatching { UUID.fromString(dataFetcherResult) }
                            .getOrElse { throw CoercingSerializeException("Invalid UUID value") }
                            .toString()
                        else -> throw CoercingSerializeException("Invalid UUID value")
                    }
                }

                override fun parseValue(input: Any): UUID {
                    return runCatching { UUID.fromString(input.toString()) }
                        .getOrElse { throw CoercingParseValueException("Invalid UUID value") }
                }

                override fun parseLiteral(input: Any): UUID {
                    if (input !is StringValue) {
                        throw CoercingParseLiteralException("Invalid UUID literal")
                    }
                    return runCatching { UUID.fromString(input.value) }
                        .getOrElse { throw CoercingParseLiteralException("Invalid UUID literal") }
                }
            })
            .build()
    }

    private fun localDateScalar(): GraphQLScalarType {
        return GraphQLScalarType.newScalar()
            .name("LocalDate")
            .coercing(object : Coercing<LocalDate, String> {
                override fun serialize(dataFetcherResult: Any): String {
                    return when (dataFetcherResult) {
                        is LocalDate -> dataFetcherResult.toString()
                        is String -> runCatching { LocalDate.parse(dataFetcherResult) }
                            .getOrElse { throw CoercingSerializeException("Invalid LocalDate value") }
                            .toString()
                        else -> throw CoercingSerializeException("Invalid LocalDate value")
                    }
                }

                override fun parseValue(input: Any): LocalDate {
                    return runCatching { LocalDate.parse(input.toString()) }
                        .getOrElse { throw CoercingParseValueException("Invalid LocalDate value") }
                }

                override fun parseLiteral(input: Any): LocalDate {
                    if (input !is StringValue) {
                        throw CoercingParseLiteralException("Invalid LocalDate literal")
                    }
                    return runCatching { LocalDate.parse(input.value) }
                        .getOrElse { throw CoercingParseLiteralException("Invalid LocalDate literal") }
                }
            })
            .build()
    }
}
