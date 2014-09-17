package org.overlord.sramp.shell;

import java.util.Locale;

import org.overlord.sramp.shell.i18n.Messages;

public class SrampShell extends AbstractSrampShell {
    /**
     * Main entry point.
     *
     * @param args
     */
    public static void main(String[] args) {
        String locale_str = System.getProperty(LOCALE_PROPERTY);
        if (locale_str != null) {
            String lang = null;
            String region = null;
            String[] lsplit = locale_str.split("_"); //$NON-NLS-1$
            if (lsplit.length > 0) {
                lang = lsplit[0];
            }
            if (lsplit.length > 1) {
                region = lsplit[1];
            }
            if (lang != null && region != null) {
                Locale.setDefault(new Locale(lang, region));
            } else if (lang != null) {
                Locale.setDefault(new Locale(lang));
            }
        }

        final SrampShell shell = new SrampShell();
        Thread shutdownHook = new Thread(new Runnable() {
            @Override
            public void run() {
                shell.shutdown();
            }
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        try {
            shell.run(args);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.out.println(Messages.i18n.format("Shell.EXITING")); //$NON-NLS-1$
        }
    }

    @Override
    protected void exit() {
        System.exit(1);
    }
}
