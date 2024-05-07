package com.example.myapp_movie

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.button.MaterialButton
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    // Variable to store the last purchase details
    private var lastPurchase: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

            val buttonGetTickets: Button = findViewById(R.id.buttonGetTickets)
        buttonGetTickets.setOnClickListener {
            showDialog()
        }

        val buttonMyTickets: Button = findViewById(R.id.buttonMyTicket)
        buttonMyTickets.setOnClickListener {
            showMyTicketsDialog()
        }
        val switcher: SwitchCompat = findViewById(R.id.my_switcher)
        setupSwitcher(switcher)

        val buttonRate: Button = findViewById(R.id.buttonRate)
        buttonRate.setOnClickListener {
            showRatingDialog()
        }

    }
    private fun setupSwitcher(switcher: SwitchCompat) {
        switcher.setOnCheckedChangeListener { _, isChecked ->
            applyTheme(isChecked)
        }
    }
    private fun applyTheme(isDarkMode: Boolean) {
        val backgroundColor = if (isDarkMode) Color.BLACK else Color.WHITE
        val buttonBackgroundColor = if (isDarkMode) Color.WHITE else Color.BLACK
        val textColor = if (isDarkMode) Color.WHITE else Color.BLACK
        val buttonTextColor = if (isDarkMode) Color.BLACK else Color.WHITE
        findViewById<View>(android.R.id.content).setBackgroundColor(backgroundColor)
        findViewById<TextView>(R.id.textViewSummary).setTextColor(textColor)
        findViewById<Button>(R.id.buttonMyTicket).setBackgroundColor(buttonBackgroundColor)
        findViewById<Button>(R.id.buttonGetTickets).setBackgroundColor(buttonBackgroundColor)
        findViewById<Button>(R.id.buttonRate).setBackgroundColor(buttonBackgroundColor)
        findViewById<Button>(R.id.buttonMyTicket).setBackgroundColor(buttonBackgroundColor)
        findViewById<Button>(R.id.buttonGetTickets).setTextColor(buttonTextColor)
        findViewById<Button>(R.id.buttonMyTicket).setTextColor(buttonTextColor)
        findViewById<Button>(R.id.buttonRate).setTextColor(buttonTextColor)
    }


    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun showDialog() {
        val dialog = Dialog(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.get_tickets_dialog, null)
        dialog.setContentView(dialogView)

        // Load animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)


        val buttonChooseDate: Button = dialog.findViewById(R.id.buttonChooseDate)
        val textViewChosenDate: TextView = dialog.findViewById(R.id.textViewChosenDate)
        val editTextNumberAdultTickets: EditText = dialog.findViewById(R.id.editTextNumberAdultTickets)
        val editTextNumberChildTickets: EditText = dialog.findViewById(R.id.editTextNumberChildTickets)
        val textViewTotalPrice: TextView = dialog.findViewById(R.id.textViewTotalPrice)
        val textViewAlert: TextView = dialog.findViewById(R.id.textViewAlert)
        val buttonConfirm: Button = dialog.findViewById(R.id.buttonConfirm)
        val spinnerTheater: Spinner = dialog.findViewById(R.id.spinnerTheater)
        val timeSpinner: Spinner = dialogView.findViewById(R.id.spinnerTime)

        // Start concurrent animations
        editTextNumberAdultTickets.startAnimation(fadeIn)
        editTextNumberChildTickets.startAnimation(fadeIn)

        val times = (0..23).map { String.format("%02d:00", it) }
        val timeOptions = listOf("") + times

        val timeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, timeOptions)
        timeSpinner.adapter = timeAdapter

        timeSpinner.setSelection(0)

        val updateTotalCost = {
            var adultTickets = editTextNumberAdultTickets.text.toString().toIntOrNull() ?: 0
            var childTickets = editTextNumberChildTickets.text.toString().toIntOrNull() ?: 0
            var totalCost =
                (adultTickets * 10) + (childTickets * 5) // $10 per adult, $5 per child
            textViewTotalPrice.text = getString(R.string.total_cost) + ": $$totalCost"
        }

        editTextNumberAdultTickets.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateTotalCost()
                }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        editTextNumberChildTickets.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateTotalCost()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        // Setup DatePicker
        buttonChooseDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                textViewChosenDate.text = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
            }, year, month, day)

            datePickerDialog.datePicker.minDate = calendar.timeInMillis // Set minimum date to today
            datePickerDialog.show()
        }


        // Setup Ticket Purchase Logic
        buttonConfirm.setOnClickListener {
            var adultTickets = editTextNumberAdultTickets.text.toString().toIntOrNull() ?: 0
            var childTickets = editTextNumberChildTickets.text.toString().toIntOrNull() ?: 0
            val selectedTheater = spinnerTheater.selectedItem?.toString()
            val selectedTime = timeSpinner.selectedItem?.toString()
            var sum = childTickets + adultTickets
            if (sum>500){
                textViewAlert.text = getString(R.string.you_cant_buy_more_than_500_tickets)
                return@setOnClickListener
            }
            if (adultTickets == 0 && childTickets == 0) {
                textViewAlert.text = getString(R.string.please_select_at_least_one_ticket)
                return@setOnClickListener
            }

            if (textViewChosenDate.text.toString() == getString(R.string.date_not_selected)) {
                textViewAlert.text = getString(R.string.please_choose_a_date)
                return@setOnClickListener
            }
            if (selectedTime == "") {
                textViewAlert.text = getString(R.string.please_select_time)
                return@setOnClickListener
            }
            if (selectedTheater == "") {
                textViewAlert.text = getString(R.string.please_select_a_theater)
                return@setOnClickListener
            }
            var totalCost =
                (adultTickets * 10) + (childTickets * 5) // $10 per adult, $5 per child
            textViewTotalPrice.text = getString(R.string.total_cost)+": $totalCost"
            lastPurchase = "${getString(R.string.adult_tickets)}: $adultTickets\n" +
                    "${getString(R.string.child_tickets)}: $childTickets\n" +
                    "${getString(R.string.date)}: ${textViewChosenDate.text}\n" +
                    "${getString(R.string.time)}: $selectedTime\n" +
                    "${getString(R.string.theater)}: $selectedTheater\n" +
                    "${getString(R.string.total_cost)}: $$totalCost\n" +
                    getString(R.string.order_status_ok)

            dialog.dismiss() // Close the dialog if all conditions are met
        }

        val theaters = arrayOf("Cinema-city", "Yes-planet")
        val theaterOptions = listOf("") + theaters  // Add an empty string as the first item
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, theaterOptions)
        spinnerTheater.adapter = adapter
        spinnerTheater.setSelection(0)

        // Set sequential animation for buttonConfirm after fadeIn
        slideUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                buttonConfirm.startAnimation(fadeIn)
            }
        })
        buttonConfirm.startAnimation(slideUp)

        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showMyTicketsDialog() {
        val dialog = Dialog(this)
        // Use LayoutInflater to inflate the layout
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.my_tickets_dialog, null)
        dialog.setContentView(dialogView)

        val textViewTickets: TextView = dialogView.findViewById(R.id.textViewTickets)

        if (lastPurchase != null) {
            textViewTickets.text = lastPurchase
        } else {
            textViewTickets.text = getString(R.string.you_have_no_tickets)
        }

        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showRatingDialog() {
        val dialog = Dialog(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.rating_dialog, null)
        dialog.setContentView(dialogView)
        val seekBar = dialogView.findViewById<SeekBar>(R.id.seekBarRating)
        val textViewRating = dialogView.findViewById<TextView>(R.id.textViewRating)

        // Set initial rating text
        textViewRating.text = "Rating: ${seekBar.progress}/10"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update the rating text dynamically as the user slides the seek bar
                textViewRating.text = "Rating: $progress/10"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        dialogView.findViewById<Button>(R.id.buttonSubmitRate).setOnClickListener {
            Toast.makeText(this, getString(R.string.thanks_for_rating), Toast.LENGTH_LONG).show()
            dialog.dismiss()  // Close the dialog
        }

        dialog.show()
    }
}
