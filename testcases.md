# Employee Management System — Test Cases

## Test Environment
- **Frontend**: http://localhost:5173 (Vite dev server)
- **Backend**: http://localhost:5000 (Express server)
- **Database**: db.json (JSON file-based)

---

## 1. Authentication Test Cases

| TC ID | Test Case | Input | Expected Result | Status |
|-------|-----------|-------|-----------------|--------|
| TC-001 | Admin login — valid credentials | username: admin, password: admin123, role: Admin | Login success, redirect to Admin Dashboard | |
| TC-002 | Admin login — wrong password | username: admin, password: wrongpass, role: Admin | Error: "Invalid Admin credentials." | |
| TC-003 | Admin login — wrong username | username: notadmin, password: admin123, role: Admin | Error: "Invalid Admin credentials." | |
| TC-004 | Employee login — valid (Bala) | username: bala, password: bala123, role: Employee | Login success, Employee dashboard for Bala | |
| TC-005 | Employee login — valid (Asbar) | username: asbar, password: asbar123, role: Employee | Login success, Employee dashboard for Asbar | |
| TC-006 | Employee login — valid (Nithis) | username: nithis, password: nithis123, role: Employee | Login success, Employee dashboard for Nithis | |
| TC-007 | Employee login — valid (Kamalesh) | username: kamalesh, password: kamalesh123, role: Employee | Login success, Employee dashboard for Kamalesh | |
| TC-008 | Employee login — wrong password | username: bala, password: wrongpass, role: Employee | Error: "Invalid Employee credentials." | |
| TC-009 | Login — empty fields | Submit with empty username/password | HTML5 validation prevents submission | |
| TC-010 | Role switch on login page | Click Admin card then Employee card | Form resets, active card highlights correctly | |

---

## 2. Role-Based Access Control Test Cases

| TC ID | Test Case | Role | Expected Result | Status |
|-------|-----------|------|-----------------|--------|
| TC-011 | Employee cannot see Salary section | Employee | Salary nav item NOT visible in sidebar | |
| TC-012 | Admin can see Salary section | Admin | Salary nav item visible in sidebar | |
| TC-013 | Employee cannot access pay slip | Employee | No pay slip view button, no salary page | |
| TC-014 | Admin can view any employee pay slip | Admin | Pay slip button visible on employee cards and salary table | |
| TC-015 | Admin can add employees | Admin | Add Employee form visible on Dashboard and Employees page | |
| TC-016 | Employee cannot add employees | Employee | Add Employee form NOT visible | |
| TC-017 | Admin can delete employees | Admin | Delete button visible on employee cards | |
| TC-018 | Employee cannot delete employees | Employee | Delete button NOT visible | |
| TC-019 | Admin can mark attendance | Admin | Present/Absent/Half Day buttons visible for all employees | |
| TC-020 | Employee can only view own attendance | Employee | Reads-only attendance log, no marking buttons | |
| TC-021 | Admin can change any password | Admin | Change Password modal accessible from sidebar | |
| TC-022 | Employee can change own password | Employee | Change Password modal accessible from sidebar | |
| TC-023 | Admin can export Excel | Admin | Export Excel button visible in topbar | |
| TC-024 | Employee cannot export Excel | Employee | Export Excel button NOT visible | |

---

## 3. Attendance Test Cases

| TC ID | Test Case | Input | Expected Result | Status |
|-------|-----------|-------|-----------------|--------|
| TC-025 | Mark employee Present | Click Present button for any employee | Green Present badge appears, DB updated | |
| TC-026 | Mark employee Absent | Click Absent button for any employee | Red Absent badge appears, DB updated | |
| TC-027 | Mark employee Half Day | Click Half Day button for any employee | Amber Half Day badge appears, DB updated | |
| TC-028 | Override attendance | Mark Present then click Absent | Status changes to Absent, overrides previous | |
| TC-029 | Attendance with note | Enter note, then mark Present | Badge shows "Present · [note]" | |
| TC-030 | View attendance by date | Select different date in date picker | Shows attendance for selected date | |
| TC-031 | Half Day count in stats | Mark 2 employees as Half Day | Half Day stat card shows count "2" | |
| TC-032 | Attendance persists on refresh | Mark attendance, refresh page | Same attendance records still shown | |

