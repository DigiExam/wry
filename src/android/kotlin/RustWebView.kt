// Copyright 2020-2023 Tauri Programme within The Commons Conservancy
// SPDX-License-Identifier: Apache-2.0
// SPDX-License-Identifier: MIT

@file:Suppress("unused", "SetJavaScriptEnabled")

package {{package}}

import android.annotation.SuppressLint
import android.webkit.*
import android.content.Context
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import kotlin.collections.Map

@SuppressLint("RestrictedApi")
class RustWebView(context: Context, val initScripts: Array<String>, val id: String): WebView(context) {
    val isDocumentStartScriptEnabled: Boolean

    init {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.setGeolocationEnabled(true)
        settings.databaseEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.javaScriptCanOpenWindowsAutomatically = true

        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            isDocumentStartScriptEnabled = true
            for (script in initScripts) {
                WebViewCompat.addDocumentStartJavaScript(this, script, setOf("*"));
            }
        } else {
          isDocumentStartScriptEnabled = false
        }

        edgeToEdgeHandler();

        {{class-init}}
    }

    fun loadUrlMainThread(url: String) {
        post {
          loadUrl(url)
        }
    }

    fun loadUrlMainThread(url: String, additionalHttpHeaders: Map<String, String>) {
        post {
          loadUrl(url, additionalHttpHeaders)
        }
    }

    override fun loadUrl(url: String) {
        if (!shouldOverride(url)) {
            super.loadUrl(url);
        }
    }

    override fun loadUrl(url: String, additionalHttpHeaders: Map<String, String>) {
        if (!shouldOverride(url)) {
            super.loadUrl(url, additionalHttpHeaders);
        }
    }

    fun loadHTMLMainThread(html: String) {
        post {
          super.loadData(html, "text/html", null)
        }
    }

    fun evalScript(id: Int, script: String) {
        post {
            super.evaluateJavascript(script) { result ->
                onEval(id, result)
            }
        }
    }

    fun clearAllBrowsingData() {
        try {
            super.getContext().deleteDatabase("webviewCache.db")
            super.getContext().deleteDatabase("webview.db")
            super.clearCache(true)
            super.clearHistory()
            super.clearFormData()
        } catch (ex: Exception) {
            Logger.error("Unable to create temporary media capture file: " + ex.message)
        }
    }

    fun getCookies(url: String): String {
        val cookieManager = CookieManager.getInstance()
        return cookieManager.getCookie(url)
    }

    fun edgeToEdgeHandler() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, windowInsets ->
            val insets: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            val mlp = v.layoutParams as MarginLayoutParams
            mlp.leftMargin = insets.left
            mlp.bottomMargin = insets.bottom
            mlp.rightMargin = insets.right
            mlp.topMargin = insets.top
            v.layoutParams = mlp

            // Don't pass window insets to children
            // TODO: Test this in WebView 140/141 - https://issues.chromium.org/issues/40699457#comment71
            WindowInsetsCompat.CONSUMED
        }
    }

    private external fun shouldOverride(url: String): Boolean
    private external fun onEval(id: Int, result: String)

    {{class-extension}}
}
