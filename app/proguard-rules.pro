# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html
# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable
# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit
# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

-dontwarn okio.**
-dontwarn okhttp3.**

-dontwarn retrofit2.**
-dontwarn org.codehaus.mojo.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions



# Optimize
-optimizations !field/*,!class/merging/*,*
-mergeinterfacesaggressively

# will keep line numbers and file name obfuscation
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

## Apache POI
#-dontwarn org.apache.**
#-dontwarn org.openxmlformats.schemas.**
#-dontwarn org.etsi.**
#-dontwarn org.w3.**
#-dontwarn com.microsoft.schemas.**
#-dontwarn com.graphbuilder.**
#-dontnote org.apache.**
#-dontnote org.openxmlformats.schemas.**
#-dontnote org.etsi.**
#-dontnote org.w3.**
#-dontnote com.microsoft.schemas.**
#-dontnote com.graphbuilder.**
#
#-keeppackagenames org.apache.poi.ss.formula.function
#
#-keep class com.fasterxml.aalto.stax.InputFactoryImpl
#-keep class com.fasterxml.aalto.stax.OutputFactoryImpl
#-keep class com.fasterxml.aalto.stax.EventFactoryImpl
#
#-keep class schemaorg_apache_xmlbeans.system.sF1327CCA741569E70F9CA8C9AF9B44B2.TypeSystemHolder { public final static *** typeSystem; }
#
#-keep class org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem { public static *** get(...); public static *** getNoType(...); }
#-keep class org.apache.xmlbeans.impl.schema.PathResourceLoader { public <init>(...); }
#-keep class org.apache.xmlbeans.impl.schema.SchemaTypeSystemCompiler { public static *** compile(...); }
#-keep class org.apache.xmlbeans.impl.schema.SchemaTypeSystemImpl { public <init>(...); public static *** get(...); public static *** getNoType(...); }
#-keep class org.apache.xmlbeans.impl.schema.SchemaTypeLoaderImpl { public static *** getContextTypeLoader(...); public static *** build(...); }
#-keep class org.apache.xmlbeans.impl.store.Locale { public static *** streamToNode(...); public static *** nodeTo*(...); }
#-keep class org.apache.xmlbeans.impl.store.Path { public static *** compilePath(...); }
#-keep class org.apache.xmlbeans.impl.store.Query { public static *** compileQuery(...); }
#
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CommentsDocument { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAuthors { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBooleanProperty { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBookView { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBookViews { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorders { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorderPr { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellAlignment { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellFormula { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellStyleXfs { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellXfs { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComments { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCommentList { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDrawing { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFills { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFonts { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFontName { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFontScheme { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFontSize { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTIntProperty { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTLegacyDrawing { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmts { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPatternFill { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPageMargins { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPane { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSelection { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetData { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetDimension { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetFormatPr { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetView { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetViews { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheets { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSst { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTStylesheet { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbookPr { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.SstDocument { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.StyleSheetDocument { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellType$Enum { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellFormulaType$Enum { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.STXstring { *; }
#
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CommentsDocumentImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTAuthorsImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTBooleanPropertyImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTBookViewImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTBookViewsImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTBorderImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTBordersImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTBorderPrImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTCellImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTCellAlignmentImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTCellFormulaImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTCellStyleXfsImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTCellXfsImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTColorImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTColImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTColsImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTCommentImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTCommentsImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTCommentListImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTDrawingImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTFillImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTFillsImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTFontImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTFontsImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTFontNameImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTFontSchemeImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTFontSizeImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTIntPropertyImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTLegacyDrawingImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTNumFmtsImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTPatternFillImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTPageMarginsImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTPaneImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTRowImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTSelectionImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTSheetImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTSheetDataImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTSheetDimensionImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTSheetFormatPrImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTSheetViewImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTSheetViewsImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTSheetsImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTSstImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTStylesheetImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTRstImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTWorkbookImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTWorkbookPrImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTWorksheetImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTXfImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.SstDocumentImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.StyleSheetDocumentImpl { *; }
#-keep class org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.STXstringImpl { *; }
#
#-keep class org.openxmlformats.schemas.officeDocument.x2006.customProperties.impl.CTPropertiesImpl { *; }
#-keep class org.openxmlformats.schemas.officeDocument.x2006.customProperties.impl.PropertiesDocumentImpl { *; }
#-keep class org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.impl.CTPropertiesImpl { *; }
#-keep class org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.impl.PropertiesDocumentImpl { *; }
#-keep class org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.impl.CTDrawingImpl { *; }
#-keep class org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.impl.CTMarkerImpl { *; }
#-keep class com.microsoft.schemas.office.office.impl.CTIdMapImpl { *; }
#-keep class com.microsoft.schemas.office.office.impl.CTShapeLayoutImpl { *; }
#-keep class com.microsoft.schemas.vml.impl.CTShadowImpl { *; }
#-keep class com.microsoft.schemas.vml.impl.CTFillImpl { *; }
#-keep class com.microsoft.schemas.vml.impl.CTPathImpl { *; }
#-keep class com.microsoft.schemas.vml.impl.CTShapeImpl { *; }
#-keep class com.microsoft.schemas.vml.impl.CTShapetypeImpl { *; }
#-keep class com.microsoft.schemas.vml.impl.CTStrokeImpl { *; }
#-keep class com.microsoft.schemas.vml.impl.CTTextboxImpl { *; }
#-keep class com.microsoft.schemas.office.excel.impl.CTClientDataImpl { *; }
#-keep class com.microsoft.schemas.office.excel.impl.STTrueFalseBlankImpl { *; }
