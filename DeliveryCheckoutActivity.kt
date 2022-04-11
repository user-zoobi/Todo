package com.app.cheflick.ui.CheckOutScreens

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.app.cheflick.Model.Checkout
import com.app.cheflick.R
import com.app.cheflick.data.local.responses.*
import com.app.cheflick.data.remote.ApiService
import com.app.cheflick.data.remote.State
import com.app.cheflick.data.remote.responses.dishDetailResponseUpdated.DishDetailData
import com.app.cheflick.data.remote.responses.dishDetailResponseUpdated.Sideline
import com.app.cheflick.data.remote.responses.kitchenDetailUpdatedResponse.KitchenDetailUpdatedData
import com.app.cheflick.databinding.ActivityDeliveryCheckoutBinding
import com.app.cheflick.di.modules.NetworkModule_ProvideRetrofitFactory
import com.app.cheflick.extension.*
import com.app.cheflick.ui.CheckOutScreens.BottomSheet.WaitingScreenBottomSheet
import com.app.cheflick.ui.DatePicker.DatePickerActivity
import com.app.cheflick.ui.Map.SelectLocationActivity
import com.app.cheflick.utils.Constants
import com.app.cheflick.utils.Constants.BASE_URL
import com.app.cheflick.utils.Constants.CART
import com.app.cheflick.utils.Constants.DELIVERY
import com.app.cheflick.utils.Constants.PICKUP
import com.app.cheflick.utils.Constants.PREORDER
import com.app.cheflick.utils.Constants.SAME_DAY
import com.app.cheflick.utils.SharedPreferences
import com.app.cheflick.viewModel.AppViewModel
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.paperdb.Paper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject

import retrofit2.Retrofit


class DeliveryCheckoutActivity : AppCompatActivity() {

    lateinit var binding: ActivityDeliveryCheckoutBinding
    lateinit var waitingScreenBottomSheet: WaitingScreenBottomSheet
    private var AddonList = arrayListOf<CartAddon>()
    private var dishDetails: DishDetailData? = null
    var checkoutBodyModel: CheckoutBodyModel? = null
    var KitchenDetailUpdated: KitchenDetailUpdatedData? = null
    var checkOutResponses: CheckoutResponse? = null
    private var cartSidelineList = arrayListOf<CartSideline>()
    private val viewModel: AppViewModel by viewModels()
    private lateinit var tempObj: CheckoutBodyModel
    lateinit var api: ApiService
    lateinit var network: NetworkModule_ProvideRetrofitFactory
    var subtotal: Double = 0.0
    var gst: Double = 0.0
    var grandtotal: Double = 0.0
    var discounted_Amount: Double = 0.0
    var modeOfDelivery = ""
    var orderMode = ""
    var orderType = ""
    var addressId = ""
    private lateinit var checkOut:JSONObject
    var launchForDatePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val selectedDate = data.getStringExtra("SelectedDate")
                    binding.date.text = selectedDate
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeliveryCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.setTransparentStatusBarColor(R.color.transparent)