---

## 4. Avatar System Test Cases

| TC ID | Test Case | Input | Expected Result | Status |
|-------|-----------|-------|-----------------|--------|
| TC-033 | Open avatar modal | Click avatar in sidebar or camera icon | Avatar modal opens with current selection highlighted | |
| TC-034 | Select Preset 1 | Click Preset 1 in avatar grid | Preview updates to Preset 1 gradient avatar | |
| TC-035 | Select Preset 2 | Click Preset 2 in avatar grid | Preview updates to Preset 2 gradient avatar | |
| TC-036 | Select Preset 3 | Click Preset 3 in avatar grid | Preview updates to Preset 3 gradient avatar | |
| TC-037 | Select Preset 4 | Click Preset 4 in avatar grid | Preview updates to Preset 4 gradient avatar | |
| TC-038 | Upload photo | Click upload zone, select image file | Crop mode activates with canvas overlay | |
| TC-039 | Drag crop circle | Click and drag the purple circle | Crop position updates in real-time | |
| TC-040 | Apply crop | Click "Apply Crop" | Cropped round avatar preview appears | |
| TC-041 | Save avatar (preset) | Select preset, click Save | Employee card and sidebar avatar update | |
| TC-042 | Save avatar (photo) | Upload, crop, save | Cropped photo displayed as avatar everywhere | |
| TC-043 | Remove photo | After uploading, click "Remove Photo" | Reverts to preset avatar selection | |
| TC-044 | Avatar in employee cards | After saving avatar | Employee card shows new avatar | |
| TC-045 | Avatar in salary table | After saving avatar | Salary table row shows new avatar | |
| TC-046 | Avatar persists | Save avatar, refresh page | Avatar still shown after reload (via DB) | |

---

## 5. Pay Slip Test Cases

| TC ID | Test Case | Input | Expected Result | Status |
|-------|-----------|-------|-----------------|--------|
| TC-047 | Admin view pay slip | Click "Slip" button in salary table | Pay slip modal opens with correct employee data | |
| TC-048 | Pay slip calculations | Open any pay slip | Basic=50%, HRA=20%, Transport=10%, PF=12% of basic | |
| TC-049 | Download PDF button | Click "Download PDF" in modal | Browser print dialog opens | |
| TC-050 | Print CSS | Trigger print | Only pay slip visible, modal header hidden | |
| TC-051 | Pay slip has payment status | Paid employee's slip | Shows green "Paid" badge with date | |
| TC-052 | Pay slip — pending | Pending employee's slip | Shows amber "Pending" badge | |
| TC-053 | Close modal | Click X or overlay | Modal closes without saving | |

---

## 6. Change Password Test Cases

| TC ID | Test Case | Input | Expected Result | Status |
|-------|-----------|-------|-----------------|--------|
| TC-054 | Admin change password — success | Current: admin123, New: newpass123, Confirm: newpass123 | Success message, password updated in DB | |
| TC-055 | Admin wrong current password | Current: wrongpass, New: newpass123, Confirm: newpass123 | Error: "Current password is incorrect." | |
| TC-056 | Passwords don't match | New: newpass123, Confirm: different | Error: "New passwords do not match." | |
| TC-057 | Password too short | New: abc, Confirm: abc | Error: "New password must be at least 6 characters." | |
| TC-058 | Employee change own password | Login as bala, change password | Success, new password works on next login | |
| TC-059 | Admin login after password change | Use new password | Login succeeds with new password | |
| TC-060 | Show/hide password toggle | Click eye icon in each field | Field toggles between password/text type | |
| TC-061 | Modal closes after success | Password saved | Modal auto-closes after 2 seconds | |

---

## 7. Dark/Light Mode Test Cases

