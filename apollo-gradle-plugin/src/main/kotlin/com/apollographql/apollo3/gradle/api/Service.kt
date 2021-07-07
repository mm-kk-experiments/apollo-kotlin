package com.apollographql.apollo3.gradle.api

import com.apollographql.apollo3.compiler.OperationIdGenerator
import com.apollographql.apollo3.compiler.OperationOutputGenerator
import com.apollographql.apollo3.compiler.PackageNameGenerator
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.TaskProvider

/**
 * A [Service] represents a GraphQL schema and associated queries.
 *
 * The queries will be compiled and verified against the schema to generate the models.
 */
interface Service {
  val name: String

  /**
   * Files to include as in [org.gradle.api.tasks.util.PatternFilterable]
   *
   * Default: ["**&#47;*.graphql", "**&#47;*.gql"]
   */
  val include: ListProperty<String>

  /**
   * Files to exclude as in [org.gradle.api.tasks.util.PatternFilterable]
   *
   * The empty list by default
   */
  val exclude: ListProperty<String>

  /**
   * Where to look for GraphQL sources.
   * The plugin will look in "src/main/graphql/$sourceFolder" for Android/JVM projects and "src/commonMain/graphql/$sourceFolder" for multiplatform projects.
   *
   * For more control, see also [srcDir]
   */
  val sourceFolder: Property<String>

  /**
   * Adds the given directory as a GraphQL source root
   *
   * Use [srcDir] if your files are outside of "src/main/graphql" or to have them in multiple folders.
   *
   */
  fun srcDir(directory: Any)

  /**
   * A shorthand property that will be used if [schemaFiles] is empty
   */
  val schemaFile: RegularFileProperty

  /**
   * The schema files as either a ".json" introspection schema or a ".sdl|.graphqls" SDL schema. You might come across schemas named "schema.graphql",
   * these are SDL schemas most of the time that need to be renamed to "schema.graphqls" to be recognized properly.
   *
   * The compiler accepts multiple schema files in order to add extensions to specify key fields and other schema extensions.
   *
   * By default, the plugin collects all "schema.[json|sdl|graphqls]" file in the source roots
   */
  val schemaFiles: ConfigurableFileCollection

  /**
   * Warn if using a deprecated field
   *
   * Default value: true
   */
  val warnOnDeprecatedUsages: Property<Boolean>

  /**
   * Fail the build if there are warnings. This is not named `allWarningAsErrors` to avoid nameclashes with the Kotlin options
   *
   * Default value: false
   */
  val failOnWarnings: Property<Boolean>

  /**
   * For custom scalar types like Date, map from the GraphQL type to the java/kotlin type.
   *
   * Default value: the empty map
   */
  val customScalarsMapping: MapProperty<String, String>

