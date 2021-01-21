// AUTO-GENERATED FILE. DO NOT MODIFY.
//
// This class was automatically generated by Apollo GraphQL plugin from the GraphQL queries it found.
// It should not be modified by hand.
//
package com.example.arguments_complex.adapter

import com.apollographql.apollo.api.ResponseField
import com.apollographql.apollo.api.internal.ResponseAdapter
import com.apollographql.apollo.api.internal.ResponseReader
import com.apollographql.apollo.api.internal.ResponseWriter
import com.example.arguments_complex.TestQuery
import kotlin.Array
import kotlin.Double
import kotlin.String
import kotlin.Suppress

@Suppress("NAME_SHADOWING", "UNUSED_ANONYMOUS_PARAMETER", "LocalVariableName",
    "RemoveExplicitTypeArguments", "NestedLambdaShadowedImplicitParameter", "PropertyName",
    "RemoveRedundantQualifierName")
object TestQuery_ResponseAdapter : ResponseAdapter<TestQuery.Data> {
  private val RESPONSE_FIELDS: Array<ResponseField> = arrayOf(
    ResponseField.forObject("heroWithReview", "heroWithReview", mapOf<String, Any?>(
      "episode" to mapOf<String, Any?>(
        "kind" to "Variable",
        "variableName" to "episode"),
      "review" to mapOf<String, Any?>(
        "stars" to mapOf<String, Any?>(
          "kind" to "Variable",
          "variableName" to "stars"),
        "favoriteColor" to mapOf<String, Any?>(
          "red" to 0,
          "green" to mapOf<String, Any?>(
            "kind" to "Variable",
            "variableName" to "greenValue"),
          "blue" to 0.0),
        "booleanNonOptional" to false,
        "listOfStringNonOptional" to emptyList<Any?>()),
      "listOfInts" to listOf<Any?>(
        mapOf<String, Any?>(
          "kind" to "Variable",
          "variableName" to "stars"),
        mapOf<String, Any?>(
          "kind" to "Variable",
          "variableName" to "stars"))), true, null)
  )

  override fun fromResponse(reader: ResponseReader, __typename: String?): TestQuery.Data {
    return reader.run {
      var heroWithReview: TestQuery.Data.HeroWithReview? = null
      while(true) {
        when (selectField(RESPONSE_FIELDS)) {
          0 -> heroWithReview = readObject<TestQuery.Data.HeroWithReview>(RESPONSE_FIELDS[0]) { reader ->
            HeroWithReview.fromResponse(reader)
          }
          else -> break
        }
      }
      TestQuery.Data(
        heroWithReview = heroWithReview
      )
    }
  }

  override fun toResponse(writer: ResponseWriter, value: TestQuery.Data) {
    if(value.heroWithReview == null) {
      writer.writeObject(RESPONSE_FIELDS[0], null)
    } else {
      writer.writeObject(RESPONSE_FIELDS[0]) { writer ->
        HeroWithReview.toResponse(writer, value.heroWithReview)
      }
    }
  }

  object HeroWithReview : ResponseAdapter<TestQuery.Data.HeroWithReview> {
    private val RESPONSE_FIELDS: Array<ResponseField> = arrayOf(
      ResponseField.forString("name", "name", null, false, null),
      ResponseField.forDouble("height", "height", mapOf<String, Any?>(
        "unit" to "FOOT"), true, null)
    )

    override fun fromResponse(reader: ResponseReader, __typename: String?):
        TestQuery.Data.HeroWithReview {
      return reader.run {
        var name: String? = null
        var height: Double? = null
        while(true) {
          when (selectField(RESPONSE_FIELDS)) {
            0 -> name = readString(RESPONSE_FIELDS[0])
            1 -> height = readDouble(RESPONSE_FIELDS[1])
            else -> break
          }
        }
        TestQuery.Data.HeroWithReview(
          name = name!!,
          height = height
        )
      }
    }

    override fun toResponse(writer: ResponseWriter, value: TestQuery.Data.HeroWithReview) {
      writer.writeString(RESPONSE_FIELDS[0], value.name)
      writer.writeDouble(RESPONSE_FIELDS[1], value.height)
    }
  }
}