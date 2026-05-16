package com.placementpro.utils;

import java.awt.Color;
import java.awt.Font;

public class ThemeConstants {
    // Fonts
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);

    // Primary & Background Colors (Updated to match Premium Dark Dashboard Reference)
    public static final Color COLOR_ACCENT       = new Color(0x3B82F6); // Vibrant Blue
    public static final Color COLOR_BG           = new Color(0x0F1117); // Deep Dark Background
    public static final Color COLOR_SURFACE      = new Color(0x161921); // Dark Surface Card (Glass-ish)
    public static final Color COLOR_BORDER       = new Color(0x242933); // Subtle border
    public static final Color COLOR_TEXT_PRIMARY = new Color(0xFFFFFF); // White text
    public static final Color COLOR_TEXT_SECONDARY = new Color(0x94A3B8); // Soft slate grey text
    public static final Color COLOR_SIDEBAR      = new Color(0x1A1C23); // Slightly darker sidebar

    // Status Colors (Adjusted for dark mode contrast)
    public static final Color STATUS_APPLIED      = new Color(0x60A5FA); // Light Blue
    public static final Color STATUS_SHORTLISTED  = new Color(0xFBBF24); // Amber
    public static final Color STATUS_INTERVIEW    = new Color(0xA78BFA); // Purple
    public static final Color STATUS_SELECTED     = new Color(0x34D399); // Green
    public static final Color STATUS_REJECTED     = new Color(0xF87171); // Red
}
