/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-manager
 */

package app.morphe.manager.util

import androidx.compose.ui.graphics.Color
import app.morphe.manager.util.KnownApps.DEFAULT_COLORS
import app.morphe.manager.util.KnownApps.getAppName
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

const val tag = "Molt Manager"

/**
 * Molt is served entirely out of its own repository - there is no Molt backend to run.
 * Anything that upstream fetched from an API is a plain JSON file under [MOLT_DATA_BASE_URL].
 */
const val MOLT_REPO_URL = "https://github.com/davidggjg/Yanpp"

/**
 * Branch the app reads its data files from. This is the branch the repository actually
 * publishes from - point it at "main" once the work lands there, otherwise every raw
 * fetch below 404s and the app reports its patch sources as unavailable.
 */
const val MOLT_DATA_BRANCH = "claude/single-square-repairs-ov8wk2"

const val MOLT_DATA_BASE_URL =
    "https://raw.githubusercontent.com/davidggjg/Yanpp/$MOLT_DATA_BRANCH/data"

const val SOURCE_NAME = "Molt Patches"
const val MANAGER_REPO_URL = MOLT_REPO_URL
const val SOURCE_REPO_URL = MOLT_REPO_URL
const val MORPHE_WEBSITE_URL = MOLT_REPO_URL

/**
 * Base for the `/v2/...` routes upstream served from its own backend. Molt runs no backend,
 * so these requests 404 and callers fall through to their offline paths - the APKMirror web
 * search in [app.morphe.manager.domain.manager.DownloadUrlResolver], and the cached blocklist.
 * Pointed at our own repository so the app never talks to another project's servers.
 */
const val MORPHE_API_URL = MOLT_DATA_BASE_URL

/** Static file in this repository - no server needed. */
const val BLOCKED_SOURCES_URL = "$MOLT_DATA_BASE_URL/blocked-sources.json"

/** Raw GitHub URL for the manager release JSON. */
const val MANAGER_RELEASE_JSON_URL =
    "https://raw.githubusercontent.com/davidggjg/Yanpp/$MOLT_DATA_BRANCH/morphe-manager/app-release.json"

/** Raw GitHub URL for the pre-release manager release JSON. */
const val MANAGER_PRERELEASE_JSON_URL = MANAGER_RELEASE_JSON_URL

/** Controls whether manager updates are fetched directly from JSON files in the repository instead of using the GitHub API */
const val USE_MANAGER_DIRECT_JSON = true

/** Controls whether patches are fetched directly from JSON files in the repository instead of using the Morphe API */
const val USE_PATCHES_DIRECT_JSON = true

/**
 * Registry of known patchable apps.
 */
object KnownApps {
    const val YOUTUBE       = "com.google.android.youtube"
    const val YOUTUBE_MUSIC = "com.google.android.apps.youtube.music"
    const val REDDIT        = "com.reddit.frontpage"
    // const val X_TWITTER     = "com.twitter.android"

    // Molt brand gradient tail: the chameleon's violet running into its mint
    // highlights, so app cards read as part of the same identity as the icon.
    val GRADIENT_MID = Color(0xFF521AB2)
    val GRADIENT_END = Color(0xFF30CFC4)

    val DEFAULT_DOWNLOAD_COLOR = Color(0xFF2B1551)

    // Default gradient for packages with no bundle-declared color
    val DEFAULT_COLORS = listOf(DEFAULT_DOWNLOAD_COLOR, GRADIENT_MID, GRADIENT_END)

    /**
     * A known patchable app entry.
     *
     * @param packageName The app's package name.
     * @param isPinnedByDefault Whether this app appears pinned on the home screen by default.
     * @param brandColor App brand color used for the home screen button gradient start and
     *   shimmer placeholder. Should match the appIconColor value shipped in the bundle's
     *   Compatibility declaration. Null means fall back to [DEFAULT_COLORS].
     */
    data class Entry(
        val packageName: String,
        val isPinnedByDefault: Boolean = true,
        val brandColor: Color? = null,
    )

    /** All known app entries in display order. */
    val all: List<Entry> = listOf(
        Entry(REDDIT,        brandColor = Color(0xFFFF4500)),
        Entry(YOUTUBE,       brandColor = Color(0xFFFF0033)),
        Entry(YOUTUBE_MUSIC, brandColor = Color(0xFFFF0000)),
        // Entry(X_TWITTER, brandColor = Color(0xFF000000)),  // Uncomment when release
    )

