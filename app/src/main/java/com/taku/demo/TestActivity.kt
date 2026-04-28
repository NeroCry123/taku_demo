package com.taku.demo

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.anythink.banner.api.ATBannerListener
import com.anythink.banner.api.ATBannerView
import com.anythink.core.api.ATAdConst
import com.anythink.core.api.ATAdInfo
import com.anythink.core.api.ATSDK
import com.anythink.core.api.AdError
import com.anythink.interstitial.api.ATInterstitial
import com.anythink.interstitial.api.ATInterstitialListener
import com.anythink.nativead.api.ATNative
import com.anythink.nativead.api.ATNativeAdView
import com.anythink.nativead.api.ATNativeEventListener
import com.anythink.nativead.api.ATNativeNetworkListener
import com.anythink.nativead.api.ATNativePrepareExInfo
import com.anythink.nativead.api.ATNativeView
import com.anythink.nativead.api.NativeAd
import com.anythink.rewardvideo.api.ATRewardVideoAd
import com.anythink.rewardvideo.api.ATRewardVideoListener
import com.anythink.splashad.api.ATSplashAd
import com.anythink.splashad.api.ATSplashAdExtraInfo
import com.anythink.splashad.api.ATSplashAdListener
import com.bytedance.sdk.openadsdk.TTAdDislike
import com.bytedance.sdk.openadsdk.TTFeedAd
import com.bytedance.sdk.openadsdk.TTNativeAd
import com.bytedance.sdk.openadsdk.TTNativeExpressAd

