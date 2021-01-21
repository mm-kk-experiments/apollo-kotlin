// AUTO-GENERATED FILE. DO NOT MODIFY.
//
// This class was automatically generated by Apollo GraphQL plugin from the GraphQL queries it found.
// It should not be modified by hand.
//
package com.example.reserved_keywords.adapter

import com.apollographql.apollo.api.ResponseField
import com.apollographql.apollo.api.internal.ResponseAdapter
import com.apollographql.apollo.api.internal.ResponseReader
import com.apollographql.apollo.api.internal.ResponseWriter
import com.example.reserved_keywords.TestQuery
import kotlin.Array
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List

@Suppress("NAME_SHADOWING", "UNUSED_ANONYMOUS_PARAMETER", "LocalVariableName",
    "RemoveExplicitTypeArguments", "NestedLambdaShadowedImplicitParameter", "PropertyName",
    "RemoveRedundantQualifierName")
object TestQuery_ResponseAdapter : ResponseAdapter<TestQuery.Data> {
  private val RESPONSE_FIELDS: Array<ResponseField> = arrayOf(
    ResponseField.forObject("yield", "hero", null, true, null),
    ResponseField.forList("objects", "search", mapOf<String, Any?>(
      "text" to "abc"), true, null)
  )

  override fun fromResponse(reader: ResponseReader, __typename: String?): TestQuery.Data {
    return reader.run {
      var yield_: TestQuery.Data.Yield? = null
      var objects: List<TestQuery.Data.Object?>? = null
      while(true) {
        when (selectField(RESPONSE_FIELDS)) {
          0 -> yield_ = readObject<TestQuery.Data.Yield>(RESPONSE_FIELDS[0]) { reader ->
            Yield.fromResponse(reader)
          }
          1 -> objects = readList<TestQuery.Data.Object>(RESPONSE_FIELDS[1]) { reader ->
            reader.readObject<TestQuery.Data.Object> { reader ->
              Object.fromResponse(reader)
            }
          }
          else -> break
        }
      }
      TestQuery.Data(
        yield_ = yield_,
        objects = objects
      )
    }
  }

  override fun toResponse(writer: ResponseWriter, value: TestQuery.Data) {
    if(value.yield_ == null) {
      writer.writeObject(RESPONSE_FIELDS[0], null)
    } else {
      writer.writeObject(RESPONSE_FIELDS[0]) { writer ->
        Yield.toResponse(writer, value.yield_)
      }
    }
    writer.writeList(RESPONSE_FIELDS[1], value.objects) { value, listItemWriter ->
      listItemWriter.writeObject { writer ->
        Object.toResponse(writer, value)
      }
    }
  }

  object Yield : ResponseAdapter<TestQuery.Data.Yield> {
    private val RESPONSE_FIELDS: Array<ResponseField> = arrayOf(
      ResponseField.forString("it", "id", null, false, null),
      ResponseField.forString("name", "name", null, false, null)
    )

    override fun fromResponse(reader: ResponseReader, __typename: String?): TestQuery.Data.Yield {
      return reader.run {
        var it_: String? = null
        var name: String? = null
        while(true) {
          when (selectField(RESPONSE_FIELDS)) {
            0 -> it_ = readString(RESPONSE_FIELDS[0])
            1 -> name = readString(RESPONSE_FIELDS[1])
            else -> break
          }
        }
        TestQuery.Data.Yield(
          it_ = it_!!,
          name = name!!
        )
      }
    }

    override fun toResponse(writer: ResponseWriter, value: TestQuery.Data.Yield) {
      writer.writeString(RESPONSE_FIELDS[0], value.it_)
      writer.writeString(RESPONSE_FIELDS[1], value.name)
    }
  }

  object Object : ResponseAdapter<TestQuery.Data.Object> {
    private val RESPONSE_FIELDS: Array<ResponseField> = arrayOf(
      ResponseField.forString("__typename", "__typename", null, false, null)
    )

    override fun fromResponse(reader: ResponseReader, __typename: String?): TestQuery.Data.Object {
      val typename = __typename ?: reader.readString(RESPONSE_FIELDS[0])
      return when(typename) {
        "Human" -> CharacterObject.fromResponse(reader, typename)
        "Droid" -> CharacterObject.fromResponse(reader, typename)
        else -> OtherObject.fromResponse(reader, typename)
      }
    }

    override fun toResponse(writer: ResponseWriter, value: TestQuery.Data.Object) {
      when(value) {
        is TestQuery.Data.Object.CharacterObject -> CharacterObject.toResponse(writer, value)
        is TestQuery.Data.Object.OtherObject -> OtherObject.toResponse(writer, value)
      }
    }

    object CharacterObject : ResponseAdapter<TestQuery.Data.Object.CharacterObject> {
      private val RESPONSE_FIELDS: Array<ResponseField> = arrayOf(
        ResponseField.forString("__typename", "__typename", null, false, null),
        ResponseField.forString("name", "name", null, false, null)
      )

      override fun fromResponse(reader: ResponseReader, __typename: String?):
          TestQuery.Data.Object.CharacterObject {
        return reader.run {
          var __typename: String? = __typename
          var name: String? = null
          while(true) {
            when (selectField(RESPONSE_FIELDS)) {
              0 -> __typename = readString(RESPONSE_FIELDS[0])
              1 -> name = readString(RESPONSE_FIELDS[1])
              else -> break
            }
          }
          TestQuery.Data.Object.CharacterObject(
            __typename = __typename!!,
            name = name!!
          )
        }
      }

      override fun toResponse(writer: ResponseWriter,
          value: TestQuery.Data.Object.CharacterObject) {
        writer.writeString(RESPONSE_FIELDS[0], value.__typename)
        writer.writeString(RESPONSE_FIELDS[1], value.name)
      }
    }

    object OtherObject : ResponseAdapter<TestQuery.Data.Object.OtherObject> {
      private val RESPONSE_FIELDS: Array<ResponseField> = arrayOf(
        ResponseField.forString("__typename", "__typename", null, false, null)
      )

      override fun fromResponse(reader: ResponseReader, __typename: String?):
          TestQuery.Data.Object.OtherObject {
        return reader.run {
          var __typename: String? = __typename
          while(true) {
            when (selectField(RESPONSE_FIELDS)) {
              0 -> __typename = readString(RESPONSE_FIELDS[0])
              else -> break
            }
          }
          TestQuery.Data.Object.OtherObject(
            __typename = __typename!!
          )
        }
      }

      override fun toResponse(writer: ResponseWriter, value: TestQuery.Data.Object.OtherObject) {
        writer.writeString(RESPONSE_FIELDS[0], value.__typename)
      }
    }
  }
}