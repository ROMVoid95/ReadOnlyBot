package net.readonly.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

public class DateFormatting {

	private static final Pattern pattern = Pattern.compile("\\d+?[a-zA-Z]");

	public static OffsetDateTime epochToDate(long epoch) {
		return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault());
	}

	public static String formatDate(OffsetDateTime date) {
		return date.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
	}

	public static String formatHours(OffsetDateTime date, String locale) {
		return date
				.format(DateTimeFormatter.ofPattern("HH:mm:ss").withLocale(StringUtils.getLocaleFromLanguage(locale)));
	}

	public static String formatHours(OffsetDateTime date, String zone, String locale) {
		return date.format(DateTimeFormatter.ofPattern("HH:mm:ss").withZone(timezoneToZoneID(zone))
				.withLocale(StringUtils.getLocaleFromLanguage(locale)));
	}

	public static String formatDate(long epoch, String lang) {
		return epochToDate(epoch).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
				.withLocale(StringUtils.getLocaleFromLanguage(lang)));
	}

	public static String formatDate(OffsetDateTime date, String lang) {
		return date.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
				.withLocale(StringUtils.getLocaleFromLanguage(lang)));
	}

	public static String formatDate(LocalDateTime date, String lang) {
		return date.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
				.withLocale(StringUtils.getLocaleFromLanguage(lang)));
	}

	public static String formatDuration(long time) {
		if (time < 1000) {
			return "less than a second";
		}

		var days = TimeUnit.MILLISECONDS.toDays(time);
		var hours = TimeUnit.MILLISECONDS.toHours(time) % TimeUnit.DAYS.toHours(1);
		var minutes = TimeUnit.MILLISECONDS.toMinutes(time) % TimeUnit.HOURS.toMinutes(1);
		var seconds = TimeUnit.MILLISECONDS.toSeconds(time) % TimeUnit.MINUTES.toSeconds(1);

		var parts = Stream.of(formatUnit(days, "day"), formatUnit(hours, "hour"), formatUnit(minutes, "minute"),
				formatUnit(seconds, "second")).filter(i -> !i.isEmpty()).iterator();

		var sb = new StringBuilder();
		var multiple = false;

		while (parts.hasNext()) {
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

	public static long parseTime(String toParse) {
		toParse = toParse.toLowerCase();
		long[] time = { 0 };

		iterate(pattern.matcher(toParse)).forEach(string -> {
			var l = string.substring(0, string.length() - 1);
			var unit = switch (string.charAt(string.length() - 1)) {
			case 'm' -> TimeUnit.MINUTES;
			case 'h' -> TimeUnit.HOURS;
			case 'd' -> TimeUnit.DAYS;
			default -> TimeUnit.SECONDS;
			};

			time[0] += unit.toMillis(Long.parseLong(l));
		});

		return time[0];
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

	private static Iterable<String> iterate(Matcher matcher) {
		return new Iterable<>() {
			@NotNull
			@Override
			public Iterator<String> iterator() {
				return new Iterator<>() {
					@Override
					public boolean hasNext() {
						return matcher.find();
					}

					@Override
					public String next() {
						return matcher.group();
					}
				};
			}

			@Override
			public void forEach(Consumer<? super String> action) {
				while (matcher.find()) {
					action.accept(matcher.group());
				}
			}
		};
	}

	public static boolean isValidTimeZone(final String timeZone) {
		if (timeZone.equals("GMT") || timeZone.equals("UTC")) {
			return true;
		} else {
			String id = TimeZone.getTimeZone(timeZone).getID();
			return !id.equals("GMT");
		}
	}

	public static ZoneId timezoneToZoneID(final String timeZone) {
		if (timeZone == null) {
			return ZoneId.systemDefault();
		}

		return TimeZone.getTimeZone(timeZone).toZoneId();
	}
}
