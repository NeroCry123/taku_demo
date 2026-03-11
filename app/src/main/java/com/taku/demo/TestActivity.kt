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
import com.anythink.rewardvideo.api.ATRewardVideoAd
import com.anythink.rewardvideo.api.ATRewardVideoListener
import com.anythink.splashad.api.ATSplashAd
import com.anythink.splashad.api.ATSplashAdExtraInfo
import com.anythink.splashad.api.ATSplashAdListener
import com.bytedance.sdk.openadsdk.TTDislikeDialogAbstract
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

    private val tag = "TakuSdk"
    private val appId = ""
    private val appKey = ""
    private val splashAdPos = ""
    private val interstitialAdPos = ""
    private val nativeAdPos = ""
    private val bannerAdPos = ""
    private val rewardVideoAdPos = ""
    private var splashContainer: FrameLayout? = null
    private var isInit = false //todo 是否已经初始化sdk

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
            return
        }
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
            return
        }
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
            return
        }
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
        rewardVideoAd!!.load()
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
            return
        }
        val atInterstitial = ATInterstitial(this, interstitialAdPos)
        atInterstitial.setAdListener(object : ATInterstitialListener {
            override fun onInterstitialAdLoaded() {
                Log.i(tag, "插屏-加载成功")
                showInterstitialAd(atInterstitial)
            }

            override fun onInterstitialAdLoadFail(adError: AdError) {
                Log.i(tag, "插屏-加载失败 code = " + adError.getCode() + " , msg = " + adError.getDesc())
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
        atInterstitial!!.load()
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
            return
        }
        var atNative: ATNative? = null
        atNative = ATNative(this, nativeAdPos, object : ATNativeNetworkListener {
            override fun onNativeAdLoaded() {
                Log.i(tag, "信息流-加载成功")
                showNativeAd(atNative)
            }

            override fun onNativeAdLoadFail(adError: AdError) {
                Log.i(tag, "信息流-加载失败 code = " + adError.getCode() + " , msg = " + adError.getDesc())
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
            //这个只面向穿山甲的=》setDevParams 需要放在prepare之前调用才有效,当点击关闭按钮的时候，就不会弹出反馈窗口（一闪而过），
            nativeAd.setDevParams(hashMapOf<String, Any>(
                "custom_dislike_dialog" to object : TTDislikeDialogAbstract(this) {

                    override fun onCreate(p0: Bundle?) {
                        super.onCreate(p0)
                        //监听弹出的时候关闭它
                        setOnShowListener {
                            dismiss()
                        }
                        //如果需要删除view，可以在这里调用（可选）
                        nativeView.parent?.let { parent ->
                            if(parent is ViewGroup){
                                parent.removeView(nativeView)
                            }
                        }
                    }

                    override fun getLayoutId(): Int {
                        return 0
                    }

                    override fun getTTDislikeListViewIds(): IntArray {
                        return intArrayOf()
                    }

                    override fun getLayoutParams(): ViewGroup.LayoutParams? {
                        return null
                    }

                }
            ))
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
            return
        }
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
            }

            override fun onBannerClicked(atAdInfo: ATAdInfo?) {
                Log.i(tag, "横幅-点击")
            }

            override fun onBannerShow(atAdInfo: ATAdInfo) {
                Log.i(tag, "横幅-展示")
                showShowEcpm(atAdInfo)

                //仅针对穿山甲的=》当点击关闭按钮的时候，就不会弹出反馈窗口（一闪而过）
                if(atAdInfo.networkFirmId != 15){
                    return
                }
                val field = atBannerView.javaClass.getDeclaredField("mCustomBannerAd")
                field.isAccessible = true
                val customBannerAd = field.get(atBannerView)
                val fields = customBannerAd.javaClass.declaredFields
                for (field in fields) {
                    if (field.type == TTNativeExpressAd::class.java) {
                        field.isAccessible = true
                        val ttNativeAd = field.get(customBannerAd) as TTNativeExpressAd
                        ttNativeAd.setDislikeDialog(object : TTDislikeDialogAbstract(this@TestActivity) {

                            override fun onCreate(p0: Bundle?) {
                                super.onCreate(p0)
                                //监听弹出的时候关闭它
                                setOnShowListener {
                                    dismiss()
                                }
                                //如果需要删除view，可以在这里调用（可选）
                                atBannerView.parent?.let { parent ->
                                    if(parent is ViewGroup){
                                        parent.removeView(atBannerView)
                                    }
                                }
                            }

                            override fun getLayoutId(): Int {
                                return 0
                            }

                            override fun getTTDislikeListViewIds(): IntArray {
                                return intArrayOf()
                            }

                            override fun getLayoutParams(): ViewGroup.LayoutParams? {
                                return null
                            }

                        })
                        break
                    }
                }
            }

            override fun onBannerClose(atAdInfo: ATAdInfo?) {

            }

            override fun onBannerAutoRefreshed(atAdInfo: ATAdInfo?) {
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
        Log.i(tag, "广告平台:" + getSdkName(atAdInfo.networkFirmId))
    }

    private fun getSdkName(id: Int): String {
        when (id) {
            8 -> return "gdt"
            15 -> return "pangle"
            28 -> return "ks"
        }
        return ""
    }
}
