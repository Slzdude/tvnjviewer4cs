package com.glavsoft.viewer.swing;

import com.glavsoft.rfb.encoding.EncodingType;
import com.glavsoft.rfb.protocol.LocalPointer;
import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.utils.Strings;
import com.glavsoft.viewer.cli.Parser;

import javax.swing.JApplet;

public class ParametersHandler {
    public static final String ARG_LOCAL_POINTER = "LocalPointer";
    public static final String ARG_SCALING_FACTOR = "ScalingFactor";
    public static final String ARG_COLOR_DEPTH = "ColorDepth";
    public static final String ARG_JPEG_IMAGE_QUALITY = "JpegImageQuality";
    public static final String ARG_COMPRESSION_LEVEL = "CompressionLevel";
    public static final String ARG_ENCODING = "Encoding";
    public static final String ARG_SHARE_DESKTOP = "ShareDesktop";
    public static final String ARG_ALLOW_COPY_RECT = "AllowCopyRect";
    public static final String ARG_VIEW_ONLY = "ViewOnly";
    public static final String ARG_SHOW_CONTROLS = "ShowControls";
    public static final String ARG_OPEN_NEW_WINDOW = "OpenNewWindow";
    public static final String ARG_PASSWORD = "password";
    public static final String ARG_PORT = "port";
    public static final String ARG_HOST = "host";
    public static final String ARG_HELP = "help";
    public static final String ARG_CONVERT_TO_ASCII = "ConvertToASCII";
    public static final String ARG_ALLOW_CLIPBOARD_TRANSFER = "AllowClipboardTransfer";
    public static final String ARG_REMOTE_CHARSET = "RemoteCharset";
    public static final String ARG_SSH_HOST = "sshHost";
    public static final String ARG_SSH_USER = "sshUser";
    public static final String ARG_SSH_PORT = "sshPort";
    public static boolean showControls;
    public static boolean isSeparateFrame;

    public ParametersHandler() {
    }

    public static void completeParserOptions(Parser parser) {
        parser.addOption("help", (String) null, "Print this help.");
        parser.addOption("host", (String) null, "Server host name.");
        parser.addOption("port", "0", "Port number.");
        parser.addOption("password", (String) null, "Password to the server.");
        parser.addOption("ShowControls", (String) null, "Set to \"No\" if you want to get rid of that button panel at the top. Default: \"Yes\".");
        parser.addOption("ViewOnly", (String) null, "When set to \"Yes\", then all keyboard and mouse events in the desktop window will be silently ignored and will not be passed to the remote side. Default: \"No\".");
        parser.addOption("AllowClipboardTransfer", (String) null, "When set to \"Yes\", transfer of clipboard contents is allowed. Default: \"Yes\".");
        parser.addOption("RemoteCharset", (String) null, "Charset encoding is used on remote system. Use this option to specify character encoding will be used for encoding clipboard text content to. Default value: local system default character encoding. Set the value to 'standard' for using 'Latin-1' charset which is only specified by rfb standard for clipboard transfers.");
        parser.addOption("ShareDesktop", (String) null, "Share the connection with other clients on the same VNC server. The exact behaviour in each case depends on the server configuration. Default: \"Yes\".");
        parser.addOption("AllowCopyRect", (String) null, "The \"CopyRect\" encoding saves bandwidth and drawing time when parts of the remote screen are moving around. Most likely, you don't want to change this setting. Default: \"Yes\".");
        parser.addOption("Encoding", (String) null, "The preferred encoding. Possible values: \"Tight\", \"Hextile\", \"ZRLE\", and \"Raw\". Default: \"Tight\".");
        parser.addOption("CompressionLevel", (String) null, "Use specified compression level for \"Tight\" and \"Zlib\" encodings. Values: 1-9. Level 1 uses minimum of CPU time on the server but achieves weak compression ratios. Level 9 offers best compression but may be slow.");
        parser.addOption("JpegImageQuality", (String) null, "Use the specified image quality level in \"Tight\" encoding. Values: 1-9, Lossless. Default value: " + String.valueOf(5) + ". To prevent server of using " + "lossy JPEG compression in \"Tight\" encoding, use \"Lossless\" value here.");
        parser.addOption("LocalPointer", (String) null, "Possible values: on/yes/true (draw pointer locally), off/no/false (let server draw pointer), hide). Default: \"On\".");
        parser.addOption("ConvertToASCII", (String) null, "Whether to convert keyboard input to ASCII ignoring locale. Possible values: yes/true, no/false). Default: \"No\".");
        parser.addOption("ColorDepth", (String) null, "Bits per pixel color format. Possible values: 3 (for 8 colors), 6 (64 colors), 8 (256 colors), 16 (65 536 colors), 24 (16 777 216 colors), 32 (same as 24).");
        parser.addOption("ScalingFactor", (String) null, "Scale local representation of the remote desktop on startup. The value is interpreted as scaling factor in percents. The default value of 100% corresponds to the original framebuffer size.");
        parser.addOption("sshHost", (String) null, "SSH host name.");
        parser.addOption("sshPort", "0", "SSH port number. When empty, standard SSH port number (22) is used.");
        parser.addOption("sshHost", (String) null, "SSH user name.");
    }

