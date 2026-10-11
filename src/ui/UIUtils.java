package ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Utility class providing consistent, high-contrast button creation
 * and styling across the UniFix user interface.
 * Bypasses OS-specific button renderers (such as WindowsButtonUI) to ensure
 * reliable contrast and readable labels on all platforms.
 */
public final class UIUtils {

    // Palette constants
    public static final Color COLOR_PRIMARY = new Color(30, 100, 200);
    public static final Color COLOR_PRIMARY_HOVER = new Color(22, 82, 170);

    public static final Color COLOR_SUCCESS = new Color(25, 135, 84);
    public static final Color COLOR_SUCCESS_HOVER = new Color(20, 110, 68);

    public static final Color COLOR_DANGER = new Color(220, 53, 69);
    public static final Color COLOR_DANGER_HOVER = new Color(185, 38, 52);

    public static final Color COLOR_SECONDARY_BG = new Color(232, 236, 242);
    public static final Color COLOR_SECONDARY_HOVER = new Color(215, 222, 230);
    public static final Color COLOR_SECONDARY_FG = new Color(33, 37, 41);
    public static final Color COLOR_SECONDARY_BORDER = new Color(195, 205, 218);

    private UIUtils() {}

    /**
     * Creates a fully styled JButton with guaranteed background and foreground rendering,
     * mouse hover effects, and hand cursor.
     */
    public static JButton createStyledButton(String text, Color bg, Color fg, Color hoverBg, Font font, Dimension size) {
        JButton button = new JButton(text);
        button.setUI(new BasicButtonUI()); // Ensures standard custom rendering across all L&Fs
        button.setBackground(bg);
        button.setForeground(fg);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (font != null) {
            button.setFont(font);
        } else {
            button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        }

        if (size != null) {
            button.setPreferredSize(size);
        }

        Border paddingBorder = BorderFactory.createEmptyBorder(6, 14, 6, 14);
        if (fg.equals(COLOR_SECONDARY_FG)) {
            Border lineBorder = BorderFactory.createLineBorder(COLOR_SECONDARY_BORDER, 1);
            button.setBorder(BorderFactory.createCompoundBorder(lineBorder, paddingBorder));
            button.setBorderPainted(true);
        } else {
            button.setBorder(paddingBorder);
            button.setBorderPainted(false);
        }

        // Interactive hover feedback
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverBg != null ? hoverBg : bg.darker());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
            }
        });

        return button;
    }

    /**
     * Primary action button with solid dark blue background and crisp white text.
     */
    public static JButton createPrimaryButton(String text, Dimension size) {
        return createStyledButton(
            text,
            COLOR_PRIMARY,
            Color.WHITE,
            COLOR_PRIMARY_HOVER,
            new Font("Segoe UI", Font.BOLD, 13),
            size
        );
    }

    /**
     * Success action button with deep green background and crisp white text.
     */
    public static JButton createSuccessButton(String text, Dimension size) {
        return createStyledButton(
            text,
            COLOR_SUCCESS,
            Color.WHITE,
            COLOR_SUCCESS_HOVER,
            new Font("Segoe UI", Font.BOLD, 13),
            size
        );
    }

    /**
     * Danger / Logout action button with red background and crisp white text.
     */
    public static JButton createDangerButton(String text, Dimension size) {
        return createStyledButton(
            text,
            COLOR_DANGER,
            Color.WHITE,
            COLOR_DANGER_HOVER,
            new Font("Segoe UI", Font.BOLD, 12),
            size
        );
    }

    /**
     * Secondary / Neutral action button with light grey background and dark charcoal text.
     */
    public static JButton createSecondaryButton(String text, Dimension size) {
        return createStyledButton(
            text,
            COLOR_SECONDARY_BG,
            COLOR_SECONDARY_FG,
            COLOR_SECONDARY_HOVER,
            new Font("Segoe UI", Font.PLAIN, 12),
            size
        );
    }
}
