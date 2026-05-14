# PlacementPro — Design System
**Visual language, component rules, and layout guide for the Java Swing UI**

**Version:** 1.0 | **Date:** May 2026

---

## SECTION 0 — DESIGN PHILOSOPHY

PlacementPro is a role-separated desktop application. The visual language must do two jobs simultaneously: feel professional enough for a faculty placement officer reviewing analytics, and feel approachable enough for a student checking their application status between classes.

The guiding principle is **Status Clarity** — at any moment, a user should know exactly where something stands in the pipeline. Color carries meaning, not decoration. Every status (Applied, Shortlisted, Selected, Rejected) has one color and that color is never used for anything else.

### The Three Rules
1. **Color = State.** Never use the accent blue for a status chip. Never use the status green for a button.
2. **Role = Layout.** Each role gets a structurally different shell. Students get a card-based view. Faculty get a data-dense table view. Companies get an action-oriented sidebar layout.
3. **Small feedback > Big popups.** Toasts and inline labels over modal dialogs wherever possible. A modal is only used when a decision is irreversible (e.g., withdrawing an application).

---

## SECTION 1 — COLOR PALETTE

### Primary Brand Color
The placement domain is about opportunity and progress. The system accent color is **Royal Blue** — professional, trustworthy, not aggressive.

| Token | Hex | Usage |
|---|---|---|
| `COLOR_ACCENT` | `#1A56DB` | Primary buttons, active tab underline, link text |
| `COLOR_ACCENT_HOVER` | `#1648C0` | Button hover state |
| `COLOR_ACCENT_LIGHT` | `#EBF1FF` | Selected row highlight, card background tint |

### Background & Surface
| Token | Hex | Usage |
|---|---|---|
| `COLOR_BG` | `#F4F6FA` | Main window background |
| `COLOR_SURFACE` | `#FFFFFF` | Card backgrounds, panel backgrounds |
| `COLOR_BORDER` | `#E2E8F0` | Table borders, card outlines, dividers |
| `COLOR_SIDEBAR` | `#1E2A3B` | Left navigation sidebar (faculty + company) |
| `COLOR_SIDEBAR_TEXT` | `#A8B9CC` | Inactive sidebar nav items |
| `COLOR_SIDEBAR_ACTIVE` | `#FFFFFF` | Active sidebar nav item text |

### Typography Colors
| Token | Hex | Usage |
|---|---|---|
| `COLOR_TEXT_PRIMARY` | `#1A202C` | Headings, table data, primary labels |
| `COLOR_TEXT_SECONDARY` | `#64748B` | Subtitles, helper text, timestamps |
| `COLOR_TEXT_DISABLED` | `#A0AEC0` | Disabled fields, greyed-out eligibility text |

### Status Colors *(reserved — do not use for decoration)*
| Status | Token | Hex | Use exclusively for |
|---|---|---|---|
| Applied | `STATUS_APPLIED` | `#3B82F6` | Application chip, table cell |
| Shortlisted | `STATUS_SHORTLISTED` | `#F59E0B` | Application chip, table cell |
| Interview Scheduled | `STATUS_INTERVIEW` | `#8B5CF6` | Application chip, table cell |
| Selected / Placed | `STATUS_SELECTED` | `#10B981` | Application chip, Is_Placed badge |
| Rejected | `STATUS_REJECTED` | `#EF4444` | Application chip, table cell |
| Pending | `STATUS_PENDING` | `#94A3B8` | Interview result chip |

### Do / Don't
| ✅ Do | ❌ Don't |
|---|---|
| Use `STATUS_SELECTED` green only on "Selected" or "Placed" status | Use green for "Submit" buttons |
| Use `COLOR_ACCENT` blue for all call-to-action buttons | Use status amber for warning text not related to shortlisting |
| Keep sidebar `#1E2A3B` dark — it signals a management interface | Make the student dashboard dark — it should feel light and accessible |
| Use `COLOR_BG` as the outer window fill | Use white `#FFFFFF` as the window background — it causes eye strain |

---

## SECTION 2 — TYPOGRAPHY

All fonts are system-available on Windows and macOS. No external font loading needed in Java Swing.

| Role | Font | Size | Weight | Token |
|---|---|---|---|---|
| Screen title | Segoe UI / Dialog | 20px | Bold | `FONT_TITLE` |
| Section heading | Segoe UI / Dialog | 15px | Bold | `FONT_HEADING` |
| Body / form labels | Segoe UI / Dialog | 13px | Plain | `FONT_BODY` |
| Table content | Monospaced (for USN) / Dialog | 12px | Plain | `FONT_TABLE` |
| Small helper text | Segoe UI / Dialog | 11px | Plain | `FONT_SMALL` |
| Button text | Segoe UI / Dialog | 13px | Bold | `FONT_BUTTON` |
| Notification message | Dialog | 12px | Plain | `FONT_NOTIF` |

