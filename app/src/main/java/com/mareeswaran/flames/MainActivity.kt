package com.mareeswaran.flames

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextClock
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityOptionsCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var input1: TextInputEditText
    lateinit var input2: TextInputEditText
    lateinit var name1: CharArray
    lateinit var name2: CharArray
    var output: MutableList<Char> = ArrayList()

    var sum: Int = 0;
    var sum1: Int = 0;
    lateinit var bonding : String

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var nameinfo: Nameinfo
    private var list = mutableListOf<Nameinfo>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        input1 = findViewById(R.id.input1)
        input2 = findViewById(R.id.input2)

        firebaseDatabase = FirebaseDatabase.getInstance()

        databaseReference = firebaseDatabase.getReference("Couples-Info")
//        databaseReference = firebaseDatabase.getReference("CouplesData")
        databaseReference = firebaseDatabase.reference
        nameinfo = Nameinfo()

    }


    private fun sumOfDigits(num: Int): Int {
        var sum = 0
        var number = num
        while (number > 0) {
            sum += (number % 10)
            number /= 10
        }
        return sum
    }

    fun onMatchClicked(view: View?) {
//        output.clear()

        val boyName = input1!!.text.toString()
        val girlName = input2!!.text.toString()

        sum = 0
        for (i in 0 until boyName.length) {
            sum += boyName[i].toLowerCase().toInt()
        }
        sum1 = 0
        for (i in 0 until girlName.length) {
            sum1 += girlName[i].toLowerCase().toInt()
        }
        var perc = ((sumOfDigits(sum) + sumOfDigits(sum1)) + 40).toFloat()
        if (perc > 100)
            perc = 100f
        bonding = perc.toString()

        Log.d("Love_Percentage: ",bonding)

        if (TextUtils.isEmpty(boyName.replace(" ", "")) || TextUtils.isEmpty(girlName.replace(" ", ""))) {
            Snackbar.make(findViewById(R.id.main), "All fields are required", Snackbar.LENGTH_SHORT).show()
            return
        }
        if (boyName.toLowerCase() == girlName.toLowerCase()) {
            Snackbar.make(findViewById(R.id.main), "Are you sure with the names you have entered?", Snackbar.LENGTH_SHORT).show()
            return
        }

        //sample
        name1 = boyName.toLowerCase().toCharArray()
        name2 = girlName.toLowerCase().toCharArray()

        for (i in name1.indices) {
            for (j in name2.indices) {
                if (name1[i] == name2[j]) {
                    name1[i] = ' '
                    name2[j] = ' '
                    break
                }
            }
        }
        for (a in name1) {
            if (a == ' ') continue
            output.add(a)
        }
        for (a in name2) {
            if (a == ' ') continue
            output.add(a)
        }

        Log.i("OUTPUT", output.toString())

        var relationIs = 0.toChar()
        val resultLength = output.size
        var baseInput = "Flames"
        var temp: String
        if (resultLength > 0) {
            while (baseInput.length != 1) {
                Log.i("OUTPUT", baseInput)
                val tmpLen = resultLength % baseInput.length //finding char position to strike
                temp = if (tmpLen != 0) {
                    baseInput.substring(tmpLen) + baseInput.substring(0, tmpLen - 1) //Append part start from next char to strike and first charater to char before strike.
                } else {
                    baseInput.substring(0, baseInput.length - 1)
                }
                baseInput = temp //Assign the temp to baseinput for next iteration.
            }
            relationIs = baseInput[0]
            Log.i("OUTPUT", relationIs.toString())
            addDatatoFirebase(boyName, girlName, relationIs.toString())

        }

        val intent = Intent(applicationContext, ResultActivity::class.java)
        intent.putExtra("boyName", boyName)
        intent.putExtra("girlName",girlName)
        intent.putExtra("percentage",bonding)
        intent.putExtra("result", relationIs.toString())
        val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, findViewById(R.id.title), "title")
        input1?.setText("")
        input2?.setText("")
        startActivity(intent, optionsCompat.toBundle())
    }


    private fun addDatatoFirebase(name: String, gname: String, outres: String) {

        val sanitizedBoyName = name.replace(" ", "").toLowerCase()
        val sanitizedGirlName = gname.replace(" ", "").toLowerCase()

        val uniqueKey = "${sanitizedBoyName}-$sanitizedGirlName"

        nameinfo.inboyName = name
        nameinfo.ingirlName = gname
        nameinfo.outResult = outres

        val newEntryRef = databaseReference.child(uniqueKey)

        newEntryRef.setValue(nameinfo)
            .addOnCanceledListener {
                Log.d("Firebase", "Data added successfully")
                Toast.makeText(this@MainActivity, "Data added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                // Handle failure
                Log.e("Firebase", "Failed to add data: ${error.message}")
                Toast.makeText(this@MainActivity, "Failed to add data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

}