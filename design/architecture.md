### Security
- create oauth 2.0 Client ID in google cloud console
- use sha-1 fingerprint + package name
- run ./gradlew signingReport in AS terminal 
- use created Client ID
### App
- single app. no need for external hosted backend 
- should create a background process that executes rules 
### Basic vibecoded ui
- think of it like a console. it should not be user friendly - on a contrary, should be hostile
- login(connect account) and logout buttons should be present
- login - on app open - find gmail account or redirect to a login page for one? 
- rules tab? contains scrollable list section, meta info when rule selected section, buttons add, suspend|resume, remove
- notification status? like in flud+


This design adheres to standard Material Design principles (perfect for Jetpack Compose) to create a utilitarian and reliable automation tool dashboard. Here’s a breakdown of the key areas and functionality shown in the interface:

### Design Preview Breakdown

1.  **Top AppBar:** This bar contains the app name ("NotiSheet Automation") and crucial actions:
    * **Search:** For quickly finding specific rules if the list gets long.
    * **Settings:** Accessible place to manage global configurations (such as OAuth credentials or default sheet settings).

2.  **The Rule List:** A clean, vertical scrolling list of distinct "Rule Cards" or list items. This view represents the core management hub. You can easily view all rules at once and see their current configuration.

3.  **Core Component Controls (Per Rule):** These buttons make rule management intuitive directly on each card:
    * **Add Button:** To maintain the cleanest main view, the addition functionality is represented by a green **Floating Action Button (FAB)** in the bottom right corner with a large "+" icon.
    * **Suspend/Active Switch:** The green Material **toggle switch** is highly visible. It lets the user instantly pause or resume a rule. (The "Gmail" entry shows how a suspended rule looks—greyed out and explicitly marked as "(Paused)").
    * **Remove (Trash Icon):** Each card includes a standard trash can icon. Clicking this icon should prompt a simple confirmation dialog before deleting the rule from the local Room database.

4.  **Meta Info Panel (Selection State):** When a user taps a rule (in this preview, the **"Telegram: New Orders"** rule), it visualizes that selection. In a real Compose app, this can be achieved by expanding the card to reveal deeper details or opening a secondary view. Here, we see a panel slide open within the card, showing crucial background execution data:
    * **Rule ID:** The unique identifier (great for debugging).
    * **Date Created:** Essential context for longer-running automations.
    * **Google Sheet ID:** The specific destination spreadsheet linked to the notification.
    * **Last Append:** The most vital meta info. It shows *when* the rule last successfully ran (e.g., "4m ago") and the status of that operation ("Success"). This helps confirm the automation is working correctly without checking the spreadsheet itself.