class TestActivity : Activity() {
    private val permissionList = ArrayList<String?>()
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    )

    private val tag = ""
    private val appId = ""
    private val appKey = ""
    private val splashAdPos = ""
    private val interstitialAdPos = ""
    private val nativeAdPos = ""
    private val bannerAdPos = ""
    private val rewardVideoAdPos = ""
    private var splashContainer: FrameLayout? = null
    private var isInit = false //todo 是否已经初始化sdk

    private val tvLog: TextView by lazy {
        findViewById(R.id.tv_log)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        for (permission in permissions) {
            val checkSelfPermission = checkSelfPermission(permission)
            if (checkSelfPermission == PackageManager.PERMISSION_GRANTED) {
                continue
            }
            permissionList.add(permission)
        }
        if (!permissionList.isEmpty()) {
            requestPermissions(permissionList.toTypedArray<String?>(), 1000)
        } else {
            initListener()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000) {
            initListener()
        }
    }

    private fun initListener() {
        //todo 初始化sdk
        findViewById<View>(R.id.btn_init).setOnClickListener {
            initSdk(this)
        }

        //todo 加载和显示开屏广告
        findViewById<View>(R.id.btn_splash).setOnClickListener {
            loadSplashAd()
        }

        //todo 加载和显示激励视频
        findViewById<View>(R.id.btn_reward).setOnClickListener {
            loadRewardVideoAd()
        }

        //todo 加载和显示插屏广告
        findViewById<View>(R.id.btn_interstitial).setOnClickListener {
            loadInterstitialAd()
        }

        //todo 加载和显示信息流广告
        findViewById<View>(R.id.btn_native).setOnClickListener {
            loadNativeAd()
        }

        //todo 加载和显示Banner广告
        findViewById<View>(R.id.btn_banner).setOnClickListener {
            loadBannerAd()
        }
    }

    private fun initSdk(context: Context?) {
        if(isInit){
            return
        }
        if(appId.isEmpty() || appKey.isEmpty()){
            Toast.makeText(this, "请先配置appId和appKey", Toast.LENGTH_SHORT).show()
            showError("初始化失败, 请先配置appId和appKey")
            return
        }
//        ATSDK.setNetworkLogDebug(true)//todo 测试阶段可以打开日志，正式上线时请关闭日志
//        ATSDK.integrationChecking(this) //todo 集成检测，检测SDK是否正常集成
        ATSDK.init(context, appId, appKey)
        ATSDK.start()
        Log.i(tag, "初始化-成功")
        isInit = true
    }

    /**
     * 加载开屏广告
     */
    private fun loadSplashAd() {
        if (!isInit) {
            Toast.makeText(this, "请先初始化SDK", Toast.LENGTH_SHORT).show()
            showError("请先初始化SDK")
            return
        }
        showError("")
        var splashAd: ATSplashAd? = null
        splashAd = ATSplashAd(this, splashAdPos, object : ATSplashAdListener {
            override fun onAdLoaded(b: Boolean) {
                showSplashAd(splashAd)
            }

            override fun onAdLoadTimeout() {
                Log.i(tag, "开屏-加载失败，加载超时")
            }

            override fun onNoAdError(adError: AdError) {
                Log.i(tag, "开屏-加载失败，error code = " + adError.getCode() + ", msg = " + adError.getDesc())
                showError("开屏-加载失败，error code = " + adError.getCode() + ", msg = " + adError.getDesc())
            }

            override fun onAdShow(atAdInfo: ATAdInfo) {
                Log.i(tag, "开屏-显示")
                showShowEcpm(atAdInfo)
            }

            override fun onAdClick(atAdInfo: ATAdInfo?) {
                Log.i(tag, "开屏-点击")
            }

            override fun onAdDismiss(atAdInfo: ATAdInfo?, atSplashAdExtraInfo: ATSplashAdExtraInfo?) {
                Log.i(tag, "开屏-关闭")
                splashContainer?.let {
                    val parent = it.parent
                    if(parent is ViewGroup){
                        parent.removeView(it)
                    }
                }

            }
        })
        splashAd.loadAd()
    }

    /**
     * 展示开屏广告
     */
    private fun showSplashAd(splashAd: ATSplashAd?) {
        splashAd?.let {
            if(it.isAdReady){
                val viewGroup = findViewById<ViewGroup>(android.R.id.content)
                splashContainer = FrameLayout(this)
                splashContainer!!.setBackgroundColor(Color.WHITE)
                viewGroup.addView(splashContainer, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                it.show(this, splashContainer)
            }
        }
    }

    /**
     * 加载激励视频广告
     */
    private fun loadRewardVideoAd() {
        if (!isInit) {
            Toast.makeText(this, "请先初始化SDK", Toast.LENGTH_SHORT).show()
            showError("请先初始化SDK")
            return
        }
        showError("")
        val rewardVideoAd = ATRewardVideoAd(this, rewardVideoAdPos)
        val localExtraMap: MutableMap<String?, Any?> = HashMap()
        localExtraMap[ATAdConst.KEY.USER_ID] = "用户id"
        localExtraMap[ATAdConst.KEY.USER_CUSTOM_DATA] = "用户自定义数据"
        rewardVideoAd.setLocalExtra(localExtraMap)
        rewardVideoAd.setAdListener(object : ATRewardVideoListener {
            override fun onRewardedVideoAdLoaded() {
                Log.i(tag, "激励视频-加载成功")
                showRewardVideoAd(rewardVideoAd)
            }

            override fun onRewardedVideoAdFailed(adError: AdError) {
                Log.i(tag, "激励视频-加载失败 code = " + adError.getCode() + ", msg = " + adError.getDesc())
                showError("激励视频-加载失败 code = " + adError.getCode() + ", msg = " + adError.getDesc())
            }

            override fun onRewardedVideoAdPlayStart(atAdInfo: ATAdInfo) {
                Log.i(tag, "激励视频-展示")
                showShowEcpm(atAdInfo)
            }

            override fun onRewardedVideoAdPlayEnd(atAdInfo: ATAdInfo?) {
                Log.i(tag, "激励视频-播放完成")
            }

            override fun onRewardedVideoAdPlayFailed(adError: AdError?, atAdInfo: ATAdInfo?) {
                Log.i(tag, "激励视频-播放错误")
            }

            override fun onRewardedVideoAdClosed(atAdInfo: ATAdInfo?) {
                Log.i(tag, "激励视频-关闭")
            }

            override fun onRewardedVideoAdPlayClicked(atAdInfo: ATAdInfo?) {
                Log.i(tag, "激励视频-点击")
            }

            override fun onReward(atAdInfo: ATAdInfo?) {
                //todo 在这里可以处理奖励
                Log.i(tag, "激励视频-奖励到达")
            }
        })
        rewardVideoAd.load()
    }

    /**
     * 展示激励视频
     */
    private fun showRewardVideoAd(rewardVideoAd: ATRewardVideoAd?) {
        rewardVideoAd?.let {
            if (it.isAdReady) {
                it.show(this)
            }
        }
    }

    private fun loadInterstitialAd() {
        if (!isInit) {
            Toast.makeText(this, "请先初始化SDK", Toast.LENGTH_SHORT).show()
            showError("请先初始化SDK")
            return
        }
        showError("")
        val atInterstitial = ATInterstitial(this, interstitialAdPos)
        atInterstitial.setAdListener(object : ATInterstitialListener {
            override fun onInterstitialAdLoaded() {
                Log.i(tag, "插屏-加载成功")
                showInterstitialAd(atInterstitial)
            }

            override fun onInterstitialAdLoadFail(adError: AdError) {
                Log.i(tag, "插屏-加载失败 code = " + adError.getCode() + " , msg = " + adError.getDesc())
                showError("插屏-加载失败 code = " + adError.getCode() + " , msg = " + adError.getDesc())
            }

            override fun onInterstitialAdClicked(atAdInfo: ATAdInfo?) {
                Log.i(tag, "插屏-点击")
            }

            override fun onInterstitialAdShow(atAdInfo: ATAdInfo) {
                Log.i(tag, "插屏-显示")
                showShowEcpm(atAdInfo)
            }

            override fun onInterstitialAdClose(atAdInfo: ATAdInfo?) {
                Log.i(tag, "插屏-关闭")
            }

            override fun onInterstitialAdVideoStart(atAdInfo: ATAdInfo?) {
            }

            override fun onInterstitialAdVideoEnd(atAdInfo: ATAdInfo?) {
                Log.i(tag, "插屏-播放完成")
            }

            override fun onInterstitialAdVideoError(adError: AdError?) {
            }
        })
        atInterstitial.load()
    }

    private fun showInterstitialAd(atInterstitial: ATInterstitial?) {
        atInterstitial?.let {
            if (it.isAdReady) {
                it.show(this)
            }
        }
    }

    /**
     * 加载信息流广告
     */
    private fun loadNativeAd() {
        if (!isInit) {
            Toast.makeText(this, "请先初始化SDK", Toast.LENGTH_SHORT).show()
            showError("请先初始化SDK")
            return
        }
        showError("")
        var atNative: ATNative? = null
        atNative = ATNative(this, nativeAdPos, object : ATNativeNetworkListener {
            override fun onNativeAdLoaded() {
                Log.i(tag, "信息流-加载成功")
                showNativeAd(atNative)
            }

            override fun onNativeAdLoadFail(adError: AdError) {
                Log.i(tag, "信息流-加载失败 code = " + adError.getCode() + " , msg = " + adError.getDesc())
                showError("信息流-加载失败 code = " + adError.getCode() + " , msg = " + adError.getDesc())
            }
        })
        val localExtraMap: MutableMap<String?, Any?> = HashMap()
        localExtraMap[ATAdConst.KEY.AD_WIDTH] = resources.displayMetrics.widthPixels
        localExtraMap[ATAdConst.KEY.AD_HEIGHT] = 0
        atNative.setLocalExtra(localExtraMap)
        atNative.makeAdRequest()
    }

    /**
     * 加载信息流广告
     */
    private fun showNativeAd(atNative: ATNative?) {
        atNative?.let {
            val nativeAd = it.nativeAd
            val nativeView = ATNativeView(this@TestActivity)

            nativeAd.setNativeEventListener(object : ATNativeEventListener {
                override fun onAdImpressed(atNativeAdView: ATNativeAdView?, atAdInfo: ATAdInfo) {
                    Log.i(tag, "信息流-展示")
                    showShowEcpm(atAdInfo)

                    nativeHandleClose(nativeAd, atAdInfo,nativeView)//信息流关闭处理(可选)
                }

                override fun onAdClicked(atNativeAdView: ATNativeAdView?, atAdInfo: ATAdInfo?) {
                    Log.i(tag, "信息流-点击")
                }

                override fun onAdVideoStart(atNativeAdView: ATNativeAdView?) {
                }

                override fun onAdVideoEnd(atNativeAdView: ATNativeAdView?) {
                }

                override fun onAdVideoProgress(atNativeAdView: ATNativeAdView?, i: Int) {
                }
            })
            nativeAd.renderAdContainer(nativeView, null)
            nativeAd.prepare(nativeView, ATNativePrepareExInfo())
            val viewGroup = findViewById<ViewGroup>(R.id.layout_ad_container)
            viewGroup.removeAllViews()
            viewGroup.addView(nativeView)
        }

    }

    /**
     * 加载横幅广告
     */
    private fun loadBannerAd() {
        if (!isInit) {
            Toast.makeText(this, "请先初始化SDK", Toast.LENGTH_SHORT).show()
            showError("请先初始化SDK")
            return
        }
        showError("")
        val atBannerView = ATBannerView(this)
        atBannerView.setPlacementId(bannerAdPos)
        val localExtraMap: MutableMap<String?, Any?> = HashMap()
        localExtraMap[ATAdConst.KEY.AD_WIDTH] = resources.displayMetrics.widthPixels
        localExtraMap[ATAdConst.KEY.AD_HEIGHT] = 0
        atBannerView.setLocalExtra(localExtraMap)
        atBannerView.setBannerAdListener(object : ATBannerListener {
            override fun onBannerLoaded() {
                Log.i(tag, "横幅-加载成功")
                showBannerAd(atBannerView)
            }

            override fun onBannerFailed(adError: AdError) {
                Log.i(tag, "横幅-加载失败: code = " + adError.getCode() + ", msg = " + adError.getDesc())
                showError("横幅-加载失败: code = " + adError.getCode() + ", msg = " + adError.getDesc())
            }

            override fun onBannerClicked(atAdInfo: ATAdInfo?) {
                Log.i(tag, "横幅-点击")
            }

            override fun onBannerShow(atAdInfo: ATAdInfo) {
                Log.i(tag, "横幅-展示")
                showShowEcpm(atAdInfo)
                bannerHandleClose(atAdInfo,atBannerView)//横幅关闭处理(可选)
            }

            override fun onBannerClose(atAdInfo: ATAdInfo?) {
                Log.i(tag, "横幅-关闭")
            }

            override fun onBannerAutoRefreshed(atAdInfo: ATAdInfo?) {
                if(atAdInfo == null)return
                showShowEcpm(atAdInfo)
                bannerHandleClose(atAdInfo,atBannerView)//横幅关闭处理(可选)
            }

            override fun onBannerAutoRefreshFail(adError: AdError) {
                Log.i(tag, "横幅-自动渲染失败：" + adError.fullErrorInfo)
            }
        })
        atBannerView.loadAd()
    }

    /**
     * 展示横幅广告
     */
    private fun showBannerAd(atBannerView: ATBannerView?) {
        atBannerView?.let {
            val viewGroup = findViewById<ViewGroup>(R.id.layout_ad_container)
            viewGroup.removeAllViews()
            viewGroup.addView(it)
        }
    }

    private fun showShowEcpm(atAdInfo: ATAdInfo) {
        Log.i(tag, "价格:" + String.format("%.2f", atAdInfo.ecpm))
        Log.i(tag, "代码位:" + atAdInfo.networkPlacementId)
        Log.i(tag, "广告平台Id:" + atAdInfo.networkFirmId)
    }

    private fun showError(msg: String) {
        tvLog.text = msg
    }

    /**
     * 处理信息流广告的关闭按钮（仅用于穿山甲和GroMore）
     */
    private fun nativeHandleClose(nativeAd: NativeAd, atAdInfo: ATAdInfo, nativeView: ATNativeAdView){
        if(atAdInfo.networkFirmId != 15 && atAdInfo.networkFirmId != 46){
            return
        }
        val field = nativeAd.javaClass.getDeclaredField("mBaseNativeAd")
        field.isAccessible = true
        val mBaseNativeAd = field.get(nativeAd)
        val fields = mBaseNativeAd.javaClass.declaredFields
        fun handle(dislikeDialog: TTAdDislike){
            dislikeDialog.setDislikeInteractionCallback(object : TTAdDislike.DislikeInteractionCallback {
                override fun onShow() {
                    Log.i(tag, "setDislikeCallback-onShow")
                    dislikeDialog.resetDislikeStatus()

                    //移除广告(可选)
                    nativeView.parent?.let { parent ->
                        if(parent is ViewGroup){
                            parent.removeView(nativeView)
                        }
                    }
                }

                override fun onSelected(p0: Int, p1: String?, p2: Boolean) {
                    Log.i(tag, "setDislikeCallback-onSelected")
                }

                override fun onCancel() {
                    Log.i(tag, "setDislikeCallback-onCancel")
                }
            })
        }
        for (field in fields) {
            if (field.type == TTFeedAd::class.java) {
                Log.i(tag, "TTFeedAd")
                field.isAccessible = true
                val ttFeedAd = field.get(mBaseNativeAd) as TTFeedAd
                val dislikeDialog = ttFeedAd.getDislikeDialog(this@TestActivity)
                handle(dislikeDialog)
                break
            }else if (field.type == TTNativeExpressAd::class.java) {
                Log.i(tag, "TTNativeExpressAd")
                field.isAccessible = true
                val ttNativeExpressAd = field.get(mBaseNativeAd) as TTNativeExpressAd
                val dislikeDialog = ttNativeExpressAd.getDislikeDialog(this@TestActivity)
                handle(dislikeDialog)
                break
            }else if (field.type == TTNativeAd::class.java){
                Log.i(tag, "TTNativeAd")
                field.isAccessible = true
                val ttNativeAd = field.get(mBaseNativeAd) as TTNativeAd
                val dislikeDialog = ttNativeAd.getDislikeDialog(this@TestActivity)
                handle(dislikeDialog)
                break
            }
        }
    }

    /**
     * 处理横幅广告的关闭按钮（仅用于穿山甲和GroMore）
     */
    private fun bannerHandleClose(atAdInfo: ATAdInfo, atBannerView: ATBannerView){

        fun getAllFields(clazz: Class<*>): List<java.lang.reflect.Field> {
            val fields = mutableListOf<java.lang.reflect.Field>()
            var current: Class<*>? = clazz

            while (current != null) {
                fields.addAll(current.declaredFields)
                current = current.superclass
            }
            return fields
        }

        if(atAdInfo.networkFirmId != 15 && atAdInfo.networkFirmId != 46 ){
            return
        }
        val field = atBannerView.javaClass.getDeclaredField("mCustomBannerAd")
        field.isAccessible = true
        val customBannerAd = field.get(atBannerView)
        val fields = getAllFields(customBannerAd.javaClass)
        for (field in fields) {
            if (field.type == TTNativeExpressAd::class.java) {
                field.isAccessible = true
                val ttNativeAd = field.get(customBannerAd) as TTNativeExpressAd
                val dislikeDialog = ttNativeAd.getDislikeDialog(this)
                dislikeDialog.setDislikeInteractionCallback(object : TTAdDislike.DislikeInteractionCallback {
                    override fun onShow() {
                        Log.i(tag, "setDislikeCallback-onShow")
                        dislikeDialog.resetDislikeStatus()
                        //移除广告(可选)
                        atBannerView.parent?.let { parent ->
                            if(parent is ViewGroup){
                                parent.removeView(atBannerView)
                            }
                        }
                    }

                    override fun onSelected(p0: Int, p1: String?, p2: Boolean) {
                        Log.i(tag, "setDislikeCallback-onSelected")
                    }

                    override fun onCancel() {
                        Log.i(tag, "setDislikeCallback-onCancel")
                    }
                })
                break
            }
        }
    }
}