    public static void completeSettingsFromCLI(final Parser parser, ConnectionParams connectionParams, ProtocolSettings rfbSettings, UiSettings uiSettings) {
        completeSettings(new ParametersHandler.ParamsRetriever() {
            public String getParamByName(String name) {
                return parser.getValueFor(name);
            }
        }, connectionParams, rfbSettings, uiSettings);
        if (!Strings.isTrimmedEmpty(connectionParams.hostName)) {
            splitConnectionParams(connectionParams, connectionParams.hostName);
        }

        if (parser.isSetPlainOptions()) {
            splitConnectionParams(connectionParams, parser.getPlainOptionAt(0));
            if (parser.getPlainOptionsNumber() > 1) {
                connectionParams.parseRfbPortNumber(parser.getPlainOptionAt(1));
            }
        }

    }

    public static void splitConnectionParams(ConnectionParams connectionParams, String host) {
        int indexOfColon = host.indexOf(58);
        if (indexOfColon > 0) {
            String[] splitted = host.split(":");
            connectionParams.hostName = splitted[0];
            if (splitted.length > 1) {
                connectionParams.parseRfbPortNumber(splitted[splitted.length - 1]);
            }
        } else {
            connectionParams.hostName = host;
        }

    }

    private static void completeSettings(ParametersHandler.ParamsRetriever pr, ConnectionParams connectionParams, ProtocolSettings rfbSettings, UiSettings uiSettings) {
        String hostName = pr.getParamByName("host");
        String portNumber = pr.getParamByName("port");
        String showControlsParam = pr.getParamByName("ShowControls");
        String viewOnlyParam = pr.getParamByName("ViewOnly");
        String allowClipboardTransfer = pr.getParamByName("AllowClipboardTransfer");
        String remoteCharsetName = pr.getParamByName("RemoteCharset");
        String allowCopyRectParam = pr.getParamByName("AllowCopyRect");
        String shareDesktopParam = pr.getParamByName("ShareDesktop");
        String encodingParam = pr.getParamByName("Encoding");
        String compressionLevelParam = pr.getParamByName("CompressionLevel");
        String jpegQualityParam = pr.getParamByName("JpegImageQuality");
        String colorDepthParam = pr.getParamByName("ColorDepth");
        String scaleFactorParam = pr.getParamByName("ScalingFactor");
        String localPointerParam = pr.getParamByName("LocalPointer");
        String convertToAsciiParam = pr.getParamByName("ConvertToASCII");
        String sshHostNameParam = pr.getParamByName("sshHost");
        String sshPortNumberParam = pr.getParamByName("sshPort");
        String sshUserNameParam = pr.getParamByName("sshUser");
        connectionParams.hostName = hostName;
        connectionParams.parseRfbPortNumber(portNumber);
        connectionParams.sshHostName = sshHostNameParam;
        connectionParams.setUseSsh(!Strings.isTrimmedEmpty(sshHostNameParam));
        connectionParams.parseSshPortNumber(sshPortNumberParam);
        connectionParams.sshUserName = sshUserNameParam;
        showControls = parseBooleanOrDefault(showControlsParam, true);
        rfbSettings.setViewOnly(parseBooleanOrDefault(viewOnlyParam, false));
        rfbSettings.setAllowClipboardTransfer(parseBooleanOrDefault(allowClipboardTransfer, true));
        rfbSettings.setRemoteCharsetName(remoteCharsetName);
        rfbSettings.setAllowCopyRect(parseBooleanOrDefault(allowCopyRectParam, true));
        rfbSettings.setSharedFlag(parseBooleanOrDefault(shareDesktopParam, true));
        rfbSettings.setConvertToAscii(parseBooleanOrDefault(convertToAsciiParam, false));
        if (EncodingType.TIGHT.getName().equalsIgnoreCase(encodingParam)) {
            rfbSettings.setPreferredEncoding(EncodingType.TIGHT);
        }

        if (EncodingType.HEXTILE.getName().equalsIgnoreCase(encodingParam)) {
            rfbSettings.setPreferredEncoding(EncodingType.HEXTILE);
        }

        if (EncodingType.ZRLE.getName().equalsIgnoreCase(encodingParam)) {
            rfbSettings.setPreferredEncoding(EncodingType.ZRLE);
        }

        if (EncodingType.RAW_ENCODING.getName().equalsIgnoreCase(encodingParam)) {
            rfbSettings.setPreferredEncoding(EncodingType.RAW_ENCODING);
        }

        int scaleFactor;
        try {
            scaleFactor = Integer.parseInt(compressionLevelParam);
            if (scaleFactor > 0 && scaleFactor <= 9) {
                rfbSettings.setCompressionLevel(scaleFactor);
            }
        } catch (NumberFormatException var25) {
        }

        try {
            scaleFactor = Integer.parseInt(jpegQualityParam);
            if (scaleFactor > 0 && scaleFactor <= 9) {
                rfbSettings.setJpegQuality(scaleFactor);
            }
        } catch (NumberFormatException var26) {
            if ("lossless".equalsIgnoreCase(jpegQualityParam)) {
                rfbSettings.setJpegQuality(-Math.abs(rfbSettings.getJpegQuality()));
            }
        }

        try {
            scaleFactor = Integer.parseInt(colorDepthParam);
            rfbSettings.setBitsPerPixel(scaleFactor);
        } catch (NumberFormatException var24) {
        }

        if (scaleFactorParam != null) {
            try {
                scaleFactor = Integer.parseInt(scaleFactorParam.replaceAll("\\D", ""));
                if (scaleFactor >= 10 && scaleFactor <= 200) {
                    uiSettings.setScalePercent((double) scaleFactor);
                }
            } catch (NumberFormatException var23) {
            }
        }

        if ("on".equalsIgnoreCase(localPointerParam) || "true".equalsIgnoreCase(localPointerParam) || "yes".equalsIgnoreCase(localPointerParam)) {
            rfbSettings.setMouseCursorTrack(LocalPointer.ON);
        }

        if ("off".equalsIgnoreCase(localPointerParam) || "no".equalsIgnoreCase(localPointerParam) || "false".equalsIgnoreCase(localPointerParam)) {
            rfbSettings.setMouseCursorTrack(LocalPointer.OFF);
        }

        if ("hide".equalsIgnoreCase(localPointerParam) || "hidden".equalsIgnoreCase(localPointerParam)) {
            rfbSettings.setMouseCursorTrack(LocalPointer.HIDE);
        }

    }

    static boolean parseBooleanOrDefault(String param, boolean defaultValue) {
        return defaultValue ? !"no".equalsIgnoreCase(param) && !"false".equalsIgnoreCase(param) : "yes".equalsIgnoreCase(param) || "true".equalsIgnoreCase(param);
    }

    public static void completeSettingsFromApplet(final JApplet applet, ConnectionParams connectionParams, ProtocolSettings rfbSettings, UiSettings uiSettings) {
        completeSettings(new ParametersHandler.ParamsRetriever() {
            public String getParamByName(String name) {
                String value = applet.getParameter(name);
                if ("host".equals(name) && Strings.isTrimmedEmpty(value)) {
                    value = applet.getCodeBase().getHost();
                }

                return value;
            }
        }, connectionParams, rfbSettings, uiSettings);
        isSeparateFrame = parseBooleanOrDefault(applet.getParameter("OpenNewWindow"), true);
    }

    interface ParamsRetriever {
        String getParamByName(String var1);
    }
}
