package com.example.weatherapp.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.adapter.ForecastAdapter
import com.example.weatherapp.adapter.TodayAdapter
import com.example.weatherapp.databinding.FragmentHomeBinding
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalTime

class HomeFragment : Fragment() {
    private val apiUrl = "https://api.weatherapi.com/v1/forecast.json?key=11b9394e7e024a2588a44954230610&q=Tashkent&days=8&aqi=no&alerts=no"
    private lateinit var binding: FragmentHomeBinding
    var forecastAdapter = ForecastAdapter(JSONArray(), object : ForecastAdapter.ItemClickInterface{
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onParentClick(day: JSONObject, position: Int) {
            changeToday(day, position)
        }
    })
    var todayAdapter = TodayAdapter(JSONArray(), 0)
    @RequiresApi(Build.VERSION_CODES.O)
    var fromHour = LocalTime.now().hour

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val requestQue = Volley.newRequestQueue(requireContext())

        binding.forecastRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.todayRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)


        val request = JsonObjectRequest(apiUrl,
            { response ->
                val current = response.getJSONObject("current")
                val tempC = current.getDouble("temp_c")
                val windKph = current.getDouble("wind_kph")
                val humidity = current.getInt("humidity")

                binding.windSpeed.text = "${windKph}km/h"
                binding.humidity.text = "${humidity}%"
                binding.temp.text = "${tempC.toInt()} C°"

                forecastAdapter = ForecastAdapter(response.getJSONObject("forecast").getJSONArray("forecastday"), object : ForecastAdapter.ItemClickInterface{
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onParentClick(day: JSONObject, position: Int) {
                        changeToday(day, position)
                    }
                })
                binding.forecastRv.adapter = forecastAdapter
                todayAdapter = TodayAdapter(response.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(0).getJSONArray("hour"), fromHour)
                binding.todayRv.adapter = todayAdapter
                binding.icon.load("https:" + current.getJSONObject("condition").getString("icon"))
                forecastAdapter.notifyDataSetChanged()
            }
        ) { error -> Log.d("TAG", "onErrorResponse: $error") }
        requestQue.add(request)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun changeToday(day: JSONObject, position:Int){
        if (position == 0){
            binding.todayAdapterText.text = "Today"
            fromHour = LocalTime.now().hour
        }else{
            binding.todayAdapterText.text = day.getString("date")
            fromHour = 0
        }
        todayAdapter.hours = day.getJSONArray("hour")
        todayAdapter.from = fromHour
        todayAdapter.notifyDataSetChanged()
    }
}