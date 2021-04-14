package net.readonly.utils;

import static net.readonly.utils.EmoteReference.BLUE_SMALL_MARKER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.LocaleUtils;

import net.readonly.core.modules.commands.i18n.I18nContext;

public class StringUtils {
    public static final Pattern SPLIT_PATTERN = Pattern.compile("\\s+");
    public static final String[] EMPTY_ARRAY = new String[0];
    
    public static final Pattern FORMAT_PATTERN = Pattern.compile("%\\d[$][,]?[a-zA-Z]");

    private static final char BACKTICK = '`';
    private static final char LEFT_TO_RIGHT_ISOLATE = '\u2066';
    private static final char POP_DIRECTIONAL_ISOLATE = '\u2069';
    
    private StringUtils() {}
    
    /**
     * Capitalizes the first letter of a string.
     *
     * @param s the string to capitalize
     * @return A string with the first letter capitalized.
     */
    public static String capitalize(String s) {
        if (s.length() == 0) {
            return s;
        }

        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public static Locale getLocaleFromLanguage(I18nContext context) {
        return getLocaleFromLanguage("en_US");
    }

    public static Locale getLocaleFromLanguage(String language) {
        if (language == null || language.isEmpty()) {
            return Locale.ENGLISH;
        }

        Locale locale = null;
        try {
            locale = LocaleUtils.toLocale(language);
        } catch (IllegalArgumentException ignore) { }

        if (locale == null) {
            locale = Locale.ENGLISH;
        }

        return locale;
    }
    
    public static String[] advancedSplitArgs(String args, int expectedArgs) {
        List<String> result = new ArrayList<>();

        var inBlock = false;
        var currentBlock = new StringBuilder();

        for (int i = 0; i < args.length(); i++) {
            var currentChar = args.charAt(i);
            if ((currentChar == '"' || currentChar == '“' || currentChar == '”') && (i == 0 || args.charAt(i - 1) != '\\' || args.charAt(i - 2) == '\\')) {
                inBlock = !inBlock;
            }

            if (inBlock) {
                currentBlock.append(currentChar);
            } else if (Character.isSpaceChar(currentChar)) {
                if (currentBlock.length() != 0) {
                    if (((currentBlock.charAt(0) == '"' || currentBlock.charAt(0) == '“') &&
                            (currentBlock.charAt(currentBlock.length() - 1) == '"' || currentBlock.charAt(currentBlock.length() - 1) == '”'))
                    ) {
                        currentBlock.deleteCharAt(0);
                        currentBlock.deleteCharAt(currentBlock.length() - 1);
                    }

                    result.add(advancedSplitArgsUnbox(currentBlock.toString()));
                    currentBlock = new StringBuilder();
                }
            } else {
                currentBlock.append(currentChar);
            }
        }

        if (currentBlock.length() != 0) {
            if ((currentBlock.charAt(0) == '"' || currentBlock.charAt(0) == '“') &&
                    (currentBlock.charAt(currentBlock.length() - 1) == '"' || currentBlock.charAt(currentBlock.length() - 1) == '”')
            ) {
                currentBlock.deleteCharAt(0);
                currentBlock.deleteCharAt(currentBlock.length() - 1);
            }

            result.add(advancedSplitArgsUnbox(currentBlock.toString()));
        }

        var raw = result.toArray(EMPTY_ARRAY);

        if (expectedArgs < 1) {
            return raw;
        }

        return normalizeArray(raw, expectedArgs);
    }

    public static String limit(String value, int length) {
        var buf = new StringBuilder(value);

        if (buf.length() > length) {
            buf.setLength(length - 3);
            buf.append("...");
        }

        return buf.toString();
    }

    /**
     * Normalize an {@link String} Array.
     *
     * @param raw          the String array to be normalized
     * @param expectedSize the final size of the Array.
     * @return {@link String}[] with the size of expectedArgs
     */
    public static String[] normalizeArray(String[] raw, int expectedSize) {
        var normalized = new String[expectedSize];

        Arrays.fill(normalized, "");
        for (int i = 0; i < normalized.length; i++) {
            if (i < raw.length && raw[i] != null && !raw[i].isEmpty()) {
                normalized[i] = raw[i];
            }
        }

        return normalized;
    }

    public static Map<String, String> parseArguments(String[] args) {
        Map<String, String> options = new HashMap<>();

        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i].charAt(0) == '-' || args[i].charAt(0) == '/') //This start with - or /
                {
                    args[i] = args[i].substring(1);
                    if (i + 1 >= args.length || args[i + 1].charAt(0) == '-' || args[i + 1].charAt(0) == '/') { //Next start with - (or last arg)
                        options.put(args[i], "null");
                    } else {
                        options.put(args[i], args[i + 1]);
                        i++;
                    }
                } else {
                    options.put(null, args[i]);
                }
            }

            return options;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * Enhanced {@link String#split(String, int)} with SPLIT_PATTERN as the Pattern used.
     *
     * @param args         the {@link String} to be split.
     * @param expectedArgs the size of the returned array of Non-null {@link String}s
     * @return a {@link String}[] with the size of expectedArgs
     */
    public static String[] splitArgs(String args, int expectedArgs) {
        var raw = SPLIT_PATTERN.split(args, expectedArgs);

        if (expectedArgs < 1) {
            return raw;
        }

        return normalizeArray(raw, expectedArgs);
    }

    private static String advancedSplitArgsUnbox(String s) {
        return s.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
    
    /**
     * Fixes the direction of the rendering of the text inside `inline codeblocks` to
     * be always left to right.
     *
     * @param src Source string.
     *
     * @return String with appropriate unicode direction modifier characters
     *         around code blocks.
     */
    @Nonnull
    @CheckReturnValue
    public static String fixInlineCodeblockDirection(@Nonnull String src) {
        if (!isRtl(src)) {
            return src;
        }
        var sb = new StringBuilder(src.length() + 8);
        var inside = false;

        for (var i = 0; i < src.length(); i++) {
            var ch = src.charAt(i);
            if (ch == BACKTICK) {
                if (inside) {
                    sb.append(BACKTICK)
                            .append(POP_DIRECTIONAL_ISOLATE);
                } else {
                    sb.append(LEFT_TO_RIGHT_ISOLATE)
                            .append(BACKTICK);
                }
                inside = !inside;
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static boolean isRtl(String string) {
        // Well why bother...
        if (string == null) {
            return false;
        }
        string = FORMAT_PATTERN.matcher(string).replaceAll("").trim();

        for (int i = 0, n = string.length(); i < n; ++i) {
            var d = Character.getDirectionality(string.charAt(i));
            switch (d) {
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE:
                    return true;

                case Character.DIRECTIONALITY_LEFT_TO_RIGHT:
                case Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING:
                case Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE:
                    return false;
            }
        }

        return false;
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (var entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
    }
    
    public static String formatDuration(long time) {
        if (time < 1000) {
            return "less than a second";
        }

        var days = TimeUnit.MILLISECONDS.toDays(time);
        var hours = TimeUnit.MILLISECONDS.toHours(time) % TimeUnit.DAYS.toHours(1);
        var minutes = TimeUnit.MILLISECONDS.toMinutes(time) % TimeUnit.HOURS.toMinutes(1);
        var seconds = TimeUnit.MILLISECONDS.toSeconds(time) % TimeUnit.MINUTES.toSeconds(1);

        var parts = Stream.of(
                formatUnit(days, "day"), formatUnit(hours, "hour"),
                formatUnit(minutes, "minute"), formatUnit(seconds, "second")
        ).filter(i -> !i.isEmpty()).iterator();

        var sb = new StringBuilder();
        var multiple = false;

        while(parts.hasNext()) {
            sb.append(parts.next());
            if (parts.hasNext()) {
                multiple = true;
                sb.append(", ");
            }
        }

        if (multiple) {
            var last = sb.lastIndexOf(", ");
            sb.replace(last, last + 2, " and ");
        }

        return sb.toString();
    }
    
    private static String formatUnit(long amount, String baseName) {
        if (amount == 0) {
            return "";
        }

        if (amount == 1) {
            return "1 " + baseName;
        }

        return amount + " " + baseName + "s";
    }
    
    public static String prettyDisplay(String header, String body) {
        return BLUE_SMALL_MARKER + "**" + header + "**: " + body;
    }

    public static String prettyDisplayLine(String header, String body) {
        return BLUE_SMALL_MARKER + "**" + header + "**:\n" + body;
    }

    private static String formatMemoryHelper(long bytes, long unitSize, String unit) {
        if (bytes % unitSize == 0) {
            return String.format("%d %s", bytes / unitSize, unit);
        }

        return String.format("%.1f %s", bytes / (double) unitSize, unit);
    }

    public static String formatMemoryUsage(long used, long total) {
        return String.format("%s/%s", formatMemoryAmount(used), formatMemoryAmount(total));
    }

    public static String formatMemoryAmount(long bytes) {
        if (bytes > 1L << 30) {
            return formatMemoryHelper(bytes, 1L << 30, "GiB");
        }

        if (bytes > 1L << 20) {
            return formatMemoryHelper(bytes, 1L << 20, "MiB");
        }

        if (bytes > 1L << 10) {
            return formatMemoryHelper(bytes, 1L << 10, "KiB");
        }

        return String.format("%d B", bytes);
    }
}