  /**
   * By default, Apollo uses `Sha256` hashing algorithm to generate an ID for the query.
   * To provide a custom ID generation logic, pass an `instance` that implements the [OperationIdGenerator]. How the ID is generated is
   * indifferent to the compiler. It can be an hashing algorithm or generated by a backend.
   *
   * Example Md5 hash generator:
   * ```groovy
   * import com.apollographql.apollo3.compiler.OperationIdGenerator
   *
   * apollo {
   *   operationIdGenerator = new OperationIdGenerator() {
   *     String apply(String operationDocument, String operationFilepath) {
   *       return operationDocument.md5()
   *     }
   *
   *     /**
   *      * Use this version override to indicate an update to the implementation.
   *      * This invalidates the current cache.
   *      */
   *     String version = "v1"
   *   }
   * }
   * ```
   *
   * Default value: [OperationIdGenerator.Sha256]
   */
  val operationIdGenerator: Property<OperationIdGenerator>

  /**
   * A generator to generate the operation output from a list of operations.
   * OperationOutputGenerator is similar to [OperationIdGenerator] but can work on lists. This is useful if you need
   * to register/whitelist your operations on your server all at once.
   *
   * Example Md5 hash generator:
   * ```groovy
   * import com.apollographql.apollo3.compiler.OperationIdGenerator
   *
   * apollo {
   *   operationOutputGenerator = new OperationIdGenerator() {
   *     String apply(List<operation operationDocument, String operationFilepath) {
   *       return operationDocument.md5()
   *     }
   *
   *     /**
   *      * Use this version override to indicate an update to the implementation.
   *      * This invalidates the current cache.
   *      */
   *     String version = "v1"
   *   }
   * }
   * ```
   *
   * Default value: [OperationIdGenerator.Sha256]
   */
  val operationOutputGenerator: Property<OperationOutputGenerator>

  /**
   * When true, the generated classes names will end with 'Query' or 'Mutation'.
   * If you write `query droid { ... }`, the generated class will be named 'DroidQuery'.
   *
   * Default value: true
   */
  val useSemanticNaming: Property<Boolean>

  /**
   * The package name of the models. The compiler will generate classes in
   *
   * - $packageName/SomeQuery.kt
   * - $packageName/fragment/SomeFragment.kt
   * - $packageName/type/Types.kt
   * - $packageName/type/SomeInputObject.kt
   * - $packageName/type/SomeEnum.kt
   *
   * Default value: ""
   */
  val packageName: Property<String>

  /**
   * Use [packageNameGenerator] to customize how to generate package names from file paths.
   *
   * See [PackageNameGenerator] for more details
   */
  val packageNameGenerator: Property<PackageNameGenerator>

  /**
   * A helper method to configure a [PackageNameGenerator] that will use the file path
   * relative to the source roots to generate the packageNames
   *
   * @param rootPackageName: a root package name to prepend to the package names
   *
   * Example, with the below configuration:
   *
   * ```
   * srcDir("src/main/graphql")
   * filePathAwarePackageNameGenerator("com.example")
   * ```
   *
   * an operation defined in `src/main/graphql/query/feature1` will use `com.example.query.feature1`
   * as package name
   * an input object defined in `src/main/graphql/schema/schema.graphqls` will use `com.example.schema.type`
   * as package name
   */
  fun filePathAwarePackageNameGenerator(rootPackageName: String? = null)

  /**
   * Whether to generate Kotlin models with `internal` visibility modifier.
   *
   * Default value: false
   */
  val generateAsInternal: Property<Boolean>

  /**
   * Whether to generate Apollo metadata. Apollo metadata is used for multi-module support. Set this to true if you want other
   * modules to be able to re-use fragments and types from this module.
   *
   * This is currently experimental and this API might change in the future.
   *
   * Default value: false
   */
  val generateApolloMetadata: Property<Boolean>

  /**
   * A list of [Regex] patterns for input/scalar/enum types that should be generated whether or not they are used by queries/fragments
   * in this module. When using multiple modules, Apollo Android will generate all the types by default in the root module
   * because the root module doesn't know what types are going to be used by dependent modules. This can be prohibitive in terms
   * of compilation speed for large projects. If that's the case, opt-in the types that are used by multiple dependent modules here.
   * You don't need to add types that are used by a single dependent module.
   *
   * This is currently experimental and this API might change in the future.
   *
   * Default value: if (generateApolloMetadata) listOf(".*") else listOf()
   */
  val alwaysGenerateTypesMatching: SetProperty<String>

  /**
   * Whether to generate default implementation classes for GraphQL fragments.
   * Default value is `false`, means only interfaces are been generated.
   *
   * Most of the time, fragment implementations are not needed because you can easily access fragments interfaces and read all
   * data from your queries. They are needed if you want to be able to build fragments outside of an operation. For an exemple
   * to programmatically build a fragment that is reused in another part of your code or to read and write fragments to the cache.
   */
  val generateFragmentImplementations: Property<Boolean>

  /**
   * Unused property for the moment. Left to save users to edit one line. Will be used again when there is
   * Java codegen
   */
  val generateKotlinModels: Property<Boolean>

  /**
   * What codegen to use. One of "operationBased", "responseBased" or "compat"
   *
   * Default value: "compat"
   */
  val codegenModels: Property<String>

  /**
   * Whether to flatten the models. File paths are limited on MacOSX to 256 chars and flattening can help keeping the path length manageable
   * The drawback is that some classes may nameclash in which case they will be suffixed with a number
   */
  val flattenModels: Property<Boolean>

  /**
   * The directory where the generated models will be written. It's called [outputDir] but this an "input" parameter for the compiler
   * If you want a [DirectoryProperty] that carries the task dependency, use [withOutputDir]
   */
  val outputDir: DirectoryProperty

  /**
   * Whether to generate the operationOutput.json
   *
   * Defaults value: false
   */
  val generateOperationOutput: Property<Boolean>

  /**
   * The file where the operation output will be written. It's called [operationOutputFile] but this an "input" parameter for the compiler
   * If you want a [RegularFileProperty] that carries the task dependency, use [withOperationOutput]
   */
  val operationOutputFile: RegularFileProperty

  /**
   * A debug directory where the compiler will output intermediary results
   */
  val debugDir: DirectoryProperty

  /**
   * Configures [Introspection] to download an introspection Json schema
   */
  fun introspection(configure: Action<in Introspection>)

  /**
   * Configures [Registry] to download a SDL schema from a studio registry
   */
  fun registry(configure: Action<in Registry>)

  /**
   * overrides the way operationOutput is wired. Use this if you want to wire the generated operationOutput. By default, oeprationOutput
   * is not generated and therefore not wired.
   */
  fun withOperationOutput(action: Action<in OperationOutputWire>)

  class OperationOutputWire(
      /**
       * The task that produces operationOutput
       */
      val task: TaskProvider<out Task>,

      /**
       * A json file containing a [Map]<[String], [com.apollographql.apollo3.compiler.operationoutput.OperationDescriptor]>
       *
       * This file can be used to upload the queries exact content and their matching operation ID to a server for whitelisting
       * or persisted queries.
       */
      val operationOutputFile: Provider<RegularFile>,
  )

  /**
   * overrides the way the task is wired. Use this if you want to wire the generated sources to another task than the default destination.
   *
   * By default, the generated sources are wired to:
   * - main sourceSet for Kotlin projects
   * - commonMain sourceSet for Kotlin multiplatform projects
   * - all variants for Android projects
   */
  fun withOutputDir(action: Action<in OutputDirWire>)

  class OutputDirWire(
      /**
       * The task that produces outputDir
       */
      val task: TaskProvider<out Task>,
      /**
       * The directory where the generated models will be written
       */
      val outputDir: Provider<Directory>,
  )
}