package com.glavsoft.rfb.encoding;

import java.util.LinkedHashSet;

public enum EncodingType {
    RAW_ENCODING(0, "Raw"),
    COPY_RECT(1, "CopyRect"),
    RRE(2, "RRE"),
    HEXTILE(5, "Hextile"),
    ZLIB(6, "ZLib"),
    TIGHT(7, "Tight"),
    ZRLE(16, "ZRLE"),
    RICH_CURSOR(-239, "RichCursor"),
    DESKTOP_SIZE(-223, "DesctopSize"),
    CURSOR_POS(-232, "CursorPos"),
    COMPRESS_LEVEL_0(-256, "CompressionLevel0"),
    COMPRESS_LEVEL_1(-255, "CompressionLevel1"),
    COMPRESS_LEVEL_2(-254, "CompressionLevel2"),
    COMPRESS_LEVEL_3(-253, "CompressionLevel3"),
    COMPRESS_LEVEL_4(-252, "CompressionLevel4"),
    COMPRESS_LEVEL_5(-251, "CompressionLevel5"),
    COMPRESS_LEVEL_6(-250, "CompressionLevel6"),
    COMPRESS_LEVEL_7(-249, "CompressionLevel7"),
    COMPRESS_LEVEL_8(-248, "CompressionLevel8"),
    COMPRESS_LEVEL_9(-247, "CompressionLevel9"),
    JPEG_QUALITY_LEVEL_0(-32, "JpegQualityLevel0"),
    JPEG_QUALITY_LEVEL_1(-31, "JpegQualityLevel1"),
    JPEG_QUALITY_LEVEL_2(-30, "JpegQualityLevel2"),
    JPEG_QUALITY_LEVEL_3(-29, "JpegQualityLevel3"),
    JPEG_QUALITY_LEVEL_4(-28, "JpegQualityLevel4"),
    JPEG_QUALITY_LEVEL_5(-27, "JpegQualityLevel5"),
    JPEG_QUALITY_LEVEL_6(-26, "JpegQualityLevel6"),
    JPEG_QUALITY_LEVEL_7(-25, "JpegQualityLevel7"),
    JPEG_QUALITY_LEVEL_8(-24, "JpegQualityLevel8"),
    JPEG_QUALITY_LEVEL_9(-23, "JpegQualityLevel9");

    private int id;
    private final String name;
    public static LinkedHashSet<EncodingType> ordinaryEncodings = new LinkedHashSet<>();
    public static LinkedHashSet<EncodingType> pseudoEncodings;
    public static LinkedHashSet<EncodingType> compressionEncodings;

    private EncodingType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static EncodingType byId(int id) {
        EncodingType[] arr = values();
        for (EncodingType type : arr) {
            if (type.getId() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported encoding id: " + id);
    }

    static {
        ordinaryEncodings.add(TIGHT);
        ordinaryEncodings.add(HEXTILE);
        ordinaryEncodings.add(ZRLE);
        ordinaryEncodings.add(ZLIB);
        ordinaryEncodings.add(RRE);
        ordinaryEncodings.add(COPY_RECT);
        pseudoEncodings = new LinkedHashSet<>();
        pseudoEncodings.add(RICH_CURSOR);
        pseudoEncodings.add(CURSOR_POS);
        pseudoEncodings.add(DESKTOP_SIZE);
        compressionEncodings = new LinkedHashSet<>();
        compressionEncodings.add(COMPRESS_LEVEL_0);
        compressionEncodings.add(JPEG_QUALITY_LEVEL_0);
    }
}