    // Fast lookup map - built once at startup.
    private val byPackage: Map<String, Entry> = all.associateBy { it.packageName }

    /** Returns the [Entry] for [packageName], or null if not a known app. */
    fun fromPackage(packageName: String): Entry? = byPackage[packageName]

    /**
     * Ordered list of shimmer placeholder gradient colors shown during cold-start loading.
     * Uses each app's [Entry.brandColor] as the gradient start — actual bundle colors will
     * replace them once the bundle loads. Falls back to [DEFAULT_COLORS] if no brand color
     * is declared.
     */
    val DEFAULT_SHIMMER_GRADIENTS: List<List<Color>> by lazy {
        all.filter { it.isPinnedByDefault }.map { entry ->
            entry.brandColor?.let { color -> listOf(color, GRADIENT_MID, GRADIENT_END) }
                ?: DEFAULT_COLORS
        }
    }

    /**
     * Fallback display names for all packages - used only when bundle metadata and installed
     * app labels are both unavailable. Includes KnownApps entries so no separate localization
     * path is needed (bundle always provides the authoritative name anyway).
     */
    private val FALLBACK_NAMES = mapOf(
        YOUTUBE       to "YouTube",
        YOUTUBE_MUSIC to "YouTube Music",
        REDDIT        to "Reddit",
    )

    /**
     * Returns a display name for [packageName].
     * Priority: fallback table → raw package name.
     * Used as the last resort when bundle metadata and installed labels are unavailable.
     */
    fun getAppName(packageName: String): String =
        FALLBACK_NAMES[packageName] ?: packageName

    /**
     * Returns a fallback display name for [packageName], or null if not in the table.
     * Unlike [getAppName], does not fall back to the raw package name — null means unknown.
     * Used for transitional metadata fallbacks where absence should be preserved.
     */
    fun fallbackName(packageName: String): String? = FALLBACK_NAMES[packageName]
}

/**
 * Timeout applied to a single uninstall step when running as part of a batch.
 * The system uninstall UI can block indefinitely if the user leaves it open;
 * this keeps the batch making forward progress.
 */
val BATCH_UNINSTALL_TIMEOUT: Duration = 2.minutes

const val APK_MIMETYPE  = "application/vnd.android.package-archive"

const val PLAY_STORE_INSTALLER_PACKAGE = "com.android.vending"

const val AOSP_INSTALLER_PACKAGE        = "com.google.android.packageinstaller"
const val AOSP_INSTALLER_PACKAGE_LEGACY = "com.android.packageinstaller"
const val AOSP_INSTALLER_LABEL          = "Package installer"
const val JSON_MIMETYPE     = "application/json"
const val BIN_MIMETYPE      = "application/octet-stream"
const val TEXT_MIMETYPE     = "text/plain"
const val MPP_MIMETYPE      = "application/vnd.ms-project"
const val IMAGE_MIMETYPE    = "image/*"
const val WILDCARD_MIMETYPE = "*/*"

val APK_FILE_MIME_TYPES = arrayOf(
    BIN_MIMETYPE,
    APK_MIMETYPE,
    // ApkMirror split files of "app-whatever123_apkmirror.com.apk" regularly misclassify
    // the file as an application or something incorrect. Renaming the file and
    // removing "apkmirror.com" from the file name fixes the issue, but that's not something the
    // end user will know or should have to do. Instead, show all files to ensure the user can
    // always select no matter what file naming ApkMirror uses.
    "application/*",
//    "application/zip",
//    "application/x-zip-compressed",
//    "application/x-apkm",
//    "application/x-apks",
//    "application/x-xapk",
//    "application/xapk",
//    "application/vnd.android.xapk",
//    "application/vnd.android.apkm",
//    "application/apkm",
//    "application/vnd.android.apks",
//    "application/apks",
)

val MPP_FILE_MIME_TYPES = arrayOf(
    BIN_MIMETYPE,
    MPP_MIMETYPE,
//    "application/x-zip-compressed"
    "*/*"
)

/** File extensions recognized as APK-family archives that Morphe can patch. */
val APK_EXTENSIONS = setOf("apk", "apks", "xapk", "apkm")
