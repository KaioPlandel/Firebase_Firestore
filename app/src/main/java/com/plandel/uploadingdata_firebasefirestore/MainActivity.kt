package com.plandel.uploadingdata_firebasefirestore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.plandel.uploadingdata_firebasefirestore.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    private val personCollectionRef = Firebase.firestore.collection("persons")
    lateinit var binding: ActivityMainBinding
    companion object {
        val TAG = "TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //subscribeToRealtimeUpdates()

        binding.buttonSave.setOnClickListener {
            val firstName = binding.editFirstName.text.toString()
            val lastName = binding.editLastName.text.toString()
            val age = Integer.parseInt(binding.editAge.text.toString())
            val person = Person(firstName,lastName,age)
            savePerson(person)
        }

        binding.buttonRetrieve.setOnClickListener {
            retrievePersons()
        }
    }

    private fun subscribeToRealtimeUpdates() {
        personCollectionRef.addSnapshotListener{querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            querySnapshot?.let {
                val sb = StringBuilder()
                for (document in it) {
                    val person = document.toObject<Person>()
                    sb.append("${person}\n")
                }
                binding.textData.text = sb.toString()
            }

        }
    }


    private fun savePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        try {
            personCollectionRef.add(person).await()
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity, "Add Successfully", Toast.LENGTH_SHORT).show()
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun retrievePersons() = CoroutineScope(Dispatchers.IO).launch {
        val fromAge = binding.editNumber1.text.toString().toInt()
        val toAge = binding.editNumber2.text.toString().toInt()
        try {
            val querySnapshot = personCollectionRef
                .whereGreaterThan("age",fromAge)
                .whereLessThan("age",toAge)
                .orderBy("age")
                .get()
                .await()

            val sb = StringBuilder()

            for (document in querySnapshot) {
                val person = document.toObject<Person>()
                Log.d(TAG, "subscribeToRealtimeUpdates: " + person.firstName)
                sb.append("${person}\n")
            }

            withContext(Dispatchers.Main){
                binding.textData.text = sb.toString()
            }

        }catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity,e.message.toString(),Toast.LENGTH_SHORT).show()
            }
        }
    }
}