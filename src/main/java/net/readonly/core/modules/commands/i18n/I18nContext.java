package net.readonly.core.modules.commands.i18n;

import net.readonly.utils.cmds.I18n;

public class I18nContext {

    public I18nContext() {
    }

    public String get(String s) {
        I18n context = I18n.en_US();
        return context.get(s);
    }

    public String withRoot(String root, String s) {
        I18n context = I18n.en_US();
        return context.withRoot(root, s);
    }
}
