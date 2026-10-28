# Hisab Pro — Daily Expense Manager

Hindi-first, offline Android expense tracker. Records are stored locally on the device using SharedPreferences.

## Included in this starter version
- Add an expense with name, amount, category, date (`YYYY-MM-DD`) and optional note
- Categories for grocery, milk, petrol, diesel, utility bills and more
- Today and current-month totals
- Expense list and delete action
- Offline use; no account or internet connection required
- GitHub Actions workflow that builds a debug APK and publishes it as an artifact

## Build an APK using GitHub (no Android Studio required)

1. On GitHub, create a **new empty repository** named `HisabPro`. Do not add a README/license there (this project already contains a README).
2. Download and unzip the `HisabPro-project.zip` file on your computer.
3. Open the new repository on GitHub and choose **Add file → Upload files**.
4. Upload the *contents* of the unzipped `HisabPro` folder, not the ZIP itself. Ensure `app/`, `.github/`, `build.gradle.kts`, `settings.gradle.kts`, and `gradle.properties` appear at the repository root.
5. Commit the files to the `main` branch.
6. Open the repository's **Actions** tab. Select **Build Hisab Pro APK** and press **Run workflow** if it has not started automatically. Confirm **Run workflow**.
7. Wait for the workflow to finish with a green check mark. Open that run, scroll to **Artifacts**, and download `HisabPro-debug-apk`.
8. Extract the downloaded artifact ZIP. It contains `app-debug.apk`. Move the APK to your Android phone and install it. Android may ask you to allow installation from the app you used to open the file; only enable this for your own downloaded APK.

## Important notes
- This workflow builds a **debug APK** for personal testing. It is not a Play Store release and is not a signed production release.
- GitHub Actions is the place where the build will be verified. If it fails, open the failed run, select the red failed step, and share the error text so it can be fixed.
- Records are stored on the phone. This starter version has no cloud sync, export/backup, recurring bills, editing existing records, or password lock. Uninstalling the app may delete its local data.
- Never upload signing keys, passwords, or other secrets into the repository.
