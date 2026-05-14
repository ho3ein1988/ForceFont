package com.fontforce.xposed

import android.graphics.Paint
import android.graphics.Typeface
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class FontForceHook : IXposedHookLoadPackage, IXposedHookZygoteInit {

    private lateinit var xsp: XSharedPreferences

    private val iconFonts = setOf(
        "MaterialIcons", "CupertinoIcons", "FontAwesome", "Ionicons",
        "Feather", "AntDesign", "Entypo", "EvilIcons",
        "SimpleLineIcons", "Octicons", "Zocial", "Foundation"
    )

    // IXposedHookZygoteInit — اینجا XSharedPreferences رو یه بار init میکنیم
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        xsp = XSharedPreferences(BuildConfig.APPLICATION_ID, "fontforce_hook_prefs")
        xsp.makeWorldReadable()
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // وقتی خود اپ لود میشه → نشون میده ماژول فعاله
        if (lpparam.packageName == BuildConfig.APPLICATION_ID) {
            XposedBridge.log("[FontForce] Module active ✓")
            return
        }

        try {
            xsp.reload()

            val typefaceEnabled = xsp.getBoolean("method_typeface", true)
            val textviewEnabled = xsp.getBoolean("method_textview", true)
            val paintEnabled    = xsp.getBoolean("method_paint",    true)
            val webviewEnabled  = xsp.getBoolean("method_webview",  true)
            val flutterEnabled  = xsp.getBoolean("method_flutter",  false)

            XposedBridge.log("[FontForce] Hooking ${lpparam.packageName}")

            if (typefaceEnabled) hookAllTypeface(lpparam)
            if (textviewEnabled) hookTextView(lpparam)
            if (paintEnabled)    hookPaint(lpparam)
            if (webviewEnabled)  hookWebView(lpparam)
            if (flutterEnabled)  hookFlutter(lpparam)

        } catch (e: Throwable) {
            XposedBridge.log("[FontForce] Error in ${lpparam.packageName}: ${e.message}")
        }
    }

    private fun hookAllTypeface(lpparam: XC_LoadPackage.LoadPackageParam) {
        val tf = "android.graphics.Typeface"
        listOf("createFromAsset", "createFromFile", "createFromResources").forEach { method ->
            try {
                XposedHelpers.findAndHookMethod(tf, lpparam.classLoader, method,
                    XC_MethodReplacement.returnConstant(Typeface.DEFAULT))
            } catch (_: Throwable) {}
        }
        XposedHelpers.findAndHookMethod(tf, lpparam.classLoader,
            "create", String::class.java, Int::class.java,
            XC_MethodReplacement.returnConstant(Typeface.DEFAULT))
        XposedHelpers.findAndHookMethod(tf, lpparam.classLoader,
            "create", Typeface::class.java, Int::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.args[0] = Typeface.DEFAULT
                }
            })
        try {
            XposedHelpers.findAndHookMethod(tf, lpparam.classLoader,
                "create", Typeface::class.java, Int::class.java, Boolean::class.java,
                XC_MethodReplacement.returnConstant(Typeface.DEFAULT))
        } catch (_: Throwable) {}
    }

    private fun hookTextView(lpparam: XC_LoadPackage.LoadPackageParam) {
        val tv = "android.widget.TextView"
        XposedHelpers.findAndHookMethod(tv, lpparam.classLoader,
            "setTypeface", Typeface::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val typeface = param.args[0] as? Typeface ?: return
                    if (isCustomFont(typeface))
                        param.args[0] = Typeface.create(Typeface.DEFAULT, typeface.style)
                }
            })
        XposedHelpers.findAndHookMethod(tv, lpparam.classLoader,
            "setTypeface", Typeface::class.java, Int::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val typeface = param.args[0] as? Typeface
                    if (typeface != null && isCustomFont(typeface))
                        param.args[0] = Typeface.create(Typeface.DEFAULT, param.args[1] as Int)
                }
            })
        try {
            XposedHelpers.findAndHookMethod(tv, lpparam.classLoader,
                "setTextAppearance", android.content.Context::class.java, Int::class.java,
                XC_MethodReplacement.DO_NOTHING)
        } catch (_: Throwable) {}
    }

    private fun hookPaint(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(Paint::class.java.name, lpparam.classLoader,
            "setTypeface", Typeface::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val typeface = param.args[0] as? Typeface
                    if (typeface != null && isCustomFont(typeface))
                        param.args[0] = Typeface.DEFAULT
                }
            })
    }

    private fun hookWebView(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val css = "javascript:(function(){" +
                "var s=document.createElement('style');" +
                "s.textContent='*{font-family:system-ui,-apple-system,Roboto,sans-serif!important}';" +
                "document.head&&document.head.appendChild(s);" +
                "})()"
            val afterHook = object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    try { XposedHelpers.callMethod(param.thisObject, "evaluateJavascript", css, null) }
                    catch (_: Throwable) { XposedHelpers.callMethod(param.thisObject, "loadUrl", css) }
                }
            }
            try { XposedHelpers.findAndHookMethod("android.webkit.WebView", lpparam.classLoader, "loadUrl", String::class.java, afterHook) } catch (_: Throwable) {}
            try { XposedHelpers.findAndHookMethod("android.webkit.WebView", lpparam.classLoader, "loadData", String::class.java, String::class.java, String::class.java, afterHook) } catch (_: Throwable) {}
            try { XposedHelpers.findAndHookMethod("android.webkit.WebView", lpparam.classLoader, "loadDataWithBaseURL", String::class.java, String::class.java, String::class.java, String::class.java, String::class.java, afterHook) } catch (_: Throwable) {}
        } catch (e: Throwable) {
            XposedBridge.log("[FontForce] WebView hook failed: ${e.message}")
        }
    }

    private fun hookFlutter(lpparam: XC_LoadPackage.LoadPackageParam) {
        listOf("dart.ui.FontLoader", "io.flutter.plugin.font.FontLoader",
               "io.flutter.embedding.engine.loader.FlutterLoader").forEach { cls ->
            try { XposedHelpers.findAndHookMethod(cls, lpparam.classLoader, "registerFont", XC_MethodReplacement.returnConstant(true)) } catch (_: Throwable) {}
            try { XposedHelpers.findAndHookMethod(cls, lpparam.classLoader, "loadFont", android.content.Context::class.java, String::class.java, XC_MethodReplacement.returnConstant(true)) } catch (_: Throwable) {}
        }
    }

    private fun isCustomFont(typeface: Typeface): Boolean {
        if (typeface == Typeface.DEFAULT || typeface == Typeface.DEFAULT_BOLD ||
            typeface == Typeface.SANS_SERIF || typeface == Typeface.SERIF ||
            typeface == Typeface.MONOSPACE) return false
        try {
            val family = XposedHelpers.getObjectField(typeface, "mFamily") as? String
            if (family != null)
                for (iconFont in iconFonts)
                    if (family.contains(iconFont, ignoreCase = true)) return false
        } catch (_: Throwable) {}
        return true
    }
}
