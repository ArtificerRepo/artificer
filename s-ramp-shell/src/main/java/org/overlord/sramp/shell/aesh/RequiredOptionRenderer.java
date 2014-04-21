package org.overlord.sramp.shell.aesh;

import org.jboss.aesh.cl.renderer.OptionRenderer;
import org.jboss.aesh.terminal.CharacterType;
import org.jboss.aesh.terminal.Color;
import org.jboss.aesh.terminal.TerminalColor;
import org.jboss.aesh.terminal.TerminalTextStyle;

public class RequiredOptionRenderer implements OptionRenderer {

    private static TerminalTextStyle style = new TerminalTextStyle(CharacterType.BOLD);
    private static TerminalColor color = new TerminalColor(42, Color.RED);

    @Override
    public TerminalColor getColor() {
        // TODO Auto-generated method stub
        return color;
    }

    @Override
    public TerminalTextStyle getTextType() {
        // TODO Auto-generated method stub
        return style;
    }

}
