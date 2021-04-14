package net.readonly.utils.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex116 implements IRegexPattern<Regex116> {

	private final Pattern REGEX = Pattern.compile("\\b(gc|galacticraft)|(1.16|116|1.16.5)|(update|port|ported)\\b");
	
	private String contentToCheck;
	private Boolean hasGroupOne; // name
	private String groupOne;

	private Boolean hasGroupTwo; // version
	private String groupTwo;

	private Boolean hasGroupThree; // uhhh, what people somtimes put
	private String groupThree;

	private Matcher matcher;
	
	@Override
	public Regex116 check(String toCheck) {
		this.contentToCheck = toCheck;
		if (!toCheck.isEmpty()) {
			checkForAll();
		}
		return this;
	}

	public final String getContentToCheck() {
		return contentToCheck;
	}

	private void checkForAll() {
		matcher = REGEX.matcher(getContentToCheck());

		StringBuilder b1 = new StringBuilder();
		StringBuilder b2 = new StringBuilder();
		StringBuilder b3 = new StringBuilder();
		while(check()) {
			b1.append(matcher.group(1));
			b2.append(matcher.group(2));
    		b3.append(matcher.group(3));
		}
		groupOne = b1.toString().replace("null", "");
		groupTwo = b2.toString().replace("null", "");
		groupThree = b3.toString().replace("null", "");
		
		hasGroupOne = groupOne.isEmpty() ? false : true;
		hasGroupTwo = groupTwo.isEmpty() ? false : true;
		hasGroupThree = groupThree.isEmpty() ? false : true;
	}

	public Boolean check() {
		return matcher.find();
	}

	public boolean hasOne() {
		return hasGroupOne;
	}

	public boolean hasTwo() {
		return hasGroupTwo;
	}

	public boolean hasThree() {
		return hasGroupThree;
	}

	public String getOne() {
		return groupOne;
	}

	public String getTwo() {
		return groupTwo;
	}

	public String getThree() {
		return groupThree;
	}
}
