package com.instagram.video.downloader.ui.plans

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams.ReplacementMode
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.common.collect.ImmutableList
import com.instagram.video.downloader.R
import com.instagram.video.downloader.common.lockView
import com.instagram.video.downloader.common.ui.views.InstaDownloaderCircularProgressView
import com.instagram.video.downloader.data.remote.api.dto.PlanDto
import com.instagram.video.downloader.databinding.ActivityPlansBinding
import com.instagram.video.downloader.ui.base.BaseActivity
import com.instagram.video.downloader.ui.base.BaseAlertDialog
import com.instagram.video.downloader.ui.base.showMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class PlansActivity : BaseActivity<ActivityPlansBinding>() {

    private val viewModel: PlansViewModel by viewModels()

    companion object {
        const val TAG = "PlansActivity"

        fun getIntent(context: Context) = Intent(context, PlansActivity::class.java)

        fun open(activityResult: ActivityResultLauncher<Intent>, context: Context) {
            val intent = Intent(context, PlansActivity::class.java)
            activityResult.launch(intent)
        }
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        Timber.tag(TAG).i("purchasesUpdatedListener response code : %s", billingResult.debugMessage)
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val purchase = purchases?.get(0)
            Timber.tag(TAG).d("Purchase state: ${purchase?.purchaseState?.toString()}")

            if (purchase?.isAcknowledged == false) {
                handler.post {
                    if (!this@PlansActivity.isFinishing && !this@PlansActivity.isDestroyed) {
                        showLoading()
                    }
                }

                val acknowledgePurchaseParams = AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)

                billingClient.acknowledgePurchase(acknowledgePurchaseParams.build()) {
                    // what ever the response is send the purchase to the server
                    runOnUiThread {
                        viewModel.saveUserSubscription(purchase, selectedPlanTag)
                    }
                }
            }

        } else {
            Timber.tag(TAG).d("Response code ko : %s", billingResult.debugMessage)
            hideLoading()
            showMessage(R.string.something_went_wrong_try_later)
        }
    }


    override fun getLayoutId() = R.layout.activity_plans

    private var playConnectionMaxAttempts = 2
    private lateinit var billingClient: BillingClient
    private var selectedPlanTag = "1_year"
    private var isUserEligibleForYearlyDiscount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        binding.privacy.movementMethod = LinkMovementMethod.getInstance()

        binding.subscription1Year.setOnClickListener { handleUserPlansClick(UiPlan.ONE_YEAR) }
        binding.subscription6Months.setOnClickListener { handleUserPlansClick(UiPlan.SIX_MONTHS) }
        binding.subscription1Month.setOnClickListener { handleUserPlansClick(UiPlan.ONE_MONTH) }

        lifecycleScope.launch {
            viewModel.plansState.collect { state ->
                if (state.isPlansLoading) showPlansProgressBar() else hidePlansProgressBar()

                if (state.isLoading) showLoading() else hideLoading()

                if (state.error != null
                    && state.isPlansLoading.not()
                    && state.isLoading.not()
                ) showMessage(
                    state.error!!
                )

                if (state.isNetworkAvailable == true
                    && state.isPlansLoading
                    && state.error == null
                    && !state.isGoogleBillingSetupDone
                )
                    initGoogleBilling()


                if (state.isNetworkAvailable == false && state.isPlansLoading) showMessage(R.string.network_not_available)
            }
        }


        // when the plans is set the remote user is also set
        lifecycleScope.launch {
            viewModel
                .plans
                .collect { plans ->
                    if (plans.isNotEmpty()) {
                        runOnUiThread {
                            setCurrentSubscriptionView()
                            getGoogleSubscriptionDetails(plans)
                        }
                    }
                }
        }

        lifecycleScope.launch {
            viewModel.plansAction.collect { action ->
                when (action) {
                    is PlansAction.OnShowMessage -> showMessage(action.message)
                    is PlansAction.OnShowSubscribedDialog -> showWelcomeToPremiumDialog()
                }
            }
        }

        binding.subscribe.setOnClickListener {
            if (billingClient.isReady.not())
                return@setOnClickListener
            lockView(binding.subscribe, handler)

            val selectedPlan = viewModel.plans.value.firstOrNull {
                it.tagName == selectedPlanTag
            }
            if (selectedPlan == null) {
                Timber.tag(TAG).e("Selected Plan is null somehow")
                showMessage(R.string.something_went_wrong_try_later)
                return@setOnClickListener
            }

            val products = ImmutableList.builder<QueryProductDetailsParams.Product>()

            if (viewModel.currentRemoteUser?.subscription == null) {
                // user first time to subscribe
                products.add(
                    QueryProductDetailsParams
                        .Product
                        .newBuilder()
                        .setProductId(selectedPlan.googlePlayId)
                        .setProductType(ProductType.SUBS)
                        .build()
                )
                val queryProductDetailsParams = QueryProductDetailsParams
                    .newBuilder()
                    .setProductList(products.build())
                    .build()
                showLoading()
                billingClient.queryProductDetailsAsync(queryProductDetailsParams) { result, productDetailsList ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        runOnUiThread {
                            hideLoading()
                            processBilling(productDetailsList, selectedPlan)
                        }
                    } else {
                        runOnUiThread {
                            hideLoading()
                            showMessage(R.string.something_went_wrong_try_later)
                            Timber.tag(TAG).v("Error response from google: ${result.debugMessage}")
                        }
                    }
                }
            } else {
                // User try to upgrade or resubscribe (only the upgrade is supported for the moment)

                products.add(
                    QueryProductDetailsParams
                        .Product
                        .newBuilder()
                        .setProductId(selectedPlan.googlePlayId)
                        .setProductType(ProductType.SUBS)
                        .build()
                )
                val queryProductDetailsParams = QueryProductDetailsParams
                    .newBuilder()
                    .setProductList(products.build())
                    .build()
                showLoading()

                if (viewModel.currentRemoteUser?.subscription?.status == "SUBSCRIPTION_STATE_EXPIRED"
                    || viewModel.currentRemoteUser?.subscription?.status == "SUBSCRIPTION_STATE_CANCELED"
                ) {
                    if (viewModel.currentRemoteUser?.subscription?.googlePlayToken.isNullOrBlank()) {
                        Timber.tag(TAG)
                            .e("The user current subscription google token is null or empty")
                        return@setOnClickListener
                    }
                    billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            runOnUiThread {
                                hideLoading()
                            }
                            processBilling(productDetailsList, selectedPlan)
                        }
                    }
                } else if (viewModel.currentRemoteUser?.subscription?.status == "SUBSCRIPTION_STATE_ACTIVE") {
                    billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            runOnUiThread {
                                hideLoading()
                            }
                            processUpgrade(productDetailsList, selectedPlan)
                        }
                    }
                }
            }
        }
    }

    private fun processBilling(productDetailsList: List<ProductDetails>, selectedPlan: PlanDto) {
        val subProduct =
            productDetailsList.firstOrNull { it.productId == selectedPlan.googlePlayId }
        if (subProduct == null) {
            Timber.tag(TAG)
                .e("Somehow the products from google does not have the selected plan, or plan googleId is incorrect")
            return
        }
        val currentYearlyDiscountId = viewModel.appConfig.value?.googleIdDiscount

        if (selectedPlan.tagName == "1_year" && currentYearlyDiscountId != null && isUserEligibleForYearlyDiscount) {
            subProduct.subscriptionOfferDetails?.firstOrNull {
                it.offerId == currentYearlyDiscountId
            }.also { yearlyOffer ->
                if (yearlyOffer == null) {
                    Timber.tag(TAG)
                        .v("Somehow the yearly offer from google is null, check app config yearly discount id")
                    subProduct
                        .subscriptionOfferDetails?.firstOrNull { it.basePlanId == selectedPlan.basePlanGoogleId }
                        ?.also { baseYearlyOffer ->
                            val billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(
                                    ImmutableList.of(
                                        ProductDetailsParams.newBuilder()
                                            .setProductDetails(subProduct)
                                            .setOfferToken(baseYearlyOffer.offerToken)
                                            .build()
                                    )
                                )
                                .build()
                            billingClient.launchBillingFlow(
                                this@PlansActivity,
                                billingFlowParams
                            )
                        }
                } else {
                    Timber.tag(TAG).v("Starting the billing with the year offer details")
                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(
                            ImmutableList.of(
                                ProductDetailsParams.newBuilder()
                                    .setProductDetails(subProduct)
                                    .setOfferToken(yearlyOffer.offerToken)
                                    .build()
                            )
                        )
                        .build()
                    billingClient.launchBillingFlow(
                        this@PlansActivity,
                        billingFlowParams
                    )
                }
            }
        } else {            // here the discount is disabled for all plans
            subProduct.subscriptionOfferDetails?.firstOrNull {
                it.basePlanId == selectedPlan.basePlanGoogleId
            }.also { baseOffer ->
                if (baseOffer == null) {
                    Timber.tag(TAG).v("Somehow the base offer from google is null")
                    return
                }
                Timber.tag(TAG)
                    .v("Starting the billing process for the ${selectedPlan.tagName} plan...")
                val params = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        ImmutableList.of(
                            ProductDetailsParams.newBuilder()
                                .setProductDetails(subProduct)
                                .setOfferToken(baseOffer.offerToken)
                                .build()
                        )
                    )
                    .build()
                billingClient.launchBillingFlow(
                    this@PlansActivity,
                    params
                )
            }
        }
    }


    /**
     *  Be sure to call this method from a plan that the user can upgrade from.
     */
    private fun processUpgrade(productDetailsList: List<ProductDetails>, selectedPlan: PlanDto) {
        val subProduct =
            productDetailsList.firstOrNull { it.productId == selectedPlan.googlePlayId }
        if (subProduct == null) {
            Timber.tag(TAG)
                .e("processUpgrade@ Somehow the products from google does not have the selected plan, or plan googleId is incorrect")
            return
        }
        val currentYearlyDiscountId = viewModel.appConfig.value?.googleIdDiscount
        if (selectedPlan.tagName == "1_year" && currentYearlyDiscountId != null && isUserEligibleForYearlyDiscount) {
            subProduct.subscriptionOfferDetails?.firstOrNull {
                it.offerId == currentYearlyDiscountId
            }.also { yearlyOffer ->
                if (yearlyOffer == null) {
                    Timber.tag(TAG)
                        .v("Somehow the yearly offer from google is null, check app config yearly discount id")
                    subProduct
                        .subscriptionOfferDetails?.firstOrNull { it.basePlanId == selectedPlan.basePlanGoogleId }
                        ?.also { baseYearlyOffer ->
                            val billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(
                                    ImmutableList.of(
                                        ProductDetailsParams.newBuilder()
                                            .setProductDetails(subProduct)
                                            .setOfferToken(baseYearlyOffer.offerToken)
                                            .build()
                                    )
                                )

                                .setSubscriptionUpdateParams(
                                    SubscriptionUpdateParams.newBuilder()
                                        .setOldPurchaseToken(viewModel.currentRemoteUser?.subscription?.googlePlayToken!!)
                                        .setSubscriptionReplacementMode(ReplacementMode.WITHOUT_PRORATION)
                                        .build()
                                )

                                .build()
                            billingClient.launchBillingFlow(this@PlansActivity, billingFlowParams)
                        }
                } else {
                    Timber.tag(TAG)
                        .v("Starting the billing upgrade: with the year offer details")
                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(
                            ImmutableList.of(
                                ProductDetailsParams.newBuilder()
                                    .setProductDetails(subProduct)
                                    .setOfferToken(yearlyOffer.offerToken)
                                    .build()
                            )
                        )
                        .setSubscriptionUpdateParams(
                            SubscriptionUpdateParams.newBuilder()
                                .setOldPurchaseToken(viewModel.currentRemoteUser?.subscription?.googlePlayToken!!)
                                .setSubscriptionReplacementMode(ReplacementMode.WITHOUT_PRORATION)
                                .build()
                        )
                        .build()
                    billingClient.launchBillingFlow(this@PlansActivity, billingFlowParams)
                }
            }
        } else {
            Timber.tag(TAG).v("upgrade@ User not eligible for a discount")
            subProduct
                .subscriptionOfferDetails?.firstOrNull { it.basePlanId == selectedPlan.basePlanGoogleId }
                ?.also { baseYearlyOffer ->
                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(
                            ImmutableList.of(
                                ProductDetailsParams.newBuilder()
                                    .setProductDetails(subProduct)
                                    .setOfferToken(baseYearlyOffer.offerToken)
                                    .build()
                            )
                        )
                        .setSubscriptionUpdateParams(
                            SubscriptionUpdateParams.newBuilder()
                                .setOldPurchaseToken(viewModel.currentRemoteUser?.subscription?.googlePlayToken!!)
                                .setSubscriptionReplacementMode(ReplacementMode.WITHOUT_PRORATION)
                                .build()
                        )
                        .build()
                    billingClient.launchBillingFlow(this@PlansActivity, billingFlowParams)
                }
        }
    }


    private fun initGoogleBilling() {
        Timber.tag(TAG).v("initGoogleBilling called...")
        billingClient = BillingClient
            .newBuilder(this@PlansActivity)
            .enablePendingPurchases()
            .setListener(purchasesUpdatedListener)
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Timber.tag(TAG).i("Service Disconnected")
                if (playConnectionMaxAttempts > 0) {
                    playConnectionMaxAttempts -= 1
                    handler.postDelayed({
                        initGoogleBilling()
                    }, 1000)
                } else {
                    showMessage(R.string.something_went_wrong_try_later)
                    finish()
                    Timber.tag("PlansActivity").e("Can't connect to play services.")
                }
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                runOnUiThread {
                    viewModel.startSubscriptionsProcess()
                }
            }
        })
    }

    private fun setCurrentSubscriptionView() {
        if (viewModel.currentRemoteUser?.subscription == null || viewModel.currentRemoteUser?.subscription?.status == "SUBSCRIPTION_STATE_EXPIRED") {
            selectOneYearSubscription()
            return
        }
        when (viewModel.currentRemoteUser?.subscription?.plan?.tagName) {
            "1_month" -> {
                selectSixMonthsSubscription()
                binding.subscribe.text = getString(R.string.upgrade)
            }

            "6_months" -> {
                selectOneYearSubscription()
                binding.subscribe.text = getString(R.string.upgrade)
            }

            "1_year" -> {
                selectOneYearSubscription()
                disableSubscribeButton()
            }
        }
    }

    enum class UiPlan {
        ONE_YEAR, SIX_MONTHS, ONE_MONTH
    }

    private fun handleUserPlansClick(uiPlan: UiPlan) {
        when (uiPlan) {
            UiPlan.ONE_MONTH -> {
                if (viewModel.currentRemoteUser?.subscription == null || viewModel.currentRemoteUser?.subscription?.status == "SUBSCRIPTION_STATE_EXPIRED") {
                    selectOneMonthSubscription()
                    return
                }

                when (viewModel.currentRemoteUser?.subscription?.plan?.tagName) {
                    "1_month" -> {
                        selectOneMonthSubscription()
                        disableSubscribeButton()
                    }

                    "6_months" -> {
                        selectOneMonthSubscription()
                        disableSubscribeButton()
                    }

                    "1_year" -> {
                        selectOneMonthSubscription()
                        disableSubscribeButton()
                    }
                }
            }

            UiPlan.SIX_MONTHS -> {
                if (viewModel.currentRemoteUser?.subscription == null || viewModel.currentRemoteUser?.subscription?.status == "SUBSCRIPTION_STATE_EXPIRED") {
                    selectSixMonthsSubscription()
                    return
                }

                when (viewModel.currentRemoteUser?.subscription?.plan?.tagName) {
                    "1_month" -> {
                        selectSixMonthsSubscription()
                        enableSubscribeButton()
                        setSubscribeButtonText(viewModel.currentRemoteUser?.subscription?.status!!)
                    }

                    "6_months" -> {
                        disableSubscribeButton()
                        selectSixMonthsSubscription()
                    }

                    "1_year" -> {
                        disableSubscribeButton()
                        selectSixMonthsSubscription()
                    }
                }
            }

            UiPlan.ONE_YEAR -> {
                if (viewModel.currentRemoteUser?.subscription == null || viewModel.currentRemoteUser?.subscription?.status == "SUBSCRIPTION_STATE_EXPIRED") {
                    selectOneYearSubscription()
                    return
                }

                when (viewModel.currentRemoteUser?.subscription?.plan?.tagName) {
                    "1_month" -> {
                        selectOneYearSubscription()
                        enableSubscribeButton()
                        setSubscribeButtonText(viewModel.currentRemoteUser?.subscription?.status!!)
                    }

                    "6_months" -> {
                        selectOneYearSubscription()
                        enableSubscribeButton()
                        setSubscribeButtonText(viewModel.currentRemoteUser?.subscription?.status!!)
                    }

                    "1_year" -> {
                        disableSubscribeButton()
                        selectOneYearSubscription()
                    }
                }
            }
        }
    }

    private fun setSubscribeButtonText(subscriptionStatus: String) {
        when (subscriptionStatus) {
            "SUBSCRIPTION_STATE_ACTIVE" -> binding.subscribe.text = getString(R.string.upgrade)
            "SUBSCRIPTION_STATE_EXPIRED", "SUBSCRIPTION_STATE_CANCELED" -> binding.subscribe.text =
                getString(R.string.re_subscribe)

            // can show a status text based on nex status
            "SUBSCRIPTION_STATE_PAUSED" -> disableSubscribeButton()
            "SUBSCRIPTION_STATE_UNSPECIFIED" -> disableSubscribeButton()
            "SUBSCRIPTION_STATE_ON_HOLD" -> disableSubscribeButton()
            "SUBSCRIPTION_STATE_PENDING" -> disableSubscribeButton()
            "SUBSCRIPTION_STATE_IN_GRACE_PERIOD" -> disableSubscribeButton()
        }
    }

    private fun getGoogleSubscriptionDetails(plans: List<PlanDto>) {
        val products = ImmutableList.builder<QueryProductDetailsParams.Product>()
        plans.forEach { plan ->
            products.add(
                QueryProductDetailsParams
                    .Product
                    .newBuilder()
                    .setProductId(plan.googlePlayId)
                    .setProductType(ProductType.SUBS)
                    .build()
            )
        }
        val queryProductDetailsParams = QueryProductDetailsParams
            .newBuilder()
            .setProductList(products.build())
            .build()
        if (billingClient.isReady) {
            initPrices(queryProductDetailsParams)
        } else {
            showMessage(R.string.something_went_wrong)
        }
    }

    private fun initPrices(params: QueryProductDetailsParams) {
        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                runOnUiThread {
                    productDetailsList.forEach { productDetails ->
                        when (productDetails.productId) {
                            "1_month_subscription" -> {
                                val offerDetails =
                                    productDetails.subscriptionOfferDetails?.firstOrNull { it.basePlanId == "one-month" }
                                offerDetails?.pricingPhases?.pricingPhaseList?.forEach { pricePhase ->
                                    Timber.tag(TAG)
                                        .v("1 Month Price: ${pricePhase.formattedPrice}")
                                    binding.subscription1Month.setPriceText(pricePhase.formattedPrice)
                                }
                            }

                            "6_months_subscription" -> {
                                val offerDetails =
                                    productDetails.subscriptionOfferDetails?.firstOrNull { it.basePlanId == "six-months" }
                                offerDetails?.pricingPhases?.pricingPhaseList?.forEach { pricePhase ->
                                    Timber.tag(TAG)
                                        .v("6 Months Price: ${pricePhase.formattedPrice}")
                                    binding.subscription6Months.setPriceText(pricePhase.formattedPrice)
                                }
                            }

                            "1_year_subscription" -> {
                                val discountId = viewModel.appConfig.value?.googleIdDiscount
                                productDetails.subscriptionOfferDetails?.firstOrNull {
                                    it.offerId == discountId && it.offerTags.contains("one-year-discount")
                                }.also { offerDetails ->
                                    if (offerDetails == null) {
                                        isUserEligibleForYearlyDiscount = false
                                        binding.subscription1Year.setFlagText("")
                                        val baseDetails =
                                            productDetails.subscriptionOfferDetails?.first { it.basePlanId == "one-year" }
                                        val price =
                                            baseDetails?.pricingPhases?.pricingPhaseList?.get(0)!!.formattedPrice
                                        Timber.tag(TAG).v("1 Year Price: price")
                                        binding.subscription1Year.setPriceText(price)
                                    } else {
                                        isUserEligibleForYearlyDiscount = true
                                        binding.subscription1Year.setFlagText("Save 50%")
                                        Timber.tag(TAG)
                                            .v("1 Year Price: ${offerDetails.pricingPhases.pricingPhaseList[0]!!.formattedPrice}")
                                        Timber.tag(TAG)
                                            .v("1 Year offer old price: ${offerDetails.pricingPhases.pricingPhaseList[1]!!.formattedPrice}")
                                        binding.subscription1Year.setPriceText(offerDetails.pricingPhases.pricingPhaseList[0]!!.formattedPrice)
                                    }
                                }
                            }
                        }
                    }
                    viewModel.pricesInitDone()
                }
            } else {
                Timber.tag("PlansActivity").e("setupPrices- %s", billingResult.debugMessage)
                runOnUiThread {
                    showMessage(R.string.something_went_wrong)
                }
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }


    private fun showPlansProgressBar() {
        binding.progressView.visibility = View.VISIBLE
        binding.progressView.setContent {
            InstaDownloaderCircularProgressView()
        }
    }

    private fun hidePlansProgressBar() {
        binding.progressView.visibility = View.GONE
        binding.subscription1Month.visibility = View.VISIBLE
        binding.subscription6Months.visibility = View.VISIBLE
        binding.subscription1Year.visibility = View.VISIBLE
    }

    private fun selectOneMonthSubscription() {
        selectedPlanTag = "1_month"
        binding.subscription1Month.setIsSelected(true)
        binding.subscription6Months.setIsSelected(false)
        binding.subscription1Year.setIsSelected(false)
    }

    private fun selectSixMonthsSubscription() {
        selectedPlanTag = "6_months"
        binding.subscription1Month.setIsSelected(false)
        binding.subscription6Months.setIsSelected(true)
        binding.subscription1Year.setIsSelected(false)
    }

    private fun selectOneYearSubscription() {
        selectedPlanTag = "1_year"
        binding.subscription1Month.setIsSelected(false)
        binding.subscription6Months.setIsSelected(false)
        binding.subscription1Year.setIsSelected(true)
    }

    private fun disableSubscribeButton() {
        binding.subscribe.isEnabled = false
        binding.subscribe.alpha = .5f
    }

    private fun enableSubscribeButton() {
        binding.subscribe.isEnabled = true
        binding.subscribe.alpha = 1.0f
    }

    private fun showWelcomeToPremiumDialog() {
        val dialog = BaseAlertDialog(this)
        dialog.isPremiumCelebration = true
        dialog.addButton(
            R.string.ok,
            isPrimary = true,
            isLoginButton = false,
            autoDismiss = true,
        ) { _, _ ->
            setResult(RESULT_OK)
            finish()
        }
        dialog.setTitle(R.string.subscription_successful)
        dialog.setMessage(R.string.subscription_successful_message)
        dialog.setExtraCloseButtonVisible(false)
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::billingClient.isInitialized && billingClient.isReady) {
            billingClient.endConnection()
        }
    }

}