| TC ID | Test Case | Input | Expected Result | Status |
|-------|-----------|-------|-----------------|--------|
| TC-062 | Toggle dark to light | Click theme toggle in topbar | Full UI switches to bright white/blue palette | |
| TC-063 | Toggle light to dark | Click theme toggle again | Returns to dark purple/cyan neon palette | |
| TC-064 | Theme persists on refresh | Set to light mode, refresh | Light mode still active after reload | |
| TC-065 | Login page theme toggle | Click theme button on login page | Login page switches between dark/light | |
| TC-066 | Sidebar dark mode | Dark theme active | Sidebar has dark transparent glass background | |
| TC-067 | Sidebar light mode | Light theme active | Sidebar has white glass background | |
| TC-068 | Topbar theme toggle button | Button renders correctly | Sun icon in dark mode, Moon icon in light mode | |

---

## 8. SVG Icons Test Cases

| TC ID | Test Case | Expected Result | Status |
|-------|-----------|-----------------|--------|
| TC-069 | No emojis visible in UI | Inspect all elements | Zero emoji characters in production UI | |
| TC-070 | Nav icons render | View sidebar navigation | All 5 nav items show crisp SVG icons | |
| TC-071 | Attendance buttons have icons | View attendance section | Present/Absent/Half Day buttons have SVG icons | |
| TC-072 | Action buttons have icons | View employee cards | Camera, FileText, Trash icons on action buttons | |
| TC-073 | Stat cards have icons | View home dashboard | Users, Check, X, HalfCircle icons in stat cards | |
| TC-074 | Icons scale correctly | Resize browser window | Icons remain crisp at all sizes (SVG scalable) | |

---

## 9. Excel Export Test Cases

| TC ID | Test Case | Expected Result | Status |
|-------|-----------|-----------------|--------|
| TC-075 | Export Excel file | Click Export Excel | Downloads .xlsx file with 3 sheets | |
| TC-076 | Employees sheet | Open exported file | Sheet 1 has all employees with columns: EMP ID, Name, Role, Department, Status, Email, Salary | |
| TC-077 | Attendance sheet | Open exported file | Sheet 2 has all attendance records with date and status | |
| TC-078 | Salary Records sheet | Open exported file | Sheet 3 has salary history with paid/pending status | |
| TC-079 | Export logged in DB | Export, check db.json | exportLog array has new entry with timestamp | |
| TC-080 | Half Day in export | Mark Half Day, export | Attendance sheet shows "Half Day" status | |

---

## 10. Employee Management Test Cases

| TC ID | Test Case | Input | Expected Result | Status |
|-------|-----------|-------|-----------------|--------|
| TC-081 | Add new employee | Fill all fields, click Add | New employee appears in list with EMP-XXXX ID | |
| TC-082 | Auto salary assignment | No salary entered | Salary auto-assigned based on department range | |
| TC-083 | Custom salary | Enter specific amount | Employee gets that exact salary | |
| TC-084 | Search employees | Type name/dept/role | Filtered results shown in real-time | |
| TC-085 | Clear search | Click X on search | Full list restores | |
| TC-086 | Change employee status | Select from dropdown | Status badge updates immediately | |
| TC-087 | Edit salary inline | Click salary amount in table | Edit input appears, save icon | |
| TC-088 | Delete employee | Click trash icon, confirm | Employee removed from all lists and DB | |
| TC-089 | Cancel delete | Click trash, cancel confirmation | Nothing deleted | |

---

## 11. Database Persistence Test Cases

| TC ID | Test Case | Action | Expected Result | Status |
|-------|-----------|--------|-----------------|--------|
| TC-090 | Attendance persists | Mark attendance, restart server | Records still in DB | |
| TC-091 | New employee persists | Add employee, restart server | Employee still in DB | |
| TC-092 | Password change persists | Change password, restart server | New password works | |
| TC-093 | Avatar persists | Set avatar, restart server | Avatar still shown | |
| TC-094 | Schedule persists | Add event, restart server | Event still in schedule | |
| TC-095 | Salary update persists | Edit salary, restart server | New salary in DB | |

---

## How to Run Tests

1. Start backend: `node server.cjs`
2. Start frontend: `npm run dev`
3. Open browser: http://localhost:5173
4. Work through each test case
5. Mark status: PASS / FAIL / SKIP

## Known Credentials for Testing

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| Employee | bala | bala123 |
| Employee | asbar | asbar123 |
| Employee | nithis | nithis123 |
| Employee | kamalesh | kamalesh123 |
