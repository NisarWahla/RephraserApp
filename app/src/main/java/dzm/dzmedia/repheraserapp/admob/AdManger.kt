package dzm.dzmedia.repheraserapp.admob

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.widget.RelativeLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dzm.dzmedia.repheraserapp.BuildConfig


@SuppressLint("StaticFieldLeak")
object AdManger {

    var mIntersital: InterstitialAd? = null
    var mRewardedAd: RewardedAd? = null
    var mRewardItem = false
    private lateinit var mContext: Context
    var adCallBackRewardedAd: CallBackRewardedAd? = null


    var adCallbackInterstisial: AdManagerListener? = null
    var context: Context? = null
    var loadRewarStatus = false
    var adLayout: RelativeLayout? = null
    var adView: AdView? = null

    private const val interstitialAds_Test = "ca-app-pub-3940256099942544/1033173712"
    private const val interstitialAds_Real = "ca-app-pub-6081967606533178/1258007049"
    private const val BANNER_ID_1_Test = "ca-app-pub-3940256099942544/6300978111"
    private const val BANNER_ID_1_Real = "ca-app-pub-6081967606533178/4814108671"
    private const val rewarded_video_add_id = "ca-app-pub-3940256099942544/5224354917"

    @JvmStatic
    fun init(context: Context) {
        MobileAds.initialize(context)
        AdManger.context = context
        loadInterstial(context)
        loadRewardedAd(context)

    }

    @JvmStatic
    fun loadBannerAds(layout: RelativeLayout, activity: Activity) {
        adLayout = layout
        bannerAd(activity)
    }

    /**************************************Banner Ads *****************************/
    fun bannerAd(activity: Activity) {
        (setUpAds(activity))
    }

    private fun setUpAds(activity: Activity) {
        try {
            if (BuildConfig.DEBUG) {
                adView = AdView(activity)
                adView!!.adUnitId = BANNER_ID_1_Test
                val adSize = adSize(activity)
                adView!!.setAdSize(adSize)
                adLayout!!.addView(adView)
                val request = AdRequest.Builder().build()
                adView!!.loadAd(request)
            } else {
                adView = AdView(activity)
                adView!!.adUnitId = BANNER_ID_1_Real
                val adSize = adSize(activity)
                adView!!.setAdSize(adSize)
                adLayout!!.addView(adView)
                val request = AdRequest.Builder().build()
                adView!!.loadAd(request)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: Error) {
            e.printStackTrace()
        }

    }

    // Determine the screen width (less decorations) to use for the ad width.
    fun adSize(activity: Activity): AdSize {
        try {
            // Determine the screen width (less decorations) to use for the ad width.
            val display = activity.windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            val density = outMetrics.density
            var adWidthPixels = adLayout!!.width.toFloat()
            // If the ad hasn't been laid out, default to the full screen width.
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context!!, adWidth)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return AdSize.BANNER
        }
    }

