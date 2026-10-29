# PDFBox loads these implementations by class name. Preserve only the reflective entry points.
-keep class com.tom_roush.pdfbox.filter.** { *; }
-keep class com.tom_roush.pdfbox.pdmodel.font.PDType1Font { *; }
-dontwarn java.awt.**
-dontwarn javax.imageio.**
-dontwarn javax.xml.bind.**
# Optional JPEG-2000 plugin is not included in PDFBox Android's standard distribution.
-dontwarn com.gemalto.jp2.JP2Decoder
-dontwarn com.gemalto.jp2.JP2Encoder