### Java Font Declaration Pattern
```java
// In a shared ThemeConstants.java file
public class ThemeConstants {
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);

    public static final Color COLOR_ACCENT       = new Color(0x1A56DB);
    public static final Color COLOR_BG           = new Color(0xF4F6FA);
    public static final Color COLOR_SURFACE      = new Color(0xFFFFFF);
    public static final Color COLOR_BORDER       = new Color(0xE2E8F0);
    public static final Color COLOR_TEXT_PRIMARY = new Color(0x1A202C);
    public static final Color COLOR_TEXT_SECONDARY = new Color(0x64748B);
    public static final Color COLOR_SIDEBAR      = new Color(0x1E2A3B);

    public static final Color STATUS_APPLIED      = new Color(0x3B82F6);
    public static final Color STATUS_SHORTLISTED  = new Color(0xF59E0B);
    public static final Color STATUS_INTERVIEW    = new Color(0x8B5CF6);
    public static final Color STATUS_SELECTED     = new Color(0x10B981);
    public static final Color STATUS_REJECTED     = new Color(0xEF4444);
}
```

---

## SECTION 3 — LAYOUT ARCHITECTURE

### Student Layout — Card Shell
```
┌─────────────────────────────────────────────────────┐
│  🎓 PlacementPro    [Amarnath Singh] [🔔 3] [Logout]│  ← TopBarPanel (45px, COLOR_SURFACE)
├─────────────────────────────────────────────────────┤
│  [ Browse Jobs ] [ My Applications ] [ Profile ]    │  ← TabNav (40px, COLOR_BG, blue underline on active)
├──────────┬──────────┬──────────┬────────────────────┤
│ Apps: 4  │ Short: 2 │ Intv: 1  │ Status: Placed ❌  │  ← QuickStats row (4 cards, COLOR_SURFACE)
├──────────┴──────────┴──────────┴────────────────────┤
│                                                     │
│              [Main Content Panel]                   │  ← Dynamic panel swapped by TabNav
│                                                     │
└─────────────────────────────────────────────────────┘
```

### Faculty / Admin Layout — Sidebar Shell
```
┌──────────┬──────────────────────────────────────────┐
│          │  [Screen Title]              [🔔 5]       │  ← TopBar
│ 📋       ├──────────────────────────────────────────┤
│ Students │                                          │
│          │                                          │
│ 🏢       │         [Main Content Panel]             │
│ Companies│                                          │
│          │                                          │
│ 📄 Apps  │                                          │
│          │                                          │
│ 📊 Report│                                          │
│          │                                          │
│ 🔔 Notif │                                          │
│          │                                          │
└──────────┴──────────────────────────────────────────┘
  ↑ SidebarPanel (180px, COLOR_SIDEBAR, dark)
```

### Company Layout — Action Sidebar Shell
Same as Faculty shell but sidebar items: My Jobs / Post Job / Applicants / Schedule Interview

---

## SECTION 4 — COMPONENT LIBRARY

### C1 — Primary Button
```java
public static JButton primaryButton(String label) {
    JButton btn = new JButton(label);
    btn.setBackground(ThemeConstants.COLOR_ACCENT);
    btn.setForeground(Color.WHITE);
    btn.setFont(ThemeConstants.FONT_BUTTON);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btn.setPreferredSize(new Dimension(140, 36));
    return btn;
}
```

### C2 — Danger Button (Reject / Withdraw)
Same as primary but `setBackground(new Color(0xEF4444))`.

### C3 — Ghost Button (Secondary action)
```java
btn.setBackground(Color.WHITE);
btn.setForeground(ThemeConstants.COLOR_ACCENT);
btn.setBorder(BorderFactory.createLineBorder(ThemeConstants.COLOR_ACCENT, 1));
```

### C4 — Status Chip (Application Status Label)
```java
public static JLabel statusChip(String status) {
    JLabel chip = new JLabel(" " + status + " ");
    chip.setOpaque(true);
    chip.setFont(new Font("Segoe UI", Font.BOLD, 11));
    chip.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

    switch (status) {
        case "Applied":             chip.setBackground(new Color(0xDBEAFE)); chip.setForeground(new Color(0x1D4ED8)); break;
        case "Shortlisted":         chip.setBackground(new Color(0xFEF3C7)); chip.setForeground(new Color(0xB45309)); break;
        case "Interview Scheduled": chip.setBackground(new Color(0xEDE9FE)); chip.setForeground(new Color(0x6D28D9)); break;
        case "Selected":            chip.setBackground(new Color(0xD1FAE5)); chip.setForeground(new Color(0x065F46)); break;
        case "Rejected":            chip.setBackground(new Color(0xFEE2E2)); chip.setForeground(new Color(0x991B1B)); break;
        default:                    chip.setBackground(new Color(0xF1F5F9)); chip.setForeground(new Color(0x475569));
    }
    return chip;
}
```
*Each status has a light background tint + dark foreground of the same hue. Never a bright filled chip — it's too visually aggressive in a table.*