        checkoutBodyModel = Paper.book().read(CART)
        subtotal = Paper.book().read<Double>("subtotal")!!
        gst = Paper.book().read<Double>("gst")!!
        grandtotal = Paper.book().read<Double>("grandtotal")!!
        discounted_Amount = Paper.book().read<Double>("discounted_Amount")!!
        orderMode = SharedPreferences.getOrderType(this)
        initlistener()
        setupUI()
        initObserver()

    }

    private fun initObserver(){

        viewModel.checkout.observe(this) {

            when (it.status) {
                State.SUCCESS -> {
//            val checkout:CheckoutBodyModel= it.data!!
                    if (checkOutResponses?.equals(null) != true) {

                        checkOut = JSONObject()
                        checkOut.put("kitchenId","")
                        checkOut.put("orderMode",orderMode)
                        checkOut.put("addressId","1")

                        if (orderMode.lowercase() == PICKUP){
                            checkOut.put("paymentMethod","pickup")
                        }else if(orderMode.lowercase() == SAME_DAY){
                            checkOut.put("paymentMethod","sameday")
                        }

                        if (orderType.lowercase() == DELIVERY){
                            checkOut.put("orderType","delivery")
                        }else if(orderType.lowercase() == PREORDER){
                            checkOut.put("orderType","preorder")
                        }

                        checkOut.put("isVoucher","false")
                        checkOut.put("voucherId","")
                        checkOut.put("orderInstructions","Order Dispatched to be carefully.")
                        checkOut.put("gst",gst)
                        checkOut.put("discount",discounted_Amount)
                        checkOut.put("subTotal",subtotal)
                        checkOut.put("grandTotal",grandtotal)
                        checkOut.put("grandTotal",grandtotal)
                        checkOut.put("contactlessDelivery","1")
                        checkOut.put("deliveryAddress","adsf, Karachi")
                        checkOut.put("deliveryLongitude","")
                        checkOut.put("deliveryLatitude","")
                        checkOut.put("deliveryCharges","")
                        checkOut.put("orderTime","01:20AM")
                        checkOut.put("preOrderTime","")
                        checkOut.put("preOrderDate","")

                        val menus = JSONObject()
                        menus.put("ifNotAvailable","Remove my item")
                        menus.put("specialInstruction","Any Special Instruction")
                        menus.put("quantity","2")
                        menus.put("menuId","1")
                        menus.put("units","2")
                        checkOut.put("menus",menus)

                        val addons = JSONObject()
                        addons.put("addonId","1")
                        menus.put("addons",addons)

                        val sidelines = JSONObject()
                        sidelines.put("sidelineId","1")
                        addons.put("sidelines",sidelines)

                        Log.i("CHECKOUTMODEL", checkOut.toString())

//                            tempObj.kitchenId = it.data?.data?.kitchenId.toString()
//                            tempObj.addressId =
//                            tempObj.subTotal = binding.subTotalPrice.text.toString()
//                            tempObj.grandTotal = binding.totalPrice.text.toString()
//                            tempObj.orderMode = it.data?.data?.orderMode.toString()
//                            tempObj.paymentMethod = binding.switch1.text.toString()
//                            tempObj.orderType = it.data?.data?.orderType.toString()
//                            tempObj.isVoucher = it.data?.data?.isVoucher.toString()
//                            tempObj.gst = binding.GSTPrice.text.toString()
//                            tempObj.orderTime = binding.time.text.toString()
//                            tempObj.deliveryCharges = it.data?.data?.deliverycharges.toString()
////

//                            val retrofit = Retrofit.Builder().baseUrl(BASE_URL).build()
//                            val service = retrofit.create(ApiService::class.java)
//
//                            val jsonObjectString = tempObj.toString()
//                            val requestBody =
//                                jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())
//                            CoroutineScope(Dispatchers.IO).launch {
//                                val response = service.orderCheckOut(Checkout())
//                                withContext(Dispatchers.Main)
//                                {
//
//                                    if (response.status) {
//
//                                        val gson = GsonBuilder().setPrettyPrinting().create()
//                                        val pretyjson = gson.toJson(
//                                            JsonParser.parseString(response.data.toString())
//                                        )
////                                Log.i("check",Gson().toJson(pretyjson))
//
//                                        Log.i("testosama", pretyjson)
//                                        Log.i("test", tempObj.toString())
//                                    } else {
//                                        Log.i("RETROFIT_ERROR", response.message)
//                                    }
//                                }
//                            }

//                try {
//                    val orderdetails =JSONObject()
//                        orderdetails.put("subtotal",100)
//                    orderdetails.put("grandTotal",500)
//
////                    orderdetails.put("subTotal",binding.subTotalPrice.toString())
////                    orderdetails
////                    orderdetails.put("kitchenId",it.data?.data?.kitchenId)
////                    orderdetails.put("orderMode",it.data?.data?.orderMode)
////                    orderdetails.put("addressId",KitchenDetailUpdated?.kitchenAddress)
////                    orderdetails.put("paymentMethod",binding.switch1.text.toString())
////                    orderdetails.put("orderType",it.data?.data?.orderType)
////                    orderdetails.put("isVoucher",it.data?.data?.isVoucher)
////                    orderdetails.put("voucherId",checkoutBodyModel?.voucherId)
////                    orderdetails.put("orderInstructions",checkoutBodyModel?.orderInstructions)
////                    orderdetails.put("gst",gst.toString())
////                    orderdetails.put("discount",binding.deliveryPrice.text.toString())
////                    orderdetails.put("subTotal",subtotal.toString())
////                    orderdetails.put("grandTotal",grandtotal.toString())
////                    orderdetails.put("contactlessDelivery",checkoutBodyModel?.contactlessDelivery)
////                    orderdetails.put("deliveryAddress",binding.addressTv.text.toString())
////                    orderdetails.put("deliveryLongitude",checkoutBodyModel?.deliveryLongitude)
////                    orderdetails.put("deliveryLatitude",checkoutBodyModel?.deliveryLatitude)
////                    orderdetails.put("deliveryCharges",checkoutBodyModel?.deliverycharges)
////                    orderdetails.put("orderTime",binding.time.text.toString())
////                    orderdetails.put("preOrderTime",checkoutBodyModel?.preOrderTime)
////                    orderdetails.put("preOrderDate",checkoutBodyModel?.preOrderDate?.format("yyyy-MM-dd"))
//
                    }
                }
                State.ERROR -> {
//            dialog.dismiss()
                    binding.root.showSnack(it.data.toString())
                }
            }

//    try {
//
//        val orderdetails =JSONObject()
//        orderdetails.put("kitchenId",it.data?.data?.kitchenId)
//    orderdetails.put("orderMode",it.data?.data?.orderMode)
//    orderdetails.put("addressId",KitchenDetailUpdated?.kitchenAddress)
//    orderdetails.put("paymentMethod",binding.switch1.text.toString())
//    orderdetails.put("orderType",it.data?.data?.orderType)
//    orderdetails.put("isVoucher",it.data?.data?.isVoucher)
//    orderdetails.put("voucherId",checkoutBodyModel?.voucherId)
//    orderdetails.put("orderInstructions",checkoutBodyModel?.orderInstructions)
//    orderdetails.put("gst",gst.toString())
//    orderdetails.put("discount",binding.deliveryPrice.text.toString())
//    orderdetails.put("subTotal",subtotal.toString())
//    orderdetails.put("grandTotal",grandtotal.toString())
//    orderdetails.put("contactlessDelivery",checkoutBodyModel?.contactlessDelivery)
//    orderdetails.put("deliveryAddress",binding.addressTv.text.toString())
//    orderdetails.put("deliveryLongitude",checkoutBodyModel?.deliveryLongitude)
//    orderdetails.put("deliveryLatitude",checkoutBodyModel?.deliveryLatitude)
//    orderdetails.put("deliveryCharges",checkoutBodyModel?.deliverycharges)
//    orderdetails.put("orderTime",binding.time.text.toString())
//    orderdetails.put("preOrderTime",checkoutBodyModel?.preOrderTime)
//    orderdetails.put("preOrderDate",checkoutBodyModel?.preOrderDate?.format("yyyy-MM-dd"))


        }

    }

    private fun initlistener() {

        binding.cashRadioBtn.radioListener {
            binding.bankTransferRadioBtn.isChecked = false
            binding.visaRadioBtn.isChecked = false
//            binding.easyPesaRadioBtn.isChecked = false
//            binding.jazzCashRadioBtn.isChecked = false
        }

        binding.visaRadioBtn.radioListener {
            binding.bankTransferRadioBtn.isChecked = false
            binding.cashRadioBtn.isChecked = false
//            binding.easyPesaRadioBtn.isChecked = false
//            binding.jazzCashRadioBtn.isChecked = false
        }

        binding.bankTransferRadioBtn.radioListener {
            binding.visaRadioBtn.isChecked = false
            binding.cashRadioBtn.isChecked = false
//            binding.easyPesaRadioBtn.isChecked = false
//            binding.jazzCashRadioBtn.isChecked = false
        }

//        binding.easyPesaRadioBtn.radioListener {
//            binding.visaRadioBtn.isChecked = false
//            binding.cashRadioBtn.isChecked = false
//            binding.bankTransferRadioBtn.isChecked = false
//            binding.jazzCashRadioBtn.isChecked = false
//        }

//        binding.jazzCashRadioBtn.radioListener {
//            binding.visaRadioBtn.isChecked = false
//            binding.cashRadioBtn.isChecked = false
//            binding.bankTransferRadioBtn.isChecked = false
//            binding.easyPesaRadioBtn.isChecked = false
//        }

        binding.sendOrderBtn.setOnClickListener {

        waitingScreenBottomSheet = WaitingScreenBottomSheet()
        waitingScreenBottomSheet.show(supportFragmentManager,"")
            val token = SharedPreferences.getBearer(this)
            viewModel.orderCheckOut(token, checkOut)
            binding.root.showSnack("Order has been  successfully completed")
            toast("Order successfully")

        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.dateFields.setOnClickListener {
            val intent = Intent(this, DatePickerActivity::class.java)
            launchForDatePicker.launch(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left)
        }
        binding.time.setOnClickListener {
            setupTimePicker()
        }
        binding.changeAddress.setOnClickListener(View.OnClickListener {

            this.launchActivity<SelectLocationActivity>(false)
        })
    }

    private fun setupUI() {

//        binding.distance.text= KitchenDetailUpdated?.kitchenAddress.toString()
        binding.addressTv.text = SharedPreferences.getUserAddress(this)
        binding.orderType.text = SharedPreferences.getOrderType(this).capitalize()
        binding.subTotalPrice.text = subtotal.toString()
        binding.GSTPrice.text = gst.toString()
        binding.totalPrice.text = grandtotal.toString()
//        binding.deliveryPrice.text= discounted_Amount.toString()


    }

    private fun newSetupTimePicker() {
        SingleDateAndTimePickerDialog.Builder(this)
            .bottomSheet()
            .curved()
            .displayMinutes(true)
            .displayHours(true)
            .displayDays(false)
            .displayMonth(false)
            .displayYears(false)
            .displayDaysOfMonth(false)
            .display()
    }

    private fun setupTimePicker() {
        val picker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(10)
                .setTitleText("Select Time")
                .build()


        picker.show(supportFragmentManager, "tag")

        picker.addOnPositiveButtonClickListener {
            val hourOfDay = picker.hour
            val minute = picker.minute
            val formattedTime: String = when {
                hourOfDay == 0 -> {
                    if (minute < 10) {
                        "${hourOfDay + 12}:0${minute} Am"
                    } else {
                        "${hourOfDay + 12}:${minute} Am"
                    }
                }
                hourOfDay > 12 -> {
                    if (minute < 10) {
                        "${hourOfDay - 12}:0${minute} Pm"
                    } else {
                        "${hourOfDay - 12}:${minute} Pm"
                    }
                }
                hourOfDay == 12 -> {
                    if (minute < 10) {
                        "${hourOfDay}:0${minute} Pm"
                    } else {
                        "${hourOfDay}:${minute} Pm"
                    }
                }
                else -> {
                    if (minute < 10) {
                        "${hourOfDay}:${minute} Am"
                    } else {
                        "${hourOfDay}:${minute} Am"
                    }
                }
            }
            binding.time.text = formattedTime
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


}