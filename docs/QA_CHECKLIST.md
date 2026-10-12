# QA checklist

Run on Android (emulator/device) and iOS (simulator/device). Mark each item per platform.

## Fresh install
- [ ] Onboarding page 1 and 2 show the animation, title and message; the arrow button advances.
- [ ] Page 3: "Let's start" with an empty name shows "Required field *".
- [ ] Unit dropdown is disabled for Yes/No and enabled for Number.
- [ ] Legend color opens the picker; a preset, "Generate random", Cancel and OK behave correctly.
- [ ] Enabling notifications asks for permission; denying it turns the switch back off with a message.
- [ ] Finishing onboarding opens the Dashboard; relaunching the app skips onboarding.

## Dashboard
- [ ] With no data the chart says "Add chart data by pressing +".
- [ ] Dashboard opens on 1W; 1D / 1W / 1M / 6M / Max change the chart axis and the list; Max starts at the first record of the filtered tasks.
- [ ] Filter sheet: Select All, individual tasks, Cancel and Apply update chart and list.
- [ ] Swiping a list item left deletes it and shows "Value deleted".
- [ ] "Get Insights" shows a summary for the visible tasks.
- [ ] Menu → Export data with no values shows the "No value found" snackbar with "Insert Value".
- [ ] Export → Share file opens the share sheet with a CSV attachment.
- [ ] Export → Save file lets the user pick a location and then shows "Saving completed".
- [ ] "Filtered" export only contains the tasks enabled in the filter.

## Enter task progress
- [ ] Save without a task shows "Invalid field name detected".
- [ ] Yes/No and Number tasks show the right input; empty value shows "Required field *".
- [ ] Future dates cannot be picked; time picker honours the 12/24h device setting.
- [ ] Saved value appears on the chart and in the list with date, time, value, unit and comment.

## Manage tasks
- [ ] List shows name, type, unit and color; empty state text when there are no tasks.
- [ ] Tapping a task opens it for editing; entry type is locked; "Update" saves.
- [ ] Duplicate names are rejected with "Task already exists."
- [ ] Swiping deletes the task and its values and shows "<name> deleted".

## Reminders
- [ ] A reminder set a few minutes ahead fires with "Don't forget to log: <task>".
- [ ] It fires again on the next selected day (Android: after the first alarm the next one is armed).
- [ ] Android: reminders survive a reboot.
- [ ] Turning the reminder off or deleting the task cancels it.

## Upgrade (Android)
- [ ] Install the previous release, create tasks/values (incl. a reminder), install the new build over it:
      data, colors, reminder settings are intact and onboarding is skipped.

## Other
- [ ] Legal Terms shows version, build number and licenses.
- [ ] Dark mode renders correctly on all screens.
- [ ] Rotation / iPad / large screens: no clipped content.