### C5 — Quick Stat Card
```java
// A white rounded card with a label on top and a big number below
// Used in Student Dashboard top row: Applications / Shortlisted / Interviews / Status
JPanel card = new JPanel(new BorderLayout());
card.setBackground(Color.WHITE);
card.setBorder(BorderFactory.createCompoundBorder(
    BorderFactory.createLineBorder(new Color(0xE2E8F0), 1),
    BorderFactory.createEmptyBorder(16, 20, 16, 20)
));
JLabel title = new JLabel("Applications");
title.setFont(ThemeConstants.FONT_SMALL);
title.setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);
JLabel value = new JLabel("4");
value.setFont(new Font("Segoe UI", Font.BOLD, 28));
value.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
card.add(title, BorderLayout.NORTH);
card.add(value, BorderLayout.CENTER);
```

### C6 — Notification Card
```java
// Unread: left border 3px COLOR_ACCENT, background COLOR_ACCENT_LIGHT
// Read: no left border, background COLOR_SURFACE
// Timestamp in FONT_SMALL, COLOR_TEXT_SECONDARY
// Message in FONT_BODY, COLOR_TEXT_PRIMARY
```

### C7 — Styled JTable
```java
JTable table = new JTable(model);
table.setFont(ThemeConstants.FONT_BODY);
table.setRowHeight(38);
table.setShowGrid(false);
table.setIntercellSpacing(new Dimension(0, 0));
table.getTableHeader().setFont(ThemeConstants.FONT_HEADING);
table.getTableHeader().setBackground(new Color(0xF8FAFC));
table.getTableHeader().setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);
// Alternating row colors
table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
    public Component getTableCellRendererComponent(...) {
        super.getTableCellRendererComponent(...);
        setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE2E8F0)));
        return this;
    }
});
```

### C8 — Sidebar Nav Item
```java
// Active state: white text, left accent bar (3px COLOR_ACCENT), semi-transparent bg
// Inactive state: COLOR_SIDEBAR_TEXT, no border, transparent bg
// On hover: background rgba(255,255,255,0.08)
```

### C9 — Toast Notification (inline, bottom of panel)
```java
// A JLabel that appears at the bottom of the current panel for 2.5 seconds
// Green toast for success: bg #D1FAE5, text #065F46
// Red toast for error: bg #FEE2E2, text #991B1B
// Use a Swing Timer to hide it after 2500ms
// Never use JOptionPane for success confirmations — always use toast
```

### C10 — Notification Bell Badge
```java
// A JLabel with a small red circle overlay drawn in paintComponent
// Shows unread count. If count > 9, show "9+"
// Hidden when unread count is 0
```

---

## SECTION 5 — SPACING & SIZING RULES

| Context | Value |
|---|---|
| Window minimum size | 1024 × 680 px |
| Sidebar width | 180 px (fixed) |
| Top bar height | 45 px |
| Tab nav height | 40 px |
| Card padding (inner) | 20px all sides |
| Card gap (between cards) | 12px |
| Table row height | 38px |
| Button height | 36px |
| Button horizontal padding | 20px |
| Form field height | 32px |
| Section spacing (between sections in a panel) | 24px |
| Panel outer padding | 24px |

---

## SECTION 6 — SCREEN-BY-SCREEN COMPONENT MAP

| Screen | Shell | Key Components |
|---|---|---|
| Login | Centered card, no shell | Form fields, Primary button, 3 tabs |
| Student Dashboard | Card Shell | TopBar, TabNav, 4x QuickStatCard, dynamic panel |
| Browse Jobs | Card Shell → TabNav | Styled JTable, StatusChip (Eligible/Not), Primary/disabled button |
| My Applications | Card Shell → TabNav | Styled JTable with StatusChip column, expandable row |
| Notifications | Card Shell → TabNav | NotificationCard list, unread dot, timestamp |
| Company Dashboard | Sidebar Shell | Sidebar nav, jobs table, applicant count column |
| Applicant Management | Sidebar Shell | Styled JTable, status dropdown, Schedule/Reschedule button |
| Faculty Admin | Sidebar Shell | Tabs inside main panel, vw_application_pipeline data |
| Placement Report | Sidebar Shell | Two result tables, optional BarChart, Export CSV button |

---

## SECTION 7 — GEMINI API INTEGRATION DESIGN