    @JvmStatic
    fun loadInterstial(context: Context) {
        try {
            if (mIntersital == null) {

                if (BuildConfig.DEBUG) {
                    val adRequest = AdRequest.Builder().build()
                    InterstitialAd.load(context,
                        interstitialAds_Test,
                        adRequest,
                        object : InterstitialAdLoadCallback() {
                            override fun onAdFailedToLoad(loadingError: LoadAdError) {
                                super.onAdFailedToLoad(loadingError)
                                Log.e("error", loadingError.message)
                                mIntersital = null
                            }

                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                super.onAdLoaded(interstitialAd)
                                mIntersital = interstitialAd
                            }
                        })
                } else {

                    val adRequest = AdRequest.Builder().build()
                    InterstitialAd.load(context,
                        interstitialAds_Real,
                        adRequest,
                        object : InterstitialAdLoadCallback() {
                            override fun onAdFailedToLoad(loadingError: LoadAdError) {
                                super.onAdFailedToLoad(loadingError)
                                Log.e("error", loadingError.message)
                                mIntersital = null
                            }

                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                super.onAdLoaded(interstitialAd)
                                mIntersital = interstitialAd
                            }
                        })
                }


            }
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: Error) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun loadRewardedAd(context: Context) {
        try {
            if (BuildConfig.DEBUG) {
                if (mRewardedAd == null) {
                    loadRewardAds(context, rewarded_video_add_id)
                } else {
                    if (mRewardedAd != null) {
                        Log.e("TAG", "Ads is already load")
                    } else {
                        loadRewardAds(context, rewarded_video_add_id)
                    }
                }
            } else {
                if (mRewardedAd == null) {
                    loadRewardAds(context, rewarded_video_add_id)
                    loadRewarStatus = !loadRewarStatus
                } else {
                    if (mRewardedAd != null) {
                        Log.e("TAG", "Ads is already load")
                    } else {
                        loadRewardAds(context, rewarded_video_add_id)
                        loadRewarStatus = !loadRewarStatus
                    }
                }
            }
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
            init(context)
        }
    }

    private fun loadRewardAds(context: Context, ids: String) {
        RewardedAd.load(context,
            ids,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    mRewardedAd = null
                }

                override fun onAdLoaded(reward: RewardedAd) {
                    super.onAdLoaded(reward)
                    mRewardedAd = reward
                }
            })
    }

    @JvmStatic
    fun showRewardedVideo(activity: Activity, adCallBack: CallBackRewardedAd) {

        adCallBackRewardedAd = adCallBack
        if (mRewardedAd == null) {
            loadRewardedAd(activity)
        } else {
            if (mRewardedAd != null) {
                mRewardItem = false
                mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        Log.e("error", p0.message)
                    }

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        mRewardedAd = null
                        adCallBackRewardedAd?.onRewardedAdClosed()
                    }
                }
                mRewardedAd?.show(
                    activity
                ) { reward ->
                    adCallBackRewardedAd?.onUserEarnedReward(reward)
                    mRewardItem = true
                }
                loadRewardedAd(activity)
            } else {
                loadRewardedAd(activity)
            }
        }
    }

    @JvmStatic
    private fun adListner(adCallback: FullScreenContentCallback) {
        mIntersital?.fullScreenContentCallback = adCallback
    }

    interface CallBackRewardedAd {
        fun onRewardedAdLoaded()
        fun onRewardedAdFailedToLoad(loadAdError: LoadAdError)

        fun onUserEarnedReward(rewardItem: RewardItem)
        fun onRewardedAdClosed()
        fun onRewardedAdFailedToShow(adError: AdError)
        fun onRewardedAdOpened()
    }

    @JvmStatic
    fun showInterstial(
        activity: Activity, context: Context, adCallBack: FullScreenContentCallback
    ) {
        try {
            adListner(adCallBack)
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    if (mIntersital != null) {
                        mIntersital!!.show(activity)
                        mIntersital = null
                        loadInterstial(context)
                    } else {
                        loadInterstial(context)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }, 0)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @JvmStatic
    fun isInterstialLoaded(): Boolean {
        return mIntersital != null
    }

    @JvmStatic
    fun isRewardedAdLoaded(): Boolean {
        return if (mRewardedAd == null) {
            false
        } else {
            mRewardedAd != null
        }
    }

    interface AdManagerListener {
        fun onAdClose(catname: String, pos: Int)
        fun onAdClose(string: String)
        fun onAdCloseActivity()
    }

    private var bannerStatus: Boolean = false
    private var intersititalStatus: Boolean = false

    //This method call only when you'r using more then one banner id
    private fun laodBannerId(): Boolean {
        bannerStatus = !bannerStatus
        return bannerStatus
    }

    //This method call only when you'r using more then one banner id
    private fun laodIntersititalId(): Boolean {
        intersititalStatus = !intersititalStatus
        return intersititalStatus
    }
}