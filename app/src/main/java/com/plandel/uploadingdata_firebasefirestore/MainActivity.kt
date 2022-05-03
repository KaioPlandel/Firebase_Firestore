package com.plandel.uploadingdata_firebasefirestore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.plandel.uploadingdata_firebasefirestore.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.StringBuilder
import kotlin.Exception

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
            val person = getOldPerson()
            savePerson(person)
        }

        binding.buttonRetrieve.setOnClickListener {
            retrievePersons()
        }

        binding.buttonUpdate.setOnClickListener {
            val oldPerson = getOldPerson()
            val newPersonMap = getNewPersonMap()
            updatePerson(oldPerson,newPersonMap)
        }

        binding.buttonDelete.setOnClickListener {
            val person = getOldPerson()
            deletePerson(person)
        }
    }

    private fun getNewPersonMap(): Map<String, Any> {
        val firstName = binding.editNewFirstName.text.toString()
        val lastName = binding.editNewLastName.text.toString()
        val age = binding.editNewAge.text.toString()
        val map = mutableMapOf<String, Any>()

        if (firstName.isNotEmpty()) {
            map["firstName"] = firstName
        }
        if(lastName.isNotEmpty()) {
            map["lastName"] = lastName
        }
        if(age.isNotEmpty()){
            map["age"] = age.toInt()
        }
        return map
    }

    private fun updatePerson(person: Person, newPersonMap: Map<String,Any>) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = personCollectionRef
            .whereEqualTo("firstName",person.firstName)
            .whereEqualTo("lastName", person.lastName)
            .whereEqualTo("age", person.age)
            .get()
            .await()
        if(personQuery.documents.isNotEmpty()) {
            for (document in personQuery){
                try {
                    personCollectionRef.document(document.id).set(
                        newPersonMap,
                        SetOptions.merge()
                    ).await()
                }catch (e: Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity,e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }else {
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity,"No person matched the query", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deletePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = personCollectionRef
            .whereEqualTo("firstName",person.firstName)
            .whereEqualTo("lastName",person.lastName)
            .whereEqualTo("age",person.age)
            .get()
            .await()
        if(personQuery.documents.isNotEmpty()){
            for (document in personQuery){
                try {
                    personCollectionRef.document(document.id).delete().await()
//                    personCollectionRef.document(document.id).update(mapOf(
//                        "firstName" to FieldValue.delete()
//                    ))
                }catch (e: Exception){
                    Toast.makeText(this@MainActivity,"No person matched the query",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getOldPerson(): Person{
        val firstName = binding.editFirstName.text.toString()
        val lastName = binding.editLastName.text.toString()
        val age = Integer.parseInt(binding.editAge.text.toString())
        return Person(firstName,lastName,age)
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