PlacementPro uses the Google Gemini API (free tier via Google AI Studio) for one intelligent feature: **AI-assisted skill gap suggestion** displayed on the Student Dashboard.

### What it does
When a student views an eligible job they have not yet applied to, a "Get AI Advice" button calls Gemini with a prompt built from the student's skills/certifications and the job's requirements. Gemini returns 2–3 specific suggestions (e.g., "Add a Python certification" or "Practice SQL JOIN queries on HackerRank"). These appear as a suggestion card below the job listing.

### API Call Pattern (Java HttpClient)
```java
// GeminiClient.java
import java.net.http.*;
import java.net.URI;

public class GeminiClient {
    private static final String API_KEY = System.getenv("GEMINI_API_KEY"); // never hardcode
    private static final String ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    public static String getSkillAdvice(String studentSkills, String jobRequirements) throws Exception {
        String prompt = String.format(
            "A student is applying for a software job. " +
            "Student's current skills and certifications: %s. " +
            "Job requirements: %s. " +
            "Give exactly 3 short, specific suggestions to improve their chances. " +
            "Format as a numbered list. No preamble.",
            studentSkills, jobRequirements
        );

        String requestBody = "{\"contents\":[{\"parts\":[{\"text\":\"" +
            prompt.replace("\"", "\\\"") + "\"}]}]}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ENDPOINT))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the "text" field from response JSON
        // Use org.json or manual substring extraction for simplicity
        return extractText(response.body());
    }
}
```

### Where it appears in the UI
- **Browse Jobs panel** → row expanded → "💡 Get AI Advice" ghost button
- Response loads into a light yellow suggestion card (`#FFFBEB` bg, `#92400E` text)
- If API fails or rate-limited, show: "Suggestions unavailable right now — check your API key."
- Never block the UI thread — call Gemini in a `SwingWorker` background thread

### API Key Rule
Store in an environment variable `GEMINI_API_KEY`. Load with `System.getenv()`. Never commit to code. Add `config.properties.example` to the repo with placeholder.

---

## SECTION 8 — MYSQL WORKBENCH INTEGRATION NOTES

Since the DB is managed in MySQL Workbench, follow these conventions so the Java app and Workbench stay in sync.

| Convention | Rule |
|---|---|
| Schema name | `placementpro_db` — use this exact name in JDBC URL |
| JDBC URL | `jdbc:mysql://localhost:3306/placementpro_db?useSSL=false&serverTimezone=UTC` |
| Connection class | `com.mysql.cj.jdbc.Driver` |
| Trigger testing | Test `trg_interview_reschedule` in Workbench → run UPDATE → check NOTIFICATIONS table manually before wiring to Java |
| Stored procedure testing | Run `CALL sp_generate_placement_report();` in Workbench query tab — verify result sets before calling from Java |
| ENUMs | Define ENUM values in Workbench first → Java mirrors them as string constants |
| Views | Create views in Workbench → Java queries the view name, not the underlying tables |

---

## SECTION 9 — ICON & VISUAL INDICATOR MAP

| Element | Visual |
|---|---|
| Student role indicator | 🎓 emoji OR blue dot next to name |
| Faculty role indicator | 📋 emoji in sidebar header |
| Company role indicator | 🏢 emoji in sidebar header |
| Notification bell (unread) | 🔔 with red badge circle |
| Notification bell (all read) | 🔔 no badge |
| Placed status badge | ✅ green chip |
| Not placed | ⬜ gray chip |
| Eligible for job | ✅ label in green |
| Not eligible | ✗ label in red with reason text |
| Interview Scheduled | 📅 icon next to date |
| Rescheduled (notification) | 🔄 icon prefix on notification message |

*Use Java Unicode or emoji in JLabel text directly — no icon image files needed for the mini project.*

---

## SECTION 10 — ANTI-PATTERNS

| ❌ Never do this | ✅ Do this instead |
|---|---|
| Use `JOptionPane.showMessageDialog` for success | Use a Toast (`JLabel` with Timer) |
| Show all screens to all roles | Check role in session before panel load |
| Store password as plain text in DB | Store SHA-256 hash — compare hash to hash |
| Call Gemini API on the Swing Event Dispatch Thread | Use `SwingWorker` — UI freezes otherwise |
| Hardcode GEMINI_API_KEY in source | Load from `System.getenv("GEMINI_API_KEY")` |
| Use `SELECT *` in production queries | Always name columns explicitly |
| Write raw SQL strings scattered in UI classes | All SQL lives in DAO classes only |
| Use `JOptionPane` for role selection at login | Three tab buttons on the login panel |
| Mix business logic into JPanel subclasses | Keep panels as view-only; DAO handles DB |
| Let status chips use the same color for two statuses | Each status = one color, reserved permanently |

---

*PlacementPro Design System v1.0 — BAI402G Mini Project*
