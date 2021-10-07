package net.readonly.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.readonly.options.base.Option;

public class OptionController {
	private static List<Option> optionList = new ArrayList<>();
	private static Map<String, Option> map = new HashMap<>();

	public static void addOption(Option option) {
		OptionController.optionList.add(option);
		map.put(option.getName(), option);
	}
	
    public static List<Option> getAvaliableOptions() {
        return OptionController.optionList;
    }
    
	public static Map<String, Option> getMap() {
		return map;
	}
}
